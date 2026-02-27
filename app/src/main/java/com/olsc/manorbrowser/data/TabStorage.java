/**
 * 标签页持久化存储类
 * 负责保存和恢复用户当前打开的所有标签页（URL、标题）及其缩略图。
 * 使用后台线程池处理磁盘写入，缩略图以 PNG 格式单独存放在子目录中。
 */
package com.olsc.manorbrowser.data;

import com.olsc.manorbrowser.Config;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.os.Handler;
import android.os.Looper;

public class TabStorage {
    /** 标签列表配置文件名 */
    private static final String FILE_NAME = "tabs.json";
    /** 缩略图存放子目录 */
    private static final String THUMB_DIR = "thumbnails";
    
    /** 专用于处理标签磁盘 IO 的后台线程池 */
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    /** 缩略图异步加载回调接口 */
    public interface ThumbnailCallback {
        void onLoaded(Bitmap bitmap);
    }

    /**
     * 保存所有标签页数据到磁盘（异步执行）
     */
    public static void saveTabs(Context context, List<TabInfo> tabs) {
        executor.execute(() -> {
            try {
                JSONArray jsonArray = new JSONArray();
                for (TabInfo tab : tabs) {
                    JSONObject json = new JSONObject();
                    json.put("id", tab.id);
                    json.put("url", tab.url);
                    json.put("title", tab.title);
                    jsonArray.put(json);
                    
                    // 同时保存对应的缩略图
                    saveThumbnail(context, tab);
                }
                
                File file = new File(context.getFilesDir(), FILE_NAME);
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    fos.write(jsonArray.toString().getBytes());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * 同步加载保存好的标签页信息（不含大图缩略图）
     */
    public static List<TabInfo> loadTabs(Context context) {
        List<TabInfo> tabs = new ArrayList<>();
        try {
            File file = new File(context.getFilesDir(), FILE_NAME);
            if (!file.exists()) return tabs;
            
            FileInputStream fis = new FileInputStream(file);
            byte[] data = new byte[(int) file.length()];
            int readBytes = fis.read(data);
            fis.close();
            
            if (readBytes > 0) {
                String jsonStr = new String(data, 0, readBytes);
                JSONArray jsonArray = new JSONArray(jsonStr);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject json = jsonArray.getJSONObject(i);
                    TabInfo tab = new TabInfo(null); // 注意：此时 Session 为空，需由调用方后续创建
                    tab.id = json.optLong("id", System.currentTimeMillis());
                    tab.url = json.optString("url", Config.URL_BLANK);
                    tab.title = json.optString("title", "New Tab");
                    tab.thumbnail = null; 
                    tabs.add(tab);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tabs;
    }

    /**
     * 异步加载指定标签的缩略图
     */
    public static void loadThumbnailAsync(Context context, long id, ThumbnailCallback callback) {
        executor.execute(() -> {
            Bitmap bmp = loadThumbnail(context, id);
            new Handler(Looper.getMainLooper()).post(() -> {
                if (callback != null) {
                    callback.onLoaded(bmp);
                }
            });
        });
    }

    /**
     * 将 Bitmap 缩略图保存到磁盘文件
     */
    private static void saveThumbnail(Context context, TabInfo tab) {
        if (tab.thumbnail == null) return;
        try {
            File dir = new File(context.getFilesDir(), THUMB_DIR);
            if (!dir.exists()) dir.mkdirs();
            File file = new File(dir, tab.id + ".png");
            try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file))) {
                tab.thumbnail.compress(Bitmap.CompressFormat.PNG, 80, bos);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 从磁盘文件读取缩略图
     */
    private static Bitmap loadThumbnail(Context context, long id) {
        try {
            File file = new File(new File(context.getFilesDir(), THUMB_DIR), id + ".png");
            if (file.exists()) {
                return BitmapFactory.decodeFile(file.getAbsolutePath());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
