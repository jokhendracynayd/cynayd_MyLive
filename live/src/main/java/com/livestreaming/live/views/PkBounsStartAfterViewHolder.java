package com.livestreaming.live.views;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.livestreaming.live.R;

public class PkBounsStartAfterViewHolder {

    private final Context mContext;
    private int mission_type;
    private int target_value;
    private View mView;
    private int startAfter;
    private int x;
    private TextView message_Long;
    ObjectAnimator animator;
    private int ticker = 0;
    private boolean isDataInit = false;


    public PkBounsStartAfterViewHolder(Context context, int startAfter, int x, int missionType, int targetValue) {
        this.mContext = context;
        ticker = 0;
        this.startAfter = startAfter;
        this.x = x;
        this.mission_type = missionType;
        this.target_value = targetValue;
        isDataInit = true;
        mView = LayoutInflater.from(mContext).inflate(R.layout.pk_bouns_start_after_view, null, false);
    }


    public void init() {
        message_Long = mView.findViewById(R.id.message_Long);
        SpannableStringBuilder spannableBuilder = new SpannableStringBuilder();

        String text1 = mContext.getString(com.livestreaming.common.R.string.pk_start_after_msg1_p1);
        int start1 = spannableBuilder.length();
        spannableBuilder.append(text1);
        int end1 = spannableBuilder.length();
        spannableBuilder.setSpan(new ForegroundColorSpan(Color.WHITE), start1, end1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        String text2 = x + "x ";
        int start2 = spannableBuilder.length();
        spannableBuilder.append(text2);
        int end2 = spannableBuilder.length();
        spannableBuilder.setSpan(new ForegroundColorSpan(Color.YELLOW), start2, end2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        String text3 = mContext.getString(com.livestreaming.common.R.string.pk_start_after_msg1_p2);
        int start3 = spannableBuilder.length();
        spannableBuilder.append(text3);
        int end3 = spannableBuilder.length();
        spannableBuilder.setSpan(new ForegroundColorSpan(Color.WHITE), start3, end3, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        message_Long.setText(spannableBuilder);


        float containerWidth = message_Long.getWidth();

        animator = ObjectAnimator.ofFloat(message_Long, "translationX", containerWidth, // Start from the right edge
                -containerWidth // Scroll completely out of the view
        );
        animator.setDuration(3000); // 5 seconds for a full scroll
        animator.setRepeatCount(ValueAnimator.INFINITE); // Repeat forever
        animator.setInterpolator(null); // Smooth scrolling
        animator.start();
    }

    public void setTimeTic(String timeStr) {
        ticker++;
        if (ticker > 4 && isDataInit) {
            animator.cancel();
            message_Long.setText("");
            handleTargetMessage();
            isDataInit = false;
            mView.invalidate();
        }
    }

    private void handleTargetMessage() {
        String text1 = "";
        int icon_id = 0;

        switch (mission_type) {
            case 1:
                text1 = mContext.getString(com.livestreaming.common.R.string.gifter);
                icon_id = com.livestreaming.common.R.drawable.ic_gifter;
                break;
            case 2:

                text1 = mContext.getString(com.livestreaming.common.R.string.coin);
                icon_id = com.livestreaming.common.R.drawable.ic_coin_g;
                break;
            case 3:

                text1 = mContext.getString(com.livestreaming.common.R.string.coin_give) + " " + mContext.getString(com.livestreaming.common.R.string.target);
                icon_id = com.livestreaming.common.R.drawable.ic_gift_pk_bouns;
                break;
            default:
                break;

        }
        SpannableStringBuilder spannableBuilder = new SpannableStringBuilder();

        int start1 = spannableBuilder.length();
        spannableBuilder.append(text1);
        int end1 = spannableBuilder.length();
        spannableBuilder.setSpan(new ForegroundColorSpan(Color.WHITE), start1, end1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        String text2 = "" + target_value;
        int start2 = spannableBuilder.length();
        spannableBuilder.append(text2);
        int end2 = spannableBuilder.length();
        spannableBuilder.setSpan(new ForegroundColorSpan(Color.YELLOW), start2, end2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        message_Long.setText(spannableBuilder);
        if (icon_id != 0) {
            ((ImageView) mView.findViewById(R.id.img)).setVisibility(View.VISIBLE);
            ((ImageView) mView.findViewById(R.id.img)).setImageResource(icon_id);
        }
    }

    public View getView() {
        return mView;
    }


}
