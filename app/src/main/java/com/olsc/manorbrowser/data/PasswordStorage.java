/**
 * 账号密码持久化存储类
 * 负责保存用户在网页中输入的登录凭据，并根据当前网页 URL 提供匹配的账号填充建议。
 * 存储格式为加密后的 JSON (由 PasswordItem 内部处理)。
 */
package com.olsc.manorbrowser.data;

import android.content.Context;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

public class PasswordStorage {
    /** 存储密码的文件名 */
    private static final String FILE_NAME = "passwords.json";

    /**
     * 加载所有已保存的账号密码
     * 结果按时间戳倒序排列，确保最近使用的账号在前。
     */
    public static List<PasswordItem> loadPasswords(Context context) {
        List<PasswordItem> items = new ArrayList<>();
        File file = new File(context.getFilesDir(), FILE_NAME);
        if (!file.exists()) return items;
        
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] data = new byte[(int) file.length()];
            fis.read(data);
            String jsonStr = new String(data, StandardCharsets.UTF_8);
            JSONArray array = new JSONArray(jsonStr);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                PasswordItem item = PasswordItem.fromJson(obj);
                if (item != null) {
                    items.add(item);
                }
            }
            // 排序：最新录入的基础在前
            Collections.sort(items, (p1, p2) -> Long.compare(p2.timestamp, p1.timestamp));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return items;
    }

    /**
     * 保存单条新的账号密码，若存在相同 URL+用户名 则覆盖。
     */
    public static void savePassword(Context context, PasswordItem newItem) {
        List<PasswordItem> items = loadPasswords(context);
        items.removeIf(item -> item.url.equals(newItem.url) && item.username.equals(newItem.username));
        items.add(0, newItem);
        saveAll(context, items);
    }

    /**
     * 删除单条账号密码项
     */
    public static void deletePassword(Context context, PasswordItem itemToDelete) {
        List<PasswordItem> items = loadPasswords(context);
        items.removeIf(item -> 
            item.url.equals(itemToDelete.url) && 
            item.username.equals(itemToDelete.username) &&
            item.timestamp == itemToDelete.timestamp);
        saveAll(context, items);
    }

    /**
     * 全量写回本地磁盘
     */
    private static void saveAll(Context context, List<PasswordItem> items) {
        try {
            JSONArray array = new JSONArray();
            for (PasswordItem item : items) {
                JSONObject obj = item.toJson();
                if (obj != null) array.put(obj);
            }
            File file = new File(context.getFilesDir(), FILE_NAME);
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(array.toString().getBytes(StandardCharsets.UTF_8));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据当前页面的 URL 获取匹配的账号列表（基于域名匹配）
     */
    public static List<PasswordItem> getPasswordsForUrl(Context context, String url) {
        List<PasswordItem> all = loadPasswords(context);
        List<PasswordItem> matches = new ArrayList<>();
        if (url == null) return matches;
        
        String domain = getDomain(url);
        for (PasswordItem item : all) {
            // 通过获取域名来匹配，例如 login.baidu.com 和 music.baidu.com 共享账号
            if (item.url != null && getDomain(item.url).equals(domain)) {
                matches.add(item);
            }
        }
        return matches;
    }

    /**
     * 工具方法：从 URL 中提取域名
     */
    private static String getDomain(String url) {
        try {
            java.net.URI uri = new java.net.URI(url);
            String host = uri.getHost();
            return host != null ? host : url;
        } catch (Exception e) {
            return url;
        }
    }
}