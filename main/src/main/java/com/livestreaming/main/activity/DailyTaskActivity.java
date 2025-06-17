package com.livestreaming.main.activity;

import static com.livestreaming.common.Constants.currenTimerTime;
import static com.livestreaming.common.Constants.lastEnteradTime;
import static com.livestreaming.common.banner.util.LogUtils.TAG;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.livestreaming.common.CommonAppConfig;
import com.livestreaming.common.Constants;
import com.livestreaming.common.activity.AbsActivity;
import com.livestreaming.common.activity.WebViewActivity;
import com.livestreaming.common.http.HttpCallback;
import com.livestreaming.common.utils.MyCountdown;
import com.livestreaming.common.utils.WordUtil;
import com.livestreaming.live.http.LiveHttpConsts;
import com.livestreaming.live.http.LiveHttpUtil;
import com.livestreaming.main.R;
import com.livestreaming.live.adapter.DailyTaskAdapter;
import com.livestreaming.live.bean.DailyTaskBean;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

import java.util.List;

public class DailyTaskActivity extends AbsActivity {

    private  String REWARDED_AD_UNIT_ID = "";
    private RewardedAd rewardedAd;
    private int adCount = 0;

    private int activationState = 0;
    private MyCountdown myCountDown;
    private boolean canclickReward = false;
    private int ads_reward_time = 0;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_daily_task;
    }

    @Override
    protected void main() {
        setTitle(WordUtil.getString(com.livestreaming.common.R.string.daily_task));
        //REWARDED_AD_UNIT_ID=getString(R.string.r_u_id);
        REWARDED_AD_UNIT_ID="ca-app-pub-3940256099942544/5224354917";
        if(Constants.currenTimerTime>0){
            lastEnteradTime=System.currentTimeMillis()-lastEnteradTime;
            lastEnteradTime/=1000;
            Constants.currenTimerTime-= (int) lastEnteradTime;
            if(currenTimerTime<0)currenTimerTime=0;
        }
        ads_reward_time = Constants.currenTimerTime;
        canclickReward = (ads_reward_time == 0);
        if (ads_reward_time > 0) {
            startRewardTimer();
        }
//        findViewById(com.livestreaming.live.R.id.mesisson_increase_layout).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                WebViewActivity.forward(mContext, getIncreaseTermsLink());
//            }
//        });
        loadRewardedAd();
        // Set up button click listener
        findViewById(com.livestreaming.live.R.id.video_mission_rewards).setOnClickListener(v -> {
            if (canclickReward) {
                if (adCount == 0) {
                    // Limit reached
                    Toast.makeText(mContext, getString(com.livestreaming.common.R.string.ad_limit_reached_for_today), Toast.LENGTH_LONG).show();
                } else if (rewardedAd != null) {
                    if (adCount > 0) {
                        rewardedAd.show(DailyTaskActivity.this, rewardItem -> {
                            LiveHttpUtil.updateWatchAdReward(new HttpCallback() {

                                @Override
                                public void onSuccess(int code, String msg, String[] info) {
                                    if (info.length > 0) {
                                        JSONObject obj = JSONObject.parseObject(info[0]);
                                        Toast.makeText(mContext, getString(com.livestreaming.common.R.string.you_ve_earned) + " " + obj.getIntValue("coin") , Toast.LENGTH_LONG).show();
                                        adCount--;
                                        if (adCount == 0) {
                                            findViewById(com.livestreaming.live.R.id.video_mission_rewards).setVisibility(View.GONE);
                                            Toast.makeText(mContext, getString(com.livestreaming.common.R.string.ad_limit_reached_for_today), Toast.LENGTH_LONG).show();
                                        } else {
                                            startRewardTimer();
                                        }
                                    }
                                }
                            });
                        });
                    } else {
                        Toast.makeText(mContext, getString(com.livestreaming.common.R.string.ad_limit_reached_for_today), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(mContext, getString(com.livestreaming.common.R.string.loading), Toast.LENGTH_LONG).show();
                }
            }
        });
        // Set up button click listener
        LiveHttpUtil.getDailyTask(CommonAppConfig.getInstance().getUid(), 1, new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0 && info.length > 0) {
                    JSONObject obj = JSON.parseObject(info[0]);
                    TextView tip = findViewById(com.livestreaming.live.R.id.tip);
                    tip.setText(obj.getString("tip_m"));
                    activationState = obj.getIntValue("active_state");
                    adCount = obj.getIntValue("ads_count");
                    if (activationState > 0) {
                       // findViewById(com.livestreaming.live.R.id.mesisson_increase_layout).setVisibility(View.GONE);
                        if (adCount > 0) {
                            findViewById(com.livestreaming.live.R.id.video_mission_rewards).setVisibility(View.VISIBLE);
                        } else {
                            findViewById(com.livestreaming.live.R.id.video_mission_rewards).setVisibility(View.GONE);
                        }
                    } else {
                        findViewById(com.livestreaming.live.R.id.video_mission_rewards).setVisibility(View.GONE);
                    }
                    RecyclerView recyclerView = findViewById(com.livestreaming.live.R.id.recyclerView);
                    recyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
                    List<DailyTaskBean> list = JSON.parseArray(obj.getString("list"), DailyTaskBean.class);
                    recyclerView.setAdapter(new DailyTaskAdapter(mContext, list));
                }
            }
        });
    }

    private String getIncreaseTermsLink() {
        return "https://donalive.net/appapi/tasks/terms?uid=" + Integer.parseInt(CommonAppConfig.getInstance().getUid()) + "&token=" + CommonAppConfig.getInstance().getToken() + "&language=" + Constants.CUR_LANGUAGE;
    }

    @Override
    protected void onDestroy() {
        LiveHttpUtil.cancel(LiveHttpConsts.GET_DAILY_TASK);
        LiveHttpUtil.cancel(LiveHttpConsts.DAILY_TASK_REWARD);
        if(myCountDown!=null){
            myCountDown.stop();
            myCountDown.release();
            lastEnteradTime=System.currentTimeMillis();
        }
        super.onDestroy();
    }

    private void startRewardTimer() {
        if (ads_reward_time <= 0) {
            ads_reward_time = 5 * 60;
        }
        ((TextView) findViewById(com.livestreaming.live.R.id.ads_timer)).setVisibility(View.VISIBLE);
        myCountDown = new MyCountdown();
        canclickReward = false;
        myCountDown.setTotalSecond(ads_reward_time).setCallback(new MyCountdown.ActionListener() {
            @Override
            public void onTimeChanged(String timeStr) {
                ads_reward_time--;
                Constants.currenTimerTime=ads_reward_time;
                ((TextView) findViewById(com.livestreaming.live.R.id.ads_timer)).setText(timeStr);
            }

            @Override
            public void onTimeEnd() {
                canclickReward = true;
                Constants.currenTimerTime=0;
                ((TextView) findViewById(com.livestreaming.live.R.id.ads_timer)).setVisibility(View.GONE);
            }
        }).start();
    }

    private void loadRewardedAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        RewardedAd.load(mContext, REWARDED_AD_UNIT_ID, adRequest, new RewardedAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull RewardedAd ad) {
                rewardedAd = ad;
                Log.d(TAG, "Rewarded ad loaded successfully.");
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                rewardedAd = null;
                Log.e(TAG, "Failed to load rewarded ad: " + adError.getMessage());
            }
        });
    }
}
