package com.livestreaming.common.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.livestreaming.common.R;
import com.livestreaming.common.bean.SelectionModel;

import java.util.ArrayList;

public class ExchangeAdapter extends RecyclerView.Adapter<ExchangeAdapter.Holder> {
    ArrayList<SelectionModel> items;
    OnItemClickListener<SelectionModel> onItemClickListener;
    public ExchangeAdapter(ArrayList<SelectionModel> items, OnItemClickListener<SelectionModel> onItemClickListener){
        this.items=items;
        this.onItemClickListener=onItemClickListener;
    }
    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.exchange_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        holder.itemName_tv.setText(String.valueOf(items.get(position).value));
        holder.itemafter_tv.setText(String.valueOf(items.get(position).afterValue));
        holder.container_cv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.notifyItemClickCallback(holder.getAdapterPosition(),items.get(holder.getAdapterPosition()));
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class Holder extends RecyclerView.ViewHolder{
        CardView container_cv;
        TextView itemName_tv;
        TextView itemafter_tv;
        public Holder(@NonNull View itemView) {
            super(itemView);
            container_cv=itemView.findViewById(R.id.container_cv);
            itemName_tv=itemView.findViewById(R.id.itemName_tv);
            itemafter_tv=itemView.findViewById(R.id.itemafter_tv);
        }
    }
}
