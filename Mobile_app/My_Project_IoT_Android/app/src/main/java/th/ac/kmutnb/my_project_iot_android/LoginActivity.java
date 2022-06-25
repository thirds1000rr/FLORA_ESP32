package th.ac.kmutnb.my_project_iot_android;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {
    private final String BASE_URL = "https://1bd4-115-87-212-76.ngrok.io";
    private final String TAG = "my_app";
    private static final String PREF_NAME = "MyPref";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }
    protected void onStart() {
        super.onStart();
        SharedPreferences pref = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        Boolean SessionKey = pref.getBoolean("SessionKey",false);
        if(SessionKey){
            moveToMainActivity();
        }
    }
    private void moveToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
    }
    public void ButtonRegister_Login(View v){
        Intent itn2 = new Intent(this,RegisterActivity.class);
        startActivity(itn2);
    }

    public void ButtonOK_Login(View v){
        EditText et1 = findViewById(R.id.UsernameETLogin);
        EditText et2 = findViewById(R.id.PasswordETLogin);
        String UsernameETLogin = et1.getText().toString();
        String PasswordETLogin = et2.getText().toString();

        String URL_SetStatus = BASE_URL+"/Login"+"/"+UsernameETLogin+"/"+PasswordETLogin;
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
                                SharedPreferences pref = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
                                SharedPreferences.Editor editor = pref.edit();
                                editor.putString("Username", Username);
                                editor.putString("Userid", Userid);
                                editor.putBoolean("SessionKey",true);
                                editor.apply();
//                                itn2.putExtra("Username", Username);
//                                itn2.putExtra("Userid",Userid);
                                Log.i(TAG, Userid);
                                moveToMainActivity();
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
        //startActivity(itn2);
    }
}