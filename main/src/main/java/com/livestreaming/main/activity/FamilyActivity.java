package com.livestreaming.main.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.livestreaming.common.CommonAppConfig;
import com.livestreaming.common.Constants;
import com.livestreaming.common.activity.AbsActivity;
import com.livestreaming.common.activity.WebViewActivity;
import com.livestreaming.common.interfaces.ActivityResultCallback;
import com.livestreaming.common.utils.ActivityResultUtil;
import com.livestreaming.common.utils.DpUtil;
import com.livestreaming.common.utils.L;
import com.livestreaming.common.utils.RouteUtil;
import com.livestreaming.main.R;

/**
 * Created by cxf on 2018/9/25.
 */

public class FamilyActivity extends AbsActivity {

    private ProgressBar mProgressBar;
    private WebView mWebView;
    //private View mBtnCreate;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_family;
    }

    @Override
    protected void main() {
//        mBtnCreate = findViewById(R.id.btn_create);
//        mBtnCreate.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                ActivityResultUtil.startActivityForResult(FamilyActivity.this, new Intent(mContext, FamilyApplyActivity.class), new ActivityResultCallback() {
//                    @Override
//                    public void onSuccess(Intent intent) {
//                        finish();
//                    }
//                });
//            }
//        });
        String url = CommonAppConfig.getHtmlUrl(getIntent().getStringExtra(Constants.URL));
        L.e("H5--->" + url);
//        if (!TextUtils.isEmpty(url) && url.contains("index")) {
//            if (mBtnCreate != null && mBtnCreate.getVisibility() != View.VISIBLE) {
//                mBtnCreate.setVisibility(View.VISIBLE);
//            }
//        }
        LinearLayout rootView = (LinearLayout) findViewById(R.id.rootView);
        mProgressBar = (ProgressBar) findViewById(R.id.progressbar);
        mWebView = new WebView(mContext);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.topMargin = DpUtil.dp2px(1);
        mWebView.setLayoutParams(params);
        mWebView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        rootView.addView(mWebView);
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                L.e("H5-------->" + url);
                if (url.startsWith("familyindex://")) {
                    url = url.substring("familyindex://".length());
                    url = url.replace("host", CommonAppConfig.HOST);
                    FamilyActivity.forward(mContext, url);
                    finish();
                } else {
                    view.loadUrl(url);
                }
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                setTitle(view.getTitle());
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                L.e("H5-----onPageStarted--->" + url);
//                if (!TextUtils.isEmpty(url) && url.contains("index")) {
//                    if (mBtnCreate != null && mBtnCreate.getVisibility() != View.VISIBLE) {
//                        mBtnCreate.setVisibility(View.VISIBLE);
//                    }
//                } else {
//                    if (mBtnCreate != null && mBtnCreate.getVisibility() == View.VISIBLE) {
//                        mBtnCreate.setVisibility(View.INVISIBLE);
//                    }
//                }
            }
        });
        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100) {
                    mProgressBar.setVisibility(View.GONE);
                } else {
                    mProgressBar.setProgress(newProgress);
                }
            }
        });
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        mWebView.addJavascriptInterface(new WebAppInterface(mContext), "Android");
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        mWebView.loadUrl(url);
    }


    protected boolean canGoBack() {
        return mWebView != null && mWebView.canGoBack();
    }

    @Override
    public void onBackPressed() {
        if (isNeedExitActivity()) {
            finish();
        } else {
            if (canGoBack()) {
                mWebView.goBack();
            } else {
                finish();
            }
        }
    }

    private boolean isNeedExitActivity() {
        if (mWebView != null) {
            String url = mWebView.getUrl();
            if (!TextUtils.isEmpty(url)) {
                return url.contains("Family/home");
            }
        }
        return false;
    }

    public static void forward(Context context, String url) {
        Intent intent = new Intent(context, FamilyActivity.class);
        intent.putExtra(Constants.URL, url);
        context.startActivity(intent);
    }


    @Override
    protected void onDestroy() {
        if (mWebView != null) {
            ViewGroup parent = (ViewGroup) mWebView.getParent();
            if (parent != null) {
                parent.removeView(mWebView);
            }
            mWebView.destroy();
        }
        super.onDestroy();
    }

    public class WebAppInterface {
        Context mContext;

        WebAppInterface(Context c) {
            mContext = c;
        }

        @JavascriptInterface
        public void sendValueToAndroid(String value) {
            Log.e("Action", value);
            if (value.equals("addNewFamily")) {
                onCreateFamily();
            }
        }
    }

    private void onCreateFamily() {
       RouteUtil.forwardCreateFamily();
    }


}
