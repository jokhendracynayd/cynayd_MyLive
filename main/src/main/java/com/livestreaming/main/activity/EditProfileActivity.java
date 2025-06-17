package com.livestreaming.main.activity;

import static com.livestreaming.common.utils.MediaUtil.FILE_PROVIDER;
import static com.livestreaming.common.utils.MediaUtil.crop;
import static com.livestreaming.common.utils.MediaUtil.getNewFile;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.FileProvider;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.livestreaming.common.CommonAppConfig;
import com.livestreaming.common.Constants;
import com.livestreaming.common.activity.AbsActivity;
import com.livestreaming.common.bean.UserBean;
import com.livestreaming.common.event.UpdateFieldEvent;
import com.livestreaming.common.glide.ImgLoader;
import com.livestreaming.common.http.HttpCallback;
import com.livestreaming.common.interfaces.ActivityResultCallback;
import com.livestreaming.common.interfaces.CommonCallback;
import com.livestreaming.common.interfaces.ImageResultCallback;
import com.livestreaming.common.upload.UploadBean;
import com.livestreaming.common.upload.UploadCallback;
import com.livestreaming.common.upload.UploadStrategy;
import com.livestreaming.common.upload.UploadUtil;
import com.livestreaming.common.utils.ActivityResultUtil;
import com.livestreaming.common.utils.DialogUitl;
import com.livestreaming.common.utils.MD5Util;
import com.livestreaming.common.utils.MediaUtil;
import com.livestreaming.common.utils.ToastUtil;
import com.livestreaming.common.utils.WordUtil;
import com.livestreaming.live.http.LiveHttpUtil;
import com.livestreaming.main.R;
import com.livestreaming.main.http.MainHttpConsts;
import com.livestreaming.main.http.MainHttpUtil;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by cxf on 2018/9/29.
 * 我的 编辑资料
 */

public class EditProfileActivity extends AbsActivity {
    int selectedType = 0;

    private ImageView mAvatar;
    private TextView mName;
    private TextView mSign;
    private TextView mBirthday;
    private TextView mSex;
    private TextView mCity;
    private UserBean mUserBean;
    private String mProvinceVal;
    private String mCityVal;
    private String mZoneVal;
    private boolean isFromFirst = false;

    private int editedInfo = 0;
    private ImageResultCallback mImageResultCallback = new ImageResultCallback() {
        @Override
        public void beforeCamera() {

        }

        @Override
        public void onSuccess(final File file) {
            if (file != null) {
                ImgLoader.display(mContext, file, mAvatar);
                UploadUtil.startUpload(new CommonCallback<UploadStrategy>() {
                    @Override
                    public void callback(UploadStrategy strategy) {
                        List<UploadBean> list = new ArrayList<>();
                        list.add(new UploadBean(file, UploadBean.IMG));
                        strategy.upload(list, true, new UploadCallback() {
                            @Override
                            public void onFinish(List<UploadBean> list, boolean success) {
                                if (success) {
                                    if (isFromFirst) {
                                        editedInfo++;
                                        updateWhenUserEditInfo();
                                    }
                                    MainHttpUtil.updateAvatar(list.get(0).getRemoteFileName(), new HttpCallback() {
                                        @Override
                                        public void onSuccess(int code, String msg, String[] info) {
                                            if (code == 0 && info.length > 0) {
                                                ToastUtil.show(com.livestreaming.common.R.string.edit_profile_update_avatar_success);
                                                UserBean bean = CommonAppConfig.getInstance().getUserBean();
                                                if (bean != null) {
                                                    JSONObject obj = JSON.parseObject(info[0]);
                                                    bean.setAvatar(obj.getString("avatar"));
                                                    bean.setAvatarThumb(obj.getString("avatarThumb"));
                                                }
                                                EventBus.getDefault().post(new UpdateFieldEvent());
                                            }
                                        }
                                    });
                                }
                            }
                        });
                    }
                });

            }
        }

        @Override
        public void onFailure() {
        }
    };
    private boolean canBack = true;

    private void updateWhenUserEditInfo() {
        if (editedInfo >= 2) {
            canBack = true;
            findViewById(R.id.done_first_editing).setVisibility(View.VISIBLE);
            findViewById(R.id.done_first_editing).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onDone();
                }
            });
        }
    }

    private void onDone() {
        LiveHttpUtil.updateUserDataDone();
        onBackPressed();
    }

    private ActivityResultLauncher<String[]> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permissions -> {
                if (selectedType == 1) {
                    if (mImageResultCallback != null) {
                        mImageResultCallback.beforeCamera();
                    }
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    final File cameraResult = getNewFile();
                    Uri uri = null;
                    if (Build.VERSION.SDK_INT >= 24) {
                        uri = FileProvider.getUriForFile(EditProfileActivity.this, FILE_PROVIDER, cameraResult);
//                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    } else {
                        uri = Uri.fromFile(cameraResult);
                    }
                    final Uri finalUri = uri;
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                    //开始拍照
                    ActivityResultUtil.startActivityForResult(EditProfileActivity.this, intent, new ActivityResultCallback() {
                        @Override
                        public void onSuccess(Intent intent) {
                            MediaUtil.crop(EditProfileActivity.this, finalUri, mImageResultCallback);
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
                    MediaUtil.getImageByAlumb(EditProfileActivity.this, mImageResultCallback);
                    Intent intent = new Intent();
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("image/*");
                    if (Build.VERSION.SDK_INT < 19) {
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                    } else {
                        intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                    }
                    ActivityResultUtil.startActivityForResult(EditProfileActivity.this, intent, new ActivityResultCallback() {
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
                                        inputStream = EditProfileActivity.this.getContentResolver().openInputStream(dataUri);
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
                                    crop(EditProfileActivity.this, Uri.fromFile(copyFile), mImageResultCallback);

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

    @Override
    protected int getLayoutId() {
        return R.layout.activity_edit_profile;
    }

    @Override
    protected void main() {
        setTitle(WordUtil.getString(com.livestreaming.common.R.string.edit_profile));
        isFromFirst = getIntent().getBooleanExtra("isFromFirst", false);
        if (isFromFirst) {
            canBack = false;
            findViewById(R.id.title_view).setVisibility(View.GONE);
            findViewById(R.id.v1).setVisibility(View.VISIBLE);
        }
        mAvatar = (ImageView) findViewById(R.id.avatar);
        mName = (TextView) findViewById(R.id.name);
        mSign = (TextView) findViewById(R.id.sign);
        mBirthday = (TextView) findViewById(R.id.birthday);
        mSex = (TextView) findViewById(R.id.sex);
        mCity = (TextView) findViewById(R.id.city);
        findViewById(R.id.btn_name).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                forwardName();
            }
        });
        findViewById(R.id.btn_sign).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                forwardSign();
            }
        });
        findViewById(R.id.btn_sex).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                forwardSex();
            }
        });
        findViewById(R.id.btn_birthday).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editBirthday();
            }
        });
        findViewById(R.id.btn_avatar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editAvatar();
            }
        });
        mUserBean = CommonAppConfig.getInstance().getUserBean();
        if (mUserBean != null) {
            showData(mUserBean);
        } else {
            MainHttpUtil.getBaseInfo(new CommonCallback<UserBean>() {
                @Override
                public void callback(UserBean u) {
                    mUserBean = u;
                    showData(u);
                }
            });
        }
    }

    private void editAvatar() {
        DialogUitl.showStringArrayDialog(mContext, new Integer[]{
                com.livestreaming.common.R.string.alumb, com.livestreaming.common.R.string.camera}, new DialogUitl.StringArrayDialogCallback() {
            @Override
            public void onItemClick(String text, int tag) {
                if (tag == com.livestreaming.common.R.string.camera) {
                    selectedType = 1;
                } else {
                    selectedType = 2;
                }
                if (selectedType == 1) {
                    MediaUtil.getImageByCamera(EditProfileActivity.this, mImageResultCallback);
                } else {
                    MediaUtil.getImageByAlumb(EditProfileActivity.this, mImageResultCallback);
                }
            }
        });
    }

    private void forwardName() {
        if (mUserBean == null) {
            return;
        }
        Intent intent = new Intent(mContext, EditNameActivity.class);
        intent.putExtra(Constants.NICK_NAME, mUserBean.getUserNiceName());
        ActivityResultUtil.startActivityForResult(this, intent, new ActivityResultCallback() {
            @Override
            public void onSuccess(Intent intent) {
                if (intent != null) {
                    String name = intent.getStringExtra(Constants.NICK_NAME);
                    mUserBean.setUserNiceName(name);
                    mName.setText(name);
                    if (isFromFirst) {
                        editedInfo++;
                        updateWhenUserEditInfo();
                    }
                    EventBus.getDefault().post(new UpdateFieldEvent());
                }
            }
        });
    }


    private void forwardSign() {
        if (mUserBean == null) {
            return;
        }
        Intent intent = new Intent(mContext, EditSignActivity.class);
        intent.putExtra(Constants.SIGN, mUserBean.getSignature());
        ActivityResultUtil.startActivityForResult(this, intent, new ActivityResultCallback() {
            @Override
            public void onSuccess(Intent intent) {
                if (intent != null) {
                    String sign = intent.getStringExtra(Constants.SIGN);
                    mUserBean.setSignature(sign);
                    mSign.setText(sign);
                    EventBus.getDefault().post(new UpdateFieldEvent());
                }
            }

        });
    }

    private void editBirthday() {
        if (mUserBean == null) {
            return;
        }
        DialogUitl.showDatePickerDialog(mContext, new DialogUitl.DataPickerCallback() {
            @Override
            public void onConfirmClick(final String date) {
                MainHttpUtil.updateFields("{\"birthday\":\"" + date + "\"}", new HttpCallback() {
                    @Override
                    public void onSuccess(int code, String msg, String[] info) {
                        if (code == 0) {
                            if (info.length > 0) {
                                ToastUtil.show(JSON.parseObject(info[0]).getString("msg"));
                                mUserBean.setBirthday(date);
                                mBirthday.setText(date);
                                EventBus.getDefault().post(new UpdateFieldEvent());
                            }
                        } else {
                            ToastUtil.show(msg);
                        }
                    }
                });
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (canBack) {
            super.onBackPressed();
        } else {
            ToastUtil.show(R.string.please_edit_name_and_image);
        }
    }

    private void forwardSex() {
        if (mUserBean == null) {
            return;
        }
        Intent intent = new Intent(mContext, EditSexActivity.class);
        intent.putExtra(Constants.SEX, mUserBean.getSex());
        ActivityResultUtil.startActivityForResult(this, intent, new ActivityResultCallback() {
            @Override
            public void onSuccess(Intent intent) {
                if (intent != null) {
                    int sex = intent.getIntExtra(Constants.SEX, 0);
                    if (sex == 1) {
                        mSex.setText(com.livestreaming.common.R.string.sex_male);
                        mUserBean.setSex(sex);
                    } else if (sex == 2) {
                        mSex.setText(com.livestreaming.common.R.string.sex_female);
                        mUserBean.setSex(sex);
                    }
                    EventBus.getDefault().post(new UpdateFieldEvent());
                }
            }

        });
    }


    @Override
    protected void onDestroy() {
        if (isFromFirst) {
            MainActivity.forward(this);
        }
        UploadUtil.cancelUpload();
        MainHttpUtil.cancel(MainHttpConsts.UPDATE_AVATAR);
        MainHttpUtil.cancel(MainHttpConsts.UPDATE_FIELDS);
        super.onDestroy();
    }

    private void showData(UserBean u) {
        ImgLoader.displayAvatar(mContext, u.getAvatar(), mAvatar);
        mName.setText(u.getUserNiceName());
        mSign.setText(u.getSignature());
        mBirthday.setText(u.getBirthday());
        mSex.setText(u.getSex() == 1 ? com.livestreaming.common.R.string.sex_male : com.livestreaming.common.R.string.sex_female);
        mCity.setText(u.getLocation());
    }


    /**
     * 选择城市
     */

}
