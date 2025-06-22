package com.livestreaming.live.dialog;

import static com.livestreaming.common.glide.ImgLoader.loadSvga;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.livestreaming.common.CommonAppConfig;
import com.livestreaming.common.Constants;
import com.livestreaming.common.activity.AbsActivity;
import com.livestreaming.common.bean.LevelBean;
import com.livestreaming.common.bean.UserBean;
import com.livestreaming.common.dialog.AbsDialogFragment;
import com.livestreaming.common.glide.ImgLoader;
import com.livestreaming.common.http.CommonHttpUtil;
import com.livestreaming.common.http.HttpCallback;
import com.livestreaming.common.interfaces.CommonCallback;
import com.livestreaming.common.utils.CommonIconUtil;
import com.livestreaming.common.utils.DialogUitl;
import com.livestreaming.common.utils.FloatWindowHelper;
import com.livestreaming.common.utils.LanguageUtil;
import com.livestreaming.common.utils.RouteUtil;
import com.livestreaming.common.utils.StringUtil;
import com.livestreaming.common.utils.ToastUtil;
import com.livestreaming.common.utils.WordUtil;
import com.livestreaming.live.R;
import com.livestreaming.live.activity.LiveActivity;
import com.livestreaming.live.activity.LiveAudienceActivity;
import com.livestreaming.live.activity.LiveReportActivity;
import com.livestreaming.live.bean.ImpressBean;
import com.livestreaming.live.bean.LiveBean;
import com.livestreaming.live.bean.SuperForbidTimeBean;
import com.livestreaming.live.custom.MyTextView;
import com.livestreaming.live.event.LiveRoomChangeEvent;
import com.livestreaming.live.http.LiveHttpConsts;
import com.livestreaming.live.http.LiveHttpUtil;
import com.livestreaming.live.presenter.UserHomeCheckLivePresenter;
import com.livestreaming.live.utils.LiveTextRender;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import pl.droidsonroids.gif.GifImageView;

/**
 * Created by cxf on 2018/10/15.
 * 直播间个人资料弹窗
 */

public class LiveUserDialogFragment extends AbsDialogFragment implements View.OnClickListener {

    private static final int TYPE_AUD_AUD = 1;//观众点别的观众
    private static final int TYPE_ANC_AUD = 2;//主播点观众
    private static final int TYPE_AUD_ANC = 3;//观众点主播
    private static final int TYPE_AUD_SELF = 4;//观众点自己
    private static final int TYPE_ANC_SELF = 5;//主播点自己

    private static final int SETTING_ACTION_SELF = 0;//设置 自己点自己
    private static final int SETTING_ACTION_AUD = 30;//设置 普通观众点普通观众 或所有人点超管
    private static final int SETTING_ACTION_ADM = 40;//设置 房间管理员点普通观众
    private static final int SETTING_ACTION_SUP = 60;//设置 超管点主播
    private static final int SETTING_ACTION_ANC_AUD = 501;//设置 主播点普通观众
    private static final int SETTING_ACTION_ANC_ADM = 502;//设置 主播点房间管理员
    public boolean isAnchor = false;
    private ViewGroup mBottomContainer;
    public WhenUserpClick whenUserpClick;
    private ImageView mAvatar;

    private ImageView mFrame;
    private ImageView mLevelAnchor;
    private ImageView mLevel;
    private TextView mLevelText;
    private TextView mLevelAnchorText;
    private ImageView mSex;
    private TextView mName;
    private TextView mID;
    private TextView mCity;
    private LinearLayout mImpressGroup;
    private TextView mFollow;
    private TextView mSign;
    private TextView mFans;
    private TextView mConsume;//消费
    private TextView mVotes;//收入
    private TextView mConsumeTip;
    private TextView mVotesTip;
    
    // Agency Info Views
    private LinearLayout mAgencyInfoContainer;
    private ImageView mAgencyProfilePic;
    private TextView mAgencyName;
    
    private String mLiveUid;
    private boolean isFromAudience = false;

    private UserHomeCheckLivePresenter mCheckLivePresenter;
    private String mStream;
    private String mToUid;
    private TextView mFollowText;
    private ImageView mFollowImage;
    private int mType;
    private int mAction;
    private String mToName;//对方的名字
    private UserBean mUserBean;
    private boolean mFollowing;
    private Drawable mFollowDrawable;
    private Drawable mUnFollowDrawable;
    private GifImageView btn_live;

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_live_user;
    }

    @Override
    protected int getDialogStyle() {
        return com.livestreaming.common.R.style.dialog2;
    }

    @Override
    protected boolean canCancel() {
        return true;
    }

    @Override
    protected void setWindowAttributes(Window window) {
        WindowManager.LayoutParams params = window.getAttributes();
        window.setWindowAnimations(com.livestreaming.common.R.style.bottomToTopAnim);
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.BOTTOM;
        window.setAttributes(params);
    }

    int iii = 0;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle == null) {
            return;
        }
        mLiveUid = bundle.getString(Constants.LIVE_UID);
        mToUid = bundle.getString(Constants.TO_UID);
        iii = bundle.getInt("iii", 0);
        isFromAudience = false;
        if (TextUtils.isEmpty(mLiveUid) || TextUtils.isEmpty(mToUid)) {
            return;
        }
        mStream = bundle.getString(Constants.STREAM);
        mBottomContainer = mRootView.findViewById(R.id.bottom_container);
        mAvatar = (ImageView) mRootView.findViewById(R.id.avatar);
        mFrame = (ImageView) mRootView.findViewById(R.id.frame);
        mAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                whenUserpClick.onClick(mToUid);
                dismiss();
            }
        });
        mLevelAnchor = (ImageView) mRootView.findViewById(R.id.anchor_level);
        mLevel = (ImageView) mRootView.findViewById(R.id.level);
        mLevelText = mRootView.findViewById(R.id.level_text);
        mLevelAnchorText = mRootView.findViewById(R.id.level_anchor_text);
        mSex = (ImageView) mRootView.findViewById(R.id.sex);
        mName = (TextView) mRootView.findViewById(R.id.name);
        mID = (TextView) mRootView.findViewById(R.id.id_val);
        mCity = (TextView) mRootView.findViewById(R.id.city);
        mSign = mRootView.findViewById(R.id.sign);
        mImpressGroup = (LinearLayout) mRootView.findViewById(R.id.impress_group);
        mFollow = (TextView) mRootView.findViewById(R.id.follow);
        mFans = (TextView) mRootView.findViewById(R.id.fans);
        mConsume = (TextView) mRootView.findViewById(R.id.consume);
        mVotes = (TextView) mRootView.findViewById(R.id.votes);
        mConsumeTip = (TextView) mRootView.findViewById(R.id.consume_tip);
        mVotesTip = (TextView) mRootView.findViewById(R.id.votes_tip);
        // mRootView.findViewById(R.id.btn_close).setOnClickListener(this);
        
        // Initialize agency info views
        mAgencyInfoContainer = (LinearLayout) mRootView.findViewById(R.id.agency_info_container);
        mAgencyProfilePic = (ImageView) mRootView.findViewById(R.id.agency_profile_pic);
        mAgencyName = (TextView) mRootView.findViewById(R.id.agency_name);
        
        // Setup copy ID button
        View btnCopyId = mRootView.findViewById(R.id.btn_copy_id);
        if (btnCopyId != null) {
            btnCopyId.setOnClickListener(this);
        }
        
        getType();
        if (mType == TYPE_AUD_ANC || mType == TYPE_ANC_SELF) {
            if (mImpressGroup.getVisibility() != View.VISIBLE) {
                mImpressGroup.setVisibility(View.VISIBLE);
            }
        }

        LayoutInflater inflater = LayoutInflater.from(mContext);
        View bottomView = null;
        if (mType == TYPE_AUD_ANC) {
            bottomView = inflater.inflate(R.layout.dialog_live_user_bottom_1, mBottomContainer, false);
        } else if (mType == TYPE_AUD_AUD) {
            bottomView = inflater.inflate(R.layout.dialog_live_user_bottom_1, mBottomContainer, false);
        } else if (mType == TYPE_ANC_AUD) {
            bottomView = inflater.inflate(R.layout.dialog_live_user_bottom_2, mBottomContainer, false);
        } else if (mType == TYPE_AUD_SELF) {
            bottomView = inflater.inflate(R.layout.dialog_live_user_bottom_3, mBottomContainer, false);
        }
        if (bottomView != null) {
            mFollowDrawable = ContextCompat.getDrawable(mContext, com.livestreaming.common.R.mipmap.icon_user_home_follow_1);
            mUnFollowDrawable = ContextCompat.getDrawable(mContext, com.livestreaming.common.R.mipmap.icon_user_home_follow_0);
            mBottomContainer.addView(bottomView);
            mFollowText = bottomView.findViewById(R.id.follow_text);
            mFollowImage = (ImageView) findViewById(R.id.follow_img);
            btn_live = findViewById(R.id.btn_live);
            btn_live.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    forwardLiveRoom();
                }
            });
            View btnFollow = bottomView.findViewById(R.id.btn_follow);
            if (btnFollow != null) {
                btnFollow.setOnClickListener(this);
            }
            View btnPriMsg = bottomView.findViewById(R.id.btn_pri_msg);
            if (btnPriMsg != null) {
                if (CommonAppConfig.getInstance().isPrivateMsgSwitchOpen()) {
                    btnPriMsg.setOnClickListener(this);
                } else {
                    btnPriMsg.setVisibility(View.GONE);
                }
            }
            View btnHomePage = bottomView.findViewById(R.id.btn_home_page);
            if (btnHomePage != null) {
                btnHomePage.setOnClickListener(this);
            }
        }
        View btnAt = findViewById(R.id.btn_at);
        if (btnAt != null) {
            btnAt.setOnClickListener(this);
        }
        loadData();
    }

    private void forwardLiveRoom() {
        LiveHttpUtil.getLiveInfo(mToUid, new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0 && info.length > 0) {
                    LiveBean liveBean = JSON.parseObject(info[0], LiveBean.class);

                    if (mCheckLivePresenter == null) {
                        mCheckLivePresenter = new UserHomeCheckLivePresenter(mContext, new UserHomeCheckLivePresenter.ActionListener() {
                            @Override
                            public void onLiveRoomChanged(LiveBean liveBean, int liveType, int liveTypeVal, int liveSdk, boolean isChatRoom, int chatRoomType) {
                                if (liveBean == null) {
                                    return;
                                }
                                if (!FloatWindowHelper.checkVoice(true)) {
                                    return;
                                }
                                LiveAudienceActivity instance = LiveAudienceActivity.instance;
                                if (instance != null) {
                                    instance.onLiveRoomChangeEvent(new LiveRoomChangeEvent(liveBean, liveType, liveTypeVal));
                                    dismiss();
                                } else {
                                    LiveAudienceActivity.forward(mContext, liveBean, liveType, liveTypeVal, "", 0, liveSdk, isChatRoom, chatRoomType, 1);
                                    dismiss();
                                }
                            }
                        });
                    }
                    mCheckLivePresenter.checkLive(liveBean);
                }
            }
        });
    }

    private void getType() {
        String uid = CommonAppConfig.getInstance().getUid();
        if (mToUid.equals(mLiveUid)) {
            if (mLiveUid.equals(uid)) {//主播点自己
                mType = TYPE_ANC_SELF;
            } else {//观众点主播
                mType = TYPE_AUD_ANC;
            }
        } else {
            if (mLiveUid.equals(uid)) {//主播点观众
                mType = TYPE_ANC_AUD;
            } else {
                if (mToUid.equals(uid)) {//观众点自己
                    mType = TYPE_AUD_SELF;
                } else {//观众点别的观众
                    mType = TYPE_AUD_AUD;
                }
            }
        }
    }

    private void loadData() {
        LiveHttpUtil.getLiveUser(mToUid, mLiveUid, new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0 && info.length > 0) {
                    showData(info[0]);
                }
            }
        });
    }

    private void showData(String data) {
        JSONObject obj = JSON.parseObject(data);
        mUserBean = JSON.toJavaObject(obj, UserBean.class);
        CommonAppConfig appConfig = CommonAppConfig.getInstance();
        mID.setText(mUserBean.getLiangNameTip());
        mToName = obj.getString("user_nickname");
        mName.setText(mToName);
        mCity.setText(obj.getString("city"));
        if (!isAnchor) {
            if (btn_live != null) {
                if (obj.getIntValue("islive") == 1) {
                    if (btn_live.getVisibility() != View.VISIBLE) {
                        btn_live.setVisibility(View.VISIBLE);
                    }
                } else {
                    if (btn_live.getVisibility() == View.VISIBLE) {
                        btn_live.setVisibility(View.INVISIBLE);
                    }
                }
            }
        }
        Glide.with(mContext).load(obj.getString("avatar")).into(mAvatar);
        JSONObject f = obj.getJSONObject("frame");
        if (f != null) {
            String mFrameUrl = f.getString("thumb");
            if (mFrameUrl != null && !mFrameUrl.isEmpty()) {
                loadSvga(mContext, mFrameUrl, mFrame);
            }
        }
        int levelAnchor = obj.getIntValue("level_anchor");
        int level = obj.getIntValue("level");
        mSign.setText(obj.getString("signature"));
        LevelBean anchorLevelBean = appConfig.getAnchorLevel(obj.getIntValue("level_anchor"));
        if (anchorLevelBean != null) {
            ImgLoader.display(mContext, anchorLevelBean.getBgIcon(), mLevelAnchor);
        }
        LevelBean levelBean = appConfig.getLevel(obj.getIntValue("level"));
        if (levelBean != null) {
            ImgLoader.display(mContext, levelBean.getBgIcon(), mLevel);
        }
        mLevelAnchorText.setText(String.valueOf(levelAnchor));
        mLevelText.setText(String.valueOf(level));
        mSex.setImageResource(CommonIconUtil.getSexIcon(obj.getIntValue("sex")));
        mFollow.setText(LiveTextRender.renderLiveUserDialogData(obj.getLongValue("follows")));
        mFans.setText(LiveTextRender.renderLiveUserDialogData(obj.getLongValue("fans")));
        mConsume.setText(LiveTextRender.renderLiveUserDialogData(obj.getLongValue("consumption")));
        mVotes.setText(LiveTextRender.renderLiveUserDialogData(obj.getLongValue("votestotal")));
        if (LanguageUtil.isZh()) {
            mConsumeTip.setText(WordUtil.getString(com.livestreaming.common.R.string.live_user_send) + appConfig.getCoinName());
            mVotesTip.setText(WordUtil.getString(com.livestreaming.common.R.string.live_user_get) + appConfig.getVotesName());
        } else {
            mConsumeTip.setText(WordUtil.getString(com.livestreaming.common.R.string.live_user_send));
            mVotesTip.setText(WordUtil.getString(com.livestreaming.common.R.string.live_user_get));
        }
        if (mType == TYPE_AUD_ANC || mType == TYPE_ANC_SELF) {
            showImpress(obj.getString("label"));
        }
        mFollowing = obj.getIntValue("isattention") == 1;
        if (mFollowText != null) {
            mFollowText.setText(mFollowing ? WordUtil.getString(com.livestreaming.common.R.string.following) : WordUtil.getString(com.livestreaming.common.R.string.follow));
        }
        if (mFollowImage != null) {
            mFollowImage.setImageDrawable(mFollowing ? mFollowDrawable : mUnFollowDrawable);
        }
        mAction = obj.getIntValue("action");
        if (mAction == SETTING_ACTION_AUD) {//Set General Audience to General Audience or Everyone to Super Management
            View btnReport = mRootView.findViewById(R.id.btn_report);
            btnReport.setVisibility(View.VISIBLE);
            btnReport.setOnClickListener(this);
        } else if (mAction == SETTING_ACTION_ADM//设置 房间管理员点普通观众
                || mAction == SETTING_ACTION_SUP//设置 超管点主播
                || mAction == SETTING_ACTION_ANC_AUD//设置 主播点普通观众
                || mAction == SETTING_ACTION_ANC_ADM) {//设置 主播点房间管理员
            View btnSetting = mRootView.findViewById(R.id.btn_setting);
            btnSetting.setVisibility(View.VISIBLE);
            btnSetting.setOnClickListener(this);
        }
        
        // Display agency info if available
        showAgencyInfo(obj);
    }

    private void showImpress(String impressJson) {
        List<ImpressBean> list = JSON.parseArray(impressJson, ImpressBean.class);
        if (list.size() > 2) {
            list = list.subList(0, 2);
        }
        LayoutInflater inflater = LayoutInflater.from(mContext);
        for (int i = 0, size = list.size(); i < size; i++) {
            MyTextView myTextView = (MyTextView) inflater.inflate(R.layout.view_impress_item_2, mImpressGroup, false);
            ImpressBean bean = list.get(i);
            bean.setCheck(1);
            myTextView.setBean(bean);
            mImpressGroup.addView(myTextView);
        }
        if (mType == TYPE_AUD_ANC) {
            TextView textView = (TextView) inflater.inflate(R.layout.view_impress_item_add, mImpressGroup, false);
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!((AbsActivity) mContext).checkLogin()) {
                        dismiss();
                        return;
                    }
                    addImpress();
                }
            });
            mImpressGroup.addView(textView);
        }

    }


    /**
     * 添加主播印象
     */
    private void addImpress() {
        dismiss();
        ((LiveActivity) mContext).openAddImpressWindow(mLiveUid);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i != R.id.btn_close && i != R.id.btn_home_page) {
            if (!((AbsActivity) mContext).checkLogin()) {
                dismiss();
                return;
            }
        }
        if (i == R.id.btn_close) {
            dismiss();

        } else if (i == R.id.btn_follow) {
            setAttention();

        } else if (i == R.id.btn_pri_msg) {
            openChatRoomWindow();

        } else if (i == R.id.btn_home_page) {
            forwardHomePage();

        } else if (i == R.id.btn_setting) {
            setting();

        } else if (i == R.id.btn_report) {
            report();

        } else if (i == R.id.btn_at) {
            if (!TextUtils.isEmpty(mToName)) {
                dismiss();
                ((LiveActivity) mContext).openChatWindow(mToName);
            }

        } else if (i == R.id.btn_copy_id) {
            copyId();
        }
    }

    /**
     * 打开私信聊天窗口
     */
    private void openChatRoomWindow() {
        LiveChatListDialogFragment fragment = new LiveChatListDialogFragment();

        Bundle bundle = new Bundle();
        if (!mToUid.equals(CommonAppConfig.getInstance().getUid())) {
            bundle.putString(Constants.URL, getPrivateMessageUrl());

        } else {
            bundle.putString(Constants.URL, getMainChatUrl());
        }
        fragment.setArguments(bundle);
        fragment.show(getChildFragmentManager(), "LiveChatListDialogFragment");
    }

    private String getPrivateMessageUrl() {
        if (!mToUid.equals(CommonAppConfig.getInstance().getUid())) {
            return "https://one.donalive.net/biklog?dona_id=" + CommonAppConfig.getInstance().getUid() +
                    "&receiver_id=" + mToUid;
        } else {
            return getMainChatUrl();
        }
    }

    private String getMainChatUrl() {
        return "https://one.donalive.net/biklog?dona_id=" + CommonAppConfig.getInstance().getUid() +
                "&home=1";
    }

    /**
     * 关注
     */
    private void setAttention() {
        CommonHttpUtil.setAttention(mToUid, mAttentionCallback);
    }

    private CommonCallback<Integer> mAttentionCallback = new CommonCallback<Integer>() {

        @Override
        public void callback(Integer isAttention) {
            mFollowing = isAttention == 1;
            if (mFollowText != null) {
                mFollowText.setText(mFollowing ? com.livestreaming.common.R.string.following : com.livestreaming.common.R.string.follow);
            }
            if (mFollowImage != null) {
                mFollowImage.setImageDrawable(mFollowing ? mFollowDrawable : mUnFollowDrawable);
            }
            if (isAttention == 1 && mLiveUid.equals(mToUid)) {//关注了主播
                String userName = CommonAppConfig.getInstance().getUserBean().getUserNiceName();
                ((LiveActivity) mContext).sendSystemMessage(
                        StringUtil.contact(userName, " followed the anchor"),
                        StringUtil.contact(userName, " followed the anchor"));
            }
        }
    };

    /**
     * 跳转到个人主页
     */
    private void forwardHomePage() {
        dismiss();
        RouteUtil.forwardUserHome(mContext, mToUid, true, mLiveUid);
    }

    /**
     * 举报
     */
    private void report() {
        LiveReportActivity.forward(mContext, mToUid);
    }

    /**
     * 设置
     */
    private void setting() {
        List<Integer> list = new ArrayList<>();
        switch (mAction) {
            case SETTING_ACTION_ADM://Settings: Room administrator clicks "General Audience"
                list.add(com.livestreaming.common.R.string.live_setting_kick);
                list.add(com.livestreaming.common.R.string.live_setting_gap);
                list.add(com.livestreaming.common.R.string.live_setting_gap_2);
                checkVoiceRoomUpMic(list);
                break;
            case SETTING_ACTION_SUP://Set up super management point anchor
                list.add(com.livestreaming.common.R.string.live_setting_close_live);
                list.add(com.livestreaming.common.R.string.live_setting_close_live_2);
                list.add(com.livestreaming.common.R.string.live_setting_forbid_account);
                break;
            case SETTING_ACTION_ANC_AUD://Set the anchor point to ordinary viewers
                list.add(com.livestreaming.common.R.string.live_setting_kick);
                list.add(com.livestreaming.common.R.string.live_setting_gap);
                list.add(com.livestreaming.common.R.string.live_setting_gap_2);
                list.add(com.livestreaming.common.R.string.live_setting_admin);
                list.add(com.livestreaming.common.R.string.live_setting_admin_list);
                checkVoiceRoomUpMic(list);
                break;
            case SETTING_ACTION_ANC_ADM://Settings Anchor Point Room Manager
                list.add(com.livestreaming.common.R.string.live_setting_kick);
                list.add(com.livestreaming.common.R.string.live_setting_gap);
                list.add(com.livestreaming.common.R.string.live_setting_gap_2);
                list.add(com.livestreaming.common.R.string.live_setting_admin_cancel);
                list.add(com.livestreaming.common.R.string.live_setting_admin_list);
                checkVoiceRoomUpMic(list);
                break;
        }

        DialogUitl.showStringArrayDialog(mContext, list.toArray(new Integer[list.size()]), mArrayDialogCallback);
    }

    private DialogUitl.StringArrayDialogCallback mArrayDialogCallback = new DialogUitl.StringArrayDialogCallback() {
        @Override
        public void onItemClick(String text, int tag) {
            if (tag == com.livestreaming.common.R.string.live_setting_kick) {
                kick();

            } else if (tag == com.livestreaming.common.R.string.live_setting_gap) {//永久禁言
                setShutUp();

            } else if (tag == com.livestreaming.common.R.string.live_setting_gap_2) {//本场禁言
                setShutUp2();

            } else if (tag == com.livestreaming.common.R.string.live_setting_admin || tag == com.livestreaming.common.R.string.live_setting_admin_cancel) {
                setAdmin();

            } else if (tag == com.livestreaming.common.R.string.live_setting_admin_list) {
                adminList();

            } else if (tag == com.livestreaming.common.R.string.live_setting_close_live) {
                closeLive();

            } else if (tag == com.livestreaming.common.R.string.live_setting_forbid_account) {
                forbidAccount();

            } else if (tag == com.livestreaming.common.R.string.live_setting_close_live_2) {//禁用直播
                closeLive2();
            } else if (tag == com.livestreaming.common.R.string.a_049) {//聊天室--下麦
                ((LiveActivity) mContext).closeUserVoiceMic(mToUid, mAction == SETTING_ACTION_ADM ? 2 : 1);
            }
//            else if (tag == R.string.a_050 || tag == R.string.a_053) {//聊天室--闭麦开麦切换
//                ((LiveActivity) mContext).changeVoiceMicOpen(mToUid);
//            }
        }
    };

    /**
     * 查看管理员列表
     */
    private void adminList() {
        dismiss();
        ((LiveActivity) mContext).openAdminListWindow();
    }

    /**
     * 踢人
     */
    private void kick() {
        LiveHttpUtil.kicking(mLiveUid, mToUid, new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0) {
                    ((LiveActivity) mContext).kickUser(mToUid, mToName);
                } else {
                    ToastUtil.show(msg);
                }
            }
        });
    }

    /**
     * 永久禁言
     */
    private void setShutUp() {
        LiveHttpUtil.setShutUp(mLiveUid, "0", 0, mToUid, new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0) {
                    ((LiveActivity) mContext).setShutUp(mToUid, mToName, 0);
                } else {
                    ToastUtil.show(msg);
                }
            }
        });
    }

    /**
     * 本场禁言
     */
    private void setShutUp2() {
        LiveHttpUtil.setShutUp(mLiveUid, mStream, 1, mToUid, new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0) {
                    ((LiveActivity) mContext).setShutUp(mToUid, mToName, 1);
                } else {
                    ToastUtil.show(msg);
                }
            }
        });
    }


    /**
     * 设置或取消管理员
     */
    private void setAdmin() {
        LiveHttpUtil.setAdmin(mLiveUid, mToUid, new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0 && info.length > 0) {
                    int res = JSON.parseObject(info[0]).getIntValue("isadmin");
                    if (res == 1) {//被设为管理员
                        mAction = SETTING_ACTION_ANC_ADM;
                    } else {//被取消管理员
                        mAction = SETTING_ACTION_ANC_AUD;
                    }
                    ((LiveActivity) mContext).sendSetAdminMessage(res, mToUid, mToName);
                }
            }
        });
    }


    /**
     * 超管关闭直播间
     */
    private void closeLive() {
        dismiss();
        LiveHttpUtil.superCloseRoom(mLiveUid, 0, null, mSuperCloseRoomCallback);
    }

    /**
     * 超管关闭直播间并禁止主播直播
     */
    private void closeLive2() {
        SuperForbidTimeDialogFragment fragment = new SuperForbidTimeDialogFragment();
        fragment.setActionListener(new SuperForbidTimeDialogFragment.ActionListener() {
            @Override
            public void onConfirmClick(SuperForbidTimeBean bean) {
                if (bean != null) {
                    dismiss();
                    LiveHttpUtil.superCloseRoom(mLiveUid, 1, bean.getId(), mSuperCloseRoomCallback);
                }
            }
        });
        fragment.show(((AbsActivity) mContext).getSupportFragmentManager(), "SuperForbidTimeDialogFragment");

    }

    /**
     * 超管关闭直播间并禁用主播账户
     */
    private void forbidAccount() {
        dismiss();
        LiveHttpUtil.superCloseRoom(mLiveUid, 2, null, mSuperCloseRoomCallback);
    }

    private HttpCallback mSuperCloseRoomCallback = new HttpCallback() {
        @Override
        public void onSuccess(int code, String msg, String[] info) {
            if (code == 0) {
                ToastUtil.show(JSON.parseObject(info[0]).getString("msg"));
                ((LiveActivity) mContext).superCloseRoom();
            } else {
                ToastUtil.show(msg);
            }
        }
    };

    @Override
    public void onDestroy() {
        LiveHttpUtil.cancel(LiveHttpConsts.GET_LIVE_USER);
        super.onDestroy();
    }

    /**
     * 判断本房间是否是语音直播间，当前用户是否上麦了，如果上麦了要加上 闭麦和下麦的操作
     */
    private void checkVoiceRoomUpMic(List<Integer> list) {
        if (((LiveActivity) mContext).isChatRoom()) {
            Integer status = ((LiveActivity) mContext).checkVoiceRoomUserUpMic(mToUid);
            if (status != null) {
                list.add(com.livestreaming.common.R.string.a_049);
//                if (status == Constants.VOICE_CTRL_OPEN) {
//                    list.add(R.string.a_050);
//                } else if (status == Constants.VOICE_CTRL_CLOSE) {
//                    list.add(R.string.a_053);
//                }
            }
        }
    }

    public interface WhenUserpClick {
        public void onClick(String uid);
    }

    private void copyId() {
        if (mID == null || TextUtils.isEmpty(mID.getText())) {
            return;
        }
        
        String idText = mID.getText().toString();
        String actualId = extractIdFromText(idText);
        
        if (TextUtils.isEmpty(actualId)) {
            return;
        }
        
        ClipboardManager clipboardManager = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboardManager != null) {
            ClipData clipData = ClipData.newPlainText("User ID", actualId);
            clipboardManager.setPrimaryClip(clipData);
            ToastUtil.show(com.livestreaming.common.R.string.copy_success);
        }
    }
    
    /**
     * Extract actual ID from text like "ID:147382" or "靓号:147382"
     * @param text The full text displayed
     * @return The actual ID number
     */
    private String extractIdFromText(String text) {
        if (TextUtils.isEmpty(text)) {
            return "";
        }
        
        // Handle "ID:147382" format
        if (text.startsWith("ID:")) {
            return text.substring(3);
        }
        
        // Handle "靓号:147382" format
        if (text.contains(":")) {
            String[] parts = text.split(":");
            if (parts.length > 1) {
                return parts[1];
            }
        }
        
        // If no prefix found, return the whole text (fallback)
        return text;
    }

    /**
     * Display agency information if available
     */
    private void showAgencyInfo(JSONObject obj) {
        JSONObject agencyInfo = obj.getJSONObject("agency_info");
        if (agencyInfo != null && mAgencyInfoContainer != null) {
            String agencyName = agencyInfo.getString("special_approval_name");
            String agencyProfilePic = agencyInfo.getString("user_profile_pic");
            
            // Check if agency data is valid (not null and not "nothing")
            if (!TextUtils.isEmpty(agencyName) && !agencyName.equals("nothing") && 
                !TextUtils.isEmpty(agencyProfilePic)) {
                
                // Set agency name
                mAgencyName.setText(agencyName);
                
                // Load agency profile picture
                ImgLoader.display(mContext, agencyProfilePic, mAgencyProfilePic);
                
                // Show the container
                mAgencyInfoContainer.setVisibility(View.VISIBLE);
            } else {
                // Hide the container if no valid agency info
                mAgencyInfoContainer.setVisibility(View.GONE);
            }
        } else {
            // Hide the container if no agency info
            if (mAgencyInfoContainer != null) {
                mAgencyInfoContainer.setVisibility(View.GONE);
            }
        }
    }
}
