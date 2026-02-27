/**
 * 多语言切换辅助类
 * 提供应用内动态切换语言环境的功能，并负责将语言设置持久化到 SharedPreferences。
 */
package com.olsc.manorbrowser.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import androidx.preference.PreferenceManager;
import java.util.Locale;

public class LocaleHelper {
    private static final String SELECTED_LANGUAGE = com.olsc.manorbrowser.Config.PREF_KEY_LANGUAGE;

    /**
     * 在 Activity 的 attachBaseContext 中调用，以确保应用启动时应用正确的语言环境
     */
    public static Context onAttach(Context context) {
        String lang = getPersistedData(context, Locale.getDefault().getLanguage());
        return setLocale(context, lang);
    }

    /**
     * 获取当前持久化的语言设置
     */
    public static String getLanguage(Context context) {
        return getPersistedData(context, Locale.getDefault().getLanguage());
    }

    /**
     * 设置并持久化新的语言环境
     */
    public static Context setLocale(Context context, String language) {
        persist(context, language);
        return updateResources(context, language);
    }

    private static String getPersistedData(Context context, String defaultLanguage) {
        // 使用默认 PreferenceManager 读取 Config 中定义的键值
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(SELECTED_LANGUAGE, defaultLanguage);
    }

    private static void persist(Context context, String language) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putString(SELECTED_LANGUAGE, language).apply();
    }

    /**
     * 更新 Context 的 Configuration 对象，并返回带有新配置的 Context
     */
    private static Context updateResources(Context context, String language) {
        Locale locale;
        // 处理区域代码，如 "zh-CN"
        if (language.contains("-")) {
            String[] parts = language.split("-");
            if (parts.length > 1) {
                locale = new Locale(parts[0], parts[1]);
            } else {
                locale = new Locale(language);
            }
        } else {
            locale = new Locale(language);
        }
        
        Locale.setDefault(locale);
        Resources res = context.getResources();
        Configuration config = new Configuration(res.getConfiguration());
        config.setLocale(locale);
        
        // Android 7.0+ 推荐使用 createConfigurationContext 方式
        return context.createConfigurationContext(config);
    }
}