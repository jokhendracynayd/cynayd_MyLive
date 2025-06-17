package com.livestreaming.mall.views;

import android.content.Context;
import android.view.ViewGroup;

import com.livestreaming.mall.activity.SellerSendActivity;
import com.livestreaming.mall.adapter.SellerOrderBaseAdapter;
import com.livestreaming.mall.adapter.SellerOrderSendAdapter;
import com.livestreaming.mall.bean.SellerOrderBean;

/**
 * 卖家 订单列表 待发货
 */
public class SellerOrderSendViewHolder extends AbsSellerOrderViewHolder {

    public SellerOrderSendViewHolder(Context context, ViewGroup parentView) {
        super(context, parentView);
    }

    @Override
    public String getOrderType() {
        return "wait_shipment";
    }

    @Override
    public SellerOrderBaseAdapter getSellerOrderAdapter() {
        return new SellerOrderSendAdapter(mContext);
    }

    /**
     * 点击item
     */
    @Override
    public void onItemClick(SellerOrderBean bean) {
        SellerSendActivity.forward(mContext, bean.getId());
    }

}
