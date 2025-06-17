package com.livestreaming.live.adapter;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.livestreaming.live.R;
import com.livestreaming.live.bean.LiveGestBean;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import java.util.ArrayList;

import jp.wasabeef.glide.transformations.BlurTransformation;

public class LiveGuestAdapter extends RecyclerView.Adapter<LiveGuestAdapter.Holder> {

    private ArrayList<LiveGestBean> guests;
    public LiveGuestAdapter(ArrayList<LiveGestBean> data){
        guests=data;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.row_gest_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        LiveGestBean bean=guests.get(position);
        if(bean.getCam_type()==0) {
            holder.cover.setVisibility(View.VISIBLE);
            holder.coverView.setVisibility(View.VISIBLE);
            Glide.with(holder.coverView.getContext()).asBitmap().load(bean.getAvatar()).into(new CustomTarget<Bitmap>() {
                @Override
                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                    BitmapDrawable drawable = new BitmapDrawable(holder.coverView.getContext().getResources(), resource);
                    holder.avatar.setImageBitmap(resource);
                    Glide.with(holder.coverView.getContext())
                            .load(drawable)  // Load the BitmapDrawable
                            .apply(RequestOptions.bitmapTransform(new BlurTransformation(25, 3)))  // Apply blur transformation
                            .into(holder.cover);
                }
                @Override
                public void onLoadCleared(@Nullable Drawable placeholder) {

                }
            });
        }else{
            holder.cover.setVisibility(View.GONE);
            holder.coverView.setVisibility(View.GONE);
        }

        holder.tvName.setText(bean.getUserName());

    }

    @Override
    public int getItemCount() {
        return guests.size();
    }
    class Holder extends RecyclerView.ViewHolder{

        ImageView cover;
        View coverView ;
        TextView tvName ;
        ImageView mic ;
        com.makeramen.roundedimageview.RoundedImageView avatar;


        public Holder(@NonNull View itemView) {
            super(itemView);
            cover = itemView.findViewById(R.id.cover);
            coverView=itemView.findViewById(R.id.cointainer);
            tvName=itemView.findViewById(R.id.guest_name);
            avatar=itemView.findViewById(R.id.avatar);
            mic= itemView.findViewById(R.id.img_mute);
        }
    }
}
