package com.livestreaming.live.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.livestreaming.live.R;

/**
 * Created by cxf on 2018/10/7.
 */

public class LiveReadyShareAdapter extends RecyclerView.Adapter<LiveReadyShareAdapter.Vh> {


    private LayoutInflater mInflater;
    private View.OnClickListener mOnClickListener;
    private int mCheckedPosition = -1;

    public LiveReadyShareAdapter(Context context) {

        mInflater = LayoutInflater.from(context);
        mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Object tag = v.getTag();
            }
        };
    }

    public String getShareType() {

        return "";
    }


    @NonNull
    @Override
    public Vh onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Vh(mInflater.inflate(R.layout.item_live_ready_share, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Vh vh, int position) {
    //    vh.setData(mList.get(position), position);
    }

    @Override
    public int getItemCount() {
        return 0;
    }

    class Vh extends RecyclerView.ViewHolder {
        ImageView mImg;

        public Vh(View itemView) {
            super(itemView);
            mImg = (ImageView) itemView.findViewById(R.id.img);
            itemView.setOnClickListener(mOnClickListener);
        }


    }


}
