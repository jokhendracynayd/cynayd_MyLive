package com.livestreaming.main.adapter;

import static android.view.View.VISIBLE;
import static com.android.volley.Request.Method.HEAD;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.livestreaming.common.adapter.RefreshAdapter;
import com.livestreaming.common.glide.ImgLoader;
import com.livestreaming.live.bean.LiveBean;
import com.livestreaming.main.R;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 云豹科技 on 2022/1/21.
 */
public class LiveVoiceListAdapter extends RefreshAdapter<LiveBean> {

    private View.OnClickListener mOnClickListener;
    private View mHeadView;
    //private static final int TYPE_NATIVE_AD = 1;

    public LiveVoiceListAdapter(Context context) {
        super(context);
        mHeadView = mInflater.inflate(R.layout.item_voice_list_head, null, false);

        mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Object tag = v.getTag();
                if (mOnItemClickListener != null && tag != null) {
                    mOnItemClickListener.onItemClick((LiveBean) tag, 0);
                }
            }
        };
    }

    public View getHeadView() {
        return mHeadView;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return HEAD;
        } else {
            return super.getItemViewType(position);
        }
//        else if (position % 6 == 0) {
//            return TYPE_NATIVE_AD;  // Show ad every 6th item
//        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        if (i == HEAD) {
            ViewParent viewParent = mHeadView.getParent();
            if (viewParent != null) {
                ((ViewGroup) viewParent).removeView(mHeadView);

            }
            LiveVoiceListAdapter.HeadVh headVh = new LiveVoiceListAdapter.HeadVh(mHeadView);
            headVh.setIsRecyclable(false);
            return headVh;
        }
//        } else if(i==TYPE_NATIVE_AD){
//                // Create native ad view holder
//                View view = LayoutInflater.from(mContext).inflate(R.layout.iter_ads_item, viewGroup, false);
//                return new NativeAdViewHolder(view);
//        }
        else {
            return new Vh(mInflater.inflate(R.layout.item_live_voice_list, viewGroup, false));
        }
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder vh, int i) {
        if (vh instanceof LiveVoiceListAdapter.Vh) {
            if (i < mList.size() && i != 0) {
                LiveBean bean = mList.get(i);
                ((Vh) vh).setData(bean);

                if (bean.getTitle().length() > 30) {
                    ((Vh) vh).mTitle.setSelected(true);
                    ((Vh) vh).mTitle.post(() -> {
                        float containerWidth = ((Vh) vh).mTitle.getWidth();

                        ObjectAnimator animator = ObjectAnimator.ofFloat(
                                ((Vh) vh).mTitle,
                                "translationX",
                                containerWidth, // Start from the right edge
                                -containerWidth // Scroll completely out of the view
                        );
                        animator.setDuration(6000); // 5 seconds for a full scroll
                        animator.setRepeatCount(ValueAnimator.INFINITE); // Repeat forever
                        animator.setInterpolator(null); // Smooth scrolling
                        animator.start();
                    });
                } else {
                    ((Vh) vh).mTitle.setSelected(false);
                }
                if (bean.getUserNiceName().length() > 30) {
                    ((Vh) vh).mName.setSelected(true);
                    ((Vh) vh).mName.post(() -> {
                        float containerWidth = ((Vh) vh).mName.getWidth();

                        ObjectAnimator animator = ObjectAnimator.ofFloat(
                                ((Vh) vh).mName,
                                "translationX",
                                containerWidth, // Start from the right edge
                                -containerWidth // Scroll completely out of the view
                        );
                        animator.setDuration(6000); // 5 seconds for a full scroll
                        animator.setRepeatCount(ValueAnimator.INFINITE); // Repeat forever
                        animator.setInterpolator(null); // Smooth scrolling
                        animator.start();
                    });
                } else {
                    ((Vh) vh).mName.setSelected(true);
                }
            }
        } else if (vh instanceof LiveVoiceListAdapter.HeadVh) {
            handleTopBeans();
        }
//        }else if (vh instanceof NativeAdViewHolder adHolder) {
//            // Bind the native ad data
//
//            loadNativeAd(adHolder);
//        }
    }

    private void handleTopBeans() {
        if (mList.size() > 0) {
            List<LiveBean> topBeans = mList.get(0).headItems;
            if (topBeans != null) {
                ImageView ivTop;
                View reRank;
                ImageView imPhoto1;
                ImageView ivRankingOne;
                ImageView imPhoto2;
                ImageView ivRankingTwo;
                ImageView imPhoto3;
                ImageView ivRankingThree;
                CardView reOne;
                RelativeLayout reTwo;
                RelativeLayout reThree;
                RelativeLayout reShop;
                ImageView ivTop2;
                ImageView ivTop3;
                ivTop = getHeadView().findViewById(R.id.iv_top);
                ivTop2 = getHeadView().findViewById(R.id.iv_top2);
                ivTop3 = getHeadView().findViewById(R.id.iv_top3);
                Glide.with(mContext).load(ContextCompat.getDrawable(mContext, R.drawable.ic_frame1)).into(ivTop);
                Glide.with(mContext).load(ContextCompat.getDrawable(mContext, R.drawable.ic_frame2)).into(ivTop2);
                Glide.with(mContext).load(ContextCompat.getDrawable(mContext, R.drawable.ic_frame3)).into(ivTop3);

                //banner_svga = findViewById(R.id.biklink_banner);
                imPhoto1 = getHeadView().findViewById(R.id.im_photo1);
                ivRankingOne = getHeadView().findViewById(R.id.iv_ranking_one);
                Glide.with(mContext).load(ContextCompat.getDrawable(mContext, R.drawable.ic_top1)).into(ivRankingOne);

                imPhoto2 = getHeadView().findViewById(R.id.im_photo2);
                ivRankingTwo = getHeadView().findViewById(R.id.iv_ranking_two);
                Glide.with(mContext).load(ContextCompat.getDrawable(mContext, R.drawable.ic_top2)).into(ivRankingTwo);


                imPhoto3 = getHeadView().findViewById(R.id.im_photo3);
                ivRankingThree = getHeadView().findViewById(R.id.iv_ranking_three);
                Glide.with(mContext).load(ContextCompat.getDrawable(mContext, R.drawable.ic_top3)).into(ivRankingThree);
                reOne = getHeadView().findViewById(R.id.re_one);
                reTwo = getHeadView().findViewById(R.id.re_two);
                reThree = getHeadView().findViewById(R.id.re_three);

                reOne.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!topBeans.isEmpty()) {
                            mOnItemClickListener.onItemClick(topBeans.get(0), 0);
                        }
                    }
                });
                reTwo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!topBeans.isEmpty()) {
                            mOnItemClickListener.onItemClick(topBeans.get(1), 0);
                        }
                    }
                });
                reThree.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!topBeans.isEmpty()) {
                            mOnItemClickListener.onItemClick(topBeans.get(2), 0);
                        }
                    }
                });

                if (!topBeans.isEmpty()) {
                    LiveBean bean1 = topBeans.get(0);
                    Glide.with(imPhoto1.getContext()).load(bean1.getAvatar()).into(imPhoto1);
                }
                if (topBeans.size() > 1) {
                    LiveBean bean2 = topBeans.get(1);

                    Glide.with(imPhoto2.getContext()).load(bean2.getAvatar()).into(imPhoto2);
                    ivRankingTwo.setVisibility(VISIBLE);
                }
                if (topBeans.size() > 2) {
                    LiveBean bean3 = topBeans.get(2);
                    Glide.with(imPhoto3.getContext()).load(bean3.getAvatar()).into(imPhoto3);
                    ivRankingThree.setVisibility(VISIBLE);
                }
            }
        }
    }




    @Override
    public int getItemCount() {
        return mList.size() + 1;
    }

    private class Vh extends RecyclerView.ViewHolder {

        ImageView mThumb;
        ImageView mAvatar;
        TextView mName;
        TextView mTitle;
        TextView mNum;
        ArrayList<com.livestreaming.main.views.CircleImageView> guestsCovers;

        public Vh(@NonNull View itemView) {
            super(itemView);
            mThumb = itemView.findViewById(R.id.thumb);
            mAvatar = itemView.findViewById(R.id.avatar);
            mName = itemView.findViewById(R.id.name);
            mTitle = itemView.findViewById(R.id.title);
            mNum = itemView.findViewById(R.id.num);
            itemView.setOnClickListener(mOnClickListener);
            guestsCovers = new ArrayList<>();
            guestsCovers.add(itemView.findViewById(R.id.image_0));
            guestsCovers.add(itemView.findViewById(R.id.image_1));
            guestsCovers.add(itemView.findViewById(R.id.image_2));
            guestsCovers.add(itemView.findViewById(R.id.image_3));
            guestsCovers.add(itemView.findViewById(R.id.image_4));
            guestsCovers.add(itemView.findViewById(R.id.image_5));
            guestsCovers.add(itemView.findViewById(R.id.image_6));
            guestsCovers.add(itemView.findViewById(R.id.image_7));
            guestsCovers.add(itemView.findViewById(R.id.image_8));
            guestsCovers.add(itemView.findViewById(R.id.image_9));
            guestsCovers.add(itemView.findViewById(R.id.image_10));
            guestsCovers.add(itemView.findViewById(R.id.image_11));
        }


        void setData(LiveBean liveBean) {
            itemView.setTag(liveBean);
            ImgLoader.display(mContext, liveBean.getThumb(), mThumb);
            ImgLoader.display(mContext, liveBean.getAvatar(), mAvatar);
            mName.setText(liveBean.getUserNiceName());
            mTitle.setText(liveBean.getTitle());
            mNum.setText(liveBean.getNums());

            try {
                for (ImageView im : guestsCovers
                ) {
                    if (im != null) {
                        im.setVisibility(View.GONE);
                    }
                }
                if (liveBean.getGuests() != null) {
                    for (int i = 0; i < liveBean.getGuests().size(); i++) {
                        if (liveBean.getGuests().get(i).getAvatar() != null && !liveBean.getUid().equals(liveBean.getGuests().get(i).getUser_id())) {
                            guestsCovers.get(i).setVisibility(View.VISIBLE);
                            Glide.with(guestsCovers.get(i).getContext())
                                    .load(liveBean.getGuests().get(i).getAvatar())
                                    .into(guestsCovers.get(i));
                        }
                    }
                }
            } catch (Exception e) {

            }
        }
    }

    class HeadVh extends RecyclerView.ViewHolder {
        public HeadVh(View itemView) {
            super(itemView);
        }
    }
//    public class NativeAdViewHolder extends RecyclerView.ViewHolder {
//        NativeAdView nativeAdView;
//        TextView headlineView;
//        TextView bodyView;
//        ImageView imageView;
//
//        public NativeAdViewHolder(View itemView) {
//            super(itemView);
//            nativeAdView = itemView.findViewById(R.id.native_ad_view);
//            headlineView = itemView.findViewById(R.id.ad_headline);
//            bodyView = itemView.findViewById(R.id.ad_body);
//            imageView = itemView.findViewById(R.id.ad_image);
//        }
////
////        void bindAd(NativeAd nativeAd) {
////            // Set the headline (TextView)
////            headlineView.setText(nativeAd.getHeadline());
////            nativeAdView.setHeadlineView(headlineView);
////
////            // Set the body (TextView)
////            bodyView.setText(nativeAd.getBody());
////            nativeAdView.setBodyView(bodyView);
////
////            // Set the image (ImageView)
////            nativeAd.getImages();
////            if (!nativeAd.getImages().isEmpty()) {
////                ImageView imageView = nativeAdView.findViewById(R.id.ad_image);
////                imageView.setVisibility(View.VISIBLE);
////                Glide.with(mContext).load(Objects.requireNonNull(nativeAd.getImages().get(0).getUri()).toString()).into(imageView); // Assuming you have an image loader utility
////                nativeAdView.setImageView(imageView);
////            } else {
////                imageView.setVisibility(View.GONE);
////            }
////            nativeAdView.setCallToActionView(itemView.findViewById(R.id.root));
////
////
////
////        }
//    }
//
//    // Load a Native ad
//    @SuppressLint("MissingPermission")
//    private void loadNativeAd(NativeAdViewHolder holder) {
//        // When the ad is loaded, bind it to the view
//        AdLoader adLoader = new AdLoader.Builder(mContext, mContext.getString(R.string.intr_u_test_id))
//                .forNativeAd(nativeAd -> {
//                    holder.bindAd(nativeAd);
//                })
//                .withNativeAdOptions(new NativeAdOptions.Builder().build())
//                .build();
//
//        // Load the ad
//        adLoader.loadAd(new AdRequest.Builder().build());
//    }
}
