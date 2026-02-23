package com.olsc.manorbrowser.activity;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.olsc.manorbrowser.R;
import com.olsc.manorbrowser.adapter.CookieAdapter;
import com.olsc.manorbrowser.data.CookieItem;
import com.olsc.manorbrowser.data.CookieDisplayItem;
import com.olsc.manorbrowser.Config;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import org.mozilla.geckoview.StorageController;
import androidx.core.view.WindowCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.core.graphics.Insets;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Cookies 和 网站数据管理界面。
 * 支持按域名分组显示数据。
 */
public class CookieManagerActivity extends AppCompatActivity {
    private static final String TAG = "CookieManager";
    private RecyclerView recyclerView;
    private CookieAdapter adapter;
    private List<CookieDisplayItem> displayList = new ArrayList<>();
    private Map<String, List<CookieItem>> groupedCookies = new TreeMap<>();
    private String currentDomain = null;
    private TextView tvEmpty;
    private Toolbar toolbar;

    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        super.attachBaseContext(com.olsc.manorbrowser.utils.LocaleHelper.onAttach(newBase));
    }

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
        setContentView(R.layout.activity_cookies);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        View mainView = findViewById(android.R.id.content);
        ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            toolbar.setPadding(toolbar.getPaddingLeft(), insets.top, toolbar.getPaddingRight(), 0);
            v.setPadding(v.getPaddingLeft(), 0, v.getPaddingRight(), insets.bottom);
            return windowInsets;
        });

        recyclerView = findViewById(R.id.rv_cookies);
        tvEmpty = findViewById(R.id.tv_empty);
        findViewById(R.id.fab_clear_all).setOnClickListener(v -> showClearAllDialog());

        adapter = new CookieAdapter(displayList, new CookieAdapter.OnCookieActionListener() {
            @Override
            public void onDomainClick(String domain) {
                currentDomain = domain;
                refreshDisplayList();
            }

            @Override
            public void onDomainLongClick(String domain) {
                showDeleteDomainDialog(domain);
            }

            @Override
            public void onDeleteCookie(CookieItem item, int position) {
                deleteCookieFromHost(item, position);
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loadCookiesFromEngine();

        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (currentDomain != null) {
                    currentDomain = null;
                    refreshDisplayList();
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                    setEnabled(true);
                }
            }
        });
    }

    private void refreshDisplayList() {
        displayList.clear();
        if (currentDomain == null) {
            toolbar.setTitle(R.string.title_cookies);
            for (Map.Entry<String, List<CookieItem>> entry : groupedCookies.entrySet()) {
                displayList.add(new CookieDisplayItem(entry.getKey(), entry.getValue().size()));
            }
        } else {
            toolbar.setTitle(currentDomain);
            List<CookieItem> cookies = groupedCookies.get(currentDomain);
            if (cookies != null) {
                for (CookieItem cookie : cookies) {
                    displayList.add(new CookieDisplayItem(cookie));
                }
            }
        }
        adapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void loadCookiesFromEngine() {
        groupedCookies.clear();
        File filesDir = getFilesDir();
        findAndReadCookies(filesDir);
        
        if (groupedCookies.isEmpty()) {
            findAndReadCookies(new File(filesDir, "geckoview"));
            findAndReadCookies(new File(filesDir, "mozilla"));
        }
        
        refreshDisplayList();
    }

    private void findAndReadCookies(File dir) {
        if (dir == null || !dir.exists() || !dir.isDirectory()) return;
        
        File[] files = dir.listFiles();
        if (files == null) return;
        
        for (File file : files) {
            if (file.isDirectory()) {
                findAndReadCookies(file);
            } else if (file.getName().equals("cookies.sqlite")) {
                loadFromDatabase(file);
            }
        }
    }

    private void loadFromDatabase(File dbFile) {
        File tempDb = new File(getCacheDir(), "temp_cookies.sqlite");
        try {
            copyFile(dbFile, tempDb);
            SQLiteDatabase db = SQLiteDatabase.openDatabase(tempDb.getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY);
            Cursor cursor = db.rawQuery("SELECT name, value, host, path, isHttpOnly FROM moz_cookies", null);
            
            int nameIdx = cursor.getColumnIndex("name");
            int valIdx = cursor.getColumnIndex("value");
            int hostIdx = cursor.getColumnIndex("host");
            int pathIdx = cursor.getColumnIndex("path");
            int httpIdx = cursor.getColumnIndex("isHttpOnly");
            
            while (cursor.moveToNext()) {
                String name = cursor.getString(nameIdx);
                String value = cursor.getString(valIdx);
                String host = cursor.getString(hostIdx);
                String path = cursor.getString(pathIdx);
                boolean isHttpOnly = cursor.getInt(httpIdx) > 0;
                
                CookieItem item = new CookieItem(name, value, host, path, isHttpOnly);
                String domain = host.startsWith(".") ? host.substring(1) : host;
                
                if (!groupedCookies.containsKey(domain)) {
                    groupedCookies.put(domain, new ArrayList<>());
                }
                groupedCookies.get(domain).add(item);
            }
            cursor.close();
            db.close();
        } catch (Exception e) {
            Log.e(TAG, "Error reading gecko cookies database", e);
        } finally {
            if (tempDb.exists()) tempDb.delete();
        }
    }

    private void copyFile(File src, File dst) throws Exception {
        try (FileInputStream inStream = new FileInputStream(src);
             FileOutputStream outStream = new FileOutputStream(dst);
             FileChannel inChannel = inStream.getChannel();
             FileChannel outChannel = outStream.getChannel()) {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        }
    }

    private void deleteCookieFromHost(CookieItem item, int position) {
        if (MainActivity.sRuntime != null) {
            MainActivity.sRuntime.getStorageController().clearDataFromHost(
                currentDomain,
                StorageController.ClearFlags.COOKIES | StorageController.ClearFlags.SITE_DATA
            );
            
            groupedCookies.remove(currentDomain);
            currentDomain = null;
            refreshDisplayList();
            Toast.makeText(this, R.string.msg_cookie_deleted, Toast.LENGTH_SHORT).show();
        }
    }

    private void showDeleteDomainDialog(String domain) {
        new MaterialAlertDialogBuilder(this)
            .setTitle(domain)
            .setMessage(getString(R.string.msg_confirm_delete_domain, domain))
            .setPositiveButton(R.string.action_delete, (dialog, which) -> {
                if (MainActivity.sRuntime != null) {
                    MainActivity.sRuntime.getStorageController().clearDataFromHost(
                        domain,
                        StorageController.ClearFlags.COOKIES | StorageController.ClearFlags.SITE_DATA
                    );
                    groupedCookies.remove(domain);
                    refreshDisplayList();
                    Toast.makeText(this, R.string.msg_cookie_deleted, Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton(android.R.string.cancel, null)
            .show();
    }

    private void showClearAllDialog() {
        new MaterialAlertDialogBuilder(this)
            .setTitle(R.string.title_cookies)
            .setMessage(R.string.msg_clear_all_cookies)
            .setPositiveButton(R.string.action_clear_all, (dialog, which) -> {
                if (MainActivity.sRuntime != null) {
                    MainActivity.sRuntime.getStorageController().clearData(
                        StorageController.ClearFlags.COOKIES | 
                        StorageController.ClearFlags.SITE_DATA |
                        StorageController.ClearFlags.ALL_CACHES
                    );
                    groupedCookies.clear();
                    currentDomain = null;
                    refreshDisplayList();
                    Toast.makeText(this, R.string.msg_cookies_cleared, Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton(android.R.string.cancel, null)
            .show();
    }

    private void updateEmptyState() {
        if (displayList.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            tvEmpty.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        menu.add(0, 1, 0, R.string.action_refresh)
            .setIcon(android.R.drawable.ic_menu_rotate)
            .setShowAsAction(android.view.MenuItem.SHOW_AS_ACTION_IF_ROOM);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (currentDomain != null) {
                currentDomain = null;
                refreshDisplayList();
            } else {
                finish();
            }
            return true;
        } else if (item.getItemId() == 1) {
            loadCookiesFromEngine();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
