package com.livestreaming.mall.activity;

import android.content.Context;
import android.content.Intent;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.view.View;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.livestreaming.common.HtmlConfig;
import com.livestreaming.common.activity.AbsActivity;
import com.livestreaming.common.activity.WebViewActivity;
import com.livestreaming.common.adapter.RefreshAdapter;
import com.livestreaming.common.custom.CommonRefreshView;
import com.livestreaming.common.http.HttpCallback;
import com.livestreaming.common.utils.StringUtil;
import com.livestreaming.common.utils.WordUtil;
import com.livestreaming.mall.R;
import com.livestreaming.mall.adapter.BuyerRefundRecordAdapter;
import com.livestreaming.mall.bean.BuyerRefundRecordBean;
import com.livestreaming.mall.http.MallHttpConsts;
import com.livestreaming.mall.http.MallHttpUtil;

import java.util.List;

public class BuyerAccountActivity extends AbsActivity implements View.OnClickListener {

    public static void forward(Context context) {
        context.startActivity(new Intent(context, BuyerAccountActivity.class));
    }

    private TextView mTotal;
    private TextView mBalance;
    private CommonRefreshView mRefreshView;
    private BuyerRefundRecordAdapter mAdapter;
    private String mBalanceVal;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_buyer_account;
    }

    @Override
    protected void main() {
        setTitle(WordUtil.getString(com.livestreaming.common.R.string.mall_004));
        mTotal = findViewById(R.id.total);
        mBalance = findViewById(R.id.balance);
        findViewById(R.id.btn_cash_record).setOnClickListener(this);
        findViewById(R.id.btn_cash).setOnClickListener(this);
        mRefreshView = findViewById(R.id.refreshView);
        mRefreshView.setEmptyLayoutId(R.layout.view_no_data_refund_record);
        mRefreshView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        mRefreshView.setDataHelper(new CommonRefreshView.DataHelper<BuyerRefundRecordBean>() {
            @Override
            public RefreshAdapter<BuyerRefundRecordBean> getAdapter() {
                if (mAdapter == null) {
                    mAdapter = new BuyerRefundRecordAdapter(mContext);
                }
                return mAdapter;
            }

            @Override
            public void loadData(int p, HttpCallback callback) {
                MallHttpUtil.getBuyerAccountInfo(p, callback);
            }

            @Override
            public List<BuyerRefundRecordBean> processData(String[] info) {
                JSONObject obj = JSON.parseObject(info[0]);
                JSONObject balanceInfo = obj.getJSONObject("user_balance");
                mBalanceVal = balanceInfo.getString("balance");
                if (mTotal != null) {
                    mTotal.setText(renderBalanceText(mBalanceVal));
                }
                if (mBalance != null) {
                    mBalance.setText(StringUtil.contact(WordUtil.getString(com.livestreaming.common.R.string.money_symbol), mBalanceVal));
                }
                return JSON.parseArray(obj.getString("list"), BuyerRefundRecordBean.class);
            }

            @Override
            public void onRefreshSuccess(List<BuyerRefundRecordBean> list, int listCount) {

            }

            @Override
            public void onRefreshFailure() {

            }

            @Override
            public void onLoadMoreSuccess(List<BuyerRefundRecordBean> loadItemList, int loadItemCount) {

            }

            @Override
            public void onLoadMoreFailure() {

            }
        });
        mRefreshView.initData();
    }

    private CharSequence renderBalanceText(String text) {
        if (TextUtils.isEmpty(text)) {
            return text;
        }
        if (!text.contains(".")) {
            text += ".00";
        }
        SpannableStringBuilder builder = new SpannableStringBuilder(text);
        builder.setSpan(new AbsoluteSizeSpan(12, true), text.indexOf("."), builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return builder;
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_cash_record) {
            WebViewActivity.forward(mContext, HtmlConfig.MALL_CASH_RECORD);
        } else if (id == R.id.btn_cash) {
            BuyerCashActivity.forward(mContext, mBalanceVal);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (mRefreshView != null) {
                mRefreshView.initData();
            }
        }
    }

    @Override
    protected void onDestroy() {
        MallHttpUtil.cancel(MallHttpConsts.GET_BUYER_ACCOUNT_INFO);
        super.onDestroy();
    }
}
