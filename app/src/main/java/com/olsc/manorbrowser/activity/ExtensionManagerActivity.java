package com.olsc.manorbrowser.activity;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.olsc.manorbrowser.R;
import com.olsc.manorbrowser.utils.LocaleHelper;

import org.mozilla.geckoview.GeckoResult;
import org.mozilla.geckoview.WebExtension;

import java.util.ArrayList;
import java.util.List;

public class ExtensionManagerActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private ExtensionAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_extension_manager);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        
        recyclerView = findViewById(R.id.recycler_view);
        tvEmpty = findViewById(R.id.tv_empty);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ExtensionAdapter();
        recyclerView.setAdapter(adapter);

        loadExtensions();
    }

    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    private void loadExtensions() {
        if (MainActivity.sRuntime != null) {
            MainActivity.sRuntime.getWebExtensionController().list().accept(
                extensions -> {
                    runOnUiThread(() -> {
                        if (extensions == null || extensions.isEmpty()) {
                            tvEmpty.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                        } else {
                            tvEmpty.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                            adapter.setExtensions(extensions);
                        }
                    });
                },
                e -> {
                    runOnUiThread(() -> Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show());
                }
            );
        }
    }

    private void uninstallExtension(WebExtension extension) {
        new AlertDialog.Builder(this)
            .setTitle(R.string.title_extensions)
            .setMessage(getString(R.string.msg_confirm_uninstall_extension, extension.metaData.name))
            .setPositiveButton(R.string.action_select, (dialog, which) -> {
                if (MainActivity.sRuntime != null) {
                    MainActivity.sRuntime.getWebExtensionController().uninstall(extension).accept(
                        v -> runOnUiThread(this::loadExtensions),
                        e -> runOnUiThread(() -> Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show())
                    );
                }
            })
            .setNegativeButton(android.R.string.cancel, null)
            .show();
    }

    private class ExtensionAdapter extends RecyclerView.Adapter<ExtensionAdapter.ViewHolder> {
        private List<WebExtension> extensions = new ArrayList<>();

        public void setExtensions(List<WebExtension> exts) {
            this.extensions.clear();
            if (exts != null) {
                this.extensions.addAll(exts);
            }
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_extension, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            WebExtension ext = extensions.get(position);
            holder.tvName.setText(ext.metaData.name != null ? ext.metaData.name : "Unknown");
            
            String version = ext.metaData.version != null ? ext.metaData.version : "";
            holder.tvVersion.setText(getString(R.string.label_version_value, version));
            
            holder.btnDelete.setOnClickListener(v -> uninstallExtension(ext));
        }

        @Override
        public int getItemCount() {
            return extensions.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvVersion;
            ImageButton btnDelete;

            ViewHolder(View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tv_name);
                tvVersion = itemView.findViewById(R.id.tv_version);
                btnDelete = itemView.findViewById(R.id.btn_delete);
            }
        }
    }
}
