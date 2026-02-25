/**
 * 多语言/语言环境切换辅助类，支持应用内动态切换语言。
 */
package com.olsc.manorbrowser.utils;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import java.util.Locale;
public class LocaleHelper {
    private static final String SELECTED_LANGUAGE = com.olsc.manorbrowser.Config.PREF_KEY_LANGUAGE;
    public static Context onAttach(Context context) {
        String lang = getPersistedData(context, Locale.getDefault().getLanguage());
        return setLocale(context, lang);
    }
    public static Context onAttach(Context context, String defaultLanguage) {
        String lang = getPersistedData(context, defaultLanguage);
        return setLocale(context, lang);
    }
    public static String getLanguage(Context context) {
        return getPersistedData(context, Locale.getDefault().getLanguage());
    }
    public static Context setLocale(Context context, String language) {
        persist(context, language);
        return updateResources(context, language);
    }
    private static String getPersistedData(Context context, String defaultLanguage) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(SELECTED_LANGUAGE, defaultLanguage);
    }
    private static void persist(Context context, String language) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(SELECTED_LANGUAGE, language);
        editor.apply();
    }
    private static Context updateResources(Context context, String language) {
        Locale locale;
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
        return context.createConfigurationContext(config);
    }
}