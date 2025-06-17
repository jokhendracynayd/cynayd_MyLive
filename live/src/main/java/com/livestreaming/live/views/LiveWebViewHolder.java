package com.livestreaming.live.views;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.livestreaming.common.CommonAppConfig;
import com.livestreaming.common.utils.L;
import com.livestreaming.common.views.AbsLivePageViewHolder;
import com.livestreaming.live.R;
import com.livestreaming.live.activity.LiveAudienceActivity;

/**
 * Created by cxf on 2018/10/15.
 * 直播间WebView
 */
public class LiveWebViewHolder extends AbsLivePageViewHolder implements View.OnClickListener {

    private ProgressBar mProgressBar;
    private WebView mWebView;
    private TextView mTitle;
    private String mUrl;

    public LiveWebViewHolder(Context context, ViewGroup parentView, String url) {
        super(context, parentView, url);
    }

    @Override
    protected void processArguments(Object... args) {
        mUrl = CommonAppConfig.getHtmlUrl((String) args[0]);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_live_web_view;
    }

    @Override
    public void init() {
        super.init();
        mTitle = (TextView) findViewById(R.id.title);
        mProgressBar = (ProgressBar) findViewById(R.id.progressbar);
        mWebView = new WebView(mContext);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mWebView.setLayoutParams(params);
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                L.e("H5-------->" + url);
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if (mTitle != null) {
                    mTitle.setText(view.getTitle());
                }
            }

        });
        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress >= 70) {
                    if (mProgressBar.getVisibility() == View.VISIBLE) {
                        mProgressBar.setVisibility(View.GONE);
                    }
                } else {
                    mProgressBar.setProgress(newProgress);
                }
            }
        });
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        ((LinearLayout) mContentView).addView(mWebView);
    }

    @Override
    public void loadData() {
        if (!mLoad) {
            mLoad = true;
            mWebView.loadUrl(mUrl);
        } else {
            mWebView.reload();
        }
    }

    @Override
    public void release() {
        super.release();
        if (mWebView != null) {
            ViewGroup parent = (ViewGroup) mWebView.getParent();
            if (parent != null) {
                parent.removeView(mWebView);
            }
            mWebView.destroy();
            mWebView = null;
        }
    }


    @Override
    public void onHide() {
        if (CommonAppConfig.LIVE_ROOM_SCROLL && mContext != null && mContext instanceof LiveAudienceActivity) {
            ((LiveAudienceActivity) mContext).setScrollFrozen(false);
        }
    }
}
