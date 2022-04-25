package com.creativeapps.schoolbusdriver.ui.activity.login;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;

import com.creativeapps.schoolbusdriver.R;
import com.creativeapps.schoolbusdriver.data.Util;
import com.creativeapps.schoolbusdriver.data.network.models.Driver;
import com.creativeapps.schoolbusdriver.ui.activity.main.MainActivity;
import com.google.firebase.FirebaseApp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;


public class LoginActivity extends AppCompatActivity {

    //view model for the activity
    private LoginModel mViewModel;

    private static Context mContext;

    public static Context getContext() {
        return mContext;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //inflate the layout
        setContentView(R.layout.activity_login);
        mContext = getApplicationContext();

        FirebaseApp.initializeApp(this);

        //create an instance of the view model
        mViewModel = createViewModel();
    }
    /*create and return an instance of the view model*/
    public LoginModel createViewModel() {
        return ViewModelProviders.of(this).get(LoginModel.class);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        //start observing the driver live data from the view model
        mViewModel.getDriver().observe(this, new LoginActivity.driverObserver());
    }

    @Override
    public void onPause()
    {
        super.onPause();
        //stop observing live data when the fragment is paused
        mViewModel.getDriver().removeObservers(this);
    }

    /*Observer for the driver live data that contains the data of the driver. This data is obtained
    after the process (verify authentication code of the drive) is finished*/
    public class driverObserver implements Observer<Driver> {
        @Override
        public void onChanged(@Nullable Driver driver) {
            if (driver == null) return;
            //if the process (verify authentication code of the drive) is returned the data of
            // the driver, i.e., the driver authenticated correctly
            if (driver!=null) {
                //save the data of the driver to the SharedPreference
                Util.saveObjectToSharedPreference(getApplicationContext(), "mPreference",
                        "Driver", driver);
                //finish the login activity
                finishAffinity();
                //redirect the main screen
                Util.redirectToActivity(LoginActivity.this, MainActivity.class);
            }
        }
    }
}
