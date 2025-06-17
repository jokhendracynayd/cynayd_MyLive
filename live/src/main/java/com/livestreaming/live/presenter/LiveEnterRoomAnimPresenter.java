package com.livestreaming.live.presenter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.livestreaming.common.Constants;
import com.livestreaming.common.bean.UserBean;
import com.livestreaming.common.glide.ImgLoader;
import com.livestreaming.common.http.CommonHttpConsts;
import com.livestreaming.common.http.CommonHttpUtil;
import com.livestreaming.common.interfaces.CommonCallback;
import com.livestreaming.common.utils.DpUtil;
import com.livestreaming.common.utils.GifCacheUtil;
import com.livestreaming.common.utils.MD5Util;
import com.livestreaming.common.utils.ScreenDimenUtil;
import com.livestreaming.live.R;
import com.livestreaming.live.bean.LiveChatBean;
import com.livestreaming.live.bean.LiveEnterRoomBean;
import com.livestreaming.live.utils.LiveTextRender;
import com.opensource.svgaplayer.SVGACallback;
import com.opensource.svgaplayer.SVGAImageView;
import com.opensource.svgaplayer.SVGAParser;
import com.opensource.svgaplayer.SVGAVideoEntity;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by cxf on 2018/10/11.
 * 观众进场动画效果
 */

public class LiveEnterRoomAnimPresenter {

    private Context mContext;
    private View mBg;
    private View mUserGroup;
    private ImageView mAvatar;
    private TextView mName;
    private View mStar;
    private SVGAImageView mSvgaImageView;
    // private GifDrawable mGifDrawable;
    private TextView mWordText;
    private MediaController mMediaController;//koral--/android-gif-drawable 这个库用来播放gif动画的
    private ObjectAnimator mBgAnimator1;
    private ObjectAnimator mBgAnimator2;
    private ObjectAnimator mBgAnimator3;
    private ObjectAnimator mUserAnimator1;
    private ObjectAnimator mUserAnimator2;
    private ObjectAnimator mUserAnimator3;
    private Animation mStarAnim;
    private int mDp500;
    private boolean mIsAnimating;//是否在执行动画
    private ConcurrentLinkedQueue<LiveEnterRoomBean> mQueue;
    private Handler mHandler;
    private int mScreenWidth;
    private CommonCallback<File> mDownloadGifCallback;
    private boolean mShowGif;
    private boolean mEnd;
    private ArrayList<LiveEnterRoomBean> mEnterList =new ArrayList<>();
    private boolean isCashed=false;


    public LiveEnterRoomAnimPresenter(Context context, View root) {
        mContext = context;
        mBg = root.findViewById(R.id.jg_bg);
        mUserGroup = root.findViewById(R.id.jg_user);
        mAvatar = (ImageView) root.findViewById(R.id.jg_avatar);
        mName = (TextView) root.findViewById(R.id.jg_name);
        mStar = root.findViewById(R.id.star);
        mSvgaImageView = (SVGAImageView) root.findViewById(R.id.enter_room_svga);

        mWordText = (TextView) root.findViewById(R.id.enter_room_word);
        mSvgaImageView.setCallback(new SVGACallback() {
            @Override
            public void onPause() {

            }

            @Override
            public void onFinished() {
                if (mWordText != null) {
                    mWordText.setText("");
                    mSvgaImageView.stopAnimation();
                    mSvgaImageView.clearAnimation();
                    mSvgaImageView.clear();
                    if(isCashed){
                        if(!mEnterList.isEmpty()) {

                            Log.e("testEnteree_play_from_cash", "..........................................");
                            startAnim(mEnterList.get(mEnterList.size() - 1));
                            mEnterList.remove(mEnterList.size() - 1);
                        }
                    }else {

                        Log.e("testEnteree_play_removed", "..........................................");
                        mSvgaImageView.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onRepeat() {

            }

            @Override
            public void onStep(int i, double v) {

            }
        });
        mDp500 = DpUtil.dp2px(500);
        mQueue = new ConcurrentLinkedQueue<>();
        Interpolator interpolator1 = new AccelerateDecelerateInterpolator();
        Interpolator interpolator2 = new LinearInterpolator();
        Interpolator interpolator3 = new AccelerateInterpolator();
        mBgAnimator1 = ObjectAnimator.ofFloat(mBg, "translationX", DpUtil.dp2px(70));
        mBgAnimator1.setDuration(1000);
        mBgAnimator1.setInterpolator(interpolator1);

        mBgAnimator2 = ObjectAnimator.ofFloat(mBg, "translationX", 0);
        mBgAnimator2.setDuration(700);
        mBgAnimator2.setInterpolator(interpolator2);

        mBgAnimator3 = ObjectAnimator.ofFloat(mBg, "translationX", -mDp500);
        mBgAnimator3.setDuration(300);
        mBgAnimator3.setInterpolator(interpolator3);

        mUserAnimator1 = ObjectAnimator.ofFloat(mUserGroup, "translationX", DpUtil.dp2px(70));
        mUserAnimator1.setDuration(1000);
        mUserAnimator1.setInterpolator(interpolator1);
        mUserAnimator1.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mBgAnimator2.start();
                mUserAnimator2.start();
            }
        });

        mUserAnimator2 = ObjectAnimator.ofFloat(mUserGroup, "translationX", 0);
        mUserAnimator2.setDuration(700);
        mUserAnimator2.setInterpolator(interpolator2);
        mUserAnimator2.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mStar.startAnimation(mStarAnim);
            }
        });

        mUserAnimator3 = ObjectAnimator.ofFloat(mUserGroup, "translationX", mDp500);
        mUserAnimator3.setDuration(450);
        mUserAnimator3.setInterpolator(interpolator3);
        mUserAnimator3.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mBg.setTranslationX(mDp500);
                mUserGroup.setTranslationX(-mDp500);
                if (!mShowGif) {
                    getNextEnterRoom();
                }
            }
        });

        mStarAnim = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mStarAnim.setDuration(1500);
        mStarAnim.setInterpolator(interpolator2);
        mStarAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mBgAnimator3.start();
                mUserAnimator3.start();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mScreenWidth = ScreenDimenUtil.getInstance().getScreenWidth();
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                mShowGif = false;
                if (mWordText != null) {
                    mWordText.setText("");
                }
                if (mMediaController != null) {
                    mMediaController.hide();
                }
                if (mSvgaImageView != null) {
                    mSvgaImageView.stopAnimation();
                    mSvgaImageView.clearAnimation();
                }
//                if (mGifDrawable != null && !mGifDrawable.isRecycled()) {
//                    mGifDrawable.stop();
//                    mGifDrawable.recycle();
//                }
                getNextEnterRoom();
            }
        };
        mDownloadGifCallback = new CommonCallback<File>() {
            @Override
            public void callback(File file) {
                if (file != null) {
                    playGif(file);
                }
            }
        };
    }

    private void getNextEnterRoom() {
        if (mQueue == null) {
            return;
        }
        LiveEnterRoomBean bean = mQueue.poll();
        if (bean == null) {
            mIsAnimating = false;
        } else {
            startAnim(bean);
        }
    }

    public void enterRoom(LiveEnterRoomBean bean) {
        startAnim(bean);
    }

    private void startAnim(LiveEnterRoomBean bean) {
        UserBean u = bean.getUserBean();
        if(mSvgaImageView.getVisibility()==View.VISIBLE){
            mEnterList.add(bean);
            isCashed=true;
        }else {
            LiveChatBean liveChatBean = bean.getLiveChatBean();
            if (u != null && liveChatBean != null) {
                mIsAnimating = true;
                boolean needAnim = false;
                if (u.getVipType() != 0 || liveChatBean.getGuardType() != Constants.GUARD_TYPE_NONE) {
                    needAnim = true;
                    ImgLoader.displayAvatar(mContext, bean.getUserBean().getAvatar(), mAvatar);
                    LiveTextRender.renderEnterRoom(mContext, mName, liveChatBean);
                    mBgAnimator1.start();
                    mUserAnimator1.start();
                }
                UserBean.Car car = u.getCar();
                if (car != null && car.getId() != 0) {
                    String url = car.getSwf();
                    if (!TextUtils.isEmpty(url)) {

                        needAnim = true;
                        mShowGif = true;
                        //mWordText.setText(u.getUserNiceName() + car.getWords());
                        GifCacheUtil.getFile(MD5Util.getMD5(url), url, mDownloadGifCallback);
                    }
                }

//            getNextEnterRoom();
//            if (!needAnim) {
//            }
            }
        }
    }


    /**
     * 调整mGifImageView的大小
     */
//    private void resizeGifImageView(Drawable drawable) {
//        float w = drawable.getIntrinsicWidth();
//        float h = drawable.getIntrinsicHeight();
//        ViewGroup.LayoutParams params = mSvgaImageView.getLayoutParams();
//        params.height = (int) (mScreenWidth * h / w);
//        mSvgaImageView.setLayoutParams(params);
//    }

    /**
     * 播放gif
     */
    private void playGif(File file) {
        try {
//            mGifDrawable = new GifDrawable(file);
//            mGifDrawable.setLoopCount(1);
//            resizeGifImageView(mGifDrawable);
            Log.e("testEnteree_play_8", "..........................................");
                mSvgaImageView.setVisibility(View.VISIBLE);
                loadSVGALineAnimationFromAssets(file);
            // mGifImageView.setImageDrawable(mGifDrawable);
//            if (mMediaController == null) {
//                mMediaController = new MediaController(mContext);
//                mMediaController.setVisibility(View.GONE);
//            }
//            mMediaController.setMediaPlayer((GifDrawable) mGifImageView.getDrawable());
//            mMediaController.setAnchorView(mGifImageView);
//            int duration = mGifDrawable.getDuration();
//            mMediaController.show(duration);
//            if (duration < 4000) {
//                duration = 4000;
//            }
//            if (mHandler != null) {
//                mHandler.sendEmptyMessageDelayed(0, duration);
//            }
        } catch (Exception e) {
            e.printStackTrace();
            if (mHandler != null) {
                mHandler.sendEmptyMessageDelayed(0, 4000);
            }
        }
    }

    private void loadSVGALineAnimationFromAssets(File file) {
        // Initialize SVGAParser to load and decode SVGA animation
        SVGAParser parser = new SVGAParser(mContext);

        try {
            // Open the SVGA file from the assets folder
            InputStream inputStream = new FileInputStream(file);

            // Decode the SVGA file and set it to the SVGAImageView
            parser.decodeFromInputStream(inputStream, "", new SVGAParser.ParseCompletion() {
                @Override
                public void onComplete(@NonNull SVGAVideoEntity svgaVideoEntity) {
                    mSvgaImageView.setVideoItem(svgaVideoEntity);
                    // Start the animation
                    mSvgaImageView.setLoops(1);
                    mSvgaImageView.startAnimation();
                }

                @Override
                public void onError() {

                }
            }, true, new SVGAParser.PlayCallback() {
                @Override
                public void onPlay(@NonNull List<? extends File> list) {

                }

            }, "");
        } catch (IOException e) {
            // Handle error if the file is not found in assets
            e.printStackTrace();
        }
    }

    public void cancelAnim() {
        CommonHttpUtil.cancel(CommonHttpConsts.DOWNLOAD_GIF);
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        if (mBgAnimator1 != null) {
            mBgAnimator1.cancel();
        }
        if (mBgAnimator2 != null) {
            mBgAnimator2.cancel();
        }
        if (mBgAnimator3 != null) {
            mBgAnimator3.cancel();
        }
        if (mUserAnimator1 != null) {
            mUserAnimator1.cancel();
        }
        if (mUserAnimator2 != null) {
            mUserAnimator2.cancel();
        }
        if (mUserAnimator3 != null) {
            mUserAnimator3.cancel();
        }
        if (mStar != null) {
            mStar.clearAnimation();
        }
        if (mQueue != null) {
            mQueue.clear();
        }
        if (mMediaController != null) {
            mMediaController.hide();
            mMediaController.setAnchorView(null);
        }
        if (mSvgaImageView != null) {
            mSvgaImageView.stopAnimation();
            mSvgaImageView.clearAnimation();
        }
        if (mWordText != null) {
            mWordText.setText("");
        }
//        if (mGifDrawable != null && !mGifDrawable.isRecycled()) {
//            mGifDrawable.stop();
//            mGifDrawable.recycle();
//            mGifDrawable = null;
//        }
        mIsAnimating = false;
    }


    public void resetAnimView() {
        if (mBg != null) {
            mBg.setTranslationX(mDp500);
        }
        if (mUserGroup != null) {
            mUserGroup.setTranslationX(-mDp500);
        }
        if (mAvatar != null) {
            mAvatar.setImageDrawable(null);
        }
        if (mName != null) {
            mName.setText("");
        }
    }

    public void release() {
        mEnd = true;
        cancelAnim();
        mDownloadGifCallback = null;
        mHandler = null;
    }
}
