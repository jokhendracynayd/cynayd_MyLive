package com.livestreaming.im.utils;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.text.TextUtils;

/**
 * Created by cxf on 2018/7/19.
 * 播放语音消息的工具类
 */

public class VoiceMediaPlayerUtil {

    private MediaPlayer mPlayer;
    private boolean mStarted;
    private boolean mPaused;
    private boolean mDestroy;
    private String mCurPath;
    private ActionListener mActionListener;

    private MediaPlayer.OnPreparedListener mOnPreparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
            if (mDestroy) {
                destroy();
            } else {
                mPlayer.start();
                mStarted = true;
            }
        }
    };

    private MediaPlayer.OnCompletionListener mOnCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            mStarted = false;
            mCurPath = null;
            if (mActionListener != null) {
                mActionListener.onPlayEnd();
            }
        }
    };

    private MediaPlayer.OnErrorListener mOnErrorListener = new MediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            mStarted = false;
            mCurPath = null;
            if (mActionListener != null) {
                mActionListener.onPlayEnd();
            }
            return false;
        }
    };


    public VoiceMediaPlayerUtil(Context context) {
        mPlayer = new MediaPlayer();
        mPlayer.setOnPreparedListener(mOnPreparedListener);
        mPlayer.setOnErrorListener(mOnErrorListener);
        mPlayer.setOnCompletionListener(mOnCompletionListener);
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    }

    public void startPlay(String path) {
        if (TextUtils.isEmpty(path)) {
            return;
        }
        if (!mStarted) {
            mCurPath = path;
            try {
                mPlayer.reset();
                mPlayer.setDataSource(path);
                mPlayer.setLooping(false);
                mPlayer.setVolume(1f, 1f);
                mPlayer.prepare();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            if (!path.equals(mCurPath)) {
                mCurPath = path;
                mStarted = false;
                try {
                    mPlayer.stop();
                    mPlayer.reset();
                    mPlayer.setDataSource(path);
                    mPlayer.setLooping(false);
                    mPlayer.setVolume(1f, 1f);
                    mPlayer.prepare();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void pausePlay() {
        if (mPlayer != null && mStarted && !mDestroy) {
            mPlayer.pause();
            mPaused = true;
        }
    }

    public void resumePlay() {
        if (mPlayer != null && mStarted && !mDestroy && mPaused) {
            mPaused = false;
            mPlayer.start();
        }
    }

    public void stopPlay() {
        if (mPlayer != null && mStarted) {
            mPlayer.stop();
        }
        mStarted = false;
        mCurPath = null;
    }

    public void destroy() {
        stopPlay();
        if (mPlayer != null) {
            mPlayer.release();
        }
        mActionListener = null;
        mDestroy = true;
    }

    public interface ActionListener {

        void onPlayEnd();
    }

    public void setActionListener(ActionListener actionListener) {
        mActionListener = actionListener;
    }
}
