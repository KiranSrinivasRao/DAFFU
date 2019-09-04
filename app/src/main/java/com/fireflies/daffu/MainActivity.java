package com.fireflies.daffu;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.fireflies.daffu.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {


    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(MainActivity.this, R.layout.activity_main);
        binding.startDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Download Clicked! ", Toast.LENGTH_LONG).show();
                //TODO (2) Call DownloadTaskLoader to download a file in background
                //TODO (3) Once Download started as a background Service disable this Button
            }
        });


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
            //TODO (2) Call DownloadTaskLoader to download a file in background
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
