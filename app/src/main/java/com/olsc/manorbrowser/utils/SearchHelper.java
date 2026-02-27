/**
 * 搜索引擎与地址解析辅助类
 * 负责解析地址栏输入内容：判断是 URL、域名、IP 还是搜索关键词，并分发至对应搜索引擎。
 */
package com.olsc.manorbrowser.utils;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;
import android.util.Patterns;
import android.webkit.URLUtil;
import com.olsc.manorbrowser.Config;

public class SearchHelper {

    /**
     * 根据输入内容生成最终跳转的 URL
     *
     * @param context 上下文用于读取搜索引擎偏好
     * @param query   用户输入的原始字符串
     * @return 最终的 URL 字符串
     */
    public static String getSearchUrl(Context context, String query) {
        query = query.trim();

        // 1. 如果已经是完整的 URL（如 http://, https://, file:// ），直接返回
        if (URLUtil.isValidUrl(query)) {
            return query;
        }

        // 2. 检查是否是 IP 地址格式（支持 IPv4 以及带端口号的情况，如 192.168.1.1:8080）
        if (isIpAddress(query)) {
            return "http://" + query;
        }

        // 3. 检查是否是常规域名格式 (如 google.com)
        if (Patterns.WEB_URL.matcher(query).matches()) {
             return "https://" + query;
        }

        // 4. 否则判定为搜索关键词，拼接对应的搜索引擎 API
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String engine = prefs.getString(Config.PREF_KEY_SEARCH_ENGINE, Config.ENGINE_BAIDU);

        if (Config.ENGINE_GOOGLE.equals(engine)) {
            return "https://www.google.com/search?q=" + query;
        } else {
            return "https://www.baidu.com/s?wd=" + query;
        }
    }

    /**
     * 识别输入字符串是否符合 IPv4 地址格式（含端口支持）
     */
    private static boolean isIpAddress(String input) {
        // 分离 IP 部分和端口部分
        String ipPart = input;
        if (input.contains(":")) {
            String[] parts = input.split(":");
            if (parts.length == 2) {
                ipPart = parts[0];
                // 验证端口号范围是否符合标准 [1, 65535]
                try {
                    int port = Integer.parseInt(parts[1]);
                    if (port < 1 || port > 65535) {
                        return false;
                    }
                } catch (NumberFormatException e) {
                    return false;
                }
            } else {
                return false;
            }
        }
        
        // 使用系统正则表达式匹配 IP 结构
        return Patterns.IP_ADDRESS.matcher(ipPart).matches();
    }
}
