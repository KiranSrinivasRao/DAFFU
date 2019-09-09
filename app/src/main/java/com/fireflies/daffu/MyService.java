package com.fireflies.daffu;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.fireflies.daffu.utilities.NetworkUtils;

public class MyService extends Service {

    final static String INTENT_URL_NAME = "URL";
    final static String INTENT_FILE = "file";
    final static String INTENT_TRANSFER_OPERATION = "transferOperation";

    final static String TRANSFER_OPERATION_DOWNLOAD = "download";

    private final static String LOG_TAG = MyService.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final String url = intent.getStringExtra(INTENT_URL_NAME);

        NetworkUtils util = new NetworkUtils(getApplicationContext(), url);

        Log.d(LOG_TAG, "Downloading " + url);
        try {
            String output = NetworkUtils.getResponseFromHttpUrl(NetworkUtils.buildUrl(url));

            Log.d(LOG_TAG, "Downloaded " + output);
        } catch (Exception e) {
            e.printStackTrace();
        }


        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
