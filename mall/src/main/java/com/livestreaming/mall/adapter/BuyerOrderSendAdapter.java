package com.livestreaming.mall.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.ViewGroup;

import com.livestreaming.mall.R;

/**
 * 买家 订单列表 待发货
 */
public class BuyerOrderSendAdapter extends BuyerOrderBaseAdapter {


    public BuyerOrderSendAdapter(Context context) {
        super(context);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new BaseVh(mInflater.inflate(R.layout.item_buyer_order_send, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder vh, int i) {
        ((BaseVh) vh).setData(mList.get(i));
    }
}
