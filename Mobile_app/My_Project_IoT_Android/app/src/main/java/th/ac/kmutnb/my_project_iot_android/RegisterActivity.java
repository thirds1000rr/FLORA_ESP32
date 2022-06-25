package th.ac.kmutnb.my_project_iot_android;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
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

public class RegisterActivity extends AppCompatActivity {
    private final String BASE_URL = "https://1bd4-115-87-212-76.ngrok.io";
    private final String TAG = "my_app";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
    }
    public void ButtonOK_Register(View v){
        EditText et1 = findViewById(R.id.UsernameETRegister);       ///Username
        EditText et2 = findViewById(R.id.PasswordETRegister);       ///Password
        EditText et3 = findViewById(R.id.EmailETRegister);          ///Email
        String UsernameETRegister = et1.getText().toString();
        String PasswordETRegister= et2.getText().toString();
        String EmailETRegister = et3.getText().toString();
        Intent itn2 = new Intent(this,LoginActivity.class);
        String URL_SetStatus = BASE_URL+"/Register"+"/"+UsernameETRegister+"/"+PasswordETRegister+"/"+EmailETRegister;////Path
        JsonObjectRequest jsObjReq = new JsonObjectRequest(Request.Method.GET, URL_SetStatus, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {  // if OK ///Show in log cat
                        String Status;
                        int id;
                        try {

                            Status = response.getString("message");
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