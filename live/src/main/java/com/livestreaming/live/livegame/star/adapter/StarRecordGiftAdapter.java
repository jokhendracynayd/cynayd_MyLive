package com.livestreaming.live.livegame.star.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.livestreaming.common.adapter.RefreshAdapter;
import com.livestreaming.common.glide.ImgLoader;
import com.livestreaming.live.R;
import com.livestreaming.live.livegame.star.bean.StarRecordGiftBean;

/**
 * Created by http://www.yunbaokj.com on 2023/3/6.
 */
public class StarRecordGiftAdapter extends RefreshAdapter<StarRecordGiftBean> {


    public StarRecordGiftAdapter(Context context) {
        super(context);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new Vh(mInflater.inflate(R.layout.game_star_record_item_gift, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder vh, int i) {
        ((Vh) vh).setData(mList.get(i));
    }

    private class Vh extends RecyclerView.ViewHolder {

        ImageView mGiftIcon;
        TextView mCount;

        public Vh(@NonNull View itemView) {
            super(itemView);
            mGiftIcon = itemView.findViewById(R.id.gift_icon);
            mCount = itemView.findViewById(R.id.count);
        }

        void setData(StarRecordGiftBean bean) {
            ImgLoader.display(mContext, bean.getGiftIcon(), mGiftIcon);
            mCount.setText(bean.getCount());
        }
    }
}
