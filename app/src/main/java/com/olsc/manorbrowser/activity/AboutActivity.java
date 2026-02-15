package com.olsc.manorbrowser.activity;

import com.olsc.manorbrowser.R;
import com.olsc.manorbrowser.Config;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.WindowCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.core.graphics.Insets;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        android.content.SharedPreferences prefs = getSharedPreferences(Config.PREF_NAME_THEME, MODE_PRIVATE);
        boolean isDarkMode = prefs.getBoolean(Config.PREF_KEY_DARK_MODE, false);
        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(isDarkMode ? 
            androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES : androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (controller != null) {
            controller.setAppearanceLightStatusBars(!isDarkMode);
            controller.setAppearanceLightNavigationBars(!isDarkMode);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.title_about);
        }

        View mainView = findViewById(android.R.id.content);
        ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            toolbar.setPadding(toolbar.getPaddingLeft(), insets.top, toolbar.getPaddingRight(), 0);
            v.setPadding(v.getPaddingLeft(), 0, v.getPaddingRight(), insets.bottom);
            return windowInsets;
        });

        View llSourceCode = findViewById(R.id.ll_source_code);
        if (llSourceCode != null) {
            llSourceCode.setOnClickListener(v -> {
                String url = getString(R.string.source_code_url);
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            });
        }
    }

    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        super.attachBaseContext(com.olsc.manorbrowser.utils.LocaleHelper.onAttach(newBase));
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
