package com.livestreaming.main.activity;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.livestreaming.common.Constants;
import com.livestreaming.common.activity.AbsActivity;
import com.livestreaming.common.activity.ChooseImageActivity;
import com.livestreaming.common.activity.ChooseLocationActivity;
import com.livestreaming.common.activity.ChooseVideoActivity;
import com.livestreaming.common.custom.ActiveVoiceLayout;
import com.livestreaming.common.custom.ItemDecoration;
import com.livestreaming.common.dialog.ActiveVideoPreviewDialog;
import com.livestreaming.common.dialog.ImagePreviewDialog;
import com.livestreaming.common.glide.ImgLoader;
import com.livestreaming.common.http.HttpCallback;
import com.livestreaming.common.interfaces.CommonCallback;
import com.livestreaming.common.interfaces.ImageResultCallback;
import com.livestreaming.common.interfaces.PermissionCallback;
import com.livestreaming.common.upload.UploadBean;
import com.livestreaming.common.upload.UploadCallback;
import com.livestreaming.common.upload.UploadStrategy;
import com.livestreaming.common.upload.UploadUtil;
import com.livestreaming.common.utils.ClickUtil;
import com.livestreaming.common.utils.DialogUitl;
import com.livestreaming.common.utils.FloatWindowHelper;
import com.livestreaming.common.utils.PermissionUtil;
import com.livestreaming.common.utils.StringUtil;
import com.livestreaming.common.utils.ToastUtil;
import com.livestreaming.common.utils.WordUtil;
import com.livestreaming.im.utils.VoiceMediaPlayerUtil;
import com.livestreaming.main.R;
import com.livestreaming.main.adapter.ActiveImageAdapter;
import com.livestreaming.main.http.MainHttpConsts;
import com.livestreaming.main.http.MainHttpUtil;
import com.livestreaming.main.views.ActiveRecordVoiceViewHolder;
import com.livestreaming.mall.bean.GoodsChooseSpecBean;
import com.livestreaming.mall.http.MallHttpConsts;
import com.livestreaming.mall.http.MallHttpUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 发布动态
 */
public class ActivePubActivity extends AbsActivity implements View.OnClickListener {

    private static final int REQUEST_CODE_IMG = 10001;
    private static final int REQUEST_CODE_VIDEO = 10002;
    private static final int REQUEST_CODE_LOCATION = 10003;
    private static final int REQUEST_CODE_TOPIC = 10004;
    private EditText mEditText;
    private TextView mTextCount;
    private View mOptionGroup;
    private TextView mLocationText;
    private RecyclerView mRecyclerViewImage;
    private ActiveImageAdapter mImageAdapter;
    private String mLocationVal = "";//详细地址
    private ActiveRecordVoiceViewHolder mRecordVoiceViewHolder;
    private VoiceMediaPlayerUtil mPlayerUtil;
    private File mVoiceFile;//语音文件
    private int mVoiceSeconds;//语音文件时长 秒数
    private View mVoiceGroup;
    private ActiveVoiceLayout mVoiceLayout;
    private View mVideoGroup;
    private ImageView mImgVideo;
    private File mVideoFile;
    private int mType = Constants.ACTIVE_TYPE_TEXT;
    private Dialog mLoading;
    private View mBtnPub;
    private String mTopicId;
    private TextView mTvTopicName;
    private String mGoodsId;
    private ImageView mGoodsThumb;
    private TextView mGoodsPrice;
    private TextView mGoodsTitle;
    private TextView mGoodsSaleNum;
    private int selectedType=0;
    private ActivityResultLauncher<String[]> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permissions -> {
                if(selectedType==1) {
                    int hasImageCount = 0;
                    if (mImageAdapter != null) {
                        hasImageCount = mImageAdapter.getImageFileCount();
                    }
                    Intent intent = new Intent(mContext, ChooseImageActivity.class);
                    intent.putExtra(Constants.MAX_COUNT, 9 - hasImageCount);
                    startActivityForResult(intent, REQUEST_CODE_IMG);
                }else if(selectedType==2){
                    if (mRecordVoiceViewHolder == null) {
                        mRecordVoiceViewHolder = new ActiveRecordVoiceViewHolder(mContext, (ViewGroup) findViewById(R.id.root));
                        mRecordVoiceViewHolder.subscribeActivityLifeCycle();
                    }
                    if (!mRecordVoiceViewHolder.isShowing()) {
                        mRecordVoiceViewHolder.addToParent();
                    }
                }else if(selectedType==3){
                    startActivityForResult(new Intent(mContext, ChooseVideoActivity.class), REQUEST_CODE_VIDEO);
                }
                selectedType=0;

            });

    private ImageResultCallback mImageResultCallback = new ImageResultCallback() {
        @Override
        public void beforeCamera() {

        }

        @Override
        public void onSuccess(File file) {
            if (file != null && file.exists()) {
                setImage(file);
            }
        }

        @Override
        public void onFailure() {

        }
    };

    @Override
    protected int getLayoutId() {
        return R.layout.activity_active;
    }

    @Override
    protected void main() {
        mBtnPub = findViewById(R.id.btn_publish);
        mBtnPub.setOnClickListener(this);
        findViewById(R.id.btn_topic).setOnClickListener(this);
        findViewById(R.id.btn_location).setOnClickListener(this);
        mTvTopicName = findViewById(R.id.topic_name);
        mOptionGroup = findViewById(R.id.option_group);
        mLocationText = findViewById(R.id.location_text);
        mEditText = findViewById(R.id.edit);
        mTextCount = findViewById(R.id.text_count);
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (mTextCount != null) {
                    mTextCount.setText(StringUtil.contact(String.valueOf(s.length()), "/200"));
                }
                if (mBtnPub != null) {
                    mBtnPub.setEnabled(!((mType == Constants.ACTIVE_TYPE_TEXT||mType == Constants.ACTIVE_TYPE_GOODS) && TextUtils.isEmpty(s)));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mGoodsId = getIntent().getStringExtra(Constants.MALL_GOODS_ID);
        if (TextUtils.isEmpty(mGoodsId)) {
            findViewById(R.id.btn_image).setOnClickListener(this);
            findViewById(R.id.btn_video).setOnClickListener(this);
            findViewById(R.id.btn_voice).setOnClickListener(this);
            mVoiceGroup = findViewById(R.id.voice_group);
            mVoiceLayout = findViewById(R.id.voice_layout);
            mVoiceLayout.setActionListener(new ActiveVoiceLayout.ActionListener() {
                @Override
                public void onPlayStart(ActiveVoiceLayout voiceLayout, File voiceFile) {
                    playVoiceFile(voiceFile);
                }

                @Override
                public void onPlayStop() {
                    stopPlayVoiceFile();
                }

                @Override
                public void onNeedDownload(ActiveVoiceLayout voiceLayout, String url) {

                }

                @Override
                public File getLocalFile() {
                    return mVoiceFile;
                }
            });
            findViewById(R.id.btn_voice_delete).setOnClickListener(this);
            mVideoGroup = findViewById(R.id.video_group);
            mImgVideo = findViewById(R.id.img_video);
            mVideoGroup.setOnClickListener(this);
            mRecyclerViewImage = findViewById(R.id.recyclerView_image);
            mRecyclerViewImage.setLayoutManager(new GridLayoutManager(mContext, 3, GridLayoutManager.VERTICAL, false));
            ItemDecoration decoration = new ItemDecoration(mContext, 0x00000000, 15, 15);
            decoration.setOnlySetItemOffsetsButNoDraw(true);
            mRecyclerViewImage.addItemDecoration(decoration);
            mImageAdapter = new ActiveImageAdapter(mContext);
            mImageAdapter.setActionListener(new ActiveImageAdapter.ActionListener() {
                @Override
                public void onAddClick() {
                    chooseImage();
                }

                @Override
                public void onItemClick(int position) {
                    if (mImageAdapter == null) {
                        return;
                    }
                    final List<File> imageFileList = mImageAdapter.getImageFileList();
                    if (imageFileList == null || imageFileList.size() == 0) {
                        return;
                    }
                    ImagePreviewDialog dialog = new ImagePreviewDialog();
                    dialog.setImageInfo(imageFileList.size(), position, true, new ImagePreviewDialog.ActionListener() {
                        @Override
                        public void loadImage(ImageView imageView, int position) {
                            ImgLoader.display(mContext, imageFileList.get(position), imageView);
                        }

                        @Override
                        public void onDeleteClick(int position) {
                            if (mImageAdapter != null) {
                                mImageAdapter.deleteItem(position);
                            }
                        }

                        @Override
                        public String getImageUrl(int position) {
                            return null;
                        }
                    });
                    dialog.show(getSupportFragmentManager(), "ImagePreviewDialog");
                }

                @Override
                public void onDeleteAll() {
                    if (mOptionGroup != null && mOptionGroup.getVisibility() != View.VISIBLE) {
                        mOptionGroup.setVisibility(View.VISIBLE);
                    }
                    mType = Constants.ACTIVE_TYPE_TEXT;
                    if (mBtnPub != null) {
                        String text = mEditText.getText().toString();
                        mBtnPub.setEnabled(!TextUtils.isEmpty(text));
                    }
                }
            });
            mRecyclerViewImage.setAdapter(mImageAdapter);
        } else {
            setTitle(WordUtil.getString(com.livestreaming.common.R.string.a_082));
            mType = Constants.ACTIVE_TYPE_GOODS;
            mOptionGroup.setVisibility(View.GONE);
            findViewById(R.id.goods_group).setVisibility(View.VISIBLE);
            mGoodsThumb = findViewById(R.id.goods_thumb);
            mGoodsPrice = findViewById(R.id.goods_price);
            mGoodsTitle = findViewById(R.id.goods_title);
            mGoodsSaleNum = findViewById(R.id.goods_sale_num);
            MallHttpUtil.getGoodsInfo(mGoodsId, new HttpCallback() {
                @Override
                public void onSuccess(int code, String msg, String[] info) {
                    JSONObject obj = JSON.parseObject(info[0]);
                    JSONObject goodsInfo = obj.getJSONObject("goods_info");
                    List<String> thumbs = JSON.parseArray(goodsInfo.getString("thumbs_format"), String.class);
                    if (thumbs != null && thumbs.size() > 0) {
                        if (mGoodsThumb != null) {
                            ImgLoader.display(mContext, thumbs.get(0), mGoodsThumb);
                        }
                    }
                    if (mGoodsTitle != null) {
                        mGoodsTitle.setText(goodsInfo.getString("name"));
                    }
                    List<GoodsChooseSpecBean> specList = JSON.parseArray(goodsInfo.getString("specs_format"), GoodsChooseSpecBean.class);
                    if (specList != null && specList.size() > 0) {
                        GoodsChooseSpecBean bean = specList.get(0);
                        if (mGoodsPrice != null) {
                            mGoodsPrice.setText(bean.getPrice());
                        }
                    }
                    if (mGoodsSaleNum != null) {
                        mGoodsSaleNum.setText(String.format(WordUtil.getString(com.livestreaming.common.R.string.mall_114), goodsInfo.getString("sale_nums")));
                    }
                }
            });
        }
    }

    @Override
    public void onClick(View v) {
        if (!ClickUtil.canClick()) {
            return;
        }
        int i = v.getId();
        if (i == R.id.btn_publish) {
            publish();
        } else if (i == R.id.btn_image) {
            chooseImage();
        } else if (i == R.id.btn_video) {
            chooseVideo();
        } else if (i == R.id.btn_voice) {
            clickVoice();
        } else if (i == R.id.btn_topic) {
            chooseTopic();
        } else if (i == R.id.btn_location) {
            chooseLocation();
        } else if (i == R.id.btn_voice_delete) {
            deleteVoice();
        } else if (i == R.id.video_group) {
            previewVideo();
        }
    }

    /**
     * 开始录音
     */
    private void clickVoice() {
        if (!FloatWindowHelper.checkVoice(false)) {
            return;
        }
        selectedType=2;
        if(Build.VERSION.SDK_INT>33){
            String[]permissions=new String[]{
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_AUDIO,
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED,
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO
            };
            requestPermissionLauncher.launch(permissions);
        }else if(Build.VERSION.SDK_INT==33){
            String[]permissions=new String[]{
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_AUDIO,
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO
            };
            requestPermissionLauncher.launch(permissions);
            /*PermissionUtil.request(this, new PermissionCallback() {
                        @Override
                        public void onAllGranted() {
                            if (mRecordVoiceViewHolder == null) {
                                mRecordVoiceViewHolder = new ActiveRecordVoiceViewHolder(mContext, (ViewGroup) findViewById(R.id.root));
                                mRecordVoiceViewHolder.subscribeActivityLifeCycle();
                            }
                            if (!mRecordVoiceViewHolder.isShowing()) {
                                mRecordVoiceViewHolder.addToParent();
                            }
                        }
                    },
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_AUDIO,
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO
            );*/
        }else{
            PermissionUtil.request(this, new PermissionCallback() {
                        @Override
                        public void onAllGranted() {
                            if (mRecordVoiceViewHolder == null) {
                                mRecordVoiceViewHolder = new ActiveRecordVoiceViewHolder(mContext, (ViewGroup) findViewById(R.id.root));
                                mRecordVoiceViewHolder.subscribeActivityLifeCycle();
                            }
                            if (!mRecordVoiceViewHolder.isShowing()) {
                                mRecordVoiceViewHolder.addToParent();
                            }
                        }
                    },
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO
            );
        }



    }

    /**
     * 选择图片
     */
    private void chooseImage() {
        selectedType=1;
        if(Build.VERSION.SDK_INT>33){
            String[] permissions=new String[]{  Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED,
                    Manifest.permission.CAMERA};
            requestPermissionLauncher.launch(permissions);

        }else if(Build.VERSION.SDK_INT==33){
            String[] permissions=new String[]{  Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.CAMERA};
            requestPermissionLauncher.launch(permissions);

        }else{
            PermissionUtil.request(this,
                    new PermissionCallback() {
                        @Override
                        public void onAllGranted() {
                            int hasImageCount = 0;
                            if (mImageAdapter != null) {
                                hasImageCount = mImageAdapter.getImageFileCount();
                            }
                            Intent intent = new Intent(mContext, ChooseImageActivity.class);
                            intent.putExtra(Constants.MAX_COUNT, 9 - hasImageCount);
                            startActivityForResult(intent, REQUEST_CODE_IMG);
                        }
                    },
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA);
        }
    }

    /**
     * 选择视频
     */
    private void chooseVideo() {
        if (!FloatWindowHelper.checkVoice(false)) {
            return;
        }
        selectedType=3;
        if(Build.VERSION.SDK_INT>33){
            String[]permissions=new String[]{
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED,
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO
            };
            requestPermissionLauncher.launch(permissions);
        }else if(Build.VERSION.SDK_INT==33){
            String[]permissions=new String[]{
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO
            };
            requestPermissionLauncher.launch(permissions);
        }else{
            PermissionUtil.request(this,
                    new PermissionCallback() {
                        @Override
                        public void onAllGranted() {
                            startActivityForResult(new Intent(mContext, ChooseVideoActivity.class), REQUEST_CODE_VIDEO);
                        }
                    }
                    ,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO
            );
        }
    }

    /**
     * 选择话题
     */
    private void chooseTopic() {
        startActivityForResult(new Intent(mContext, ActiveChooseTopicActivity.class), REQUEST_CODE_TOPIC);
    }

    /**
     * 选择地址
     */
    private void chooseLocation() {
        startActivityForResult(new Intent(mContext, ChooseLocationActivity.class), REQUEST_CODE_LOCATION);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode == RESULT_OK && intent != null) {
            if (requestCode == REQUEST_CODE_IMG) {
                setImage(intent);
            } else if (requestCode == REQUEST_CODE_VIDEO) {
                setVideo(intent);
            } else if (requestCode == REQUEST_CODE_LOCATION) {
                setLocation(intent);
            } else if (requestCode == REQUEST_CODE_TOPIC) {
                String topicName = intent.getStringExtra(Constants.CLASS_NAME);
                if (mTvTopicName != null) {
                    mTvTopicName.setHint("");
                    mTvTopicName.setText(topicName);
                }
                mTopicId = intent.getStringExtra(Constants.CLASS_ID);
            }
        }
    }


    /**
     * 选择图片
     */
    private void setImage(File file) {
        List<String> imagePathList = new ArrayList<>();
        imagePathList.add(file.getAbsolutePath());
        if (mImageAdapter != null) {
            mImageAdapter.insertList(imagePathList);
        }
        if (mOptionGroup != null && mOptionGroup.getVisibility() != View.GONE) {
            mOptionGroup.setVisibility(View.GONE);
        }
        mType = Constants.ACTIVE_TYPE_IMAGE;
        if (mBtnPub != null) {
            mBtnPub.setEnabled(true);
        }
    }


    /**
     * 选择图片
     */
    private void setImage(Intent intent) {
        List<String> imagePathList = intent.getStringArrayListExtra(Constants.CHOOSE_IMG);
        if (imagePathList == null || imagePathList.size() == 0) {
            return;
        }
        if (mImageAdapter != null) {
            mImageAdapter.insertList(imagePathList);
        }
        if (mOptionGroup != null && mOptionGroup.getVisibility() != View.GONE) {
            mOptionGroup.setVisibility(View.GONE);
        }
        mType = Constants.ACTIVE_TYPE_IMAGE;
        if (mBtnPub != null) {
            mBtnPub.setEnabled(true);
        }
    }


    /**
     * 选择视频
     */
    private void setVideo(Intent intent) {
        String videoPath = intent.getStringExtra(Constants.VIDEO_PATH);
        if (TextUtils.isEmpty(videoPath)) {
            return;
        }
        mVideoFile = new File(videoPath);
        if (mVideoGroup != null && mVideoGroup.getVisibility() != View.VISIBLE) {
            mVideoGroup.setVisibility(View.VISIBLE);
        }
        ImgLoader.displayVideoThumb(mContext, mVideoFile, mImgVideo);
        if (mOptionGroup != null && mOptionGroup.getVisibility() != View.GONE) {
            mOptionGroup.setVisibility(View.GONE);
        }
        mType = Constants.ACTIVE_TYPE_VIDEO;
        if (mBtnPub != null) {
            mBtnPub.setEnabled(true);
        }
    }


    /**
     * 取消视频
     */
    private void cancelVideo() {
        mVideoFile = null;
        if (mImgVideo != null) {
            mImgVideo.setImageDrawable(null);
        }
        if (mVideoGroup != null && mVideoGroup.getVisibility() != View.GONE) {
            mVideoGroup.setVisibility(View.GONE);
        }
        if (mOptionGroup != null && mOptionGroup.getVisibility() != View.VISIBLE) {
            mOptionGroup.setVisibility(View.VISIBLE);
        }
        mType = Constants.ACTIVE_TYPE_TEXT;
        if (mBtnPub != null) {
            String text = mEditText.getText().toString();
            mBtnPub.setEnabled(!TextUtils.isEmpty(text));
        }
    }

    /**
     * 预览视频
     */
    private void previewVideo() {
        if (mVideoFile == null) {
            return;
        }
        ActiveVideoPreviewDialog dialog = new ActiveVideoPreviewDialog();
        dialog.setActionListener(new ActiveVideoPreviewDialog.ActionListener() {
            @Override
            public void onDeleteClick() {
                cancelVideo();
            }
        });
        Bundle bundle = new Bundle();
        bundle.putString(Constants.VIDEO_PATH, mVideoFile.getAbsolutePath());
        dialog.setArguments(bundle);
        dialog.show(getSupportFragmentManager(), "ActiveVideoPreviewDialog");
    }


    /**
     * 选择地址
     */
    public void setLocation(Intent intent) {
        mLocationVal = intent.getStringExtra(Constants.CHOOSE_LOCATION);
        if (mLocationText != null) {
            if (TextUtils.isEmpty(mLocationVal)) {
                mLocationText.setText(com.livestreaming.common.R.string.location_no);
            } else {
                mLocationText.setText(mLocationVal);
            }
        }
    }


    /**
     * 播放语音
     */
    public void playVoiceFile(File file) {
        if (file == null) {
            return;
        }
        if (mPlayerUtil == null) {
            mPlayerUtil = new VoiceMediaPlayerUtil(mContext);
            mPlayerUtil.setActionListener(new VoiceMediaPlayerUtil.ActionListener() {
                @Override
                public void onPlayEnd() {
                    if (mRecordVoiceViewHolder != null && mRecordVoiceViewHolder.isShowing()) {
                        mRecordVoiceViewHolder.onListenEnd();
                    }
                    if (mVoiceGroup.getVisibility() == View.VISIBLE && mVoiceLayout != null) {
                        mVoiceLayout.stopPlay();
                    }
                }
            });
        }
        mPlayerUtil.startPlay(file.getAbsolutePath());
    }


    /**
     * 停止播放语音
     */
    public void stopPlayVoiceFile() {
        if (mPlayerUtil != null) {
            mPlayerUtil.stopPlay();
        }
    }

    /**
     * 使用语音
     */
    public void useVoice(File voiceFile, int voiceSeconds) {
        mVoiceFile = voiceFile;
        mVoiceSeconds = voiceSeconds;
        if (mVoiceGroup != null && mVoiceGroup.getVisibility() != View.VISIBLE) {
            mVoiceGroup.setVisibility(View.VISIBLE);
        }
        if (mOptionGroup != null && mOptionGroup.getVisibility() != View.GONE) {
            mOptionGroup.setVisibility(View.GONE);
        }
        if (mVoiceLayout != null) {
            mVoiceLayout.setSecondsMax(voiceSeconds);
        }
        mType = Constants.ACTIVE_TYPE_VOICE;
        if (mBtnPub != null) {
            mBtnPub.setEnabled(true);
        }
    }


    /**
     * 删除语音文件
     */
    private void deleteVoice() {
        if (mVoiceLayout != null) {
            mVoiceLayout.stopPlay();
        }
        mVoiceSeconds = 0;
        if (mVoiceFile != null && mVoiceFile.exists()) {
            mVoiceFile.delete();
        }
        mVoiceFile = null;
        if (mVoiceGroup != null && mVoiceGroup.getVisibility() != View.GONE) {
            mVoiceGroup.setVisibility(View.GONE);
        }
        if (mOptionGroup != null && mOptionGroup.getVisibility() != View.VISIBLE) {
            mOptionGroup.setVisibility(View.VISIBLE);
        }
        mType = Constants.ACTIVE_TYPE_TEXT;
        if (mBtnPub != null) {
            String text = mEditText.getText().toString();
            mBtnPub.setEnabled(!TextUtils.isEmpty(text));
        }
    }

    private void release() {
        MainHttpUtil.cancel(MainHttpConsts.ACTIVE_PUBLISH);
        MallHttpUtil.cancel(MallHttpConsts.GET_GOODS_INFO);
        if (mPlayerUtil != null) {
            mPlayerUtil.destroy();
        }
        mPlayerUtil = null;
        if (mVoiceLayout != null) {
            mVoiceLayout.release();
        }
        mVoiceLayout = null;
        UploadUtil.cancelUpload();
        hideLoading();
        mLoading = null;
    }

    @Override
    protected void onDestroy() {
        release();
        super.onDestroy();
    }


    @Override
    public void onBackPressed() {
        boolean showDialog = false;
        if (mType == Constants.ACTIVE_TYPE_TEXT) {
            if (mEditText != null && mEditText.getText().length() > 0) {
                showDialog = true;
            }
        } else {
            showDialog = true;
        }
        if (showDialog) {
            new DialogUitl.Builder(mContext)
                    .setContent(WordUtil.getString(com.livestreaming.common.R.string.active_cancel))
                    .setCancelable(true)
                    .setBackgroundDimEnabled(true)
                    .setClickCallback(new DialogUitl.SimpleCallback() {
                        @Override
                        public void onConfirmClick(Dialog dialog, String content) {
                            release();
                            ActivePubActivity.super.onBackPressed();
                        }
                    })
                    .build()
                    .show();

        } else {
            super.onBackPressed();
        }
    }


    private void showLoading() {
        if (mLoading == null) {
            mLoading = DialogUitl.loadingDialog(mContext, WordUtil.getString(com.livestreaming.common.R.string.video_pub_ing));
        }
        if (!mLoading.isShowing()) {
            mLoading.show();
        }
    }


    private void hideLoading() {
        if (mLoading != null && mLoading.isShowing()) {
            mLoading.dismiss();
        }
    }

    /**
     * 发布动态
     */
    private void publish() {
        if (mType == Constants.ACTIVE_TYPE_IMAGE) {
            uploadImage();
        } else if (mType == Constants.ACTIVE_TYPE_VOICE) {
            uploadVoice();
        } else if (mType == Constants.ACTIVE_TYPE_VIDEO) {
            uploadVideo();
        } else {
            submit("", "", "", "");
        }
    }

    /**
     * 上传图片
     */
    private void uploadImage() {
        if (mImageAdapter == null) {
            return;
        }
        List<File> imageFileList = mImageAdapter.getImageFileList();
        if (imageFileList == null || imageFileList.size() == 0) {
            return;
        }
        final List<UploadBean> uploadList = new ArrayList<>();
        for (File file : imageFileList) {
            uploadList.add(new UploadBean(file, UploadBean.IMG));
        }
        showLoading();
        UploadUtil.startUpload(new CommonCallback<UploadStrategy>() {
            @Override
            public void callback(UploadStrategy uploadStrategy) {
                uploadStrategy.upload(uploadList, true, new UploadCallback() {
                    @Override
                    public void onFinish(List<UploadBean> list, boolean success) {
                        if (success) {
                            StringBuilder sb = new StringBuilder();
                            for (UploadBean bean : list) {
                                sb.append(bean.getRemoteFileName());
                                sb.append(";");
                            }
                            String s = sb.toString();
                            if (s.length() > 0) {
                                s = s.substring(0, s.length() - 1);
                            }
                            submit(s, "", "", "");
                        }
                    }
                });
            }
        });
    }


    /**
     * 上传语音
     */
    private void uploadVoice() {
        if (mVoiceFile == null || mVoiceFile.length() == 0 || !mVoiceFile.exists()) {
            return;
        }
        final List<UploadBean> uploadList = new ArrayList<>();
        uploadList.add(new UploadBean(mVoiceFile, UploadBean.VOICE));
        showLoading();
        UploadUtil.startUpload(new CommonCallback<UploadStrategy>() {
            @Override
            public void callback(UploadStrategy strategy) {
                strategy.upload(uploadList, false, new UploadCallback() {
                    @Override
                    public void onFinish(List<UploadBean> list, boolean success) {
                        submit("", "", "", list.get(0).getRemoteFileName());
                    }
                });
            }
        });
    }


    /**
     * 上传视频
     */
    private void uploadVideo() {
        if (mVideoFile == null || mVideoFile.length() == 0 || !mVideoFile.exists()) {
            return;
        }
        final List<UploadBean> uploadList = new ArrayList<>();
        uploadList.add(new UploadBean(mVideoFile, UploadBean.VIDEO));
        File coverImageFile = createVideoCoverImage(mVideoFile.getAbsolutePath());
        if (coverImageFile != null && coverImageFile.length() > 0 && coverImageFile.exists()) {
            uploadList.add(new UploadBean(coverImageFile, UploadBean.IMG));
        }
        showLoading();
        UploadUtil.startUpload(new CommonCallback<UploadStrategy>() {
            @Override
            public void callback(UploadStrategy strategy) {
                strategy.upload(uploadList, true, new UploadCallback() {
                    @Override
                    public void onFinish(List<UploadBean> list, boolean success) {
                        String videoUrl = list.get(0).getRemoteFileName();
                        String videoImage = "";
                        if (list.size() > 1) {
                            videoImage = list.get(1).getRemoteFileName();
                        }
                        submit("", videoImage, videoUrl, "");
                    }
                });
            }
        });

    }

    /**
     * 生成视频封面图
     */
    private File createVideoCoverImage(String videoPath) {
        MediaMetadataRetriever mmr = null;
        Bitmap bitmap = null;
        try {
            mmr = new MediaMetadataRetriever();
            mmr.setDataSource(videoPath);
            bitmap = mmr.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST);
        } catch (Exception e) {
            bitmap = null;
            e.printStackTrace();
        } finally {
            if (mmr != null) {
                try {
                    mmr.release();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        if (bitmap == null) {
            return null;
        }
        String coverImagePath = videoPath.replace(".mp4", ".jpg");
        File imageFile = new File(coverImagePath);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if (bitmap != null) {
            bitmap.recycle();
        }
        return imageFile;

    }

    private void submit(String images, String videoImage, String videoUrl, String voiceUrl) {
        String text = mEditText.getText().toString();
        MainHttpUtil.activePublish(mType, text, images, videoImage, videoUrl, mLocationVal, voiceUrl, mVoiceSeconds, mTopicId, mGoodsId, new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0) {
                    if (mPlayerUtil != null) {
                        mPlayerUtil.destroy();
                    }
                    mPlayerUtil = null;
                    finish();
                }
                ToastUtil.show(msg);
            }

            @Override
            public void onFinish() {
                hideLoading();
            }
        });
    }

}
