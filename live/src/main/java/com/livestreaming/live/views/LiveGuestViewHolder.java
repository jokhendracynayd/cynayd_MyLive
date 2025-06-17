package com.livestreaming.live.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.livestreaming.common.views.AbsViewHolder;
import com.livestreaming.live.R;
import com.livestreaming.live.bean.LiveGestBean;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import jp.wasabeef.glide.transformations.BlurTransformation;

public class LiveGuestViewHolder extends AbsViewHolder {

    private final boolean isGuestAnchor;
    private  OnClickGuestCloseListener closeListener;
    ImageView cover;
    private IShowUserDialog iShowUserDialog;
    com.makeramen.roundedimageview.RoundedImageView avatar;
    View coverView;
    TextView tvName;
    TextView guestIncome;
    ImageView imFrame;
    ImageView img_mute;
    ImageView speak_iv;

    TextureView textureView;
    public LiveGestBean bean;
    private ImageView icon_close;

    public LiveGestBean getBean() {
        return bean;
    }

    public LiveGuestViewHolder(Context context, ViewGroup parentView, IShowUserDialog iShowUserDialog) {
        super(context, parentView);
        this.iShowUserDialog = iShowUserDialog;
        isGuestAnchor=false;
    }
    public LiveGuestViewHolder(Context context, ViewGroup parentView, IShowUserDialog iShowUserDialog, OnClickGuestCloseListener listener) {
        super(context, parentView);
        this.iShowUserDialog = iShowUserDialog;
        this.closeListener=listener;
        isGuestAnchor=true;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.row_gest_item_anchor;
    }

    @Override
    public void init() {
        cover = findViewById(R.id.cover);
        avatar = findViewById(R.id.avatar);
        coverView = findViewById(R.id.cointainer);
        tvName = findViewById(R.id.guest_name);
        guestIncome = findViewById(R.id.guest_income_tv);
        imFrame = findViewById(R.id.im_frame);
        textureView = findViewById(R.id.item_texture);
        img_mute = findViewById(R.id.img_mute);
        speak_iv = findViewById(R.id.speak_iv);
        icon_close=findViewById(R.id.icon_close);

    }

    public void setData() {
        if(isGuestAnchor){
            icon_close.setVisibility(View.VISIBLE);
            icon_close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    closeListener.onClose(String.valueOf(bean.getUser_id()));
                }
            });
        }else{
            icon_close.setVisibility(View.GONE);
        }
        if (bean.getCam_type() == 0) {
            if (bean.getFrame() != null) {
                Glide.with(imFrame).load(bean.getFrame()).into(imFrame);
            } else {
                imFrame.setVisibility(View.GONE);
            }
            guestIncome.setText("" + handleIncome(bean.getIncome()));
            tvName.setText(bean.getUserName());
            coverView.setVisibility(View.VISIBLE);
            cover.setVisibility(View.VISIBLE);
            Glide.with(mContext).asBitmap().load(bean.getAvatar()).into(new CustomTarget<Bitmap>() {
                @Override
                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                    BitmapDrawable drawable = new BitmapDrawable(mContext.getResources(), resource);
                    avatar.setImageBitmap(resource);
                    Glide.with(mContext)
                            .load(drawable)  // Load the BitmapDrawable
                            .apply(RequestOptions.bitmapTransform(new BlurTransformation(25, 3)))  // Apply blur transformation
                            .into(cover);
                }

                @Override
                public void onLoadCleared(@Nullable Drawable placeholder) {

                }
            });
        }
        getContentView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showUserDialog(String.valueOf(bean.getUser_id()));
            }
        });
    }

    public void showUserDialog(String toUid) {
        iShowUserDialog.showUserDialog(toUid);
    }

    private String handleIncome(int income) {
        if (income == 0) {
            return "0";
        }
        if (income >= 1000 && income < 1000000) {
            return (income / 1000) + "K";
        } else if (income >= 1000000) {
            return (income / 1000000) + "M";
        } else {
            return "" + income;
        }
    }


   public void handleSpeak(boolean speak){
        if(speak){
            speak_iv.setVisibility(View.VISIBLE);
        }else{
            speak_iv.setVisibility(View.GONE);
        }
   }
   public void handleMute(boolean mute) {
       bean.setMic_type(mute?1:0);
       if (mute) {
           img_mute.setVisibility(View.GONE);
       } else {
           img_mute.setVisibility(View.VISIBLE);
       }
       speak_iv.setVisibility(View.GONE);
   }
   public void handleGuestCam(boolean cam) {
        bean.setCam_type(cam?1:0);
       if (!cam) {
           cover.setVisibility(View.VISIBLE);
           avatar.setVisibility(View.VISIBLE);
           coverView.setVisibility(View.VISIBLE);
           Glide.with(mContext).asBitmap().load(bean.getAvatar()).into(new CustomTarget<Bitmap>() {
               @Override
               public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                   avatar.setImageBitmap(resource);
                   BitmapDrawable drawable = new BitmapDrawable(mContext.getResources(), resource);
                   Glide.with(mContext)
                           .load(drawable)  // Load the BitmapDrawable
                           .apply(RequestOptions.bitmapTransform(new BlurTransformation(25, 3)))  // Apply blur transformation
                           .into(cover);
               }

               @Override
               public void onLoadCleared(@Nullable Drawable placeholder) {

               }
           });
       } else {
           cover.setVisibility(View.GONE);
           avatar.setVisibility(View.GONE);
           coverView.setVisibility(View.GONE);
       }
   }

    public void handleGuestIncome(int income) {
        guestIncome.setText("" + handleIncome(income));
    }

    public interface IShowUserDialog {
        void showUserDialog(String toUid);
    }
    public interface OnClickGuestCloseListener {
        void onClose(String toUid);
    }
}
