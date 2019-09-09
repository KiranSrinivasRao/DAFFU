package com.fireflies.daffu;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.fireflies.daffu.databinding.ActivityMainBinding;

import java.io.File;

public class DownloadActivity extends AppCompatActivity {


    private static final String TAG = DownloadActivity.class.getSimpleName();

    private static final int REQUEST_WRITE_STORAGE = 122; // Random unique number
    private static final String DOWNLOADS_DIR = "Downloads";
    final String[] path = {"https://github.com/KiranSrinivasRao/mlkit-material-android/archive/master.zip"};

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(DownloadActivity.this, R.layout.activity_main);

        binding.startDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(DownloadActivity.this, "Download Clicked! ", Toast.LENGTH_LONG).show();

                if (isConnectingToInternet()) {

                    if (!binding.urlPath.getText().toString().isEmpty()) {

                        path[0] = binding.urlPath.getText().toString();

                    }
                    // update Seekbar every minute
                    new MyProgressClass().execute(5);

                    new DownloadTask(DownloadActivity.this, path[0]);
                }
            }
        });

        binding.dummyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                beginDownloadInBackground();
            }
        });

        requestWriteExternalStoragePermission();
    }

    //Check if internet is present or not
    private boolean isConnectingToInternet() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager
                .getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    /*
     * Begins to download the file specified by the key in the bucket.
     */
    private void beginDownloadInBackground() {
        // Location to download files from URL to. You can choose any accessible
        // file.
        File file = new File(Environment.getExternalStorageDirectory().toString() + "/" + DOWNLOADS_DIR);

        if (!binding.urlPath.getText().toString().isEmpty()) {
            path[0] = binding.urlPath.getText().toString();
        }
        // Wrap the download call from a background service to
        // support long-running downloads. Uncomment the following
        // code in order to start a download from the background
        // service.
        Context context = getApplicationContext();
        Intent intent = new Intent(context, MyService.class);
        intent.putExtra(MyService.INTENT_URL_NAME, path[0]);
        intent.putExtra(MyService.INTENT_TRANSFER_OPERATION, MyService.TRANSFER_OPERATION_DOWNLOAD);
        intent.putExtra(MyService.INTENT_FILE, file);
        context.startService(intent);

    }

    private void requestWriteExternalStoragePermission() {
        //ask for the permission in android M
        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission to store data in external storage is not granted");

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Permission to access the External Storage is required " +
                        "for this application to store the downloaded data from website " +
                        "url entered by the user")
                        .setTitle("Permission required");

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {
                        Log.i(TAG, "Clicked");
                        makeRequest();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();

            } else {
                makeRequest();
            }
        } else {
            Log.i(TAG, "Permission to store data in external storage is granted.");
        }

    }

    protected void makeRequest() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                REQUEST_WRITE_STORAGE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.download_file) {
            Toast.makeText(DownloadActivity.this, "Download Clicked! ", Toast.LENGTH_LONG).show();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("StaticFieldLeak")
    private class MyProgressClass extends AsyncTask<Integer, Integer, Integer> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            binding.startDownload.setText(getResources().getString(R.string.started));
        }

        @Override
        protected Integer doInBackground(Integer... integers) {
            try {

                for (int i = 0; i < integers[0]; i++) {
                    Thread.sleep(1000 * 60);
                    publishProgress(i);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            binding.downloadProgress.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);

            binding.startDownload.setText(getResources().getString(R.string.completed));
        }
    }
}
