package com.livestreaming.mall.adapter;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.livestreaming.common.adapter.RefreshAdapter;
import com.livestreaming.common.glide.ImgLoader;
import com.livestreaming.common.utils.StringUtil;
import com.livestreaming.common.utils.WordUtil;
import com.livestreaming.mall.R;
import com.livestreaming.mall.bean.SellerOrderBean;

public abstract class SellerOrderBaseAdapter extends RefreshAdapter<SellerOrderBean> {

    private String mOrderNoString;
    private String mBuyerNameString;
    protected String mMoneySymbol;
    private View.OnClickListener mItemClickListener;
    protected ActionListener mActionListener;

    public SellerOrderBaseAdapter(Context context) {
        super(context);
        mOrderNoString = WordUtil.getString(com.livestreaming.common.R.string.mall_206);
        mBuyerNameString = WordUtil.getString(com.livestreaming.common.R.string.mall_207);
        mMoneySymbol = WordUtil.getString(com.livestreaming.common.R.string.money_symbol);
        mItemClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Object tag = v.getTag();
                if (tag != null && mActionListener != null) {
                    mActionListener.onItemClick((SellerOrderBean) tag);
                }
            }
        };
    }

    public void setActionListener(ActionListener actionListener) {
        mActionListener = actionListener;
    }


    public class BaseVh extends RecyclerView.ViewHolder {

        TextView mOrderNo;
        TextView mStatusTip;
        ImageView mGoodsThumb;
        TextView mGoodsName;
        TextView mGoodsPrice;
        TextView mGoodsSpecName;
        TextView mGoodsNum;
        TextView mBuyerName;

        public BaseVh(@NonNull View itemView) {
            super(itemView);
            mOrderNo = itemView.findViewById(R.id.order_no);
            mStatusTip = itemView.findViewById(R.id.status_tip);
            mGoodsThumb = itemView.findViewById(R.id.goods_thumb);
            mGoodsName = itemView.findViewById(R.id.goods_name);
            mGoodsPrice = itemView.findViewById(R.id.goods_price);
            mGoodsSpecName = itemView.findViewById(R.id.goods_spec_name);
            mGoodsNum = itemView.findViewById(R.id.goods_num);
            mBuyerName = itemView.findViewById(R.id.buyer_name);
            itemView.setOnClickListener(mItemClickListener);
        }

        public void setData(SellerOrderBean bean) {
            itemView.setTag(bean);
            mOrderNo.setText(String.format(mOrderNoString, bean.getOrderNo()));
            mStatusTip.setText(bean.getStatusTip());
            ImgLoader.display(mContext, bean.getGoodsSpecThumb(), mGoodsThumb);
            mGoodsName.setText(bean.getGoodsName());
            mGoodsPrice.setText(StringUtil.contact(mMoneySymbol, bean.getGoodsPrice()));
            mGoodsSpecName.setText(bean.getGoodsSpecName());
            mGoodsNum.setText(StringUtil.contact("x", bean.getGoodsNum()));
            mBuyerName.setText(String.format(mBuyerNameString, bean.getBuyerName()));
        }
    }

    public interface ActionListener {

        /**
         * 点击item
         */
        void onItemClick(SellerOrderBean bean);

        /**
         * 去发货
         */
        void onSendClick(SellerOrderBean bean);

        /**
         * 删除订单
         */
        void onDeleteClick(SellerOrderBean bean);

        /**
         * 查看物流
         */
        void onWuLiuClick(SellerOrderBean bean);

        /**
         * 退款详情
         */
        void onRefundClick(SellerOrderBean bean);

        /**
         * 联系买家
         */
        void onContactBuyerClick(SellerOrderBean bean);
    }

}
