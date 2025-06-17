package com.livestreaming.common.activity;

import android.os.Bundle;
import android.widget.LinearLayout;

import com.livestreaming.common.R;
import com.just.agentweb.AgentWeb;


/**
 * Created by shibx on 2017/3/24.
 */

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
        initIntent();
        initView();
    }

    @Override
    protected void main(Bundle savedInstanceState) {
        super.main(savedInstanceState);
        init();
        initIntent();
    }

    private void initIntent()
    {
        mUrl = getIntent().getStringExtra(EXTRA_URL);
    }

    private void initView()
    {
        mAgentWeb = AgentWeb.with(this)
                .setAgentWebParent(cus_webview, new LinearLayout.LayoutParams(-1, -1))
                .useDefaultIndicator()
                .createAgentWeb()
                .ready()
                .go(mUrl);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }
}
