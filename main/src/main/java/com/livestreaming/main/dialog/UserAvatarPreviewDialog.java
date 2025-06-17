package com.livestreaming.main.dialog;

import static com.livestreaming.common.glide.ImgLoader.loadSvga;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;

import com.livestreaming.common.Constants;
import com.livestreaming.main.R;
import com.livestreaming.common.dialog.AbsDialogFragment;
import com.livestreaming.common.glide.ImgLoader;

/**
 * Created by cxf on 2018/11/28.
 * 用户头像图片预览弹窗
 */

public class UserAvatarPreviewDialog extends AbsDialogFragment implements View.OnClickListener {

    private View mBg;
    private ImageView mImageView;
    private ValueAnimator mAnimator;

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_user_avatar_preview;
    }

    @Override
    protected int getDialogStyle() {
        return com.livestreaming.common.R.style.dialog;
    }

    @Override
    protected boolean canCancel() {
        return true;
    }

    @SuppressLint("WrongConstant")
    @Override
    protected void setWindowAttributes(Window window) {
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE /*| View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR*/);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        WindowManager.LayoutParams params = window.getAttributes();
        if (Build.VERSION.SDK_INT >= 30) {
            params.layoutInDisplayCutoutMode = 3;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            params.layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(params);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mBg = mRootView.findViewById(R.id.bg);
        findViewById(R.id.btn_close).setOnClickListener(this);
        mImageView = findViewById(R.id.img);
        mAnimator = ValueAnimator.ofFloat(0, 1);
        mAnimator.setDuration(150);
        mAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float v = (float) animation.getAnimatedValue();
                mBg.setAlpha(v);
            }
        });
        mAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                Bundle bundle = getArguments();
                if (bundle != null) {
                    String url = bundle.getString(Constants.URL, "");
                    ImgLoader.display(mContext, url, mImageView);
                }
            }
        });
        mAnimator.start();
    }


    @Override
    public void onDestroy() {
        if (mAnimator != null) {
            mAnimator.cancel();
        }
        mContext = null;
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_close) {
            dismiss();
        }
    }


}
