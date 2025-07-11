package com.livestreaming.common.activity;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

import com.livestreaming.common.Constants;
import com.livestreaming.common.R;
import com.livestreaming.common.adapter.ChooseVideoAdapter;
import com.livestreaming.common.bean.ChooseImageBean;
import com.livestreaming.common.bean.ChooseVideoBean;
import com.livestreaming.common.custom.ItemDecoration;
import com.livestreaming.common.dialog.VideoPreviewDialog;
import com.livestreaming.common.interfaces.CommonCallback;
import com.livestreaming.common.interfaces.VideoResultCallback;
import com.livestreaming.common.utils.ChooseVideoUtil;
import com.livestreaming.common.utils.MediaUtil;
import com.livestreaming.common.utils.WordUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 选择视频
 */
public class ChooseVideoActivity extends AbsActivity implements ChooseVideoAdapter.ActionListener {

    private RecyclerView mRecyclerView;
    private ChooseVideoAdapter mAdapter;
    private ChooseVideoUtil mChooseVideoUtil;
    private boolean mUseCamera;//是否使用拍照
    private boolean mUsePreview;//是否使用预览
    private ActivityResultLauncher<String[]> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permissions -> {
                MediaUtil.startVideoRecord(this, new VideoResultCallback() {
                    @Override
                    public void onSuccess(File file, long duration) {
                        Intent intent = new Intent();
                        intent.putExtra(Constants.VIDEO_PATH, file.getAbsolutePath());
                        intent.putExtra(Constants.VIDEO_DURATION, duration);
                        setResult(RESULT_OK, intent);
                        finish();
                    }

                    @Override
                    public void onFailure() {

                    }
                });
            });

                @Override
    protected int getLayoutId() {
        return R.layout.activity_choose_video;
    }


    @Override
    protected void main() {
        Intent intent = getIntent();
        mUseCamera = intent.getBooleanExtra(Constants.USE_CAMERA, true);
        mUsePreview = intent.getBooleanExtra(Constants.USE_PREVIEW, true);
        setTitle(WordUtil.getString(R.string.video_local));
        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new GridLayoutManager(mContext, 4, GridLayoutManager.VERTICAL, false));
        ItemDecoration decoration = new ItemDecoration(mContext, 0x00000000, 5, 5);
        decoration.setOnlySetItemOffsetsButNoDraw(true);
        mRecyclerView.addItemDecoration(decoration);
        mChooseVideoUtil = new ChooseVideoUtil();
        mChooseVideoUtil.getLocalVideoList(new CommonCallback<List<ChooseVideoBean>>() {
            @Override
            public void callback(List<ChooseVideoBean> videoList) {
                if (mContext != null && mRecyclerView != null) {
                    List<ChooseVideoBean> list = new ArrayList<>();
                    if (mUseCamera) {
                        list.add(new ChooseVideoBean(ChooseImageBean.CAMERA,null));
                    }
                    if (videoList != null && videoList.size() > 0) {
                        list.addAll(videoList);
                    } else {
                        View noData = findViewById(R.id.no_data);
                        if (noData != null && noData.getVisibility() != View.VISIBLE) {
                            noData.setVisibility(View.VISIBLE);
                        }
                    }
                    mAdapter = new ChooseVideoAdapter(mContext, list);
                    mAdapter.setActionListener(ChooseVideoActivity.this);
                    mRecyclerView.setAdapter(mAdapter);
                }
            }
        });
    }

    @Override
    public void onCameraClick() {

        if(Build.VERSION.SDK_INT>33){
            String[]permissions=new String[]{
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_AUDIO,
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED,
                    Manifest.permission.CAMERA
            };
            requestPermissionLauncher.launch(permissions);
        }else{
            MediaUtil.startVideoRecord(this, new VideoResultCallback() {
                @Override
                public void onSuccess(File file, long duration) {
                    Intent intent = new Intent();
                    intent.putExtra(Constants.VIDEO_PATH, file.getAbsolutePath());
                    intent.putExtra(Constants.VIDEO_DURATION, duration);
                    setResult(RESULT_OK, intent);
                    finish();
                }

                @Override
                public void onFailure() {

                }
            });
        }
    }

    @Override
    public void onVideoItemClick(ChooseVideoBean bean) {
        if (mUsePreview) {
            VideoPreviewDialog dialog = new VideoPreviewDialog();
            Bundle bundle = new Bundle();
            bundle.putString(Constants.VIDEO_PATH, bean.getVideoFile().getAbsolutePath());
            bundle.putLong(Constants.VIDEO_DURATION, bean.getDuration());
            dialog.setArguments(bundle);
            dialog.show(getSupportFragmentManager(), "VideoPreviewDialog");
        } else {
            useVideo(bean.getVideoFile().getAbsolutePath(), bean.getDuration());
        }
    }

    /**
     * 使用视频
     */
    public void useVideo(String videoPath, long duration) {
        Intent intent = new Intent();
        intent.putExtra(Constants.VIDEO_PATH, videoPath);
        intent.putExtra(Constants.VIDEO_DURATION, duration);
        setResult(RESULT_OK, intent);
        finish();
    }


    @Override
    protected void onDestroy() {
        if (mChooseVideoUtil != null) {
            mChooseVideoUtil.release();
        }
        mChooseVideoUtil = null;
        super.onDestroy();
    }

}
