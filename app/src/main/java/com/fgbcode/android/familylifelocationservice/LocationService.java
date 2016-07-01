package com.fgbcode.android.familylifelocationservice;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by U540874 on 27.06.2016.
 */
public class LocationService extends IntentService {

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    private PendingIntent pendingIntent;

    private static final int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    public static final String TAG = LocationService.class.getSimpleName();
    public static final String NOTIFICATION = "com.fgbcode.android.notification";
    public static final int REQ_INTERVAL = 10 * 1000; // 10 seconds
    public static final int REQ_INTERVAL_FAST = 2 * 1000; // 2 seconds

    public static final String LATITUDE_STRING = "Latitude";
    public static final String LONGITUDE_STRING = "Logitude";
    public static final String ALTITUDE_STRING = "Altitude";
    public static final String TIME_STRING = "Time";
    public static final String ACCURACY_STRING = "Accuracy";

    public LocationService() {
        super("LocationService");
    }

    // Default
    public LocationService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i(TAG, "(==FGBCODE==) onHandleIntent called!");

        if (LocationResult.hasResult(intent)) {
            LocationResult locationResult = LocationResult.extractResult(intent);
            Location location = locationResult.getLastLocation();
            if (location != null) {
                storeLocationUpdate(location);
            }

            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            NotificationCompat.Builder noti = new NotificationCompat.Builder(this);
            noti.setContentTitle("Family Location Service");
            noti.setContentText(location.getLatitude() + "," + location.getLongitude());
            noti.setSmallIcon(R.drawable.common_full_open_on_phone);
            notificationManager.notify(19080815, noti.build());

        } else {
            Log.w(TAG, "(==FGBCODE==) onHandleIntent - Intent has not location result");
        }



//
//
//        Intent localIntent =
//                new Intent(Constants.BROADCAST_ACTION)
//                        // Puts the status into the Intent
//                        .putExtra(Constants.EXTENDED_DATA_STATUS, "woohoo");
//        // Broadcasts the Intent to receivers in this app.
//        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);


    }

    private void storeLocationUpdate(Location location) {

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss (z) yyyy.MM.dd");
        String time = sdf.format(new Date(location.getTime()));

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(time);
        stringBuilder.append(": ");
        stringBuilder.append("Lat: ");
        stringBuilder.append(location.getLatitude());
        stringBuilder.append(" Long: ");
        stringBuilder.append(location.getLongitude());
        stringBuilder.append("\n");

        Log.i(TAG, "(==FGB==) LocationUpdate: " + stringBuilder.toString());

        writeDataToStorage(stringBuilder.toString());

    }

    private void writeDataToStorage(String locationInfo) {

        File file;
        FileOutputStream fileOutputStream;

        try {
            file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "MyLocationHistory.txt");

            Log.i(TAG, "(==FGB==) Writing to: " + file.getAbsolutePath());

            fileOutputStream = new FileOutputStream(file, true);
            fileOutputStream.write(locationInfo.getBytes());
            fileOutputStream.close();

        } catch (IOException e) {
            Log.wtf(TAG, e);
            Log.e(TAG, "(==FGB==) Couldnt write to file!");
        }

    }

    private void broadcastLocationUpdate(Location location) {

        Log.i(TAG, "(==FGB==) broadcasting location update");

        Intent intent = new Intent(NOTIFICATION_SERVICE);
        intent.putExtra("Latitude", location.getLatitude());
        intent.putExtra("Longitude", location.getLongitude());
        intent.putExtra("Altitude", location.getAltitude());
        intent.putExtra("Time", location.getTime());
        intent.putExtra("Accuracy", location.getAccuracy());
        sendBroadcast(intent);



    }



}
