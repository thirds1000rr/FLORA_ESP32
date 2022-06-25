package th.ac.kmutnb.my_project_iot_android;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class AddSensorActivity extends AppCompatActivity {
    private final String BASE_URL = "https://1bd4-115-87-212-76.ngrok.io";
    private static final String TAG = "my_app";
    private static final String PREF_NAME = "MyPref";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_sensor);
    }

    public void ButtonOK_AddSensor(View v){
        EditText et1 = findViewById(R.id.ZoneETAddsensor);
        String ZoneETAddsensor = et1.getText().toString();
        SharedPreferences pref = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        String Userid = pref.getString("Userid", "no record");

        Intent itn2 = new Intent(this,MainActivity.class);
        String URL_SetStatus = BASE_URL+"/CreateSensor/"+ZoneETAddsensor+"/"+Userid;
        JsonObjectRequest jsObjReq = new JsonObjectRequest(Request.Method.GET, URL_SetStatus, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {  // if OK
                        Boolean Status;
                        String Username,Userid;
                        int id;
                        try {
                            Username =  response.getString("Username");
                            Userid = response.getString("Userid");
                            Status = response.getBoolean("message");
                            if (Status){
                                itn2.putExtra("Username", Username);
                                itn2.putExtra("Userid",Userid);
                                Log.i(TAG, Userid);
                                startActivity(itn2);
                            }
                            else{
                                Log.i(TAG, "onResponse: "+Status);
                            }
                            Log.i(TAG, "onResponse: "+Status);

                        } catch (JSONException e) {
                            Log.d(TAG, e.getMessage());
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {  // if Error
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.i(TAG, "onErrorResponse: ");
                    }
                });

        // Add the request to the queue
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(jsObjReq);
        startActivity(itn2);
    }


}