package com.mobileapplicationdev.downloadservice;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Florian on 14.01.2018.
 * DownloadService
 */

public class DownloadService extends Service {
    private static final String THREAD_NAME = "download.service.thread";

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

    /**
     * Liest die gesendeten Bytes ein und berechnet den Fortschritt von gelesenen Bytes zu der
     * gesamten Byteanzahl
     *
     * @param downloadUrlString URL des Downloads
     */
    public void startDownload(final String downloadUrlString) {
        currentProgress = 0.0;

        new Thread(new Runnable() {
            @Override
            public void run() {
                long fileSize, bytesRead, totalBytesRead = 0;
                byte[] data;
                BufferedInputStream in;
                HttpURLConnection conn;
                URL downloadUrl;

                try {
                    downloadUrl = new URL(downloadUrlString);
                    conn = (HttpURLConnection) downloadUrl.openConnection();

                    fileSize = conn.getContentLength();
                    in = new BufferedInputStream(conn.getInputStream());

                    data = new byte[1024];

                    while ((bytesRead = in.read(data, 0, 1024)) >= 0) {
                        totalBytesRead += bytesRead;
                        currentProgress = ((((double)totalBytesRead) / ((double)fileSize))) * 100;
                    }

                } catch (IOException ex) {
                    Log.e(THREAD_NAME, ex.getMessage());
                }

                stopSelf();
            }
        }, THREAD_NAME).start();
    }

    public double getCurrentProgress() {
        return currentProgress;
    }
}