package com.livestreaming.video.activity;

import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.tencent.liteav.beauty.TXBeautyManager;
import com.tencent.ugc.TXUGCRecord;
import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.ui.TXCloudVideoView;
import com.tencent.ugc.TXRecordCommon;
import com.tencent.ugc.TXUGCPartsManager;
import com.livestreaming.beauty.bean.MeiYanValueBean;
import com.livestreaming.beauty.interfaces.IBeautyEffectListener;
import com.livestreaming.beauty.interfaces.IBeautyViewHolder;
import com.livestreaming.beauty.utils.MhDataManager;
import com.livestreaming.beauty.utils.SimpleDataManager;
import com.livestreaming.beauty.views.BeautyViewHolder;
import com.livestreaming.beauty.views.SimpleBeautyViewHolder;
import com.livestreaming.common.CommonAppConfig;
import com.livestreaming.common.CommonAppContext;
import com.livestreaming.common.Constants;
import com.livestreaming.common.activity.AbsActivity;
import com.livestreaming.common.activity.ChooseVideoActivity;
import com.livestreaming.common.custom.DrawableRadioButton2;
import com.livestreaming.common.http.CommonHttpConsts;
import com.livestreaming.common.http.CommonHttpUtil;
import com.livestreaming.common.http.HttpCallback;
import com.livestreaming.common.interfaces.ActivityResultCallback;
import com.livestreaming.common.utils.ActivityResultUtil;
import com.livestreaming.common.utils.BitmapUtil;
import com.livestreaming.common.utils.DialogUitl;
import com.livestreaming.common.utils.L;
import com.livestreaming.common.utils.StringUtil;
import com.livestreaming.common.utils.ToastUtil;
import com.livestreaming.common.utils.WordUtil;
import com.livestreaming.video.R;
import com.livestreaming.video.bean.MusicBean;
import com.livestreaming.video.custom.RecordProgressView;
import com.livestreaming.video.custom.VideoRecordBtnView;
import com.livestreaming.video.views.VideoMusicViewHolder;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by cxf on 2018/12/5.
 * 视频录制
 */

public class VideoRecordActivity extends AbsActivity implements
        TXRecordCommon.ITXVideoRecordListener //视频录制进度回调
{

    private static final String TAG = "VideoRecordActivity";
    private static final int MIN_DURATION = 5000;//最小录制时间5s
    private static final int MAX_DURATION = 15000;//最大录制时间15s
    
    // Beauty type constants
    private static final int BEAUTY_MHTECH = 1; // Meihu beauty SDK
    private static final int BEAUTY_SIMPLE = 2; // Simple beauty implementation
    
    private int mBeauty = BEAUTY_MHTECH; // Default to Meihu beauty

    //按钮动画相关
    private VideoRecordBtnView mVideoRecordBtnView;
    private View mRecordView;
    private ValueAnimator mRecordBtnAnimator;//录制开始后，录制按钮动画
    private Drawable mRecordDrawable;
    private Drawable mUnRecordDrawable;

    /****************************/
    private boolean mRecordStarted;//是否开始了录制（true 已开始 false 未开始）
    private boolean mRecordStoped;//是否结束了录制
    private boolean mRecording;//是否在录制中（（true 录制中 false 暂停中）
    private ViewGroup mRoot;
    private TXCloudVideoView mVideoView;//预览控件
    private RecordProgressView mRecordProgressView;//录制进度条
    private TextView mTime;//录制时间
    private DrawableRadioButton2 mBtnFlash;//闪光灯按钮
    private TXUGCRecord mRecorder;//录制器
    private TXRecordCommon.TXUGCCustomConfig mCustomConfig;//录制相关配置
    private boolean mFrontCamera = true;//是否是前置摄像头
    private String mVideoPath;//视频的保存路径
    private int mRecordSpeed;//录制速度
    private View mGroup1;
    private View mGroup2;
    private View mGroup3;
    private View mGroup4;
    private View mBtnNext;//录制完成，下一步
    private Dialog mStopRecordDialog;//停止录制的时候的dialog
    private boolean mIsReachMaxRecordDuration;//是否达到最大录制时间
    private long mDuration;//录制视频的长度
    private IBeautyViewHolder mBeautyViewHolder;//美颜
    private VideoMusicViewHolder mVideoMusicViewHolder;
    private MusicBean mMusicBean;//背景音乐
    private boolean mBgmPlayStarted;//是否已经开始播放背景音乐了
    private long mRecordTime;
    private View mBtnMusic;//音乐按钮
    private Handler mHandler;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_video_record;
    }

    @Override
    protected boolean isStatusBarWhite() {
        return true;
    }

    @Override
    protected void main() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //按钮动画相关
        mVideoRecordBtnView = (VideoRecordBtnView) findViewById(R.id.record_btn_view);
        mRecordView = findViewById(R.id.record_view);
        mUnRecordDrawable = ContextCompat.getDrawable(mContext, R.drawable.bg_btn_record_1);
        mRecordDrawable = ContextCompat.getDrawable(mContext, R.drawable.bg_btn_record_2);
        mRecordBtnAnimator = ValueAnimator.ofFloat(100, 0);
        mRecordBtnAnimator.setDuration(500);
        mRecordBtnAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float v = (float) animation.getAnimatedValue();
                if (mVideoRecordBtnView != null) {
                    mVideoRecordBtnView.setRate((int) v);
                }
            }
        });
        mRecordBtnAnimator.setRepeatCount(-1);
        mRecordBtnAnimator.setRepeatMode(ValueAnimator.REVERSE);

        /****************************/
        mRoot = (ViewGroup) findViewById(R.id.root);
        mGroup1 = findViewById(R.id.group_1);
        mGroup2 = findViewById(R.id.group_2);
        mGroup3 = findViewById(R.id.group_3);
        mGroup4 = findViewById(R.id.group_4);
        mVideoView = (TXCloudVideoView) findViewById(R.id.video_view);
        // mVideoView.enableHardwareDecode(true);
        mTime = findViewById(R.id.time);
        mRecordProgressView = (RecordProgressView) findViewById(R.id.record_progress_view);
        mRecordProgressView.setMaxDuration(MAX_DURATION);
        mRecordProgressView.setMinDuration(MIN_DURATION);
        mBtnFlash = (DrawableRadioButton2) findViewById(R.id.btn_flash);
        mBtnNext = findViewById(R.id.btn_next);
        mBtnMusic = findViewById(R.id.btn_music);
        mHandler = new Handler();
        initCameraRecord();
        startCameraPreview();
    }


    /**
     * 初始化录制器
     */
    private void initCameraRecord() {
        mRecorder = TXUGCRecord.getInstance(CommonAppContext.getInstance());
        mRecorder.setHomeOrientation(TXLiveConstants.VIDEO_ANGLE_HOME_DOWN);
        mRecorder.setRenderRotation(TXLiveConstants.RENDER_ROTATION_PORTRAIT);
        mRecorder.setVideoRenderMode(TXLiveConstants.RENDER_MODE_FULL_FILL_SCREEN);
        mRecordSpeed = TXRecordCommon.RECORD_SPEED_NORMAL;
        mRecorder.setRecordSpeed(mRecordSpeed);
        mRecorder.setAspectRatio(TXRecordCommon.VIDEO_ASPECT_RATIO_9_16);
        mCustomConfig = new TXRecordCommon.TXUGCCustomConfig();
        mCustomConfig.videoResolution = TXRecordCommon.VIDEO_RESOLUTION_720_1280;
        mCustomConfig.minDuration = MIN_DURATION;
        mCustomConfig.maxDuration = MAX_DURATION;
        mCustomConfig.isFront = mFrontCamera;
        mRecorder.setVideoRecordListener(this);
    }


    /**
     * 录制回调
     */
    @Override
    public void onRecordEvent(int event, Bundle bundle) {
        if (event == TXRecordCommon.EVT_ID_PAUSE) {
            if (mRecordProgressView != null) {
                mRecordProgressView.clipComplete();
            }
        } else if (event == TXRecordCommon.EVT_CAMERA_CANNOT_USE) {
            ToastUtil.show(com.livestreaming.common.R.string.video_record_camera_failed);
        } else if (event == TXRecordCommon.EVT_MIC_CANNOT_USE) {
            ToastUtil.show(com.livestreaming.common.R.string.video_record_audio_failed);
        }
    }

    /**
     * 录制回调 录制进度
     */
    @Override
    public void onRecordProgress(long milliSecond) {
        if (mRecordProgressView != null) {
            mRecordProgressView.setProgress((int) milliSecond);
        }
        if (mTime != null) {
            mTime.setText(String.format("%.2f", milliSecond / 1000f) + "s");
        }
        mRecordTime = milliSecond;
        setBtnMusicEnable(milliSecond==0);
        if (milliSecond >= MIN_DURATION) {
            if (mBtnNext != null && mBtnNext.getVisibility() != View.VISIBLE) {
                mBtnNext.setVisibility(View.VISIBLE);
            }
        }
        if (milliSecond >= MAX_DURATION) {
            if (!mIsReachMaxRecordDuration) {
                mIsReachMaxRecordDuration = true;
                if (mRecordBtnAnimator != null) {
                    mRecordBtnAnimator.cancel();
                }
                showProccessDialog();
            }
        }
    }

    /**
     * 录制回调
     */
    @Override
    public void onRecordComplete(TXRecordCommon.TXRecordResult result) {
        hideProccessDialog();
        mRecordStarted = false;
        mRecordStoped = true;
        if (mRecorder != null) {
            mRecorder.toggleTorch(false);
            mRecorder.stopBGM();
            mDuration = mRecorder.getPartsManager().getDuration();
        }
        if (result.retCode < 0) {
            release();
            ToastUtil.show(com.livestreaming.common.R.string.video_record_failed);
        } else {
            VideoEditActivity.forward(this, mDuration, mVideoPath, true, mMusicBean);
        }
        exit();
    }


    public void recordClick(View v) {
        if (mRecordStoped || !canClick()) {
            return;
        }
        int i = v.getId();
        if (i == R.id.btn_start_record) {
            clickRecord();

        } else if (i == R.id.btn_camera) {
            clickCamera();

        } else if (i == R.id.btn_flash) {
            clickFlash();

        } else if (i == R.id.btn_beauty) {
            clickBeauty();

        } else if (i == R.id.btn_music) {
            clickMusic();

        } else if (i == R.id.btn_speed_1) {
            changeRecordSpeed(TXRecordCommon.RECORD_SPEED_SLOWEST);

        } else if (i == R.id.btn_speed_2) {
            changeRecordSpeed(TXRecordCommon.RECORD_SPEED_SLOW);

        } else if (i == R.id.btn_speed_3) {
            changeRecordSpeed(TXRecordCommon.RECORD_SPEED_NORMAL);

        } else if (i == R.id.btn_speed_4) {
            changeRecordSpeed(TXRecordCommon.RECORD_SPEED_FAST);

        } else if (i == R.id.btn_speed_5) {
            changeRecordSpeed(TXRecordCommon.RECORD_SPEED_FASTEST);

        } else if (i == R.id.btn_upload) {
            clickUpload();

        } else if (i == R.id.btn_delete) {
            clickDelete();

        } else if (i == R.id.btn_next) {
            clickNext();

        }
    }

    /**
     * 点击摄像头
     */
    private void clickCamera() {
        if (mRecorder != null) {
            if (mBtnFlash != null && mBtnFlash.isChecked()) {
                mBtnFlash.doToggle();
                mRecorder.toggleTorch(mBtnFlash.isChecked());
            }
            mFrontCamera = !mFrontCamera;
            mRecorder.switchCamera(mFrontCamera);
        }
    }

    private void setBtnMusicEnable(boolean enable) {
        if (mBtnMusic != null) {
            if (mBtnMusic.isEnabled() != enable) {
                mBtnMusic.setEnabled(enable);
                mBtnMusic.setAlpha(enable ? 1f : 0.5f);
            }
        }
    }

    /**
     * 点击闪光灯
     */
    private void clickFlash() {
        if (mFrontCamera) {
            ToastUtil.show(com.livestreaming.common.R.string.live_open_flash);
            return;
        }
        if (mBtnFlash != null) {
            mBtnFlash.doToggle();
            if (mRecorder != null) {
                mRecorder.toggleTorch(mBtnFlash.isChecked());
            }
        }
    }

    /**
     * 点击美颜
     */
    private void clickBeauty() {
        if (mBeautyViewHolder == null) {
            if (CommonAppConfig.getInstance().isMhBeautyEnable()) {
                mBeautyViewHolder = new BeautyViewHolder(mContext, mRoot);
            } else {
                mBeautyViewHolder = new SimpleBeautyViewHolder(mContext, mRoot,true);
            }
            mBeautyViewHolder.setVisibleListener(new IBeautyViewHolder.VisibleListener() {
                @Override
                public void onVisibleChanged(boolean visible) {
                    if (mGroup1 != null) {
                        if (visible) {
                            if (mGroup1.getVisibility() == View.VISIBLE) {
                                mGroup1.setVisibility(View.INVISIBLE);
                            }
                        } else {
                            if (mGroup1.getVisibility() != View.VISIBLE) {
                                mGroup1.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                }
            });
        }
        mBeautyViewHolder.show();
    }


    /**
     * 点击音乐
     */
    private void clickMusic() {
        if (mVideoMusicViewHolder == null) {
            mVideoMusicViewHolder = new VideoMusicViewHolder(mContext, mRoot);
            mVideoMusicViewHolder.setActionListener(new VideoMusicViewHolder.ActionListener() {
                @Override
                public void onChooseMusic(MusicBean musicBean) {
                    mMusicBean = musicBean;
                    mBgmPlayStarted = false;
                }
            });
            mVideoMusicViewHolder.addToParent();
            mVideoMusicViewHolder.subscribeActivityLifeCycle();
        }
        mVideoMusicViewHolder.show();
    }

    /**
     * 点击上传，选择本地视频
     */
    private void clickUpload() {
        Intent i = new Intent(mContext, ChooseVideoActivity.class);
        i.putExtra(Constants.USE_CAMERA, false);
        i.putExtra(Constants.USE_PREVIEW, false);
        ActivityResultUtil.startActivityForResult(this, i, new ActivityResultCallback() {
            @Override
            public void onSuccess(Intent intent) {
                if (intent != null) {
                    mVideoPath = intent.getStringExtra(Constants.VIDEO_PATH);
                    mDuration = intent.getLongExtra(Constants.VIDEO_DURATION, 0);
                    VideoEditActivity.forward(VideoRecordActivity.this, mDuration, mVideoPath, false, mMusicBean);
                    exit();
                }
            }
        });
    }

    private IBeautyEffectListener getMeiYanChangedListener() {
        return new IBeautyEffectListener() {
            @Override
            public void onMeiYanChanged(int meiBai, boolean meiBaiChanged, int moPi, boolean moPiChanged, int hongRun, boolean hongRunChanged) {
                if (meiBaiChanged || moPiChanged || hongRunChanged) {
                    if (mRecorder != null) {
                        mRecorder.setBeautyDepth(TXLiveConstants.BEAUTY_STYLE_SMOOTH, moPi, meiBai, hongRun);
                    }
                }
            }

            @Override
            public void onAdvancedBeautyChanged(String key, int value) {
                if (mRecorder != null) {
                    if (CommonAppConfig.getInstance().isMhBeautyEnable()) {
                        // Use MhDataManager for advanced beauty features
                        switch (key) {
                            case "brightness":
                                MhDataManager.getInstance().setLiangDu(value);
                                break;
                            case "sharpness":
                                MhDataManager.getInstance().setMoPi(value);
                                break;
                            case "face_slim":
                                MhDataManager.getInstance().setShouLian(value);
                                break;
                            case "big_eye":
                                MhDataManager.getInstance().setDaYan(value);
                                break;
                            case "jaw":
                                MhDataManager.getInstance().setXiaBa(value);
                                break;
                            case "eye_distance":
                                MhDataManager.getInstance().setYanJu(value);
                                break;
                            case "face_shape":
                                MhDataManager.getInstance().setETou(value);
                                break;
                            case "eye_brow":
                                MhDataManager.getInstance().setMeiMao(value);
                                break;
                            case "eye_corner":
                                MhDataManager.getInstance().setYanJiao(value);
                                break;
                            case "mouse_lift":
                                MhDataManager.getInstance().setZuiXing(value);
                                break;
                            case "nose_lift":
                                MhDataManager.getInstance().setShouBi(value);
                                break;
                            case "lengthen_noseLift":
                                MhDataManager.getInstance().setChangBi(value);
                                break;
                            case "face_shave":
                                MhDataManager.getInstance().setXueLian(value);
                                break;
                            case "eye_alat":
                                MhDataManager.getInstance().setKaiYanJiao(value);
                                break;
                        }
                        
                        // Save beauty values to server when they change
                        MhDataManager.getInstance().saveBeautyValue();
                    } else {
                        // Use Tencent SDK for basic beauty features
                        TXBeautyManager txBeautyManager = mRecorder.getBeautyManager();
                        if (txBeautyManager != null) {
                            switch (key) {
                                case "brightness":
                                    // For brightness in this version of SDK, we use the beauty level with style 2
                                    txBeautyManager.setBeautyStyle(2); // Natural beauty style
                                    txBeautyManager.setBeautyLevel(value);
                                    break;
                                case "sharpness":
                                    // For sharpness in this version, we can use whiteness level as an approximation
                                    txBeautyManager.setWhitenessLevel(value);
                                    break;
                                case "face_slim":
                                    // Face slimming feature
                                    txBeautyManager.setFaceSlimLevel(value);
                                    break;
                                case "big_eye":
                                    // Big eye feature
                                    txBeautyManager.setEyeScaleLevel(value);
                                    break;
                                case "jaw":
                                    // Jawline adjustment
                                    txBeautyManager.setChinLevel(value);
                                    break;
                                case "eye_distance":
                                    // Eye distance adjustment
                                    txBeautyManager.setEyeDistanceLevel(value);
                                    break;
                                case "face_shape":
                                    // Face shape adjustment
                                    txBeautyManager.setFaceVLevel(value);
                                    break;
                                case "eye_brow":
                                    // Eyebrow adjustment - map to existing feature if possible
                                    txBeautyManager.setEyeAngleLevel(value);
                                    break;
                                case "eye_corner":
                                    // Eye corner adjustment - map to existing feature if possible
                                    txBeautyManager.setEyeAngleLevel(value);
                                    break;
                                case "mouse_lift":
                                    // Mouth shape adjustment - map to existing feature if possible
                                    txBeautyManager.setMouthShapeLevel(value);
                                    break;
                                case "nose_lift":
                                    // Nose adjustment - map to existing feature if possible
                                    txBeautyManager.setNoseSlimLevel(value);
                                    break;
                                case "lengthen_noseLift":
                                    // Long nose adjustment - map to existing feature if possible
                                    txBeautyManager.setNoseSlimLevel(value);
                                    break;
                                case "face_shave":
                                    // Face shaving - map to existing feature if possible
                                    txBeautyManager.setFaceSlimLevel(value);
                                    break;
                                case "eye_alat":
                                    // Eye alat - map to existing feature if possible
                                    txBeautyManager.setEyeAngleLevel(value);
                                    break;
                            }
                        }
                        
                        // Save beauty values to server when they change
                        SimpleDataManager.getInstance().saveBeautyValue();
                    }
                }
            }

            @Override
            public void onFilterChanged(int filterName) {
                if (!CommonAppConfig.getInstance().isMhBeautyEnable()) {
                    if (mRecorder != null) {
                        TXBeautyManager manager = mRecorder.getBeautyManager();
                        if (manager != null) {
                            if (filterName == 0) {
                                manager.setFilter(null);
                            } else {
                                Bitmap bitmap = BitmapUtil.getInstance().decodeBitmap(filterName);
                                manager.setFilter(bitmap);
                            }
                        }
                    }
                }
            }

            @Override
            public boolean isUseMhFilter() {
                return true;
            }

            @Override
            public boolean isTieZhiEnable() {
                return true;
            }
        };
    }


    /**
     * 设置美颜效果
     */
    private void setBeauty() {
        CommonHttpUtil.getBeautyValue(new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0 && info.length > 0) {
                    JSONObject obj = JSON.parseObject(info[0]);
                    if (CommonAppConfig.getInstance().isMhBeautyEnable()) {
                        mBeauty = BEAUTY_MHTECH;
                        if (mRecorder != null) {
                            MhDataManager.getInstance().setMeiYanChangedListener(getMeiYanChangedListener());
                            mRecorder.setVideoProcessListener(new com.tencent.ugc.TXUGCRecord.VideoCustomProcessListener() {

                                @Override
                                public int onTextureCustomProcess(int texture, int width, int height) {
                                    return MhDataManager.getInstance().render(texture, width, height);
                                }

                                @Override
                                public void onDetectFacePoints(float[] floats) {
                                }

                                @Override
                                public void onTextureDestroyed() {
                                    MhDataManager.getInstance().release();
                                }


                            });
                            MeiYanValueBean meiYanValueBean = JSON.parseObject(info[0], MeiYanValueBean.class);
                            MhDataManager.getInstance()
                                    .setMeiYanValue(meiYanValueBean)
                                    .useMeiYan().restoreBeautyValue();
                        }
                    } else {
                        mBeauty = BEAUTY_SIMPLE;
                        SimpleDataManager.getInstance().setMeiYanChangedListener(getMeiYanChangedListener());
                        int meiBai = obj.getIntValue("skin_whiting");
                        int moPi = obj.getIntValue("skin_smooth");
                        int hongRun = obj.getIntValue("skin_tenderness");
                        
                        // Set individual beauty parameters
                        SimpleDataManager instance = SimpleDataManager.getInstance();
                        instance.setMeiBai(meiBai);
                        instance.setMoPi(moPi);
                        instance.setHongRun(hongRun);
                    }
                }
            }
        });
    }


    /**
     * 开始预览
     */
    private void startCameraPreview() {
        if (mRecorder != null && mCustomConfig != null && mVideoView != null) {
            mRecorder.startCameraCustomPreview(mCustomConfig, mVideoView);
            if (!mFrontCamera) {
                mRecorder.switchCamera(false);
            }
            setBeauty();
        }
    }

    /**
     * 停止预览
     */
    private void stopCameraPreview() {
        if (mRecorder != null) {
            if (mRecording) {
                pauseRecord();
            }
            mRecorder.stopCameraPreview();
            mRecorder.setVideoProcessListener(null);
        }
    }

    /**
     * 点击录制
     */
    private void clickRecord() {
        if (mRecordStarted) {
            if (mRecording) {
                pauseRecord();
            } else {
                resumeRecord();
            }
        } else {
            startRecord();
        }
    }

    /**
     * 开始录制
     */
    private void startRecord() {
        if (mRecorder != null) {
            mVideoPath = StringUtil.generateVideoOutputPath();//视频保存的路径
            int result = mRecorder.startRecord(mVideoPath, CommonAppConfig.VIDEO_RECORD_PARTS_PATH, null);//为空表示不需要生成视频封面
            if (result != TXRecordCommon.START_RECORD_OK) {
                if (result == TXRecordCommon.START_RECORD_ERR_NOT_INIT) {
                    ToastUtil.show(com.livestreaming.common.R.string.video_record_tip_1);
                } else if (result == TXRecordCommon.START_RECORD_ERR_IS_IN_RECORDING) {
                    ToastUtil.show(com.livestreaming.common.R.string.video_record_tip_2);
                } else if (result == TXRecordCommon.START_RECORD_ERR_VIDEO_PATH_IS_EMPTY) {
                    ToastUtil.show(com.livestreaming.common.R.string.video_record_tip_3);
                } else if (result == TXRecordCommon.START_RECORD_ERR_API_IS_LOWER_THAN_18) {
                    ToastUtil.show(com.livestreaming.common.R.string.video_record_tip_4);
                } else if (result == TXRecordCommon.START_RECORD_ERR_LICENCE_VERIFICATION_FAILED) {
                    ToastUtil.show(com.livestreaming.common.R.string.video_record_tip_5);
                }
                return;
            }
        }
        mRecordStarted = true;
        mRecording = true;
        resumeBgm();
        startRecordBtnAnim();
        if (mGroup2 != null && mGroup2.getVisibility() == View.VISIBLE) {
            mGroup2.setVisibility(View.INVISIBLE);
        }
    }


    /**
     * 暂停录制
     */
    private void pauseRecord() {
        if (mRecorder == null) {
            return;
        }
        pauseBgm();
        mRecorder.pauseRecord();
        mRecording = false;
        stopRecordBtnAnim();
        delay(new Runnable() {
            @Override
            public void run() {
                if (mRecorder == null) {
                    return;
                }
                if (mGroup2 != null && mGroup2.getVisibility() != View.VISIBLE) {
                    mGroup2.setVisibility(View.VISIBLE);
                }
                TXUGCPartsManager partsManager = mRecorder.getPartsManager();
                if (partsManager != null) {
                    List<String> partList = partsManager.getPartsPathList();
                    if (partList != null && partList.size() > 0) {
                        if (mGroup3 != null && mGroup3.getVisibility() == View.VISIBLE) {
                            mGroup3.setVisibility(View.INVISIBLE);
                        }
                        if (mGroup4 != null && mGroup4.getVisibility() != View.VISIBLE) {
                            mGroup4.setVisibility(View.VISIBLE);
                        }
                    } else {
                        if (mGroup3 != null && mGroup3.getVisibility() != View.VISIBLE) {
                            mGroup3.setVisibility(View.VISIBLE);
                        }
                        if (mGroup4 != null && mGroup4.getVisibility() == View.VISIBLE) {
                            mGroup4.setVisibility(View.INVISIBLE);
                        }
                    }
                }
            }
        }, 200);

    }

    /**
     * 恢复录制
     */
    private void resumeRecord() {
        if (mRecorder != null) {
            int startResult = mRecorder.resumeRecord();
            if (startResult != TXRecordCommon.START_RECORD_OK) {
                ToastUtil.show(WordUtil.getString(com.livestreaming.common.R.string.video_record_failed));
                return;
            }
        }
        mRecording = true;
        resumeBgm();
        startRecordBtnAnim();
        if (mGroup2 != null && mGroup2.getVisibility() == View.VISIBLE) {
            mGroup2.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * 暂停背景音乐
     */
    private void pauseBgm() {
        if (mRecorder != null) {
            mRecorder.pauseBGM();
        }
    }

    /**
     * 恢复背景音乐
     */
    private void resumeBgm() {
        if (mRecorder == null) {
            return;
        }
        if (!mBgmPlayStarted) {
            if (mMusicBean == null) {
                return;
            }
            int bgmDuration = mRecorder.setBGM(mMusicBean.getLocalPath());
            mRecorder.playBGMFromTime(0, bgmDuration);
            mRecorder.setBGMVolume(1);//背景音为1最大
            mRecorder.setMicVolume(0);//原声音为0
            mBgmPlayStarted = true;
        } else {
            mRecorder.resumeBGM();
        }
    }

    /**
     * 按钮录制动画开始
     */
    private void startRecordBtnAnim() {
        if (mRecordView != null) {
            mRecordView.setBackground(mRecordDrawable);
        }
        if (mRecordBtnAnimator != null) {
            mRecordBtnAnimator.start();
        }
    }

    /**
     * 按钮录制动画停止
     */
    private void stopRecordBtnAnim() {
        if (mRecordView != null) {
            mRecordView.setBackground(mUnRecordDrawable);
        }
        if (mRecordBtnAnimator != null) {
            mRecordBtnAnimator.cancel();
        }
        if (mVideoRecordBtnView != null) {
            mVideoRecordBtnView.reset();
        }
    }

    /**
     * 调整录制速度
     */
    private void changeRecordSpeed(int speed) {
        if (mRecordSpeed == speed) {
            return;
        }
        mRecordSpeed = speed;
        if (mRecorder != null) {
            mRecorder.setRecordSpeed(speed);
        }
    }

    /**
     * 删除录制分段
     */
    private void clickDelete() {
        DialogUitl.showSimpleDialog(mContext, WordUtil.getString(com.livestreaming.common.R.string.video_record_delete_last), new DialogUitl.SimpleCallback() {
            @Override
            public void onConfirmClick(Dialog dialog, String content) {
                doClickDelete();
            }
        });
    }

    /**
     * 删除录制分段
     */
    private void doClickDelete() {
        if (!mRecordStarted || mRecording || mRecorder == null) {
            return;
        }
        TXUGCPartsManager partsManager = mRecorder.getPartsManager();
        if (partsManager == null) {
            return;
        }
        List<String> partList = partsManager.getPartsPathList();
        if (partList == null || partList.size() == 0) {
            return;
        }
        partsManager.deleteLastPart();
        int time = partsManager.getDuration();
        if (mTime != null) {
            mTime.setText(StringUtil.contact(String.format("%.2f", time / 1000f), "s"));
        }
        mRecordTime = time;
        setBtnMusicEnable(time == 0);
        if (time < MIN_DURATION && mBtnNext != null && mBtnNext.getVisibility() == View.VISIBLE) {
            mBtnNext.setVisibility(View.INVISIBLE);
        }
        if (mRecordProgressView != null) {
            mRecordProgressView.deleteLast();
        }
        partList = partsManager.getPartsPathList();
        if (partList != null && partList.size() == 0) {
            if (mGroup2 != null && mGroup2.getVisibility() != View.VISIBLE) {
                mGroup2.setVisibility(View.VISIBLE);
            }
            if (mGroup3 != null && mGroup3.getVisibility() != View.VISIBLE) {
                mGroup3.setVisibility(View.VISIBLE);
            }
            if (mGroup4 != null && mGroup4.getVisibility() == View.VISIBLE) {
                mGroup4.setVisibility(View.INVISIBLE);
            }
        }
    }

    /**
     * 结束录制，会触发 onRecordComplete
     */
    public void clickNext() {
        stopRecordBtnAnim();
        if (mRecorder != null) {
            mRecorder.stopBGM();
            mRecorder.stopRecord();
            showProccessDialog();
        }
    }


    /**
     * 录制结束时候显示处理中的弹窗
     */
    private void showProccessDialog() {
        if (mStopRecordDialog == null) {
            mStopRecordDialog = DialogUitl.loadingDialog(mContext, WordUtil.getString(com.livestreaming.common.R.string.video_processing));
            mStopRecordDialog.show();
        }
    }

    /**
     * 隐藏处理中的弹窗
     */
    private void hideProccessDialog() {
        if (mStopRecordDialog != null) {
            mStopRecordDialog.dismiss();
        }
        mStopRecordDialog = null;
    }


    @Override
    public void onBackPressed() {
        if (!canClick()) {
            return;
        }
        if (mBeautyViewHolder != null && mBeautyViewHolder.isShowed()) {
            mBeautyViewHolder.hide();
            return;
        }
        if (mVideoMusicViewHolder != null && mVideoMusicViewHolder.isShowed()) {
            mVideoMusicViewHolder.hide();
            return;
        }
        List<Integer> list = new ArrayList<>();
        if (mRecordTime > 0) {
            list.add(com.livestreaming.common.R.string.video_re_record);
        }
        list.add(com.livestreaming.common.R.string.video_exit);
        DialogUitl.showStringArrayDialog(mContext, list.toArray(new Integer[list.size()]), new DialogUitl.StringArrayDialogCallback() {
            @Override
            public void onItemClick(String text, int tag) {
                if (tag == com.livestreaming.common.R.string.video_re_record) {
                    reRecord();
                } else if (tag ==com.livestreaming.common. R.string.video_exit) {
                    exit();
                }
            }
        });
    }

    /**
     * 重新录制
     */
    private void reRecord() {
        if (mRecorder == null) {
            return;
        }
        mRecorder.pauseBGM();
        mMusicBean = null;
        mBgmPlayStarted = false;
        mRecorder.pauseRecord();
        mRecording = false;
        stopRecordBtnAnim();
        if (mGroup2 != null && mGroup2.getVisibility() != View.VISIBLE) {
            mGroup2.setVisibility(View.VISIBLE);
        }
        TXUGCPartsManager partsManager = mRecorder.getPartsManager();
        if (partsManager != null) {
            partsManager.deleteAllParts();
        }
//        mRecorder.onDeleteAllParts();
        if (mTime != null) {
            mTime.setText("0.00s");
        }
        mRecordTime = 0;
        setBtnMusicEnable(true);
        if (mBtnNext != null && mBtnNext.getVisibility() == View.VISIBLE) {
            mBtnNext.setVisibility(View.INVISIBLE);
        }
        if (mRecordProgressView != null) {
            mRecordProgressView.deleteAll();
        }
        if (mGroup3 != null && mGroup3.getVisibility() != View.VISIBLE) {
            mGroup3.setVisibility(View.VISIBLE);
        }
        if (mGroup4 != null && mGroup4.getVisibility() == View.VISIBLE) {
            mGroup4.setVisibility(View.INVISIBLE);
        }

    }

    /**
     * 退出
     */
    private void exit() {
        release();
        finish();
    }


    @Override
    protected void onStop() {
        super.onStop();
        if (mRecorder != null && mBtnFlash != null && mBtnFlash.isChecked()) {
            mBtnFlash.doToggle();
            mRecorder.toggleTorch(mBtnFlash.isChecked());
        }
    }

    @Override
    protected void onDestroy() {
        if (mBeauty == BEAUTY_MHTECH) {
            MhDataManager.getInstance().release();
        } else if (mBeauty == BEAUTY_SIMPLE) {
            // No need to explicitly release resources
            SimpleDataManager.getInstance().saveBeautyValue();
        }
        super.onDestroy();
        L.e(TAG, "-------->onDestroy");
    }

    private void release() {
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        mHandler = null;
        CommonHttpUtil.cancel(CommonHttpConsts.GET_BEAUTY_VALUE);
        stopCameraPreview();
        if (mStopRecordDialog != null && mStopRecordDialog.isShowing()) {
            mStopRecordDialog.dismiss();
        }
        if (mVideoMusicViewHolder != null) {
            mVideoMusicViewHolder.release();
        }
        if (mRecordProgressView != null) {
            mRecordProgressView.release();
        }
        if (mRecordBtnAnimator != null) {
            mRecordBtnAnimator.cancel();
        }
        if (mRecorder != null) {
            mRecorder.toggleTorch(false);
            mRecorder.stopBGM();
            if (mRecordStarted) {
                mRecorder.stopRecord();
            }
            mRecorder.stopCameraPreview();
            mRecorder.setVideoProcessListener(null);
            mRecorder.setVideoRecordListener(null);
            TXUGCPartsManager getPartsManager = mRecorder.getPartsManager();
            if (getPartsManager != null) {
                getPartsManager.deleteAllParts();
            }
            mRecorder.release();
        }
        if (CommonAppConfig.getInstance().isMhBeautyEnable()) {
            MhDataManager.getInstance().release();
        } else {
            SimpleDataManager.getInstance().saveBeautyValue();
        }
        mStopRecordDialog = null;
        mBeautyViewHolder = null;
        mVideoMusicViewHolder = null;
        mRecordProgressView = null;
        mRecordBtnAnimator = null;
        mRecorder = null;
    }


    private boolean mPaused;

    @Override
    protected void onPause() {
        super.onPause();
        mPaused = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mPaused) {
            mPaused = false;
            if (mRecorder != null) {
                mRecorder.switchCamera(mFrontCamera);
            }
        }
    }

    private void delay(Runnable runnable, int time) {
        if (mHandler != null) {
            mHandler.postDelayed(runnable, time);
        }
    }
}
