package com.fgbcode.android.familylifelocationservice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by U540874 on 28.06.2016.
 */
public class ResponseReceiver extends BroadcastReceiver {

    public static final String TAG = LocationService.class.getSimpleName();



    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "(==FGBCODE==) onReceive called! Reacting to Broadcast!");


        // handle intent here.
    }
}
