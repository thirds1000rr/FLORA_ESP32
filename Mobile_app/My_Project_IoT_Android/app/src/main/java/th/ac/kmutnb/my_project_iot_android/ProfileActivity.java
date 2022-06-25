package th.ac.kmutnb.my_project_iot_android;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class ProfileActivity extends AppCompatActivity {
    private static final String PREF_NAME = "MyPref";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        SharedPreferences pref = getSharedPreferences(PREF_NAME, MODE_PRIVATE); ///Setup
        SharedPreferences.Editor editor = pref.edit();
        String Username = pref.getString("Username", "no record"); ///Get string username from shared preference
//        Intent itn = getIntent();
//        String Username = itn.getStringExtra("Username");
        TextView tv1 = findViewById(R.id.textView);//Show
        tv1.setText(Username);//Show
//        Log.i("MyApp", Username);
    }

    public void LogoutButton_Profile(View v){
        SharedPreferences pref = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("Userid", "-1"); /// clear
        editor.putString("Username", "-1");///clear
        editor.putBoolean("SessionKey",false);  ////Set Session to false
        editor.apply();
        Intent itn2 = new Intent(this,LoginActivity.class); ///Back to login
        startActivity(itn2);
    }
}