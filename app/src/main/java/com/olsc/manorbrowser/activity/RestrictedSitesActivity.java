package com.olsc.manorbrowser.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.preference.PreferenceManager;
import com.olsc.manorbrowser.R;
import com.olsc.manorbrowser.Config;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.WindowCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.core.graphics.Insets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RestrictedSitesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private View tvEmpty;
    private List<String> restrictedHosts = new ArrayList<>();
    private RestrictedSitesAdapter adapter;

    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        super.attachBaseContext(com.olsc.manorbrowser.utils.LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        android.content.SharedPreferences themePrefs = getSharedPreferences(Config.PREF_NAME_THEME, MODE_PRIVATE);
        boolean isDarkMode = themePrefs.getBoolean(Config.PREF_KEY_DARK_MODE, false);
        AppCompatDelegate.setDefaultNightMode(isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (controller != null) {
            controller.setAppearanceLightStatusBars(!isDarkMode);
            controller.setAppearanceLightNavigationBars(!isDarkMode);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restricted_sites);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
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

        recyclerView = findViewById(R.id.recycler_view);
        tvEmpty = findViewById(R.id.tv_empty);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadData();
    }

    private void loadData() {
        android.content.SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Set<String> restricted = prefs.getStringSet(Config.PREF_KEY_LOCATION_RESTRICTED_SITES, new HashSet<>());
        restrictedHosts = new ArrayList<>(restricted);
        
        if (restrictedHosts.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }

        if (adapter == null) {
            adapter = new RestrictedSitesAdapter();
            recyclerView.setAdapter(adapter);
        } else {
            adapter.notifyDataSetChanged();
        }
    }

    private void removeRestriction(String host) {
        android.content.SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Set<String> restricted = new HashSet<>(prefs.getStringSet(Config.PREF_KEY_LOCATION_RESTRICTED_SITES, new HashSet<>()));
        if (restricted.remove(host)) {
            prefs.edit().putStringSet(Config.PREF_KEY_LOCATION_RESTRICTED_SITES, restricted).apply();
            loadData();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private class RestrictedSitesAdapter extends RecyclerView.Adapter<RestrictedSitesAdapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_restricted_site, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String host = restrictedHosts.get(position);
            holder.tvHost.setText(host);
            holder.btnRemove.setOnClickListener(v -> removeRestriction(host));
        }

        @Override
        public int getItemCount() {
            return restrictedHosts.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvHost;
            TextView btnRemove;

            ViewHolder(View itemView) {
                super(itemView);
                tvHost = itemView.findViewById(R.id.tv_host);
                btnRemove = itemView.findViewById(R.id.btn_remove);
            }
        }
    }
}
