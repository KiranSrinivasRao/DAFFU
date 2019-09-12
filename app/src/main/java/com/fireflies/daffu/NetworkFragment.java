package com.fireflies.daffu;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.fireflies.daffu.utilities.CheckForSDCard;
import com.fireflies.daffu.utilities.NetworkUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link NetworkFragment#getInstance} factory method to
 * create an instance of this fragment.
 */
public class NetworkFragment extends Fragment {

    private static final String LOG_TAG = NetworkFragment.class.getSimpleName();
    private static final String URL_KEY = "urlkey";

    private DownloadCallback mCallback;
    private DownloadTask mDownloadTask;
    private String mUrlString;

    public NetworkFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param fragmentManager getFragmentManager from Activity (Host)
     * @param url             url string
     * @return A new instance of fragment NetworkFragment.
     */
    static NetworkFragment getInstance(FragmentManager fragmentManager, String url) {
        // Recover NetworkFragment in case we are re-creating the Activity due to a config change.
        // This is necessary because NetworkFragment might have a task that began running before
        // the config change and has not finished yet.
        // The NetworkFragment is recoverable via this method because it calls
        // setRetainInstance(true) upon creation.
        NetworkFragment networkFragment = (NetworkFragment) fragmentManager
                .findFragmentByTag(NetworkFragment.LOG_TAG);
        if (networkFragment == null) {
            networkFragment = new NetworkFragment();
            Bundle args = new Bundle();
            args.putString(URL_KEY, url);
            networkFragment.setArguments(args);
            fragmentManager.beginTransaction().add(networkFragment, LOG_TAG).commit();
        }
        return networkFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retain this Fragment across configuration changes in the host Activity.
        setRetainInstance(true);
        if (getArguments() != null) {
            mUrlString = getArguments().getString(URL_KEY);
        }
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // Host Activity will handle callbacks from task.
        mCallback = (DownloadCallback) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        // Clear reference to host Activity.
        mCallback = null;
    }

    @Override
    public void onDestroy() {
        // Cancel task when Fragment is destroyed.
        cancelDownload();
        super.onDestroy();
    }

    /**
     * Start non-blocking execution of DownloadTask.
     */
    void startDownload(String url) {
        cancelDownload();
        mUrlString = url;
        mDownloadTask = new DownloadTask();
        mDownloadTask.execute(mUrlString);
    }

    /**
     * Cancel (and interrupt if necessary) any ongoing DownloadTask execution.
     */
    void cancelDownload() {
        if (mDownloadTask != null) {
            mDownloadTask.cancel(true);
            mDownloadTask = null;
        }
    }

    /**
     * Implementation of AsyncTask that runs a network operation on a background thread.
     */
    @SuppressLint("StaticFieldLeak")
    private class DownloadTask extends AsyncTask<String, Integer, DownloadTask.Result> {

        File apkStorage = null;
        File outputFile = null;

        /**
         * Cancel background network operation if we do not have network connectivity.
         */
        @Override
        protected void onPreExecute() {
            if (mCallback != null) {
                NetworkInfo networkInfo = mCallback.getActiveNetworkInfo();
                if (networkInfo == null || !networkInfo.isConnected() ||
                        (networkInfo.getType() != ConnectivityManager.TYPE_WIFI
                                && networkInfo.getType() != ConnectivityManager.TYPE_MOBILE)) {
                    // If no connectivity, cancel task and update Callback with null data.
                    mCallback.updateFromDownload(null);
                    cancel(true);
                }
            }
        }

        /**
         * Defines work to perform on the background thread.
         */
        @Override
        protected DownloadTask.Result doInBackground(String... urls) {
            Result result = null;
            if (!isCancelled() && urls != null && urls.length > 0) {
                String urlString = urls[0];
                try {
                    URL url = new URL(urlString);
                    String resultString = downloadUrl(url);
                    if (resultString != null) {
                        result = new Result(resultString);
                    } else {
                        throw new IOException("No response received.");
                    }
                } catch (Exception e) {
                    result = new Result(e);
                }
            }
            return result;
        }

        /**
         * Updates the DownloadCallback with the result.
         */
        @Override
        protected void onPostExecute(Result result) {
            if (result != null && mCallback != null) {
                if (result.mException != null) {
                    mCallback.updateFromDownload(result.mException.getMessage());
                } else if (result.mResultValue != null) {
                    mCallback.updateFromDownload(result.mResultValue);
                }
                mCallback.finishDownloading();
            }
        }

        /**
         * Override to add special behavior for cancelled AsyncTask.
         */
        @Override
        protected void onCancelled(Result result) {
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            if (values.length >= 2) {
                mCallback.onProgressUpdate(values[0], values[1]);
            }
        }

        /**
         * Given a URL, sets up a connection and gets the HTTP response body from the server.
         * If the network request is successful, it returns the response body in String form. Otherwise,
         * it will throw an IOException.
         */
        private String downloadUrl(URL urlString) throws IOException {
            String result = null;
            String downloadUrl = urlString.toString();
            FileOutputStream fos = null;
            InputStream is = null;
            try {

                URL url = NetworkUtils.buildUrl(downloadUrl);//Create Download URl
                String downloadFileName = downloadUrl.replace(downloadUrl,
                        downloadUrl.substring(downloadUrl.lastIndexOf('/') + 1));
                HttpURLConnection c = (HttpURLConnection) url.openConnection();//Open Url Connection
                c.setRequestMethod("GET");//Set Request Method to "GET" since we are grtting data
                c.connect();//connect the URL Connection

                publishProgress(DownloadCallback.Progress.CONNECTION_SUCCESS);
                //If Connection response is not OK then show Logs
                int responseCode = c.getResponseCode();
                if (responseCode != HttpsURLConnection.HTTP_OK) {
                    Log.e(LOG_TAG, "Server returned HTTP " + c.getResponseCode()
                            + " " + c.getResponseMessage());
                    throw new IOException("HTTP error code: " + responseCode);
                }

                //Get File if SD card is present
                if (new CheckForSDCard().isSDCardPresent()) {

                    apkStorage = new File(
                            Environment.getExternalStorageDirectory() + "/"
                                    + Environment.DIRECTORY_DOWNLOADS);
                } else
                    Toast.makeText(getActivity(), "Oops!! There is no SD Card.", Toast.LENGTH_SHORT).show();

                //If File is not present create directory
                if (!apkStorage.exists()) {
                    apkStorage.mkdir();
                    Log.e(LOG_TAG, "Directory Created.");
                }

                outputFile = new File(apkStorage, downloadFileName);//Create Output file in Main File

                //Create New File if not present
                if (!outputFile.exists()) {
                    outputFile.createNewFile();
                    Log.e(LOG_TAG, "File Created");
                }

                fos = new FileOutputStream(outputFile);//Get OutputStream for NewFile Location

                is = c.getInputStream();//Get InputStream for connection

                if (is != null) {
                    byte[] buffer = new byte[2046];//Set buffer type
                    int len1;//init length
                    // Populate temporary buffer with Stream data.
                    int numChars = 0;
                    int readSize = 0;
                    while ((len1 = is.read(buffer)) != -1) {
                        numChars += readSize;
                        int pct = (100 * numChars) / len1;
                        publishProgress(DownloadCallback.Progress.PROCESS_INPUT_STREAM_IN_PROGRESS, len1);

                        fos.write(buffer, 0, len1);//Write new file
                    }
                    result = "Downloaded";
                }

            } finally {
                //Close all connection after doing task
                if (fos != null) {
                    fos.close();
                }
                if (is != null) {
                    is.close();
                }

                outputFile = null;
            }
            return result;
        }

        /**
         * Wrapper class that serves as a union of a result value and an exception. When the
         * download task has completed, either the result value or exception can be a non-null
         * value. This allows you to pass exceptions to the UI thread that were thrown during
         * doInBackground().
         */
        class Result {
            String mResultValue;
            Exception mException;

            Result(String resultValue) {
                mResultValue = resultValue;
            }

            Result(Exception exception) {
                mException = exception;
            }
        }

    }

}
