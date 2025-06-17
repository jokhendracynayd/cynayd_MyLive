package com.livestreaming.video.activity;

import android.Manifest;
import android.app.Dialog;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.WindowManager;

import com.livestreaming.common.CommonAppConfig;
import com.livestreaming.common.CommonAppContext;
import com.livestreaming.common.bean.ConfigBean;
import com.livestreaming.common.http.HttpCallback;
import com.livestreaming.common.http.UriDownloadCallback;
import com.livestreaming.common.interfaces.CommonCallback;
import com.livestreaming.common.interfaces.PermissionCallback;
import com.livestreaming.common.utils.DialogUitl;
import com.livestreaming.common.utils.DownloadUtil;
import com.livestreaming.common.utils.PermissionUtil;
import com.livestreaming.common.utils.StringUtil;
import com.livestreaming.common.utils.ToastUtil;
import com.livestreaming.common.utils.WordUtil;
import com.livestreaming.video.bean.VideoBean;
import com.livestreaming.video.event.VideoDeleteEvent;
import com.livestreaming.video.http.VideoHttpConsts;
import com.livestreaming.video.http.VideoHttpUtil;
import com.livestreaming.video.utils.VideoStorge;
import com.livestreaming.video.views.VideoScrollViewHolder;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by cxf on 2019/2/28.
 */

public abstract class AbsVideoPlayActivity extends AbsVideoCommentActivity {

    protected VideoScrollViewHolder mVideoScrollViewHolder;
    private Dialog mDownloadVideoDialog;
    private DownloadUtil mDownloadUtil;
    private ConfigBean mConfigBean;
    protected String mVideoKey;
    private boolean mPaused;


    @Override
    protected void main() {
        super.main();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        CommonAppConfig.getInstance().getConfig(new CommonCallback<ConfigBean>() {
            @Override
            public void callback(ConfigBean bean) {
                mConfigBean = bean;
            }
        });
    }



    public void downloadVideo(final VideoBean videoBean) {
        if (videoBean == null || TextUtils.isEmpty(videoBean.getHrefW())) {
            return;
        }
        if(Build.VERSION.SDK_INT>33){

            PermissionUtil.request(this, new PermissionCallback() {
                        @Override
                        public void onAllGranted() {
                            mDownloadVideoDialog = DialogUitl.loadingDialog(mContext);
                            mDownloadVideoDialog.show();
                            if (mDownloadUtil == null) {
                                mDownloadUtil = new DownloadUtil(videoBean.getTag());
                            }
                            long currentTimeMillis = SystemClock.uptimeMillis();
                            String fileName = StringUtil.generateFileName() + ".mp4";
                            ContentValues values = new ContentValues();
                            values.put(MediaStore.MediaColumns.TITLE, fileName);
                            values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                            values.put(MediaStore.MediaColumns.DATE_MODIFIED, currentTimeMillis);
                            values.put(MediaStore.MediaColumns.DATE_ADDED, currentTimeMillis);
                            values.put(MediaStore.Video.VideoColumns.DATE_TAKEN, currentTimeMillis);
                            values.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4");
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM);
                            } else {
                                values.put(MediaStore.MediaColumns.DATA, CommonAppConfig.VIDEO_DOWNLOAD_PATH + fileName);
                            }
                            Uri uri = CommonAppContext.getInstance().getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
                            mDownloadUtil.download(uri, videoBean.getHrefW(), new UriDownloadCallback() {
                                @Override
                                public void onSuccess() {
                                    ToastUtil.show(com.livestreaming.common.R.string.video_download_success);
                                    if (mDownloadVideoDialog != null && mDownloadVideoDialog.isShowing()) {
                                        mDownloadVideoDialog.dismiss();
                                    }
                                    mDownloadVideoDialog = null;
                                }


                                @Override
                                public void onError(Throwable e) {
                                    ToastUtil.show(com.livestreaming.common.R.string.video_download_failed);
                                    if (mDownloadVideoDialog != null && mDownloadVideoDialog.isShowing()) {
                                        mDownloadVideoDialog.dismiss();
                                    }
                                    mDownloadVideoDialog = null;
                                }
                            });
                        }
                    }, Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.READ_MEDIA_AUDIO,
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);

        }else  if(Build.VERSION.SDK_INT==33){

            PermissionUtil.request(this, new PermissionCallback() {
                        @Override
                        public void onAllGranted() {
                            mDownloadVideoDialog = DialogUitl.loadingDialog(mContext);
                            mDownloadVideoDialog.show();
                            if (mDownloadUtil == null) {
                                mDownloadUtil = new DownloadUtil(videoBean.getTag());
                            }
                            long currentTimeMillis = SystemClock.uptimeMillis();
                            String fileName = StringUtil.generateFileName() + ".mp4";
                            ContentValues values = new ContentValues();
                            values.put(MediaStore.MediaColumns.TITLE, fileName);
                            values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                            values.put(MediaStore.MediaColumns.DATE_MODIFIED, currentTimeMillis);
                            values.put(MediaStore.MediaColumns.DATE_ADDED, currentTimeMillis);
                            values.put(MediaStore.Video.VideoColumns.DATE_TAKEN, currentTimeMillis);
                            values.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4");
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM);
                            } else {
                                values.put(MediaStore.MediaColumns.DATA, CommonAppConfig.VIDEO_DOWNLOAD_PATH + fileName);
                            }
                            Uri uri = CommonAppContext.getInstance().getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
                            mDownloadUtil.download(uri, videoBean.getHrefW(), new UriDownloadCallback() {
                                @Override
                                public void onSuccess() {
                                    ToastUtil.show(com.livestreaming.common.R.string.video_download_success);
                                    if (mDownloadVideoDialog != null && mDownloadVideoDialog.isShowing()) {
                                        mDownloadVideoDialog.dismiss();
                                    }
                                    mDownloadVideoDialog = null;
                                }


                                @Override
                                public void onError(Throwable e) {
                                    ToastUtil.show(com.livestreaming.common.R.string.video_download_failed);
                                    if (mDownloadVideoDialog != null && mDownloadVideoDialog.isShowing()) {
                                        mDownloadVideoDialog.dismiss();
                                    }
                                    mDownloadVideoDialog = null;
                                }
                            });
                        }
                    }, Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.READ_MEDIA_AUDIO,
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);

        }else{
            PermissionUtil.request(this, new PermissionCallback() {
                        @Override
                        public void onAllGranted() {
                            mDownloadVideoDialog = DialogUitl.loadingDialog(mContext);
                            mDownloadVideoDialog.show();
                            if (mDownloadUtil == null) {
                                mDownloadUtil = new DownloadUtil(videoBean.getTag());
                            }
                            long currentTimeMillis = SystemClock.uptimeMillis();
                            String fileName = StringUtil.generateFileName() + ".mp4";
                            ContentValues values = new ContentValues();
                            values.put(MediaStore.MediaColumns.TITLE, fileName);
                            values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                            values.put(MediaStore.MediaColumns.DATE_MODIFIED, currentTimeMillis);
                            values.put(MediaStore.MediaColumns.DATE_ADDED, currentTimeMillis);
                            values.put(MediaStore.Video.VideoColumns.DATE_TAKEN, currentTimeMillis);
                            values.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4");
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM);
                            } else {
                                values.put(MediaStore.MediaColumns.DATA, CommonAppConfig.VIDEO_DOWNLOAD_PATH + fileName);
                            }
                            Uri uri = CommonAppContext.getInstance().getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
                            mDownloadUtil.download(uri, videoBean.getHrefW(), new UriDownloadCallback() {
                                @Override
                                public void onSuccess() {
                                    ToastUtil.show(com.livestreaming.common.R.string.video_download_success);
                                    if (mDownloadVideoDialog != null && mDownloadVideoDialog.isShowing()) {
                                        mDownloadVideoDialog.dismiss();
                                    }
                                    mDownloadVideoDialog = null;
                                }


                                @Override
                                public void onError(Throwable e) {
                                    ToastUtil.show(com.livestreaming.common.R.string.video_download_failed);
                                    if (mDownloadVideoDialog != null && mDownloadVideoDialog.isShowing()) {
                                        mDownloadVideoDialog.dismiss();
                                    }
                                    mDownloadVideoDialog = null;
                                }
                            });
                        }
                    }, Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
    }

    /**
     * 删除视频
     */
    public void deleteVideo(final VideoBean videoBean) {
        DialogUitl.showSimpleDialog(mContext, WordUtil.getString(com.livestreaming.common.R.string.confirm_delete), new DialogUitl.SimpleCallback() {
            @Override
            public void onConfirmClick(Dialog dialog, String content) {
                VideoHttpUtil.videoDelete(videoBean.getId(), new HttpCallback() {
                    @Override
                    public void onSuccess(int code, String msg, String[] info) {
                        if (code == 0) {
                            if (mVideoScrollViewHolder != null) {
                                mVideoScrollViewHolder.deleteVideo(videoBean);
                                EventBus.getDefault().post(new VideoDeleteEvent(videoBean.getId()));
                            }
                        }
                        ToastUtil.show(msg);
                    }
                });
            }
        });
    }


    public boolean isPaused() {
        return mPaused;
    }

    @Override
    protected void onPause() {
        mPaused = true;
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPaused = false;
    }

    @Override
    public void release() {
        super.release();
        VideoHttpUtil.cancel(VideoHttpConsts.SET_VIDEO_SHARE);
        VideoHttpUtil.cancel(VideoHttpConsts.VIDEO_DELETE);
        if (mDownloadVideoDialog != null && mDownloadVideoDialog.isShowing()) {
            mDownloadVideoDialog.dismiss();
        }
        if (mVideoScrollViewHolder != null) {
            mVideoScrollViewHolder.release();
        }
        VideoStorge.getInstance().removeDataHelper(mVideoKey);
        mDownloadVideoDialog = null;
        mVideoScrollViewHolder = null;
    }


    public void setVideoScrollViewHolder(VideoScrollViewHolder videoScrollViewHolder) {
        mVideoScrollViewHolder = videoScrollViewHolder;
    }
}
