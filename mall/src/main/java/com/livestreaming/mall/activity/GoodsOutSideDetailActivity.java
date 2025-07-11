package com.livestreaming.mall.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.livestreaming.common.CommonAppConfig;
import com.livestreaming.common.Constants;
import com.livestreaming.common.activity.AbsActivity;
import com.livestreaming.common.dialog.NotLoginDialogFragment;
import com.livestreaming.common.glide.ImgLoader;
import com.livestreaming.common.http.HttpCallback;
import com.livestreaming.common.utils.DialogUitl;
import com.livestreaming.common.utils.RouteUtil;
import com.livestreaming.common.utils.StringUtil;
import com.livestreaming.common.utils.ToastUtil;
import com.livestreaming.common.utils.WordUtil;
import com.livestreaming.im.activity.ChatRoomActivity;
import com.livestreaming.im.bean.ImConUserBean;
import com.livestreaming.im.http.ImHttpConsts;
import com.livestreaming.im.http.ImHttpUtil;
import com.livestreaming.live.bean.LiveBean;
import com.livestreaming.live.event.LiveFloatWindowEvent;
import com.livestreaming.live.floatwindow.FloatWindowUtil;
import com.livestreaming.mall.R;
import com.livestreaming.mall.dialog.GoodsCertDialogFragment;
import com.livestreaming.mall.dialog.GoodsShareDialogFragment;
import com.livestreaming.mall.http.MallHttpConsts;
import com.livestreaming.mall.http.MallHttpUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

/**
 * 站外商品详情
 */
public class GoodsOutSideDetailActivity extends AbsActivity implements View.OnClickListener {

    public static void forward(Context context, String goodsId) {
        forward(context, goodsId, false);
    }

    public static void forward(Context context, String goodsId, boolean fromShop) {
        Intent intent = new Intent(context, GoodsOutSideDetailActivity.class);
        intent.putExtra(Constants.MALL_GOODS_ID, goodsId);
        intent.putExtra(Constants.MALL_GOODS_FROM_SHOP, fromShop);
        context.startActivity(intent);
    }

    private TextView mGoodsPrice;
    private TextView mOriginPrice;
    private TextView mGoodsName;
    private TextView mGoodsDes;
    private ImageView mGoodsThumb;
    private ImageView mShopThumb;
    private TextView mShopName;
    private TextView mSaleNumAll;//总销量
    private TextView mGoodsQuality;//商品质量
    private TextView mTaiDuFuWu;//服务态度
    private TextView mTaiDuWuLiu;//物流态度
    private String mUnitString;
    private String mToUid;
    private String mGoodsId;
    private boolean mIsCanBuy;//是否可以购买
    private boolean mFromShop;
    private String mHref;

    private Drawable mBgCollect0;
    private Drawable mBgCollect1;
    private ImageView mImgCollect;
    private View mXiajiaStatus;//下架
    private boolean mIsXiajia;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_goods_detail_out_side;
    }

    @Override
    protected void main() {
        Intent intent = getIntent();
        mGoodsId = intent.getStringExtra(Constants.MALL_GOODS_ID);
        mFromShop = intent.getBooleanExtra(Constants.MALL_GOODS_FROM_SHOP, false);
        mGoodsPrice = findViewById(R.id.goods_price);
        mOriginPrice = findViewById(R.id.origin_price);
        mOriginPrice.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
        mGoodsName = findViewById(R.id.goods_name);
        mGoodsDes = findViewById(R.id.goods_des);
        mGoodsThumb = findViewById(R.id.goods_thumb);
        mShopThumb = findViewById(R.id.shop_thumb);
        mShopName = findViewById(R.id.shop_name);
        mSaleNumAll = findViewById(R.id.sale_num_all);
        mGoodsQuality = findViewById(R.id.goods_quality);
        mTaiDuFuWu = findViewById(R.id.taidu_fuwu);
        mTaiDuWuLiu = findViewById(R.id.taidu_wuliu);
        mUnitString = WordUtil.getString(com.livestreaming.common.R.string.mall_168);
        findViewById(R.id.btn_service).setOnClickListener(this);
        findViewById(R.id.btn_shop_home).setOnClickListener(this);
        findViewById(R.id.btn_shop).setOnClickListener(this);
        findViewById(R.id.btn_kefu).setOnClickListener(this);
        findViewById(R.id.btn_collect).setOnClickListener(this);
        findViewById(R.id.btn_buy_now).setOnClickListener(this);
        findViewById(R.id.btn_share).setOnClickListener(this);
        mXiajiaStatus = findViewById(R.id.xiajia_status);
        mImgCollect = findViewById(R.id.img_collect);
        mBgCollect0 = ContextCompat.getDrawable(mContext, R.mipmap.icon_shop_collect_0);
        mBgCollect1 = ContextCompat.getDrawable(mContext, R.drawable.icon_shop_collect_0);
        EventBus.getDefault().register(this);
        getGoodsInfo();
        LiveBean liveBean = intent.getParcelableExtra(Constants.LIVE_BEAN);
        if (liveBean != null) {
            FloatWindowUtil.getInstance().setType(Constants.FLOAT_TYPE_GOODS).setLiveBean(liveBean).requestPermission();
        }
    }

    /**
     * 获取商品详情，展示数据
     */
    private void getGoodsInfo() {
        MallHttpUtil.getGoodsInfo(mGoodsId, new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0 && info.length > 0) {
                    JSONObject obj = JSON.parseObject(info[0]);
                    JSONObject goodsInfo = obj.getJSONObject("goods_info");
                    mHref = goodsInfo.getString("href");
                    List<String> thumbs = JSON.parseArray(goodsInfo.getString("thumbs_format"), String.class);
                    if (thumbs != null && thumbs.size() > 0) {
                        if (mGoodsThumb != null) {
                            ImgLoader.display(mContext, thumbs.get(0), mGoodsThumb);
                        }
                    }

                    if (mGoodsName != null) {
                        mGoodsName.setText(goodsInfo.getString("name"));
                    }
                    if (mGoodsDes != null) {
                        mGoodsDes.setText(goodsInfo.getString("goods_desc"));
                    }
                    if (mGoodsPrice != null) {
                        mGoodsPrice.setText(goodsInfo.getString("present_price"));
                    }
                    if (mOriginPrice != null) {
                        mOriginPrice.setText(StringUtil.contact(WordUtil.getString(com.livestreaming.common.R.string.money_symbol)
                                , goodsInfo.getString("original_price")));
                    }
                    JSONObject shopInfo = obj.getJSONObject("shop_info");
                    mToUid = shopInfo.getString("uid");
                    if (mShopThumb != null) {
                        ImgLoader.display(mContext, shopInfo.getString("avatar"), mShopThumb);
                    }
                    if (mShopName != null) {
                        mShopName.setText(shopInfo.getString("name"));
                    }
                    if (mSaleNumAll != null) {
                        mSaleNumAll.setText(String.format(mUnitString, shopInfo.getString("sale_nums")));
                    }
                    if (mGoodsQuality != null) {
                        mGoodsQuality.setText(shopInfo.getString("quality_points"));
                    }
                    if (mTaiDuFuWu != null) {
                        mTaiDuFuWu.setText(shopInfo.getString("service_points"));
                    }
                    if (mTaiDuWuLiu != null) {
                        mTaiDuWuLiu.setText(shopInfo.getString("express_points"));
                    }
                    String sellerId = goodsInfo.getString("uid");
                    mIsCanBuy = !TextUtils.isEmpty(sellerId) && !sellerId.equals(CommonAppConfig.getInstance().getUid());
                    if (mIsCanBuy) {
                        setTitle(WordUtil.getString(com.livestreaming.common.R.string.mall_120));
                        if (CommonAppConfig.getInstance().isLogin()) {
                            MallHttpUtil.buyerAddBrowseRecord(mGoodsId);
                        }
                    } else {
                        setTitle(WordUtil.getString(com.livestreaming.common.R.string.mall_119));
                    }
                    showCollect(goodsInfo.getIntValue("iscollect") == 1);
                    mIsXiajia = goodsInfo.getIntValue("status") == -1;
                    if (mIsXiajia) {//下架
                        if (mXiajiaStatus != null && mXiajiaStatus.getVisibility() != View.VISIBLE) {
                            mXiajiaStatus.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        FloatWindowUtil.getInstance().dismiss();
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        MallHttpUtil.cancel(MallHttpConsts.GET_GOODS_INFO);
        MallHttpUtil.cancel(MallHttpConsts.SET_GOODS_COLLECT);
        ImHttpUtil.cancel(ImHttpConsts.GET_IM_USER_INFO);
        FloatWindowUtil.getInstance().dismiss();
        super.onDestroy();
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id != R.id.btn_shop_home && id != R.id.btn_shop) {
            if (!((AbsActivity) mContext).checkLogin()) {
                return;
            }
        }
        if (id == R.id.btn_service) {
            clickService();
        } else if (id == R.id.btn_buy_now) {
            buy();
        } else if (id == R.id.btn_shop_home || id == R.id.btn_shop) {
            forwardShopHome();
        } else if (id == R.id.btn_kefu) {
            forwardChat();
        } else if (id == R.id.btn_collect) {
            clickCollect();
        } else if (id == R.id.btn_share) {
            shareGoods();
        }


    }

    /**
     * 购买
     */
    private void buy() {
        if (!mIsCanBuy) {
            ToastUtil.show(com.livestreaming.common.R.string.mall_307);
            return;
        }
        if (mIsXiajia) {
            ToastUtil.show(com.livestreaming.common.R.string.mall_403);
            return;
        }
        new DialogUitl.Builder(mContext)
                .setContent(WordUtil.getString(com.livestreaming.common.R.string.mall_377))
                .setCancelable(true)
                .setBackgroundDimEnabled(true)
                .setConfrimString(WordUtil.getString(com.livestreaming.common.R.string.mall_378))
                .setClickCallback(new DialogUitl.SimpleCallback() {
                    @Override
                    public void onConfirmClick(Dialog dialog, String content) {
                        if (!TextUtils.isEmpty(mHref)) {
                            try {
                                Intent intent = new Intent();
                                intent.setAction(Intent.ACTION_VIEW);
                                intent.setData(Uri.parse(mHref));
                                startActivity(intent);
                            } catch (Exception e) {
                                ToastUtil.show(com.livestreaming.common.R.string.mall_379);
                            }
                        } else {
                            ToastUtil.show(com.livestreaming.common.R.string.mall_379);
                        }
                    }
                })
                .build()
                .show();
    }

    /**
     * 服务资质
     */
    private void clickService() {
        if (!mIsCanBuy) {
            ToastUtil.show(com.livestreaming.common.R.string.mall_307);
            return;
        }
        GoodsCertDialogFragment fragment = new GoodsCertDialogFragment();
        fragment.show(getSupportFragmentManager(), "GoodsCertDialogFragment");
    }


    /**
     * 前往店铺
     */
    private void forwardShopHome() {
        if (!mIsCanBuy) {
            ToastUtil.show(com.livestreaming.common.R.string.mall_307);
            return;
        }
        if (mFromShop) {
            onBackPressed();
        } else {
            ShopHomeActivity.forward(mContext, mToUid);
        }
    }

    /**
     * 跳转到个人主页
     */
    private void forwardUserHome() {
        if (!mIsCanBuy) {
            ToastUtil.show(com.livestreaming.common.R.string.mall_307);
            return;
        }
        if (!TextUtils.isEmpty(mToUid)) {
            RouteUtil.forwardUserHome(mContext, mToUid);
        }
    }


    /**
     * 私信聊天
     */
    private void forwardChat() {
        if (!mIsCanBuy) {
            ToastUtil.show(com.livestreaming.common.R.string.mall_307);
            return;
        }
        if (TextUtils.isEmpty(mToUid)) {
            return;
        }
        ImHttpUtil.getImUserInfo(mToUid, new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0 && info.length > 0) {
                    ImConUserBean bean = JSON.parseObject(info[0], ImConUserBean.class);
                    if (bean != null) {
                        ChatRoomActivity.forward(mContext, bean, bean.getAttent() == 1, false);
                    }
                }
            }
        });
    }


    /**
     * 点击收藏
     */
    private void clickCollect() {
        if (!mIsCanBuy) {
            ToastUtil.show(com.livestreaming.common.R.string.mall_307);
            return;
        }
        MallHttpUtil.setGoodsCollect(mGoodsId, new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0 && info.length > 0) {
                    JSONObject obj = JSON.parseObject(info[0]);
                    showCollect(obj.getIntValue("iscollect") == 1);
                }
                ToastUtil.show(msg);
            }
        });
    }


    private void showCollect(boolean isCollect) {
        if (mImgCollect != null) {
            mImgCollect.setImageDrawable(isCollect ? mBgCollect1 : mBgCollect0);
        }
    }

    /**
     * 未登录的弹窗
     */
    @Override
    public void showNotLoginDialog() {
        NotLoginDialogFragment fragment = new NotLoginDialogFragment();
        fragment.setActionListener(new NotLoginDialogFragment.ActionListener() {
            @Override
            public void beforeForwardLogin() {
                finish();
            }
        });
        fragment.show(getSupportFragmentManager(), "NotLoginDialogFragment");
    }


    /**
     * 分享
     */
    private void shareGoods() {
        GoodsShareDialogFragment fragment = new GoodsShareDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString(Constants.MALL_GOODS_ID, mGoodsId);
        fragment.setArguments(bundle);
        fragment.show(getSupportFragmentManager(), "GoodsShareDialogFragment");
    }

    /**
     * 点击悬浮窗
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLiveFloatWindowEvent(LiveFloatWindowEvent e) {
        if (e.getType() == Constants.FLOAT_TYPE_GOODS) {
            RouteUtil.forwardLiveAudienceActivity(mContext);
        }
    }

}
