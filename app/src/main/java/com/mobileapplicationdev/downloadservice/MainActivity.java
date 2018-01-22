package com.mobileapplicationdev.downloadservice;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private ProgressBar progressBar;
    private TextView textView;

    private DownloadService downloadService;
    private boolean mBound = false;
    private int progressStatus = 0;
    private Handler handler = new Handler();

    private ServiceConnection mConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            DownloadService.LocalBinder binder = (DownloadService.LocalBinder) service;
            downloadService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = findViewById(R.id.url_text);
        progressBar = findViewById(R.id.progressBar);
        Button button = findViewById(R.id.button);

        Intent intent = new Intent(this, DownloadService.class);

        if (!isServiceRunning(DownloadService.class)) {
            startService(intent);
            bindService(intent, mConn, Context.BIND_AUTO_CREATE);
        } else {
            bindService(intent, mConn, Context.BIND_AUTO_CREATE);
            textView.setActivated(false);
            button.setActivated(false);
            updateProgressBar();
        }


    }

    public void onClick(View view) throws InterruptedException {
        String text = textView.getText().toString();

        //text = "http://emrolab.htw-saarland.de/android/wp-content/uploads/2017/12/WS17-MADA-09-Persistenz.pdf";
        text = "http://eu25.gamersplatoon.com/Aslains_WoT_Modpack_Installer_v.9.21.0.3_07.exe";

        if (text.equals("")) {
            Toast.makeText(MainActivity.this, R.string.no_url_exception, Toast.LENGTH_SHORT).show();
        } else {
            if (mBound) {
                downloadService.startDownload(text);
                updateProgressBar();
            }
        }
    }

    public void updateProgressBar() {
        progressStatus = 0;

        new Thread(new Runnable() {
            public void run() {
                while (progressStatus < 100) {
                    progressStatus = (int) downloadService.getCurrentProgress();

                    handler.post(new Runnable() {
                        public void run() {
                            progressBar.setProgress(progressStatus);
                        }
                    });
                }
            }
        }).start();
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)){
            if(serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
