package com.livestreaming.main.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.livestreaming.common.glide.ImgLoader;
import com.livestreaming.live.bean.LiveBean;
import com.livestreaming.main.R;
import com.livestreaming.main.views.MainHomeLiveViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cxf on 2018/9/26.
 * 首页 直播
 */

public class MainHomeLiveAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final Context mContext;


    private View.OnClickListener mOnClickListener;
    public static int MAIN_TYPE_NATIVE_AD = 111;
    public static int LEFT = 113;
    public static int HEAD = 123;
    public static int MAIN_TYPE_NATIVE_AD_Empty_Balncer = -5;
    private boolean typee = false;
    private ArrayList<LiveBean> mList;
    private MainHomeLiveViewHolder mOnItemClickListener;
    public MainHomeLiveAdapter(Context context,MainHomeLiveViewHolder mOnItemClickListener) {
        this.mContext = context;

        this.mOnItemClickListener=mOnItemClickListener;
        mList=new ArrayList<>();
        mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = (int) v.getTag();
                mOnItemClickListener.onItemClick(mList.get(position), position);
            }
        };
    }


    public int getItemViewType(int position) {
        if ((position == 0)) {
            return HEAD;
        }
        return LEFT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        return new Vh(LayoutInflater.from(mContext).inflate(R.layout.item_main_home_live_left, parent, false));
    }



    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder  vh, int position) {

        if (vh instanceof Vh) {
            ((Vh) vh).setData(mList.get(position), position);
        }
    }



    @Override
    public int getItemCount() {
        return mList.size();
    }

    public List<LiveBean> getList() {
        return mList;
    }

    public void setList(List<LiveBean> list) {
        mList.clear();
        mList.addAll(list);
        notifyDataSetChanged();
    }

    public void insertList(List<LiveBean> list) {
        mList.addAll(list);
        notifyDataSetChanged();
    }


    class Vh extends RecyclerView.ViewHolder {

        ImageView mCover;
        //ImageView mAvatar;
        TextView mName;
        TextView mTitle;
        TextView mLikesNum;
        TextView mTrendNum;
        LinearLayout ispk;
        LinearLayout rooms_img;
        ImageView flag_img;
        LinearLayout live_host_gests_tv;

        public Vh(View itemView) {
            super(itemView);
            mCover = (ImageView) itemView.findViewById(R.id.cover);
            flag_img = (ImageView) itemView.findViewById(R.id.flag_img);
            mName = (TextView) itemView.findViewById(R.id.name);
            mTitle = (TextView) itemView.findViewById(R.id.title);
            mLikesNum = (TextView) itemView.findViewById(R.id.tv_likes);
            mTrendNum = (TextView) itemView.findViewById(R.id.trend_id);
            ispk =  itemView.findViewById(R.id.pk_img);
            rooms_img =   itemView.findViewById(R.id.rooms_img);
            live_host_gests_tv =  itemView.findViewById(R.id.live_host_gests_tv);
            itemView.setOnClickListener(mOnClickListener);
        }

        void setData(LiveBean bean, int position) {
            itemView.setTag(position);
            ImgLoader.display(mContext, bean.getThumb(), mCover);
            if (bean.getCountry_img() != null) {
                flag_img.setVisibility(View.VISIBLE);
                Glide.with(flag_img).load(bean.getCountry_img()).into(flag_img);
            } else {
                flag_img.setVisibility(View.INVISIBLE);
            }
            if (bean.getIsPk() == 1) {
                ispk.setVisibility(View.VISIBLE);
                live_host_gests_tv.setVisibility(View.GONE);
            } else if (bean.getHave_guests() == 1) {
                live_host_gests_tv.setVisibility(View.VISIBLE);
                ispk.setVisibility(View.GONE);
            } else {
                live_host_gests_tv.setVisibility(View.GONE);
                ispk.setVisibility(View.GONE);
            }
            if (bean.getIsVoice() == 1) {
                rooms_img.setVisibility(View.VISIBLE);
            } else {
                rooms_img.setVisibility(View.GONE);
            }

            mName.setText(bean.getUserNiceName());
            if (TextUtils.isEmpty(bean.getTitle())) {
                if (mTitle.getVisibility() == View.VISIBLE) {
                    mTitle.setVisibility(View.VISIBLE);
                    mTitle.setText(mContext.getString(R.string.no_pio_yet));
                }
            } else {
                if (mTitle.getVisibility() != View.VISIBLE) {
                    mTitle.setVisibility(View.VISIBLE);
                }
                mTitle.setText(bean.getTitle());
            }
            if (mLikesNum != null) {
                if (bean.getTotal_like() == 0) {
                    mLikesNum.setVisibility(View.GONE);
                } else {
                    mLikesNum.setVisibility(View.VISIBLE);
                    mLikesNum.setText(handleTotalLikes(bean.getTotal_like()));
                }
            }
            if (bean.getTrendId() != 0) {
                mTrendNum.setVisibility(View.VISIBLE);
                if (bean.getTrendId() == 1) {
                    mTrendNum.setText(mContext.getString(com.livestreaming.common.R.string.trend) + " " + mContext.getString(com.livestreaming.common.R.string.一麦));
                } else if (bean.getTrendId() == 2) {
                    mTrendNum.setText(mContext.getString(com.livestreaming.common.R.string.trend) + " " + mContext.getString(com.livestreaming.common.R.string.二麦));
                } else if (bean.getTrendId() == 3) {
                    mTrendNum.setText(mContext.getString(com.livestreaming.common.R.string.trend) + " " + mContext.getString(com.livestreaming.common.R.string.三麦));
                } else {
                    mTrendNum.setVisibility(View.GONE);
                }
            } else {
                mTrendNum.setVisibility(View.GONE);
            }


        }
    }

    
    
    private String handleTotalLikes(int totalLike) {
        if (totalLike < 1000) {
            return totalLike + "";
        } else if (totalLike < 1000000) {
            return (totalLike / 1000) + "K";
        } else {
            return (totalLike / 1000000) + "M";
        }
    }

}
