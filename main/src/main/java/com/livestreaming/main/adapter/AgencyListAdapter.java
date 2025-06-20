package com.livestreaming.main.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.livestreaming.common.Constants;
import com.livestreaming.common.adapter.RefreshAdapter;
import com.livestreaming.common.glide.ImgLoader;
import com.livestreaming.common.utils.StringUtil;
import com.livestreaming.main.R;
import com.livestreaming.main.bean.AgencyListBean;

import java.util.List;

/**
 * Agency list adapter
 */
public class AgencyListAdapter extends RefreshAdapter<AgencyListBean> {

    private final String mCoinName;
    private final View.OnClickListener mItemClickListener;

    public AgencyListAdapter(Context context, String coinName) {
        super(context);
        mCoinName = coinName;
        mItemClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!canClick()) {
                    return;
                }
                AgencyListBean bean = (AgencyListBean) v.getTag();
                if (bean != null) {
                    // Handle agency item click if needed
                }
            }
        };
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Vh(mInflater.inflate(R.layout.item_agency_list, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((Vh) holder).setData(mList.get(position), position, null);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder vh, int position, @NonNull List payloads) {
        Object payload = payloads.size() > 0 ? payloads.get(0) : null;
        ((Vh) vh).setData(mList.get(position), position, payload);
    }

    class Vh extends RecyclerView.ViewHolder {

        TextView mOrder;
        ImageView mAvatar;
        TextView mName;
        TextView mVotes;

        public Vh(View itemView) {
            super(itemView);
            mOrder = itemView.findViewById(R.id.order);
            mAvatar = itemView.findViewById(R.id.avatar);
            mName = itemView.findViewById(R.id.name);
            mVotes = itemView.findViewById(R.id.votes);
            itemView.setOnClickListener(mItemClickListener);
        }

        void setData(AgencyListBean bean, int position, Object payload) {
            if (payload == null) {
                itemView.setTag(bean);
                mOrder.setText(StringUtil.contact("NO.", String.valueOf(position + 4)));
                ImgLoader.display(mContext, bean.getUserProfilePic(), mAvatar);
                mName.setText(bean.getUsername());
                mVotes.setText(StringUtil.contact(bean.getTotalRevenueFormat(), " ", mCoinName));
            }
        }
    }
} 