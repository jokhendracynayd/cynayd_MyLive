package com.livestreaming.video.custom;

import android.view.MotionEvent;
import android.view.View;

/**
 * Created by vinsonswang on 2017/11/7.
 */

public class ViewTouchProcess implements View.OnTouchListener{
    private final String TAG = "RangeSliderChildViewTouchProcess";
    private View mView;
    private float mStartX;
    private OnPositionChangedListener mOnPositionChangedListener;

    public interface OnPositionChangedListener{
        void onPostionChanged(float distance);
        void onChangeComplete();
    }

    public ViewTouchProcess(View view){
        mView = view;
        mView.setOnTouchListener(this);
    }

    public void setOnPositionChangedListener(OnPositionChangedListener onPositionChangedListener){
        mOnPositionChangedListener = onPositionChangedListener;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        int eventId = motionEvent.getAction();
        switch (eventId){
            case MotionEvent.ACTION_DOWN:
                mStartX = motionEvent.getRawX();
                break;
            case MotionEvent.ACTION_MOVE:
                float dx = motionEvent.getRawX() - mStartX;
                mStartX = motionEvent.getRawX();
                if(mOnPositionChangedListener != null){
                    mOnPositionChangedListener.onPostionChanged(dx);
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if(mOnPositionChangedListener != null){
                    mOnPositionChangedListener.onChangeComplete();
                }
                mStartX = 0;
                break;
            default:
                mStartX = 0;
        }
        return true;
    }
}
