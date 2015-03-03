package com.cts;

import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Random;

public class MapsActivity extends FragmentActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    SharedPreferences sp;
    GoogleApiClient mGoogleApiClient;
    private GoogleMap mMap;
    public static LatLng myLoc, chLoc;
    public static Marker myMarker, chMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        sp = getApplicationContext().getSharedPreferences(AppConfig.TAG, MODE_PRIVATE);

        buildGoogleApiClient();

        setUpMapIfNeeded();


        // Generate Random New Lat & Lng of Child
        Random r = new Random();
        Float lat = r.nextInt(90) + r.nextFloat();
        Float lng = r.nextInt(90) + r.nextFloat();
        sendSMS(AppConfig.RX_MOBILE_NUMBER, "" + lat + "," + lng);
    }

    private void sendSMS(String phoneNumber, String message) {
        try {
            SmsManager sms = SmsManager.getDefault();
            ArrayList<String> mSMSMessage = sms.divideMessage(message);
            sms.sendMultipartTextMessage(phoneNumber, null, mSMSMessage, null, null);

        } catch (Exception e) {
            Log.e(AppConfig.TAG, "SMS sending failed...", e);
            Toast.makeText(getBaseContext(), "SMS sending failed...", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Method to create Map
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * create an instance of the Google Play services API client
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location != null) {
            myLoc = new LatLng(location.getLatitude(), location.getLongitude());
        } else {
            Log.e(AppConfig.TAG, "Null my Location received.");
        }
    }

    /**
     * To update Child location when SMS received by SMSReceiver
     * @param lat Child latitude
     * @param lng Child longitude
     */
    public static void updateLocation(Float lat, Float lng) {
        if (chMarker != null) {
            chLoc = new LatLng(lat, lng);
            chMarker.setPosition(chLoc);
            Log.d(AppConfig.TAG, "Location updated on Map.");
        } else {
            Log.e(AppConfig.TAG, "Location cannot be updated on Map.");
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        // Get My Location
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location != null) {
            myLoc = new LatLng(location.getLatitude(), location.getLongitude());
        } else {
            Log.e(AppConfig.TAG, "Null my Location received.");
        }

        if (myLoc == null) {
            myLoc = new LatLng(0, 0);
        }
        myMarker = mMap.addMarker(new MarkerOptions().position(myLoc).title("My Location"));

        // Set Last Child Location
        Float chLat = sp.getFloat("chLat", 0.0f);
        Float chLng = sp.getFloat("chLng", 0.0f);
        chLoc = new LatLng(chLat, chLng);

        chMarker = mMap.addMarker(new MarkerOptions().position(chLoc).title("Child Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
