/**
 * 浏览器内置下载器
 * 使用 OkHttp 实现手动下载逻辑，支持通知栏进度显示、多线程下载以及 Firefox WebExtension (.xpi) 的自动安装。
 */
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
    /** 下载通知渠道 ID */
    private static final String CHANNEL_ID = "browser_downloads_http";
    /** 单例 OkHttpClient 实例 */
    private static OkHttpClient client;

    /**
     * 获取 OkHttpClient 单例，配置超时时间
     */
    private static synchronized OkHttpClient getClient() {
        if (client == null) {
            client = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .build();
        }
        return client;
    }

    /**
     * 执行下载的核心方法
     *
     * @param context   上下文
     * @param url       下载地址
     * @param userAgent User-Agent
     * @param cookie    Cookie
     * @param referer   Referer
     * @param filename  保存的文件名
     */
    public static void download(Context context, String url, String userAgent, String cookie, String referer, String filename) {
        Context appContext = context.getApplicationContext();
        NotificationManager notificationManager = (NotificationManager) appContext.getSystemService(Context.NOTIFICATION_SERVICE);

        // 创建通知渠道 (Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    appContext.getString(R.string.action_downloads),
                    NotificationManager.IMPORTANCE_LOW
            );
            notificationManager.createNotificationChannel(channel);
        }

        // 初始化通知
        final int notificationId = (int) System.currentTimeMillis();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(appContext, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(filename)
                .setContentText(appContext.getString(R.string.msg_download_starting))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true);

        notificationManager.notify(notificationId, builder.build());

        // 构造 OkHttp 请求
        Request.Builder reqBuilder = new Request.Builder().url(url);
        if (!TextUtils.isEmpty(userAgent)) reqBuilder.header("User-Agent", userAgent);
        if (!TextUtils.isEmpty(cookie)) reqBuilder.header("Cookie", cookie);
        if (!TextUtils.isEmpty(referer) && !referer.startsWith("about:")) reqBuilder.header("Referer", referer);

        // 开始异步网络请求
        getClient().newCall(reqBuilder.build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, java.io.IOException e) {
                // 下载失败回调
                builder.setContentText(appContext.getString(R.string.download_status_failed))
                       .setProgress(0, 0, false)
                       .setOngoing(false);
                notificationManager.notify(notificationId, builder.build());
                new Handler(Looper.getMainLooper()).post(() ->
                        Toast.makeText(appContext, appContext.getString(R.string.msg_download_failed, filename), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws java.io.IOException {
                // 响应成功逻辑
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

                // 流式写入文件并更新进度
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
                        // 每 500ms 更新一次 UI 进度
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

                    // 下载完成
                    builder.setContentText(appContext.getString(R.string.download_status_completed))
                           .setProgress(0, 0, false)
                           .setOngoing(false);

                    // 特殊逻辑：处理 Firefox 扩展文件 (.xpi)
                    if (filename.toLowerCase().endsWith(".xpi")) {
                        handleExtensionInstall(appContext, outputFile, filename);
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

    /**
     * 处理下载完成后的 Firefox 扩展安装
     */
    private static void handleExtensionInstall(Context appContext, File outputFile, String filename) {
        java.io.File tempDir = new java.io.File(appContext.getCacheDir(), "extensions");
        if (!tempDir.exists()) tempDir.mkdirs();
        tempDir.setExecutable(true, false);
        tempDir.setReadable(true, false);
        java.io.File tempFile = new java.io.File(tempDir, filename);

        try {
            // 将文件拷贝到内部缓存，以解决权限问题
            java.io.InputStream in = new java.io.FileInputStream(outputFile);
            java.io.FileOutputStream out = new java.io.FileOutputStream(tempFile);
            byte[] buf = new byte[8192];
            int len;
            while ((len = in.read(buf)) > 0) out.write(buf, 0, len);
            in.close();
            out.close();
            tempFile.setReadable(true, false);
        } catch (Exception ignored) { }

        // 调用 GeckoView 运行时的扩展控制器进行安装
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
}
