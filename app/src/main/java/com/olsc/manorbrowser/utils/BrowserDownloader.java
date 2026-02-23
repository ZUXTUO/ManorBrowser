package com.olsc.manorbrowser.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.olsc.manorbrowser.R;
import com.olsc.manorbrowser.activity.MainActivity;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

public class BrowserDownloader {
    private static final String CHANNEL_ID = "browser_downloads_http";

    private static OkHttpClient client;

    private static synchronized OkHttpClient getClient() {
        if (client == null) {
            client = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .build();
        }
        return client;
    }

    public static void download(Context context, String url, String userAgent, String cookie, String referer, String filename) {
        Context appContext = context.getApplicationContext();
        NotificationManager notificationManager = (NotificationManager) appContext.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    appContext.getString(R.string.action_downloads),
                    NotificationManager.IMPORTANCE_LOW
            );
            notificationManager.createNotificationChannel(channel);
        }

        final int notificationId = (int) System.currentTimeMillis();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(appContext, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(filename)
                .setContentText(appContext.getString(R.string.msg_download_starting))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true);

        notificationManager.notify(notificationId, builder.build());

        Request.Builder reqBuilder = new Request.Builder().url(url);
        if (!TextUtils.isEmpty(userAgent)) reqBuilder.header("User-Agent", userAgent);
        if (!TextUtils.isEmpty(cookie)) reqBuilder.header("Cookie", cookie);
        if (!TextUtils.isEmpty(referer) && !referer.startsWith("about:")) reqBuilder.header("Referer", referer);

        getClient().newCall(reqBuilder.build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, java.io.IOException e) {
                builder.setContentText(appContext.getString(R.string.download_status_failed))
                       .setProgress(0, 0, false)
                       .setOngoing(false);
                notificationManager.notify(notificationId, builder.build());
                new Handler(Looper.getMainLooper()).post(() -> 
                        Toast.makeText(appContext, appContext.getString(R.string.msg_download_failed, filename), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws java.io.IOException {
                if (!response.isSuccessful()) {
                    builder.setContentText(appContext.getString(R.string.download_status_failed))
                           .setProgress(0, 0, false)
                           .setOngoing(false);
                    notificationManager.notify(notificationId, builder.build());
                    new Handler(Looper.getMainLooper()).post(() -> 
                            Toast.makeText(appContext, appContext.getString(R.string.msg_download_failed, filename), Toast.LENGTH_SHORT).show());
                    return;
                }

                File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                if (!downloadDir.exists()) downloadDir.mkdirs();
                File outputFile = new File(downloadDir, filename);

                try (InputStream is = response.body().byteStream();
                     FileOutputStream fos = new FileOutputStream(outputFile)) {

                    long totalLength = response.body().contentLength();
                    long downloaded = 0;
                    byte[] buffer = new byte[8192];
                    int read;
                    long lastUpdateTime = 0;

                    while ((read = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, read);
                        downloaded += read;

                        long currentTime = System.currentTimeMillis();
                        if (currentTime - lastUpdateTime > 500) {
                            lastUpdateTime = currentTime;
                            if (totalLength > 0) {
                                int progress = (int) ((downloaded * 100) / totalLength);
                                builder.setProgress(100, progress, false);
                                builder.setContentText(String.format(appContext.getString(R.string.download_status_running), progress));
                            } else {
                                builder.setProgress(0, 0, true);
                                builder.setContentText(appContext.getString(R.string.download_status_waiting) + " " + (downloaded / 1024) + " KB");
                            }
                            notificationManager.notify(notificationId, builder.build());
                        }
                    }
                    fos.flush();
                    
                    builder.setContentText(appContext.getString(R.string.download_status_completed))
                           .setProgress(0, 0, false)
                           .setOngoing(false);

                    if (filename.toLowerCase().endsWith(".xpi")) {
                        java.io.File tempDir = new java.io.File(appContext.getCacheDir(), "extensions");
                        if (!tempDir.exists()) tempDir.mkdirs();
                        tempDir.setExecutable(true, false);
                        tempDir.setReadable(true, false);
                        java.io.File tempFile = new java.io.File(tempDir, filename);
                        try {
                            java.io.InputStream in = new java.io.FileInputStream(outputFile);
                            java.io.FileOutputStream out = new java.io.FileOutputStream(tempFile);
                            byte[] buf = new byte[8192];
                            int len;
                            while ((len = in.read(buf)) > 0) out.write(buf, 0, len);
                            in.close();
                            out.close();
                            tempFile.setReadable(true, false);
                        } catch (Exception e) {}

                        if (MainActivity.sRuntime != null) {
                            new Handler(Looper.getMainLooper()).post(() -> {
                                if (MainActivity.sRuntime != null) {
                                    MainActivity.sRuntime.getWebExtensionController().install("file://" + tempFile.getAbsolutePath())
                                        .accept(
                                            ext -> {
                                                new Handler(Looper.getMainLooper()).post(() -> 
                                                    Toast.makeText(appContext, appContext.getString(R.string.msg_extension_installed, ext.metaData.name), Toast.LENGTH_LONG).show()
                                                );
                                            },
                                            e -> {
                                                new Handler(Looper.getMainLooper()).post(() -> 
                                                    Toast.makeText(appContext, appContext.getString(R.string.msg_extension_install_failed), Toast.LENGTH_SHORT).show()
                                                );
                                            }
                                        );
                                }
                            });
                        }
                    }

                    builder.setAutoCancel(true);
                    notificationManager.notify(notificationId, builder.build());
                    new Handler(Looper.getMainLooper()).post(() -> 
                            Toast.makeText(appContext, appContext.getString(R.string.download_status_completed), Toast.LENGTH_SHORT).show());
                            
                } catch (Exception e) {
                    builder.setContentText(appContext.getString(R.string.download_status_failed))
                           .setProgress(0, 0, false)
                           .setOngoing(false);
                    notificationManager.notify(notificationId, builder.build());
                }
            }
        });
    }
}
