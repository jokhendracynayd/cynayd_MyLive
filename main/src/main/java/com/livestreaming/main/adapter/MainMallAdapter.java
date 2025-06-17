package com.livestreaming.main.adapter;

import android.content.Context;
import android.graphics.Paint;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.TextView;

import com.livestreaming.common.adapter.RefreshAdapter;
import com.livestreaming.common.utils.DpUtil;
import com.livestreaming.common.utils.ScreenDimenUtil;
import com.livestreaming.common.utils.WordUtil;
import com.livestreaming.main.R;
import com.livestreaming.mall.bean.MainMallGoodsBean;


public class MainMallAdapter extends RefreshAdapter<MainMallGoodsBean> {

    private static final int HEAD = -1;
    private View.OnClickListener mOnClickListener;
    private View mHeadView;
    private String mSaleString;
    //    private String mMoneySymbol;
    private final int mDisplayWidth;
    private final int mDisplayHeightMax;
    private boolean mNeedRefreshData;


    public MainMallAdapter(Context context) {
        super(context);
        mSaleString = WordUtil.getString(com.livestreaming.common.R.string.mall_114);
//        mMoneySymbol = WordUtil.getString(R.string.money_symbol);
        mHeadView = mInflater.inflate(R.layout.item_main_mall_head, null, false);
        mHeadView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!canClick()) {
                    return;
                }
                Object tag = v.getTag();
                if (tag != null && mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick((MainMallGoodsBean) tag, 0);
                }
            }
        };
        mDisplayWidth = (ScreenDimenUtil.getInstance().getScreenWidth() - DpUtil.dp2px(30)) / 2;
        mDisplayHeightMax = DpUtil.dp2px(250);
    }

    public View getHeadView() {
        return mHeadView;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return HEAD;
        }
        return 0;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        if (viewType == HEAD) {
            ViewParent viewParent = mHeadView.getParent();
            if (viewParent != null) {
                ((ViewGroup) viewParent).removeView(mHeadView);
            }
            HeadVh headVh = new HeadVh(mHeadView);
            headVh.setIsRecyclable(false);
            return headVh;
        }
        return new Vh(mInflater.inflate(com.livestreaming.mall.R.layout.item_shop_home, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder vh, int position) {
        if (vh instanceof Vh) {
            ((Vh) vh).setData(position, mList.get(position - 1));
        }
    }

    @Override
    public int getItemCount() {
        return super.getItemCount() + 1;
    }

    @Override
    public void onViewAttachedToWindow(@NonNull RecyclerView.ViewHolder holder) {
        int position = holder.getLayoutPosition();
//        L.e("MainMallAdapter", "---onViewAttachedToWindow---->" + position);
        if (position == 0) {
            ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
            if (null != lp && lp instanceof StaggeredGridLayoutManager.LayoutParams) {
                StaggeredGridLayoutManager.LayoutParams p = (StaggeredGridLayoutManager.LayoutParams) lp;
                p.setFullSpan(true);//占满一行
            }
        }
    }


    class HeadVh extends RecyclerView.ViewHolder {
        public HeadVh(View itemView) {
            super(itemView);
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

        void setData(int position, final MainMallGoodsBean bean) {
            itemView.setTag(bean);
            bean.display(mContext, mThumb, mDisplayWidth, mDisplayHeightMax);
            mName.setText(bean.getName());
            mPirce.setText(bean.getPrice());
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