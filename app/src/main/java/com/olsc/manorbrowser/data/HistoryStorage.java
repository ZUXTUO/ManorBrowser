/**
 * 历史记录持久化存储类
 * 负责网页访问历史的记录、查询、单条删除以及全量清理。
 * 使用异步单线程池 (ExecutorService) 保证磁盘 IO 不阻塞主线程。
 */
package com.olsc.manorbrowser.data;

import com.olsc.manorbrowser.Config;
import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HistoryStorage {
    private static final String FILE_NAME = "history.json";
    /** 最大历史记录保存条数 */
    private static final int MAX_HISTORY_SIZE = 500;
    
    /** 专用于处理历史记录磁盘操作的后台线程池 */
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    /**
     * 历史记录项数据模型
     */
    public static class HistoryItem {
        public String title;
        public String url;
        public long timestamp;

        public HistoryItem(String title, String url, long timestamp) {
            this.title = title;
            this.url = url;
            this.timestamp = timestamp;
        }
    }

    /**
     * 添加一条历史记录（异步执行）
     * 如果 URL 已存在，则先移除旧纪录，将最新纪录置顶。
     */
    public static void addHistory(Context context, String title, String url) {
        if (url == null || url.isEmpty() || url.equals(Config.URL_BLANK)) return;
        
        executor.execute(() -> {
            List<HistoryItem> history = loadHistory(context);
            // 去重逻辑：移除已存在的相同 URL 的旧历史
            history.removeIf(item -> item.url.equals(url));
            // 将新网页加入顶部
            history.add(0, new HistoryItem(title, url, System.currentTimeMillis()));
            
            // 限制最大存储量
            if (history.size() > MAX_HISTORY_SIZE) {
                history = history.subList(0, MAX_HISTORY_SIZE);
            }
            saveHistory(context, history);
        });
    }

    /**
     * 从本地文件同步加载历史记录列表
     */
    public static List<HistoryItem> loadHistory(Context context) {
        List<HistoryItem> history = new ArrayList<>();
        try {
            File file = new File(context.getFilesDir(), FILE_NAME);
            if (!file.exists()) return history;
            
            FileInputStream fis = new FileInputStream(file);
            byte[] data = new byte[(int) file.length()];
            int readBytes = fis.read(data);
            fis.close();
            
            if (readBytes > 0) {
                String jsonStr = new String(data, 0, readBytes);
                JSONArray jsonArray = new JSONArray(jsonStr);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject json = jsonArray.getJSONObject(i);
                    history.add(new HistoryItem(
                            json.optString("title"),
                            json.optString("url"),
                            json.optLong("timestamp")
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return history;
    }

    /**
     * 将历史记录全量同步写回本地磁盘
     */
    private static void saveHistory(Context context, List<HistoryItem> history) {
        try {
            JSONArray jsonArray = new JSONArray();
            for (HistoryItem item : history) {
                JSONObject json = new JSONObject();
                json.put("title", item.title);
                json.put("url", item.url);
                json.put("timestamp", item.timestamp);
                jsonArray.put(json);
            }
            File file = new File(context.getFilesDir(), FILE_NAME);
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(jsonArray.toString().getBytes());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 清空所有历史记录
     */
    public static void clearHistory(Context context) {
        executor.execute(() -> {
            File file = new File(context.getFilesDir(), FILE_NAME);
            if (file.exists()) file.delete();
        });
    }

    /**
     * 删除单条指定的历史记录
     */
    public static void deleteHistoryItem(Context context, HistoryItem itemToDelete) {
        executor.execute(() -> {
            List<HistoryItem> history = loadHistory(context);
            history.removeIf(item -> 
                item.url.equals(itemToDelete.url) && 
                item.timestamp == itemToDelete.timestamp);
            saveHistory(context, history);
        });
    }
}
