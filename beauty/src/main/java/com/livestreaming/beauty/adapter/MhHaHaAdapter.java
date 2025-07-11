package com.livestreaming.beauty.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.livestreaming.beauty.R;
import com.livestreaming.beauty.bean.HaHaBean;
import com.livestreaming.beauty.constant.Constants;
import com.livestreaming.beauty.interfaces.OnItemClickListener;
import com.livestreaming.common.utils.WordUtil;

import java.util.List;

public class MhHaHaAdapter extends RecyclerView.Adapter {

    private LayoutInflater mInflater;
    private List<HaHaBean> mList;
    private View.OnClickListener mOnClickListener;
    private int mCheckedPosition;
    private Drawable mCheckedDrawable;
    private int mColor0;
    private int mColor1;
    private OnItemClickListener<HaHaBean> mOnItemClickListener;

    public MhHaHaAdapter(Context context, List<HaHaBean> list) {
        mList = list;
        mInflater = LayoutInflater.from(context);
        mCheckedDrawable = ContextCompat.getDrawable(context, R.drawable.bg_water_check);
        mColor0 = ContextCompat.getColor(context, R.color.mh_textColor2);
        mColor1 = ContextCompat.getColor(context, R.color.mh_global);
        mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = (int) v.getTag();
                if (position != mCheckedPosition) {
                    HaHaBean bean = mList.get(position);
                    bean.setChecked(true);
                    mList.get(mCheckedPosition).setChecked(false);
                    notifyItemChanged(position, Constants.PAYLOAD);
                    notifyItemChanged(mCheckedPosition, Constants.PAYLOAD);
                    mCheckedPosition = position;
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(bean, position);
                    }
                }
            }
        };
    }

    public void setOnItemClickListener(OnItemClickListener<HaHaBean> onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new Vh(mInflater.inflate(R.layout.item_haha, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {

    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder vh, int position, @NonNull List payloads) {
        Object payload = payloads.size() > 0 ? payloads.get(0) : null;
        ((Vh) vh).setData(mList.get(position), position, payload);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    class Vh extends RecyclerView.ViewHolder {

        ImageView mThumb;
        TextView mName;
        View mBg;

        public Vh(@NonNull View itemView) {
            super(itemView);
            mThumb = itemView.findViewById(R.id.thumb);
            mName = itemView.findViewById(R.id.name);
            mBg = itemView.findViewById(R.id.bg);
            itemView.setOnClickListener(mOnClickListener);
        }

        void setData(HaHaBean bean, int position, Object payload) {
            if (payload == null) {
                itemView.setTag(position);
                if (bean.getName() == 0) {
                    if (mName.getVisibility() != View.GONE) {
                        mName.setVisibility(View.GONE);
                    }
                } else {
                    if (mName.getVisibility() != View.VISIBLE) {
                        mName.setVisibility(View.VISIBLE);
                    }
                    mName.setText(WordUtil.getString(bean.getName()));
                }
                mThumb.setImageResource(bean.getThumb());
            }
            if (bean.getName() != 0) {
                mName.setTextColor(bean.isChecked() ? mColor1 : mColor0);
            }
            mBg.setBackground(bean.isChecked() ? mCheckedDrawable : null);
        }

    }
}
