package com.livestreaming.live.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.livestreaming.common.CommonAppConfig;
import com.livestreaming.common.adapter.RefreshAdapter;
import com.livestreaming.common.bean.LevelBean;
import com.livestreaming.common.glide.ImgLoader;
import com.livestreaming.common.utils.CommonIconUtil;
import com.livestreaming.common.utils.WordUtil;
import com.livestreaming.live.R;
import com.livestreaming.live.bean.GuardUserBean;

/**
 * Created by cxf on 2018/11/6.
 */

public class GuardAdapter extends RefreshAdapter<GuardUserBean> {

    private static final int HEAD = 1;
    private static final int NORMAL = 0;
    private String mVotesName;
    private boolean mDialog;
    private Drawable mGuardDrawable0;
    private Drawable mGuardDrawable1;

    public GuardAdapter(Context context, boolean dialog) {
        super(context);
        mDialog = dialog;
        mVotesName = CommonAppConfig.getInstance().getVotesName();
        mGuardDrawable0 = ContextCompat.getDrawable(context, R.drawable.icon_live_chat_guard_1);
        mGuardDrawable1 = ContextCompat.getDrawable(context, R.drawable.icon_live_chat_guard_2);
    }


    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return HEAD;
        }
        return NORMAL;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == HEAD) {
            return new HeadVh(mInflater.inflate(mDialog ? R.layout.guard_list_head : R.layout.guard_list_head_2, parent, false));
        }
        return new Vh(mInflater.inflate(mDialog ? R.layout.guard_list : R.layout.guard_list_2, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder vh, int position) {
        if (vh instanceof HeadVh) {
            ((HeadVh) vh).setData(mList.get(position));
        } else {
            ((Vh) vh).setData(mList.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }


    class HeadVh extends RecyclerView.ViewHolder {

        ImageView mAvatar;
        TextView mName;
        ImageView mSex;
        ImageView mLevel;
        TextView mVotes;


        public HeadVh(@NonNull View itemView) {
            super(itemView);
            mAvatar = (ImageView) itemView.findViewById(R.id.avatar);
            mName = (TextView) itemView.findViewById(R.id.name);
            mSex = (ImageView) itemView.findViewById(R.id.sex);
            mLevel = (ImageView) itemView.findViewById(R.id.level);
            mVotes = (TextView) itemView.findViewById(R.id.votes);
        }

        void setData(GuardUserBean bean) {
            ImgLoader.displayAvatar(mContext, bean.getAvatar(), mAvatar);
            mName.setText(bean.getUserNiceName());
            mSex.setImageResource(CommonIconUtil.getSexIcon(bean.getSex()));
            LevelBean levelBean = CommonAppConfig.getInstance().getLevel(bean.getLevel());
            if (levelBean != null) {
                ImgLoader.display(mContext, levelBean.getThumb(), mLevel);
            }
            String contribute = bean.getContribute();
            String content = String.format(WordUtil.getString(com.livestreaming.common.R.string.guard_week_con), contribute, mVotesName);
            SpannableString spannableString = new SpannableString(content);
            int startIndex = content.indexOf(contribute);
            if (startIndex >= 0) {
                int endIndex = startIndex + contribute.length();
                spannableString.setSpan(new ForegroundColorSpan(0xffff5878), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            mVotes.setText(spannableString);
        }
    }

    class Vh extends RecyclerView.ViewHolder {

        ImageView mIcon;
        ImageView mAvatar;
        TextView mName;
        ImageView mSex;
        ImageView mLevel;
        TextView mVotes;


        public Vh(@NonNull View itemView) {
            super(itemView);
            mIcon = (ImageView) itemView.findViewById(R.id.icon);
            mAvatar = (ImageView) itemView.findViewById(R.id.avatar);
            mName = (TextView) itemView.findViewById(R.id.name);
            mSex = (ImageView) itemView.findViewById(R.id.sex);
            mLevel = (ImageView) itemView.findViewById(R.id.level);
            mVotes = (TextView) itemView.findViewById(R.id.votes);
        }

        void setData(GuardUserBean bean) {
            ImgLoader.displayAvatar(mContext, bean.getAvatar(), mAvatar);
            mName.setText(bean.getUserNiceName());
            mSex.setImageResource(CommonIconUtil.getSexIcon(bean.getSex()));
            LevelBean levelBean = CommonAppConfig.getInstance().getLevel(bean.getLevel());
            if (levelBean != null) {
                ImgLoader.display(mContext, levelBean.getThumb(), mLevel);
            }
            mVotes.setText(bean.getContribute() + " " + mVotesName);
            if (bean.getType() == 1) {
                mIcon.setImageDrawable(mGuardDrawable0);
            } else {
                mIcon.setImageDrawable(mGuardDrawable1);
            }
        }
    }
}
