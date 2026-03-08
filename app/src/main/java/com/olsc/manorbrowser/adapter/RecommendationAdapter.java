package com.olsc.manorbrowser.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.olsc.manorbrowser.R;
import com.olsc.manorbrowser.data.HistoryStorage;

import java.util.ArrayList;
import java.util.List;

public class RecommendationAdapter extends RecyclerView.Adapter<RecommendationAdapter.ViewHolder> {

    private List<HistoryStorage.HistoryItem> items = new ArrayList<>();
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(HistoryStorage.HistoryItem item);
    }

    public RecommendationAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<HistoryStorage.HistoryItem> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recommendation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HistoryStorage.HistoryItem item = items.get(position);
        holder.tvTitle.setText(item.title != null && !item.title.isEmpty() ? item.title : item.url);
        holder.tvUrl.setText(item.url);
        
        // Show weight if it's significant (e.g., visit count > 1)
        if (item.visitCount > 1) {
            holder.tvWeight.setVisibility(View.VISIBLE);
            holder.tvWeight.setText(String.valueOf(item.visitCount));
        } else {
            holder.tvWeight.setVisibility(ViewGroup.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvUrl;
        TextView tvWeight;

        ViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvUrl = itemView.findViewById(R.id.tv_url);
            tvWeight = itemView.findViewById(R.id.tv_weight);
        }
    }
}
