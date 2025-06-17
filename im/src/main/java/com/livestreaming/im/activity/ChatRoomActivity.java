package com.livestreaming.im.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;

import com.livestreaming.common.Constants;
import com.livestreaming.common.activity.AbsActivity;
import com.livestreaming.common.bean.UserBean;
import com.livestreaming.common.interfaces.ActivityResultCallback;
import com.livestreaming.common.interfaces.ImageResultCallback;
import com.livestreaming.common.interfaces.PermissionCallback;
import com.livestreaming.common.utils.ActivityResultUtil;
import com.livestreaming.common.utils.FloatWindowHelper;
import com.livestreaming.common.utils.KeyBoardUtil;
import com.livestreaming.common.utils.MediaUtil;
import com.livestreaming.common.utils.PermissionUtil;
import com.livestreaming.common.utils.ToastUtil;
import com.livestreaming.common.utils.WordUtil;
import com.livestreaming.im.R;
import com.livestreaming.im.dialog.ChatVoiceInputDialog;
import com.livestreaming.im.interfaces.ChatRoomActionListener;
import com.livestreaming.im.utils.ImMessageUtil;
import com.livestreaming.im.views.ChatRoomViewHolder;

import java.io.File;

/**
 * Created by cxf on 2018/10/24.
 */

public class ChatRoomActivity extends AbsActivity implements KeyBoardUtil.KeyBoardHeightListener {

    public static void forward(Context context, UserBean userBean, boolean following, boolean fromUserHome) {
        Intent intent = new Intent(context, ChatRoomActivity.class);
        intent.putExtra(Constants.USER_BEAN, userBean);
        intent.putExtra(Constants.FOLLOW, following);
        intent.putExtra(Constants.IM_FROM_HOME, fromUserHome);
        context.startActivity(intent);
    }

    private ViewGroup mRoot;
    private ViewGroup mContianer;
    private ChatRoomViewHolder mChatRoomViewHolder;
    private boolean mFromUserHome;
    private KeyBoardUtil mKeyBoardUtil;
    private ImageResultCallback mImageResultCallback = new ImageResultCallback() {
        @Override
        public void beforeCamera() {

        }

        @Override
        public void onSuccess(File file) {
            if (file != null && file.exists() && mChatRoomViewHolder != null) {
                mChatRoomViewHolder.sendImage(file.getAbsolutePath());
            }
        }

        @Override
        public void onFailure() {

        }
    };


    @Override
    protected int getLayoutId() {
        return R.layout.activity_chat_room;
    }
    private int selectedType=0;
    private ActivityResultLauncher<String[]> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permissions -> {
                if (selectedType == 1) {
                    forwardChooseImage();
                }else if (selectedType==2){
                    openVoiceInputDialog();
                }else if(selectedType==3){
                    MediaUtil.callBackForCamImageSelection(ChatRoomActivity.this, false, mImageResultCallback);
                }
            });

    @Override
    protected void main() {
        Intent intent = getIntent();
        UserBean userBean = intent.getParcelableExtra(Constants.USER_BEAN);
        if (userBean == null) {
            return;
        }
        boolean following = intent.getBooleanExtra(Constants.FOLLOW, false);
        mFromUserHome = intent.getBooleanExtra(Constants.IM_FROM_HOME, false);
        mRoot = (ViewGroup) findViewById(R.id.root);
        mContianer = (ViewGroup) findViewById(R.id.container);
        mChatRoomViewHolder = new ChatRoomViewHolder(mContext, mContianer, userBean, following);
        mChatRoomViewHolder.setActionListener(new ChatRoomActionListener() {
            @Override
            public void onCloseClick() {
                superBackPressed();
            }

            @Override
            public void onChooseImageClick() {

                selectedType = 1;
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
                    String[] permissions = new String[]{
                            Manifest.permission.READ_MEDIA_IMAGES,
                            Manifest.permission.READ_MEDIA_AUDIO,
                            Manifest.permission.READ_MEDIA_VIDEO,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.CAMERA,
                            Manifest.permission.RECORD_AUDIO
                    };
                    requestPermissionLauncher.launch(permissions);
                }else{
                    PermissionUtil.request(
                            ChatRoomActivity.this,
                            new PermissionCallback() {
                                @Override
                                public void onAllGranted() {
                                    forwardChooseImage();
                                }
                            },
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    );
                }
            }

            @Override
            public void onCameraClick() {
                selectedType=3;
                if(Build.VERSION.SDK_INT>33){
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
                }else if(Build.VERSION.SDK_INT==33){
                    String[] permissions = new String[]{
                            Manifest.permission.READ_MEDIA_IMAGES,
                            Manifest.permission.READ_MEDIA_AUDIO,
                            Manifest.permission.READ_MEDIA_VIDEO,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.CAMERA,
                            Manifest.permission.RECORD_AUDIO
                    };
                    requestPermissionLauncher.launch(permissions);
                }else{
                    MediaUtil.getImageByCamera(ChatRoomActivity.this, false, mImageResultCallback);
                }
               }

            @Override
            public void onVoiceInputClick() {
                selectedType=2;
                if (!FloatWindowHelper.checkVoice(false)) {
                    return;
                }
                if(Build.VERSION.SDK_INT>33){
                    String[] permissions = new String[]{
                            Manifest.permission.READ_MEDIA_IMAGES,
                            Manifest.permission.READ_MEDIA_AUDIO,
                            Manifest.permission.READ_MEDIA_VIDEO,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED,
                            Manifest.permission.CAMERA,
                            Manifest.permission.RECORD_AUDIO
                    };
                    boolean hasPermission=true;
                    for (int i = 0; i < permissions.length; i++) {
                        if(ContextCompat.checkSelfPermission(mContext,permissions[i])!= PackageManager.PERMISSION_GRANTED){
                            hasPermission=false;
                        }
                    }
                    if(!hasPermission){
                    requestPermissionLauncher.launch(permissions);
                    }
                }else if(Build.VERSION.SDK_INT==33){
                    String[] permissions = new String[]{
                            Manifest.permission.READ_MEDIA_IMAGES,
                            Manifest.permission.READ_MEDIA_AUDIO,
                            Manifest.permission.READ_MEDIA_VIDEO,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.CAMERA,
                            Manifest.permission.RECORD_AUDIO
                    };
                    requestPermissionLauncher.launch(permissions);
                }else{
                    PermissionUtil.request(
                            ChatRoomActivity.this, new PermissionCallback() {
                                @Override
                                public void onAllGranted() {
                                    openVoiceInputDialog();
                                }
                            },
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    );
                }
            }

            @Override
            public void onLocationClick() {
                PermissionUtil.request(ChatRoomActivity.this, new PermissionCallback() {
                    @Override
                    public void onAllGranted() {
                        forwardLocation();
                    }
                }, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION);
            }

            @Override
            public ViewGroup getImageParentView() {
                return mRoot;
            }


        });
        mChatRoomViewHolder.addToParent();
        mChatRoomViewHolder.loadData();
        mKeyBoardUtil = new KeyBoardUtil(mRoot, this);
        ImMessageUtil.getInstance().setOpenChatActivity(true);
    }

    @Override
    public void onKeyBoardHeightChanged(int keyboardHeight) {
        if (mChatRoomViewHolder != null) {
            mChatRoomViewHolder.onKeyBoardHeightChanged(keyboardHeight);
        }
    }

    @Override
    public void onBackPressed() {

        if (mChatRoomViewHolder != null) {
            mChatRoomViewHolder.back();
        } else {
            superBackPressed();
        }
    }

    private void release() {
        if (mKeyBoardUtil != null) {
            mKeyBoardUtil.release();
        }
        mKeyBoardUtil = null;
        if (mChatRoomViewHolder != null) {
            mChatRoomViewHolder.release();
        }
        mChatRoomViewHolder = null;
        ImMessageUtil.getInstance().setOpenChatActivity(false);
    }

    public void superBackPressed() {
        release();
        super.onBackPressed();
    }


    @Override
    protected void onDestroy() {
        release();
        super.onDestroy();
    }


    /**
     * 前往选择图片页面
     */
    private void forwardChooseImage() {
        ActivityResultUtil.startActivityForResult(this, new Intent(mContext, ChatChooseImageActivity.class), new ActivityResultCallback() {
            @Override
            public void onSuccess(Intent intent) {
                if (intent != null) {
                    String imagePath = intent.getStringExtra(Constants.SELECT_IMAGE_PATH);
                    if (mChatRoomViewHolder != null) {
                        mChatRoomViewHolder.sendImage(imagePath);
                    }
                }
            }
        });
//        MediaUtil.getImageByAlumb(this, false, mImageResultCallback);
    }


    /**
     * 前往发送位置页面
     */
    private void forwardLocation() {
        ActivityResultUtil.startActivityForResult(this, new Intent(mContext, LocationActivity.class), new ActivityResultCallback() {
            @Override
            public void onSuccess(Intent intent) {
                if (intent != null) {
                    double lat = intent.getDoubleExtra(Constants.LAT, 0);
                    double lng = intent.getDoubleExtra(Constants.LNG, 0);
                    String address = intent.getStringExtra(Constants.ADDRESS);
                    if (lat > 0 && lng > 0 && !TextUtils.isEmpty(address)) {
                        if (mChatRoomViewHolder != null) {
                            mChatRoomViewHolder.sendLocation(lat, lng, address);
                        }
                    } else {
                        ToastUtil.show(WordUtil.getString(com.livestreaming.common.R.string.im_get_location_failed));
                    }
                }
            }
        });
    }


    /**
     * 打开语音输入窗口
     */
    private void openVoiceInputDialog() {
        ChatVoiceInputDialog fragment = new ChatVoiceInputDialog();
        fragment.setChatRoomViewHolder(mChatRoomViewHolder);
        fragment.show(getSupportFragmentManager(), "ChatVoiceInputDialog");
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (mChatRoomViewHolder != null) {
            mChatRoomViewHolder.onPause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mChatRoomViewHolder != null) {
            mChatRoomViewHolder.onResume();
        }
    }

    public boolean isFromUserHome() {
        return mFromUserHome;
    }
}
