package com.livestreaming.live.views;

import static com.livestreaming.common.Constants.LiveVoiceRoomUserCount;

import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.livestreaming.common.CommonAppConfig;
import com.livestreaming.common.Constants;
import com.livestreaming.common.activity.AbsActivity;
import com.livestreaming.common.event.LocationCityEvent;
import com.livestreaming.common.glide.ImgLoader;
import com.livestreaming.common.http.HttpCallback;
import com.livestreaming.common.interfaces.ActivityResultCallback;
import com.livestreaming.common.interfaces.CommonCallback;
import com.livestreaming.common.interfaces.ImageResultCallback;
import com.livestreaming.common.upload.UploadBean;
import com.livestreaming.common.upload.UploadCallback;
import com.livestreaming.common.upload.UploadStrategy;
import com.livestreaming.common.upload.UploadUtil;
import com.livestreaming.common.utils.DialogUitl;
import com.livestreaming.common.utils.DpUtil;
import com.livestreaming.common.utils.L;
import com.livestreaming.common.utils.LocationUtil;
import com.livestreaming.common.utils.MediaUtil;
import com.livestreaming.common.utils.ScreenDimenUtil;
import com.livestreaming.common.utils.ToastUtil;
import com.livestreaming.common.utils.WordUtil;
import com.livestreaming.live.R;
import com.livestreaming.live.activity.LiveAnchorActivity;
import com.livestreaming.live.adapter.LiveReadyShareAdapter;
import com.livestreaming.live.bean.LiveRoomTypeBean;
import com.livestreaming.live.http.LiveHttpConsts;
import com.livestreaming.live.http.LiveHttpUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by cxf on 2018/10/7.
 * 开播前准备
 */

public class LiveReadyChatRoomViewHolder extends LiveReadyViewHolder implements View.OnClickListener {

    private ImageView mAvatar;
    private TextView mCoverText;
    private EditText mEditTitle;
    private RecyclerView mLiveShareRecyclerView;
    private LiveReadyShareAdapter mLiveShareAdapter;
    private File mAvatarFile;
    private TextView mCity;
    private ImageView mLocationImg;
    private int mLiveClassID;//直播频道id
    private int mLiveType;//房间类型
    private int mLiveTypeVal;//门票收费金额
    private ImageView room_4, room_8, room_12;
    private String mLivePwd = "";//房间密码，
    private int mLiveTimeCoin;//计时收费金额
    private ActivityResultCallback mActivityResultCallback;
    private CommonCallback<LiveRoomTypeBean> mLiveTypeCallback;
    private boolean mOpenLocation = true;
    private int mLiveSdk;
    private int haveStore;
    private String mForbidLiveTip;//直播间封禁提示
    private boolean mIsCreateRoom;
    private View mBtnVoiceType;
    private View mBtnVideoType;
    private ImageView mIconVoiceType;
    private ImageView mIconVideoType;
    private int mChatRoomType = Constants.CHAT_ROOM_TYPE_VOICE;
    private ImageView room_16;


    public LiveReadyChatRoomViewHolder(Context context, ViewGroup parentView, int liveSdk, int haveStore, String forbidLiveTip) {
        super(context, parentView, liveSdk, haveStore, forbidLiveTip, false);
        View group_1 = findViewById(R.id.group_1);
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) group_1.getLayoutParams();
        params.topMargin = ScreenDimenUtil.getInstance().getStatusBarHeight() + DpUtil.dp2px(5);
        group_1.requestLayout();
    }

    @Override
    protected void processArguments(Object... args) {
        if (args.length > 0) {
            mLiveSdk = (int) args[0];
        }
        if (args.length > 1) {
            haveStore = (int) args[1];
        }
        if (args.length > 2) {
            mForbidLiveTip = (String) args[2];
        }

    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_live_ready_voice_0;
    }

    @Override
    public void init() {
        mBtnVoiceType = findViewById(R.id.btn_room_type_voice);
        mBtnVideoType = findViewById(R.id.btn_room_type_video);
        mIconVoiceType = findViewById(R.id.icon_room_type_voice);
        mIconVideoType = findViewById(R.id.icon_room_type_video);
        room_4 = (ImageView) findViewById(R.id.room_4);
        room_8 = (ImageView) findViewById(R.id.room_8);
        room_12 = (ImageView) findViewById(R.id.room_12);
        room_16 = (ImageView) findViewById(R.id.room_16);
        LiveVoiceRoomUserCount = 16;

        room_4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LiveVoiceRoomUserCount = 4;
                setRoomNumberSelected(4);
            }
        });
        room_8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LiveVoiceRoomUserCount = 8;
                setRoomNumberSelected(8);
            }
        });
        room_12.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LiveVoiceRoomUserCount = 12;
                setRoomNumberSelected(12);
            }
        });
        room_16.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LiveVoiceRoomUserCount = 16;
                setRoomNumberSelected(16);
            }
        });
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int i = v.getId();
                if (i == R.id.btn_room_type_voice) {
                    setChatRoomType(Constants.CHAT_ROOM_TYPE_VOICE);
                    findViewById(R.id.container_choose).setVisibility(View.VISIBLE);
                } else if (i == R.id.btn_room_type_video) {
                    LiveVoiceRoomUserCount=9;
                    setChatRoomType(Constants.CHAT_ROOM_TYPE_VIDEO);
                    findViewById(R.id.container_choose).setVisibility(View.INVISIBLE);
                }
            }
        };
        mBtnVoiceType.setOnClickListener(onClickListener);
        mBtnVideoType.setOnClickListener(onClickListener);
        mAvatar = (ImageView) findViewById(R.id.avatar);
        mCoverText = (TextView) findViewById(R.id.cover_text);
        mEditTitle = (EditText) findViewById(R.id.edit_title);
        mCity = (TextView) findViewById(R.id.city);
        mCity.setText(CommonAppConfig.getInstance().getCity());
        mLocationImg = (ImageView) findViewById(R.id.location_img);
        findViewById(R.id.btn_locaiton).setOnClickListener(this);
        mOpenLocation = true;

        mLiveShareRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mLiveShareRecyclerView.setHasFixedSize(true);
        mLiveShareRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
        mLiveShareAdapter = new LiveReadyShareAdapter(mContext);
        mLiveShareRecyclerView.setAdapter(mLiveShareAdapter);
        findViewById(R.id.avatar_group).setOnClickListener(this);
        findViewById(R.id.btn_close).setOnClickListener(this);
        findViewById(R.id.btn_start_live).setOnClickListener(this);

        TextView tvForbidTip = findViewById(R.id.forbid_tip);
        if (tvForbidTip != null) {
            tvForbidTip.setText(mForbidLiveTip);
        }
        if (CommonAppConfig.getInstance().getLat() == 0 || CommonAppConfig.getInstance().getLng() == 0) {
            EventBus.getDefault().register(this);
            if (((AbsActivity) mContext).hasLocationPermission()) {
                LocationUtil.getInstance().startLocation();
            } else {
                ((AbsActivity) mContext).checkLocationPermission(new Runnable() {
                    @Override
                    public void run() {
                        LocationUtil.getInstance().startLocation();
                    }
                });
            }
        }
        setRoomNumberSelected(16);
    }

    private void setRoomNumberSelected(int i) {
        int glopal = ContextCompat.getColor(mContext, com.livestreaming.common.R.color.global);
        int grey = ContextCompat.getColor(mContext, com.livestreaming.common.R.color.gray);
        if (i == 4) {
            room_4.setColorFilter(glopal);
            room_8.setColorFilter(grey);
            room_12.setColorFilter(grey);

            room_16.setColorFilter(grey);
        } else if (i == 8) {

            room_8.setColorFilter(glopal);
            room_4.setColorFilter(grey);
            room_12.setColorFilter(grey);

            room_16.setColorFilter(grey);
        } else if (i == 12) {
            room_12.setColorFilter(glopal);
            room_8.setColorFilter(grey);
            room_4.setColorFilter(grey);
            room_16.setColorFilter(grey);
        } else if (i == 16) {
            room_16.setColorFilter(glopal);
            room_12.setColorFilter(grey);
            room_8.setColorFilter(grey);
            room_4.setColorFilter(grey);
        }

    }

    @Override
    public void onClick(View v) {
        if (!canClick()) {
            return;
        }
        int i = v.getId();
        if (i == R.id.avatar_group) {
            setAvatar();

        } else if (i == R.id.btn_close) {
            close();

        } else if (i == R.id.btn_start_live) {
            startLive();

        } else if (i == R.id.btn_locaiton) {
            switchLocation();
        }
    }

    private void setChatRoomType(int chatRoomType) {
        if (mChatRoomType != chatRoomType) {
            mChatRoomType = chatRoomType;
            if (mChatRoomType == Constants.CHAT_ROOM_TYPE_VOICE) {
                if (mBtnVoiceType != null) {
                    mBtnVoiceType.setBackgroundResource(R.drawable.bg_live_room_type_1);
                }
                if (mBtnVideoType != null) {
                    mBtnVideoType.setBackgroundResource(R.drawable.bg_live_room_type_0);
                }
                if (mIconVoiceType != null) {
                    mIconVoiceType.setImageResource(R.mipmap.icon_live_room_voice_1);
                }
                if (mIconVideoType != null) {
                    mIconVideoType.setImageResource(R.mipmap.icon_live_room_video_0);
                }
            } else {
                if (mBtnVoiceType != null) {
                    mBtnVoiceType.setBackgroundResource(R.drawable.bg_live_room_type_0);
                }
                if (mBtnVideoType != null) {
                    mBtnVideoType.setBackgroundResource(R.drawable.bg_live_room_type_1);
                }
                if (mIconVoiceType != null) {
                    mIconVoiceType.setImageResource(R.mipmap.icon_live_room_voice_0);
                }
                if (mIconVideoType != null) {
                    mIconVideoType.setImageResource(R.mipmap.icon_live_room_video_1);
                }
            }
        }
    }


    /**
     * 打开 关闭位置
     */
    private void switchLocation() {
        if (mOpenLocation) {
            new DialogUitl.Builder(mContext)
                    .setContent(WordUtil.getString(com.livestreaming.common.R.string.live_location_close_3))
                    .setCancelable(true)
                    .setConfrimString(WordUtil.getString(com.livestreaming.common.R.string.live_location_close_2))
                    .setClickCallback(new DialogUitl.SimpleCallback() {

                        @Override
                        public void onConfirmClick(Dialog dialog, String content) {
                            toggleLocation();
                        }
                    })
                    .build()
                    .show();
        } else {
            toggleLocation();
        }
    }

    private void toggleLocation() {
        mOpenLocation = !mOpenLocation;
        if (mLocationImg != null) {
            mLocationImg.setImageResource(mOpenLocation ? R.mipmap.icon_live_ready_location_1 : R.mipmap.icon_live_ready_location_0);
        }
        if (mCity != null) {
            mCity.setText(mOpenLocation ? CommonAppConfig.getInstance().getCity() : WordUtil.getString(com.livestreaming.common.R.string.live_location_close));
        }
    }

    /**
     * 设置头像
     */
    private void setAvatar() {
        final ImageResultCallback imageResultCallback = new ImageResultCallback() {

            @Override
            public void beforeCamera() {
                ((LiveAnchorActivity) mContext).beforeCamera();
            }

            @Override
            public void onSuccess(File file) {
                if (file != null) {
                    ImgLoader.display(mContext, file, mAvatar);
                    if (mAvatarFile == null) {
                        mCoverText.setText(WordUtil.getString(com.livestreaming.common.R.string.live_cover_2));
                        mCoverText.setBackground(ContextCompat.getDrawable(mContext, R.drawable.bg_live_cover));
                    }
                    mAvatarFile = file;
                }
            }

            @Override
            public void onFailure() {
            }
        };
        if (mLiveSdk == Constants.LIVE_SDK_TX) {
            MediaUtil.getImageByAlumb((AbsActivity) mContext, imageResultCallback);
        } else {
            DialogUitl.showStringArrayDialog(mContext, new Integer[]{
                    com.livestreaming.common.R.string.alumb, com.livestreaming.common.R.string.camera}, new DialogUitl.StringArrayDialogCallback() {
                @Override
                public void onItemClick(String text, int tag) {
                    if (tag == com.livestreaming.common.R.string.camera) {
                        MediaUtil.getImageByCamera((AbsActivity) mContext, imageResultCallback);
                    } else {
                        MediaUtil.getImageByAlumb((AbsActivity) mContext, imageResultCallback);
                    }
                }
            });
        }
    }


    /**
     * 关闭
     */
    private void close() {
        ((LiveAnchorActivity) mContext).onBackPressed();
    }


    public void hide() {
        if (mContentView != null && mContentView.getVisibility() == View.VISIBLE) {
            mContentView.setVisibility(View.INVISIBLE);
        }
    }


    public void show() {
        if (mContentView != null && mContentView.getVisibility() != View.VISIBLE) {
            mContentView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 点击开始直播按钮
     */
    private void startLive() {

        if (mLiveShareAdapter != null) {
            String type = mLiveShareAdapter.getShareType();
            if (!TextUtils.isEmpty(type)) {
//                ((LiveActivity) mContext).shareLive(type, mEditTitle.getText().toString().trim(), new MobCallback() {
//                    @Override
//                    public void onSuccess(Object data) {
//                        LiveHttpUtil.dailyTaskShareLive();
//                    }
//
//                    @Override
//                    public void onError() {
//
//                    }
//
//                    @Override
//                    public void onCancel() {
//
//                    }
//
//                    @Override
//                    public void onFinish() {
//
//                    }
//                });
                createRoom();
            } else {
                createRoom();
            }
        } else {
            createRoom();
        }
    }

    /**
     * 请求创建直播间接口，开始直播
     */
    private void createRoom() {
        final String title = mEditTitle.getText().toString().trim();
        final int finalShop = 0;
        final String typeVal = mLiveType == Constants.LIVE_TYPE_PWD ? mLivePwd : String.valueOf(mLiveTypeVal);
        if (mAvatarFile != null && mAvatarFile.exists()) {
            UploadUtil.startUpload(new CommonCallback<UploadStrategy>() {
                @Override
                public void callback(UploadStrategy strategy) {
                    List<UploadBean> list = new ArrayList<>();
                    list.add(new UploadBean(mAvatarFile, UploadBean.IMG));
                    strategy.upload(list, false, new UploadCallback() {
                        @Override
                        public void onFinish(List<UploadBean> list, boolean success) {
                            if (success) {
                                if (!mIsCreateRoom) {
                                    mIsCreateRoom = true;
                                    LiveHttpUtil.createRoom(title, mLiveClassID, mLiveType, typeVal, finalShop, list.get(0).getRemoteFileName(), mOpenLocation, 1, mChatRoomType, new HttpCallback() {
                                        @Override
                                        public void onSuccess(int code, String msg, String[] info) {
                                            if (code == 0 && info.length > 0) {
                                                L.e("开播", "createRoom------->" + info[0]);
                                                ((LiveAnchorActivity) mContext).setChatRoomType(mChatRoomType);
                                                ((LiveAnchorActivity) mContext).startLiveSuccess(info[0], mLiveType, mLiveTypeVal, title);
                                                ((LiveAnchorActivity) mContext).openShop(finalShop == 1);
                                            } else {
                                                ToastUtil.show(msg);
                                            }
                                        }
                                    });
                                }
                            }
                        }
                    });
                }
            });
        } else {
            if (!mIsCreateRoom) {
                mIsCreateRoom = true;
                LiveHttpUtil.createRoom(title, mLiveClassID, mLiveType, typeVal, finalShop, null, mOpenLocation, 1, mChatRoomType, new HttpCallback() {
                    @Override
                    public void onSuccess(int code, String msg, String[] info) {
                        if (code == 0 && info.length > 0) {
                            L.e("开播", "createRoom------->" + info[0]);
                            ((LiveAnchorActivity) mContext).setChatRoomType(mChatRoomType);
                            ((LiveAnchorActivity) mContext).startLiveSuccess(info[0], mLiveType, mLiveTypeVal, title);
                            ((LiveAnchorActivity) mContext).openShop(finalShop == 1);
                        } else {
                            ToastUtil.show(msg);
                        }
                    }
                });
            }
        }
    }

    public void release() {
        mActivityResultCallback = null;
        mLiveTypeCallback = null;
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @Override
    public void onDestroy() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        UploadUtil.cancelUpload();
        LiveHttpUtil.cancel(LiveHttpConsts.CREATE_ROOM);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLocationCityEvent(LocationCityEvent e) {
        if (mOpenLocation) {
            if (mCity != null) {
                mCity.setText(e.getCity());
            }
        }
    }
}
