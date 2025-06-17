package com.livestreaming.common.utils;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.core.content.FileProvider;

import android.text.TextUtils;

import com.yalantis.ucrop.UCrop;
import com.livestreaming.common.CommonAppConfig;
import com.livestreaming.common.R;
import com.livestreaming.common.interfaces.ActivityResultCallback;
import com.livestreaming.common.interfaces.ImageResultCallback;
import com.livestreaming.common.interfaces.PermissionCallback;
import com.livestreaming.common.interfaces.VideoResultCallback;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by cxf on 2018/9/29.
 * 选择图片 裁剪,录视频等
 */

public class MediaUtil {

    public static final String FILE_PROVIDER = "com.livestreaming.mylive.fileprovider";

    /**
     * 拍照获取图片
     */

    public static void getImageByCamera(final FragmentActivity activity, final boolean needCrop, final ImageResultCallback imageResultCallback) {

        PermissionCallback permissionCallback = new PermissionCallback() {
            @Override
            public void onAllGranted() {
                callBackForCamImageSelection(activity,needCrop,imageResultCallback);
            }
        };

        if(Build.VERSION.SDK_INT>33){
            PermissionUtil.request(
                    activity,
                    permissionCallback,
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_AUDIO,
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED,
                    Manifest.permission.CAMERA
            );
        }else if(Build.VERSION.SDK_INT==33){
            PermissionUtil.request(
                    activity,
                    permissionCallback,
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_AUDIO,
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA
            );
        }else{
            PermissionUtil.request(
                    activity,
                    permissionCallback,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA
            );
        }

    }

    public static void callBackForCamImageSelection(FragmentActivity activity, boolean needCrop, ImageResultCallback imageResultCallback) {
        if (imageResultCallback != null) {
            imageResultCallback.beforeCamera();
        }
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        final File cameraResult = getNewFile();
        Uri uri = null;
        if (Build.VERSION.SDK_INT >= 24) {
            uri = FileProvider.getUriForFile(activity, FILE_PROVIDER, cameraResult);
//                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        } else {
            uri = Uri.fromFile(cameraResult);
        }
        final Uri finalUri = uri;
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        //开始拍照
        ActivityResultUtil.startActivityForResult(activity, intent, new ActivityResultCallback() {
            @Override
            public void onSuccess(Intent intent) {
                if (needCrop) {//需要裁剪
                    crop(activity, finalUri, imageResultCallback);
                } else {
                    if (imageResultCallback != null) {
                        imageResultCallback.onSuccess(cameraResult);
                    }
                }
            }

            @Override
            public void onFailure() {
                ToastUtil.show(R.string.img_camera_cancel);
                if (imageResultCallback != null) {
                    imageResultCallback.onFailure();
                }
            }
        });
    }

    /**
     * 拍照获取图片
     */
    public static void getImageByCamera(FragmentActivity activity, ImageResultCallback imageResultCallback) {
        getImageByCamera(activity, true, imageResultCallback);
    }


    /**
     * 相册获取图片
     */
    public static void getImageByAlumb(final FragmentActivity activity, final boolean needCrop, final ImageResultCallback imageResultCallback) {
        //请求存储的权限的回调
        PermissionCallback permissionCallback = new PermissionCallback() {
            @Override
            public void onAllGranted() {
                callBackForImageSelectionAlboum(activity,needCrop,imageResultCallback);

            }
        };

        if(Build.VERSION.SDK_INT>33){
            PermissionUtil.request(
                    activity,
                    permissionCallback,
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_AUDIO,
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED,
                    Manifest.permission.CAMERA
            );
        }else if(Build.VERSION.SDK_INT==33){
            PermissionUtil.request(
                    activity,
                    permissionCallback,
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_AUDIO,
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA
            );
        }else{
            PermissionUtil.request(
                    activity,
                    permissionCallback,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA
            );
        }
    }

    public static void callBackForImageSelectionAlboum(FragmentActivity activity, boolean needCrop, ImageResultCallback imageResultCallback) {
        Intent intent = new Intent();
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        ActivityResultUtil.startActivityForResult(activity, intent, new ActivityResultCallback() {
            @Override
            public void onSuccess(Intent intent) {
                Uri dataUri = intent.getData();
                if (dataUri != null) {
                    String uriStr = dataUri.toString();
                    File dir = new File(CommonAppConfig.IMAGE_PATH);
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }
                    File copyFile = new File(dir, MD5Util.getMD5(uriStr) + ".png");
                    if (!copyFile.exists() || copyFile.length() <= 0) {
                        InputStream inputStream = null;
                        FileOutputStream outputStream = null;
                        try {
                            inputStream = activity.getContentResolver().openInputStream(dataUri);
                            outputStream = new FileOutputStream(copyFile);
                            byte[] buf = new byte[4096];
                            int len = 0;
                            while ((len = inputStream.read(buf)) > 0) {
                                outputStream.write(buf, 0, len);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                if (outputStream != null) {
                                    outputStream.close();
                                }
                                if (inputStream != null) {
                                    inputStream.close();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    if (copyFile.exists() && copyFile.length() > 0) {
                        if (needCrop) {
                            crop(activity, Uri.fromFile(copyFile), imageResultCallback);
                        } else {
                            if (imageResultCallback != null) {
                                imageResultCallback.onSuccess(copyFile);
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure() {
                ToastUtil.show(R.string.img_alumb_cancel);
                if (imageResultCallback != null) {
                    imageResultCallback.onFailure();
                }
            }
        });
    }

    /**
     * 相册获取图片
     */
    public static void getImageByAlumb(FragmentActivity activity, ImageResultCallback imageResultCallback) {
        getImageByAlumb(activity, true, imageResultCallback);
    }

    /**
     * 录制视频
     */
    public static void startVideoRecord(final FragmentActivity activity, final VideoResultCallback videoResultCallback) {
        PermissionCallback permissionCallback = new PermissionCallback() {
            @Override
            public void onAllGranted() {
                Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);// 表示跳转至相机的录视频界面
                final File videoResult = getNewVideoFile();
                Uri uri = null;
                if (Build.VERSION.SDK_INT >= 24) {
                    uri = FileProvider.getUriForFile(activity, FILE_PROVIDER, videoResult);
                    //intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                } else {
                    uri = Uri.fromFile(videoResult);
                }
                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 15);
                //开始录制
                ActivityResultUtil.startActivityForResult(activity, intent, new ActivityResultCallback() {
                    @Override
                    public void onSuccess(Intent intent) {
                        if (intent != null && intent.getData() != null && videoResultCallback != null) {
                            if (videoResult != null && videoResult.exists() && videoResult.length() > 0) {
                                String path = videoResult.getAbsolutePath();
                                long duration = 0;
                                MediaMetadataRetriever mmr = null;
                                try {
                                    mmr = new MediaMetadataRetriever();
                                    mmr.setDataSource(path);
                                    String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                                    if (!TextUtils.isEmpty(durationStr) && StringUtil.isInt(durationStr)) {
                                        duration = Long.parseLong(durationStr);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                } finally {
                                    if (mmr != null) {
                                        try {
                                            mmr.release();
                                        } catch (IOException e) {
                                            throw new RuntimeException(e);
                                        }
                                    }
                                    mmr = null;
                                }
                                saveVideoInfo(activity, path, duration);
                                videoResultCallback.onSuccess(videoResult, duration);
                            }
                        }
                    }

                    @Override
                    public void onFailure() {
                        ToastUtil.show(R.string.record_cancel);
                    }
                });
            }
        };
        if(Build.VERSION.SDK_INT>33){
            Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);// 表示跳转至相机的录视频界面
            final File videoResult = getNewVideoFile();
            Uri uri = null;
            if (Build.VERSION.SDK_INT >= 24) {
                uri = FileProvider.getUriForFile(activity, FILE_PROVIDER, videoResult);
                //intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            } else {
                uri = Uri.fromFile(videoResult);
            }
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 15);
            //开始录制
            ActivityResultUtil.startActivityForResult(activity, intent, new ActivityResultCallback() {
                @Override
                public void onSuccess(Intent intent) {
                    if (intent != null && intent.getData() != null && videoResultCallback != null) {
                        if (videoResult != null && videoResult.exists() && videoResult.length() > 0) {
                            String path = videoResult.getAbsolutePath();
                            long duration = 0;
                            MediaMetadataRetriever mmr = null;
                            try {
                                mmr = new MediaMetadataRetriever();
                                mmr.setDataSource(path);
                                String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                                if (!TextUtils.isEmpty(durationStr) && StringUtil.isInt(durationStr)) {
                                    duration = Long.parseLong(durationStr);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            } finally {
                                if (mmr != null) {
                                    try {
                                        mmr.release();
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                                mmr = null;
                            }
                            saveVideoInfo(activity, path, duration);
                            videoResultCallback.onSuccess(videoResult, duration);
                        }
                    }
                }

                @Override
                public void onFailure() {
                    ToastUtil.show(R.string.record_cancel);
                }
            });
        }else if(Build.VERSION.SDK_INT==33){
            PermissionUtil.request(
                    activity,
                    permissionCallback,
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_AUDIO,
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA
            );
        }else{
            PermissionUtil.request(
                    activity,
                    permissionCallback,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA
            );
        }
    }

    public static File getNewFile() {
        // 裁剪头像的绝对路径
        File dir = new File(CommonAppConfig.IMAGE_PATH);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return new File(dir, DateFormatUtil.getCurTimeString() + ".png");
    }


    /**
     * 裁剪
     */
    public static void crop(FragmentActivity activity, Uri inputUri, final ImageResultCallback imageResultCallback) {
        final File corpResult = getNewFile();
        try {
            int color = ContextCompat.getColor(activity, R.color.colorAccent);
            UCrop.Options options = new UCrop.Options();
            options.setToolbarColor(color);
            options.setToolbarWidgetColor(Color.WHITE);
            options.setStatusBarColor(color);
            options.setActiveControlsWidgetColor(color);
            UCrop uCrop = UCrop.of(inputUri, Uri.fromFile(corpResult))
                    .withAspectRatio(1, 1)
                    .withMaxResultSize(400, 400)
                    .withOptions(options);
            Intent intent = uCrop.getIntent(activity);
            ActivityResultUtil.startActivityForResult(activity, intent, new ActivityResultCallback() {
                @Override
                public void onSuccess(Intent intent) {
                    if (imageResultCallback != null) {
                        imageResultCallback.onSuccess(corpResult);
                    }
                }

                @Override
                public void onFailure() {
                    ToastUtil.show(R.string.img_crop_cancel);
                    if (imageResultCallback != null) {
                        imageResultCallback.onFailure();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            if (imageResultCallback != null) {
                imageResultCallback.onFailure();
            }
            ToastUtil.show(R.string.img_crop_failed);
        }
    }


    private static File getNewVideoFile() {
        File dir = new File(CommonAppConfig.VIDEO_RECORD_PATH);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return new File(dir, DateFormatUtil.getCurTimeString() + ".mp4");
    }

    /**
     * 把视频保存到ContentProvider,在选择上传的时候能找到
     */
    public static void saveVideoInfo(Context context, String videoPath, long duration) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            try {
                File videoFile = new File(videoPath);
                String fileName = videoFile.getName();
                long currentTimeMillis = System.currentTimeMillis();
                ContentValues values = new ContentValues();
                values.put(MediaStore.MediaColumns.TITLE, fileName);
                values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                values.put(MediaStore.MediaColumns.DATE_MODIFIED, currentTimeMillis);
                values.put(MediaStore.MediaColumns.DATE_ADDED, currentTimeMillis);
                values.put(MediaStore.MediaColumns.DATA, videoPath);
                values.put(MediaStore.MediaColumns.SIZE, videoFile.length());
                values.put(MediaStore.Video.VideoColumns.DATE_TAKEN, currentTimeMillis);
                values.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4");
                values.put(MediaStore.Video.VideoColumns.DURATION, duration);
                context.getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
