//package com.livestreaming.main.adapter;
//
//import android.content.Context;
//
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.RecyclerView;
//
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//
//import com.livestreaming.main.R;
//
//import java.util.List;
//
///**
// * Created by cxf on 2018/9/22.
// */
//
//public class LoginTypeAdapter extends RecyclerView.Adapter<LoginTypeAdapter.Vh> {
//
//    private List<MobBean> mList;
//    private LayoutInflater mInflater;
//    private OnItemClickListener<MobBean> mOnItemClickListener;
//    private View.OnClickListener mOnClickListener;
//
//    public LoginTypeAdapter(Context context, List<MobBean> list) {
//        mList = list;
//        mInflater = LayoutInflater.from(context);
//        mOnClickListener = new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Object tag = v.getTag();
//                if (tag != null) {
//                    int position = (int) tag;
//                    if (mOnItemClickListener != null) {
//                        mOnItemClickListener.onItemClick(mList.get(position), position);
//                    }
//                }
//            }
//        };
//    }
//
//    @NonNull
//    @Override
//    public Vh onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        return new Vh(mInflater.inflate(R.layout.item_login_type, parent, false));
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull Vh vh, int position) {
//        vh.setData(mList.get(position), position);
//    }
//
//    @Override
//    public int getItemCount() {
//        return mList.size();
//    }
//
//    class Vh extends RecyclerView.ViewHolder {
//
//        ImageView mImg;
//
//        public Vh(View itemView) {
//            super(itemView);
//            mImg = (ImageView) itemView;
//            itemView.setOnClickListener(mOnClickListener);
//        }
//
//        void setData(MobBean bean, int position) {
//            itemView.setTag(position);
//            mImg.setImageResource(bean.getIcon1());
//        }
//    }
//
//    public void setOnItemClickListener(OnItemClickListener<MobBean> onItemClickListener) {
//        mOnItemClickListener = onItemClickListener;
//    }
//}
