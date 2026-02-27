/**
 * Intent处理辅助类，简化Activity跳转和数据传递。
 */
package com.olsc.manorbrowser.utils;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.widget.Toast;
import com.olsc.manorbrowser.R;
import java.net.URISyntaxException;
import java.util.List;
public class IntentHelper {
    /**
     * 尝试打开外部应用链接
     * @return true 如果成功处理，false 如果应该在浏览器中打开
     */
    public static boolean tryOpenExternalApp(Context context, String url) {
        if (url == null || url.isEmpty()) return false;
        
        // 1. 检查是否是intent scheme
        if (url.startsWith("intent://")) {
            return handleIntentScheme(context, url);
        }
        
        // 2. 检查是否是特殊scheme
        if (isSpecialScheme(url)) {
            return handleSpecialScheme(context, url);
        }
        
        // 3. 检查是否是应用商店链接
        if (isAppStoreLink(url)) {
            return handleAppStoreLink(context, url);
        }
        
        // 4. 检查是否是已知的应用deep link (包含在其他scheme中)
        if (isKnownAppDeepLink(url)) {
            return handleDeepLink(context, url);
        }
        
        return false;
    }

    private static boolean isSpecialScheme(String url) {
        String[] schemes = {
            "mailto:", "tel:", "sms:", "market:", "geo:",
            "whatsapp:", "tg:", "telegram:", "wechat:", "weixin:",
            "alipay:", "alipays:", "taobao:", "tmall:", "jd:", "openjd:", 
            "douyin:", "snssdk1128:", "snssdk1112:",
            "bilibili:", "zhihu:", "weibo:", "sinaweibo:",
            "xhsdiscover:", "xhslink:", "xiaohongshu:",
            "meituan:", "dianping:", "eleme:", "pinduoduo:",
            "baiduboxapp:", "baidumap:", "diditaxi:",
            "youku:", "tenvideo:", "iqiyi:",
            "youtube:", "twitter:", "facebook:", "instagram:", "snapchat:", "tiktok:",
            "spotify:", "netflix:", "maps:"
        };
        String lowerUrl = url.toLowerCase();
        for (String scheme : schemes) {
            if (lowerUrl.startsWith(scheme)) {
                return true;
            }
        }
        return false;
    }

    private static boolean handleSpecialScheme(Context context, String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            
            PackageManager pm = context.getPackageManager();
            List<ResolveInfo> activities = pm.queryIntentActivities(intent, 0);
            
            if (activities != null && !activities.isEmpty()) {
                // 如果只有一个匹配项（非浏览器），直接设置包名以确保跳转
                if (activities.size() == 1) {
                    intent.setPackage(activities.get(0).activityInfo.packageName);
                }
                context.startActivity(intent);
                return true;
            } else {
                // 如果没有找到应用且是http/https开头的（由于某些原因被判定为special），则返回false让浏览器处理
                if (url.startsWith("http")) {
                    return false;
                }
                Toast.makeText(context, R.string.error_no_app_found, Toast.LENGTH_SHORT).show();
                return true; 
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean handleIntentScheme(Context context, String url) {
        try {
            Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            
            PackageManager pm = context.getPackageManager();
            ResolveInfo info = pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
            
            if (info != null) {
                context.startActivity(intent);
                return true;
            } else {
                // 尝试获取fallback URL
                String fallbackUrl = intent.getStringExtra("browser_fallback_url");
                if (fallbackUrl != null && !fallbackUrl.isEmpty()) {
                    // 如果有fallback URL，在这里不处理，返回false让浏览器加载这个URL
                    // 但我们需要在此时修改浏览器的当前URL，这在Static方法里做不到
                    // 所以我们让MainActivity来处理这种情况
                    return false; 
                }
                
                // 尝试打开应用商店
                String packageName = intent.getPackage();
                if (packageName != null && !packageName.isEmpty()) {
                    openAppStore(context, packageName);
                    return true;
                }
                
                Toast.makeText(context, R.string.error_no_app_found, Toast.LENGTH_SHORT).show();
                return true;
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean isAppStoreLink(String url) {
        return url.contains("play.google.com/store/apps") ||
               url.contains("apps.apple.com") ||
               url.contains("itunes.apple.com");
    }

    private static boolean handleAppStoreLink(Context context, String url) {
        try {
            // 提取包名
            String packageName = null;
            if (url.contains("id=")) {
                int start = url.indexOf("id=") + 3;
                int end = url.indexOf("&", start);
                if (end == -1) end = url.length();
                packageName = url.substring(start, end);
            }
            if (packageName != null && !packageName.isEmpty()) {
                openAppStore(context, packageName);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static void openAppStore(Context context, String packageName) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("market://details?id=" + packageName));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=" + packageName));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

    private static boolean isKnownAppDeepLink(String url) {
        String[] patterns = {
            "://open", "://launch", "://app", "://share",
            "weixin://", "alipay://", "taobao://", "tmall://",
            "jd://", "openjd://", "douyin://", "snssdk1128://", "bilibili://", "zhihu://",
            "weibo://", "sinaweibo://", "qq://", "mqqapi://", "tim://", 
            "xhsdiscover://", "xhslink://", "xiaohongshu://",
            "xhslink.com", "sns.xhs.com", "xiaohongshu.com",
            "bilibili.com", "b23.tv", "biliapi://",
            "zhihu.com", "zhihu://",
            "douyin.com", "snssdk1128://", "amemv.com",
            "taobao.com", "tmall.com", "jd.com", "pinduoduo.com",
            "meituan.com", "dianping.com", "ele.me"
        };
        String lowerUrl = url.toLowerCase();
        for (String pattern : patterns) {
            if (lowerUrl.contains(pattern)) {
                return true;
            }
        }
        return false;
    }

    private static boolean handleDeepLink(Context context, String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            
            PackageManager pm = context.getPackageManager();
            List<ResolveInfo> activities = pm.queryIntentActivities(intent, 0);
            
            // 过滤掉浏览器，防止循环跳转
            if (activities != null) {
                activities.removeIf(info -> {
                    String packageName = info.activityInfo.packageName;
                    return packageName.contains("com.olsc.manorbrowser") || 
                           packageName.contains("browser") || 
                           packageName.contains("chrome") ||
                           packageName.contains("firefox");
                });
            }
            
            if (activities != null && !activities.isEmpty()) {
                // 如果只有一个匹配项（非浏览器），直接设置包名以确保跳转
                if (activities.size() == 1) {
                    intent.setPackage(activities.get(0).activityInfo.packageName);
                }
                context.startActivity(intent);
                return true;
            } else {
                return false; // 让浏览器尝试处理
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean shouldInterceptUrl(String url) {
        if (url == null || url.isEmpty()) return false;
        
        // HTTP/HTTPS通常在浏览器中打开，除非是已知的App Link
        if (url.startsWith("http://") || url.startsWith("https://")) {
            return isAppStoreLink(url) || isKnownAppDeepLink(url);
        }
        
        // 排除掉浏览器内部协议
        return !url.startsWith("about:") && 
               !url.startsWith("data:") && 
               !url.startsWith("javascript:") &&
               !url.startsWith("resource:") &&
               !url.startsWith("chrome:");
    }
}
