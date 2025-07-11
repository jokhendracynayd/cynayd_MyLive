package com.livestreaming.live.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.livestreaming.common.CommonAppConfig;
import com.livestreaming.common.bean.ConfigBean;
import com.livestreaming.common.utils.StringUtil;
import com.livestreaming.common.utils.WordUtil;
import com.livestreaming.live.R;
import com.livestreaming.live.bean.LiveTimeChargeBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cxf on 2018/10/8.
 */

public class LiveTimeChargeAdapter extends RecyclerView.Adapter<LiveTimeChargeAdapter.Vh> {

    private List<LiveTimeChargeBean> mList;
    private LayoutInflater mInflater;
    private View.OnClickListener mOnClickListener;
    private int mCheckedPosition = -1;
    private String mCoinName;
    private int mCheckedColor;
    private int mUnCheckedColor;
    private String mStringMinute;

    public LiveTimeChargeAdapter(Context context, int checkedCoin) {
        mCheckedColor = ContextCompat.getColor(context,com.livestreaming.common. R.color.global);
        mUnCheckedColor = ContextCompat.getColor(context, com.livestreaming.common.R.color.textColor);
        mStringMinute = WordUtil.getString(com.livestreaming.common.R.string.minute);
        mList = new ArrayList<>();
        ConfigBean configBean = CommonAppConfig.getInstance().getConfig();
        if (configBean != null) {
            mCoinName = configBean.getCoinName();
            String[] coins = configBean.getLiveTimeCoin();
            if (coins != null) {
                for (int i = 0, length = coins.length; i < length; i++) {
                    int coin = Integer.parseInt(coins[i]);
                    LiveTimeChargeBean bean = new LiveTimeChargeBean(coin);
                    if (coin == checkedCoin) {
                        bean.setChecked(true);
                        mCheckedPosition = i;
                    }
                    mList.add(bean);
                }
                if (mCheckedPosition < 0) {
                    mCheckedPosition = 0;
                    mList.get(0).setChecked(true);
                }
            }
        }
        mInflater = LayoutInflater.from(context);
        mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Object tag = v.getTag();
                if (tag != null) {
                    int position = (int) tag;
                    if (mCheckedPosition == position) {
                        return;
                    }
                    if (mCheckedPosition >= 0 && mCheckedPosition < mList.size()) {
                        mList.get(mCheckedPosition).setChecked(false);
                        notifyItemChanged(mCheckedPosition);
                    }
                    mList.get(position).setChecked(true);
                    notifyItemChanged(position);
                    mCheckedPosition = position;
                }
            }
        };
    }


    public int getCheckedCoin() {
        if (mCheckedPosition >= 0 && mCheckedPosition < mList.size()) {
            return mList.get(mCheckedPosition).getCoin();
        }
        return 0;
    }

    @NonNull
    @Override
    public Vh onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Vh(mInflater.inflate(R.layout.item_live_time_charge, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Vh vh, int position) {
        vh.setData(mList.get(position), position);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    class Vh extends RecyclerView.ViewHolder {

        TextView mTextView;

        public Vh(View itemView) {
            super(itemView);
            mTextView = (TextView) itemView;
            itemView.setOnClickListener(mOnClickListener);
        }

        void setData(LiveTimeChargeBean bean, int position) {
            itemView.setTag(position);
            mTextView.setText(StringUtil.contact(String.valueOf(bean.getCoin()), mCoinName, "/", mStringMinute));
            if (bean.isChecked()) {
                mTextView.setTextColor(mCheckedColor);
            } else {
                mTextView.setTextColor(mUnCheckedColor);
            }
        }
    }
}
