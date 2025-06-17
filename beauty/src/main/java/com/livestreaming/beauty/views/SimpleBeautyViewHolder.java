package com.livestreaming.beauty.views;

import android.content.Context;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.SeekBar;
import android.widget.TextView;

import com.livestreaming.beauty.R;
import com.livestreaming.beauty.adapter.SimpleFilterAdapter;
import com.livestreaming.beauty.bean.SimpleFilterBean;
import com.livestreaming.beauty.interfaces.IBeautyViewHolder;
import com.livestreaming.beauty.utils.SimpleDataManager;
import com.livestreaming.common.interfaces.OnItemClickListener;
import com.livestreaming.common.views.AbsViewHolder;

public class SimpleBeautyViewHolder extends AbsViewHolder implements IBeautyViewHolder, View.OnClickListener, OnItemClickListener<SimpleFilterBean> {

    private View mGroupBeauty;
    private View mGroupFilter;
    private TextView mTvMeiBai;
    private TextView mTvMoPi;
    private TextView mTvHongRun;
    private boolean mShowed;
    private VisibleListener mVisibleListener;
    private boolean mIsTxSDK;

    public SimpleBeautyViewHolder(Context context, ViewGroup parentView,boolean isTxSDK) {
        super(context, parentView,isTxSDK);
    }

    @Override
    protected void processArguments(Object... args) {
        mIsTxSDK= (boolean) args[0];
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_beauty_simple;
    }

    @Override
    public void init() {
        mGroupBeauty = findViewById(R.id.group_beauty);
        mGroupFilter = findViewById(R.id.group_filter);
        findViewById(R.id.btn_hide).setOnClickListener(this);
        findViewById(R.id.btn_beauty).setOnClickListener(this);
        findViewById(R.id.btn_filter).setOnClickListener(this);
        SeekBar seekBarMeiBai = findViewById(R.id.seek_bar_meibai);
        SeekBar seekBarMoPi = findViewById(R.id.seek_bar_mopi);
        SeekBar seekBarHongRun = findViewById(R.id.seek_bar_hongrun);
        if(!mIsTxSDK){
            findViewById(R.id.bottom).setVisibility(View.GONE);
            seekBarMeiBai.setMax(10);
            seekBarMoPi.setMax(10);
            seekBarHongRun.setMax(10);
        }
        mTvMeiBai = findViewById(R.id.text_meibai);
        mTvMoPi = findViewById(R.id.text_mopi);
        mTvHongRun = findViewById(R.id.text_hongrun);
        SeekBar.OnSeekBarChangeListener listener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int id = seekBar.getId();
                if (id == R.id.seek_bar_meibai) {
                    if (mTvMeiBai != null) {
                        mTvMeiBai.setText(String.valueOf(progress));
                    }
                    SimpleDataManager.getInstance().setMeiBai(progress);
                } else if (id == R.id.seek_bar_mopi) {
                    if (mTvMoPi != null) {
                        mTvMoPi.setText(String.valueOf(progress));
                    }
                    SimpleDataManager.getInstance().setMoPi(progress);
                } else if (id == R.id.seek_bar_hongrun) {
                    if (mTvHongRun != null) {
                        mTvHongRun.setText(String.valueOf(progress));
                    }
                    SimpleDataManager.getInstance().setHongRun(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        };
        seekBarMeiBai.setOnSeekBarChangeListener(listener);
        seekBarMoPi.setOnSeekBarChangeListener(listener);
        seekBarHongRun.setOnSeekBarChangeListener(listener);

        seekBarMeiBai.setProgress(SimpleDataManager.getInstance().getMeiBai());
        seekBarMoPi.setProgress(SimpleDataManager.getInstance().getMoPi());
        seekBarHongRun.setProgress(SimpleDataManager.getInstance().getHongRun());


        RecyclerView filterRecyclerView = (RecyclerView) findViewById(R.id.filter_recyclerView);
        filterRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
        SimpleFilterAdapter adapter = new SimpleFilterAdapter(mContext);
        adapter.setOnItemClickListener(this);
        filterRecyclerView.setAdapter(adapter);
    }

    @Override
    public void show() {
        if (mVisibleListener != null) {
            mVisibleListener.onVisibleChanged(true);
        }
        if (mParentView != null && mContentView != null) {
            ViewParent parent = mContentView.getParent();
            if (parent != null) {
                ((ViewGroup) parent).removeView(mContentView);
            }
            mParentView.addView(mContentView);
        }
        mShowed = true;
    }

    @Override
    public void hide() {
        removeFromParent();
        if (mVisibleListener != null) {
            mVisibleListener.onVisibleChanged(false);
        }
        mShowed = false;
        SimpleDataManager.getInstance().saveBeautyValue();
    }

    @Override
    public boolean isShowed() {
        return mShowed;
    }

    @Override
    public void setVisibleListener(VisibleListener visibleListener) {
        mVisibleListener = visibleListener;
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_hide) {
            hide();
        } else if (i == R.id.btn_beauty) {
            if (mGroupBeauty != null && mGroupBeauty.getVisibility() != View.VISIBLE) {
                mGroupBeauty.setVisibility(View.VISIBLE);
            }
            if (mGroupFilter != null && mGroupFilter.getVisibility() != View.GONE) {
                mGroupFilter.setVisibility(View.GONE);
            }
        } else if (i == R.id.btn_filter) {
            if (mGroupBeauty != null && mGroupBeauty.getVisibility() != View.GONE) {
                mGroupBeauty.setVisibility(View.GONE);
            }
            if (mGroupFilter != null && mGroupFilter.getVisibility() != View.VISIBLE) {
                mGroupFilter.setVisibility(View.VISIBLE);
            }
        }
    }


    @Override
    public void onItemClick(SimpleFilterBean bean, int position) {
        SimpleDataManager.getInstance().setFilter(bean.getFilterSrc());
    }
}
