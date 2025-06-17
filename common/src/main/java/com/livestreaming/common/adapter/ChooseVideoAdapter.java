package com.livestreaming.common.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.livestreaming.common.R;
import com.livestreaming.common.bean.ChooseVideoBean;
import com.livestreaming.common.glide.ImgLoader;

import java.util.List;

public class ChooseVideoAdapter extends RecyclerView.Adapter {

    private Context mContext;
    private LayoutInflater mInflater;
    private List<ChooseVideoBean> mList;
    private View.OnClickListener mOnClickListener;
    private ActionListener mActionListener;

    public ChooseVideoAdapter(Context context, List<ChooseVideoBean> list) {
        mInflater = LayoutInflater.from(context);
        mContext = context;
        mList = list;
        mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Object tag = v.getTag();
                if (tag == null) {
                    return;
                }
                int position = (int) tag;
                ChooseVideoBean bean = mList.get(position);
                if (bean.getType() == ChooseVideoBean.CAMERA) {
                    if (mActionListener != null) {
                        mActionListener.onCameraClick();
                    }
                    return;
                }else{
                    if (mActionListener != null) {
                        mActionListener.onVideoItemClick(bean);
                    }
                }
            }
        };
    }

    public void setActionListener(ActionListener actionListener) {
        mActionListener = actionListener;
    }

    @Override
    public int getItemViewType(int position) {
        return mList.get(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ChooseVideoBean.CAMERA) {
            return new Vh(mInflater.inflate(R.layout.item_choose_video_camera, parent, false));
        }
        return new FileVh(mInflater.inflate(R.layout.item_choose_video_file, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder vh, int position) {
        ((Vh) vh).setData(mList.get(position), position);
    }


    @Override
    public int getItemCount() {
        return mList.size();
    }


    class Vh extends RecyclerView.ViewHolder {

        public Vh(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(mOnClickListener);
        }

        void setData(ChooseVideoBean bean, int position) {
            itemView.setTag(position);
        }
    }


    class FileVh extends Vh {

        ImageView mImg;
        TextView mDuration;

        public FileVh(@NonNull View itemView) {
            super(itemView);
            mImg = itemView.findViewById(R.id.img);
            mDuration = itemView.findViewById(R.id.duration);
        }

        void setData(ChooseVideoBean bean, int position) {
            super.setData(bean, position);
            ImgLoader.displayVideoThumb(mContext, bean.getVideoUri(), mImg);
            mDuration.setText(bean.getDurationString());
        }
    }


    public interface ActionListener {
        void onCameraClick();

        void onVideoItemClick(ChooseVideoBean bean);
    }

}
