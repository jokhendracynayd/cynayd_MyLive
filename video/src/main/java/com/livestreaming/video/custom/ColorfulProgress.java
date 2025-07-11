package com.livestreaming.video.custom;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vinsonswang on 2017/11/9.
 */

public class ColorfulProgress extends View {

    private Paint mPaint;
    private Paint mColorPaint;

    private RectF mViewRectf;
    private RectF mColorRectf;
    private float mWidth;
    private float mHeight;

    private List<MarkInfo> mMarkInfoList;
    private float mCurPositioin;

    private VideoProgressController mVideoProgressController;

    public ColorfulProgress(Context context) {
        super(context);
        init();
    }


    public ColorfulProgress(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ColorfulProgress(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        mPaint = new Paint();
        mColorPaint = new Paint();
        mViewRectf = new RectF();
        mColorRectf = new RectF();

        mPaint.setAntiAlias(true);
        mColorPaint.setAntiAlias(true);

        mPaint.setColor(0x00000000);

        mMarkInfoList = new ArrayList<>();
    }

    public void setVideoProgressController(VideoProgressController videoProgressController){
        mVideoProgressController = videoProgressController;
    }

    public void setWidthHeight(float width, float height){
        mViewRectf.left = 0;
        mViewRectf.top = 0;
        mViewRectf.right = width;
        mViewRectf.bottom = height;

        mWidth = width;
        mHeight = height;

        invalidate();
    }

    /**
     *
     * @param curPosition
     */
    public void setCurPosition(float curPosition) {
        mCurPositioin = curPosition;
//        mCurTimeUs = mVideoProgressController.distance2Duration(mCurPositioin);
        invalidate();
    }

    /**
     * 开始标记
     *
     * @param color
     */
    public void startMark(int color) {
        MarkInfo info = new MarkInfo();
        info.startTimeMs = mVideoProgressController.getCurrentTimeMs();
        info.left = mCurPositioin;
        info.color = color;
        mMarkInfoList.add(info);
    }

    /**
     * 结束标记
     */
    public void endMark() {
        MarkInfo info = mMarkInfoList.get(mMarkInfoList.size() - 1);
        info.right = mCurPositioin;
    }

    public MarkInfo deleteLastMark() {
        MarkInfo info = null;
        if (mMarkInfoList != null && mMarkInfoList.size() != 0) {
            info = mMarkInfoList.remove(mMarkInfoList.size() - 1);
            invalidate();
        }
        return info;
    }

    public int getMarkListSize() {
        return mMarkInfoList.size();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawView(canvas);

        drawMarkInfo(canvas);
    }

    private void drawMarkInfo(Canvas canvas) {
        for (MarkInfo info : mMarkInfoList) {
            mColorPaint.setColor(info.color);
            mColorRectf.left = info.left;
            mColorRectf.top = 0;
            mColorRectf.bottom = mHeight;
            mColorRectf.right = info.right == -1 ? mCurPositioin : info.right;

            if (mColorRectf.left > mColorRectf.right) {
                float tmp = mColorRectf.left;
                mColorRectf.left = mColorRectf.right;
                mColorRectf.right = tmp;
            }

            canvas.drawRect(mColorRectf, mColorPaint);
        }
    }

    private void drawView(Canvas canvas){
        canvas.drawRect(mViewRectf, mPaint);
    }

    public class MarkInfo {
        public int color;
        public long startTimeMs;
        public float left = -1;
        private float right = -1;
    }

}
