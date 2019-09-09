package com.fireflies.daffu.utilities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * These utilities will be used to communicate with the network.
 */
public class NetworkUtils {


    private static final String LOG_TAG = NetworkUtils.class.getSimpleName();
    @SuppressLint("StaticFieldLeak")
    private static Context context;
    private static String downloadFileName = "";
    private String downloadUrl;

    public NetworkUtils(Context context, String downloadUrl) {
        NetworkUtils.context = context;
        this.downloadUrl = downloadUrl;

        downloadFileName = downloadUrl.replace(downloadUrl,
                downloadUrl.substring(downloadUrl.lastIndexOf('/') + 1));//Create file name by picking download file name from URL
        Log.e(LOG_TAG, downloadFileName);
    }

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
        }

        return url;
    }

    /**
     * This method returns the entire result from the HTTP response.
     *
     * @param url The URL to fetch the HTTP response from.
     * @return The contents of the HTTP response.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static String getResponseFromHttpUrl(URL url) {

        File outputFile;
        HttpURLConnection urlConnection = null;
        try {

            File fileStorage = null;

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();


            //If Connection response is not OK then show Logs
            if (urlConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                Log.e(LOG_TAG, "Server returned HTTP " + urlConnection.getResponseCode()
                        + " " + urlConnection.getResponseMessage());
            }

            //Get File if SD card is present
            if (new CheckForSDCard().isSDCardPresent()) {

                fileStorage = new File(
                        Environment.getExternalStorageDirectory() + "/"
                                + Environment.DIRECTORY_DOWNLOADS);
            } else
                Toast.makeText(context, "Oops!! There is no SD Card.", Toast.LENGTH_SHORT).show();

            //If File is not present create directory
            if (fileStorage != null && !fileStorage.exists()) {
                fileStorage.mkdir();
                Log.e(LOG_TAG, "Directory Created.");
            }

            outputFile = new File(fileStorage, downloadFileName);//Create Output file in Main File

            //Create New File if not present
            if (!outputFile.exists()) {
                outputFile.createNewFile();
                Log.e(LOG_TAG, "File Created");
            }

            FileOutputStream fos = new FileOutputStream(outputFile);//Get OutputStream for NewFile Location

            InputStream is = urlConnection.getInputStream();//Get InputStream for connection

            byte[] buffer = new byte[2046];//Set buffer type
            int len1;//init length
            while ((len1 = is.read(buffer)) != -1) {
                fos.write(buffer, 0, len1);//Write new file
            }
            //Close all connection after doing task
            fos.close();
            is.close();
            return outputFile.getAbsolutePath();

        } catch (Exception e) {
            //Read exception if something went wrong
            e.printStackTrace();
            Log.e(LOG_TAG, "Download Error Exception " + e.getMessage());
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }
}