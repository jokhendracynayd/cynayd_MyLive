package com.livestreaming.main.adapter;

import android.content.Context;
import android.graphics.Paint;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.livestreaming.common.adapter.RefreshAdapter;
import com.livestreaming.common.glide.ImgLoader;
import com.livestreaming.common.utils.StringUtil;
import com.livestreaming.common.utils.WordUtil;
import com.livestreaming.main.R;
import com.livestreaming.mall.activity.GoodsDetailActivity;
import com.livestreaming.mall.bean.GoodsSimpleBean;

public class MallSearchAdapter extends RefreshAdapter<GoodsSimpleBean> {

    private static final int LEFT = 1;
    private static final int RIGHT = 2;
    private View.OnClickListener mOnClickListener;
    private String mSaleString;
    private String mMoneySymbol;
    private boolean mLayoutLinear;

    public MallSearchAdapter(Context context) {
        super(context);
        mSaleString = WordUtil.getString(com.livestreaming.common.R.string.mall_114);
        mMoneySymbol = WordUtil.getString(com.livestreaming.common.R.string.money_symbol);
        mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!canClick()) {
                    return;
                }
                GoodsSimpleBean bean = (GoodsSimpleBean) v.getTag();
                GoodsDetailActivity.forward(mContext, bean.getId(), false, bean.getType());
            }
        };
    }

    public void setLayoutLinear(boolean layoutLinear) {
        mLayoutLinear = layoutLinear;
    }


    @Override
    public int getItemViewType(int position) {
        if (mLayoutLinear) {
            return 0;
        }
        if (position % 2 == 1) {
            return RIGHT;
        }
        return LEFT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        if (mLayoutLinear) {
            return new Vh(mInflater.inflate(com.livestreaming.mall.R.layout.item_shop_home_linear, viewGroup, false));
        }
        if (viewType == LEFT) {
            return new Vh(mInflater.inflate(com.livestreaming.mall.R.layout.item_shop_home_left, viewGroup, false));
        }
        return new Vh(mInflater.inflate(com.livestreaming.mall.R.layout.item_shop_home_right, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder vh, int position) {
        if (vh instanceof Vh) {
            ((Vh) vh).setData(mList.get(position));
        }
    }

    class Vh extends RecyclerView.ViewHolder {

        ImageView mThumb;
        TextView mName;
        TextView mPirce;
        TextView mSaleNum;
        TextView mOriginPrice;

        public Vh(@NonNull View itemView) {
            super(itemView);
            mThumb = itemView.findViewById(R.id.thumb);
            mName = itemView.findViewById(R.id.name);
            mPirce = itemView.findViewById(R.id.price);
            mSaleNum = itemView.findViewById(com.livestreaming.mall.R.id.sale_num);
            mOriginPrice = itemView.findViewById(com.livestreaming.mall.R.id.origin_price);
            mOriginPrice.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
            itemView.setOnClickListener(mOnClickListener);
        }

        void setData(GoodsSimpleBean bean) {
            itemView.setTag(bean);
            ImgLoader.display(mContext, bean.getThumb(), mThumb);
            mName.setText(bean.getName());
            mPirce.setText(StringUtil.contact(mMoneySymbol, bean.getPrice()));
            if (bean.getType() == 1) {
                mSaleNum.setText(null);
                mOriginPrice.setText(bean.getOriginPrice());
            } else {
                mSaleNum.setText(String.format(mSaleString, bean.getSaleNum()));
                mOriginPrice.setText(null);
            }
        }

    }
}
