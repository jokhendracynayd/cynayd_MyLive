package com.livestreaming.video.views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.livestreaming.common.CommonAppConfig;
import com.livestreaming.common.activity.AbsActivity;
import com.livestreaming.common.bean.UserBean;
import com.livestreaming.common.glide.ImgLoader;
import com.livestreaming.common.http.CommonHttpUtil;
import com.livestreaming.common.http.HttpCallback;
import com.livestreaming.common.interfaces.CommonCallback;
import com.livestreaming.common.utils.RouteUtil;
import com.livestreaming.common.utils.StringUtil;
import com.livestreaming.common.utils.ToastUtil;
import com.livestreaming.common.views.AbsViewHolder;
import com.livestreaming.video.R;
import com.livestreaming.video.activity.AbsVideoPlayActivity;
import com.livestreaming.video.bean.VideoBean;
import com.livestreaming.video.event.VideoLikeEvent;
import com.livestreaming.video.http.VideoHttpUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by cxf on 2018/11/26.
 * 视频播放外框
 */

public class VideoPlayWrapViewHolder extends AbsViewHolder implements View.OnClickListener {
    private static final String SPACE = "                 ";
    private ViewGroup mVideoContainer;
    private ImageView mCover;
    private ImageView mAvatar;
    private TextView mName;
    private TextView mTitle;
    private ImageView mBtnLike;//点赞按钮
    private TextView mLikeNum;//点赞数
    private TextView mCommentNum;//评论数
    private TextView mShareNum;//分享数
    private ImageView mBtnFollow;//关注按钮
    private TextView mBtnGoods;//查看同款商品
    private View mAd;
    private VideoBean mVideoBean;
    private Drawable mFollowDrawable;//已关注
    private Drawable mUnFollowDrawable;//未关注
    private Animation mFollowAnimation;
    private boolean mCurPageShowed;//当前页面是否可见
    private ValueAnimator mLikeAnimtor;
    private Drawable[] mLikeAnimDrawables;//点赞帧动画
    private int mLikeAnimIndex;
    private String mTag;
    private TextView mMusicTitle;
    private ImageView mMusicThumb;
    private View mMusicThumbGroup;
    private RotateAnimation mRotateAnimation;


    public VideoPlayWrapViewHolder(Context context, ViewGroup parentView) {
        super(context, parentView);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_video_play_wrap;
    }

    @Override
    public void init() {
        mTag = this.toString();
        mVideoContainer = (ViewGroup) findViewById(R.id.video_container);
        mCover = (ImageView) findViewById(R.id.cover);
        mAvatar = (ImageView) findViewById(R.id.avatar);
        mName = (TextView) findViewById(R.id.name);
        mTitle = (TextView) findViewById(R.id.title);
        mBtnLike = (ImageView) findViewById(R.id.btn_like);
        mLikeNum = (TextView) findViewById(R.id.like_num);
        mCommentNum = (TextView) findViewById(R.id.comment_num);
        mShareNum = (TextView) findViewById(R.id.share_num);
        mBtnFollow = (ImageView) findViewById(R.id.btn_follow);
        mAd = findViewById(R.id.ad);
        mFollowDrawable = ContextCompat.getDrawable(mContext, R.mipmap.icon_video_follow_1);
        mUnFollowDrawable = ContextCompat.getDrawable(mContext, R.mipmap.icon_video_follow_0);
        mAvatar.setOnClickListener(this);
        mBtnFollow.setOnClickListener(this);
        mBtnLike.setOnClickListener(this);
        findViewById(R.id.btn_comment).setOnClickListener(this);
        findViewById(R.id.btn_share).setOnClickListener(this);
        mBtnGoods = findViewById(R.id.btn_goods);
        mBtnGoods.setOnClickListener(this);
        mMusicTitle = findViewById(R.id.music_title);
        mMusicThumb = findViewById(R.id.music_thumb);
        mMusicThumbGroup = findViewById(R.id.music_thumb_group);
        RotateAnimation rotateAnimation = new RotateAnimation(0, 359, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setDuration(5000);
        rotateAnimation.setRepeatCount(-1);
        rotateAnimation.setInterpolator(new LinearInterpolator());
        mRotateAnimation = rotateAnimation;
    }

    /**
     * 初始化点赞动画
     */
    private void initLikeAnimtor() {
        if (mLikeAnimDrawables != null && mLikeAnimDrawables.length > 0) {
            mLikeAnimtor = ValueAnimator.ofFloat(0, mLikeAnimDrawables.length);
            mLikeAnimtor.setDuration(800);
            mLikeAnimtor.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float v = (float) animation.getAnimatedValue();
                    int index = (int) v;
                    if (mLikeAnimIndex != index) {
                        mLikeAnimIndex = index;
                        if (mBtnLike != null && mLikeAnimDrawables != null && index < mLikeAnimDrawables.length) {
                            mBtnLike.setImageDrawable(mLikeAnimDrawables[index]);
                        }
                    }
                }
            });
        }
    }

    /**
     * 初始化关注动画
     */
    public void initFollowAnimation() {
        mFollowAnimation = new ScaleAnimation(1, 0.3f, 1, 0.3f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mFollowAnimation.setRepeatMode(Animation.REVERSE);
        mFollowAnimation.setRepeatCount(1);
        mFollowAnimation.setDuration(200);
        mFollowAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                if (mBtnFollow != null && mVideoBean != null) {
                    if (mVideoBean.getAttent() == 1) {
                        mBtnFollow.setImageDrawable(mFollowDrawable);
                    } else {
                        mBtnFollow.setImageDrawable(mUnFollowDrawable);
                    }
                }
            }
        });
    }

    public void setLikeAnimDrawables(Drawable[] drawables) {
        mLikeAnimDrawables = drawables;
    }

    public void setData(VideoBean bean, Object payload) {
        if (bean == null) {
            return;
        }
        mVideoBean = bean;
        UserBean u = mVideoBean.getUserBean();
        if (payload == null) {
            if (mCover != null) {
                setCoverImage();
            }
            if (mTitle != null) {
                mTitle.setText(bean.getTitle());
            }
            if (u != null) {
                if (mAvatar != null) {
                    ImgLoader.displayAvatar(mContext, u.getAvatar(), mAvatar);
                }
                if (mName != null) {
                    mName.setText("@" + u.getUserNiceName());
                }
            }
        }
        if (mBtnLike != null) {
            if (bean.getLike() == 1) {
                if (mLikeAnimDrawables != null && mLikeAnimDrawables.length > 0) {
                    mBtnLike.setImageDrawable(mLikeAnimDrawables[mLikeAnimDrawables.length - 1]);
                }
            } else {
                mBtnLike.setImageResource(R.mipmap.icon_video_zan_01);
            }
        }
        if (mLikeNum != null) {
            mLikeNum.setText(bean.getLikeNum());
        }
        if (mCommentNum != null) {
            mCommentNum.setText(bean.getCommentNum());
        }
        if (mShareNum != null) {
            mShareNum.setText(bean.getShareNum());
        }
        if (u != null && mBtnFollow != null) {
            String toUid = u.getId();
            if (!TextUtils.isEmpty(toUid) && !toUid.equals(CommonAppConfig.getInstance().getUid())) {
                if (mBtnFollow.getVisibility() != View.VISIBLE) {
                    mBtnFollow.setVisibility(View.VISIBLE);
                }
                if (bean.getAttent() == 1) {
                    mBtnFollow.setImageDrawable(mFollowDrawable);
                } else {
                    mBtnFollow.setImageDrawable(mUnFollowDrawable);
                }
            } else {
                if (mBtnFollow.getVisibility() == View.VISIBLE) {
                    mBtnFollow.setVisibility(View.INVISIBLE);
                }
            }
        }
        if (mAd != null) {
            if (bean.getIsAd() == 1) {
                if (mAd.getVisibility() != View.VISIBLE) {
                    mAd.setVisibility(View.VISIBLE);
                }
            } else {
                if (mAd.getVisibility() != View.INVISIBLE) {
                    mAd.setVisibility(View.INVISIBLE);
                }
            }
        }
        if (mBtnGoods != null) {
            if (bean.getIsAd() == 1) {
                if (mBtnGoods.getVisibility() != View.VISIBLE) {
                    mBtnGoods.setVisibility(View.VISIBLE);
                }
                mBtnGoods.setText(com.livestreaming.common.R.string.goods_tip_32_1);
            } else {
                int goodsType = bean.getType();
                if (goodsType == 0) {
                    if (mBtnGoods.getVisibility() != View.GONE) {
                        mBtnGoods.setVisibility(View.GONE);
                    }
                } else {
                    if (mBtnGoods.getVisibility() != View.VISIBLE) {
                        mBtnGoods.setVisibility(View.VISIBLE);
                    }
                    if (goodsType == 1) {//商品
                        mBtnGoods.setText(com.livestreaming.common.R.string.goods_tip_32);
                    } else if (goodsType == 2) {//付费内容
                        mBtnGoods.setText(com.livestreaming.common.R.string.mall_356);
                    }
                }
            }
        }
        if (mMusicTitle != null) {
            String title = bean.getMusicTitle();
            if (!TextUtils.isEmpty(title)) {
                mMusicTitle.setText(StringUtil.contact(title, SPACE, title, SPACE, title, SPACE, title, SPACE, title));
            } else {
                mMusicTitle.setText(null);
            }
        }
        if (mMusicThumb != null) {
            String musicImageUrl = bean.getMusicImageUrl();
            if (!TextUtils.isEmpty(musicImageUrl)) {
                ImgLoader.display(mContext, musicImageUrl, mMusicThumb);
            } else {
                if (u != null) {
                    ImgLoader.display(mContext, u.getAvatar(), mMusicThumb);
                } else {
                    mMusicThumb.setImageDrawable(null);
                }
            }
        }
        if (mMusicThumbGroup != null && mRotateAnimation != null) {
            mMusicThumbGroup.startAnimation(mRotateAnimation);
        }
    }

    private void setCoverImage() {
        ImgLoader.displayDrawable(mContext, mVideoBean.getThumb(), new ImgLoader.DrawableCallback() {
            @Override
            public void onLoadSuccess(Drawable drawable) {
                if (mCover != null && drawable != null) {
                    float w = drawable.getIntrinsicWidth();
                    float h = drawable.getIntrinsicHeight();
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mCover.getLayoutParams();
                    int targetH = 0;
                    if (w / h > 0.5625f) {//横屏  9:16=0.5625
                        targetH = (int) (mCover.getWidth() / w * h);
                    } else {
                        targetH = ViewGroup.LayoutParams.MATCH_PARENT;
                    }
                    if (targetH != params.height) {
                        params.height = targetH;
                        mCover.requestLayout();
                    }
                    mCover.setImageDrawable(drawable);
                }
            }

            @Override
            public void onLoadFailed() {

            }
        });
    }

    public void addVideoView(View view) {
        if (mVideoContainer != null && view != null) {
            ViewParent parent = view.getParent();
            if (parent != null) {
                ViewGroup viewGroup = (ViewGroup) parent;
                if (viewGroup != mVideoContainer) {
                    viewGroup.removeView(view);
                    mVideoContainer.addView(view);
                }
            } else {
                mVideoContainer.addView(view);
            }
        }
    }

    public VideoBean getVideoBean() {
        return mVideoBean;
    }


    /**
     * 获取到视频首帧回调
     */
    public void onFirstFrame() {
        if (mCover != null && mCover.getVisibility() == View.VISIBLE) {
            mCover.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * 滑出屏幕
     */
    public void onPageOutWindow() {
        mCurPageShowed = false;
        if (mCover != null && mCover.getVisibility() != View.VISIBLE) {
            mCover.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 滑入屏幕
     */
    public void onPageInWindow() {
        if (mCover != null) {
            if (mCover.getVisibility() != View.VISIBLE) {
                mCover.setVisibility(View.VISIBLE);
            }
            mCover.setImageDrawable(null);
            setCoverImage();
        }
    }

    /**
     * 滑动到这一页 准备开始播放
     */
    public void onPageSelected() {
        mCurPageShowed = true;
    }

    @Override
    public void onClick(View v) {
        if (!canClick()) {
            return;
        }
        int i = v.getId();
        if (i != R.id.avatar && i != R.id.btn_goods) {
            if (!((AbsActivity) mContext).checkLogin()) {
                return;
            }
        }
        if (i == R.id.btn_follow) {
            clickFollow();

        } else if (i == R.id.btn_comment) {
            clickComment();

        } else if (i == R.id.btn_share) {
            clickShare();

        } else if (i == R.id.btn_like) {
            clickLike();

        } else if (i == R.id.avatar) {
            clickAvatar();

        } else if (i == R.id.btn_goods) {
            clickGoods();
        }
    }

    /**
     * 点击  查看同款商品
     */
    private void clickGoods() {
        if (mContext != null && mVideoBean != null) {
            if (mVideoBean.getIsAd() == 1) {//广告
                String adUrl = mVideoBean.getAdUrl();
                if (!TextUtils.isEmpty(adUrl)) {
                    try {
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(adUrl));
                        mContext.startActivity(intent);
                    } catch (Exception e) {
                        ToastUtil.show(com.livestreaming.common.R.string.goods_tip_32_2);
                    }
                }
            } else {
                if (mVideoBean.getType() == 1) {//商品
                    if (mVideoBean.getGoodsType() == 1) {
                        RouteUtil.forwardGoodsDetailOutSide(mContext, mVideoBean.getGoodsId(), false);
                    } else {
                        RouteUtil.forwardGoodsDetail(mContext, mVideoBean.getGoodsId(), false, mVideoBean.getUid(), null);
                    }
                } else if (mVideoBean.getType() == 2) {//付费内容
                    RouteUtil.forwardPayContentDetail(mContext, mVideoBean.getGoodsId());
                }
            }
        }
    }


    /**
     * 点击头像
     */
    public void clickAvatar() {
        if (mVideoBean != null) {
            RouteUtil.forwardUserHome(mContext, mVideoBean.getUid());
        }
    }

    /**
     * 点赞,取消点赞
     */
    private void clickLike() {
        if (mVideoBean == null) {
            return;
        }
        VideoHttpUtil.setVideoLike(mTag, mVideoBean.getId(), new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0 && info.length > 0) {
                    JSONObject obj = JSON.parseObject(info[0]);
                    String likeNum = obj.getString("likes");
                    int like = obj.getIntValue("islike");
                    if (mVideoBean != null) {
                        mVideoBean.setLikeNum(likeNum);
                        mVideoBean.setLike(like);
                        EventBus.getDefault().post(new VideoLikeEvent(mVideoBean.getId(), like, likeNum));
                    }
                    if (mLikeNum != null) {
                        mLikeNum.setText(likeNum);
                    }
                    if (mBtnLike != null) {
                        if (like == 1) {
                            if (mLikeAnimtor == null) {
                                initLikeAnimtor();
                            }
                            mLikeAnimIndex = -1;
                            if (mLikeAnimtor != null) {
                                mLikeAnimtor.start();
                            }
                        } else {
                            mBtnLike.setImageResource(R.mipmap.icon_video_zan_01);
                        }
                    }

                } else {
                    ToastUtil.show(msg);
                }
            }
        });
    }

    /**
     * 点击关注按钮
     */
    private void clickFollow() {
        if (mVideoBean == null) {
            return;
        }
        final UserBean u = mVideoBean.getUserBean();
        if (u == null) {
            return;
        }
        CommonHttpUtil.setAttention(mTag, u.getId(), new CommonCallback<Integer>() {
            @Override
            public void callback(Integer attent) {
                mVideoBean.setAttent(attent);
                if (mCurPageShowed) {
                    if (mFollowAnimation == null) {
                        initFollowAnimation();
                    }
                    mBtnFollow.startAnimation(mFollowAnimation);
                } else {
                    if (attent == 1) {
                        mBtnFollow.setImageDrawable(mFollowDrawable);
                    } else {
                        mBtnFollow.setImageDrawable(mUnFollowDrawable);
                    }
                }
            }
        });
    }

    /**
     * 点击评论按钮
     */
    private void clickComment() {
        ((AbsVideoPlayActivity) mContext).openCommentWindow(true, false, true, mVideoBean.getId(), mVideoBean.getUid());
    }

    /**
     * 点击分享按钮
     */
    private void clickShare() {
        if (mVideoBean == null) {
            return;
        }
        shareVideo();
        /*
        VideoShareDialogFragment fragment = new VideoShareDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.VIDEO_BEAN, mVideoBean);
        fragment.setArguments(bundle);
        fragment.show(((AbsVideoPlayActivity) mContext).getSupportFragmentManager(), "VideoShareDialogFragment");
    */
    }
    public void shareVideo() {
        if(mVideoBean!=null) {
            FirebaseDynamicLinks.getInstance().createDynamicLink()
                    .setLink(Uri.parse("https://donalivest.page.link/live/stream?video_info=" + new Gson().toJson(mVideoBean)))  // Deep link to content
                    .setDomainUriPrefix("https://donalivest.page.link")         // Dynamic link domain
                    .setAndroidParameters(new DynamicLink.AndroidParameters.Builder("com.livestreaming").build()) // Android settings
                    .buildShortDynamicLink()
                    .addOnCompleteListener(new OnCompleteListener<ShortDynamicLink>() {
                        @Override
                        public void onComplete(@NonNull Task<ShortDynamicLink> task) {
                            if (task.isSuccessful()) {
                                // Short link created successfully
                                Uri shortLink = task.getResult().getShortLink();
                                Uri flowchartLink = task.getResult().getPreviewLink();

                                // Share the short dynamic link
                                Intent intent = new Intent();
                                intent.setAction(Intent.ACTION_SEND);
                                intent.putExtra(Intent.EXTRA_TEXT, "See my Video: " + shortLink.toString());
                                intent.setType("text/plain");
                                mContext.startActivity(Intent.createChooser(intent, "Share Dynamic Link"));
                            } else {
                                // Handle error
                            }
                        }
                    });
        }
    }

    public void release() {
        VideoHttpUtil.cancel(mTag);
        if (mLikeAnimtor != null) {
            mLikeAnimtor.cancel();
        }
        if (mBtnFollow != null && mFollowAnimation != null) {
            mBtnFollow.clearAnimation();
        }
    }


}
