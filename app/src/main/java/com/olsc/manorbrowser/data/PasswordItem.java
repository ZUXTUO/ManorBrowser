/**
 * 账号密码项实体类
 * 存储特定 URL 下对应的登录凭据（用户名、密码）及创建/修改的时间戳。
 */
package com.olsc.manorbrowser.data;

import org.json.JSONException;
import org.json.JSONObject;

public class PasswordItem {
    /** 站点 URL */
    public String url;
    /** 用户名 */
    public String username;
    /** 密码 */
    public String password;
    /** 时间戳 */
    public long timestamp;

    public PasswordItem(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 转换为 JSON 对象
     */
    public JSONObject toJson() {
        try {
            JSONObject json = new JSONObject();
            json.put("url", url);
            json.put("username", username);
            json.put("password", password);
            json.put("timestamp", timestamp);
            return json;
        } catch (JSONException e) {
            return null;
        }
    }

    /**
     * 从 JSON 对象解析
     */
    public static PasswordItem fromJson(JSONObject json) {
        if (json == null) return null;
        PasswordItem item = new PasswordItem(
            json.optString("url"),
            json.optString("username"),
            json.optString("password")
        );
        // 恢复原始时间戳
        item.timestamp = json.optLong("timestamp", item.timestamp);
        return item;
    }
}
