package com.livestreaming.live.adapter;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.livestreaming.live.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;


public class Mp3Adapter extends RecyclerView.Adapter<Mp3Adapter.Mp3ViewHolder> {
    private final ArrayList<HashMap<String, String>> originalList;
    private final ArrayList<HashMap<String, String>> filteredList;
    private final Select select;

    public interface Select {
        void onReturnPath(String path, String id);
    }

    public Mp3Adapter(ArrayList<HashMap<String, String>> mp3List, Select select) {
        this.originalList = new ArrayList<>(mp3List);
        this.filteredList = new ArrayList<>(mp3List);
        this.select = select;
    }

    @NonNull
    @Override
    public Mp3ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mp3, parent, false);
        return new Mp3ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull Mp3ViewHolder holder, int position) {
        HashMap<String, String> item = filteredList.get(position);
        holder.title.setText(item.get("name"));
//        holder.path.setText(item.get("duration"));
        holder.itemView.setOnClickListener(v -> {
            select.onReturnPath(item.get("path"), item.get("id"));
        });
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void filter(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(originalList);
        } else {
            for (HashMap<String, String> item : originalList) {
                if (Objects.requireNonNull(item.get("name")).toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    static class Mp3ViewHolder extends RecyclerView.ViewHolder {
        TextView title, path;

        Mp3ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.mp3Title);
            path = itemView.findViewById(R.id.mp3Path);
        }
    }
}
