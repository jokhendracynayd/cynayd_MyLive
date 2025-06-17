package com.livestreaming.live.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.livestreaming.common.utils.DpUtil;
import com.livestreaming.common.utils.ScreenDimenUtil;
import com.livestreaming.common.views.AbsViewHolder;
import com.livestreaming.live.R;
import com.livestreaming.live.custom.PkProgressBar;
import com.livestreaming.live.custom.ProgressTextView;
import com.opensource.svgaplayer.SVGAImageView;
import com.opensource.svgaplayer.SVGAParser;
import com.opensource.svgaplayer.SVGAVideoEntity;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Created by cxf on 2018/11/17.
 * 主播连麦pk相关逻辑
 */

public class LiveLinkMicPkViewHolder extends AbsViewHolder {

    // FrameImageView mFrameImageView;
    private PkProgressBar mPkProgressBar;
    private TextView mLeft;
    private TextView mRight;
    private int mHalfScreenWidth;
    private ValueAnimator mAnimator1;
    private ValueAnimator mAnimator2;
    private TextView mTime;
    private SVGAImageView mResultImageView;
    private SVGAImageView svgaLineImageView;
    private SVGAImageView result_lose;
    private ValueAnimator mEndAnimator1;
    private ScaleAnimation mEndAnim2;
    private ScaleAnimation mEndAnim22;
    private ValueAnimator mEndAnimator3;
    private ValueAnimator mEndAnimator4;
    private int mOffsetX;
    private int mOffsetY;
    private ProgressTextView mPkWaitProgress;
    private long mRightVal = 0;
    private long mLeftVal = 0;
    private FrameLayout leftLayout;
    private FrameLayout rightLayout;


    Animation breathAnimation;
    private LinearLayout time_layout;
    private SVGAImageView mStartmageView;
    private SVGAImageView mSDrawImageView;

    public LiveLinkMicPkViewHolder(Context context, ViewGroup parentView) {
        super(context, parentView);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_link_mic_pk;
    }

    @Override
    public void init() {
        mHalfScreenWidth = ScreenDimenUtil.getInstance().getScreenWidth() / 2;
        // mFrameImageView = (FrameImageView) findViewById(R.id.frame_img);
        //mFrameImageView.setImageList(LiveIconUtil.getLinkMicPkAnim());
        mPkProgressBar = (PkProgressBar) findViewById(R.id.progressbar);
        mLeft = (TextView) findViewById(R.id.left);
        leftLayout = (FrameLayout) findViewById(R.id.left_layout);
        mRight = (TextView) findViewById(R.id.right);
        rightLayout = (FrameLayout) findViewById(R.id.right_layout);
        mLeft.setText("  0");
        mRight.setText("0  ");
        leftLayout.setTranslationX(-mHalfScreenWidth);
        breathAnimation = AnimationUtils.loadAnimation(mContext, R.anim.breath_increase);

        // Start the animation
        rightLayout.setTranslationX(mHalfScreenWidth);
        mAnimator1 = ValueAnimator.ofFloat(0, mHalfScreenWidth);
        mAnimator1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float v = (float) animation.getAnimatedValue();
                leftLayout.setTranslationX(v - mHalfScreenWidth);
                rightLayout.setTranslationX(mHalfScreenWidth - v);
            }
        });
        mAnimator1.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (mPkProgressBar != null && mPkProgressBar.getVisibility() != View.VISIBLE) {
                    mPkProgressBar.setVisibility(View.VISIBLE);
                    svgaLineImageView.setVisibility(View.VISIBLE);

                }
                if (leftLayout != null) {
                    leftLayout.setBackground(null);
                }
                if (rightLayout != null) {
                    rightLayout.setBackground(null);
                }
            }
        });
        mAnimator1.setInterpolator(new AccelerateDecelerateInterpolator());
        mAnimator1.setDuration(400);
        mAnimator2 = ValueAnimator.ofFloat(0, 18);
        mAnimator2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {

            }
        });
        mAnimator2.setDuration(800);
        mAnimator2.setInterpolator(new LinearInterpolator());
        mTime = (TextView) findViewById(R.id.time);
        time_layout = (LinearLayout) findViewById(R.id.time_layout);
        mResultImageView = findViewById(R.id.result);
        mStartmageView = findViewById(R.id.mStartmageView);
        mSDrawImageView = findViewById(R.id.mSDrawImageView);
        svgaLineImageView = findViewById(R.id.svgaImageView);
        // Position the SVGAImageView dynamically
        mPkProgressBar.setOnProgressChangeListener((right) -> {
            svgaLineImageView.setX(right - 30);
        });
        loadSVGALineAnimationFromAssets("pk_line.svga");
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(mStartmageView, "scaleX", 1.0f, 12f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(mStartmageView, "scaleY", 1.0f, 12f);
        scaleX.setDuration(200);
        scaleY.setDuration(200);
        scaleX.start();
        scaleY.start();
        ObjectAnimator scaleX2 = ObjectAnimator.ofFloat(mSDrawImageView, "scaleX", 1.0f, 3f);
        ObjectAnimator scaleY2 = ObjectAnimator.ofFloat(mSDrawImageView, "scaleY", 1.0f, 3f);
        scaleX2.setDuration(200);
        scaleY2.setDuration(200);
        scaleX2.start();
        scaleY2.start();

        ObjectAnimator scaleX3 = ObjectAnimator.ofFloat(svgaLineImageView, "scaleX", 1.0f, 4f);
        ObjectAnimator scaleY3 = ObjectAnimator.ofFloat(svgaLineImageView, "scaleY", 1.0f, 4f);
        scaleX3.setDuration(200);
        scaleY3.setDuration(200);
        scaleX3.start();
        scaleY3.start();
        result_lose = findViewById(R.id.result_lose);
        mOffsetX = DpUtil.dp2px(75) / 2;
        mOffsetY = DpUtil.dp2px(50) / 2;
        mPkWaitProgress = (ProgressTextView) findViewById(R.id.pk_wait_progress);
    }

    public void startAnim() {
        if (mAnimator1 != null) {
            mAnimator1.start();
        }

        loadSVGAAnimationFromAssetsOnce("pk_vs_start.svga");
        if (mAnimator2 != null) {
            mAnimator2.start();
        }
    }

    public void showTime() {
        if (time_layout != null && time_layout.getVisibility() != View.VISIBLE) {
            time_layout.setVisibility(View.VISIBLE);
        }
    }

    public void hideTime() {
        if (time_layout != null && time_layout.getVisibility() == View.VISIBLE) {
            time_layout.setVisibility(View.INVISIBLE);
            if (mStartmageView != null) {
                mStartmageView.stopAnimation();
                mStartmageView.clearAnimation();
                mStartmageView.setVisibility(View.GONE);
                mStartmageView = null;
                mStartmageView = findViewById(R.id.mStartmageView);

            }
            if (mSDrawImageView != null) {
                mSDrawImageView.stopAnimation();
                mSDrawImageView.clearAnimation();
                mSDrawImageView.setVisibility(View.GONE);
                mSDrawImageView = null;
                mSDrawImageView = findViewById(R.id.mSDrawImageView);
            }
            if (result_lose != null) {
                result_lose.stopAnimation();
                result_lose.clearAnimation();

                result_lose.setVisibility(View.GONE);
                result_lose = null;
                result_lose = findViewById(R.id.result_lose);
            }
            if (mResultImageView != null) {
                mResultImageView.stopAnimation();
                mResultImageView.clearAnimation();

                mResultImageView.setVisibility(View.GONE);
                mResultImageView = null;
                mResultImageView = findViewById(R.id.result);
            }
        }
    }

    public void setTime(String content) {
        if (mTime != null) {
            mTime.setText(content);
        }
    }

    public void onEnterRoomPkStart() {
//        if (mFrameImageView != null) {
//            mFrameImageView.setImageResource(R.drawable.pk19);
//        }
        if (mPkProgressBar != null && mPkProgressBar.getVisibility() != View.VISIBLE) {
            mPkProgressBar.setVisibility(View.VISIBLE);
            svgaLineImageView.setVisibility(View.VISIBLE);
        }
        if (leftLayout != null) {
            leftLayout.setBackground(null);
            leftLayout.setTranslationX(0);
        }
        if (rightLayout != null) {
            rightLayout.setBackground(null);
            rightLayout.setTranslationX(0);
        }
    }

    public void onEnterRoomPkEnd() {
//        if (mFrameImageView != null) {
//            mFrameImageView.setImageResource(R.drawable.pk19);
//        }
        if (mPkProgressBar != null && mPkProgressBar.getVisibility() != View.VISIBLE) {
            mPkProgressBar.setVisibility(View.VISIBLE);
            svgaLineImageView.setVisibility(View.VISIBLE);
        }
        if (leftLayout != null) {
            leftLayout.setBackground(null);
            leftLayout.setTranslationX(0);
        }
        if (rightLayout != null) {
            rightLayout.setBackground(null);
            rightLayout.setTranslationX(0);
        }
    }

    public void onProgressChanged(long leftGift, long rightGift) {
        if (mPkProgressBar != null && mPkProgressBar.getVisibility() != View.VISIBLE) {
            mPkProgressBar.setVisibility(View.VISIBLE);
            svgaLineImageView.setVisibility(View.VISIBLE);
        }
        if (leftLayout != null) {
            leftLayout.setBackground(null);
            leftLayout.setTranslationX(0);
        }
        if (rightLayout != null) {
            rightLayout.setBackground(null);
            rightLayout.setTranslationX(0);
        }
        mLeft.setVisibility(View.VISIBLE);
        mRight.setVisibility(View.VISIBLE);
        mLeft.setText("  " + leftGift);
        mRight.setText("  " + rightGift);
        if (mLeftVal != leftGift) {
            mLeft.startAnimation(breathAnimation);
            mLeftVal = leftGift;
        }
        if (mRightVal != rightGift) {
            mRight.startAnimation(breathAnimation);
            mRightVal = rightGift;
        }
        if (leftGift == rightGift) {
            mPkProgressBar.setProgress(0.5f);

        } else {
            mPkProgressBar.setProgress(leftGift * 1f / (leftGift + rightGift));
        }
    }


    private ValueAnimator getEndValueAnimator(int result) {
        ValueAnimator valueAnimator = null;
        if (result == 0) {
            valueAnimator = ObjectAnimator.ofFloat(mResultImageView, "translationY", DpUtil.dp2px(80));
        } else {
            int width = mContentView.getWidth();
            int height = mContentView.getHeight();
            Path path = new Path();
            path.lineTo(0, 0);
            path.moveTo(width / 2, height / 2);
            path.arcTo(new RectF(width / 2 - height / 2, height / 2, width / 2 + height / 2, height / 2 + height), -90, result > 0 ? -70 : 70);
            final PathMeasure pathMeasure = new PathMeasure(path, false);
            final float[] position = new float[2];
            valueAnimator = ValueAnimator.ofFloat(0, pathMeasure.getLength());
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float v = (float) animation.getAnimatedValue();
                    pathMeasure.getPosTan(v, position, null);
                    mResultImageView.setX(position[0] - mOffsetX);
                    mResultImageView.setY(position[1] - mOffsetY);
                }
            });
        }
        valueAnimator.setDuration(1500);
        return valueAnimator;
    }

    private ValueAnimator getEndValueAnimator2(int result) {
        ValueAnimator valueAnimator = null;
        if (result == 0) {
            valueAnimator = ObjectAnimator.ofFloat(mResultImageView, "translationY", DpUtil.dp2px(80));
        } else {
            int width = mContentView.getWidth();
            int height = mContentView.getHeight();
            Path path = new Path();
            path.lineTo(0, 0);
            path.moveTo(width / 2, height / 2);
            path.arcTo(new RectF(width / 2 - height / 2, height / 2, width / 2 + height / 2, height / 2 + height), -90, result > 0 ? 70 : -70);
            final PathMeasure pathMeasure = new PathMeasure(path, false);
            final float[] position = new float[2];
            valueAnimator = ValueAnimator.ofFloat(0, pathMeasure.getLength());
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float v = (float) animation.getAnimatedValue();
                    pathMeasure.getPosTan(v, position, null);
                    result_lose.setX(position[0] - mOffsetX);
                    result_lose.setY(position[1] - mOffsetY);
                }
            });
        }
        valueAnimator.setDuration(1500);
        return valueAnimator;
    }


    /**
     * pk结束
     *
     * @param result -1自己的主播输 0平  1自己的主播赢
     */
    public void end(final int result) {
        if (mResultImageView == null) {
            return;
        }
        mEndAnimator1 = ValueAnimator.ofFloat(1, 0.2f);
        mEndAnimator1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
//                float v = (float) animation.getAnimatedValue();
//                mFrameImageView.setScaleX(v);
//                mFrameImageView.setScaleY(v);
//                mFrameImageView.setAlpha(v);
            }
        });
        mEndAnimator1.setDuration(500);
        mEndAnimator1.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (mResultImageView != null) {
//                    if (mFrameImageView.getVisibility() == View.VISIBLE) {
//                        mFrameImageView.setVisibility(View.INVISIBLE);
//                    }
                    if (result == 0) {
                        loadSVGAAnimationFromAssetsOnceDraw("pk_draw.svga");
                    } else {
                        loadSVGAAnimationFromAssets("pk_win.svga");
                        loadSVGAAnimationLoseFromAssets("pk_lose.svga");
                    }
                    mResultImageView.startAnimation(mEndAnim2);
                    result_lose.startAnimation(mEndAnim22);
                }
            }
        });
        mEndAnim2 = new ScaleAnimation(0.2f, 1, 0.2f, 1, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mEndAnim22 = new ScaleAnimation(0.2f, 1, 0.2f, 1, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mEndAnim2.setDuration(500);
        mEndAnim22.setDuration(500);
        mEndAnim2.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mEndAnimator3 = getEndValueAnimator(result);
                mEndAnimator3.start();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        mEndAnim22.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mEndAnimator4 = getEndValueAnimator2(result);
                mEndAnimator4.start();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mEndAnimator1.start();
    }

    private void loadSVGAAnimationFromAssets(String fileName) {
        // Initialize SVGAParser to load and decode SVGA animation
        SVGAParser parser = new SVGAParser(mContext);

        try {
            // Open the SVGA file from the assets folder
            InputStream inputStream = mContext.getAssets().open(fileName);

            // Decode the SVGA file and set it to the SVGAImageView
            parser.decodeFromInputStream(inputStream, "", new SVGAParser.ParseCompletion() {
                @Override
                public void onComplete(@NonNull SVGAVideoEntity svgaVideoEntity) {
                    mResultImageView.setVideoItem(svgaVideoEntity);
                    // Start the animation
                    mResultImageView.startAnimation();
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

    private void loadSVGALineAnimationFromAssets(String fileName) {
        // Initialize SVGAParser to load and decode SVGA animation
        SVGAParser parser = new SVGAParser(mContext);

        try {
            // Open the SVGA file from the assets folder
            InputStream inputStream = mContext.getAssets().open(fileName);

            // Decode the SVGA file and set it to the SVGAImageView
            parser.decodeFromInputStream(inputStream, "", new SVGAParser.ParseCompletion() {
                @Override
                public void onComplete(@NonNull SVGAVideoEntity svgaVideoEntity) {
                    svgaLineImageView.setVideoItem(svgaVideoEntity);
                    // Start the animation
                    svgaLineImageView.startAnimation();
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

    private void loadSVGAAnimationFromAssetsOnce(String fileName) {
        // Initialize SVGAParser to load and decode SVGA animation
        SVGAParser parser = new SVGAParser(mContext);

        try {
            // Open the SVGA file from the assets folder
            InputStream inputStream = mContext.getAssets().open(fileName);

            // Decode the SVGA file and set it to the SVGAImageView
            parser.decodeFromInputStream(inputStream, "", new SVGAParser.ParseCompletion() {
                @Override
                public void onComplete(@NonNull SVGAVideoEntity svgaVideoEntity) {

                    mStartmageView.setVideoItem(svgaVideoEntity);
                    mStartmageView.setLoops(1);
                    // Start the animation
                    mStartmageView.startAnimation();

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

    private void loadSVGAAnimationFromAssetsOnceDraw(String fileName) {
        // Initialize SVGAParser to load and decode SVGA animation
        SVGAParser parser = new SVGAParser(mContext);

        try {
            // Open the SVGA file from the assets folder
            InputStream inputStream = mContext.getAssets().open(fileName);

            // Decode the SVGA file and set it to the SVGAImageView
            parser.decodeFromInputStream(inputStream, "", new SVGAParser.ParseCompletion() {
                @Override
                public void onComplete(@NonNull SVGAVideoEntity svgaVideoEntity) {

                    mSDrawImageView.setVideoItem(svgaVideoEntity);
                    mSDrawImageView.setLoops(1);
                    // Start the animation
                    mSDrawImageView.startAnimation();
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

    private void loadSVGAAnimationLoseFromAssets(String fileName) {
        // Initialize SVGAParser to load and decode SVGA animation
        SVGAParser parser = new SVGAParser(mContext);

        try {
            // Open the SVGA file from the assets folder
            InputStream inputStream = mContext.getAssets().open(fileName);

            // Decode the SVGA file and set it to the SVGAImageView
            parser.decodeFromInputStream(inputStream, "", new SVGAParser.ParseCompletion() {
                @Override
                public void onComplete(@NonNull SVGAVideoEntity svgaVideoEntity) {
                    result_lose.setVideoItem(svgaVideoEntity);
                    // Start the animation
                    result_lose.startAnimation();
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

    public void release() {
        if (mAnimator1 != null) {
            mAnimator1.cancel();
        }
        if (mAnimator2 != null) {
            mAnimator2.cancel();
        }
        if (mEndAnimator1 != null) {
            mEndAnimator1.cancel();
        }
        if (mEndAnim2 != null) {
            mEndAnim2.cancel();
        }
        if (mEndAnim22 != null) {
            mEndAnim22.cancel();
        }
//        if (mFrameImageView != null) {
//            mFrameImageView.clearAnimation();
//            mFrameImageView.release();
//        }
        if (mEndAnimator3 != null) {
            mEndAnimator3.cancel();
        }
        if (mEndAnimator4 != null) {
            mEndAnimator4.cancel();
        }
    }

    public void setPkWaitProgress(int progress) {
        if (mPkWaitProgress != null && mPkWaitProgress.getVisibility() == View.VISIBLE) {
            mPkWaitProgress.setProgress(progress);
        }
    }

    public void setPkWaitProgressVisible(boolean visible) {
        if (mPkWaitProgress != null) {
            if (visible) {
                if (mPkWaitProgress.getVisibility() != View.VISIBLE) {
                    mPkWaitProgress.setVisibility(View.VISIBLE);
                }
            } else {
                if (mPkWaitProgress.getVisibility() == View.VISIBLE) {
                    mPkWaitProgress.setVisibility(View.INVISIBLE);
                }
            }
        }
    }

}
