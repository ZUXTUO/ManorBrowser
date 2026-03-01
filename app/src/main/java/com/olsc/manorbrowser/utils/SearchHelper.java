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

        // 1. 如果已经包含协议，直接返回
        String lowerQuery = query.toLowerCase();
        if (lowerQuery.startsWith("http://") || lowerQuery.startsWith("https://") ||
            lowerQuery.startsWith("file://") || lowerQuery.startsWith("about:") ||
            lowerQuery.startsWith("javascript:") || lowerQuery.startsWith("data:")) {
            return query;
        }

        // 2. 检查是否是 IP 地址或局域网特有域名，默认使用 http://
        // 这是为了解决局域网内大量 HTTP 服务被误强制跳转到 HTTPS 的问题
        if (isLocalOrIpFormat(query)) {
            return "http://" + query;
        }

        // 3. 检查是否是常规域名格式 (如 google.com)
        if (Patterns.WEB_URL.matcher(query).matches()) {
             // 对于外网常规域名，优先使用 HTTPS 确保安全
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
     * 判断输入内容是否为局域网地址、IP 地址或直连主机名
     */
    private static boolean isLocalOrIpFormat(String input) {
        if (input == null || input.isEmpty()) return false;
        
        // 分离 Host 和 路径/端口
        String hostPart = input.split("/")[0].trim();
        
        // 移除可选的端口号进行主机名校验
        String ipOrHost = hostPart;
        if (hostPart.contains(":")) {
            try {
                ipOrHost = hostPart.split(":")[0];
            } catch (Exception ignored) {}
        }

        // 1. 匹配 IPv4 地址 (使用 Android 标准库正则)
        if (Patterns.IP_ADDRESS.matcher(ipOrHost).matches()) {
            return true;
        }
        
        // 2. 匹配 localhost
        if ("localhost".equalsIgnoreCase(ipOrHost) || "127.0.0.1".equals(ipOrHost)) {
            return true;
        }
        
        // 3. 检查局域网特有的常见域名后缀 (如 .local, .lan, .home 等)
        String lowHost = ipOrHost.toLowerCase();
        if (lowHost.endsWith(".local") || lowHost.endsWith(".lan") || 
            lowHost.endsWith(".home") || lowHost.endsWith(".internal")) {
            return true;
        }

        // 4. (可选) 检查是否为纯主机名（不含点，且带有端口号，如 nas:5000）
        // 如果包含端口号且不含点，通常是内网服务
        if (hostPart.contains(":") && !ipOrHost.contains(".")) {
            return true;
        }

        // 5. 检查是否为 IPv6 地址格式 (如 [::1] 或 [fe80::...])
        if (ipOrHost.startsWith("[") && ipOrHost.endsWith("]")) {
            return true;
        }

        return false;
    }
}


