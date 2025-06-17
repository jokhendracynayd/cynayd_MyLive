package com.livestreaming.mall.views;

import android.content.Context;
import android.view.ViewGroup;

import com.livestreaming.mall.adapter.SellerOrderBaseAdapter;
import com.livestreaming.mall.adapter.SellerOrderReceiveAdapter;

/**
 * 卖家 订单列表 已发货，待收货
 */
public class SellerOrderReceiveViewHolder extends AbsSellerOrderViewHolder {

    public SellerOrderReceiveViewHolder(Context context, ViewGroup parentView) {
        super(context, parentView);
    }

    @Override
    public String getOrderType() {
        return "wait_receive";
    }

    @Override
    public SellerOrderBaseAdapter getSellerOrderAdapter() {
        return new SellerOrderReceiveAdapter(mContext);
    }

}
