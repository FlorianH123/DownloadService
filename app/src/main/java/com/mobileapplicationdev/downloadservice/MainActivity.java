package com.mobileapplicationdev.downloadservice;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private static final String PROGRESSBAR_THREAD_NAME = "update.progressbar.thread";
    private ProgressBar progressBar;
    private TextView textView;

    private DownloadService downloadService;
    private boolean mBound = false;
    private int progressStatus;
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

        Intent intent = new Intent(this, DownloadService.class);

        startService(intent);
        bindService(intent, mConn, Context.BIND_AUTO_CREATE);
    }

    /**
     * Wenn der User auf die Downloadschaltflaeche klickt startet der DownloadService und
     * die Methode startDownload, der die URL, die in dem Textfeld steht, übergeben wird, gestartet
     *
     * @param view die geklickte View
     */
    public void onClick(View view) throws InterruptedException {
        String text = textView.getText().toString();

        if (text.trim().isEmpty()) {
            Toast.makeText(MainActivity.this, R.string.no_url_exception, Toast.LENGTH_SHORT).show();
        } else {
            progressBar.setProgress(0);

            if (mBound) {
                downloadService.startDownload(text);
                updateProgressBar();
            }
        }
    }

    /**
     * Holt sich den aktuellen Downloadfortschritt von dem DownloadService
     * und updatet die PrgressBar mit dem neuen Fortschritt
     */
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

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, R.string.download_finished,
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
        }, PROGRESSBAR_THREAD_NAME).start();
    }
}
