package com.livestreaming.beauty.adapter;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.livestreaming.beauty.R;
import com.livestreaming.beauty.bean.MeiYanOneKeyBean;
import com.livestreaming.beauty.constant.Constants;
import com.livestreaming.beauty.interfaces.OnItemClickListener;
import com.livestreaming.common.utils.WordUtil;

import java.util.List;

public class MhMeiYanOneKeyAdapter extends RecyclerView.Adapter {

    private LayoutInflater mInflater;
    private List<MeiYanOneKeyBean> mList;
    private View.OnClickListener mOnClickListener;
    private int mCheckedPosition;
    private int mColor0;
    private int mColor1;
    private OnItemClickListener<MeiYanOneKeyBean> mOnItemClickListener;

    public MhMeiYanOneKeyAdapter(Context context, List<MeiYanOneKeyBean> list) {
        mList = list;
        mInflater = LayoutInflater.from(context);
        mColor0 = ContextCompat.getColor(context, R.color.mh_textColor4);
        mColor1 = ContextCompat.getColor(context, R.color.mh_textColor5);
        mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = (int) v.getTag();
                if (position != mCheckedPosition) {
                    MeiYanOneKeyBean bean = mList.get(position);
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

    public MeiYanOneKeyBean getCheckedBean() {
        if (mList.size() == 0){
            return null;
        }
        return mList.get(mCheckedPosition);
    }

    public void setOnItemClickListener(OnItemClickListener<MeiYanOneKeyBean> onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new Vh(mInflater.inflate(R.layout.item_meiyan_2, viewGroup, false));
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
        View mCheck;
        FrameLayout itemGroup;

        public Vh(@NonNull View itemView) {
            super(itemView);
            mThumb = itemView.findViewById(R.id.thumb);
            mName = itemView.findViewById(R.id.name);
            mCheck = itemView.findViewById(R.id.check);
            itemGroup = itemView.findViewById(R.id.item_group);
            itemView.setOnClickListener(mOnClickListener);
        }

        void setData(MeiYanOneKeyBean bean, int position, Object payload) {
            if (payload == null) {
                itemView.setTag(position);
                mName.setText(WordUtil.getString(bean.getName()));
                mThumb.setImageResource(bean.getThumb());
            }
            if (bean.isChecked()) {
                mName.setTextColor(mColor1);
                if (itemGroup.getVisibility() != View.VISIBLE) {
                    itemGroup.setVisibility(View.VISIBLE);
                }
            } else {
                mName.setTextColor(mColor0);
                if (itemGroup.getVisibility() == View.VISIBLE) {
                    itemGroup.setVisibility(View.INVISIBLE);
                }
            }
        }

    }
}
