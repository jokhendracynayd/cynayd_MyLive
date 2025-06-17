package com.livestreaming.common.http;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.android.billingclient.api.ProductDetails;
import com.livestreaming.common.CommonAppConfig;
import com.livestreaming.common.R;
import com.livestreaming.common.activity.ErrorActivity;
import com.livestreaming.common.bean.ConfigBean;
import com.livestreaming.common.event.FollowEvent;
import com.livestreaming.common.interfaces.CommonCallback;
import com.livestreaming.common.utils.L;
import com.livestreaming.common.utils.MD5Util;
import com.livestreaming.common.utils.SpUtil;
import com.livestreaming.common.utils.StringUtil;
import com.livestreaming.common.utils.ToastUtil;
import com.livestreaming.common.utils.WordFilterUtil;
import com.livestreaming.common.utils.WordUtil;

import org.greenrobot.eventbus.EventBus;

import java.util.Map;


public class CommonHttpUtil {

    public static final String SALT = "76576076c1f5f657b634e966c8836a06";

    /**
     * 取消网络请求
     */
    public static void cancel(String tag) {
        HttpClient.getInstance().cancel(tag);
    }

    public static void getConfig(HttpCallback callback) {
        HttpClient.getInstance().get("Home.getConfig", CommonHttpConsts.GET_CONFIG).execute(callback);
    }

    /**
     * 获取config
     */
    public static void getConfig(final CommonCallback<ConfigBean> commonCallback) {
        HttpClient.getInstance().get("Home.getConfig", CommonHttpConsts.GET_CONFIG)
                .execute(new HttpCallback() {
                    @Override
                    public void onSuccess(int code, String msg, String[] info) {
                        if (code == 0 && info.length > 0) {
                            try {
                                JSONObject obj = JSON.parseObject(info[0]);
                                ConfigBean bean = JSON.toJavaObject(obj, ConfigBean.class);
                                CommonAppConfig.getInstance().setConfig(bean);
                                CommonAppConfig.getInstance().setLevel(obj.getString("level"));
                                CommonAppConfig.getInstance().setAnchorLevel(obj.getString("levelanchor"));
                                SpUtil.getInstance().setStringValue(SpUtil.CONFIG, info[0]);
                                WordFilterUtil.getInstance().initWordMap(JSON.parseArray(obj.getString("sensitive_words"), String.class));
                                if (commonCallback != null) {
                                    commonCallback.callback(bean);
                                }
                            } catch (Exception e) {
                                String error = "info[0]:" + info[0] + "\n\n\n" + "Exception:" + e.getClass() + "---message--->" + e.getMessage();
                                ErrorActivity.forward("getconfig interface returns data exception", error);
                            }
                        }
                    }

                });
    }


    /**
     * QQ登录的时候 获取unionID 与PC端互通的时候用
     */
    public static void getQQLoginUnionID(String accessToken, final CommonCallback<String> commonCallback) {
        HttpClient.getInstance().getString("https://graph.qq.com/oauth2.0/me?access_token=" + accessToken + "&unionid=1", CommonHttpConsts.GET_QQ_LOGIN_UNION_ID)
                .execute(new StringHttpCallback() {
                    @Override
                    public void onSuccess(String data) {
                        if (commonCallback != null) {
                            data = data.substring(data.indexOf("{"), data.lastIndexOf("}") + 1);
                            L.e("getQQLoginUnionID------>" + data);
                            JSONObject obj = JSON.parseObject(data);
                            commonCallback.callback(obj.getString("unionid"));
                        }
                    }
                });
    }


    /**
     * 关注别人 或 取消对别人的关注的接口
     */
    public static void setAttention(String touid, CommonCallback<Integer> callback) {
        setAttention(CommonHttpConsts.SET_ATTENTION, touid, callback);
    }

    /**
     * 关注别人 或 取消对别人的关注的接口
     */
    public static void setAttention(String tag, final String touid, final CommonCallback<Integer> callback) {
        if (touid.equals(CommonAppConfig.getInstance().getUid())) {
            ToastUtil.show(WordUtil.getString(R.string.cannot_follow_self));
            return;
        }
        HttpClient.getInstance().get("User.setAttent", tag)
                .params("uid", CommonAppConfig.getInstance().getUid())
                .params("token", CommonAppConfig.getInstance().getToken())
                .params("touid", touid)
                .execute(new HttpCallback() {
                    @Override
                    public void onSuccess(int code, String msg, String[] info) {
                        if (code == 0 && info.length > 0) {
                            int isAttention = JSON.parseObject(info[0]).getIntValue("isattent");//1是 关注  0是未关注
                            EventBus.getDefault().post(new FollowEvent(touid, isAttention));
                            if (callback != null) {
                                callback.callback(isAttention);
                            }
                        }
                    }
                });
    }

    /**
     * 充值页面，我的钻石
     */
    public static void getBalance(HttpCallback callback) {
        HttpClient.getInstance().get("User.getBalance", CommonHttpConsts.GET_BALANCE)
                .params("uid", CommonAppConfig.getInstance().getUid())
                .params("token", CommonAppConfig.getInstance().getToken())
                .params("type", 0)
                .execute(callback);
    }

    public static void exchangeTicketToCoins(String amount, HttpCallback callback) {
        HttpClient.getInstance().get("User.convertCoins", "")
                .params("uid", CommonAppConfig.getInstance().getUid())
                .params("amount", amount)
                .execute(callback);
    }

    /**
     * 用支付宝充值 的时候在服务端生成订单号
     *
     * @param callback
     */
    public static void getAliOrder(String serviceName, Map<String, String> params, HttpCallback callback) {
        HttpClient.getInstance().get(serviceName, CommonHttpConsts.GET_ALI_ORDER, params)
                .execute(callback);
    }

    /**
     * 用微信支付充值 的时候在服务端生成订单号
     *
     * @param callback
     */
    public static void getWxOrder(String serviceName, Map<String, String> params, HttpCallback callback) {
        HttpClient.getInstance().get(serviceName, CommonHttpConsts.GET_WX_ORDER, params)
                .execute(callback);
    }


    /**
     * 用Paypal支付充值 的时候在服务端生成订单号
     *
     * @param callback
     */
    public static void getPaypalOrder(String serviceName, Map<String, String> params, HttpCallback callback) {
        HttpClient.getInstance().get(serviceName, CommonHttpConsts.GET_PAYPAL_ORDER, params)
                .execute(callback);
    }


    /**
     * 检查token是否失效
     */
    public static void checkTokenInvalid() {
        HttpClient.getInstance().get("User.ifToken", CommonHttpConsts.CHECK_TOKEN_INVALID)
                .params("uid", CommonAppConfig.getInstance().getUid())
                .params("token", CommonAppConfig.getInstance().getToken())
                .execute(NO_CALLBACK);
    }

    //不做任何操作的HttpCallback
    public static final HttpCallback NO_CALLBACK = new HttpCallback() {
        @Override
        public void onSuccess(int code, String msg, String[] info) {

        }
    };


    /**
     * 上传文件 获取七牛云token的接口
     */
    public static void getUploadQiNiuToken(HttpCallback callback) {
        HttpClient.getInstance().get("Video.getQiniuToken", CommonHttpConsts.GET_UPLOAD_QI_NIU_TOKEN)
                .params("uid", CommonAppConfig.getInstance().getUid())
                .params("token", CommonAppConfig.getInstance().getToken())
                .execute(callback);
    }

    /**
     * 判断商品是否下架及被删除
     */
    public static void checkGoodsExist(String goodsId, HttpCallback callback) {
        cancel("CHECK_GOODS_EXIST");
        HttpClient.getInstance().get("Shop.getGoodExistence", "CHECK_GOODS_EXIST")
                .params("goodsid", goodsId)
                .params("uid", CommonAppConfig.getInstance().getUid())
                .params("token", CommonAppConfig.getInstance().getToken())
                .execute(callback);
    }


    /**
     * 获取美颜值
     */
    public static void getBeautyValue(HttpCallback callback) {
        HttpClient.getInstance().get("User.getBeautyParams", CommonHttpConsts.GET_BEAUTY_VALUE)
                .params("uid", CommonAppConfig.getInstance().getUid())
                .params("token", CommonAppConfig.getInstance().getToken())
                .execute(callback);
    }


    /**
     * 设置美颜值
     */
    public static void setBeautyValue(String jsonStr) {
        HttpClient.getInstance().get("User.setBeautyParams", CommonHttpConsts.SET_BEAUTY_VALUE)
                .params("uid", CommonAppConfig.getInstance().getUid())
                .params("token", CommonAppConfig.getInstance().getToken())
                .params("params", jsonStr)
                .execute(NO_CALLBACK);
    }

    /**
     * 获取上传信息
     */
    public static void getUploadInfo(HttpCallback callback) {
        HttpClient.getInstance().get("Upload.getCosInfo", CommonHttpConsts.GET_UPLOAD_INFO)
                .execute(callback);
    }

    /**
     * 获取 BraintreeToken
     */
    public static void getBraintreeToken(HttpCallback callback) {
        HttpClient.getInstance().get("User.getBraintreeToken", CommonHttpConsts.GET_BRAINTREE_TOKEN)
                .params("uid", CommonAppConfig.getInstance().getUid())
                .params("token", CommonAppConfig.getInstance().getToken())
                .execute(callback);
    }


    /**
     * Braintree支付回调
     */
    public static void braintreeCallback(String orderId, String buyType, String nonce, String money, HttpCallback callback) {
        String time = String.valueOf(System.currentTimeMillis() / 1000);
        CommonAppConfig appConfig = CommonAppConfig.getInstance();
        String uid = appConfig.getUid();
        String token = appConfig.getToken();
        String sign = MD5Util.getMD5(StringUtil.contact("nonce=", nonce, "&orderno=", orderId, "&ordertype=", buyType, "&time=", time, "&uid=", uid, "&", CommonHttpUtil.SALT));
        HttpClient.getInstance().get("User.BraintreeCallback", CommonHttpConsts.BRAINTREE_CALLBACK)
                .params("uid", uid)
                .params("token", token)
                .params("orderno", orderId)
                .params("ordertype", buyType)
                .params("nonce", nonce)
                .params("money", money)
                .params("time", time)
                .params("sign", sign)
                .execute(callback);
    }


    /**
     * 获取商品详情
     */
    public static void getGoodsInfo(String goodsId, HttpCallback callback) {
        HttpClient.getInstance().get("Shop.getGoodsInfo", CommonHttpConsts.GET_GOODS_INFO)
                .params("uid", CommonAppConfig.getInstance().getUid())
                .params("token", CommonAppConfig.getInstance().getToken())
                .params("goodsid", goodsId)
                .execute(callback);
    }

    /**
     * 获取首充充值规则列表
     */
    public static void getFirstChargeRules(HttpCallback callback) {
        HttpClient.getInstance().get("Charge.getFirstChargeRules", CommonHttpConsts.GET_FIRST_CHARGE_RULES)
                .params("uid", CommonAppConfig.getInstance().getUid())
                .params("token", CommonAppConfig.getInstance().getToken())
                .execute(callback);
    }

    /**
     * 用户更新自己的所在城市
     */
    public static void updateCity(String city) {
        HttpClient.getInstance().get("Home.updateCity", "updateCity")
                .params("uid", CommonAppConfig.getInstance().getUid())
                .params("token", CommonAppConfig.getInstance().getToken())
                .params("city", city)
                .execute(NO_CALLBACK);
    }

    public static void inializePaypalPayment(String amount, String changeId, String money, HttpCallback callback) {
        HttpClient.getInstance().get("Charge.addPaypalCharge", "")
                .params("uid", CommonAppConfig.getInstance().getUid())
                .params("amount", amount)
                .params("changeid", changeId)
                .params("money", money)
                .execute(callback);
    } public static void inializePaypalPaymentStore(String amount, String changeId, String money,int stor, HttpCallback callback) {
        HttpClient.getInstance().get("Charge.addPaypalCharge", "")
                .params("uid", CommonAppConfig.getInstance().getUid())
                .params("amount", amount)
                .params("changeid", changeId)
                .params("money", money)
                .params("store",stor)
                .execute(callback);
    }

    public static void checkGooglePayPayment(String purchaseToken, ProductDetails selectedProduct, HttpCallback httpCallback) {
        HttpClient.getInstance().get("Seller.add_diamonds_new", "")
                .params("user_id", CommonAppConfig.getInstance().getUid())
                .params("amount", selectedProduct.getName())
                .params("packageName", "com.livestreaming.mylive")
                .params("productId", selectedProduct.getProductId())
                .params("token", purchaseToken)
                .execute(httpCallback);
    }

    public static void onChangeCam(String mStream, String s, int roomType) {
        HttpClient.getInstance().get("Live.closeUserCamera", "")
                .params("uid", CommonAppConfig.getInstance().getUid())
                .params("token", CommonAppConfig.getInstance().getToken())
                .params("stream", mStream)
                .params("touid", CommonAppConfig.getInstance().getUid())
                .params("cam_status", s)
                .params("room", roomType)
                .execute(NO_CALLBACK);
    }

    public static void exchangeCoinsToGameCoins(String amount, HttpCallback httpCallback) {
        HttpClient.getInstance().get("User.exchangeToGames", "")
                .params("uid", CommonAppConfig.getInstance().getUid())
                .params("amount", amount)
                .execute(httpCallback);
    }

    public static void getGamesCoins(HttpCallback httpCallback) {
        HttpClient.getInstance().get("User.getUserGameCoins", "")
                .params("uid", CommonAppConfig.getInstance().getUid())
                .execute(httpCallback);
    }

    public static void withdrawGameCoins(String s, HttpCallback httpCallback) {
        HttpClient.getInstance().get("User.exchangeFromGames", "")
                .params("uid", CommonAppConfig.getInstance().getUid())
                .params("amount", s)
                .execute(httpCallback);
    }
}




