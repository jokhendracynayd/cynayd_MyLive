package com.livestreaming.live.activity;

import static com.livestreaming.common.utils.MediaUtil.FILE_PROVIDER;
import static com.livestreaming.common.utils.MediaUtil.crop;
import static com.livestreaming.common.utils.MediaUtil.getNewFile;

import android.Manifest;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.FileProvider;

import com.alibaba.fastjson.JSON;
import com.livestreaming.common.CommonAppConfig;
import com.livestreaming.common.CommonAppContext;
import com.livestreaming.common.Constants;
import com.livestreaming.common.activity.AbsActivity;
import com.livestreaming.common.event.UpdateFieldEvent;
import com.livestreaming.common.glide.ImgLoader;
import com.livestreaming.common.http.HttpCallback;
import com.livestreaming.common.http.UriDownloadCallback;
import com.livestreaming.common.interfaces.ActivityResultCallback;
import com.livestreaming.common.interfaces.CommonCallback;
import com.livestreaming.common.interfaces.ImageResultCallback;
import com.livestreaming.common.interfaces.PermissionCallback;
import com.livestreaming.common.upload.UploadBean;
import com.livestreaming.common.upload.UploadCallback;
import com.livestreaming.common.upload.UploadStrategy;
import com.livestreaming.common.upload.UploadUtil;
import com.livestreaming.common.utils.ActivityResultUtil;
import com.livestreaming.common.utils.DialogUitl;
import com.livestreaming.common.utils.DownloadUtil;
import com.livestreaming.common.utils.MD5Util;
import com.livestreaming.common.utils.MediaUtil;
import com.livestreaming.common.utils.PermissionUtil;
import com.livestreaming.common.utils.StringUtil;
import com.livestreaming.common.utils.ToastUtil;
import com.livestreaming.live.R;
import com.livestreaming.live.http.LiveHttpConsts;
import com.livestreaming.live.http.LiveHttpUtil;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by 云豹科技 on 2022/6/6.
 */
public class UserHomeBgActivity extends AbsActivity implements View.OnClickListener {

    private ImageView mImg;
    private String mImgUrl;

    private ImageResultCallback mImageResultCallback = new ImageResultCallback() {
        @Override
        public void beforeCamera() {

        }

        @Override
        public void onSuccess(File file) {
            if (file != null && file.exists()) {
                ImgLoader.display(mContext, file, mImg);

                final List<UploadBean> list = new ArrayList<>();
                list.add(new UploadBean(file, UploadBean.IMG));

                final Dialog loading = DialogUitl.loadingDialog(mContext);
                loading.show();

                UploadUtil.startUpload(new CommonCallback<UploadStrategy>() {
                    @Override
                    public void callback(UploadStrategy uploadStrategy) {
                        uploadStrategy.upload(list, true, new UploadCallback() {
                            @Override
                            public void onFinish(List<UploadBean> list, boolean success) {
                                if (success) {
                                    LiveHttpUtil.setUserHomeBgImg(list.get(0).getRemoteFileName(), new HttpCallback() {
                                        @Override
                                        public void onSuccess(int code, String msg, String[] info) {
                                            if (code == 0) {
                                                if (info.length > 0) {
                                                    mImgUrl = JSON.parseObject(info[0]).getString("bg_img");
                                                }
                                                EventBus.getDefault().post(new UpdateFieldEvent());
                                            }
                                            ToastUtil.show(msg);
                                        }

                                        @Override
                                        public void onFinish() {
                                            loading.dismiss();
                                        }
                                    });
                                }
                            }
                        });
                    }
                });


            } else {
                ToastUtil.show(com.livestreaming.common.R.string.file_not_exist);
            }
        }


        @Override
        public void onFailure() {
        }
    };
    private ActivityResultLauncher<String[]> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permissions -> {
                long currentTimeMillis = SystemClock.uptimeMillis();
                String fileName = StringUtil.generateFileName() + ".png";
                ContentValues values = new ContentValues();
                values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                values.put(MediaStore.MediaColumns.MIME_TYPE, "image/png");
                values.put(MediaStore.MediaColumns.TITLE, fileName);
                values.put(MediaStore.MediaColumns.DATE_MODIFIED, currentTimeMillis);
                values.put(MediaStore.MediaColumns.DATE_ADDED, currentTimeMillis);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM);
                } else {
                    values.put(MediaStore.MediaColumns.DATA, CommonAppConfig.IMAGE_DOWNLOAD_PATH + fileName);
                }
                Uri uri = CommonAppContext.getInstance().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                new DownloadUtil("save_img").download(uri, mImgUrl, new UriDownloadCallback() {
                    @Override
                    public void onSuccess() {
                        ToastUtil.show(com.livestreaming.common.R.string.save_success);
                    }
                });
            });
    int selectedType = 0;
    private ActivityResultLauncher<String[]> requestPermissionLauncherImage =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permissions -> {
                if (selectedType == 1) {
                    if (mImageResultCallback != null) {
                        mImageResultCallback.beforeCamera();
                    }
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    final File cameraResult = getNewFile();
                    Uri uri = null;
                    if (Build.VERSION.SDK_INT >= 24) {
                        uri = FileProvider.getUriForFile(UserHomeBgActivity.this, FILE_PROVIDER, cameraResult);
//                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    } else {
                        uri = Uri.fromFile(cameraResult);
                    }
                    final Uri finalUri = uri;
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                    //开始拍照
                    ActivityResultUtil.startActivityForResult(UserHomeBgActivity.this, intent, new ActivityResultCallback() {
                        @Override
                        public void onSuccess(Intent intent) {
                            MediaUtil.crop(UserHomeBgActivity.this, finalUri, mImageResultCallback);
                        }

                        @Override
                        public void onFailure() {
                            ToastUtil.show(com.livestreaming.common.R.string.img_camera_cancel);
                            if (mImageResultCallback != null) {
                                mImageResultCallback.onFailure();
                            }
                        }
                    });
                } else if (selectedType == 2) {
                    MediaUtil.getImageByAlumb(UserHomeBgActivity.this, mImageResultCallback);
                    Intent intent = new Intent();
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("image/*");
                    if (Build.VERSION.SDK_INT < 19) {
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                    } else {
                        intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                    }
                    ActivityResultUtil.startActivityForResult(UserHomeBgActivity.this, intent, new ActivityResultCallback() {
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
                                        inputStream = UserHomeBgActivity.this.getContentResolver().openInputStream(dataUri);
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
                                    crop(UserHomeBgActivity.this, Uri.fromFile(copyFile), mImageResultCallback);

                                }
                            }
                        }

                        @Override
                        public void onFailure() {
                            ToastUtil.show(com.livestreaming.common.R.string.img_alumb_cancel);
                            if (mImageResultCallback != null) {
                                mImageResultCallback.onFailure();
                            }
                        }
                    });
                }
            });

    public static void forward(Context context, String url, boolean self) {
        Intent intent = new Intent(context, UserHomeBgActivity.class);
        intent.putExtra(Constants.URL, url);
        intent.putExtra("self", self);
        context.startActivity(intent);
    }

    @Override
    protected boolean isStatusBarWhite() {
        return true;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_user_home_bg;
    }

    @Override
    protected void main() {
        Intent intent = getIntent();
        boolean self = intent.getBooleanExtra("self", false);
        if (self) {
            findViewById(R.id.btn_choose).setOnClickListener(this);
            findViewById(R.id.btn_download).setOnClickListener(this);
        } else {
            findViewById(R.id.btn_choose).setVisibility(View.INVISIBLE);
            findViewById(R.id.btn_download).setVisibility(View.INVISIBLE);
        }
        mImg = findViewById(R.id.img);
        mImg.setOnClickListener(this);
        mImgUrl = intent.getStringExtra(Constants.URL);
        ImgLoader.display(mContext, mImgUrl, mImg);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_choose) {
            chooseImage();
        } else if (i == R.id.btn_download) {
            download();
        } else if (i == R.id.img) {
            finish();
        }
    }

    /**
     * 选择图片
     */
    private void chooseImage() {
        DialogUitl.showStringArrayDialog(mContext, new Integer[]{com.livestreaming.common.R.string.alumb, com.livestreaming.common.R.string.camera}, new DialogUitl.StringArrayDialogCallback() {
            @Override
            public void onItemClick(String text, int tag) {
                if (tag == com.livestreaming.common.R.string.camera) {
                    selectedType=1;
                } else if (tag == com.livestreaming.common.R.string.alumb) {
                    selectedType=2;
                }
                if (Build.VERSION.SDK_INT > 33) {
                    String[] permissions = new String[]{
                            Manifest.permission.READ_MEDIA_IMAGES,
                            Manifest.permission.READ_MEDIA_AUDIO,
                            Manifest.permission.READ_MEDIA_VIDEO,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED,
                            Manifest.permission.CAMERA,
                            Manifest.permission.RECORD_AUDIO
                    };
                    requestPermissionLauncherImage.launch(permissions);
                } else if (Build.VERSION.SDK_INT == 33) {
                    String[] permissions = new String[]{
                            Manifest.permission.READ_MEDIA_IMAGES,
                            Manifest.permission.READ_MEDIA_AUDIO,
                            Manifest.permission.READ_MEDIA_VIDEO,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.CAMERA,
                            Manifest.permission.RECORD_AUDIO
                    };
                    requestPermissionLauncherImage.launch(permissions);
                }else{
                    if(selectedType==1){
                        MediaUtil.getImageByCamera(UserHomeBgActivity.this, false, mImageResultCallback);
                    }else{
                        MediaUtil.getImageByAlumb(UserHomeBgActivity.this, false, mImageResultCallback);
                    }
                }
            }
        });
    }

    private void download() {

        if (Build.VERSION.SDK_INT > 33) {
            String[] permissions = new String[]{
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_AUDIO,
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED,
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO
            };
            requestPermissionLauncher.launch(permissions);
        } else if (Build.VERSION.SDK_INT == 33) {
            String[] permissions = new String[]{
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_AUDIO,
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO
            };
            requestPermissionLauncher.launch(permissions);
        } else {
            PermissionUtil.request((AbsActivity) mContext,
                    new PermissionCallback() {
                        @Override
                        public void onAllGranted() {
                            long currentTimeMillis = SystemClock.uptimeMillis();
                            String fileName = StringUtil.generateFileName() + ".png";
                            ContentValues values = new ContentValues();
                            values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                            values.put(MediaStore.MediaColumns.MIME_TYPE, "image/png");
                            values.put(MediaStore.MediaColumns.TITLE, fileName);
                            values.put(MediaStore.MediaColumns.DATE_MODIFIED, currentTimeMillis);
                            values.put(MediaStore.MediaColumns.DATE_ADDED, currentTimeMillis);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM);
                            } else {
                                values.put(MediaStore.MediaColumns.DATA, CommonAppConfig.IMAGE_DOWNLOAD_PATH + fileName);
                            }
                            Uri uri = CommonAppContext.getInstance().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                            new DownloadUtil("save_img").download(uri, mImgUrl, new UriDownloadCallback() {
                                @Override
                                public void onSuccess() {
                                    ToastUtil.show(com.livestreaming.common.R.string.save_success);
                                }
                            });
                        }
                    }, Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
    }

    @Override
    protected void onDestroy() {
        LiveHttpUtil.cancel(LiveHttpConsts.SET_USER_HOME_BG_IMG);
        super.onDestroy();
    }
}
