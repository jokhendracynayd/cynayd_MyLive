package com.livestreaming.video.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.livestreaming.common.CommonAppConfig;
import com.livestreaming.common.Constants;
import com.livestreaming.common.adapter.RefreshAdapter;
import com.livestreaming.common.bean.UserBean;
import com.livestreaming.common.glide.ImgLoader;
import com.livestreaming.common.http.HttpCallback;
import com.livestreaming.common.utils.FaceTextUtil;
import com.livestreaming.common.utils.RouteUtil;
import com.livestreaming.common.utils.ToastUtil;
import com.livestreaming.video.R;
import com.livestreaming.video.bean.VideoCommentBean;
import com.livestreaming.video.http.VideoHttpUtil;

import java.util.List;

/**
 * Created by cxf on 2018/12/3.
 */

public class VideoCommentAdapter extends RefreshAdapter<VideoCommentBean> {

    private static final int PARENT = 1;
    private static final int CHILD = 2;
    private Drawable mLikeDrawable;
    private Drawable mUnLikeDrawable;
    private int mLikeColor;
    private int mUnLikeColor;
    private ScaleAnimation mLikeAnimation;
    private View.OnClickListener mOnClickListener;
    private View.OnLongClickListener mOnLongClickListener;
    private View.OnClickListener mLikeClickListener;
    private View.OnClickListener mExpandClickListener;
    private View.OnClickListener mCollapsedClickListener;
    private View.OnClickListener mAvatarClickListener;
    private ActionListener mActionListener;
    private ImageView mCurLikeImageView;
    private int mCurLikeCommentPosition;
    private VideoCommentBean mCurLikeCommentBean;
    private HttpCallback mLikeCommentCallback;
    private String mVideoUid;

    public VideoCommentAdapter(Context context) {
        super(context);
        mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Object tag = v.getTag();
                if (tag != null && mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(((VideoCommentBean) tag), 0);
                }
            }
        };
        mOnLongClickListener = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Object tag = v.getTag();
                if (tag != null && mActionListener != null) {
                    mActionListener.onItemLongClick((VideoCommentBean) tag);
                }
                return true;
            }
        };
        mLikeDrawable = ContextCompat.getDrawable(context, R.drawable.bg_video_comment_like_1);
        mUnLikeDrawable = ContextCompat.getDrawable(context, R.drawable.bg_video_comment_like_0);
        mLikeColor = ContextCompat.getColor(context,com.livestreaming.common. R.color.red);
        mUnLikeColor = ContextCompat.getColor(context, com.livestreaming.common.R.color.gray3);
        mLikeAnimation = new ScaleAnimation(1f, 0.5f, 1f, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mLikeAnimation.setDuration(200);
        mLikeAnimation.setRepeatCount(1);
        mLikeAnimation.setRepeatMode(Animation.REVERSE);
        mLikeAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                if (mCurLikeCommentBean != null) {
                    if (mCurLikeImageView != null) {
                        mCurLikeImageView.setImageDrawable(mCurLikeCommentBean.getIsLike() == 1 ? mLikeDrawable : mUnLikeDrawable);
                    }
                }
            }
        });
        mLikeCommentCallback = new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0 && info.length > 0 && mCurLikeCommentBean != null) {
                    JSONObject obj = JSON.parseObject(info[0]);
                    int like = obj.getIntValue("islike");
                    String likeNum = obj.getString("likes");
                    if (mCurLikeCommentBean != null) {
                        mCurLikeCommentBean.setIsLike(like);
                        mCurLikeCommentBean.setLikeNum(likeNum);
                        notifyItemChanged(mCurLikeCommentPosition, Constants.PAYLOAD);
                    }
                    if (mCurLikeImageView != null && mLikeAnimation != null) {
                        mCurLikeImageView.startAnimation(mLikeAnimation);
                    }
                }
            }
        };
        mLikeClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Object tag = v.getTag();
                if (tag == null) {
                    return;
                }
                VideoCommentBean bean = (VideoCommentBean) tag;
                String uid = bean.getUid();
                if (!TextUtils.isEmpty(uid) && uid.equals(CommonAppConfig.getInstance().getUid())) {
                    ToastUtil.show(com.livestreaming.common.R.string.video_comment_cannot_self);
                    return;
                }
                mCurLikeImageView = (ImageView) v;
                mCurLikeCommentPosition = bean.getPosition();
                mCurLikeCommentBean = bean;
                VideoHttpUtil.setCommentLike(bean.getId(), mLikeCommentCallback);
            }
        };
        mExpandClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Object tag = v.getTag();
                if (tag != null && mActionListener != null) {
                    mActionListener.onExpandClicked((VideoCommentBean) tag);
                }
            }
        };
        mCollapsedClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Object tag = v.getTag();
                if (tag != null && mActionListener != null) {
                    mActionListener.onCollapsedClicked((VideoCommentBean) tag);
                }
            }
        };
        mAvatarClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String toUid = (String) v.getTag(R.id.avatar);
                RouteUtil.forwardUserHome(mContext, toUid);
            }
        };
    }

    @Override
    public int getItemCount() {
        int count = 0;
        for (int i = 0, size = mList.size(); i < size; i++) {
            count++;
            List<VideoCommentBean> childList = mList.get(i).getChildList();
            if (childList != null) {
                count += childList.size();
            }
        }
        return count;
    }

    public void setVideoUid(String videoUid) {
        mVideoUid = videoUid;
    }

    private VideoCommentBean getItem(int position) {
        int index = 0;
        for (int i = 0, size = mList.size(); i < size; i++) {
            VideoCommentBean parentNode = mList.get(i);
            if (index == position) {
                return parentNode;
            }
            index++;
            List<VideoCommentBean> childList = mList.get(i).getChildList();
            if (childList != null) {
                for (int j = 0, childSize = childList.size(); j < childSize; j++) {
                    if (position == index) {
                        return childList.get(j);
                    }
                    index++;
                }
            }
        }
        return null;
    }


    /**
     * 展开子评论
     *
     * @param childNode   在这个子评论下面插入子评论
     * @param insertCount 插入的子评论的个数
     */
    public void insertReplyList(VideoCommentBean childNode, int insertCount) {
        //这种方式也能达到  notifyItemRangeInserted 的效果，而且不容易出问题
        if (mRecyclerView != null) {
            mRecyclerView.scrollToPosition(childNode.getPosition());
        }
        //notifyItemRangeChanged(childNode.getPosition(), getItemCount(), Constants.PAYLOAD);
        notifyItemRangeChanged(childNode.getPosition(), getItemCount(), null);
    }

    /**
     * 收起子评论
     *
     * @param childNode   在这个子评论下面收起子评论
     * @param removeCount 被收起的子评论的个数
     */
    public void removeReplyList(VideoCommentBean childNode, int removeCount) {
        //这种方式也能达到  notifyItemRangeRemoved 的效果，而且不容易出问题
        if (mRecyclerView != null) {
            mRecyclerView.scrollToPosition(childNode.getPosition());
        }
        //notifyItemRangeChanged(childNode.getPosition(), getItemCount(), Constants.PAYLOAD);
        notifyItemRangeChanged(childNode.getPosition(), getItemCount(), null);
    }

    @Override
    public int getItemViewType(int position) {
        VideoCommentBean bean = getItem(position);
        if (bean != null && bean.isParentNode()) {
            return PARENT;
        }
        return CHILD;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == PARENT) {
            return new ParentVh(mInflater.inflate(R.layout.item_video_comment_parent, parent, false));
        }
        return new ChildVh(mInflater.inflate(R.layout.item_video_comment_child, parent, false));

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder vh, int position, @NonNull List payloads) {
        VideoCommentBean bean = getItem(position);
        if (bean != null) {
            bean.setPosition(position);
            Object payload = payloads.size() > 0 ? payloads.get(0) : null;
            if (vh instanceof ParentVh) {
                ((ParentVh) vh).setData(bean, payload);
            } else if (vh instanceof ChildVh) {
                ((ChildVh) vh).setData(bean, payload);
            }
        }
    }


    class Vh extends RecyclerView.ViewHolder {

        TextView mName;
        TextView mContent;
        ImageView mAvatar;
        View mAuthor;

        public Vh(View itemView) {
            super(itemView);
            mName = (TextView) itemView.findViewById(R.id.name);
            mContent = (TextView) itemView.findViewById(R.id.content);
            itemView.setOnClickListener(mOnClickListener);
            itemView.setOnLongClickListener(mOnLongClickListener);
            mAvatar = itemView.findViewById(R.id.avatar);
            mAuthor = itemView.findViewById(R.id.author);
            if (mAvatar != null) {
                mAvatar.setOnClickListener(mAvatarClickListener);
            }
//            mName.setOnClickListener(mAvatarClickListener);
        }

        void setData(VideoCommentBean bean, Object payload) {
            itemView.setTag(bean);
            if (payload == null) {
                UserBean u = bean.getUserBean();
                if (u != null) {
                    if (mAvatar != null) {
                        ImgLoader.displayAvatar(mContext, u.getAvatar(), mAvatar);
                        mAvatar.setTag(R.id.avatar, u.getId());
                    }
                    mName.setText(u.getUserNiceName());
//                    mName.setTag(R.id.avatar, u.getId());
                }
                mContent.setText(FaceTextUtil.renderVideoComment(bean.getContent(), "  " + bean.getDatetime()));
                if (!TextUtils.isEmpty(mVideoUid) && mVideoUid.equals(bean.getUid())) {
                    if (mAuthor.getVisibility() != View.VISIBLE) {
                        mAuthor.setVisibility(View.VISIBLE);
                    }
                } else {
                    if (mAuthor.getVisibility() == View.VISIBLE) {
                        mAuthor.setVisibility(View.INVISIBLE);
                    }
                }
            }
        }
    }

    class ParentVh extends Vh {

        ImageView mBtnLike;
        TextView mLikeNum;

        public ParentVh(View itemView) {
            super(itemView);
            mBtnLike = (ImageView) itemView.findViewById(R.id.btn_like);
            mLikeNum = (TextView) itemView.findViewById(R.id.like_num);
            mBtnLike.setOnClickListener(mLikeClickListener);

        }

        void setData(VideoCommentBean bean, Object payload) {
            super.setData(bean, payload);
            mBtnLike.setTag(bean);
            boolean like = bean.getIsLike() == 1;
            mBtnLike.setImageDrawable(like ? mLikeDrawable : mUnLikeDrawable);
            mLikeNum.setText(bean.getLikeNum());
            mLikeNum.setTextColor(like ? mLikeColor : mUnLikeColor);
        }
    }

    class ChildVh extends Vh {

        View mBtnGroup;
        View mBtnExpand;//展开按钮
        View mBtnbCollapsed;//收起按钮

        public ChildVh(View itemView) {
            super(itemView);
            mBtnGroup = itemView.findViewById(R.id.btn_group);
            mBtnExpand = itemView.findViewById(R.id.btn_expand);
            mBtnbCollapsed = itemView.findViewById(R.id.btn_collapsed);
            mBtnExpand.setOnClickListener(mExpandClickListener);
            mBtnbCollapsed.setOnClickListener(mCollapsedClickListener);
        }

        void setData(VideoCommentBean bean, Object payload) {
            super.setData(bean, payload);
            mBtnExpand.setTag(bean);
            mBtnbCollapsed.setTag(bean);
            VideoCommentBean parentNodeBean = bean.getParentNodeBean();
            if (bean.needShowExpand(parentNodeBean)) {
                if (mBtnGroup.getVisibility() != View.VISIBLE) {
                    mBtnGroup.setVisibility(View.VISIBLE);
                }
                if (mBtnbCollapsed.getVisibility() == View.VISIBLE) {
                    mBtnbCollapsed.setVisibility(View.INVISIBLE);
                }
                if (mBtnExpand.getVisibility() != View.VISIBLE) {
                    mBtnExpand.setVisibility(View.VISIBLE);
                }
            } else if (bean.needShowCollapsed(parentNodeBean)) {
                if (mBtnGroup.getVisibility() != View.VISIBLE) {
                    mBtnGroup.setVisibility(View.VISIBLE);
                }
                if (mBtnExpand.getVisibility() == View.VISIBLE) {
                    mBtnExpand.setVisibility(View.INVISIBLE);
                }
                if (mBtnbCollapsed.getVisibility() != View.VISIBLE) {
                    mBtnbCollapsed.setVisibility(View.VISIBLE);
                }
            } else {
                if (mBtnGroup.getVisibility() == View.VISIBLE) {
                    mBtnGroup.setVisibility(View.GONE);
                }
            }
        }
    }


    public interface ActionListener {
        void onExpandClicked(VideoCommentBean commentBean);

        void onCollapsedClicked(VideoCommentBean commentBean);

        void onItemLongClick(VideoCommentBean bean);
    }

    public void setActionListener(ActionListener actionListener) {
        mActionListener = actionListener;
    }

}
