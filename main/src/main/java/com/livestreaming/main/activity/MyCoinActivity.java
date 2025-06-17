package com.livestreaming.main.activity;

import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.livestreaming.common.CommonAppConfig;
import com.livestreaming.common.Constants;
import com.livestreaming.common.HtmlConfig;
import com.livestreaming.common.activity.AbsActivity;
import com.livestreaming.common.activity.WebViewActivity;
import com.livestreaming.common.bean.CoinBean;
import com.livestreaming.common.bean.CoinPayBean;
import com.livestreaming.common.custom.ItemDecoration;
import com.livestreaming.common.dialog.ExchangeDiaog;
import com.livestreaming.common.dialog.FirstChargeDialogFragment;
import com.livestreaming.common.event.CoinChangeEvent;
import com.livestreaming.common.event.FirstChargeEvent;
import com.livestreaming.common.http.CommonHttpConsts;
import com.livestreaming.common.http.CommonHttpUtil;
import com.livestreaming.common.http.HttpCallback;
import com.livestreaming.common.interfaces.OnItemClickListener;
import com.livestreaming.common.pay.PayCallback;
import com.livestreaming.common.pay.PayPresenter;
import com.livestreaming.common.utils.StringUtil;
import com.livestreaming.common.utils.ToastUtil;
import com.livestreaming.common.utils.WordUtil;
import com.livestreaming.main.R;
import com.livestreaming.main.adapter.CoinAdapter;
import com.livestreaming.main.adapter.CoinPayAdapter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by cxf on 2018/10/23.
 * 充值
 */
public class MyCoinActivity extends AbsActivity implements View.OnClickListener {

    private SwipeRefreshLayout mRefreshLayout;
    private RecyclerView mRecyclerView;
    private RecyclerView mPayRecyclerView;
    private CoinAdapter mAdapter;
    private CoinPayAdapter mPayAdapter;
    private TextView mBalance;
    private TextView btn_exchange;
    private long mBalanceValue;
    private boolean mFirstLoad = true;
    private PayPresenter mPayPresenter;
    private String mCoinName;
    //    private TextView mTip1;
//    private TextView mTip2;
    private TextView mCoin2;
    private TextView mBtnCharge;
    private boolean mIsPayPal;


    @Override
    protected int getLayoutId() {
        return R.layout.activity_coin;
    }

    @Override
    protected void main() {
        setTitle(WordUtil.getString(com.livestreaming.common.R.string.wallet));
        mBtnCharge = findViewById(R.id.btn_charge);
        btn_exchange = findViewById(R.id.btn_exchange);
        mBtnCharge.setOnClickListener(this);
        btn_exchange.setOnClickListener(this);
        findViewById(R.id.btn_charge_first).setOnClickListener(this);
        findViewById(R.id.btn_charge_detail).setOnClickListener(this);
        mCoin2 = findViewById(R.id.coin_2);
        mRefreshLayout = findViewById(R.id.refreshLayout);
        mRefreshLayout.setColorSchemeResources(com.livestreaming.common.R.color.blue3);
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadData();
            }
        });
        mCoinName = CommonAppConfig.getInstance().getCoinName();
        mBalance = findViewById(R.id.coin);
        TextView coinNameTextView = findViewById(R.id.coin_name);
        coinNameTextView.setText(String.format(WordUtil.getString(com.livestreaming.common.R.string.wallet_coin_name), mCoinName));
        TextView scoreName = findViewById(R.id.score_name);
        scoreName.setText(String.format(WordUtil.getString(com.livestreaming.common.R.string.wallet_coin_name), CommonAppConfig.getInstance().getScoreName()));
        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new GridLayoutManager(mContext, 3, GridLayoutManager.VERTICAL, false));
        ItemDecoration decoration = new ItemDecoration(mContext, 0x00000000, 5, 10);
        decoration.setOnlySetItemOffsetsButNoDraw(true);
        mRecyclerView.addItemDecoration(decoration);
        mAdapter = new CoinAdapter(mContext, mCoinName);
        mAdapter.setOnItemClickListener(new OnItemClickListener<CoinBean>() {
            @Override
            public void onItemClick(CoinBean bean, int position) {
                if (bean != null && mBtnCharge != null) {
                    String money = StringUtil.contact("$", bean.getMoney());
                    mBtnCharge.setText(String.format(WordUtil.getString(com.livestreaming.common.R.string.chat_charge_tip), money));
                }
            }
        });
//        mAdapter.setContactView(findViewById(R.id.top));
        mRecyclerView.setAdapter(mAdapter);
        findViewById(R.id.btn_tip).setOnClickListener(this);
//        View headView = mAdapter.getHeadView();
        mPayRecyclerView = findViewById(R.id.pay_recyclerView);
        ItemDecoration decoration2 = new ItemDecoration(mContext, 0x00000000, 14, 10);
        decoration2.setOnlySetItemOffsetsButNoDraw(true);
        mPayRecyclerView.addItemDecoration(decoration2);
        mPayRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        mPayAdapter = new CoinPayAdapter(mContext);
        mPayAdapter.setOnItemClickListener(new OnItemClickListener<CoinPayBean>() {
            @Override
            public void onItemClick(CoinPayBean bean, int position) {
                boolean isPayPal = Constants.PAY_TYPE_PAYPAL.equals(bean.getId());
                if (mIsPayPal != isPayPal) {
                    if (mAdapter != null) {
                        CoinBean coinBean = mAdapter.getCheckedBean();
                        if (coinBean != null && mBtnCharge != null) {
                            String money = StringUtil.contact("$", coinBean.getMoney());
                            mBtnCharge.setText(String.format(WordUtil.getString(com.livestreaming.common.R.string.chat_charge_tip), money));
                        }
                    }
                }
                mIsPayPal = isPayPal;
                if (mAdapter != null) {
                    mAdapter.setIsPaypal(isPayPal);
                }
            }
        });
        mPayRecyclerView.setAdapter(mPayAdapter);
        mPayPresenter = new PayPresenter(this);
        mPayPresenter.setServiceNamePaypal(Constants.PAY_BUY_COIN_PAYPAL);
        mPayPresenter.setPayCallback(new PayCallback() {
            @Override
            public void onSuccess() {
                if (mPayPresenter != null) {
                    mPayPresenter.checkPayResult();
                }
            }

            @Override
            public void onFailed() {

            }
        });
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mFirstLoad) {
            mFirstLoad = false;
            loadData();
        }
    }

    private void loadData() {
        CommonHttpUtil.getBalance(new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0 && info.length > 0) {
                    JSONObject obj = JSON.parseObject(info[0]);
                    String coin = obj.getString("coin");
                    mBalanceValue = Long.parseLong(coin);
                    mBalance.setText(coin);
//                    mTip1.setText(obj.getString("tip_t"));
//                    mTip2.setText(obj.getString("tip_d"));
                    mCoin2.setText(obj.getString("score"));
                    List<CoinPayBean> payList = JSON.parseArray(obj.getString("paylist"), CoinPayBean.class);
                    if (mPayAdapter != null) {
                        mPayAdapter.setList(payList);
                    }
                    List<CoinBean> list = JSON.parseArray(obj.getString("rules"), CoinBean.class);
                    if (mAdapter != null) {
                        mAdapter.setList(list);
                        if (payList != null && payList.size() > 0) {
                            mIsPayPal = Constants.PAY_TYPE_PAYPAL.equals(payList.get(0).getId());
                            mAdapter.setIsPaypal(mIsPayPal);
                        }
                        CoinBean coinBean = mAdapter.getCheckedBean();
                        if (coinBean != null && mBtnCharge != null) {
                            String money = StringUtil.contact("$", coinBean.getMoney());
                            mBtnCharge.setText(String.format(WordUtil.getString(com.livestreaming.common.R.string.chat_charge_tip), money));
                        }
                    }
                    if (mPayPresenter != null) {
                        mPayPresenter.setBalanceValue(mBalanceValue);
                    }
                }
            }

            @Override
            public void onFinish() {
                if (mRefreshLayout != null) {
                    mRefreshLayout.setRefreshing(false);
                }
            }
        });
    }

    /**
     * 充值
     */
    private void charge() {
        if (mAdapter == null) {
            return;
        }
        if (mPayPresenter == null) {
            return;
        }
        if (mPayAdapter == null) {
            return;
        }
        CoinBean bean = mAdapter.getCheckedBean();
        if (bean == null) {
            return;
        }
        CoinPayBean coinPayBean = mPayAdapter.getPayCoinPayBean();
        if (coinPayBean == null) {
            ToastUtil.show(com.livestreaming.common.R.string.wallet_tip_5);
            return;
        }
        String href = coinPayBean.getHref();
        if (TextUtils.isEmpty(href)) {
            String money = bean.getMoney();
            String coin = Constants.PAY_TYPE_PAYPAL.equals(coinPayBean.getId()) ? bean.getCoinPaypal() : bean.getCoin();
            String goodsName = StringUtil.contact(coin, mCoinName);
            Map<String, String> orderParams = new HashMap<>();
            orderParams.put("uid", CommonAppConfig.getInstance().getUid());
            orderParams.put("token", CommonAppConfig.getInstance().getToken());
            orderParams.put("money", money);
            orderParams.put("changeid", bean.getId());
            orderParams.put("coin", coin);
            mPayPresenter.pay(coinPayBean.getId(), money, goodsName, orderParams);
        } else {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(href));
            mContext.startActivity(intent);
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCoinChangeEvent(CoinChangeEvent e) {
        if (mBalance != null) {
            mBalance.setText(e.getCoin());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFirstChargeEvent(FirstChargeEvent e) {
        loadData();
    }


    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_tip) {
            WebViewActivity.forward(mContext, HtmlConfig.CHARGE_PRIVCAY);
        } else if (i == R.id.btn_charge) {
            charge();
        } else if (i == R.id.btn_charge_first) {
            FirstChargeDialogFragment fragment = new FirstChargeDialogFragment();
            fragment.show(getSupportFragmentManager(), "FirstChargeDialogFragment");
        } else if (i == R.id.btn_charge_detail) {
            WebViewActivity.forward(mContext, HtmlConfig.CHARGE_DETAIL);
        }else if (i == R.id.btn_exchange) {
            showExchangeDialog();
        }
    }
    private void showExchangeDialog() {
        ExchangeDiaog diaog= new ExchangeDiaog();
        diaog.show(getSupportFragmentManager(),"");
    }
    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        CommonHttpUtil.cancel(CommonHttpConsts.GET_BALANCE);
        CommonHttpUtil.cancel(CommonHttpConsts.GET_ALI_ORDER);
        CommonHttpUtil.cancel(CommonHttpConsts.GET_WX_ORDER);
        if (mRefreshLayout != null) {
            mRefreshLayout.setOnRefreshListener(null);
        }
        mRefreshLayout = null;
        if (mPayPresenter != null) {
            mPayPresenter.release();
        }
        mPayPresenter = null;
        super.onDestroy();
    }

}
