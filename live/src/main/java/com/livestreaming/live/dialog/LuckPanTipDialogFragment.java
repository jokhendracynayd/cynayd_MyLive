package com.livestreaming.live.dialog;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.livestreaming.common.CommonAppConfig;
import com.livestreaming.common.HtmlConfig;
import com.livestreaming.common.dialog.AbsDialogFragment;
import com.livestreaming.common.utils.DpUtil;
import com.livestreaming.live.R;

/**
 * Created by cxf on 2019/8/27.
 * 转盘规则说明
 */

public class LuckPanTipDialogFragment extends AbsDialogFragment implements View.OnClickListener {
    private WebView mWebView;

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_luck_pan_tip;
    }

    @Override
    protected int getDialogStyle() {
        return com.livestreaming.common.R.style.dialog;
    }

    @Override
    protected boolean canCancel() {
        return true;
    }

    @Override
    protected void setWindowAttributes(Window window) {
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = DpUtil.dp2px(300);
        params.height = DpUtil.dp2px(320);
        params.gravity = Gravity.CENTER;
        window.setAttributes(params);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        findViewById(R.id.btn_confirm).setOnClickListener(this);
        ViewGroup container = findViewById(R.id.container);

        mWebView = new WebView(mContext);
        mWebView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mWebView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        container.addView(mWebView);
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        mWebView.loadUrl(CommonAppConfig.getHtmlUrl(HtmlConfig.GAME_RULE));
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_confirm) {
            dismiss();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mWebView != null) {
            ViewGroup parent = (ViewGroup) mWebView.getParent();
            if (parent != null) {
                parent.removeView(mWebView);
            }
            mWebView.destroy();
        }
        super.onDestroy();
    }
}
