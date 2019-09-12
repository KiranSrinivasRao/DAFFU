package com.fireflies.daffu.utilities;

import android.net.Uri;
import android.util.Log;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * These utilities will be used to communicate with the network.
 */
public class NetworkUtils {


    private static final String LOG_TAG = NetworkUtils.class.getSimpleName();

    /**
     * Builds the URL used to query OpenMap.
     *
     * @param urlQuery The keyword that will be queried for.
     * @return The URL to use to query the weather server.
     */
    public static java.net.URL buildUrl(String urlQuery) {
        Uri builtUri = Uri.parse(urlQuery).buildUpon()
                .build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
            Log.d(LOG_TAG, e.getLocalizedMessage());
        }

        return url;
    }

}