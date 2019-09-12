package com.fireflies.daffu;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentActivity;

import com.fireflies.daffu.databinding.ActivityMainBinding;

public class MainActivity extends FragmentActivity implements DownloadCallback {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_WRITE_STORAGE = 132;


    // Keep a reference to the NetworkFragment which owns the AsyncTask object
    // that is used to execute network ops.
    private NetworkFragment mNetworkFragment;

    // Boolean telling us whether a download is in progress, so we don't trigger overlapping
    // downloads with consecutive button clicks.
    private boolean mDownloading = false;

    private ActivityMainBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(MainActivity.this, R.layout.activity_main);
        String defaultUrl = "https://www.google.com";
        mNetworkFragment = NetworkFragment.getInstance(getSupportFragmentManager(), defaultUrl);
        binding.startDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Download Clicked! ", Toast.LENGTH_LONG).show();
                startDownload();
                binding.fileName.setText("");
            }
        });
        requestWriteExternalStoragePermission();
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
    public void updateFromDownload(String result) {
        if (result != null) {
            binding.fileName.setText(result);
        } else {
            binding.fileName.setText(getString(R.string.connection_error));
        }
    }

    @Override
    public NetworkInfo getActiveNetworkInfo() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivityManager.getActiveNetworkInfo();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.download_file) {
            Toast.makeText(MainActivity.this, "Download Clicked! ", Toast.LENGTH_LONG).show();
            startDownload();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void startDownload() {
        if (!mDownloading && mNetworkFragment != null) {
            // Execute the async download.
            mNetworkFragment.startDownload(binding.urlPath.getText().toString());
            mDownloading = true;
        }
    }

    @Override
    public void onProgressUpdate(int progressCode, int percentComplete) {


        switch (progressCode) {
            // You can add UI behavior for progress updates here.
            case Progress.ERROR:
            case Progress.CONNECTION_SUCCESS:
            case Progress.GET_INPUT_STREAM_SUCCESS:
            case Progress.PROCESS_INPUT_STREAM_SUCCESS:
                break;
            case Progress.PROCESS_INPUT_STREAM_IN_PROGRESS:
                binding.downloadProgress.setProgress(percentComplete);
                //TODO(3) Logic to update every minute
                binding.fileName.setText(String.valueOf(percentComplete).concat("%"));
                break;


        }
    }

    @Override
    public void finishDownloading() {
        mDownloading = false;
        if (mNetworkFragment != null) {
            mNetworkFragment.cancelDownload();
        }
    }
}
