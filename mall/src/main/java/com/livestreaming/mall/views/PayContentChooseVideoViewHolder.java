package com.livestreaming.mall.views;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import androidx.viewpager.widget.ViewPager;

import android.os.Build;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.livestreaming.common.Constants;
import com.livestreaming.common.activity.AbsActivity;
import com.livestreaming.common.activity.ChooseVideoActivity;
import com.livestreaming.common.adapter.ViewPagerAdapter;
import com.livestreaming.common.interfaces.ActivityResultCallback;
import com.livestreaming.common.interfaces.PermissionCallback;
import com.livestreaming.common.utils.ActivityResultUtil;
import com.livestreaming.common.utils.FloatWindowHelper;
import com.livestreaming.common.utils.PermissionUtil;
import com.livestreaming.common.utils.ToastUtil;
import com.livestreaming.common.views.AbsCommonViewHolder;
import com.livestreaming.common.views.AbsLivePageViewHolder;
import com.livestreaming.mall.R;
import com.livestreaming.mall.activity.PayContentPubActivity;
import com.livestreaming.mall.bean.PayContentVideoBean;

import java.util.ArrayList;
import java.util.List;

/**
 * 付费内容 选择视频
 */
public class PayContentChooseVideoViewHolder extends AbsLivePageViewHolder implements View.OnClickListener {

    private static final int PAGE_COUNT = 2;
    private List<FrameLayout> mViewList;
    private AbsCommonViewHolder[] mViewHolders;
    private PayContentSingleViewHolder mSingleViewHolder;
    private PayContentMulViewHolder mMulViewHolder;
    private ViewPager mViewPager;

    public PayContentChooseVideoViewHolder(Context context, ViewGroup parentView) {
        super(context, parentView);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_pay_content_choose_video;
    }

    @Override
    public void init() {
        super.init();
        mViewList = new ArrayList<>();
        for (int i = 0; i < PAGE_COUNT; i++) {
            FrameLayout frameLayout = new FrameLayout(mContext);
            frameLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            mViewList.add(frameLayout);
        }
        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        if (PAGE_COUNT > 1) {
            mViewPager.setOffscreenPageLimit(PAGE_COUNT - 1);
        }
        mViewPager.setAdapter(new ViewPagerAdapter(mViewList));
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                loadPageData(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mViewHolders = new AbsCommonViewHolder[PAGE_COUNT];
        findViewById(R.id.btn_single).setOnClickListener(this);
        findViewById(R.id.btn_mul).setOnClickListener(this);
        findViewById(R.id.btn_save).setOnClickListener(this);
    }

    private void loadPageData(int position) {
        if (mViewHolders == null) {
            return;
        }
        AbsCommonViewHolder vh = mViewHolders[position];
        if (vh == null) {
            if (mViewList != null && position < mViewList.size()) {
                FrameLayout parent = mViewList.get(position);
                if (parent == null) {
                    return;
                }
                if (position == 0) {
                    mSingleViewHolder = new PayContentSingleViewHolder(mContext, parent);
                    vh = mSingleViewHolder;
                } else if (position == 1) {
                    mMulViewHolder = new PayContentMulViewHolder(mContext, parent);
                    vh = mMulViewHolder;
                }
                if (vh == null) {
                    return;
                }
                mViewHolders[position] = vh;
                vh.addToParent();
                vh.subscribeActivityLifeCycle();
            }
        }
        if (vh != null) {
            vh.loadData();
        }
    }

    @Override
    public void loadData() {
        if (!mLoad) {
            mLoad = true;
            loadPageData(0);
        }
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        int id = v.getId();
        if (id == R.id.btn_single) {
            if (mViewPager != null && mViewPager.getCurrentItem() != 0) {
                mViewPager.setCurrentItem(0, false);
            }
        } else if (id == R.id.btn_mul) {
            if (mViewPager != null && mViewPager.getCurrentItem() != 1) {
                mViewPager.setCurrentItem(1, false);
            }
        } else if (id == R.id.btn_save) {
            save();
        }
    }

    /**
     * 选择视频
     */
    public void chooseVideo() {
        if (!FloatWindowHelper.checkVoice(false)) {
            return;
        }

        if (Build.VERSION.SDK_INT >33) {
            PermissionUtil.request((AbsActivity) mContext, new PermissionCallback() {
                        @Override
                        public void onAllGranted() {
                            Intent intent = new Intent(mContext, ChooseVideoActivity.class);
                            ActivityResultUtil.startActivityForResult(((AbsActivity) mContext), intent, new ActivityResultCallback() {
                                @Override
                                public void onSuccess(Intent intent) {
                                    String videoPath = intent.getStringExtra(Constants.VIDEO_PATH);
                                    long videoDuration = intent.getLongExtra(Constants.VIDEO_DURATION, 0);
                                    if (!TextUtils.isEmpty(videoPath)) {
                                        if (mViewPager != null && mViewPager.getCurrentItem() == 0) {
                                            if (mSingleViewHolder != null) {
                                                mSingleViewHolder.setFilePath(videoPath, String.valueOf(videoDuration / 1000));
                                            }
                                        } else {
                                            if (mMulViewHolder != null) {
                                                mMulViewHolder.setFilePath(videoPath, String.valueOf(videoDuration / 1000));
                                            }
                                        }
                                    }
                                }
                            });
                        }
                    },
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_AUDIO,
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED,
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO);
        }else if (Build.VERSION.SDK_INT ==33) {
            PermissionUtil.request((AbsActivity) mContext, new PermissionCallback() {
                        @Override
                        public void onAllGranted() {
                            Intent intent = new Intent(mContext, ChooseVideoActivity.class);
                            ActivityResultUtil.startActivityForResult(((AbsActivity) mContext), intent, new ActivityResultCallback() {
                                @Override
                                public void onSuccess(Intent intent) {
                                    String videoPath = intent.getStringExtra(Constants.VIDEO_PATH);
                                    long videoDuration = intent.getLongExtra(Constants.VIDEO_DURATION, 0);
                                    if (!TextUtils.isEmpty(videoPath)) {
                                        if (mViewPager != null && mViewPager.getCurrentItem() == 0) {
                                            if (mSingleViewHolder != null) {
                                                mSingleViewHolder.setFilePath(videoPath, String.valueOf(videoDuration / 1000));
                                            }
                                        } else {
                                            if (mMulViewHolder != null) {
                                                mMulViewHolder.setFilePath(videoPath, String.valueOf(videoDuration / 1000));
                                            }
                                        }
                                    }
                                }
                            });
                        }
                    },
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_AUDIO,
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO);
        }else{
            PermissionUtil.request((AbsActivity) mContext, new PermissionCallback() {
                        @Override
                        public void onAllGranted() {
                            Intent intent = new Intent(mContext, ChooseVideoActivity.class);
                            ActivityResultUtil.startActivityForResult(((AbsActivity) mContext), intent, new ActivityResultCallback() {
                                @Override
                                public void onSuccess(Intent intent) {
                                    String videoPath = intent.getStringExtra(Constants.VIDEO_PATH);
                                    long videoDuration = intent.getLongExtra(Constants.VIDEO_DURATION, 0);
                                    if (!TextUtils.isEmpty(videoPath)) {
                                        if (mViewPager != null && mViewPager.getCurrentItem() == 0) {
                                            if (mSingleViewHolder != null) {
                                                mSingleViewHolder.setFilePath(videoPath, String.valueOf(videoDuration / 1000));
                                            }
                                        } else {
                                            if (mMulViewHolder != null) {
                                                mMulViewHolder.setFilePath(videoPath, String.valueOf(videoDuration / 1000));
                                            }
                                        }
                                    }
                                }
                            });
                        }
                    },
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO);
        }
    }

    /**
     * 保存
     */
    private void save() {
        if (mViewPager == null) {
            return;
        }
        if (mViewPager.getCurrentItem() == 0) {
            if (mSingleViewHolder != null) {
                PayContentVideoBean bean = mSingleViewHolder.getPayContentVideoBean();
                if (bean == null || !bean.hasFile()) {
                    ToastUtil.show(com.livestreaming.common.R.string.mall_333);
                    return;
                }
                ((PayContentPubActivity) mContext).setVideoList(bean);
                hide();
            }
        } else {
            if (mMulViewHolder != null) {
                List<PayContentVideoBean> list = mMulViewHolder.getList();
                if (list != null && list.size() > 0) {
                    for (PayContentVideoBean bean : list) {
                        if (!bean.hasFile()) {
                            ToastUtil.show(com.livestreaming.common.R.string.mall_333);
                            return;
                        }
                        if (TextUtils.isEmpty(bean.getTitle())) {
                            ToastUtil.show(com.livestreaming.common.R.string.mall_334);
                            return;
                        }
                    }
                    ((PayContentPubActivity) mContext).setVideoList(list);
                    hide();
                }
            }
        }
    }


}
