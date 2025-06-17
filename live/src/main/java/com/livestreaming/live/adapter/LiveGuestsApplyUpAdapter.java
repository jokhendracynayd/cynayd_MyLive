package com.livestreaming.live.adapter;

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
import com.livestreaming.live.bean.LiveGestBean;

import java.util.List;

public class LiveGuestsApplyUpAdapter extends RefreshAdapter<LiveGestBean> {

    private View.OnClickListener mAgreeClickListener;
    private View.OnClickListener mRefuseClickListener;
    private ActionListener mActionListener;

    public LiveGuestsApplyUpAdapter(Context context, List<LiveGestBean> list) {
        super(context, list);
        mAgreeClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mActionListener != null) {
                    mActionListener.onAgreeUpMicClick((LiveGestBean) v.getTag(), 1);
                }
            }
        };
        mRefuseClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mActionListener != null) {
                    mActionListener.onAgreeUpMicClick((LiveGestBean) v.getTag(), 0);
                }
            }
        };
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new VhAnchor(mInflater.inflate(R.layout.item_live_guest_apply_up_anchor, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder vh, int i) {

        ((VhAnchor) vh).setData(mList.get(i), i);

    }


    class VhAnchor extends RecyclerView.ViewHolder {

        TextView mIndex;
        ImageView mAvatar;
        TextView mName;
        View mBtnAgree;
        View mBtnRefuse;


        public VhAnchor(@NonNull View itemView) {
            super(itemView);
            mIndex = itemView.findViewById(R.id.index);
            mAvatar = (ImageView) itemView.findViewById(R.id.avatar);
            mName = (TextView) itemView.findViewById(R.id.name);
            mBtnAgree = itemView.findViewById(R.id.btn_agree);
            mBtnRefuse = itemView.findViewById(R.id.btn_refuse);
            mBtnAgree.setOnClickListener(mAgreeClickListener);
            mBtnRefuse.setOnClickListener(mRefuseClickListener);
            mAvatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mActionListener.onShowUserDialog(""+((LiveGestBean) v.getTag()).getUser_id());
                }
            });
        }

        void setData(LiveGestBean bean, int position) {
            mBtnAgree.setTag(bean);
            mBtnRefuse.setTag(bean);
            mIndex.setText(String.valueOf(position + 1));
            ImgLoader.displayAvatar(mContext, bean.getAvatar(), mAvatar);
            mName.setText(bean.getUserName());
        }
    }

    public void setActionListener(ActionListener actionListener) {
        mActionListener = actionListener;
    }

    public interface ActionListener {
        void onAgreeUpMicClick(LiveGestBean userBean, int isAgree);
        void onShowUserDialog(String uid);
    }

}
