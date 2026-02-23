/**
 * 搜索建议和搜索引擎辅助类，处理地址栏搜索逻辑。
 */
package com.olsc.manorbrowser.utils;
import android.content.Context;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;
import android.util.Patterns;
import android.webkit.URLUtil;
import com.olsc.manorbrowser.Config;
public class SearchHelper {
    public static String getSearchUrl(Context context, String query) {
        query = query.trim();
        // 如果已经是完整的URL（包含协议），直接返回
        if (URLUtil.isValidUrl(query)) {
            return query;
        }
        // 检查是否是IP地址（支持IPv4和带端口号的情况）
        if (isIpAddress(query)) {
            return "http://" + query;
        }
        // 检查是否是域名格式
        if (Patterns.WEB_URL.matcher(query).matches()) {
             return "https://" + query;
        }
        // 否则作为搜索关键词处理
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String engine = prefs.getString(Config.PREF_KEY_SEARCH_ENGINE, Config.ENGINE_BAIDU);
        if (Config.ENGINE_GOOGLE.equals(engine)) {
            return "https://www.google.com/search?q=" + query;
        } else {
            return "https://www.baidu.com/s?wd=" + query;
        }
    }
    /**
     * 检查是否是IP地址（支持IPv4和带端口号的情况）
     */
    private static boolean isIpAddress(String input) {
        // 分离IP和端口
        String ipPart = input;
        if (input.contains(":")) {
            String[] parts = input.split(":");
            if (parts.length == 2) {
                ipPart = parts[0];
                // 验证端口号是否有效
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
        
        // 验证IP地址格式
        return Patterns.IP_ADDRESS.matcher(ipPart).matches();
    }
}
