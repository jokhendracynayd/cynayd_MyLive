package com.livestreaming.mall.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;

import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.livestreaming.common.Constants;
import com.livestreaming.common.activity.AbsActivity;
import com.livestreaming.common.adapter.ViewPagerAdapter;
import com.livestreaming.common.utils.DpUtil;
import com.livestreaming.common.utils.LanguageUtil;
import com.livestreaming.common.utils.StringUtil;
import com.livestreaming.common.utils.WordUtil;
import com.livestreaming.im.http.ImHttpConsts;
import com.livestreaming.mall.R;
import com.livestreaming.mall.http.MallHttpConsts;
import com.livestreaming.mall.http.MallHttpUtil;
import com.livestreaming.mall.views.AbsSellerOrderViewHolder;
import com.livestreaming.mall.views.SellerOrderAllRefundViewHolder;
import com.livestreaming.mall.views.SellerOrderAllViewHolder;
import com.livestreaming.mall.views.SellerOrderClosedViewHolder;
import com.livestreaming.mall.views.SellerOrderCommentViewHolder;
import com.livestreaming.mall.views.SellerOrderFinishViewHolder;
import com.livestreaming.mall.views.SellerOrderPayViewHolder;
import com.livestreaming.mall.views.SellerOrderReceiveViewHolder;
import com.livestreaming.mall.views.SellerOrderRefundViewHolder;
import com.livestreaming.mall.views.SellerOrderSendViewHolder;

import net.lucode.hackware.magicindicator.MagicIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.ColorTransitionPagerTitleView;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.SimplePagerTitleView;

import java.util.ArrayList;
import java.util.List;

/**
 * 卖家 订单列表
 */
public class SellerOrderActivity extends AbsActivity implements View.OnClickListener {

    public static void forward(Context context, int index) {
        Intent intent = new Intent(context, SellerOrderActivity.class);
        intent.putExtra(Constants.MALL_ORDER_INDEX, index);
        context.startActivity(intent);
    }

    private static final int PAGE_COUNT = 10;
    private List<FrameLayout> mViewList;
    private MagicIndicator mIndicator;
    private AbsSellerOrderViewHolder[] mViewHolders;
    private ViewPager mViewPager;
    private boolean mPaused;

    private TextView mTvSend;
    private TextView mTvRefund;
    private TextView mTvPay;
    private TextView mTvOther;

    private String mWaitSendString;//待发货
    private String mWaitRefundString;//待退款
    private String mWaitPayString;//待支付
    private String mOtherString;//其他
    private String mAllRefundString;//全部退款
    private String mSendString;//已发货
    private String mReceiveString;//已签收
    private String mFinishString;//交易成功
    private String mClosedString;//已关闭
    private String mAllString;//全部

    private View mDialog;
    private View mShadow;
    private View mBtnDismiss;
    private ObjectAnimator mShowAnimator;
    private ObjectAnimator mHideAnimator;
    private View mArrow;//箭头

    private int mPageIndex;
    private int mTabIndex;
    private boolean mDialogShowed;


    @Override
    protected int getLayoutId() {
        return R.layout.activity_seller_order_activity;
    }

    @Override
    protected void main() {
        setTitle(WordUtil.getString(com.livestreaming.common.R.string.mall_073));
        mWaitSendString = WordUtil.getString(com.livestreaming.common.R.string.mall_008);
        mWaitRefundString = WordUtil.getString(com.livestreaming.common.R.string.mall_204);
        mWaitPayString = WordUtil.getString(com.livestreaming.common.R.string.mall_203);
        mOtherString = WordUtil.getString(com.livestreaming.common.R.string.mall_205);
        mAllRefundString = WordUtil.getString(com.livestreaming.common.R.string.mall_213);
        mSendString = WordUtil.getString(com.livestreaming.common.R.string.mall_214);
        mReceiveString = WordUtil.getString(com.livestreaming.common.R.string.mall_215);
        mFinishString = WordUtil.getString(com.livestreaming.common.R.string.mall_216);
        mClosedString = WordUtil.getString(com.livestreaming.common.R.string.mall_217);
        mAllString = WordUtil.getString(com.livestreaming.common.R.string.all);

        mDialog = findViewById(R.id.dialog);
        mShadow = findViewById(R.id.shadow);
        mBtnDismiss = findViewById(R.id.btn_dismiss);
        mBtnDismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (canClick()) {
                    hideDialog();
                }
            }
        });
        mDialog.post(new Runnable() {
            @Override
            public void run() {
                initAnim();
            }
        });
        mArrow = findViewById(R.id.arrow);
        findViewById(R.id.btn_all_refund).setOnClickListener(this);
        findViewById(R.id.btn_send).setOnClickListener(this);
        findViewById(R.id.btn_receive).setOnClickListener(this);
        findViewById(R.id.btn_finish).setOnClickListener(this);
        findViewById(R.id.btn_close).setOnClickListener(this);
        findViewById(R.id.btn_all).setOnClickListener(this);


        mViewList = new ArrayList<>();
        for (int i = 0; i < PAGE_COUNT; i++) {
            FrameLayout frameLayout = new FrameLayout(mContext);
            frameLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            mViewList.add(frameLayout);
        }
        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        if (PAGE_COUNT > 1) {
            mViewPager.setOffscreenPageLimit(PAGE_COUNT - 1);
        }
        mViewPager.setAdapter(new ViewPagerAdapter(mViewList));
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                loadPageData(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mViewHolders = new AbsSellerOrderViewHolder[PAGE_COUNT];
        mIndicator = (MagicIndicator) findViewById(R.id.indicator);
        if (!LanguageUtil.isZh()) {
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) mIndicator.getLayoutParams();
            lp.rightMargin = DpUtil.dp2px(20);
            mIndicator.setLayoutParams(lp);
        }
        final String[] titles = new String[]{
                mWaitSendString,
                mWaitRefundString,
                mWaitPayString,
                mOtherString
        };
        CommonNavigator commonNavigator = new CommonNavigator(mContext);
        commonNavigator.setAdapter(new CommonNavigatorAdapter() {

            @Override
            public int getCount() {
                return titles.length;
            }

            @Override
            public IPagerTitleView getTitleView(Context context, final int index) {
                SimplePagerTitleView simplePagerTitleView = new ColorTransitionPagerTitleView(context);
                if (LanguageUtil.isZh()) {
                    simplePagerTitleView.setPadding(0, 0, 0, 0);
                }
                simplePagerTitleView.setNormalColor(ContextCompat.getColor(mContext, com.livestreaming.common.R.color.gray1));
                simplePagerTitleView.setSelectedColor(ContextCompat.getColor(mContext, com.livestreaming.common.R.color.textColor));
                simplePagerTitleView.setText(titles[index]);
                simplePagerTitleView.setTextSize(14);
                simplePagerTitleView.getPaint().setFakeBoldText(true);
                simplePagerTitleView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (index == 3) {
                            showDialog();
                        } else {
                            setTabIndex(index);
                            setPageIndex(index);
                        }
                    }
                });

                if (index == 0) {
                    mTvSend = simplePagerTitleView;
                } else if (index == 1) {
                    mTvRefund = simplePagerTitleView;
                } else if (index == 2) {
                    mTvPay = simplePagerTitleView;
                } else if (index == 3) {
                    mTvOther = simplePagerTitleView;
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
        if (LanguageUtil.isZh()) {
            commonNavigator.setAdjustMode(true);
        }
        mIndicator.setNavigator(commonNavigator);
        int index = getIntent().getIntExtra(Constants.MALL_ORDER_INDEX, 0);
        if (index == 0) {
            loadPageData(0);
        } else {
            setTabIndex(index);
            mPageIndex = index;
            mViewPager.setCurrentItem(index, false);
        }

    }

    /**
     * 初始化弹窗动画
     */
    private void initAnim() {
        final int height = mDialog.getHeight();
        mDialog.setTranslationY(-height);
        mShowAnimator = ObjectAnimator.ofFloat(mDialog, "translationY", 0);
        mShowAnimator.setDuration(200);
        mHideAnimator = ObjectAnimator.ofFloat(mDialog, "translationY", -height);
        mHideAnimator.setDuration(200);
        TimeInterpolator interpolator = new AccelerateDecelerateInterpolator();
        mShowAnimator.setInterpolator(interpolator);
        mHideAnimator.setInterpolator(interpolator);
        ValueAnimator.AnimatorUpdateListener updateListener = new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float rate = 1 + ((float) animation.getAnimatedValue() / height);
                mShadow.setAlpha(rate);
            }
        };
        mShowAnimator.addUpdateListener(updateListener);
        mHideAnimator.addUpdateListener(updateListener);
        mHideAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (mBtnDismiss != null && mBtnDismiss.getVisibility() == View.VISIBLE) {
                    mBtnDismiss.setVisibility(View.INVISIBLE);
                }
                mDialogShowed = false;
            }
        });
    }


    /**
     * 显示弹窗
     */
    private void showDialog() {
        if (mDialogShowed) {
            return;
        }
        mDialogShowed = true;
        if (mArrow != null) {
            mArrow.setRotation(-90);
        }
        if (mBtnDismiss != null && mBtnDismiss.getVisibility() != View.VISIBLE) {
            mBtnDismiss.setVisibility(View.VISIBLE);
        }
        if (mShowAnimator != null) {
            mShowAnimator.start();
        }
    }

    /**
     * 隐藏弹窗
     */
    private void hideDialog() {
        if (mArrow != null) {
            mArrow.setRotation(90);
        }
        if (mShowAnimator != null) {
            mShowAnimator.cancel();
        }
        if (mHideAnimator != null) {
            mHideAnimator.start();
        }
    }


    private void loadPageData(int position) {
        if (mViewHolders == null) {
            return;
        }
        AbsSellerOrderViewHolder vh = mViewHolders[position];
        if (vh == null) {
            if (mViewList != null && position < mViewList.size()) {
                FrameLayout parent = mViewList.get(position);
                if (parent == null) {
                    return;
                }
                if (position == 0) {
                    vh = new SellerOrderSendViewHolder(mContext, parent);
                } else if (position == 1) {
                    vh = new SellerOrderRefundViewHolder(mContext, parent);
                } else if (position == 2) {
                    vh = new SellerOrderPayViewHolder(mContext, parent);
                } else if (position == 4) {
                    vh = new SellerOrderAllRefundViewHolder(mContext, parent);
                } else if (position == 5) {
                    vh = new SellerOrderReceiveViewHolder(mContext, parent);
                } else if (position == 6) {
                    vh = new SellerOrderCommentViewHolder(mContext, parent);
                } else if (position == 7) {
                    vh = new SellerOrderFinishViewHolder(mContext, parent);
                } else if (position == 8) {
                    vh = new SellerOrderClosedViewHolder(mContext, parent);
                } else if (position == 9) {
                    vh = new SellerOrderAllViewHolder(mContext, parent);
                }
                if (vh == null) {
                    return;
                }
                mViewHolders[position] = vh;
                vh.addToParent();
                vh.subscribeActivityLifeCycle();
            }
        }
        if (vh != null) {
            vh.loadData();
        }
    }

    @Override
    protected void onPause() {
        mPaused = true;
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mPaused) {
            if (mViewPager != null && mViewHolders != null) {
                AbsSellerOrderViewHolder vh = mViewHolders[mViewPager.getCurrentItem()];
                if (vh != null) {
                    vh.refreshData();
                }
            }
        }
        mPaused = false;
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_all_refund) {//全部退款订单
            setTabIndex(3);
            setPageIndex(4);
        } else if (id == R.id.btn_send) {//已发货订单
            setTabIndex(3);
            setPageIndex(5);
        } else if (id == R.id.btn_receive) {//已签收订单
            setTabIndex(3);
            setPageIndex(6);
        } else if (id == R.id.btn_finish) {//已完成订单
            setTabIndex(3);
            setPageIndex(7);
        } else if (id == R.id.btn_close) {//已关闭订单
            setTabIndex(3);
            setPageIndex(8);
        } else if (id == R.id.btn_all) {//全部订单
            setTabIndex(3);
            setPageIndex(9);
        }
    }

    private void setPageIndex(int index) {
        hideDialog();
        if (mPageIndex != index) {
            mPageIndex = index;
            if (mViewPager != null) {
                mViewPager.setCurrentItem(index, false);
            }
        }
    }


    private void setTabIndex(int index) {
        if (index > 3) {
            index = 3;
        }
        if (mTabIndex != index) {
            mTabIndex = index;
            mIndicator.onPageScrollStateChanged(2);
            mIndicator.onPageSelected(index);
            mIndicator.onPageScrolled(index, 0, 0);
            mIndicator.onPageScrollStateChanged(0);
        }
    }

    /**
     * 显示订单数量
     */
    public void setOrderNum(String nums) {
        JSONObject obj = JSON.parseObject(nums);
        if (mTvSend != null) {
            mTvSend.setText(StringUtil.contact(mWaitSendString, " ", obj.getString("wait_shipment_nums")));
        }
        if (mTvRefund != null) {
            mTvRefund.setText(StringUtil.contact(mWaitRefundString, " ", obj.getString("wait_refund_nums")));
        }
        if (mTvPay != null) {
            mTvPay.setText(StringUtil.contact(mWaitPayString, " ", obj.getString("wait_payment_nums")));
        }
        if (mPageIndex == 4) {
            mTvOther.setText(StringUtil.contact(mAllRefundString, " ", obj.getString("all_refund_nums")));
        } else if (mPageIndex == 5) {
            mTvOther.setText(StringUtil.contact(mSendString, " ", obj.getString("wait_receive_nums")));
        } else if (mPageIndex == 6) {
            mTvOther.setText(StringUtil.contact(mReceiveString, " ", obj.getString("wait_evaluate_nums")));
        } else if (mPageIndex == 7) {
            mTvOther.setText(StringUtil.contact(mFinishString, obj.getString("finished_nums")));
        } else if (mPageIndex == 8) {
            mTvOther.setText(StringUtil.contact(mClosedString, " ", obj.getString("closed_nums")));
        } else if (mPageIndex == 9) {
            mTvOther.setText(StringUtil.contact(mAllString, " ", obj.getString("all_nums")));
        }
    }


    @Override
    protected void onDestroy() {
        MallHttpUtil.cancel(MallHttpConsts.GET_SELLER_ORDER_LIST);
        MallHttpUtil.cancel(MallHttpConsts.SELLER_DELETE_ORDER);
        MallHttpUtil.cancel(ImHttpConsts.GET_IM_USER_INFO);
        if (mHideAnimator != null) {
            mHideAnimator.cancel();
            mHideAnimator.removeAllUpdateListeners();
            mHideAnimator.removeAllListeners();
        }
        if (mShowAnimator != null) {
            mShowAnimator.cancel();
            mShowAnimator.removeAllUpdateListeners();
            mShowAnimator.removeAllListeners();
        }
        mShowAnimator = null;
        mHideAnimator = null;
        super.onDestroy();
    }

}
