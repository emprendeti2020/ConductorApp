package com.creativeapps.schoolbusdriver.ui.activity.splash;

import android.os.Bundle;
import android.util.Log;

import com.creativeapps.schoolbusdriver.R;

import com.creativeapps.schoolbusdriver.data.Util;
import com.creativeapps.schoolbusdriver.data.network.models.Driver;
import com.creativeapps.schoolbusdriver.ui.activity.login.LoginActivity;
import com.creativeapps.schoolbusdriver.ui.activity.main.MainActivity;

import androidx.appcompat.app.AppCompatActivity;


public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        Thread background = new Thread() {
            public void run() {
                try {
                    // Thread will sleep for 2 seconds
                    sleep(2*1000);

                    //check if the user already has saved login data
                    Driver d = Util.getSavedObjectFromPreference(getApplicationContext(),
                            "mPreference", "Driver", Driver.class);

                    //if the user already has saved login data
                    if(d!=null)
                    {
                        //redirect the main screen
                        Util.redirectToActivity(SplashScreenActivity.this, MainActivity.class);
                    }
                    else
                    {
                        //if not, redirect the user to the login screen
                        Util.redirectToActivity(SplashScreenActivity.this, LoginActivity.class);
                    }

                } catch (Exception e) {
                    Log.d("Splash", "run: " + e.getMessage());
                }
            }
        };

        // start thread
        background.start();
    }
}
