package com.livestreaming.common.event;

/**
 * Created by cxf on 2018/11/1.
 * 钻石数量变化的事件
 */

public class CoinChangeEvent {

    private String coin;
    private boolean chargeSuccess;

    public CoinChangeEvent(String coin) {
        this.coin = coin;
    }

    public CoinChangeEvent(String coin, boolean chargeSuccess) {
        this.coin = coin;
        this.chargeSuccess = chargeSuccess;
    }

    public String getCoin() {
        return coin;
    }

    public void setCoin(String coin) {
        this.coin = coin;
    }

    public boolean isChargeSuccess() {
        return chargeSuccess;
    }

    public void setChargeSuccess(boolean chargeSuccess) {
        this.chargeSuccess = chargeSuccess;
    }

}
