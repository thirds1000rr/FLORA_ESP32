from email.mime import image
from xmlrpc.client import Boolean
from flask import Flask,jsonify
import sqlite3
from flask_login import LoginManager,login_required,UserMixin,logout_user,current_user,login_user
from flask_sqlalchemy import SQLAlchemy#ORM
from werkzeug.security import generate_password_hash,check_password_hash
from line_notify import LineNotify

ACCESS_TOKEN = "5wMagAFZfVvwNzkqTNgZ5iD7j3pAEOdEkB81FtUFW1i"

notify = LineNotify(ACCESS_TOKEN)

app = Flask(__name__)

login_manager = LoginManager()
login_manager.init_app(app)
db = SQLAlchemy(app)
app.config['SECRET_KEY'] = 'asasddaadsdasad'
app.config['SQLALCHEMY_DATABASE_URI'] = 'sqlite:///TempAndHumid.db'

class User(UserMixin,db.Model): 
    id = db.Column(db.Integer,unique=True,primary_key=True)
    username = db.Column(db.String(15),unique=True)
    email = db.Column(db.String(50),unique=True)
    password = db.Column(db.String(80))
    sensor_id = db.Column(db.String(80))

@login_manager.user_loader
def load_user(id):
    return User.query.get(int(id))

@app.route("/")
def HelloWorld():
    i = 20
    notify.send("Test int i\n"+str(i),image_path='C:\\Users\\vittapong\\Pictures\\Camera Roll\\panda.jpg')
    return jsonify({"message":"success"})


@app.route("/IoT/Sensor/<temp>/<humid>/<ID>")
def Sensor1(temp,humid,ID):
    with sqlite3.connect("TempAndHumid.db") as con:
        curr = con.cursor()
        sql_cmd ="""
        UPDATE SENSOR SET Temp=?,Humid=? WHERE Id=?;
        """
        curr.execute(sql_cmd,(temp,humid,ID))
        con.commit()
        sql_cmd = """
        SELECT * FROM Sensor WHERE Id == ?;
        """
        data = [i for i in curr.execute(sql_cmd,(ID,))][0]
        if(data[1]>30):
            notify.send("SensorID:"+str(data[0])+"\n"+"Zone:"+str(data[5])+"\n"+"Temp:"+str(data[1])+"\n"+"Humid:"+str(data[2]),image_path='C:\\Users\\vittapong\\Pictures\\Camera Roll\\panda.jpg')
        

    return jsonify({"StatusOnOff":data[3],"StatusAuto":data[4]})

#from VaseActivity
@app.route("/Android/Sensor/<ID>")
def ShowSensor1(ID):
    with sqlite3.connect("TempAndHumid.db") as con:
        curr = con.cursor()
        sql_cmd = """
        SELECT * FROM Sensor WHERE Id == ?;
        """
        data = [i for i in curr.execute(sql_cmd,(ID,))][0]          #excute sql command get data from database
        if(data[3] == "ON"):
            StatusOnOff = True
        else:
            StatusOnOff = False
        if(data[4] == "ON"):
            StatusAuto = True
        else:
            StatusAuto = False
        send_data = {'Id':data[0],'Temp':data[1],'Humid':data[2],'StatusOnOff':StatusOnOff,'StatusAuto':StatusAuto} #create Dictionary

    return jsonify(send_data) #return data to Android in JSON type

@app.route("/IoT/Sensor/<ID>/StatusOnOff/<Status>")
def UpdateStatusOnOff(ID,Status):
    with sqlite3.connect("TempAndHumid.db") as con:
        curr = con.cursor()
        sql_cmd ="""
        UPDATE SENSOR SET StatusOnOff=? WHERE Id=?;
        """
        curr.execute(sql_cmd,(Status,ID))
        con.commit()

    return jsonify({"message":"success"})

@app.route("/IoT/Sensor/<ID>/SensorAuto_Manual/<Status>")
def UpdateStatusSensorAuto_Manual(ID,Status):
    with sqlite3.connect("TempAndHumid.db") as con:
        curr = con.cursor()
        sql_cmd ="""
        UPDATE SENSOR SET StatusAuto=? WHERE Id=?;
        """
        curr.execute(sql_cmd,(Status,ID))
        con.commit()

    return jsonify({"message":"success"})


#Register
@app.route("/Register/<Username>/<Password>/<Email>")
def InsertUser(Username,Password,Email):
    hashed_password = generate_password_hash(Password,method='sha256')

    with sqlite3.connect("TempAndHumid.db") as con:
        curr = con.cursor()
        sql_cmd = """
        INSERT INTO user(username,email,password) VALUES(?,?,?);
        """
        curr.execute(sql_cmd,(Username,Email,hashed_password))
        con.commit()
    return "Hello"

#Login
@app.route("/Login/<Username>/<password>")                                  
def Login(Username,password):
    user = User.query.filter_by(username=Username).first() #หาข้อมูลจากตาราง user ที่มี username 
    if user:                                               #Check
        #print(user[1],user[2],user[3])
        print(user.id)                                     
        if check_password_hash(user.password,password):    #Check User , Password
            login_user(user,False)                      #Login  
            notify.send("LoginSuccessfully")        #Notify Line
            return jsonify({"message":True,"Username":user.username,"Userid":user.id}) #Return True Username and User ID

    return jsonify({"message":"False"})             #Return False


#Show listview
@app.route("/ShowListView/User/<ID>")
def ShowListView(ID):
    with sqlite3.connect("TempAndHumid.db") as con: #coonect to data base
        curr = con.cursor()     #create cursor
        sql_cmd = """
        SELECT sensor_id FROM user WHERE id==?;        
        """
        sensor_pack = [i for i in curr.execute(sql_cmd,(ID,))][0][0]    #ดึงข้อมูลจากตาราง user column sensor_id ที่มี id ตรงกับที่ส่งมา 
        sensor_list = sensor_pack.split(" ")   #แยกข้อมูลจากสตริงเป็น list
        print(sensor_pack)  
        print(sensor_list)
        # args=[1,2]
        sql="SELECT * FROM sensor WHERE id IN ({seq})".format(seq=','.join(['?']*len(sensor_list))) #เรียกข้อมูลทุกตัวจากตาราง Sensor ที่มี id เก็บอยู่ใน sensor_id ทั้งหมด
        curr.execute(sql, sensor_list)
        
        send_data = curr.fetchall() #เอาข้อมูลที่มีออกมาแสดงทั้งหมด
        insertObject = []
        columnNames = [column[0] for column in curr.description]
        for record in send_data:
            insertObject.append( dict( zip( columnNames , record ) ) ) #เอาข้อมูลที่ได้มาแสดงทั้งหมดในรูปแบบของ Dictionary

    return jsonify(insertObject)
#add_sensor
@app.route("/CreateSensor/<Zone>/<user_id>")
def create_sensor(Zone,user_id):

    with sqlite3.connect("TempAndHumid.db") as con:
        curr = con.cursor()
        sql_cmd ="""
        INSERT INTO SENSOR(Zone) VALUES(?);
        """
        curr.execute(sql_cmd,(Zone,))
        con.commit()

    with sqlite3.connect("TempAndHumid.db") as con:
        curr = con.cursor()
        sql_cmd= """
        SELECT MAX(ID) AS LargestID FROM Sensor;
        """
        curr.execute(sql_cmd)
        send_data = curr.fetchall()
        insertObject = []
        columnNames = [column[0] for column in curr.description]
        for record in send_data:
            insertObject.append( dict( zip( columnNames , record ) ) )

    with sqlite3.connect("TempAndHumid.db") as con:
        curr = con.cursor()
        sql_cmd = """
        
        SELECT sensor_id FROM user Where id==?;
        """
        
        sensor_id_old_list = [i for i in curr.execute(sql_cmd,(user_id,))][0][0]
        if(sensor_id_old_list):
            sensor_id_old_list = [i for i in curr.execute(sql_cmd,(user_id,))][0][0].split(" ") + [str(insertObject[0]['LargestID'])]
            print("have")
        else:
            print("none")
            
    with sqlite3.connect("TempAndHumid.db") as con:
        curr = con.cursor()
        sql_cmd = """
        
        UPDATE user SET sensor_id = ? WHERE id==?; 
        """    
        if(sensor_id_old_list):
            sensor_new_text = " ".join(sensor_id_old_list)
            curr.execute(sql_cmd,(sensor_new_text,user_id))
            print("have")
        else:
            print("none")
            curr.execute(sql_cmd,(insertObject[0]['LargestID'],user_id,))

        con.commit()

    return "User ID : {} Sensor_id_old = {} Sensor_id_new = {}".format(user_id,sensor_id_old_list,sensor_id_old_list)

#funtion vase_delete
@app.route("/DeleteSensor/<ID>/<user_id>")
def Delete_Sensor(ID,user_id):

    with sqlite3.connect("TempAndHumid.db") as con:
        curr = con.cursor()
        sql_cmd ="""
        DELETE FROM Sensor WHERE ID == ?;
        """
        curr.execute(sql_cmd,(ID,))
        con.commit()

    with sqlite3.connect("TempAndHumid.db") as con:
        curr = con.cursor()
        sql_cmd = """
        
        SELECT sensor_id FROM user Where id==?;
        """
        
        sensor_id_old_list = [i for i in curr.execute(sql_cmd,(user_id,))][0][0]
        if(sensor_id_old_list):
            sensor_id_old_list = [i for i in curr.execute(sql_cmd,(user_id,))][0][0].split(" ")
            sensor_id_old_list.remove(ID)
            print("have")
        else:
            print("none")
        
    with sqlite3.connect("TempAndHumid.db") as con:
        curr = con.cursor()
        sql_cmd = """
        
        UPDATE user SET sensor_id = ? WHERE id==?; 
        """    
  
        sensor_new_text = "".join(sensor_id_old_list)
        curr.execute(sql_cmd,(sensor_new_text,user_id))

        con.commit()
    return "User ID : {} Sensor_id_old = {} Sensor_id_new = {}".format(user_id,sensor_id_old_list,sensor_id_old_list)

#port
if __name__ == '__main__':
    app.run(port=5000,debug=True)
