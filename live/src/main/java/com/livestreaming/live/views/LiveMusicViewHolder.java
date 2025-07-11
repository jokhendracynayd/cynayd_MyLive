package com.livestreaming.live.views;

import android.content.Context;
import android.os.Handler;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.livestreaming.common.utils.StringUtil;
import com.livestreaming.common.views.AbsViewHolder;
import com.livestreaming.live.R;
import com.livestreaming.live.activity.LiveAnchorActivity;
import com.livestreaming.live.interfaces.ILivePushViewHolder;
import com.livestreaming.live.music.LiveMusicPlayer;
import com.livestreaming.live.music.LrcTextView;

/**
 * Created by cxf on 2018/10/22.
 * 直播间背景音乐
 */

public class LiveMusicViewHolder extends AbsViewHolder implements View.OnClickListener, View.OnTouchListener {

    private LrcTextView mLrcTextView;//歌词控件
    private TextView mBtnEnd;//关闭按钮
    private TextView mTimeTextView;//时间
    private ILivePushViewHolder mILivePushViewHolder;
    private int mParentWidth;
    private int mParentHeight;
    private float mLastX;
    private float mLastY;
    private boolean mPaused;
    private LiveMusicPlayer mLiveMusicPlayer;
    private final StringBuilder mStringBuilder;
    private final Runnable mRunnable;
    private final Handler mHandler;
    private long mNextTime;
    private int mSecondCount;

    public LiveMusicViewHolder(Context context, ViewGroup parentView, ILivePushViewHolder livePushViewHolder) {
        super(context, parentView);
        mILivePushViewHolder = livePushViewHolder;
        mParentWidth = parentView.getWidth();
        mParentHeight = parentView.getHeight();
        mStringBuilder = new StringBuilder();
        mHandler = new Handler();
        mRunnable = new Runnable() {
            @Override
            public void run() {
                if (mTimeTextView != null) {
                    mTimeTextView.setText(StringUtil.getDurationText2(mSecondCount, mStringBuilder));
                }
                mSecondCount++;
                mNextTime += 1000;
                mHandler.postAtTime(mRunnable, mNextTime);
            }
        };
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_live_music;
    }

    @Override
    public void init() {
        mBtnEnd = (TextView) findViewById(R.id.btn_end);
        mBtnEnd.setOnClickListener(this);
        mTimeTextView = (TextView) findViewById(R.id.time);
        mLrcTextView = (LrcTextView) findViewById(R.id.lrc);
        mContentView.setOnTouchListener(this);
        mLiveMusicPlayer = new LiveMusicPlayer();
        mLiveMusicPlayer.setActionListener(new LiveMusicPlayer.ActionListener() {
            @Override
            public void onPrepareSuccess(String path) {
                if (mILivePushViewHolder != null) {
                    mILivePushViewHolder.startBgm(path);
                }
                mHandler.removeCallbacksAndMessages(null);
                mNextTime = SystemClock.uptimeMillis();
                mSecondCount = 0;
                if (mRunnable != null) {
                    mRunnable.run();
                }
            }

            @Override
            public void onCompletion(String path) {
//                mTime = 0;
//                if (mTimeTextView != null) {
//                    mTimeTextView.setText("00:00");
//                }
//                if (mHandler != null) {
//                    mHandler.removeCallbacksAndMessages(null);
////                    mHandler.sendEmptyMessageDelayed(0, 1000);
//                }
//                if (mILivePushViewHolder != null) {
//                    mILivePushViewHolder.startBgm(path);
//                }

                if (mContext != null) {
                    ((LiveAnchorActivity) mContext).stopBgm();
                } else {
                    release();
                }
            }

            @Override
            public void onParseLrcResult(boolean success) {
                if (!success && mLrcTextView != null) {
                    mLrcTextView.setText(com.livestreaming.common.R.string.music_lrc_not_found);
                }
            }


            @Override
            public void onLrcChanged(String lrc) {
                if (mLrcTextView != null) {
                    mLrcTextView.setText(lrc);
                }

            }

            @Override
            public void onLrcProgressChanged(float progress) {
                if (mLrcTextView != null) {
                    mLrcTextView.setProgress(progress);
                }
            }

        });
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_end) {
            ((LiveAnchorActivity) mContext).stopBgm();
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent e) {
        float x = e.getRawX();
        float y = e.getRawY();
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastX = x;
                mLastY = y;
                break;
            case MotionEvent.ACTION_MOVE:
                float dx = x - mLastX;
                float dy = y - mLastY;
                if (dx != 0) {
                    float targetX = mContentView.getTranslationX() + dx;
                    if (targetX < 0) {
                        targetX = 0;
                    }
                    int rightLimit = mParentWidth - mContentView.getWidth();
                    if (targetX > rightLimit) {
                        targetX = rightLimit;
                    }
                    mContentView.setTranslationX(targetX);
                }
                if (dy != 0) {
                    float targetY = mContentView.getTranslationY() + dy;
                    if (targetY < 0) {
                        targetY = 0;
                    }
                    int bottomLimit = mParentHeight - mContentView.getHeight();
                    if (targetY > bottomLimit) {
                        targetY = bottomLimit;
                    }
                    mContentView.setTranslationY(targetY);
                }
        }
        mLastX = x;
        mLastY = y;
        return true;
    }

    /**
     * 播放背景音乐
     */
    public void play(String musicId) {
        if (TextUtils.isEmpty(musicId)) {
            return;
        }
        if (mLiveMusicPlayer != null) {
            mLiveMusicPlayer.play(musicId);
        }
    }

//    @Override
//    public void onPause() {
//        mPaused = true;
//        if (mILivePushViewHolder != null) {
//            mILivePushViewHolder.pauseBgm();
//        }
//        if (mHandler != null) {
//            mHandler.removeCallbacksAndMessages(null);
//        }
//        if (mLiveMusicPlayer != null) {
//            mLiveMusicPlayer.pause();
//        }
//    }
//
//    @Override
//    public void onResume() {
//        if (mPaused) {
//            mPaused = false;
//            if (mILivePushViewHolder != null) {
//                mILivePushViewHolder.resumeBgm();
//            }
//            if (mLiveMusicPlayer != null) {
//                mLiveMusicPlayer.resume();
//            }
//            if (mHandler != null) {
//                mHandler.sendEmptyMessageDelayed(0, 1000);
//            }
//        }
//    }


    @Override
    public void release() {
        mHandler.removeCallbacksAndMessages(null);
        if (mLiveMusicPlayer != null) {
            mLiveMusicPlayer.release();
        }
        mLiveMusicPlayer = null;
        removeFromParent();
        if (mILivePushViewHolder != null) {
            mILivePushViewHolder.stopBgm();
        }
        mILivePushViewHolder = null;
    }
    
}
