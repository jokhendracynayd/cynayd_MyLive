package com.livestreaming.live.views;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.livestreaming.live.R;

public class PkBounsStartViewHolder {
    private final Context mContext;
    private final View mView;
    private final int targDuration;
    private int duration;
    private int x;
    private int mission_type;
    private int target_value;

    private TextView message_Long;
    private TextView time_text;

    private boolean isDataInit = false;
    private int ticker = 0;
    public boolean targetComplete = false;
    private boolean showBouns = false;


    public PkBounsStartViewHolder(Context context, int duration, int x, int mission_type, int target_value, int targetDuration) {

        this.mContext = context;
        this.duration = duration;
        this.x = x;
        ticker = 0;
        this.mission_type = mission_type;
        this.target_value = target_value;
        this.targDuration = targetDuration;
        isDataInit = true;
        mView = LayoutInflater.from(mContext).inflate(R.layout.pk_bouns_start_view, null, false);
        init();
    }


    public void init() {
        message_Long = mView.findViewById(R.id.message_Long);
        time_text = mView.findViewById(R.id.timer_text);
        String text1 = "" + target_value + "/0";
        if (mission_type == 1) {
            ((ImageView) mView.findViewById(R.id.img)).setImageResource(com.livestreaming.common.R.drawable.ic_gifter);
        } else if (mission_type == 2) {
            ((ImageView) mView.findViewById(R.id.img)).setImageResource(com.livestreaming.common.R.drawable.ic_coin_g);
        } else if (mission_type == 3) {

            ((ImageView) mView.findViewById(R.id.img)).setImageResource(com.livestreaming.common.R.drawable.ic_gift_pk_bouns);
        }
        message_Long.setText(text1);
    }

    int secToShowTarget = 3;

    public void setTimeTic(String timeStr) {
        if (showBouns) {
            secToShowTarget--;
        }
        if (secToShowTarget == 0) {
            showBounsWillStartSoon();
            secToShowTarget = -1;
        } else {
            if (secToShowTarget != -1) {
                if (ticker < duration) {
                    ticker++;
                    if (time_text != null) {
                        time_text.setText((duration - ticker) + " s");
                    }
                    mView.invalidate();
                }
            }
        }
    }

    private void showBounsWillStartSoon() {
        (mView.findViewById(R.id.target_completed_tv)).setVisibility(View.GONE);
        (mView.findViewById(R.id.layout_parent)).setVisibility(View.GONE);
        (mView.findViewById(R.id.target_will_start_soon_tv)).setVisibility(View.VISIBLE);
        (mView.findViewById(R.id.root)).setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.mpk_bouns)));
        ((TextView) mView.findViewById(R.id.target_will_start_soon_tv)).setText(x + "x " +
                mContext.getString(com.livestreaming.common.R.string.gifted_points)
                + " " + mContext.getString(com.livestreaming.common.R.string.target) + " " +
                mContext.getString(com.livestreaming.common.R.string.will_start_soon)
        );
    }

    public void handleTargetMessage(int current_value) {
        if (!targetComplete) {
            String text1 = "" + target_value + "/" + (target_value - current_value);
            message_Long.setText(text1);
        }

    }

    public View getView() {
        return mView;
    }

    public void targetCompleted(int uid) {
        targetComplete = true;
        (mView.findViewById(R.id.layout_parent)).setVisibility(View.GONE);
        (mView.findViewById(R.id.target_completed_tv)).setVisibility(View.VISIBLE);
        showBouns = true;

    }

    public void targetNotCompleted() {
        if (!targetComplete) {
            (mView.findViewById(R.id.layout_parent)).setVisibility(View.GONE);
            ((ConstraintLayout) mView.findViewById(R.id.root)).setBackgroundTintList(ColorStateList.valueOf(mContext.getColor(com.livestreaming.common.R.color.gray)));
            (mView.findViewById(R.id.target_not_completed_tv)).setVisibility(View.VISIBLE);
        }
    }

    public void handleWhenBounsBegin() {
        (mView.findViewById(R.id.layout_parent)).setVisibility(View.VISIBLE);
        (mView.findViewById(R.id.img)).setVisibility(View.GONE);
        (mView.findViewById(R.id.target_will_start_soon_tv)).setVisibility(View.GONE);
        message_Long.setText(x + "x " + mContext.getString(com.livestreaming.common.R.string.gifted_points));
        ticker = 0;
        time_text.setText(targDuration + " s");

    }

    public void whenTargetStartTicks(String value) {
        ticker++;
        if (time_text != null) {
            time_text.setText((targDuration - ticker) + " s");
        }
    }

    public void showTotalPoints(int totalPoints) {
        (mView.findViewById(R.id.root)).setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.mpk_bouns)));
        (mView.findViewById(R.id.target_completed_tv)).setVisibility(View.GONE);
        (mView.findViewById(R.id.layout_parent)).setVisibility(View.GONE);
        (mView.findViewById(R.id.target_will_start_soon_tv)).setVisibility(View.GONE);
        (mView.findViewById(R.id.message_Long)).setVisibility(View.GONE);
        (mView.findViewById(R.id.target_total_points)).setVisibility(View.VISIBLE);
        ((TextView) mView.findViewById(R.id.target_total_points)).setText(""+totalPoints);
    }
}
