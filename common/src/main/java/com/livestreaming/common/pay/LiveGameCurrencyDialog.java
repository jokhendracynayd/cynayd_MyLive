package com.livestreaming.common.pay;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.livestreaming.common.CommonAppConfig;
import com.livestreaming.common.R;
import com.livestreaming.common.dialog.AbsDialogFragment;
import com.livestreaming.common.dialog.GameExchangeDiaog;
import com.livestreaming.common.dialog.GameWithdrawDiaog;
import com.livestreaming.common.http.CommonHttpUtil;
import com.livestreaming.common.http.HttpCallback;
import com.livestreaming.common.utils.ToastUtil;

public class LiveGameCurrencyDialog extends AbsDialogFragment {

    // @BindView(R.id.tv_account)
    public TextView mTvAccount;
    public boolean isFromHome = false;
    // @BindView(R.id.tv_diamond)
    public TextView mTvDiamond;
    //@BindView(R.id.tv_withdrawal)
    public TextView mTvWithdrawal;
    //@BindView(R.id.tv_exchange)
    public TextView mTvExchange;
    private double myCoins = 0;
    private ProgressBar progressBar;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mTvAccount = findViewById((R.id.tv_account));
        progressBar = findViewById(R.id.prog);
        findViewById(R.id.back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        progressBar.setVisibility(View.VISIBLE);
        getgGameCoins();
        mTvDiamond = findViewById((R.id.tv_diamond));
        mTvWithdrawal = findViewById((R.id.tv_withdrawal));
        mTvExchange = findViewById((R.id.tv_exchange));
        mTvDiamond.setText(String.valueOf(myCoins));
        mTvAccount.setText(CommonAppConfig.getInstance().getUserBean().getCoin());

        mTvWithdrawal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBindClick(v);
            }
        });
        mTvExchange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBindClick(v);
            }
        });
    }

    private void getgGameCoins() {
        CommonHttpUtil.getGamesCoins(new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                JSONObject obj = JSON.parseObject(info[0]);
                myCoins = obj.getDouble("balance");
                mTvDiamond.setText(String.valueOf(myCoins));
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    @Override
    protected int getLayoutId() {
        return R.layout.act_live_game_currency;
    }

    @Override
    protected int getDialogStyle() {
        return R.style.dialog2;
    }

    @Override
    protected boolean canCancel() {
        return true;
    }

    @Override
    protected void setWindowAttributes(Window window) {
        window.setWindowAnimations(R.style.bottomToTopAnim);
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        if (isFromHome)
            params.height = WindowManager.LayoutParams.MATCH_PARENT;
        else
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.BOTTOM;
        window.setAttributes(params);
    }

    public void onBindClick(View view) {
        int id = view.getId();
        if (id == R.id.tv_withdrawal) {
            GameWithdrawDiaog diaog = new GameWithdrawDiaog();
            diaog.gameConis = myCoins;
            diaog.onGameCoins = new GameExchangeDiaog.OnGameCoins() {
                @Override
                public void withdraw(double amount) {
                    LiveGameCurrencyDialog.this.withdraw(amount);
                }
            };
            diaog.show(getChildFragmentManager(), "");
        } else if (id == R.id.tv_exchange) {
            GameExchangeDiaog diaog = new GameExchangeDiaog();
            diaog.onGameCoins = new GameExchangeDiaog.OnGameCoins() {
                @Override
                public void exchange(double amount) {
                    LiveGameCurrencyDialog.this.exchange(amount);
                }
            };
            diaog.show(getChildFragmentManager(), "");
        }
    }

    private void withdraw(double amount) {
        progressBar.setVisibility(View.VISIBLE);
        CommonHttpUtil.withdrawGameCoins(String.valueOf(amount), new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                progressBar.setVisibility(View.GONE);
                ToastUtil.show(getString(R.string.done));
                JSONObject obj = JSON.parseObject(info[0]);

                double myBalanceCoins = obj.getJSONObject("dona_info").getDouble("coin");
                JSONObject objGame = obj.getJSONObject("games_info");
                CommonAppConfig.getInstance().getUserBean().setCoin("" + myCoins);
                mTvAccount.setText("" + myBalanceCoins);
                myCoins=objGame.getDouble("balance");
                mTvDiamond.setText("" + myCoins);
            }
        });
    }

    private void exchange(double amount) {
        progressBar.setVisibility(View.VISIBLE);
        CommonHttpUtil.exchangeCoinsToGameCoins(String.valueOf(amount), new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                progressBar.setVisibility(View.GONE);
                ToastUtil.show(getString(R.string.done));
                JSONObject obj = JSON.parseObject(info[0]);
                double myCoin = obj.getJSONObject("dona_info").getDouble("coin");
                JSONObject objGame = obj.getJSONObject("games_info");
                mTvAccount.setText("" + myCoin);
                myCoins=objGame.getDouble("balance");
                mTvDiamond.setText("" +myCoins);
            }
        });
    }

}

