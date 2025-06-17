package com.livestreaming.main.adapter;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.livestreaming.common.adapter.RefreshAdapter;
import com.livestreaming.main.R;
import com.livestreaming.main.activity.ActiveTopicActivity;
import com.livestreaming.main.bean.ActiveTopicBean;
import com.bumptech.glide.Glide;

public class ActiveAllTopicAdapter extends RefreshAdapter<ActiveTopicBean> {

    private View.OnClickListener mOnClickListener;

    public ActiveAllTopicAdapter(Context context) {
        super(context);
        mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActiveTopicBean bean = (ActiveTopicBean) v.getTag();
                if (bean != null) {
                    ActiveTopicActivity.forward(mContext, bean.getId(), bean.getName());
                }
            }
        };
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new Vh(mInflater.inflate(R.layout.item_active_all_topic, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder vh, int i) {
        ((Vh) vh).setData(mList.get(i));
    }


    class Vh extends RecyclerView.ViewHolder {

        ImageView mThumb;
        TextView mName;
        TextView mNum;

        public Vh(@NonNull View itemView) {
            super(itemView);
            mThumb = itemView.findViewById(R.id.thumb);
            mName = itemView.findViewById(R.id.name);
            mNum = itemView.findViewById(R.id.num);
            itemView.setOnClickListener(mOnClickListener);
        }

        void setData(ActiveTopicBean bean) {
            if (bean != null) {
                itemView.setTag(bean);
                String thumb = bean.getThumb();
                Glide.with(itemView.getContext()).load(thumb).into(mThumb);
                mName.setText(bean.getName());
                mNum.setText(bean.getNumString());
            }
        }
    }
}
