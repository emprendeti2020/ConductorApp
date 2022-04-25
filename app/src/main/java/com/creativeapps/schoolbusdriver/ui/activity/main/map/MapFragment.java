package com.creativeapps.schoolbusdriver.ui.activity.main.map;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.creativeapps.schoolbusdriver.R;
import com.creativeapps.schoolbusdriver.data.Util;
import com.creativeapps.schoolbusdriver.data.network.models.Driver;
import com.creativeapps.schoolbusdriver.data.network.models.Parent;
import com.creativeapps.schoolbusdriver.ui.activity.login.LoginActivity;
import com.creativeapps.schoolbusdriver.ui.activity.main.MainActivity;
import com.creativeapps.schoolbusdriver.ui.activity.main.MainActivityModel;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

public class MapFragment extends Fragment implements View.OnClickListener, OnMapReadyCallback {

    final String TAG = "MapFragment";

    //default zoom level for the Google map
    private static final int DEFAULT_ZOOM = 17;
    //view model of the main activity
    private MainActivityModel mViewModel;
    private ImageView mShowMyLocation, mShowHomes, mToggleSatt;
    //Google map object
    private GoogleMap mGoogleMap;
    //bus marker
    private Marker mBusMarker;
    //accuracy circle with a center of bus marker position
    private Circle mAccuracyCircle;
    //bound of all locations of the homes of parents
    private LatLngBounds mParentBounds;
    //driver object that holds driver information
    private Driver mDriver;
    //Text view that display the connectivity status of the app (online or offline)
    private TextView mStatus;
    //Layout that contains the connectivity status
    private RelativeLayout mStatusLayout;
    private Menu mMenu;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        //inflate the layout
        View root = inflater.inflate(R.layout.fragment_map, container, false);
        //instantiate the view model object
        mViewModel = ((MainActivity) getActivity()).createViewModel();

        return root;

    }

    // This event is triggered soon after onCreateView().
    // Any view setup should occur here.  E.g., view lookups and attaching view listeners.
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        //show location image view is hidden until Google map is loaded correctly
        mShowMyLocation = view.findViewById(R.id.showLocation);
        mShowMyLocation.setVisibility(View.INVISIBLE);
        mShowMyLocation.setOnClickListener(this);


        //show homes image view is hidden until Google map is loaded correctly
        mShowHomes = view.findViewById(R.id.showHomes);
        mShowHomes.setVisibility(View.INVISIBLE);
        mShowHomes.setOnClickListener(this);


        //show homes image view is hidden until Google map is loaded correctly
        mToggleSatt = view.findViewById(R.id.toggleSatt);
        mToggleSatt.setVisibility(View.INVISIBLE);
        mToggleSatt.setOnClickListener(this);


        mStatus = view.findViewById(R.id.status);
        mStatusLayout = view.findViewById(R.id.statusLayout);

        //start Google map
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.menu_tracking_fragment, menu);
        mMenu = menu;

        String tracking_service_preference = Util.getSavedObjectFromPreference(getActivity().getApplicationContext(),
                "mPreference", "service", String.class);

        if (tracking_service_preference == null || tracking_service_preference.matches("track_off")) {
            setTrackingOnOffMenuColor(1);
        }
        else
        {
            setTrackingOnOffMenuColor(0);
        }
    }

    void setTrackingOnOffMenuColor(int tracking_on_off)
    {
        int color = 0;
        if(tracking_on_off == 0) //tracking on
        {
            color = R.color.green;
        }
        else //tracking off
        {
            color = R.color.red;
        }

        for(int i = 0; i < mMenu.size(); i++){
            Drawable drawable = mMenu.getItem(i).getIcon();
            if(drawable != null) {
                drawable.mutate();
                drawable.setColorFilter(getResources().getColor(color), PorterDuff.Mode.SRC_ATOP);
            }
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.start_stop_tracking:
                String tracking_service_preference = Util.getSavedObjectFromPreference(getActivity().getApplicationContext(),
                        "mPreference", "service", String.class);

                if (tracking_service_preference == null || tracking_service_preference.matches("track_off")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Start Tracking");
                    String warnMessage = "This app collects location data to enable parents to know the bus location. " +
                            "The app will collect the location even when the app is closed or not in use. " +
                            "However, you can stop the location access by turning the location icon on the top menu to be red, Continue?";
                    builder.setMessage(warnMessage);
                    builder.setPositiveButton("Start", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.d(TAG, "onClick: yes");
                            //save to preference
                            Util.saveObjectToSharedPreference(getActivity().getApplicationContext(),
                                    "mPreference", "service", "track_on");
                            ((MainActivity) getActivity()).startLocationUpdates();
                            setTrackingOnOffMenuColor(0);
                            setOnlineTitle();
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            setOnlineTitle();
                        }
                    });
                    builder.show();
                }
                else
                {
                    //save to preference
                    Util.saveObjectToSharedPreference(getActivity().getApplicationContext(),
                            "mPreference", "service", "track_off");
                    ((MainActivity) getActivity()).stopLocationUpdates();
                    setTrackingOnOffMenuColor(1);
                }
                setOnlineTitle();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    /*make the status "online" */
    private void setOnlineTitle() {
        //mStatusLayout.setVisibility(View.INVISIBLE);
        String tracking_service_preference = Util.getSavedObjectFromPreference(getActivity().getApplicationContext(),
                "mPreference", "service", String.class);

        if (tracking_service_preference == null || tracking_service_preference.matches("track_off")) {
            mStatus.setText(getString(R.string.no_tracking));
            mStatusLayout.setBackgroundColor(Color.RED);
        }
        else if (tracking_service_preference.matches("track_on"))
        {
            mStatus.setText(getString(R.string.tracking_on));
            mStatusLayout.setBackgroundColor(Color.GREEN);
        }

    }

    /*make the status "offline" with red background*/
    private void setOfflineTitle() {
        //mStatusLayout.setVisibility(View.VISIBLE);
        mStatus.setText(getString(R.string.no_internet));
        mStatusLayout.setBackgroundColor(Color.RED);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        //when the map is ready
        this.mGoogleMap = googleMap;

        if(mViewModel.getMapType() != null)
        {
            this.mGoogleMap.setMapType(mViewModel.getMapType());
            if(mViewModel.getMapType() == GoogleMap.MAP_TYPE_SATELLITE)
            {
                mToggleSatt.setImageResource(R.drawable.map);
            }
            else
            {
                mToggleSatt.setImageResource(R.drawable.satellite);
            }
        }
        //observe changes for the position of the driver
        mViewModel.getPosDriver().observe(this, new PosDriverObserver());
        //observe changes for connectivity status
        mViewModel.getConnectivityStatus().observe(this, new ConnectivityStatusObserver());
        //observe changes for driver information
        mViewModel.getDriver().observe(this, new DriverObserver());
        //update the gui on the map after the map is ready. The function will make "show location"
        // and "show homes" image views visible to the user
        updateMapUI();
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            //center the map on the current location of the bus
            case R.id.showLocation:
                if (mBusMarker != null) {
                    CameraPosition cameraPosition = new CameraPosition.Builder().target(
                            mBusMarker.getPosition()).zoom(DEFAULT_ZOOM).build();

                    mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                }
                break;
            //change the zoom level of the map to fit the locations of all parent homes
            case R.id.showHomes:
                if (mParentBounds != null) {
                    //get the current width and height of the screen to detect the orientation of
                    // the device. Then, set the margins around the parent homes bound appropriately
                    DisplayMetrics dMetrics = new DisplayMetrics();
                    this.getActivity().getWindowManager().getDefaultDisplay().getMetrics(dMetrics);
                    int w = dMetrics.widthPixels;
                    int h = dMetrics.heightPixels;

                    mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(mParentBounds, w, h, w < h ? 100 : 300));
                }
                break;
            case R.id.toggleSatt:
                if (mGoogleMap != null) {
                    //toggle SATELLITE map view
                    if(this.mGoogleMap.getMapType() == GoogleMap.MAP_TYPE_SATELLITE)
                    {
                        this.mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                        mViewModel.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                        mToggleSatt.setImageResource(R.drawable.satellite);
                    }
                    else
                    {
                        this.mGoogleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                        mViewModel.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                        mToggleSatt.setImageResource(R.drawable.map);
                    }
                }
                break;
        }
    }

    /*update the gui on the map after the map is ready. The function will make the "show location"
    and "show homes" image views visible to the user*/
    private void updateMapUI() {
        if (mGoogleMap == null) {
            return;
        }
        mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
        mShowMyLocation.setVisibility(View.VISIBLE);
        mShowHomes.setVisibility(View.VISIBLE);
        mToggleSatt.setVisibility(View.VISIBLE);
    }

    /*set bus icon marker location on Google map. The function initialize the bus marker if it is
    not initialized yet, or move the marker with animation to a new position*/
    private void setBusLocation(LatLng bus_location) {

        //verify that Google map is loaded correctly before proceed
        if (mGoogleMap == null) {
            return;
        }
        //if the bus marker is not initialized, initialize it and add it to the map on the
        // specified location
        if (mBusMarker == null) {
            //define marker options with a bus icon
            MarkerOptions marker_option = new MarkerOptions()
                    .position(bus_location)
                    .title("You are here")
                    .flat(true)
                    .anchor(0.5f, 0.5f)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.school_bus));

            // adding marker
            mBusMarker = mGoogleMap.addMarker(marker_option);

            //move the camera to make the bus icon centered on the map
            CameraPosition cameraPosition = new CameraPosition.Builder().target(
                    bus_location).zoom(DEFAULT_ZOOM).build();
            mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        } else //if the bus marker is already initialized before, move it to the new location
        {
            //convert from LatLng to Location object
            Location locationDriver = new Location(LocationManager.GPS_PROVIDER);
            locationDriver.setLatitude(bus_location.latitude);
            locationDriver.setLongitude(bus_location.longitude);
            //Helper method for smooth animation
            animateMarker(mBusMarker, locationDriver);
            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLng(bus_location));
        }


    }

    /*put the homes of the parents on the map*/
    private void setParentsLocationUI() {

        if (mGoogleMap == null) {
            return;
        }

        //construct LatLngBounds that include locations of homes of all parents
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        if(mDriver.school != null && mDriver.school.getLast_latitude() != null && mDriver.school.getLast_longitude() != null) {
            LatLng schoolPos = new LatLng(mDriver.school.getLast_latitude(), mDriver.school.getLast_longitude());
            builder.include(schoolPos);

            //define marker options with a school icon
            MarkerOptions marker_option = new MarkerOptions()
                    .position(schoolPos)
                    .title(mDriver.school.getName())
                    .flat(true)
                    .anchor(0.5f, 0.5f)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.school));

            // adding marker
            mGoogleMap.addMarker(marker_option);

        }

        Log.d(TAG, "setParentsLocationUI: " + mDriver.school.getName());
        for (int i = 0; i < mDriver.getParents().size(); i++) {
            Parent p = mDriver.getParents().get(i);
            //if the parent set his home location
            if (p.getAddress_latitude() != null && p.getAddress_longitude() != null) {
                LatLng pos = new LatLng(p.getAddress_latitude(), p.getAddress_longitude());

                //draw the home icon on a bitmap along with the parent name with the following 4 steps
                //1 - define an empty bitmap
                Bitmap.Config conf = Bitmap.Config.ARGB_8888;
                Bitmap bmp = Bitmap.createBitmap(400, 200, conf);
                //2 - get a canvas of this bitmap
                Canvas bmpCanvas = new Canvas(bmp);

                // paint defines the text color, stroke width and size
                Paint color = new Paint();
                color.setTextSize(55);
                color.setColor(Color.BLACK);

                Bitmap homeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.home);

                //3- modify canvas by drawing the homeIcon and the parent name
                bmpCanvas.drawBitmap(homeIcon, (bmp.getWidth() - homeIcon.getWidth()) / 2, (bmp.getHeight() - homeIcon.getHeight()) / 2, color);
                bmpCanvas.drawText(p.getName(), 20, 50, color);
                //4 - add marker to Map
                mGoogleMap.addMarker(new MarkerOptions()
                        .position(pos)
                        .icon(BitmapDescriptorFactory.fromBitmap(bmp))
                        // Specifies the anchor to be at a particular point in the marker image.
                        .anchor(0.5f, 0.5f));

                builder.include(pos);
            }
        }
        try {
            //build the bound with included homes locations
            mParentBounds = builder.build();
        } catch (Exception e) {

        }

    }

    /*helper function to animate the change in position of the bus location so that it appears with smooth motion on
    the map*/
    public void animateMarker(final Marker marker, final Location location) {

        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final LatLng startLatLng = marker.getPosition();
        final double startRotation = marker.getRotation();
        final long duration = 500;
        final Interpolator interpolator = new LinearInterpolator();
        final int accuracyStrokeColor = Color.argb(255, 130, 182, 228);
        final int accuracyFillColor = Color.argb(100, 130, 182, 228);

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed
                        / duration);

                double lng = t * location.getLongitude() + (1 - t)
                        * startLatLng.longitude;
                double lat = t * location.getLatitude() + (1 - t)
                        * startLatLng.latitude;

                float rotation = (float) (t * location.getBearing() + (1 - t)
                        * startRotation);

                marker.setPosition(new LatLng(lat, lng));
                marker.setRotation(rotation);

                if (mAccuracyCircle != null) {
                    mAccuracyCircle.remove();
                }
                if (mViewModel.getAccuracy().getValue() != null) {
                    final CircleOptions accuracyCircleOptions = new CircleOptions()
                            .center(marker.getPosition())
                            .radius(mViewModel.getAccuracy().getValue())
                            .fillColor(accuracyFillColor)
                            .strokeColor(accuracyStrokeColor)
                            .strokeWidth(2.0f);
                    mAccuracyCircle = mGoogleMap.addCircle(accuracyCircleOptions);
                }

                if (t < 1.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                }
            }
        });
    }

    /*observe changes for the position of the driver*/
    private class PosDriverObserver implements Observer<LatLng> {

        @Override
        public void onChanged(@Nullable LatLng posDriver) {
            if (posDriver == null) return;
            if (mGoogleMap == null) return;

            Log.d(TAG,
                    "PosDriverObserver => onChanged: posDriver " + posDriver.latitude + ", " +
                            posDriver.longitude + ", " + mViewModel.getAccuracy().getValue());
            //the bus change its location so update it on Google map
            setBusLocation(posDriver);
        }
    }

    /*observer for connectivity status. If no connection with the backend web socket, the app will
    display a red bar at the bottom*/
    private class ConnectivityStatusObserver implements Observer<Boolean> {

        @Override
        public void onChanged(Boolean connectivityStatus) {
            if (connectivityStatus == null)
                return;

            if (connectivityStatus) {
                setOnlineTitle();
            }
            else
                setOfflineTitle();
        }
    }


    //observer for driver data, which when changed, the status of the app becomes "online" and the
    // updated driver data is saved to SharedPreferences. If the driver is not verified, the user
    // is redirected to the login activity
    private class DriverObserver implements Observer<Driver> {

        @Override
        public void onChanged(@Nullable Driver driver) {
            //((MainActivity) getActivity()).showHideProgressBar(false);
            if (driver == null) {
                //get the last saved parent information from the SharedPreference
                mDriver = Util.getSavedObjectFromPreference(getContext(),
                        "mPreference", "Driver", Driver.class);
                //update the UI to show "offline" status
                mViewModel.setConnectivityStatus(false);
            } else {
                mDriver = driver;
                Log.d(TAG, "DriverObserver => onChanged: " + driver.getName());
                Util.saveObjectToSharedPreference(getContext(),
                        "mPreference", "Driver", driver);
                //update the UI to show "online" status
                mViewModel.setConnectivityStatus(true);
            }
            if(mDriver!=null && mDriver.getParents()!=null && mDriver.getParents().size()>0)
                //put the homes of the parents on the map
                setParentsLocationUI();
            //if the driver is not verified, go to the login activity
            if (mDriver.getVerified() != 1)
                //logout
                ((MainActivity) getActivity()).logout();
        }
    }
}