package th.ac.kmutnb.my_project_iot_android;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private List<Data> datas = new ArrayList<>();
    private static final String TAG = "my_app";
    public static final String REQUEST_TAG = "myrequest";
    private RequestQueue mQueue;
    private static final String PREF_NAME = "MyPref";
    //ProgressDialog pDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences pref = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        String Userid = pref.getString("Userid", "no record");
        if(Userid!="-1"){                                               //Check in shared preference if have userid
            String BASE_URL = "https://1bd4-115-87-212-76.ngrok.io";
            String urlStr = BASE_URL+"/ShowListView/User/"+Userid;
            JsonArrayRequest jsRequest = new JsonArrayRequest(Request.Method.GET, urlStr, null, ///
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {

                            Gson gson = new Gson();

                            JSONObject jsObj;   // = null;
                            for (int i=0; i < response.length(); i++ ) {
                                try {
                                    jsObj = response.getJSONObject(i);
                                    String title = jsObj.getString("ID");
                                    String title2 = jsObj.getString("Zone");
                                    Log.d(TAG, title);
                                    datas.add(new Data(title,title2)); ///Loop add data to listview
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            if (datas.size() > 0){                      ///Check data in listview if more than 0 show
                                displayListview();
                            }

                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d(TAG,error.toString());
                            Toast.makeText(getBaseContext(),error.toString(),Toast.LENGTH_SHORT).show();
                        }
                    });  // Request

            mQueue = Volley.newRequestQueue(this);
            jsRequest.setTag(REQUEST_TAG);
            mQueue.add(jsRequest);
        }


//        Data item = new Data("xiaomi flora 4 in 1 , solenoid valve","Vase1");
//        datas.add(item);
//        datas.add(new Data("xiaomi flora 4 in 1 , solenoid valve","Vase2"));
//
//
//        ListView lv = findViewById(R.id.listView);
//        MyAdapter adapter = new MyAdapter(this,datas);
//        lv.setAdapter(adapter);
//        lv.setOnItemClickListener(this);

    }
    public void displayListview(){
        MyAdapter adapter = new MyAdapter(this,datas);
        ListView lv = findViewById(R.id.listView);
        lv.setOnItemClickListener(this);
        lv.setAdapter(adapter);
    }
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Intent itn = new Intent(this,VaseActivity.class);
        Log.i("MyApp", String.valueOf(i));
        Log.i("MyApp", datas.get(i).getmText2());
        itn.putExtra("recID", datas.get(i).getmText1()); ///send data to VaseActivity.class ID sensor
//        Toast.makeText(this, (i) + " " + datas.get(i).getmText1(), Toast.LENGTH_SHORT).show();
//        itn.putExtra("id",i);
//        itn.putExtra("ProductName",datas.get(i).getmText1());
        startActivity(itn);
    }
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return super.onCreateOptionsMenu(menu);
    }
    public boolean onOptionsItemSelected(@NonNull MenuItem item){
        switch (item.getItemId()){
            case R.id.profile:
                Intent itn2 = new Intent(this,ProfileActivity.class); /// Button to profile Activity
                startActivity(itn2);
                return true;
            case R.id.AddSensor:
                Intent itn3 = new Intent(this,AddSensorActivity.class); //// Button add sensorActivity
                startActivity(itn3);
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}