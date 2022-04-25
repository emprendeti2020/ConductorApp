package com.creativeapps.schoolbusdriver.ui.activity.main;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.creativeapps.schoolbusdriver.R;
import com.creativeapps.schoolbusdriver.data.Util;
import com.creativeapps.schoolbusdriver.data.network.models.Driver;
import com.creativeapps.schoolbusdriver.ui.activity.login.LoginActivity;
import com.creativeapps.schoolbusdriver.ui.activity.main.map.AutostartReceiver;
import com.creativeapps.schoolbusdriver.ui.activity.main.map.BackgroundLocationUpdateUtils;
import com.creativeapps.schoolbusdriver.ui.activity.main.map.LocationBackgroundService;
import com.creativeapps.schoolbusdriver.ui.activity.main.map.MapFragment;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import static com.creativeapps.schoolbusdriver.ui.activity.login.LoginActivity.getContext;

import com.google.android.gms.ads.MobileAds;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    // Used in checking for runtime permissions.
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    private static final int ALARM_MANAGER_INTERVAL = 15000;


        // The BroadcastReceiver used to listen from broadcasts from the service.
    private MyReceiver myReceiver;

    // A reference to the service used to get location updates.
    private LocationBackgroundService mService = null;

    // Tracks the bound state of the service.
    private boolean mBound = false;

    final String TAG = "MainActivity";
    public static final String KEY_WAKELOCK = "wakelock";


    public static final int PERMISSIONS_REQUEST_CALL = 2;

    //view model of the main activity
    private MainActivityModel mViewModel;
    //driver object that holds driver information
    private Driver mDriver;
    //overlay that prevent the user from interacting with any gui element on the screen while the
    // spinner is shown
    private Dialog mOverlayDialog;
    private ProgressBar mProgressBar;
    private boolean mLocationPermissionGranted;
    private LocationManager mLocationManager;
    //navigation and drawer layout
    private AppBarConfiguration mAppBarConfiguration;
    public NavController navController;
    public NavigationView navigationView;
    private DrawerLayout mDrawer;

    private AlarmManager alarmManager;
    private PendingIntent alarmIntent;

    private static Context mContext;
    private AdView mAdView;

    public static Context getContext() {
        return mContext;
    }

    // Monitors the state of the connection to the service.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationBackgroundService.LocalBinder binder = (LocationBackgroundService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;

            String tracking_service_preference = Util.getSavedObjectFromPreference(getApplicationContext(),
                    "mPreference", "service", String.class);

            if (tracking_service_preference == null || tracking_service_preference.matches("track_off")) {
                //off
                stopLocationUpdates();
            }
            else
            {
                //on
                startLocationUpdates();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mBound = false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        myReceiver = new MyReceiver();

        //inflate the layout
        setContentView(R.layout.activity_main);
        //setup the navigation with drawer
        setupNavigation();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarmIntent = PendingIntent.getBroadcast(this, 0, new Intent(this, AutostartReceiver.class), 0);


        mProgressBar = findViewById(R.id.MapProgressBar);
        mProgressBar.setVisibility(View.GONE);
        mOverlayDialog = new Dialog(this, android.R.style.Theme_Panel);
        //instantiate the view model
        mViewModel = createViewModel();
        //instantiate the location manager object
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        //check if GPS is enabled
        if ( !mLocationManager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            //if not, display an Alert message that may take the user to the settings to enable
            // the GPS
            buildAlertMessageNoGps();
        }


        //get the last saved driver information from the SharedPreference
        mDriver = Util.getSavedObjectFromPreference(getApplicationContext(),
                "mPreference", "Driver", Driver.class);

        //check if there are already saved data for the driver
        if(mDriver !=null)
            //if so, update the driver information by getting the latest driver data from the server
            //note that, the observer of this function is defined in the map fragment class
            mViewModel.getDriverServer(mDriver.getCountry_code(), mDriver.getTel_number(), mDriver.getSecretKey());
        else
            //otherwise, go to login again
            Util.redirectToActivity(this, LoginActivity.class);


        MobileAds.initialize(this);


        mAdView = findViewById(R.id.adView);

        mAdView.setVisibility(View.GONE);

        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    // Setting Up One Time Navigation
    private void setupNavigation() {

        mDrawer = findViewById(R.id.drawer_layout);

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_parentContacts, R.id.nav_childList, R.id.nav_about,
                R.id.nav_logout)
                .setDrawerLayout(mDrawer)
                .build();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //setup the navigation controller
        navigationView = findViewById(R.id.navigationView);
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        navigationView.setNavigationItemSelectedListener(this);

    }


    @Override
    public void onBackPressed() {
        //close drawer when back button pressed
        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        //close drawer when any item selected
        mDrawer.closeDrawers();
        //if the item already selected, do nothing
        if(menuItem.isChecked())
            return false;

        //check which menu item is selcted
        switch(menuItem.getItemId()) {
            //show map
            case R.id.nav_home:
                menuItem.setChecked(true);
                navController.navigate(R.id.nav_home);
                break;
            //show the parents contacts fragment
            case R.id.nav_parentContacts:
                menuItem.setChecked(true);
                navController.navigate(R.id.nav_parentContacts);
                break;
            case R.id.nav_childList:
                menuItem.setChecked(true);
                navController.navigate(R.id.nav_childList);
                break;
            //share the app
            case R.id.nav_shareApp:
                shareApp();
                //do not check the menu item and do not add it to the navigation stack
                return false;
            //show about
            case R.id.nav_about:
                menuItem.setChecked(true);
                navController.navigate(R.id.nav_about);
                break;
            //logout
            case R.id.nav_logout:
                //display an alert dialog to warn the user that he is about to make phone call. If the user
                // chooses yes, a call will be placed
                new AlertDialog.Builder(this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle(getString(R.string.logout))
                        .setMessage(getString(R.string.are_you_sure))
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @SuppressLint("MissingPermission")
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                logout();
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
                //do not check the menu item and do not add it to the navigation stack
                return false;
        }
        return true;
    }

    private void shareApp() {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        String shareBody = getString(R.string.share_body);
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Share");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(sharingIntent, "Share via"));
    }

    /*return a view model for the activity*/
    public MainActivityModel createViewModel() {
        return ViewModelProviders.of(this).get(MainActivityModel.class);
    }

    /*function used to show/hide spinner and the overlay dialog*/
    public void showHideProgressBar(Boolean show)
    {
        if(show)
        {
            mOverlayDialog.show();
            mProgressBar.setVisibility(View.VISIBLE);
        }
        else
        {
            mOverlayDialog.dismiss();
            mProgressBar.setVisibility(View.GONE);
        }
    }

    /*start listening for location change of the driver. Note that, this function is executed in
    the main activity so that the app track the driver location regardless the fragment displayed*/
    public void startLocationUpdates()
    {
        if (!checkPermissions()) {
            requestPermissions();
            ContextCompat.startForegroundService(this, new Intent(this, LocationBackgroundService.class));

            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    ALARM_MANAGER_INTERVAL, ALARM_MANAGER_INTERVAL, alarmIntent);
        } else {
            showHideProgressBar(true);
                    mService.requestLocationUpdates(mDriver);
        }
    }

    /*stop listening for location change*/
    public void stopLocationUpdates()
    {
        mService.removeLocationUpdates();
    }

    /*display an alert that take the user to the settings of his device to turn the GPS on*/
    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.gps_seems_disabled))
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                        Util.displayExitMessage(getString(R.string.turn_gps_on_to_continue),MainActivity.this, true);
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    /*log out from the app*/
    public void logout() {
        Util.saveObjectToSharedPreference(getApplicationContext(),
                "mPreference", "Driver", null);
        FirebaseAuth.getInstance().signOut();
        finishAffinity();
        Util.redirectToActivity(this, LoginActivity.class);
    }


    @Override
    protected void onStart() {
        super.onStart();

        // Bind to the service. If the service is in foreground mode, this signals to the service
        // that since this activity is in the foreground, the service can exit foreground mode.
        bindService(new Intent(this, LocationBackgroundService.class), mServiceConnection,
                Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(myReceiver,
                new IntentFilter(LocationBackgroundService.ACTION_BROADCAST));

        mViewModel.getShowAds().observe(this, new ShowAdsObserver());
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(myReceiver);
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (mBound) {
            // Unbind from the service. This signals to the service that this activity is no longer
            // in the foreground, and the service can respond by promoting itself to a foreground
            // service.
            unbindService(mServiceConnection);
            mBound = false;
        }
        super.onStop();
    }

    /**
     * Returns the current state of the permissions needed.
     */
    private boolean checkPermissions() {
        return  PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");
            Snackbar.make(
                    findViewById(R.id.drawer_layout),
                    R.string.permission_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    REQUEST_PERMISSIONS_REQUEST_CODE);
                        }
                    })
                    .show();
        } else {
            Log.i(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionResult");
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted.
                mService.requestLocationUpdates(mDriver);
            } else {
                // Permission denied.
                //setButtonsState(false);
                Snackbar.make(
                        findViewById(R.id.drawer_layout),
                        R.string.permission_denied_explanation,
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.settings, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Build intent that displays the App settings screen.
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",
                                        getApplicationContext().getPackageName(), null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        })
                        .show();
            }
        }
        else if (requestCode == PERMISSIONS_REQUEST_CALL) {
            if (grantResults.length > 0
                    && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                //if not granted, display a message
                Toast.makeText(getApplicationContext(), "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Receiver for broadcasts sent by {@link LocationBackgroundService}.
     */
    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            Location location = intent.getParcelableExtra(LocationBackgroundService.EXTRA_LOCATION);
            if (location != null) {

                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                float accuracy = location.getAccuracy();
                //convert the location to LatLng object
                LatLng driverPos = new LatLng(latitude, longitude);

                mViewModel.setAccuracy(accuracy);
                mViewModel.setPosDriver(driverPos);

                showHideProgressBar(false);
            }
        }
    }

    private class ShowAdsObserver implements Observer<Integer> {

        @Override
        public void onChanged(Integer showAds) {
            if (showAds == null)
                return;

            if (showAds==1) {
                mAdView.setVisibility(View.VISIBLE);
            }
            else
                mAdView.setVisibility(View.GONE);
        }
    }

//    @Override
//    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
//        // Update the buttons state depending on whether location updates are being requested.
//        if (s.equals(BackgroundLocationUpdateUtils.KEY_REQUESTING_LOCATION_UPDATES)) {
//            setButtonsState(sharedPreferences.getBoolean(BackgroundLocationUpdateUtils.KEY_REQUESTING_LOCATION_UPDATES,
//                    false));
//        }
//    }
//
//    private void setButtonsState(boolean requestingLocationUpdates) {
//        if (requestingLocationUpdates) {
//            mRequestLocationUpdatesButton.setEnabled(false);
//            mRemoveLocationUpdatesButton.setEnabled(true);
//        } else {
//            mRequestLocationUpdatesButton.setEnabled(true);
//            mRemoveLocationUpdatesButton.setEnabled(false);
//        }
//    }

}