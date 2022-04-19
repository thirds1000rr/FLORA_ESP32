char* FLORA_DEVICES[] = { "80:EA:CA:60:05:05" };
#define SLEEP_DURATION 5
#define BATTERY_INTERVAL 6
#define RETRY 3
#include "BLEDevice.h"
#include "config.h"
#include <Adafruit_LiquidCrystal.h>
#include <LiquidCrystal_I2C.h>
#include <PubSubClient.h>
#include <HTTPClient.h>
#include <ArduinoJson.h>
#include <Wire.h>
#include <WiFi.h>
#define ssid "87_90 Home"
#define password "024475319"
#define led_Power 14

String temp , humid , light , coduc , battery_status;  ////temp = tempurature , humid = moisture , Light = light , conduc = Conductivity อััตราการนำไไฟฟ้าในดิน
int buzzer = 15 ; ///buzzer



RTC_DATA_ATTR int bootCount = 0;
LiquidCrystal_I2C lcd(0x27, 16, 2);
void Screen_Normal(){
  lcd.clear();
  lcd.setCursor(0,0);
  lcd.print("TEMP "+temp+char(223)+"C");
  lcd.setCursor(0,1);
  lcd.print("MOISTURE "+humid+" %");
  delay(3000);
  }
// device count
static int deviceCount = sizeof FLORA_DEVICES / sizeof FLORA_DEVICES[0];

// the remote service we wish to connect to
static BLEUUID serviceUUID("00001204-0000-1000-8000-00805f9b34fb");

// the characteristic of the remote service we are interested in
static BLEUUID uuid_version_battery("00001a02-0000-1000-8000-00805f9b34fb");
static BLEUUID uuid_sensor_data("00001a01-0000-1000-8000-00805f9b34fb");
static BLEUUID uuid_write_mode("00001a00-0000-1000-8000-00805f9b34fb");

BLEClient* getFloraClient(BLEAddress floraAddress) {
  BLEClient* floraClient = BLEDevice::createClient();

  if (!floraClient->connect(floraAddress)) {
    Serial.println("- Connection failed, skipping");
    return nullptr;
  }

  Serial.println("- Connection successful");
  return floraClient;
}
BLERemoteService* getFloraService(BLEClient* floraClient) {
  BLERemoteService* floraService = nullptr;

  try {
    floraService = floraClient->getService(serviceUUID);
  }
  catch (...) {
    // something went wrong
  }
  if (floraService == nullptr) {
    Serial.println("- Failed to find data service");  
    lcd.clear();
    lcd.setCursor(0,0);
    lcd.println("FLORA ERROR");
    delay(3000);
    lcd.clear();
    lcd.setCursor(0,0);
    lcd.println("RESTART...");
    delay(2000);
    ESP.restart();
    
  }
  else {
    Serial.println("- Found data service");
  }

  return floraService;
} 

bool forceFloraServiceDataMode(BLERemoteService* floraService) {
  BLERemoteCharacteristic* floraCharacteristic;
  
  // get device mode characteristic, needs to be changed to read data
  Serial.println("- Force device in data mode");
  floraCharacteristic = nullptr;
  try {
    floraCharacteristic = floraService->getCharacteristic(uuid_write_mode);
  }
  catch (...) {
    // something went wrong
  }
  if (floraCharacteristic == nullptr) {
    Serial.println("-- Failed, skipping device");
    return false;
  }

  // write the magic data
  uint8_t buf[2] = {0xA0, 0x1F};
  floraCharacteristic->writeValue(buf, 2, true);

  delay(500);
  return true;
}

bool readFloraDataCharacteristic(BLERemoteService* floraService, String baseTopic) {
  BLERemoteCharacteristic* floraCharacteristic = nullptr;

  // get the main device data characteristic
  Serial.println("- Access characteristic from device");
  try {
    floraCharacteristic = floraService->getCharacteristic(uuid_sensor_data);
  }
  catch (...) {
    // something went wrong
  }
  if (floraCharacteristic == nullptr) {
    Serial.println("-- Failed, skipping device");
    return false;
  }

  // read characteristic value
  Serial.println("- Read value from characteristic");
  std::string value;
  try{
    value = floraCharacteristic->readValue();
  }
  catch (...) {
    // something went wrong
    Serial.println("-- Failed, skipping device");
    return false;
  }
  const char *val = value.c_str();

  Serial.print("Hex: ");
  for (int i = 0; i < 16; i++) {
    Serial.print((int)val[i], HEX);
    Serial.print(" ");
  }
  Serial.println(" ");

  uint16_t temperature = (val[0] + val[1]*256)/10;
  Serial.print("-- Temperature: ");
  Serial.println(temperature);
  temp = String(temperature);
  
  uint8_t moisture = val[7];
  Serial.print("-- Moisture: ");
  Serial.println(moisture);
  humid = String(moisture);
  


  uint16_t light = val[3] + val[4] *256;
  Serial.print("-- Light: ");
  Serial.println(light);
 
  int conductivity = val[8] + val[9] * 256;
  Serial.print("-- Conductivity: ");
  Serial.println(conductivity);

  if (temperature > 200) {
    Serial.println("-- Unreasonable values received, skip publish");
    return false;
  }
  return true;
}

bool readFloraBatteryCharacteristic(BLERemoteService* floraService, String baseTopic) {
  BLERemoteCharacteristic* floraCharacteristic = nullptr;

  // get the device battery characteristic
  Serial.println("- Access battery characteristic from device");
  try {
    floraCharacteristic = floraService->getCharacteristic(uuid_version_battery);
  }
  catch (...) {
    // something went wrong
  }
  if (floraCharacteristic == nullptr) {
    Serial.println("-- Failed, skipping battery level");
    return false;
  }

  // read characteristic value
  Serial.println("- Read value from characteristic");
  std::string value;
  try{
    value = floraCharacteristic->readValue();
  }
  catch (...) {
    // something went wrong
    Serial.println("-- Failed, skipping battery level");
    return false;
  }
  const char *val2 = value.c_str();
  int battery = val2[0];

  char buffer[64];
  Serial.print("-- Battery: ");
  Serial.println(battery);
  snprintf(buffer, 64, "%d", battery);
  lcd.clear();
  lcd.setCursor(0,0);
  lcd.println("battery flora:"+String(battery)+"%");
  //client.publish((baseTopic + "battery").c_str(), buffer);

  return true;
}

bool processFloraService(BLERemoteService* floraService, char* deviceMacAddress, bool readBattery) {
  // set device in data mode
  if (!forceFloraServiceDataMode(floraService)) {
    return false;
  }

  String baseTopic = deviceMacAddress;
  bool dataSuccess = readFloraDataCharacteristic(floraService, baseTopic);

  bool batterySuccess = true;
  if (readBattery) {
    batterySuccess = readFloraBatteryCharacteristic(floraService, baseTopic);
  }
   
  return true;
}

bool processFloraDevice(BLEAddress floraAddress, char* deviceMacAddress, bool getBattery, int tryCount) {
  Serial.print("Processing Flora device at ");
  Serial.print(floraAddress.toString().c_str());
  Serial.print(" (try ");
  Serial.print(tryCount);
  Serial.println(")");

  // connect to flora ble server
  BLEClient* floraClient = getFloraClient(floraAddress);
  if (floraClient == nullptr) {
    return false;
  }

  // connect data service
  BLERemoteService* floraService = getFloraService(floraClient);
  if (floraService == nullptr) {
    floraClient->disconnect();
    return false;
  }

  // process devices data
  bool success = processFloraService(floraService, deviceMacAddress, getBattery);

  // disconnect from device
  floraClient->disconnect();

  return success;
}

//void hibernate() {
//  esp_sleep_enable_timer_wakeup(SLEEP_DURATION * 1000000ll);
//  Serial.println("Going to sleep now.");
//  delay(100);
//  esp_deep_sleep_start();
//}
void buzzerSound(){
  digitalWrite(buzzer, HIGH);
  delay(500);
  digitalWrite(buzzer, LOW);
  delay(500);
}
void setup_wifi() {
  delay(2000);
  Serial.println();
  Serial.print("Connecting to ");
  Serial.println(ssid);
  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("");
  Serial.println("WiFi connected");
  Serial.println("IP address: ");
  Serial.println(WiFi.localIP());
}
//void hibernate() {
//  esp_sleep_enable_timer_wakeup(SLEEP_DURATION * 1000000ll);
//  Serial.println("Going to sleep now.");
//  delay(100);
//  esp_deep_sleep_start();
//}
void FLORA_GET(){
  //////FLORA SETUP
  bootCount++;
  Serial.println("Initialize BLE client...");
  BLEDevice::init("");
  BLEDevice::setPower(ESP_PWR_LVL_P7);

  // check if battery status should be read - based on boot count
  bool readBattery = ((bootCount % BATTERY_INTERVAL) == 0);

  // process devices
  for (int i=0; i<deviceCount; i++) {
    int tryCount = 0;
    char* deviceMacAddress = FLORA_DEVICES[i];
    BLEAddress floraAddress(deviceMacAddress);

    while (tryCount < RETRY) {
      tryCount++;
      if (processFloraDevice(floraAddress, deviceMacAddress, readBattery, tryCount)) {
        break;
      }
      delay(1000);
    }
    delay(1500);
  }  
}
//void auto_sensor(String temp , String humid){
//  if(temp >= 30 or humid <= ){
//    }
//  }
void Pump_on(){
     lcd.clear();
     lcd.setCursor(0,0);
     lcd.println("REMOTE FROM APP");
     lcd.setCursor(0,1);
     lcd.println("PUMP = ON");
     buzzerSound();
  }
void lcd_cannot_conected_server(String code){
  lcd.clear();
  lcd.setCursor(0,0);
  lcd.println("PLEASE CHECK");
  lcd.setCursor(0,1);
  lcd.println("HTTP CODE:"+code);
  }
void setup() {
  Serial.begin(115200);
  
  //WIFI BEGIN
  setup_wifi();
  
  lcd.init(); // initialize the lcd
  lcd.backlight();
  lcd.clear();
  lcd.setCursor(0,0);
  lcd.println("Connecting");
  lcd.setCursor(0,1);
  lcd.println(ssid);
  delay(3000);
  lcd.clear();
  lcd.setCursor(0,0);
  lcd.print("WAIT...");
  delay(2000);
  pinMode(buzzer, OUTPUT);
  pinMode(led_Power , OUTPUT);
  //////////
}

void loop() {
  FLORA_GET();
  Screen_Normal();
//////////URL PATH ////////
  String url ="http://ee23-115-87-212-76.ngrok.io/IoT/Sensor/";
  url = url+temp ;
  url = url + "/" ;
  url = url+humid;
  url = url+"/" ;
  url = url+"1" ; 
////////////////////////////////////////
  HTTPClient http;
  http.useHTTP10(true);
  http.begin(url);
  int httpCode = http.GET();
  digitalWrite(buzzer , HIGH);
  digitalWrite(led_Power , HIGH);
  if (httpCode == 200) {
    String content = http.getString();
    Serial.println("Content ---------");
    Serial.println(content);
    Serial.println("-----------------");
    
    /////JSON parsing
    StaticJsonDocument<200> doc;
    DeserializationError error = deserializeJson(doc, content);
  // Test if parsing succeeds.
    if (error) {
    Serial.print(F("deserializeJson() failed: "));
    Serial.println(error.f_str());
    return;
    }
    const String sensor = doc["StatusAuto"];
    const String status1 = doc["StatusOnOff"];
    ///READ STATUS
      if(status1 == "OFF" && sensor == "ON"){
       ////auto_sensor(int(temp , humid);
       int(temp);
        }
      else if(status1 == "ON" && sensor == "OFF"){
        Pump_on();
        }
  }else {
    Serial.println("Fail. error code " + String(httpCode));
    Serial.println("Link URL : "+url);
    Serial.println("Get content from " + url);
    lcd_cannot_conected_server(String(httpCode));
    }
    delay(10000);
}
  
