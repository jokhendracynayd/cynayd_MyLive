package com.livestreaming.main.adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.livestreaming.main.R;

/**
 * Created by cxf on 2018/10/19.
 * 直播分享
 */

public class LiveShareAdapter extends RecyclerView.Adapter<LiveShareAdapter.Vh> {

    //private List<MobBean> mList;
    private LayoutInflater mInflater;
    private View.OnClickListener mOnClickListener;
    //private OnItemClickListener<MobBean> mOnItemClickListener;

    /*public LiveShareAdapter(Context context) {
        mList = new ArrayList<>();
        ConfigBean configBean = CommonAppConfig.getInstance().getConfig();
        if (configBean != null) {
            List<MobBean> list = MobBean.getLiveShareTypeList(configBean.getShareType());
            mList.addAll(list);
        }
        MobBean linkBean = new MobBean();
        linkBean.setType(Constants.LINK);
        linkBean.setName(com.livestreaming.common.R.string.copy_link);
        linkBean.setIcon1(com.livestreaming.live.R.mipmap.icon_share_link);
        mList.add(linkBean);
        mInflater = LayoutInflater.from(context);
        mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Object tag = v.getTag();
                if (tag != null) {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick((MobBean) tag, 0);
                    }
                }
            }
        };
    }

    public void setOnItemClickListener(OnItemClickListener<MobBean> onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }*/

    @NonNull
    @Override
    public Vh onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Vh(mInflater.inflate(com.livestreaming.live.R.layout.item_live_share, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Vh vh, int position) {
       // vh.setData(mList.get(position));
    }

    @Override
    public int getItemCount() {
        return 0;//mList.size();
    }

    class Vh extends RecyclerView.ViewHolder {

        ImageView mIcon;
        TextView mName;

        public Vh(View itemView) {
            super(itemView);
            mIcon = (ImageView) itemView.findViewById(R.id.icon);
            mName = (TextView) itemView.findViewById(R.id.name);
            itemView.setOnClickListener(mOnClickListener);
        }

//        void setData(MobBean bean) {
//            itemView.setTag(bean);
//            mIcon.setImageResource(bean.getIcon1());
//            mName.setText(bean.getName());
//        }
    }
}
