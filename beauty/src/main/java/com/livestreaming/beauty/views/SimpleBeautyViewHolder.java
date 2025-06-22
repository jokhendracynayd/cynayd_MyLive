package com.livestreaming.beauty.views;

import android.content.Context;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.livestreaming.beauty.R;
import com.livestreaming.beauty.adapter.SimpleFilterAdapter;
import com.livestreaming.beauty.bean.SimpleFilterBean;
import com.livestreaming.beauty.interfaces.IBeautyViewHolder;
import com.livestreaming.beauty.utils.SimpleDataManager;
import com.livestreaming.common.interfaces.OnItemClickListener;
import com.livestreaming.common.views.AbsViewHolder;

public class SimpleBeautyViewHolder extends AbsViewHolder implements IBeautyViewHolder, View.OnClickListener, OnItemClickListener<SimpleFilterBean> {

    private static final String TAG = "SimpleBeautyViewHolder";
    private View mGroupBeauty;
    private View mGroupFilter;
    private View mGroupAdvanced;
    private TextView mTvMeiBai;
    private TextView mTvMoPi;
    private TextView mTvHongRun;
    private TextView mTvBrightness;
    private TextView mTvSharpness;
    private TextView mTvFaceSlim;
    private TextView mTvBigEye;
    private TextView mTvJaw;
    private TextView mTvEyeDistance;
    private TextView mTvFaceShape;
    private TextView mTvEyeBrow;
    private TextView mTvEyeCorner;
    private TextView mTvMouseLift;
    private TextView mTvNoseLift;
    private TextView mTvLengthenNose;
    private TextView mTvEyeAlat;
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
        mGroupAdvanced = findViewById(R.id.group_advanced);
        
        // Verify that the groups are properly initialized
        if (mGroupBeauty == null) {
            Log.e(TAG, "Beauty group view is null");
        }
        
        if (mGroupAdvanced == null) {
            Log.e(TAG, "Advanced group view is null");
        }
        
        if (mGroupFilter == null) {
            Log.e(TAG, "Filter group view is null");
        }
        
        // Move the advanced features into the basic beauty group
        if (mGroupBeauty != null && mGroupAdvanced != null) {
            ViewGroup beautyGroup = (ViewGroup) mGroupBeauty;
            ViewGroup advancedGroup = (ViewGroup) mGroupAdvanced;
            
            // Get the three columns from advanced group
            if (advancedGroup.getChildCount() >= 3) {
                View leftColumn = advancedGroup.getChildAt(0);
                View middleColumn = advancedGroup.getChildAt(1);
                View rightColumn = advancedGroup.getChildAt(2);
                
                // Get the three columns from beauty group
                if (beautyGroup.getChildCount() >= 3) {
                    ViewGroup leftBeautyColumn = (ViewGroup) beautyGroup.getChildAt(0);
                    ViewGroup middleBeautyColumn = (ViewGroup) beautyGroup.getChildAt(1);
                    ViewGroup rightBeautyColumn = (ViewGroup) beautyGroup.getChildAt(2);
                    
                    // Add advanced features to basic beauty columns
                    if (leftColumn instanceof ViewGroup && 
                        middleColumn instanceof ViewGroup && 
                        rightColumn instanceof ViewGroup) {
                        
                        // Copy children from advanced to beauty columns
                        copyChildren((ViewGroup)leftColumn, leftBeautyColumn);
                        copyChildren((ViewGroup)middleColumn, middleBeautyColumn);
                        copyChildren((ViewGroup)rightColumn, rightBeautyColumn);
                    }
                }
            }
        }
        
        // Hide the advanced group
        if (mGroupAdvanced != null) {
            mGroupAdvanced.setVisibility(View.GONE);
        }
        
        View btnHide = findViewById(R.id.btn_hide);
        View btnBeauty = findViewById(R.id.btn_beauty);
        View btnFilter = findViewById(R.id.btn_filter);
        
        if (btnHide != null) {
            btnHide.setOnClickListener(this);
        } else {
            Log.e(TAG, "Hide button is null");
        }
        
        if (btnBeauty != null) {
            btnBeauty.setOnClickListener(this);
        } else {
            Log.e(TAG, "Beauty button is null");
        }
        
        if (btnFilter != null) {
            btnFilter.setOnClickListener(this);
        } else {
            Log.e(TAG, "Filter button is null");
        }
        
        // Basic beauty seekbars
        SeekBar seekBarMeiBai = findViewById(R.id.seek_bar_meibai);
        SeekBar seekBarMoPi = findViewById(R.id.seek_bar_mopi);
        SeekBar seekBarHongRun = findViewById(R.id.seek_bar_hongrun);
        SeekBar seekBarBrightness = findViewById(R.id.seek_bar_brightness);
        SeekBar seekBarSharpness = findViewById(R.id.seek_bar_sharpness);
        
        // Advanced beauty seekbars
        SeekBar seekBarFaceSlim = findViewById(R.id.seek_bar_face_slim);
        SeekBar seekBarBigEye = findViewById(R.id.seek_bar_big_eye);
        SeekBar seekBarJaw = findViewById(R.id.seek_bar_jaw);
        SeekBar seekBarEyeDistance = findViewById(R.id.seek_bar_eye_distance);
        SeekBar seekBarFaceShape = findViewById(R.id.seek_bar_face_shape);
        
        // New beauty seekbars
        SeekBar seekBarEyeBrow = findViewById(R.id.seek_bar_eye_brow);
        SeekBar seekBarEyeCorner = findViewById(R.id.seek_bar_eye_corner);
        SeekBar seekBarMouseLift = findViewById(R.id.seek_bar_mouse_lift);
        SeekBar seekBarNoseLift = findViewById(R.id.seek_bar_nose_lift);
        SeekBar seekBarLengthenNose = findViewById(R.id.seek_bar_lengthen_nose);
        SeekBar seekBarEyeAlat = findViewById(R.id.seek_bar_eye_alat);
        
        if(!mIsTxSDK){
            findViewById(R.id.bottom).setVisibility(View.GONE);
            seekBarMeiBai.setMax(10);
            seekBarMoPi.setMax(10);
            seekBarHongRun.setMax(10);
            seekBarBrightness.setMax(10);
            seekBarSharpness.setMax(10);
            seekBarFaceSlim.setMax(10);
            seekBarBigEye.setMax(10);
            seekBarJaw.setMax(10);
            seekBarEyeDistance.setMax(10);
            seekBarFaceShape.setMax(10);
            seekBarEyeBrow.setMax(10);
            seekBarEyeCorner.setMax(10);
            seekBarMouseLift.setMax(10);
            seekBarNoseLift.setMax(10);
            seekBarLengthenNose.setMax(10);
            seekBarEyeAlat.setMax(10);
        }
        
        // Basic beauty text views
        mTvMeiBai = findViewById(R.id.text_meibai);
        mTvMoPi = findViewById(R.id.text_mopi);
        mTvHongRun = findViewById(R.id.text_hongrun);
        mTvBrightness = findViewById(R.id.text_brightness);
        mTvSharpness = findViewById(R.id.text_sharpness);
        
        // Advanced beauty text views
        mTvFaceSlim = findViewById(R.id.text_face_slim);
        mTvBigEye = findViewById(R.id.text_big_eye);
        mTvJaw = findViewById(R.id.text_jaw);
        mTvEyeDistance = findViewById(R.id.text_eye_distance);
        mTvFaceShape = findViewById(R.id.text_face_shape);
        
        // New beauty text views
        mTvEyeBrow = findViewById(R.id.text_eye_brow);
        mTvEyeCorner = findViewById(R.id.text_eye_corner);
        mTvMouseLift = findViewById(R.id.text_mouse_lift);
        mTvNoseLift = findViewById(R.id.text_nose_lift);
        mTvLengthenNose = findViewById(R.id.text_lengthen_nose);
        mTvEyeAlat = findViewById(R.id.text_eye_alat);
        
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
                } else if (id == R.id.seek_bar_brightness) {
                    if (mTvBrightness != null) {
                        mTvBrightness.setText(String.valueOf(progress));
                    }
                    SimpleDataManager.getInstance().setBrightness(progress);
                } else if (id == R.id.seek_bar_sharpness) {
                    if (mTvSharpness != null) {
                        mTvSharpness.setText(String.valueOf(progress));
                    }
                    SimpleDataManager.getInstance().setSharpness(progress);
                } else if (id == R.id.seek_bar_face_slim) {
                    if (mTvFaceSlim != null) {
                        mTvFaceSlim.setText(String.valueOf(progress));
                    }
                    SimpleDataManager.getInstance().setFaceSlim(progress);
                } else if (id == R.id.seek_bar_big_eye) {
                    if (mTvBigEye != null) {
                        mTvBigEye.setText(String.valueOf(progress));
                    }
                    SimpleDataManager.getInstance().setBigEye(progress);
                } else if (id == R.id.seek_bar_jaw) {
                    if (mTvJaw != null) {
                        mTvJaw.setText(String.valueOf(progress));
                    }
                    SimpleDataManager.getInstance().setJaw(progress);
                } else if (id == R.id.seek_bar_eye_distance) {
                    if (mTvEyeDistance != null) {
                        mTvEyeDistance.setText(String.valueOf(progress));
                    }
                    SimpleDataManager.getInstance().setEyeDistance(progress);
                } else if (id == R.id.seek_bar_face_shape) {
                    if (mTvFaceShape != null) {
                        mTvFaceShape.setText(String.valueOf(progress));
                    }
                    SimpleDataManager.getInstance().setFaceShape(progress);
                } else if (id == R.id.seek_bar_eye_brow) {
                    if (mTvEyeBrow != null) {
                        mTvEyeBrow.setText(String.valueOf(progress));
                    }
                    SimpleDataManager.getInstance().setEyeBrow(progress);
                } else if (id == R.id.seek_bar_eye_corner) {
                    if (mTvEyeCorner != null) {
                        mTvEyeCorner.setText(String.valueOf(progress));
                    }
                    SimpleDataManager.getInstance().setEyeCorner(progress);
                } else if (id == R.id.seek_bar_mouse_lift) {
                    if (mTvMouseLift != null) {
                        mTvMouseLift.setText(String.valueOf(progress));
                    }
                    SimpleDataManager.getInstance().setMouthShape(progress);
                } else if (id == R.id.seek_bar_nose_lift) {
                    if (mTvNoseLift != null) {
                        mTvNoseLift.setText(String.valueOf(progress));
                    }
                    SimpleDataManager.getInstance().setNoseLift(progress);
                } else if (id == R.id.seek_bar_lengthen_nose) {
                    if (mTvLengthenNose != null) {
                        mTvLengthenNose.setText(String.valueOf(progress));
                    }
                    SimpleDataManager.getInstance().setLongNose(progress);
                } else if (id == R.id.seek_bar_eye_alat) {
                    if (mTvEyeAlat != null) {
                        mTvEyeAlat.setText(String.valueOf(progress));
                    }
                    SimpleDataManager.getInstance().setEyeAlat(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Save beauty values to server whenever any seekbar is adjusted
                SimpleDataManager.getInstance().saveBeautyValue();
            }
        };
        
        // Set seek bar listeners for basic beauty
        seekBarMeiBai.setOnSeekBarChangeListener(listener);
        seekBarMoPi.setOnSeekBarChangeListener(listener);
        seekBarHongRun.setOnSeekBarChangeListener(listener);
        seekBarBrightness.setOnSeekBarChangeListener(listener);
        seekBarSharpness.setOnSeekBarChangeListener(listener);
        
        // Set seek bar listeners for advanced beauty
        seekBarFaceSlim.setOnSeekBarChangeListener(listener);
        seekBarBigEye.setOnSeekBarChangeListener(listener);
        seekBarJaw.setOnSeekBarChangeListener(listener);
        seekBarEyeDistance.setOnSeekBarChangeListener(listener);
        seekBarFaceShape.setOnSeekBarChangeListener(listener);
        
        // Set seek bar listeners for new beauty parameters
        seekBarEyeBrow.setOnSeekBarChangeListener(listener);
        seekBarEyeCorner.setOnSeekBarChangeListener(listener);
        seekBarMouseLift.setOnSeekBarChangeListener(listener);
        seekBarNoseLift.setOnSeekBarChangeListener(listener);
        seekBarLengthenNose.setOnSeekBarChangeListener(listener);
        seekBarEyeAlat.setOnSeekBarChangeListener(listener);

        // Set initial values for basic beauty
        SimpleDataManager dataManager = SimpleDataManager.getInstance();
        seekBarMeiBai.setProgress(dataManager.getMeiBai());
        seekBarMoPi.setProgress(dataManager.getMoPi());
        seekBarHongRun.setProgress(dataManager.getHongRun());
        seekBarBrightness.setProgress(dataManager.getBrightness());
        seekBarSharpness.setProgress(dataManager.getSharpness());
        
        // Set initial values for advanced beauty
        seekBarFaceSlim.setProgress(dataManager.getFaceSlim());
        seekBarBigEye.setProgress(dataManager.getBigEye());
        seekBarJaw.setProgress(dataManager.getJaw());
        seekBarEyeDistance.setProgress(dataManager.getEyeDistance());
        seekBarFaceShape.setProgress(dataManager.getFaceShape());
        
        // Set initial values for new beauty parameters
        seekBarEyeBrow.setProgress(dataManager.getEyeBrow());
        seekBarEyeCorner.setProgress(dataManager.getEyeCorner());
        seekBarMouseLift.setProgress(dataManager.getMouthShape());
        seekBarNoseLift.setProgress(dataManager.getNoseLift());
        seekBarLengthenNose.setProgress(dataManager.getLongNose());
        seekBarEyeAlat.setProgress(dataManager.getEyeAlat());

        // Set text values for basic beauty
        mTvMeiBai.setText(String.valueOf(dataManager.getMeiBai()));
        mTvMoPi.setText(String.valueOf(dataManager.getMoPi()));
        mTvHongRun.setText(String.valueOf(dataManager.getHongRun()));
        mTvBrightness.setText(String.valueOf(dataManager.getBrightness()));
        mTvSharpness.setText(String.valueOf(dataManager.getSharpness()));
        
        // Set text values for advanced beauty
        mTvFaceSlim.setText(String.valueOf(dataManager.getFaceSlim()));
        mTvBigEye.setText(String.valueOf(dataManager.getBigEye()));
        mTvJaw.setText(String.valueOf(dataManager.getJaw()));
        mTvEyeDistance.setText(String.valueOf(dataManager.getEyeDistance()));
        mTvFaceShape.setText(String.valueOf(dataManager.getFaceShape()));
        
        // Set text values for new beauty parameters
        mTvEyeBrow.setText(String.valueOf(dataManager.getEyeBrow()));
        mTvEyeCorner.setText(String.valueOf(dataManager.getEyeCorner()));
        mTvMouseLift.setText(String.valueOf(dataManager.getMouthShape()));
        mTvNoseLift.setText(String.valueOf(dataManager.getNoseLift()));
        mTvLengthenNose.setText(String.valueOf(dataManager.getLongNose()));
        mTvEyeAlat.setText(String.valueOf(dataManager.getEyeAlat()));

        RecyclerView filterRecyclerView = (RecyclerView) findViewById(R.id.filter_recyclerView);
        filterRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
        SimpleFilterAdapter adapter = new SimpleFilterAdapter(mContext);
        adapter.setOnItemClickListener(this);
        filterRecyclerView.setAdapter(adapter);
        
        // Set default tab to beauty
        showBeautyTab();
        
        // Simplify tab UI - hide the advanced tab button
        RadioButton advancedBtn = findViewById(R.id.btn_advanced);
        if (advancedBtn != null) {
            advancedBtn.setVisibility(View.GONE);
        }
        
        // Hide the divider after beauty button
        View divider = ((ViewGroup)findViewById(R.id.bottom)).getChildAt(1);
        if (divider != null) {
            divider.setVisibility(View.GONE);
        }
    }
    
    // Helper method to copy children from one ViewGroup to another
    private void copyChildren(ViewGroup source, ViewGroup target) {
        if (source == null || target == null) return;
        
        for (int i = 0; i < source.getChildCount(); i++) {
            View child = source.getChildAt(i);
            if (child != null) {
                // Create a copy of the view
                View copy = View.inflate(mContext, child.getId(), null);
                if (copy != null) {
                    target.addView(copy);
                }
            }
        }
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
    
    private void showBeautyTab() {
        Log.d(TAG, "Showing Beauty Tab");
        if (mGroupBeauty != null && mGroupBeauty.getVisibility() != View.VISIBLE) {
            mGroupBeauty.setVisibility(View.VISIBLE);
        }
        if (mGroupFilter != null && mGroupFilter.getVisibility() != View.GONE) {
            mGroupFilter.setVisibility(View.GONE);
        }
        
        RadioButton beautyBtn = findViewById(R.id.btn_beauty);
        if (beautyBtn != null) {
            beautyBtn.setChecked(true);
        }
    }
    
    private void showFilterTab() {
        Log.d(TAG, "Showing Filter Tab");
        if (mGroupBeauty != null && mGroupBeauty.getVisibility() != View.GONE) {
            mGroupBeauty.setVisibility(View.GONE);
        }
        if (mGroupFilter != null && mGroupFilter.getVisibility() != View.VISIBLE) {
            mGroupFilter.setVisibility(View.VISIBLE);
        }
        
        RadioButton filterBtn = findViewById(R.id.btn_filter);
        if (filterBtn != null) {
            filterBtn.setChecked(true);
        }
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_hide) {
            hide();
        } else if (i == R.id.btn_beauty) {
            showBeautyTab();
        } else if (i == R.id.btn_filter) {
            showFilterTab();
        }
    }

    @Override
    public void onItemClick(SimpleFilterBean bean, int position) {
        SimpleDataManager.getInstance().setFilter(bean.getFilterSrc());
    }
}
