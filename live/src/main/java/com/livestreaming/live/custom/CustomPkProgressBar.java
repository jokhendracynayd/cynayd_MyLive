package com.livestreaming.live.custom;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.livestreaming.live.R;
import com.opensource.svgaplayer.SVGAImageView;
import com.opensource.svgaplayer.SVGAParser;
import com.opensource.svgaplayer.SVGAVideoEntity;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class CustomPkProgressBar extends FrameLayout {

    private Paint mPaint1;
    private Paint mPaint2;
    private Paint mPaintStroke1;
    private Paint mPaintStroke2;
    private int mMinWidth;
    private float mRate;
    private int mLeftColor;
    private int mRightColor;
    private int mLeftStrokeColor;
    private int mRightStrokeColor;
    private Rect mRect1;
    private Rect mRect2;
    private int mWidth;
    private float mScale;
    private SVGAImageView mSVGAImageView;

    public CustomPkProgressBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mScale = context.getResources().getDisplayMetrics().density;
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.PkProgressBar);
        mMinWidth = (int) ta.getDimension(R.styleable.PkProgressBar_ppb_minWidth, 0);
        mRate = ta.getFloat(R.styleable.PkProgressBar_ppb_rate, 0.5f);
        mLeftColor = ta.getColor(R.styleable.PkProgressBar_ppb_left_color, 0);
        mRightColor = ta.getColor(R.styleable.PkProgressBar_ppb_right_color, 0);
        mLeftStrokeColor = ta.getColor(R.styleable.PkProgressBar_ppb_left_color_stroke, 0);
        mRightStrokeColor = ta.getColor(R.styleable.PkProgressBar_ppb_right_color_stroke, 0);
        ta.recycle();
        init();
    }

    private void init() {
        // Initialize paints and rectangles
        mPaint1 = new Paint();
        mPaint1.setAntiAlias(true);
        mPaint1.setStyle(Paint.Style.FILL);
        mPaint1.setColor(mLeftColor);

        mPaintStroke1 = new Paint();
        mPaintStroke1.setAntiAlias(true);
        mPaintStroke1.setStyle(Paint.Style.STROKE);
        mPaintStroke1.setStrokeWidth(dp2px(1));
        mPaintStroke1.setColor(mLeftStrokeColor);

        mPaint2 = new Paint();
        mPaint2.setAntiAlias(true);
        mPaint2.setStyle(Paint.Style.FILL);
        mPaint2.setColor(mRightColor);

        mPaintStroke2 = new Paint();
        mPaintStroke2.setAntiAlias(true);
        mPaintStroke2.setStyle(Paint.Style.STROKE);
        mPaintStroke2.setStrokeWidth(dp2px(1));
        mPaintStroke2.setColor(mRightStrokeColor);

        mRect1 = new Rect();
        mRect2 = new Rect();

        // Initialize SVGAImageView
        mSVGAImageView = new SVGAImageView(getContext());
        mSVGAImageView.setLayoutParams(new LayoutParams(dp2px(30), dp2px(30)));
        addView(mSVGAImageView);
        mSVGAImageView.bringToFront();
        loadSVGAAnimationFromAssets("pk_line.svga");

    }

    private void loadSVGAAnimationFromAssets(String fileName) {
        // Initialize SVGAParser to load and decode SVGA animation
        SVGAParser parser = new SVGAParser(getContext());

        try {
            // Open the SVGA file from the assets folder
            InputStream inputStream = getContext().getAssets().open(fileName);

            // Decode the SVGA file and set it to the SVGAImageView
            parser.decodeFromInputStream(inputStream, "", new SVGAParser.ParseCompletion() {
                @Override
                public void onComplete(@NonNull SVGAVideoEntity svgaVideoEntity) {
                    mSVGAImageView.setVideoItem(svgaVideoEntity);
                    // Start the animation
                    mSVGAImageView.startAnimation();
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
    private int dp2px(int dpVal) {
        return (int) (dpVal * mScale + 0.5f);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mWidth = w;
        mRect1.left = 0;
        mRect1.top = 0;
        mRect1.bottom = h;
        mRect2.top = 0;
        mRect2.right = w;
        mRect2.bottom = h;
        changeProgress();
        updateSVGAImageViewPosition();
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        // Draw progress rectangles
        canvas.drawRect(mRect1, mPaint1);
        canvas.drawRect(mRect1, mPaintStroke1);
        canvas.drawRect(mRect2, mPaint2);
        canvas.drawRect(mRect2, mPaintStroke2);

        // Call super to draw child views
        super.dispatchDraw(canvas);
    }

    public void setProgress(float rate) {
        if (mRate == rate) {
            return;
        }
        mRate = rate;
        changeProgress();
        invalidate();
        updateSVGAImageViewPosition();
    }

    private void changeProgress() {
        int bound = (int) (mWidth * mRate);
        if (bound < mMinWidth) {
            bound = mMinWidth;
        }
        if (bound > mWidth - mMinWidth) {
            bound = mWidth - mMinWidth;
        }
        mRect1.right = bound;
        mRect2.left = bound;
    }

    private void updateSVGAImageViewPosition() {
        int centerX;
        if (mRect1.right > mRect2.left) {
            centerX = mRect1.right;
        } else {
            centerX = mRect2.left;
        }
        int centerY = getHeight() / 2;

        // Center the SVGAImageView
        mSVGAImageView.setX(centerX - mSVGAImageView.getWidth() / 2);
        mSVGAImageView.setY(centerY - mSVGAImageView.getHeight() / 2);
    }
}
