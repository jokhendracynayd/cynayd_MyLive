package com.livestreaming.main.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.livestreaming.common.activity.AbsActivity;
import com.livestreaming.common.adapter.RefreshAdapter;
import com.livestreaming.common.glide.ImgLoader;
import com.livestreaming.live.bean.ItemGuest;
import com.livestreaming.main.R;

/**
 * Created by 云豹科技 on 2022/1/21.
 */
public class LiveHomeGuestsListAdapter extends RefreshAdapter<ItemGuest> {

    private View.OnClickListener mOnClickListener;

    public LiveHomeGuestsListAdapter(Context context) {
        super(context);
        mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!((AbsActivity) mContext).checkLogin()) {
                    return;
                }
                Object tag = v.getTag();
                if (mOnItemClickListener != null && tag != null) {
                    mOnItemClickListener.onItemClick((ItemGuest) tag, 0);
                }
            }
        };
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new Vh(mInflater.inflate(R.layout.home_guest_item, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder vh, int i) {
        ((Vh) vh).setData(mList.get(i));
    }


    private class Vh extends RecyclerView.ViewHolder {

        ImageView mThumb;

        public Vh(@NonNull View itemView) {
            super(itemView);
            mThumb = itemView.findViewById(R.id.thumb);
            itemView.setOnClickListener(mOnClickListener);
        }


        void setData(ItemGuest liveBean) {
            itemView.setTag(liveBean);
            ImgLoader.display(mContext, liveBean.getAvatar(), mThumb);
        }
    }
}
