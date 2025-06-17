package com.livestreaming.mall.dialog;

import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.livestreaming.common.CommonAppConfig;
import com.livestreaming.common.Constants;
import com.livestreaming.common.activity.AbsActivity;
import com.livestreaming.common.dialog.AbsDialogFragment;
import com.livestreaming.common.http.CommonHttpConsts;
import com.livestreaming.common.http.CommonHttpUtil;
import com.livestreaming.common.http.HttpCallback;
import com.livestreaming.common.pay.PayCallback;
import com.livestreaming.common.pay.PayPresenter;
import com.livestreaming.common.utils.MD5Util;
import com.livestreaming.common.utils.StringUtil;
import com.livestreaming.common.utils.ToastUtil;
import com.livestreaming.mall.R;
import com.livestreaming.mall.adapter.GoodsPayAdapter;
import com.livestreaming.mall.bean.GoodsPayBean;
import com.livestreaming.mall.http.MallHttpConsts;
import com.livestreaming.mall.http.MallHttpUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 购买付费内容 付款弹窗
 */
public class PayContentPayDialogFragment extends AbsDialogFragment implements View.OnClickListener {

    private RecyclerView mRecyclerView;
    private GoodsPayAdapter mAdapter;
    private String mGoodsId;
    private double mMoneyVal;
    private String mGoodsNameVal;
    private ActionListener mActionListener;
    private PayPresenter mPayPresenter;

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_pay_content_pay;
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
        window.setWindowAnimations(com.livestreaming.common.R.style.bottomToTopAnim);
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.BOTTOM;
        window.setAttributes(params);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle == null) {
            return;
        }
        mGoodsId = bundle.getString(Constants.MALL_GOODS_ID);
        mMoneyVal = bundle.getDouble(Constants.MALL_ORDER_MONEY, 0);
        mGoodsNameVal = bundle.getString(Constants.MALL_GOODS_NAME);
        TextView money = findViewById(R.id.money);
        money.setText(String.valueOf(mMoneyVal));
        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));

        findViewById(R.id.btn_close).setOnClickListener(this);
        findViewById(R.id.btn_pay).setOnClickListener(this);

        MallHttpUtil.getPayContentPayList(new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0 && info.length > 0) {
                    JSONObject obj = JSON.parseObject(info[0]);

                    List<GoodsPayBean> payList = JSON.parseArray(obj.getString("paylist"), GoodsPayBean.class);
                    if (payList != null && payList.size() > 0) {
                        payList.get(0).setChecked(true);
                        mAdapter = new GoodsPayAdapter(mContext, payList, obj.getString("balance"));
                        if (mRecyclerView != null) {
                            mRecyclerView.setAdapter(mAdapter);
                        }
                        mPayPresenter = new PayPresenter((AbsActivity) mContext);
                        mPayPresenter.setServiceNamePaypal(Constants.MALL_PAY_CONTENT_PAYPAL);
                        mPayPresenter.setPayCallback(new PayCallback() {
                            @Override
                            public void onSuccess() {
                                dismiss();
                                if (mActionListener != null) {
                                    mActionListener.onPaySuccess();
                                }
                            }

                            @Override
                            public void onFailed() {
                                ToastUtil.show(com.livestreaming.common.R.string.mall_367);
                            }
                        });
                    }
                }
            }
        });
    }

    public void setActionListener(ActionListener actionListener) {
        mActionListener = actionListener;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_close) {
            dismiss();
        } else if (id == R.id.btn_pay) {
            pay();
        }
    }

    private void pay() {
        if (TextUtils.isEmpty(mGoodsId) || mContext == null || mAdapter == null) {
            return;
        }
        GoodsPayBean bean = mAdapter.getCheckedPayType();
        if (bean == null) {
            return;
        }
        if (Constants.PAY_TYPE_BALANCE.equals(bean.getId())) {//余额支付
            balancePay();
        } else {//支付宝和微信支付
            if (mPayPresenter == null) {
                return;
            }
            String time = String.valueOf(System.currentTimeMillis() / 1000);
            CommonAppConfig appConfig = CommonAppConfig.getInstance();
            String uid = appConfig.getUid();
            String token = appConfig.getToken();
            String sign = MD5Util.getMD5(StringUtil.contact("object_id=", mGoodsId, "&time=", time,
                    "&token=", token, "&uid=", uid, "&", CommonHttpUtil.SALT));
            Map<String, String> orderParams = new HashMap<>();
            orderParams.put("uid", uid);
            orderParams.put("token", token);
            orderParams.put("time", time);
            orderParams.put("sign", sign);
            orderParams.put("object_id", mGoodsId);
            mPayPresenter.pay(bean.getId(), String.valueOf(mMoneyVal), mGoodsNameVal, orderParams);
        }
    }

    /**
     * 余额支付
     */
    private void balancePay() {
        MallHttpUtil.buyPayContent(mGoodsId, new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0) {
                    dismiss();
                    if (mActionListener != null) {
                        mActionListener.onPaySuccess();
                    }
                }
                ToastUtil.show(msg);
            }
        });
    }


    @Override
    public void onDestroy() {
        MallHttpUtil.cancel(MallHttpConsts.GET_PAY_CONTENT_PAY_LIST);
        MallHttpUtil.cancel(MallHttpConsts.BUY_PAY_CONTENT);
        MallHttpUtil.cancel(CommonHttpConsts.GET_ALI_ORDER);
        MallHttpUtil.cancel(CommonHttpConsts.GET_WX_ORDER);
        if (mPayPresenter != null) {
            mPayPresenter.release();
        }
        mPayPresenter = null;
        mContext = null;
        mAdapter = null;
        super.onDestroy();
    }

    public interface ActionListener {
        void onPaySuccess();
    }


}
