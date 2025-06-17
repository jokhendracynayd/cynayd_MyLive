package com.livestreaming.common.pay;

import android.content.Intent;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.android.billingclient.api.ProductDetails;
import com.livestreaming.common.CommonAppConfig;
import com.livestreaming.common.Constants;
import com.livestreaming.common.R;
import com.livestreaming.common.activity.AbsActivity;
import com.livestreaming.common.bean.UserBean;
import com.livestreaming.common.event.CoinChangeEvent;
import com.livestreaming.common.http.CommonHttpUtil;
import com.livestreaming.common.http.HttpCallback;
import com.livestreaming.common.pay.paypal.LivePayWebViewActivity;
import com.livestreaming.common.utils.ToastUtil;

import org.greenrobot.eventbus.EventBus;

import java.util.Map;


public class PayPresenter {

    private String mServiceNamePaypal;
    private long mBalanceValue;
    private AbsActivity mActivity;
    private PayCallback mPayCallback;


    public PayPresenter(AbsActivity activity) {
        mActivity = activity;
    }

    public void setPayCallback(PayCallback callback) {
        mPayCallback = callback;
    }

    public long getBalanceValue() {
        return mBalanceValue;
    }

    public void setBalanceValue(long balanceValue) {
        mBalanceValue = balanceValue;
    }


    public void setServiceNamePaypal(String serviceNamePaypal) {
        mServiceNamePaypal = serviceNamePaypal;
    }


    public void pay(String payType, String money, String goodsName, Map<String, String> orderParams) {
        if (TextUtils.isEmpty(payType)) {
            ToastUtil.show(R.string.wallet_tip_5);
            return;
        }
        switch (payType) {
            case Constants.PAY_TYPE_GOOGLE://微信支付
                googlePay(orderParams);
                break;
            case Constants.PAY_TYPE_PAYPAL://paypal支付
                paypal(money, goodsName, orderParams);
                break;
            case Constants.PAY_COD:
                payCashOnDiliveryGoods(money, goodsName, orderParams);
            case Constants.PAY_COINS:
                payWithCoins(money, goodsName, orderParams);
            default:
                break;
        }
    }

    private void payWithCoins(String money, String goodsName, Map<String, String> orderParams) {

    }

    private void payCashOnDiliveryGoods(String money, String goodsName, Map<String, String> orderParams) {

    }

    public ProductDetails selectedProduct;

    public void googlePay(Map<String, String> orderParams) {
        GooglePayPresenter.getInstance().initClient();
        GooglePayPresenter.getInstance().startConnection(mActivity,orderParams);
    }

    /**
     * paypal支付
     */
    private void paypal(final String money, final String goodsName, final Map<String, String> orderParams) {
        if (mActivity == null || TextUtils.isEmpty(mServiceNamePaypal) ||
                orderParams == null || orderParams.size() == 0 || TextUtils.isEmpty(money) || TextUtils.isEmpty(goodsName)) {
            return;
        }
        String amount = orderParams.get("coin");
        if (amount == null) {
            amount = money;
        }
        String changeId = orderParams.get("changeid");
        String isOffer = orderParams.get("isOffer");
        if (!amount.isEmpty()) {
            if (changeId != null && !changeId.isEmpty()) {
                if (isOffer != null && !isOffer.isEmpty()) {
                    CommonHttpUtil.inializePaypalPaymentStore(amount, changeId, money, 2, new HttpCallback() {
                        @Override
                        public void onSuccess(int code, String msg, String[] info) {
                            if (info.length > 0) {
                                JSONObject obj = JSON.parseObject(info[0]);
                                String orderId = obj.getString("orderid");
                                Intent intent = new Intent(mActivity, LivePayWebViewActivity.class);
                                String url = Constants.PaypalUrl + orderId;
                                intent.putExtra(LivePayWebViewActivity.EXTRA_URL, url);
                                mActivity.startActivity(intent);
                            } else {
                                ToastUtil.show(R.string.pay_fail);
                            }
                        }
                    });
                } else {
                    CommonHttpUtil.inializePaypalPayment(amount, changeId, money, new HttpCallback() {
                        @Override
                        public void onSuccess(int code, String msg, String[] info) {
                            if (info.length > 0) {
                                JSONObject obj = JSON.parseObject(info[0]);
                                String orderId = obj.getString("orderid");
                                Intent intent = new Intent(mActivity, LivePayWebViewActivity.class);
                                String url = Constants.PaypalUrl + orderId;
                                intent.putExtra(LivePayWebViewActivity.EXTRA_URL, url);
                                mActivity.startActivity(intent);
                            } else {
                                ToastUtil.show(R.string.pay_fail);
                            }
                        }
                    });
                }
            } else {
                changeId = orderParams.get("orderid");
                CommonHttpUtil.inializePaypalPaymentStore(amount, changeId, money, 1, new HttpCallback() {
                    @Override
                    public void onSuccess(int code, String msg, String[] info) {
                        if (info.length > 0) {
                            JSONObject obj = JSON.parseObject(info[0]);
                            String orderId = obj.getString("orderid");
                            Intent intent = new Intent(mActivity, LivePayWebViewActivity.class);
                            String url = Constants.PaypalUrl + orderId;
                            intent.putExtra(LivePayWebViewActivity.EXTRA_URL, url);
                            mActivity.startActivity(intent);
                        } else {
                            ToastUtil.show(R.string.pay_fail);
                        }
                    }
                });
            }
        }
    }


    /**
     * 检查支付结果
     */
    public void checkPayResult() {
        CommonHttpUtil.getBalance(new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0 && info.length > 0) {
                    JSONObject obj = JSON.parseObject(info[0]);
                    String coin = obj.getString("coin");
                    long balanceValue = Long.parseLong(coin);
                    if (balanceValue > mBalanceValue) {
                        mBalanceValue = balanceValue;
                        ToastUtil.show(R.string.coin_charge_success);
                        UserBean u = CommonAppConfig.getInstance().getUserBean();
                        if (u != null) {
                            u.setCoin(coin);
                        }
                        EventBus.getDefault().post(new CoinChangeEvent(coin, true));
                    }
                }
            }
        });
    }


    public void release() {
        mActivity = null;
        mPayCallback = null;
    }
}
