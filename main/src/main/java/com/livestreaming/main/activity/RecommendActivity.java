package com.livestreaming.main.activity;

import android.content.Context;
import android.content.Intent;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.View;

import com.alibaba.fastjson.JSON;
import com.livestreaming.common.Constants;
import com.livestreaming.common.activity.AbsActivity;
import com.livestreaming.common.event.LoginChangeEvent;
import com.livestreaming.common.http.HttpCallback;
import com.livestreaming.main.R;
import com.livestreaming.main.adapter.RecommendAdapter;
import com.livestreaming.main.bean.RecommendBean;
import com.livestreaming.main.http.MainHttpConsts;
import com.livestreaming.main.http.MainHttpUtil;

import org.greenrobot.eventbus.EventBus;

import java.util.Arrays;
import java.util.List;

/**
 * Created by cxf on 2018/11/2.
 */

public class RecommendActivity extends AbsActivity implements View.OnClickListener {

    private RecyclerView mRecyclerView;
    private RecommendAdapter mAdapter;
    private boolean mFirstLogin;//是否是第一次登录

    @Override
    protected int getLayoutId() {
        return R.layout.activity_recommend;
    }

    @Override
    protected void main() {
        Intent intent = getIntent();
        mFirstLogin = intent.getBooleanExtra(Constants.FIRST_LOGIN, false);
        findViewById(R.id.btn_enter).setOnClickListener(this);
        findViewById(R.id.btn_skip).setOnClickListener(this);
        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new GridLayoutManager(mContext, 3, GridLayoutManager.VERTICAL, false));
        MainHttpUtil.getRecommend(new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0) {
                    List<RecommendBean> list = JSON.parseArray(Arrays.toString(info), RecommendBean.class);
                    if (mAdapter == null) {
                        mAdapter = new RecommendAdapter(mContext, list);
                        mRecyclerView.setAdapter(mAdapter);
                    }
                }
            }
        });
    }

    public static void forward(Context context, boolean firstLogin) {
        Intent intent = new Intent(context, RecommendActivity.class);
        intent.putExtra(Constants.FIRST_LOGIN, firstLogin);
        context.startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_enter) {
            enter();

        } else if (i == R.id.btn_skip) {
            skip();

        }
    }

    private void enter() {
        if (mAdapter == null) {
            skip();
            return;
        }
        String uids = mAdapter.getCheckedUid();
        if (TextUtils.isEmpty(uids)) {
            skip();
            return;
        }
        MainHttpUtil.recommendFollow(uids, new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0) {
                    skip();
                }
            }
        });
    }

    /**
     * 跳过
     */
    private void skip() {
        EventBus.getDefault().post(new LoginChangeEvent(true, mFirstLogin));
        finish();
    }

    @Override
    public void onBackPressed() {
        skip();
    }

    @Override
    protected void onDestroy() {
        MainHttpUtil.cancel(MainHttpConsts.GET_RECOMMEND);
        MainHttpUtil.cancel(MainHttpConsts.RECOMMEND_FOLLOW);
        super.onDestroy();
    }
}
