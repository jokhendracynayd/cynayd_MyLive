package com.livestreaming.live.adapter;

import static com.livestreaming.common.glide.ImgLoader.loadSvga;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.livestreaming.live.R;
import com.livestreaming.live.bean.ChangeRoomBackBean;
import com.livestreaming.live.dialog.LiveChooseBackgroundDialogFragment;

import java.util.ArrayList;

public class BackChangeAdapter extends RecyclerView.Adapter<BackChangeAdapter.Holder> {
    LiveChooseBackgroundDialogFragment.OnBackChangeDoneListener listener;
    private ArrayList<ChangeRoomBackBean> dataList;

    public BackChangeAdapter(ArrayList<ChangeRoomBackBean> data, LiveChooseBackgroundDialogFragment.OnBackChangeDoneListener listener) {
        dataList = data;
        this.listener = listener;
    }

    public void setList(ArrayList<ChangeRoomBackBean> list) {
        dataList = list;
        notifyDataSetChanged();
    }

    class Holder extends RecyclerView.ViewHolder {
        ImageView imageView;
        CardView cont;

        public Holder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.img_back);
            cont = itemView.findViewById(R.id.container);
        }
    }

    @NonNull
    @Override
    public BackChangeAdapter.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_back_change, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull BackChangeAdapter.Holder holder, int position) {
        loadSvga(holder.imageView.getContext(), dataList.get(position).getSwf(), holder.imageView);
        holder.cont.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onClick(dataList.get(holder.getAbsoluteAdapterPosition()));
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

}
