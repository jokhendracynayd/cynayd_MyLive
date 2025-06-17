package com.livestreaming.live.adapter;

import android.content.Context;
import android.graphics.Paint;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.livestreaming.common.adapter.RefreshAdapter;
import com.livestreaming.common.bean.GoodsBean;
import com.livestreaming.common.glide.ImgLoader;
import com.livestreaming.common.utils.StringUtil;
import com.livestreaming.common.utils.WordUtil;
import com.livestreaming.live.R;

/**
 * Created by cxf on 2019/8/29.
 */

public class LiveGoodsAdapter extends RefreshAdapter<GoodsBean> {

    private View.OnClickListener mOnClickListener;
    private String mMoneySymbol;

    public LiveGoodsAdapter(Context context) {
        super(context);
        mMoneySymbol = WordUtil.getString(com.livestreaming.common.R.string.money_symbol);
        mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Object tag = v.getTag();
                if (tag != null && mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick((GoodsBean) tag, 0);
                }
            }
        };

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Vh(mInflater.inflate(R.layout.item_live_goods, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder vh, int position) {
        ((Vh) vh).setData(mList.get(position), position);
    }

    class Vh extends RecyclerView.ViewHolder {

        ImageView mThumb;
        TextView mTitle;
        TextView mPrice;
        TextView mPriceOrigin;
        View mBtnBuy;
        TextView mNum;

        public Vh(View itemView) {
            super(itemView);
            mThumb = itemView.findViewById(R.id.thumb);
            mTitle = itemView.findViewById(R.id.title);
            mPrice = itemView.findViewById(R.id.price);
            mNum = itemView.findViewById(R.id.num);
            mPriceOrigin = itemView.findViewById(R.id.price_origin);
            mPriceOrigin.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
            mBtnBuy = itemView.findViewById(R.id.btn_buy);
            mBtnBuy.setOnClickListener(mOnClickListener);
        }

        void setData(GoodsBean bean, int position) {
            mBtnBuy.setTag(bean);
            ImgLoader.display(mContext, bean.getThumb(), mThumb);
            mPrice.setText(StringUtil.contact(mMoneySymbol, bean.getPriceNow()));
            mTitle.setText(bean.getName());
            if (bean.getType() == 1) {
                mPriceOrigin.setText(StringUtil.contact(mMoneySymbol, bean.getOriginPrice()));
            } else {
                mPriceOrigin.setText(null);
            }
            mNum.setText(String.valueOf(position + 1));
        }
    }
}
