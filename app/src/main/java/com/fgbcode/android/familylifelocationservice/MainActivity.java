package com.fgbcode.android.familylifelocationservice;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    public static final long REQ_INTERVAL_LOW = 30 * 60 * 1000; // 30 minutes
    public static final long REQ_INTERVAL_LOW_FAST = 10 * 60 * 1000; // 10 minutes
    public static final long REQ_INTERVAL = 20 * 1000; // 20 seconds
    public static final long REQ_INTERVAL_FAST = 10 * 1000; // 10 seconds
    public static final String TAG = MainActivity.class.getSimpleName();

    private static final String BUTTON_STANDARD = "Pause Service";
    private static final String BUTTON_PAUSE = "Start Service";

    public boolean pauseModeActivated = false;

    private PendingIntent pendingIntent;

    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {

                Location tmpLocation = new Location("TMP-MainActivity-LOCATION");

                Double latitude = bundle.getDouble(LocationService.LATITUDE_STRING);
                Double longitude = bundle.getDouble(LocationService.LONGITUDE_STRING);
                Long time = bundle.getLong(LocationService.TIME_STRING);
                Float accuracy = bundle.getFloat(LocationService.ACCURACY_STRING);
                Double altitude = bundle.getDouble(LocationService.ALTITUDE_STRING);

                tmpLocation.setLatitude(latitude);
                tmpLocation.setLongitude(longitude);
                tmpLocation.setTime(time);
                tmpLocation.setAccuracy(accuracy);
                tmpLocation.setAltitude(altitude);

                updateView(tmpLocation);

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setUpBackgroundService();

        /*
         * Creates a new Intent to start the RSSPullService
         * IntentService. Passes a URI in the
         * Intent's "data" field.
         */
        //Intent mServiceIntent = new Intent(this, ResponseReceiver.class);
        //mServiceIntent.setData(getIntent().getData());

        // starting service
        //startService(mServiceIntent);

//        IntentFilter mServiceFilter = new IntentFilter(Constants.BROADCAST_ACTION);
//        mServiceFilter.addDataScheme("http");
//        ResponseReceiver responseReceiver = new ResponseReceiver();
//        LocalBroadcastManager.getInstance(this).registerReceiver(responseReceiver,mServiceFilter);


        Button btnFetchLastLocation = (Button) findViewById(R.id.btnGetLocation);
        Button btnPause = (Button) findViewById(R.id.btnPause);

        btnFetchLastLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateView(LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient));
            }
        });

        btnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (pendingIntent != null && !pauseModeActivated) {
                    LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, pendingIntent);
                    Log.w(TAG, "(==FGBCODE==) PAUSE PAUSE PAUSE called");
                    pauseModeActivated = true;

                    Button btn = (Button) findViewById(R.id.btnPause);
                    btn.setText(BUTTON_PAUSE);
                    updateView(null);

                } else if (pendingIntent != null && pauseModeActivated) {
                    LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, pendingIntent);
                    Log.w(TAG, "(==FGBCODE==) UN-PAUSE UN-PAUSE UN-PAUSE called");
                    pauseModeActivated = false;

                    Button btn = (Button) findViewById(R.id.btnPause);
                    btn.setText(BUTTON_STANDARD);
                    updateView(null);
                }
            }
        });

    }

    private void setUpBackgroundService() {

        Intent mResponseReceiver = new Intent(getApplicationContext(), LocationService.class);
        mResponseReceiver.setData(getIntent().getData());

        pendingIntent = PendingIntent.getService(getApplicationContext(), 0, mResponseReceiver, PendingIntent.FLAG_UPDATE_CURRENT);

        // create the client
        // ATTENTION: This "addApi(AppIndex.API)"was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(AppIndex.API).build();

        // create a location request object
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                .setInterval(REQ_INTERVAL)
                .setFastestInterval(REQ_INTERVAL_FAST);

        if (mGoogleApiClient.isConnected() || mGoogleApiClient.isConnecting()) {
            Log.w(TAG, "(==FGBCODE==) is already conencted or is connecting. So pls wait...");
        } else {
            Log.w(TAG, "(==FGBCODE==) start connecting mGoogleApiClient...");
            mGoogleApiClient.connect();
        }

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.w(TAG, "(==FGBCODE==) onConnected called");
        if (pendingIntent != null) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, pendingIntent);
            pauseModeActivated = false;
            Log.w(TAG, "(==FGBCODE==) onConnected called");
        } else {
            Log.w(TAG, "(==FGBCODE==) pendingIntent IST NULL");
        }
        String writetxt = "onConnected(): Prio:" + mLocationRequest.getPriority() + " Int:" + mLocationRequest.getInterval() / 1000 + " sec. FInt:" + mLocationRequest.getFastestInterval() / 1000 + " sec.";
        writeCacheFile(writetxt);

        updateView(LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient));

    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.w(TAG, "(==FGBCODE==) onConnectionSuspended called");
        updateView(null);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, "(==FGBCODE==) onConnectionFailed called");
        Toast.makeText(this, "Connection failed!",Toast.LENGTH_SHORT).show();
        updateView(null);
    }

    @Override
    protected void onResume() {
        Log.w(TAG, "(==FGBCODE==) onResume called");
        super.onResume();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setFastestInterval(REQ_INTERVAL_FAST);
        mLocationRequest.setInterval(REQ_INTERVAL);

        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }

        String writetxt = "onResume(): Prio:" + mLocationRequest.getPriority() + " Int:" + mLocationRequest.getInterval() / 1000 + " sec. FInt:" + mLocationRequest.getFastestInterval() / 1000 + " sec.";
        writeCacheFile(writetxt);
        updateView(null);
    }

    @Override
    protected void onPause() {
        Log.w(TAG, "(==FGBCODE==) onPause called");

        // location request will not be paused!
        mLocationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);
        mLocationRequest.setFastestInterval(REQ_INTERVAL_LOW_FAST);
        mLocationRequest.setInterval(REQ_INTERVAL_LOW);

        String writetxt = "onPause(): Prio:" + mLocationRequest.getPriority() + " Int:" + mLocationRequest.getInterval() / 1000 + " sec. FInt:" + mLocationRequest.getFastestInterval() / 1000 + " sec.";
        writeCacheFile(writetxt);
        updateView(null);

        super.onPause();
    }

    @Override
    protected void onStop() {

        Log.w(TAG, "(==FGBCODE==) onStop called");
        mLocationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);
        mLocationRequest.setFastestInterval(REQ_INTERVAL_LOW_FAST);
        mLocationRequest.setInterval(REQ_INTERVAL_LOW);

        String writetxt = "onStop(): Prio:" + mLocationRequest.getPriority() + " Int:" + mLocationRequest.getInterval() / 1000 + " sec. FInt:" + mLocationRequest.getFastestInterval() / 1000 + " sec.";
        writeCacheFile(writetxt);
        updateView(null);

        super.onStop();

    }

    @Override
    protected void onDestroy() {


        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, pendingIntent);
        pauseModeActivated = true;
        mGoogleApiClient.disconnect();

        writeCacheFile("onDestroy() called. Everything will be stopped.");
        updateView(null);

        super.onDestroy();

    }

    private String addTimePrefix(String text) {

        StringBuilder stringBuilder = new StringBuilder();

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss (z) yyyy.MM.dd");
        String time = sdf.format(new Date());

        stringBuilder.append(time);
        stringBuilder.append(": ");
        stringBuilder.append(text);
        stringBuilder.append("\n");

        return stringBuilder.toString();

    }

    private void writeCacheFile(String text) {

        File file;
        FileOutputStream fileOutputStream;

        text = addTimePrefix(text);

        try {
            file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "MyLocationHistory.txt");

            Log.i(TAG, "(==FGB==) writeCacheFile: Writing to: " + file.getAbsolutePath());

            fileOutputStream = new FileOutputStream(file, true);
            fileOutputStream.write(text.getBytes());
            fileOutputStream.close();

        } catch (IOException e) {
            Log.wtf(TAG, e);
            Log.e(TAG, "(==FGB==) Couldnt write to file!");
        }

    }

    private void updateView(@Nullable Location location) {

        Log.i(TAG, "(==FGB==) updateView called.");

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss yyyy.MM.dd");

        // fetching all the view elements
        EditText txtConnected = (EditText) findViewById(R.id.txtConStatus);

        EditText txtInterval = (EditText) findViewById(R.id.txtInterval);
        EditText txtFInterval = (EditText) findViewById(R.id.txtFInterval);
        EditText txtDateTime = (EditText) findViewById(R.id.txtDateTime);
        EditText txtLat = (EditText) findViewById(R.id.txtLat);
        EditText txtLong = (EditText) findViewById(R.id.txtLong);
        EditText txtAccuracy = (EditText) findViewById(R.id.txtAccuracy);
        EditText txtAltitude = (EditText) findViewById(R.id.txtAltitude);

        if (mLocationRequest != null) {
            Long lInterval = new Long(mLocationRequest.getInterval());
            Long lFInterval = new Long(mLocationRequest.getFastestInterval());

            Integer iInterval = new Integer(lInterval.intValue() / 1000);
            Integer iFInterval = new Integer(lFInterval.intValue() / 1000);

            txtInterval.setText(iInterval.toString());
            txtFInterval.setText(iFInterval.toString());


        }



        txtConnected.setText(mGoogleApiClient.isConnected() ? "Connected" : "Disconnected");

        if (location != null) {
            Double latitude = new Double(location.getLatitude());
            Double longitude = new Double(location.getLongitude());
            Double altitude = new Double(location.getAltitude());
            Float accuracy = new Float(location.getAccuracy());

            txtAccuracy.setText(accuracy.toString());
            txtAltitude.setText(altitude.toString().substring(0, 7));

            txtLat.setText(latitude.toString());
            txtLong.setText(longitude.toString());
            txtDateTime.setText(sdf.format(location.getTime()));


        }

        TextView txtLastUpdate = (TextView) findViewById(R.id.txtViewUpdate);

        SimpleDateFormat sdfUpdate = new SimpleDateFormat("HH:mm:ss (MM.dd)");
        txtLastUpdate.setText(sdfUpdate.format(new Date()) + ((location==null) ? "" : " l") + (pauseModeActivated ? " (p)" : ""));

    }


    @Override
    public void onLocationChanged(Location location) {

        Log.i(TAG, "(==FGB==) onLocationChanged called : Location is " + (location==null ? "NULL" : "not null"));

        if (location != null) {
            updateView(location);
        }

    }

}
