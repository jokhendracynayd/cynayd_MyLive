package com.livestreaming.common.pay.paypal;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.widget.LinearLayout;

import com.livestreaming.common.R;
import com.livestreaming.common.activity.AbsActivity;
import com.just.agentweb.AgentWeb;


public class LivePayWebViewActivity extends AbsActivity
{
    public static final String EXTRA_URL = "extra_url";

    //@ViewInject
    private LinearLayout cus_webview;

    private String mUrl;
    private AgentWeb   mAgentWeb;


    @Override
    protected int getLayoutId() {
        return R.layout.act_live_pay_web;
    }

    private void init()
    {
        cus_webview=findViewById((R.id.cus_webview));
        initIntent();
        initView();
    }

    @Override
    protected void main(Bundle savedInstanceState) {
        super.main(savedInstanceState);
        init();
    }


    private void initIntent()
    {
        mUrl = getIntent().getStringExtra(EXTRA_URL);
    }

    private void initView()
    {
        JSInterface handler = new JSInterface(this);
//        cus_webview.addJavascriptInterface(handler);
//        cus_webview.setWebChromeClientListener(new DefaultWebChromeClient());
//        cus_webview.get(mUrl);

        mAgentWeb = AgentWeb.with(this)
                .setAgentWebParent(cus_webview, new LinearLayout.LayoutParams(-1, -1))
                .useDefaultIndicator()
                .createAgentWeb()
                .ready()
                .go(mUrl);

        mAgentWeb.getJsInterfaceHolder().addJavaObject("android",handler);
    }


    public class JSInterface {
        public Activity activity;

        public JSInterface(Activity activity) {
            this.activity = activity;
        }

        @JavascriptInterface
        public void setAuthToken(String authToken) {

        }

        @JavascriptInterface
        public void recharge() {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // 充值，用户在游戏内点击充值按钮 或者 用户下注时，余额不足

                }
            });
        }
    }
}
