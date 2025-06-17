package com.livestreaming.mall.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.livestreaming.common.CommonAppConfig;
import com.livestreaming.common.Constants;
import com.livestreaming.common.activity.AbsActivity;
import com.livestreaming.common.dialog.NotLoginDialogFragment;
import com.livestreaming.common.glide.ImgLoader;
import com.livestreaming.common.http.CommonHttpUtil;
import com.livestreaming.common.http.HttpCallback;
import com.livestreaming.common.utils.DialogUitl;
import com.livestreaming.common.utils.DpUtil;
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
import com.livestreaming.mall.adapter.GoodsCommentAdapter;
import com.livestreaming.mall.adapter.GoodsDetailAdapter;
import com.livestreaming.mall.adapter.GoodsTitleAdapter;
import com.livestreaming.mall.bean.GoodsChooseSpecBean;
import com.livestreaming.mall.bean.GoodsCommentBean;
import com.livestreaming.mall.bean.GoodsSpecBean;
import com.livestreaming.mall.dialog.GoodsCertDialogFragment;
import com.livestreaming.mall.dialog.GoodsShareDialogFragment;
import com.livestreaming.mall.dialog.GoodsSpecDialogFragment;
import com.livestreaming.mall.http.MallHttpConsts;
import com.livestreaming.mall.http.MallHttpUtil;

import net.lucode.hackware.magicindicator.MagicIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.ColorTransitionPagerTitleView;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.SimplePagerTitleView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

/**
 * 商品详情
 */
public class GoodsDetailActivity extends AbsActivity implements View.OnClickListener {

    public static void forward(Context context, String goodsId, int type) {
        forward(context, goodsId, false, type);
    }

    public static void forward(Context context, String goodsId, boolean fromShop, int type) {
        forward(context, goodsId, fromShop, type, "0");
    }

    public static void forward(final Context context, final String goodsId, final boolean fromShop, final int type, final String liveUid) {
        if (CommonAppConfig.getInstance().isTeenagerType()) {
            ToastUtil.show(com.livestreaming.common.R.string.a_137);
            return;
        }
        CommonHttpUtil.checkGoodsExist(goodsId, new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0) {
                    if (type == Constants.GOODS_TYPE_OUT) {
                        Intent intent = new Intent(context, GoodsOutSideDetailActivity.class);
                        intent.putExtra(Constants.MALL_GOODS_ID, goodsId);
                        intent.putExtra(Constants.MALL_GOODS_FROM_SHOP, fromShop);
                        intent.putExtra(Constants.LIVE_UID, liveUid);
                        context.startActivity(intent);
                    } else {
                        Intent intent = new Intent(context, GoodsDetailActivity.class);
                        intent.putExtra(Constants.MALL_GOODS_ID, goodsId);
                        intent.putExtra(Constants.MALL_GOODS_FROM_SHOP, fromShop);
                        intent.putExtra(Constants.LIVE_UID, liveUid);
                        context.startActivity(intent);
                    }
                } else {
                    ToastUtil.show(msg);
                }
            }
        });

    }

    private ViewPager mViewPager;
    private GoodsTitleAdapter mTitleAdapter;
    private TextView mPageIndex;
    private TextView mGoodsPrice;
    private TextView mGoodsPriceYong;//佣金
    private TextView mGoodsName;
    private TextView mGoodsPostage;
    private TextView mGoodsSaleNum;
    private TextView mSellerAddress;//卖家地区
    private TextView mSaleNumAll;//总销量
    private TextView mGoodsQuality;//商品质量
    private TextView mTaiDuFuWu;//服务态度
    private TextView mTaiDuWuLiu;//物流态度
    private ImageView mShopThumb;
    private TextView mShopName;
    private View mBtnCommentMore;
    private String mUnitString;
    private MagicIndicator mIndicator;
    private String mCommentString;

    private View mGroupCommment;
    private View mGroupDetail;
    private RecyclerView mRecyclerViewComment;
    private RecyclerView mRecyclerViewDetail;
    private View mNoComment;
    private View mNoDetail;
    private TextView mCommentCountTextView;
    private int mTabIndex;

    private String mToUid;
    private String mGoodsId;
    private boolean mIsCanBuy;//是否可以购买
    private boolean mFromShop;
    private boolean mPaused;
    private List<GoodsChooseSpecBean> mSpecList;
    private double mPostageVal;

    private Drawable mBgCollect0;
    private Drawable mBgCollect1;
    private ImageView mImgCollect;
    private View mXiajiaStatus;//下架
    private boolean mXiajia;
    private String mLiveUid = "0";
    private String mShareUid;
    private String mShareIncome;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_goods_detail;
    }

    @Override
    protected void main() {
        mCommentString = WordUtil.getString(com.livestreaming.common.R.string.mall_131);
        Intent intent = getIntent();
        mGoodsId = intent.getStringExtra(Constants.MALL_GOODS_ID);
        mFromShop = intent.getBooleanExtra(Constants.MALL_GOODS_FROM_SHOP, false);
        mLiveUid = intent.getStringExtra(Constants.LIVE_UID);
        if (Constants.MALL_PLAT_UID.equals(mLiveUid)) {
            mLiveUid = "0";
        }
        mShareUid = intent.getStringExtra(Constants.SHARE_UID);
        mGoodsPrice = findViewById(R.id.goods_price);
        mGoodsPriceYong = findViewById(R.id.price_yong);
        mGoodsName = findViewById(R.id.goods_name);
        mGoodsPostage = findViewById(R.id.goods_postage);
        mGoodsSaleNum = findViewById(R.id.goods_sale_num);
        mSellerAddress = findViewById(R.id.address);

        mSaleNumAll = findViewById(R.id.sale_num_all);
        mGoodsQuality = findViewById(R.id.goods_quality);
        mTaiDuFuWu = findViewById(R.id.taidu_fuwu);
        mTaiDuWuLiu = findViewById(R.id.taidu_wuliu);
        mUnitString = WordUtil.getString(com.livestreaming.common.R.string.mall_168);
        mShopThumb = findViewById(R.id.shop_thumb);
        mShopName = findViewById(R.id.shop_name);
        mBtnCommentMore = findViewById(R.id.btn_comment_more);
        mXiajiaStatus = findViewById(R.id.xiajia_status);

        findViewById(R.id.btn_choose_spec).setOnClickListener(this);
        findViewById(R.id.btn_service).setOnClickListener(this);
        findViewById(R.id.btn_shop_home).setOnClickListener(this);
        findViewById(R.id.btn_shop).setOnClickListener(this);
        findViewById(R.id.btn_kefu).setOnClickListener(this);
        findViewById(R.id.btn_buy_now).setOnClickListener(this);
        findViewById(R.id.btn_collect).setOnClickListener(this);
        findViewById(R.id.btn_share).setOnClickListener(this);
        mBtnCommentMore.setOnClickListener(this);

        mPageIndex = findViewById(R.id.page_index);
        mViewPager = findViewById(R.id.view_pager);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int position) {
                if (mTitleAdapter != null) {
                    if (position == 0) {
                        mTitleAdapter.resumePlayVideo();
                    } else {
                        mTitleAdapter.pausePlayVideo();
                    }
                    if (mPageIndex != null) {
                        mPageIndex.setText(StringUtil.contact(String.valueOf(position + 1), "/", String.valueOf(mTitleAdapter.getCount())));
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

        mIndicator = (MagicIndicator) findViewById(R.id.indicator);
        final String[] titles = new String[]{WordUtil.getString(com.livestreaming.common.R.string.mall_132), mCommentString};
        CommonNavigator commonNavigator = new CommonNavigator(mContext);
        commonNavigator.setAdapter(new CommonNavigatorAdapter() {

            @Override
            public int getCount() {
                return titles.length;
            }

            @Override
            public IPagerTitleView getTitleView(Context context, final int index) {
                SimplePagerTitleView simplePagerTitleView = new ColorTransitionPagerTitleView(context);
                simplePagerTitleView.setNormalColor(ContextCompat.getColor(mContext, com.livestreaming.common.R.color.gray3));
                simplePagerTitleView.setSelectedColor(ContextCompat.getColor(mContext, com.livestreaming.common.R.color.textColor));
                simplePagerTitleView.setText(titles[index]);
                simplePagerTitleView.setTextSize(14);
                simplePagerTitleView.getPaint().setFakeBoldText(true);
                simplePagerTitleView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mTabIndex == index) {
                            return;
                        }
                        mTabIndex = index;
                        mIndicator.onPageScrollStateChanged(2);
                        mIndicator.onPageSelected(index);
                        mIndicator.onPageScrolled(index, 0, 0);
                        mIndicator.onPageScrollStateChanged(0);
                        tab(index);
                    }
                });
                if (index == 1) {
                    mCommentCountTextView = simplePagerTitleView;
                }
                return simplePagerTitleView;
            }

            @Override
            public IPagerIndicator getIndicator(Context context) {
                LinePagerIndicator linePagerIndicator = new LinePagerIndicator(context);
                linePagerIndicator.setMode(LinePagerIndicator.MODE_EXACTLY);
                linePagerIndicator.setLineWidth(DpUtil.dp2px(20));
                linePagerIndicator.setRoundRadius(DpUtil.dp2px(2));
                linePagerIndicator.setColors(ContextCompat.getColor(mContext, com.livestreaming.common.R.color.global));
                return linePagerIndicator;
            }

        });
        commonNavigator.setAdjustMode(true);
        mIndicator.setNavigator(commonNavigator);

        mGroupCommment = findViewById(R.id.group_comment);
        mGroupDetail = findViewById(R.id.group_detail);
        mRecyclerViewComment = findViewById(R.id.recyclerView_comment);
        mRecyclerViewDetail = findViewById(R.id.recyclerView_detail);
        mNoComment = findViewById(R.id.no_comment);
        mNoDetail = findViewById(R.id.no_detail);
        mRecyclerViewComment.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        mRecyclerViewDetail.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));

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
                    List<String> thumbs = JSON.parseArray(goodsInfo.getString("thumbs_format"), String.class);
                    mTitleAdapter = new GoodsTitleAdapter(mContext, goodsInfo.getString("video_url_format"),
                            goodsInfo.getString("video_thumb_format"), thumbs);
                    if (mViewPager != null) {
                        mViewPager.setOffscreenPageLimit(thumbs.size());
                        mViewPager.setAdapter(mTitleAdapter);
                    }
                    if (mPageIndex != null) {
                        mPageIndex.setText(StringUtil.contact("1/", String.valueOf(mTitleAdapter.getCount())));
                    }
                    if (mCommentCountTextView != null) {
                        mCommentCountTextView.setText(StringUtil.contact(mCommentString, "(", goodsInfo.getString("comment_nums"), ")"));
                    }
                    List<GoodsCommentBean> commentList = JSON.parseArray(obj.getString("comment_lists"), GoodsCommentBean.class);
                    if (commentList.size() == 0) {
                        if (mNoComment != null && mNoComment.getVisibility() != View.VISIBLE) {
                            mNoComment.setVisibility(View.VISIBLE);
                        }
                    } else {
                        if (mBtnCommentMore != null && mBtnCommentMore.getVisibility() != View.VISIBLE) {
                            mBtnCommentMore.setVisibility(View.VISIBLE);
                        }
                        if (mRecyclerViewComment != null) {
                            GoodsCommentAdapter commentAdapter = new GoodsCommentAdapter(mContext, commentList, false);
                            mRecyclerViewComment.setAdapter(commentAdapter);
                        }
                    }
                    String detailText = goodsInfo.getString("content");
                    boolean hasDetailText = !TextUtils.isEmpty(detailText);
                    List<String> detailList = new ArrayList<>();
                    if (hasDetailText) {
                        detailList.add(detailText);
                    }
                    JSONArray detailImgArray = goodsInfo.getJSONArray("pictures_format");
                    for (int i = 0, size = detailImgArray.size(); i < size; i++) {
                        detailList.add(detailImgArray.getString(i));
                    }
                    if (detailList.size() == 0) {
                        if (mNoDetail.getVisibility() != View.VISIBLE) {
                            mNoDetail.setVisibility(View.VISIBLE);
                        }
                    } else {
                        if (mRecyclerViewDetail != null) {
                            GoodsDetailAdapter detailAdapter = new GoodsDetailAdapter(mContext, detailList, hasDetailText);
                            mRecyclerViewDetail.setAdapter(detailAdapter);
                        }
                    }
                    mPostageVal = goodsInfo.getDoubleValue("postage");
                    if (mGoodsPostage != null) {
                        mGoodsPostage.setText(String.format(WordUtil.getString(com.livestreaming.common.R.string.mall_179), goodsInfo.getString("postage")));
                    }
                    if (mGoodsSaleNum != null) {
                        mGoodsSaleNum.setText(String.format(WordUtil.getString(com.livestreaming.common.R.string.mall_114), goodsInfo.getString("sale_nums")));
                    }
                    if (mGoodsName != null) {
                        mGoodsName.setText(goodsInfo.getString("name"));
                    }
                    mSpecList = JSON.parseArray(goodsInfo.getString("specs_format"), GoodsChooseSpecBean.class);
                    if (mSpecList != null && mSpecList.size() > 0) {
                        GoodsChooseSpecBean bean = mSpecList.get(0);
                        bean.setChecked(true);
                        if (mGoodsPrice != null) {
                            mGoodsPrice.setText(bean.getPrice());
                        }
                    }
                    if (goodsInfo.getIntValue("is_sale_platform") == 1 && mGoodsPriceYong != null) {
                        mGoodsPriceYong.setText(goodsInfo.getIntValue("type") == 2 ?
                                StringUtil.contact(WordUtil.getString(com.livestreaming.common.R.string.mall_408), WordUtil.getString(com.livestreaming.common.R.string.money_symbol), goodsInfo.getString("commission")) : null
                        );
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
                    if (mSellerAddress != null) {
                        mSellerAddress.setText(StringUtil.contact(shopInfo.getString("city"), shopInfo.getString("area")));
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
                    mXiajia = goodsInfo.getIntValue("status") == -1;
                    if (mXiajia) {//下架
                        if (mXiajiaStatus != null && mXiajiaStatus.getVisibility() != View.VISIBLE) {
                            mXiajiaStatus.setVisibility(View.VISIBLE);
                        }
                    }
                    mShareIncome = goodsInfo.getString("share_income");
                }
            }
        });
    }


    private void tab(int index) {
        if (index == 0) {
            if (mGroupCommment.getVisibility() != View.GONE) {
                mGroupCommment.setVisibility(View.GONE);
            }
            if (mGroupDetail.getVisibility() != View.VISIBLE) {
                mGroupDetail.setVisibility(View.VISIBLE);
            }
        } else {
            if (mGroupCommment.getVisibility() != View.VISIBLE) {
                mGroupCommment.setVisibility(View.VISIBLE);
            }
            if (mGroupDetail.getVisibility() != View.GONE) {
                mGroupDetail.setVisibility(View.GONE);
            }
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (mViewPager != null && mViewPager.getCurrentItem() == 0 && mTitleAdapter != null) {
            mTitleAdapter.pausePlayVideo();
        }
        mPaused = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mPaused) {
            if (mViewPager != null && mViewPager.getCurrentItem() == 0 && mTitleAdapter != null) {
                mTitleAdapter.resumePlayVideo();
            }
        }
        mPaused = false;

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
        if (mTitleAdapter != null) {
            mTitleAdapter.release();
        }
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
        } else if (id == R.id.btn_choose_spec || id == R.id.btn_buy_now) {
            chooseSpec();
        } else if (id == R.id.btn_shop_home || id == R.id.btn_shop) {
            forwardShopHome();
        } else if (id == R.id.btn_kefu) {
            if (Constants.MALL_PLAT_UID.equals(mToUid)) {
                DialogUitl.showSimpleTipDialog(mContext, null, true, WordUtil.getString(com.livestreaming.common.R.string.mall_412), true);
            } else {
                forwardChat();
            }
        } else if (id == R.id.btn_comment_more) {
            GoodsCommentActivity.forward(mContext, mGoodsId);
        } else if (id == R.id.btn_collect) {
            clickCollect();
        } else if (id == R.id.btn_share) {
            shareGoods();
        }
    }

    /**
     * 选择规格
     */
    private void chooseSpec() {
        if (!mIsCanBuy) {
            ToastUtil.show(com.livestreaming.common.R.string.mall_307);
            return;
        }
        if (mXiajia) {
            ToastUtil.show(com.livestreaming.common.R.string.mall_403);
            return;
        }
        if (mSpecList == null || mSpecList.size() == 0) {
            return;
        }
        GoodsSpecDialogFragment fragment = new GoodsSpecDialogFragment();
        fragment.setSpecList(mSpecList);
        fragment.show(getSupportFragmentManager(), "GoodsSpecDialogFragment");
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
     * 下单
     */
    public void forwardMakeOrder(int countVal) {
        if (mSpecList == null || mSpecList.size() == 0) {
            return;
        }
        GoodsSpecBean specBean = null;
        for (GoodsChooseSpecBean bean : mSpecList) {
            if (bean.isChecked()) {
                specBean = bean;
                break;
            }
        }
        if (specBean == null) {
            return;
        }
        String shopName = mShopName.getText().toString();
        String goodsName = mGoodsName.getText().toString();
        GoodsMakeOrderActivity.forward(mContext, shopName, mGoodsId, goodsName, specBean, countVal, mPostageVal, mLiveUid, mShareUid);
    }


    private void showCollect(boolean isCollect) {
        if (mImgCollect != null) {
            mImgCollect.setImageDrawable(isCollect ? mBgCollect1 : mBgCollect0);
        }
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
        bundle.putString(Constants.TIP, StringUtil.contact(WordUtil.getString(com.livestreaming.common.R.string.money_symbol), mShareIncome));
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
