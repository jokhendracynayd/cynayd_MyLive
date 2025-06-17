package com.livestreaming.mall.views;

import android.content.Context;
import android.view.ViewGroup;

import com.livestreaming.mall.adapter.BuyerOrderBaseAdapter;
import com.livestreaming.mall.adapter.BuyerOrderSendAdapter;

/**
 * 买家 订单列表 待发货
 */
public class BuyerOrderSendViewHolder extends AbsBuyerOrderViewHolder {

    public BuyerOrderSendViewHolder(Context context, ViewGroup parentView) {
        super(context, parentView);
    }

    @Override
    public String getOrderType() {
        return "wait_shipment";
    }

    @Override
    public BuyerOrderBaseAdapter getBuyerOrderAdapter() {
        return new BuyerOrderSendAdapter(mContext);
    }


}
