package com.livestreaming.main.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.livestreaming.common.adapter.RefreshAdapter;
import com.livestreaming.common.glide.ImgLoader;
import com.livestreaming.main.R;
import com.livestreaming.main.activity.ActiveTopicActivity;
import com.livestreaming.main.bean.ActiveTopicBean;

import java.util.List;

public class ActiveHotTopicAdapter extends RefreshAdapter<ActiveTopicBean> {

    private View.OnClickListener mOnClickListener;

    public ActiveHotTopicAdapter(Context context, List<ActiveTopicBean> list) {
        super(context, list);
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
        return new Vh(mInflater.inflate(R.layout.item_active_hot_topic, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder vh, int i) {
        ((Vh) vh).setData(mList.get(i));
    }


    class Vh extends RecyclerView.ViewHolder {

        ImageView mThumb;
        TextView mName;

        public Vh(@NonNull View itemView) {
            super(itemView);
            mThumb = itemView.findViewById(R.id.thumb);
            mName = itemView.findViewById(R.id.name);
            itemView.setOnClickListener(mOnClickListener);
        }

        void setData(ActiveTopicBean bean) {
            itemView.setTag(bean);
            ImgLoader.display(mContext, bean.getThumb(), mThumb);
            mName.setText(bean.getName());
        }
    }
}
