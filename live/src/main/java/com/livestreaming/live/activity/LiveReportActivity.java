package com.livestreaming.live.activity;

import android.content.Context;
import android.content.Intent;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.livestreaming.common.Constants;
import com.livestreaming.common.activity.AbsActivity;
import com.livestreaming.common.http.HttpCallback;
import com.livestreaming.common.utils.KeyBoardUtil;
import com.livestreaming.common.utils.ToastUtil;
import com.livestreaming.common.utils.WordUtil;
import com.livestreaming.live.R;
import com.livestreaming.live.adapter.LiveReportAdapter;
import com.livestreaming.live.bean.LiveReportBean;
import com.livestreaming.live.http.LiveHttpConsts;
import com.livestreaming.live.http.LiveHttpUtil;
import com.livestreaming.live.utils.LiveStorge;

import java.util.Arrays;
import java.util.List;

/**
 * Created by cxf on 2018/12/15.
 * 直播间举报
 */

public class LiveReportActivity extends AbsActivity implements LiveReportAdapter.ActionListener, KeyBoardUtil.KeyBoardHeightListener {

    public static void forward(Context context, String toUid) {
        Intent intent = new Intent(context, LiveReportActivity.class);
        intent.putExtra(Constants.TO_UID, toUid);
        context.startActivity(intent);
    }

    private String mToUid;
    private RecyclerView mRecyclerView;
    private LiveReportAdapter mAdapter;
    private KeyBoardUtil mKeyBoardUtil;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_live_report;
    }

    @Override
    protected void main() {
        if (LiveStorge.isSecure()){
            getWindow().setFlags(
                    android.view.WindowManager.LayoutParams.FLAG_SECURE,
                    android.view.WindowManager.LayoutParams.FLAG_SECURE
            );
        }
        setTitle(WordUtil.getString(com.livestreaming.common.R.string.report));
        mToUid = getIntent().getStringExtra(Constants.TO_UID);
        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        mKeyBoardUtil = new KeyBoardUtil(mRecyclerView,this);
        LiveHttpUtil.getLiveReportList(new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0) {
                    List<LiveReportBean> list = JSON.parseArray(Arrays.toString(info), LiveReportBean.class);
                    mAdapter = new LiveReportAdapter(mContext, list);
                    mAdapter.setActionListener(LiveReportActivity.this);
                    if (mRecyclerView != null) {
                        mRecyclerView.setAdapter(mAdapter);
                    }
                }
            }
        });
    }

    @Override
    public void onReportClick(LiveReportBean bean, String text) {
        if (TextUtils.isEmpty(mToUid)) {
            return;
        }
        if (bean == null) {
            ToastUtil.show(com.livestreaming.common.R.string.video_report_tip_3);
            return;
        }
        String content = bean.getName();
        if (!TextUtils.isEmpty(text)) {
            content += " " + text;
        }
        LiveHttpUtil.setReport(mToUid, content, mReportCallback);
    }

    private HttpCallback mReportCallback = new HttpCallback() {
        @Override
        public void onSuccess(int code, String msg, String[] info) {
            if (code == 0) {
                ToastUtil.show(com.livestreaming.common.R.string.video_report_tip_4);
                onBackPressed();
            } else {
                ToastUtil.show(msg);
            }
        }
    };

    @Override
    public void onKeyBoardHeightChanged(int keyboardHeight) {
        if (mRecyclerView != null) {
            mRecyclerView.setTranslationY(-keyboardHeight);
            if (keyboardHeight > 0 && mAdapter != null) {
                mRecyclerView.smoothScrollToPosition(mAdapter.getItemCount() - 1);
            }
        }
    }


    private void release() {
        LiveHttpUtil.cancel(LiveHttpConsts.GET_LIVE_REPORT_LIST);
        LiveHttpUtil.cancel(LiveHttpConsts.SET_REPORT);
        if (mKeyBoardUtil != null) {
            mKeyBoardUtil.release();
        }
        mKeyBoardUtil = null;
        if (mAdapter != null) {
            mAdapter.setActionListener(null);
        }
        mAdapter = null;
    }

    @Override
    public void onBackPressed() {
        release();
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        release();
        super.onDestroy();
    }
}
