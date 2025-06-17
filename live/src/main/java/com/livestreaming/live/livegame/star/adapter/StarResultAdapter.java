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
import com.livestreaming.live.livegame.star.bean.StarResultBean;

import java.util.List;

/**
 * Created by http://www.yunbaokj.com on 2023/3/4.
 */
public class StarResultAdapter extends RefreshAdapter<StarResultBean> {
    public StarResultAdapter(Context context, List<StarResultBean> list) {
        super(context, list);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new Vh(mInflater.inflate(R.layout.game_star_result, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder vh, int i) {
        ((Vh) vh).setData(mList.get(i));
    }

    private class Vh extends RecyclerView.ViewHolder {

        ImageView mGiftIcon;
        TextView mCount;
        TextView mGiftName;
        TextView mTotal;

        public Vh(@NonNull View itemView) {
            super(itemView);
            mGiftIcon = itemView.findViewById(R.id.gift_icon);
            mCount = itemView.findViewById(R.id.count);
            mGiftName = itemView.findViewById(R.id.gift_name);
            mTotal = itemView.findViewById(R.id.total);
        }

        void setData(StarResultBean bean) {
            ImgLoader.display(mContext, bean.getGiftIcon(), mGiftIcon);
            mCount.setText(bean.getCount());
            mGiftName.setText(bean.getGiftNameTrans());
            mTotal.setText(bean.getTotalCoin());
        }
    }
}
