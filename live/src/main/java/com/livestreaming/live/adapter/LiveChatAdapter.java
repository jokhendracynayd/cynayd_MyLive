package com.livestreaming.live.adapter;

import static com.livestreaming.common.glide.ImgLoader.loadSvga;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.livestreaming.common.CommonAppConfig;
import com.livestreaming.common.CommonAppContext;
import com.livestreaming.common.Constants;
import com.livestreaming.common.bean.LevelBean;
import com.livestreaming.common.custom.VerticalImageSpan;
import com.livestreaming.common.glide.ImgLoader;
import com.livestreaming.common.interfaces.OnItemClickListener;
import com.livestreaming.common.utils.DpUtil;
import com.livestreaming.live.R;
import com.livestreaming.live.bean.LiveChatBean;
import com.livestreaming.live.utils.LiveIconUtil;
import com.bumptech.glide.Glide;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cxf on 2018/10/10.
 */

public class LiveChatAdapter extends RecyclerView.Adapter {

    private Context mContext;
    private List<LiveChatBean> mList;
    private LayoutInflater mInflater;
    private View.OnClickListener mOnClickListener;
    private OnItemClickListener<LiveChatBean> mOnItemClickListener;
    private RecyclerView mRecyclerView;
    //    private LinearLayoutManager mLayoutManager;
    private Runnable mRunnable;

    public LiveChatAdapter(Context context) {
        mContext = context;
        mList = new ArrayList<>();
        mInflater = LayoutInflater.from(context);
        mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Object tag = v.getTag();
                if (tag != null) {
                    LiveChatBean bean = (LiveChatBean) tag;
                    if (bean.getType() != LiveChatBean.SYSTEM && mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(bean, 0);
                    }
                }
            }
        };
        mRunnable = new Runnable() {
            @Override
            public void run() {
                if (!mList.isEmpty() && mRecyclerView != null) {
                    mRecyclerView.scrollToPosition(mList.size() - 1);
                }
            }
        };

    }

    public void setOnItemClickListener(OnItemClickListener<LiveChatBean> onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    @Override
    public int getItemViewType(int position) {
        return mList.get(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == LiveChatBean.SYSTEM) {
            return new SystemVh(mInflater.inflate(R.layout.item_live_chat_system, parent, false));
        } else if (viewType == LiveChatBean.RED_PACK) {
            return new RedPackVh(mInflater.inflate(R.layout.item_live_chat_red_pack, parent, false));
        } else {
            return new Vh(mInflater.inflate(R.layout.item_live_chat, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder vh, int position) {
        if (vh instanceof SystemVh) {
            ((SystemVh) vh).setData(mList.get(position));
        } else if (vh instanceof Vh) {
            ((Vh) vh).setData(mList.get(position));
        } else if (vh instanceof RedPackVh) {
            ((RedPackVh) vh).setData(mList.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        mRecyclerView = recyclerView;
//        mLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
//        mLayoutManager.setStackFromEnd(true);
    }

    class RedPackVh extends RecyclerView.ViewHolder {

        TextView mTextView;

        public RedPackVh(View itemView) {
            super(itemView);
            mTextView = (TextView) itemView;
        }

        void setData(LiveChatBean bean) {
            mTextView.setText(bean.getContent());
        }
    }

    class SystemVh extends RecyclerView.ViewHolder {

        TextView mTextView;

        public SystemVh(View itemView) {
            super(itemView);
            mTextView = (TextView) itemView;
        }

        void setData(LiveChatBean bean) {
            mTextView.setText(bean.getContent());
        }
    }


    class Vh extends RecyclerView.ViewHolder {

        TextView mUserName;
        ImageView mLevel;
        View mVip;
        View mManage;
        ImageView mGuard;
        RoundedImageView avatar;
        ImageView frame;
        View mLiang;
        TextView mTextView;

        public Vh(View itemView) {
            super(itemView);
            mUserName = itemView.findViewById(R.id.user_name);
            mLevel = itemView.findViewById(R.id.level);
            mVip = itemView.findViewById(R.id.vip);
            mManage = itemView.findViewById(R.id.manage);
            mGuard = itemView.findViewById(R.id.guard);
            mLiang = itemView.findViewById(R.id.liang);
            mTextView = itemView.findViewById(R.id.text);
            itemView.setOnClickListener(mOnClickListener);
            avatar=itemView.findViewById(R.id.avatar);
            avatar.setVisibility(View.VISIBLE);
            frame=itemView.findViewById(R.id.frame);
        }

        void setData(LiveChatBean bean) {
            itemView.setTag(bean);
            if(bean.frame!=null){
                frame.setVisibility(View.VISIBLE);
                Glide.with(itemView.getContext()).load(bean.frame).into(frame);
            }else{
                frame.setImageDrawable(null);
                frame.setVisibility(View.GONE);
            }
            avatar.setVisibility(View.VISIBLE);
            if(bean.avatar!=null && !bean.avatar.isEmpty()){
                // Load the avatar from URL
                Glide.with(avatar.getContext())
                     .load(bean.avatar)
                     .error(R.mipmap.icon_avatar_none) // Use placeholder if loading fails
                     .placeholder(R.mipmap.icon_avatar_none) // Use placeholder while loading
                     .into(avatar);
            }else{
                // If no avatar, use placeholder
                avatar.setImageResource(R.mipmap.icon_avatar_none);
            }

            mUserName.setText(bean.getUserNiceName());
            LevelBean levelBean = CommonAppConfig.getInstance().getLevel(bean.getLevel());
            if (levelBean != null) {
                mUserName.setTextColor(bean.isAnchor() ? 0xffff5878 : levelBean.getColorValue());
                if (mLevel.getVisibility() != View.VISIBLE) {
                    mLevel.setVisibility(View.VISIBLE);
                }
                ImgLoader.display(mContext, levelBean.getThumb(), mLevel);
            } else {
                mUserName.setTextColor(bean.isAnchor() ? 0xffff5878 : 0xffffffff);
                if (mLevel.getVisibility() != View.GONE) {
                    mLevel.setVisibility(View.GONE);
                }
            }
            if (bean.getVipType() != 0) {
                if (mVip.getVisibility() != View.VISIBLE) {
                    mVip.setVisibility(View.VISIBLE);
                }
            } else {
                if (mVip.getVisibility() != View.GONE) {
                    mVip.setVisibility(View.GONE);
                }
            }
            if (bean.isManager()) {
                if (mManage.getVisibility() != View.VISIBLE) {
                    mManage.setVisibility(View.VISIBLE);
                }
            } else {
                if (mManage.getVisibility() != View.GONE) {
                    mManage.setVisibility(View.GONE);
                }
            }
            if (bean.getGuardType() != Constants.GUARD_TYPE_NONE) {
                if (mGuard.getVisibility() != View.VISIBLE) {
                    mGuard.setVisibility(View.VISIBLE);
                }
                mGuard.setImageResource(bean.getGuardType() == Constants.GUARD_TYPE_YEAR ? R.drawable.icon_live_chat_guard_2 : R.drawable.icon_live_chat_guard_1);
            } else {
                if (mGuard.getVisibility() != View.GONE) {
                    mGuard.setVisibility(View.GONE);
                }
            }
            if (!TextUtils.isEmpty(bean.getLiangName())) {
                if (mLiang.getVisibility() != View.VISIBLE) {
                    mLiang.setVisibility(View.VISIBLE);
                }
            } else {
                if (mLiang.getVisibility() != View.GONE) {
                    mLiang.setVisibility(View.GONE);
                }
            }
            if (bean.getType() == LiveChatBean.GIFT) {
                mTextView.setTextColor(0xffff5878);
            } else {
                mTextView.setTextColor(0xfff1f1f1);
            }
            if (bean.getType() == LiveChatBean.LIGHT) {
                Drawable heartDrawable = ContextCompat.getDrawable(CommonAppContext.getInstance(), LiveIconUtil.getLiveLightIcon(bean.getHeart()));
                if (heartDrawable != null) {
                    SpannableStringBuilder builder = new SpannableStringBuilder(bean.getContent());
                    builder.append(" ");
                    heartDrawable.setBounds(0, 0, DpUtil.dp2px(16), DpUtil.dp2px(16));
                    int length = builder.length();
                    builder.setSpan(new VerticalImageSpan(heartDrawable), length - 1, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    mTextView.setText(builder);
                } else {
                    mTextView.setText(bean.getContent());
                }
            } else {
                mTextView.setText(bean.getContent());
            }

        }
    }

    public void insertItem(LiveChatBean bean) {
        if (bean == null) {
            return;
        }
        int size = mList.size();
        mList.add(bean);
        notifyItemInserted(size);
        scrollToBottom();
    }

    public void insertList(List<LiveChatBean> list) {
        if (list == null || list.size() == 0) {
            return;
        }
        int size = mList.size();
        mList.addAll(list);
        notifyItemRangeInserted(size, list.size());
        scrollToBottom();
    }


    public void scrollToBottom() {
        if (!mList.isEmpty() && mRecyclerView != null) {
            mRecyclerView.scrollToPosition(mList.size() - 1);
            mRecyclerView.postDelayed(mRunnable, 10);
        }
    }


    public void clear() {
        if (mList != null) {
            mList.clear();
        }
        notifyDataSetChanged();
    }
}
