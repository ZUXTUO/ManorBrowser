package com.olsc.manorbrowser.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.olsc.manorbrowser.R;
import com.olsc.manorbrowser.data.CookieDisplayItem;
import com.olsc.manorbrowser.data.CookieItem;
import java.util.List;

public class CookieAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface OnCookieActionListener {
        void onDomainClick(String domain);
        void onDomainLongClick(String domain);
        void onDeleteCookie(CookieItem item, int position);
    }

    private static final int VIEW_TYPE_GROUP = 0;
    private static final int VIEW_TYPE_COOKIE = 1;

    private final List<CookieDisplayItem> list;
    private final OnCookieActionListener listener;

    public CookieAdapter(List<CookieDisplayItem> list, OnCookieActionListener listener) {
        this.list = list;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return list.get(position).type == CookieDisplayItem.Type.GROUP ? VIEW_TYPE_GROUP : VIEW_TYPE_COOKIE;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_GROUP) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cookie_group, parent, false);
            return new GroupViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cookie, parent, false);
            return new CookieViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        CookieDisplayItem displayItem = list.get(position);
        if (holder instanceof GroupViewHolder) {
            GroupViewHolder groupHolder = (GroupViewHolder) holder;
            groupHolder.tvDomain.setText(displayItem.domain);
            groupHolder.tvCount.setText(displayItem.count + " cookies");
            groupHolder.itemView.setOnClickListener(v -> listener.onDomainClick(displayItem.domain));
            groupHolder.itemView.setOnLongClickListener(v -> {
                listener.onDomainLongClick(displayItem.domain);
                return true;
            });
        } else if (holder instanceof CookieViewHolder) {
            CookieViewHolder cookieHolder = (CookieViewHolder) holder;
            CookieItem item = displayItem.cookie;
            String name = item.name;
            if (item.isHttpOnly) {
                name += " (HttpOnly)";
            }
            cookieHolder.tvName.setText(name);
            cookieHolder.tvDomain.setText(item.domain);
            cookieHolder.tvValue.setText(item.value);
            cookieHolder.btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteCookie(item, holder.getBindingAdapterPosition());
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class GroupViewHolder extends RecyclerView.ViewHolder {
        TextView tvDomain, tvCount;

        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDomain = itemView.findViewById(R.id.tv_domain_name);
            tvCount = itemView.findViewById(R.id.tv_cookie_count);
        }
    }

    static class CookieViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDomain, tvValue;
        ImageButton btnDelete;

        public CookieViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_cookie_name);
            tvDomain = itemView.findViewById(R.id.tv_cookie_domain);
            tvValue = itemView.findViewById(R.id.tv_cookie_value);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}
