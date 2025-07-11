package com.livestreaming.mall.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.livestreaming.common.utils.WordUtil;
import com.livestreaming.mall.R;
import com.livestreaming.mall.bean.SellerOrderBean;

/**
 * 卖家 订单列表 全部退款
 */
public class SellerOrderAllRefundAdapter extends SellerOrderBaseAdapter {

    private View.OnClickListener mDeleteClickListener;
    private String mRefundString;
    private String mRefundString0;
    private String mRefundString1;

    public SellerOrderAllRefundAdapter(Context context) {
        super(context);
        mMoneySymbol = WordUtil.getString(com.livestreaming.common.R.string.money_symbol);
        mRefundString = WordUtil.getString(com.livestreaming.common.R.string.mall_212);
        mRefundString0 = WordUtil.getString(com.livestreaming.common.R.string.mall_255);
        mRefundString1 = WordUtil.getString(com.livestreaming.common.R.string.mall_256);
        mDeleteClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Object tag = v.getTag();
                if (tag != null && mActionListener != null) {
                    mActionListener.onDeleteClick((SellerOrderBean) tag);
                }
            }
        };
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new Vh(mInflater.inflate(R.layout.item_seller_order_all_refund, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder vh, int i) {
        ((Vh) vh).setData(mList.get(i));
    }

    class Vh extends BaseVh {

        TextView mRefundTip;
        View mBtnDelete;

        public Vh(@NonNull View itemView) {
            super(itemView);
            mRefundTip = itemView.findViewById(R.id.refund_tip);
            mBtnDelete = itemView.findViewById(R.id.btn_delete_order);
            mBtnDelete.setOnClickListener(mDeleteClickListener);
        }

        @Override
        public void setData(SellerOrderBean bean) {
            mRefundTip.setText(String.format(mRefundString, bean.getRefundType() == 0 ? mRefundString0 : mRefundString1, mMoneySymbol, bean.getTotalPrice()));
            mBtnDelete.setTag(bean);
            super.setData(bean);
        }
    }
}
