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

        // 2. 检查是否是 IP 地址格式（支持带端口和路径，如 192.168.1.1:8080/admin）
        // 关键：必须在常规 WEB_URL 前拦截，防止 IP 被误认为域名
        if (isIpAddressFormat(query)) {
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
     * 更严谨地识别 IPv4 地址（含可选端口与路径）
     */
    private static boolean isIpAddressFormat(String input) {
        if (input == null || input.isEmpty()) return false;
        
        // 分离 Host 和 路径
        String hostPart = input.split("/")[0].trim();
        
        // 移除可选的端口号
        String ipOnly = hostPart;
        if (hostPart.contains(":")) {
            String[] parts = hostPart.split(":");
            ipOnly = parts[0];
        }

        // 匹配 IPv4 正则
        // 为确保兼容性，使用标准正则取代 Patterns.IP_ADDRESS（某些 OEM 系统中此常量定义可能不同）
        String ipv4Pattern = "^(25[0-5]|2[0-4][0-9]|[0-1]?[0-9][0-9]?)\\." +
                             "(25[0-5]|2[0-4][0-9]|[0-1]?[0-9][0-9]?)\\." +
                             "(25[0-5]|2[0-4][0-9]|[0-1]?[0-9][0-9]?)\\." +
                             "(25[0-5]|2[0-4][0-9]|[0-1]?[0-9][0-9]?)$";
        
        return ipOnly.matches(ipv4Pattern);
    }
}
