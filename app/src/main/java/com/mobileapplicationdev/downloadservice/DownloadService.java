package com.mobileapplicationdev.downloadservice;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Florian on 14.01.2018.
 * DownloadService
 */

public class DownloadService extends Service {
    private final IBinder mBinder = new LocalBinder();
    private double currentProgress;

    class LocalBinder extends Binder {
        DownloadService getService() {
            return DownloadService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void startDownload(final String downloadUrl) {
        currentProgress = 0.0;

        new Thread(new Runnable() {
            @Override
            public void run() {
                long fileSize;
                int bytesRead;
                int totalBytesRead = 0;
                double currentProgress;

                try {
                    URL url = new URL(downloadUrl);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                    fileSize = conn.getContentLength();
                    BufferedInputStream in = new BufferedInputStream(conn.getInputStream());

                    byte[] data = new byte[1024];

                    while ((bytesRead = in.read(data, 0, 1024)) >= 0) {
                        totalBytesRead += bytesRead;

                        currentProgress = ((((double)totalBytesRead) / ((double)fileSize))) * 100;
                        setCurrentProgress(currentProgress);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                stopSelf();
            }
        }, "my.thread.name").start();
    }

    public void setCurrentProgress(double currentProgress) {
        this.currentProgress = currentProgress;
    }

    public double getCurrentProgress() {
        return currentProgress;
    }
}