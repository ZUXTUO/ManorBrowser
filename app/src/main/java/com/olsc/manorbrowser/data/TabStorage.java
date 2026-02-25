/**
 * 标签页持久化存储类，负责保存当前打开的标签页状态。
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
    private static final String FILE_NAME = "tabs.json";
    private static final String THUMB_DIR = "thumbnails";
    
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    public interface ThumbnailCallback {
        void onLoaded(Bitmap bitmap);
    }

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
                    TabInfo tab = new TabInfo(null);
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
