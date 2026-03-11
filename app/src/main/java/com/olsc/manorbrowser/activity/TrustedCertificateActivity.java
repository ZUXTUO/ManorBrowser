package com.olsc.manorbrowser.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.olsc.manorbrowser.Config;
import com.olsc.manorbrowser.R;
import com.olsc.manorbrowser.data.TrustedCertificateStorage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TrustedCertificateActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CertificateAdapter adapter;
    private List<TrustedCertificateStorage.TrustedCertificate> certificateList = new ArrayList<>();
    private TextView tvEmpty;

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
        setContentView(R.layout.activity_trusted_certificates);

        Toolbar toolbar = findViewById(R.id.toolbar_certificates);
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

        recyclerView = findViewById(R.id.rv_certificates);
        tvEmpty = findViewById(R.id.tv_empty);

        adapter = new CertificateAdapter(certificateList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loadData();
    }

    private void loadData() {
        new Thread(() -> {
            List<TrustedCertificateStorage.TrustedCertificate> list = TrustedCertificateStorage.loadAll(this);
            runOnUiThread(() -> {
                certificateList.clear();
                certificateList.addAll(list);
                adapter.notifyDataSetChanged();
                tvEmpty.setVisibility(certificateList.isEmpty() ? View.VISIBLE : View.GONE);
            });
        }).start();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class CertificateAdapter extends RecyclerView.Adapter<CertificateAdapter.ViewHolder> {
        private final List<TrustedCertificateStorage.TrustedCertificate> items;
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

        CertificateAdapter(List<TrustedCertificateStorage.TrustedCertificate> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_trusted_certificate, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            TrustedCertificateStorage.TrustedCertificate cert = items.get(position);
            holder.tvHost.setText(cert.host);
            holder.tvFingerprint.setText("SHA-256: " + cert.fingerprint);
            holder.tvDate.setText(dateFormat.format(new Date(cert.addedTime)));

            holder.btnDelete.setOnClickListener(v -> showDeleteConfirm(cert, position));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        private void showDeleteConfirm(TrustedCertificateStorage.TrustedCertificate cert, int position) {
            new MaterialAlertDialogBuilder(TrustedCertificateActivity.this)
                    .setTitle(R.string.title_trusted_certificates)
                    .setMessage(getString(R.string.msg_confirm_delete_certificate, cert.host))
                    .setPositiveButton(R.string.action_delete, (dialog, which) -> {
                        TrustedCertificateStorage.delete(TrustedCertificateActivity.this, cert.id);
                        items.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, items.size());
                        if (items.isEmpty()) {
                            tvEmpty.setVisibility(View.VISIBLE);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvHost, tvFingerprint, tvDate;
            ImageButton btnDelete;

            ViewHolder(View itemView) {
                super(itemView);
                tvHost = itemView.findViewById(R.id.tv_cert_host);
                tvFingerprint = itemView.findViewById(R.id.tv_cert_fingerprint);
                tvDate = itemView.findViewById(R.id.tv_cert_date);
                btnDelete = itemView.findViewById(R.id.btn_delete_cert);
            }
        }
    }
}
