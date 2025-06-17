package com.livestreaming.video.views;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.RelativeLayout;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.video.VideoSize;
import com.livestreaming.common.utils.L;
import com.livestreaming.common.views.AbsViewHolder;
import com.livestreaming.video.R;
import com.livestreaming.video.bean.VideoBean;
import com.livestreaming.video.http.VideoHttpConsts;
import com.livestreaming.video.http.VideoHttpUtil;

/**
 * Created by cxf on 2018/11/30.
 * 视频播放器
 */

public class VideoPlayViewHolder extends AbsViewHolder implements View.OnClickListener {

    private PlayerView mTXCloudVideoView;
    private View mVideoCover;
    private ExoPlayer mPlayer;
    private boolean mPaused;//生命周期暂停
    private boolean mClickPaused;//点击暂停
    private ActionListener mActionListener;
    private View mPlayBtn;
    private ObjectAnimator mPlayBtnAnimator;//暂停按钮的动画
    private boolean mStartPlay;
    private boolean mEndPlay;
    private VideoBean mVideoBean;


    public VideoPlayViewHolder(Context context, ViewGroup parentView) {
        super(context, parentView);

    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_video_play;
    }

    @Override
    public void init() {
        mTXCloudVideoView = (PlayerView) findViewById(R.id.video_view);
        mPlayer = new ExoPlayer.Builder(mContext).build();
        mTXCloudVideoView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
        mPlayer.setRepeatMode(Player.REPEAT_MODE_ALL);
        mTXCloudVideoView.setPlayer(mPlayer);
//        TXVodPlayConfig playConfig = new TXVodPlayConfig();
//        playConfig.setProgressInterval(200);
//        playConfig.setHeaders(CommonAppConfig.HEADER);
//        mPlayer.setConfig(playConfig);
//        mPlayer.setRenderMode(TXLiveConstants.RENDER_MODE_FULL_FILL_SCREEN);
//        mPlayer.setLoop(true);
//        mPlayer.setAutoPlay(true);
//        mPlayer.setVodListener(this);
//        mPlayer.setPlayerView(mTXCloudVideoView);
        findViewById(R.id.root).setOnClickListener(this);
        mVideoCover = findViewById(R.id.video_cover);
        mPlayBtn = findViewById(R.id.btn_play);
        //暂停按钮动画
        mPlayBtnAnimator = ObjectAnimator.ofPropertyValuesHolder(mPlayBtn,
                PropertyValuesHolder.ofFloat("scaleX", 4f, 0.8f, 1f),
                PropertyValuesHolder.ofFloat("scaleY", 4f, 0.8f, 1f),
                PropertyValuesHolder.ofFloat("alpha", 0f, 1f));
        mPlayBtnAnimator.setDuration(150);
        mPlayBtnAnimator.setInterpolator(new AccelerateInterpolator());
        mPlayer.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                switch (playbackState) {
                    case Player.STATE_BUFFERING:
                        if (mActionListener != null) {
                            mActionListener.onPlayLoading();
                        }
                        break;

                    case Player.STATE_READY:
                        mStartPlay = true;
                        if (mActionListener != null) {
                            mActionListener.onPlayBegin();
                        }
                        break;
                    case Player.STATE_ENDED:
                        if (!mEndPlay) {
                            mEndPlay = true;
                            if (mVideoBean != null) {
                                VideoHttpUtil.videoWatchEnd(mVideoBean.getUid(), mVideoBean.getId());
                            }
                        }
                        break;

                }
            }

            @Override
            public void onRenderedFirstFrame() {
                // Equivalent to PLAY_EVT_RCV_FIRST_I_FRAME
                if (mActionListener != null) {
                    mActionListener.onFirstFrame();  // Your custom listener
                }

                if (mPaused && mPlayer != null) {
                    pausePlay();
                }
            }

            @Override
            public void onVideoSizeChanged(VideoSize videoSize) {
                // Equivalent to PLAY_EVT_CHANGE_RESOLUTION
                int width = videoSize.width;
                int height = videoSize.height;
                VideoPlayViewHolder.this.onVideoSizeChanged(width, height);
            }

        });
    }

    /**
     * 播放器事件回调
     */

    /**
     * 获取到视频宽高回调
     */
    public void onVideoSizeChanged(float videoWidth, float videoHeight) {
        if (mTXCloudVideoView != null && videoWidth > 0 && videoHeight > 0) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mTXCloudVideoView.getLayoutParams();
            int targetH = 0;
            if (videoWidth / videoHeight > 0.5625f) {//横屏 9:16=0.5625
                targetH = (int) (mTXCloudVideoView.getWidth() / videoWidth * videoHeight);
            } else {
                targetH = ViewGroup.LayoutParams.MATCH_PARENT;
            }
            if (targetH != params.height) {
                params.height = targetH;
                mTXCloudVideoView.requestLayout();
            }
            if (mVideoCover != null && mVideoCover.getVisibility() == View.VISIBLE) {
                mVideoCover.setVisibility(View.INVISIBLE);
            }
        }
    }

    /**
     * 开始播放
     */
    public void startPlay(VideoBean videoBean) {
        mStartPlay = false;
        mClickPaused = false;
        mEndPlay = false;
        mVideoBean = videoBean;
        if (mVideoCover != null && mVideoCover.getVisibility() != View.VISIBLE) {
            mVideoCover.setVisibility(View.VISIBLE);
        }
        hidePlayBtn();
        L.e("播放视频--->" + videoBean);
        if (videoBean == null) {
            return;
        }
        String url = videoBean.getHref();
        if (TextUtils.isEmpty(url)) {
            return;
        }
        if (mPlayer != null) {
            MediaItem mediaItem = MediaItem.fromUri(Uri.parse(url));
            mPlayer.setMediaItem(mediaItem);
            mPlayer.prepare();
            mPlayer.setPlayWhenReady(true);
        }
        VideoHttpUtil.videoWatchStart(videoBean.getUid(), videoBean.getId());
    }

    /**
     * 停止播放
     */
    public void stopPlay() {
        if (mPlayer != null) {
            mPlayer.stop();
        }
    }

    /**
     * 循环播放
     */
    private void replay() {
        if (mPlayer != null) {
            mPlayer.seekTo(0);
            resumePlayer();
        }
    }

    public void release() {
        VideoHttpUtil.cancel(VideoHttpConsts.VIDEO_WATCH_START);
        VideoHttpUtil.cancel(VideoHttpConsts.VIDEO_WATCH_END);
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.release();
        }
        mPlayer = null;
        mActionListener = null;
    }

    /**
     * 生命周期暂停
     */
    public void pausePlay() {
        mPaused = true;
        if (!mClickPaused && mPlayer != null) {
            pausePlayer();
        }
    }

    /**
     * 生命周期恢复
     */
    public void resumePlay() {
        if (mPaused) {
            if (!mClickPaused && mPlayer != null) {
               resumePlayer();
            }
        }
        mPaused = false;
    }

    /**
     * 显示开始播放按钮
     */
    private void showPlayBtn() {
        if (mPlayBtn != null && mPlayBtn.getVisibility() != View.VISIBLE) {
            mPlayBtn.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 隐藏开始播放按钮
     */
    private void hidePlayBtn() {
        if (mPlayBtn != null && mPlayBtn.getVisibility() == View.VISIBLE) {
            mPlayBtn.setVisibility(View.INVISIBLE);
        }
    }

    public void pausePlayer() {
        if (mPlayer != null) {
            mPlayer.setPlayWhenReady(false);  // Pause playback
            mPaused = true;
        }
    }

    // Method to resume playback
    public void resumePlayer() {
        if (mPlayer != null && mPaused) {
            mPlayer.setPlayWhenReady(true);  // Resume playback
            mPaused = false;
        }
    }
    /**
     * 点击切换播放和暂停
     */
    private void clickTogglePlay() {
        if (!mStartPlay) {
            return;
        }
        if (mPlayer != null) {
            if (mClickPaused) {
                resumePlayer();
            } else {
                pausePlay();
            }
        }
        mClickPaused = !mClickPaused;
        if (mClickPaused) {
            showPlayBtn();
            if (mPlayBtnAnimator != null) {
                mPlayBtnAnimator.start();
            }
        } else {
            hidePlayBtn();
        }
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.root) {
            clickTogglePlay();
        }
    }


    public interface ActionListener {
        void onPlayBegin();

        void onPlayLoading();

        void onFirstFrame();
    }


    public void setActionListener(ActionListener actionListener) {
        mActionListener = actionListener;
    }
}
