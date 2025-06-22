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
    private final ArrayList<HashMap<String, String>> mp3List;

    Select select;

    public interface Select{
        void onReturnPath(String path,String id);
    }

    public Mp3Adapter(ArrayList<HashMap<String, String>> mp3List,Select select) {
        this.mp3List = mp3List;
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
        HashMap<String, String> item = mp3List.get(position);
        holder.title.setText(item.get("name"));
        holder.path.setText(item.get("path"));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                select.onReturnPath(item.get("path"), item.get("id"));
            }
        });

    }

    @Override
    public int getItemCount() {
        return mp3List.size();
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
