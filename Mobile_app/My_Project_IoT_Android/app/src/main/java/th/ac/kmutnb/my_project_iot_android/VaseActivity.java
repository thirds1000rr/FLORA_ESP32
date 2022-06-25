package th.ac.kmutnb.my_project_iot_android;

import androidx.appcompat.app.AppCompatActivity;


import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class VaseActivity extends AppCompatActivity {
    private final String BASE_URL = "https://1bd4-115-87-212-76.ngrok.io";
    //private final String BASE_URL = "http://itpart.com/android/json/";
    private final String TAG = "my_app";
    private static final String PREF_NAME = "MyPref";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vase);
        Intent itn = getIntent();
        String recID = itn.getStringExtra("recID");
        Log.d(TAG, String.valueOf(recID));
        String urlStr = BASE_URL + "/Android/Sensor/" +recID; /// path for sent req.

        JsonObjectRequest jsObjReq = new JsonObjectRequest(Request.Method.GET, urlStr, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Boolean StatusOnOff,StatusAuto;
                        String Temp,Humid,ID;// if OK
                        try {
                            Switch SensorOn_off = findViewById(R.id.switch2);
                            Switch SensorAuto_Manual = findViewById(R.id.switch3);
                            StatusOnOff = response.getBoolean("StatusOnOff");   ///SHOW
                            StatusAuto = response.getBoolean("StatusAuto");
                            Temp = response.getString("Temp");
                            Humid = response.getString("Humid");
                            ID = response.getString("Id");
                            TextView tv1 = findViewById(R.id.textView4);
                            tv1.setText("SensorID:  "+ID);
                            TextView tv2 = findViewById(R.id.textView5);
                            tv2.setText("Temp:   "+Temp);
                            TextView tv3 = findViewById(R.id.textView6);
                            tv3.setText("Humid:  "+Humid);                      ///TIL HERE
                            ////Set switch state
                            if(StatusAuto){
                                SensorAuto_Manual.setChecked(true);
                                Log.i(TAG, "StatusAuto=");
                            }
                            else if(StatusAuto){
                                SensorAuto_Manual.setChecked(false);
                                Log.i(TAG, "StatusAuto2=");
                            }
                            if(StatusOnOff){
                                SensorOn_off.setChecked(true);
                                Log.i(TAG, "StatusAuto=");
                            }
                            else if(StatusOnOff){
                                SensorOn_off.setChecked(false);
                                Log.i(TAG, "StatusAuto2=");
                            }
                            //////////
                            ///////// catch event switch button and update to webserver
                            SensorAuto_Manual.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                @Override
                                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                                    if(SensorAuto_Manual.isChecked()){
                                        UpdateStatusAuto_Manual("ON");   ////sent data to webserver
                                    }
                                    else{
                                        UpdateStatusAuto_Manual("OFF");
                                    }
                                }
                            });
                            SensorOn_off.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                @Override
                                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                                    if(SensorOn_off.isChecked()){
                                        UpdateStatusON_Off("ON");   ////sent data to webserver
                                    }
                                    else{
                                        UpdateStatusON_Off("OFF");
                                    }
                                }
                            });
                        } catch (JSONException e) {
                            Log.d(TAG, e.getMessage());
                            e.printStackTrace();
                        }
                        //Log.i(TAG, response.toString());

                        //pDialog.hide();
                    }
                },
                new Response.ErrorListener() {  // if Error
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Error handling
                        //pDialog.hide();
                        Log.i(TAG, "onErrorResponse: ");
                    }
                });

        // Add the request to the queue
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(jsObjReq);
        Log.i(TAG,urlStr);
    }

    public void UpdateStatusON_Off(String Status){          /////function update
        Intent itn = getIntent();
        String recID = itn.getStringExtra("recID");
        String URL_SetStatus = BASE_URL+"/IoT/Sensor/"+String.valueOf(recID)+"/StatusOnOff/"+Status;
        JsonObjectRequest jsObjReq = new JsonObjectRequest(Request.Method.GET, URL_SetStatus, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {  // if OK
                        String Status;
                        int id;
                        try {

                            Status = response.getString("message");
                            Log.i(TAG, "onResponse: "+Status);

                        } catch (JSONException e) {
                            Log.d(TAG, e.getMessage());
                            e.printStackTrace();
                        }
                        //Log.i(TAG, response.toString());

                        //pDialog.hide();
                    }
                },
                new Response.ErrorListener() {  // if Error
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Error handling
                        //pDialog.hide();
                        Log.i(TAG, "onErrorResponse: ");
                    }
                });

        // Add the request to the queue
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(jsObjReq);
    }
    public void UpdateStatusAuto_Manual(String Status){   ///function update
        Intent itn = getIntent();
        String recID = itn.getStringExtra("recID");
        String URL_SetStatus = BASE_URL+"/IoT/Sensor/"+String.valueOf(recID)+"/SensorAuto_Manual/"+Status;
        JsonObjectRequest jsObjReq = new JsonObjectRequest(Request.Method.GET, URL_SetStatus, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {  // if OK
                        String Status;
                        int id;
                        try {

                            Status = response.getString("message");
                            Log.i(TAG, "onResponse: "+Status);

                        } catch (JSONException e) {
                            Log.d(TAG, e.getMessage());
                            e.printStackTrace();
                        }
                        //Log.i(TAG, response.toString());

                        //pDialog.hide();
                    }
                },
                new Response.ErrorListener() {  // if Error
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Error handling
                        //pDialog.hide();
                        Log.i(TAG, "onErrorResponse: ");
                    }
                });

        // Add the request to the queue
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(jsObjReq);
    }
    public void DeleteSensor(View v){                                       /////sent data  sensor_id , user_id for delete sensor
        SharedPreferences pref = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        String Userid = pref.getString("Userid", "no record");
        Intent itn = getIntent();
        String recID = itn.getStringExtra("recID");
        String URL_SetStatus = BASE_URL+"/DeleteSensor/"+String.valueOf(recID)+"/"+Userid;
        JsonObjectRequest jsObjReq = new JsonObjectRequest(Request.Method.GET, URL_SetStatus, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {  // if OK
                        String Status;
                        int id;
                        try {

                            Status = response.getString("message");
                            Log.i(TAG, "onResponse: "+Status);

                        } catch (JSONException e) {
                            Log.d(TAG, e.getMessage());
                            e.printStackTrace();
                        }
                        //Log.i(TAG, response.toString());

                        //pDialog.hide();
                    }
                },
                new Response.ErrorListener() {  // if Error
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Error handling
                        //pDialog.hide();
                        Log.i(TAG, "onErrorResponse: ");
                    }
                });

        // Add the request to the queue
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(jsObjReq);
        Intent itn2 = new Intent(this,MainActivity.class);
        startActivity(itn2);
    }
}