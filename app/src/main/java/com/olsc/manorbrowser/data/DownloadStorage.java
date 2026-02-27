/**
 * 下载历史持久化类
 * 负责保存和读取浏览器内置下载器的下载记录。
 */
package com.olsc.manorbrowser.data;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class DownloadStorage {
    private static final String PREF_NAME = "download_storage";
    private static final String KEY_DOWNLOADS = "internal_downloads";

    public static synchronized void saveDownload(Context context, DownloadInfo info) {
        if (info == null) return;
        List<DownloadInfo> list = getAllDownloads(context);
        
        // 检查是否存在（根据 ID 更新）
        boolean found = false;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).id == info.id) {
                list.set(i, info);
                found = true;
                break;
            }
        }
        if (!found) {
            list.add(0, info); // 新任务放在最前面
        }
        
        saveAll(context, list);
    }

    public static synchronized void removeDownload(Context context, long id) {
        List<DownloadInfo> list = getAllDownloads(context);
        list.removeIf(info -> info.id == id);
        saveAll(context, list);
    }

    public static List<DownloadInfo> getAllDownloads(Context context) {
        List<DownloadInfo> list = new ArrayList<>();
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_DOWNLOADS, "[]");
        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                DownloadInfo info = new DownloadInfo(
                    obj.getLong("id"),
                    obj.getString("title"),
                    obj.getString("url"),
                    obj.optString("filePath", null),
                    obj.optString("mimeType", null)
                );
                info.status = obj.getInt("status");
                info.totalBytes = obj.getLong("totalBytes");
                info.currentBytes = obj.getLong("currentBytes");
                info.timestamp = obj.getLong("timestamp");
                info.isInternal = obj.optBoolean("isInternal", false);
                list.add(info);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    private static void saveAll(Context context, List<DownloadInfo> list) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        try {
            JSONArray array = new JSONArray();
            // 限制保存最近的 100 条记录
            int count = Math.min(list.size(), 100);
            for (int i = 0; i < count; i++) {
                DownloadInfo info = list.get(i);
                JSONObject obj = new JSONObject();
                obj.put("id", info.id);
                obj.put("title", info.title);
                obj.put("url", info.url);
                obj.put("filePath", info.filePath);
                obj.put("mimeType", info.mimeType);
                obj.put("status", info.status);
                obj.put("totalBytes", info.totalBytes);
                obj.put("currentBytes", info.currentBytes);
                obj.put("timestamp", info.timestamp);
                obj.put("isInternal", info.isInternal);
                array.put(obj);
            }
            prefs.edit().putString(KEY_DOWNLOADS, array.toString()).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
