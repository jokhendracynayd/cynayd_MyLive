package com.livestreaming.im.adapter;

import android.animation.ValueAnimator;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.livestreaming.common.CommonAppConfig;
import com.livestreaming.common.Constants;
import com.livestreaming.common.bean.UserBean;
import com.livestreaming.common.glide.ImgLoader;
import com.livestreaming.common.http.CommonHttpUtil;
import com.livestreaming.common.http.HttpCallback;
import com.livestreaming.common.interfaces.CommonCallback;
import com.livestreaming.common.utils.ClickUtil;
import com.livestreaming.common.utils.DialogUitl;
import com.livestreaming.common.utils.DpUtil;
import com.livestreaming.common.utils.FaceTextUtil;
import com.livestreaming.common.utils.MD5Util;
import com.livestreaming.common.utils.RouteUtil;
import com.livestreaming.common.utils.StringUtil;
import com.livestreaming.common.utils.ToastUtil;
import com.livestreaming.common.utils.WordUtil;
import com.livestreaming.im.R;
import com.livestreaming.im.bean.ImChatImageBean;
import com.livestreaming.im.bean.ImMessageBean;
import com.livestreaming.im.custom.ChatVoiceLayout;
import com.livestreaming.im.custom.MyImageView;
import com.livestreaming.im.utils.ImDateUtil;
import com.livestreaming.im.utils.ImMessageUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by cxf on 2018/10/25.
 */

public class ImRoomAdapter extends RecyclerView.Adapter {

    private static final int TYPE_TEXT_LEFT = 1;
    private static final int TYPE_TEXT_RIGHT = 2;
    private static final int TYPE_IMAGE_LEFT = 3;
    private static final int TYPE_IMAGE_RIGHT = 4;
    private static final int TYPE_VOICE_LEFT = 5;
    private static final int TYPE_VOICE_RIGHT = 6;
    private static final int TYPE_LOCATION_LEFT = 7;
    private static final int TYPE_LOCATION_RIGHT = 8;
    private static final int TYPE_GOODS_LEFT = 10;
    private static final int TYPE_GOODS_RIGHT = 11;
    private static final int TYPE_PROMPT = 9;

    private Context mContext;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private UserBean mUserBean;
    private UserBean mToUserBean;
    private String mToUid;
    private String mTxMapAppKey;
    private String mTxMapAppSecret;
    private List<ImMessageBean> mList;
    private LayoutInflater mInflater;
    private String mUserAvatar;
    private String mToUserAvatar;
    private ActionListener mActionListener;
    private View.OnClickListener mOnImageClickListener;
    private int[] mLocation;
    private ValueAnimator mAnimator;
    private ChatVoiceLayout mChatVoiceLayout;
    private View.OnClickListener mOnVoiceClickListener;
    private View.OnLongClickListener mOnLongClickListener;
    private PopupWindow mPopupWindow;
    private View.OnClickListener mGoodsClickListener;
    private View.OnClickListener mLocationClickListener;
    private int mDp120;
    private long mLastMsgTime;

    public ImRoomAdapter(Context context, String toUid, UserBean toUserBean) {
        mContext = context;
        mList = new ArrayList<>();
        mInflater = LayoutInflater.from(context);
        mUserBean = CommonAppConfig.getInstance().getUserBean();
        mToUserBean = toUserBean;
        mToUid = toUid;
        mTxMapAppKey = CommonAppConfig.getInstance().getTxMapAppKey();
        mTxMapAppSecret = CommonAppConfig.getInstance().getTxMapAppSecret();
        mUserAvatar = mUserBean.getAvatar();
        mToUserAvatar = mToUserBean.getAvatar();
        mLocation = new int[2];
        mDp120 = DpUtil.dp2px(120);
        mOnImageClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!ClickUtil.canClick()) {
                    return;
                }
                MyImageView imageView = (MyImageView) v;
                imageView.getLocationOnScreen(mLocation);
                if (mActionListener != null) {
                    mActionListener.onImageClick(imageView, mLocation[0], mLocation[1]);
                }
            }
        };
        mOnVoiceClickListener = new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!ClickUtil.canClick()) {
                    return;
                }
                Object tag = v.getTag();
                if (tag == null) {
                    return;
                }
                final int position = (int) tag;
                ImMessageBean bean = mList.get(position);
                if (mChatVoiceLayout != null) {
                    mChatVoiceLayout.cancelAnim();
                    if (mChatVoiceLayout.getImMessageBean() == bean) {//同一个消息对象
                        if (mRecyclerView != null) {
                            mRecyclerView.setLayoutFrozen(false);
                        }
                        mChatVoiceLayout = null;
                        if (mActionListener != null) {
                            mActionListener.onVoiceStopPlay();
                        }
                    } else {
                        mChatVoiceLayout = (ChatVoiceLayout) v;
                        ImMessageUtil.getInstance().getSoundFile(bean, new CommonCallback<File>() {
                            @Override
                            public void callback(File file) {
                                notifyItemChanged(position, Constants.PAYLOAD);
                                if (mAnimator != null) {
                                    mAnimator.start();
                                }
                                if (mActionListener != null) {
                                    mActionListener.onVoiceStartPlay(file);
                                }
                            }
                        });
                    }
                } else {
                    mChatVoiceLayout = (ChatVoiceLayout) v;
                    ImMessageUtil.getInstance().getSoundFile(bean, new CommonCallback<File>() {
                        @Override
                        public void callback(File file) {
                            notifyItemChanged(position, Constants.PAYLOAD);
                            if (mRecyclerView != null) {
                                mRecyclerView.setLayoutFrozen(true);
                            }
                            if (mAnimator != null) {
                                mAnimator.start();
                            }
                            if (mActionListener != null) {
                                mActionListener.onVoiceStartPlay(file);
                            }
                        }
                    });
                }
            }
        };
        mAnimator = ValueAnimator.ofFloat(0, 900);
        mAnimator.setInterpolator(new LinearInterpolator());
        mAnimator.setDuration(700);
        mAnimator.setRepeatCount(-1);
        mAnimator.setRepeatMode(ValueAnimator.RESTART);
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float v = (float) animation.getAnimatedValue();
                if (mChatVoiceLayout != null) {
                    mChatVoiceLayout.animate((int) (v / 300));
                }
            }
        });

        mOnLongClickListener = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                final ImMessageBean bean = (ImMessageBean) v.getTag(R.id.btn_copy);
                View contentV = null;
                if (bean.getType() == ImMessageBean.TYPE_TEXT) {
                    contentV = LayoutInflater.from(mContext).inflate(R.layout.view_chat_popup, null);
                    contentV.findViewById(R.id.btn_copy).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String text = bean.getTextMsgContent();
                            ClipboardManager cm = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                            ClipData clipData = ClipData.newPlainText("text", text);
                            cm.setPrimaryClip(clipData);
                            ToastUtil.show(com.livestreaming.common.R.string.copy_success);
                            if (mPopupWindow != null) {
                                mPopupWindow.dismiss();
                            }
                        }
                    });
                } else {
                    contentV = LayoutInflater.from(mContext).inflate(R.layout.view_chat_popup_2, null);
                }
                if (contentV == null) {
                    return false;
                }
                contentV.findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onPromptVoiceMessage(bean);
                        ImMessageUtil.getInstance().revokeMessage(mToUserBean.getId(), bean.getMsgId());
                        if (mPopupWindow != null) {
                            mPopupWindow.dismiss();
                        }
                    }
                });
                v.getLocationInWindow(mLocation);
//                L.e("mLocation---x---> " + mLocation[0] + "---y--->" + mLocation[1]);
                PopupWindow popupWindow = new PopupWindow(contentV, ViewGroup.LayoutParams.WRAP_CONTENT, DpUtil.dp2px(30), true);
                popupWindow.setBackgroundDrawable(new ColorDrawable());
                popupWindow.setOutsideTouchable(true);
                popupWindow.showAtLocation(mRecyclerView, Gravity.LEFT | Gravity.TOP, mLocation[0], mLocation[1] - DpUtil.dp2px(30));
                mPopupWindow = popupWindow;
                return true;
            }
        };

        mGoodsClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String goodsId = (String) v.getTag(com.livestreaming.common.R.string.a_001);
                int type = Integer.parseInt((String) v.getTag(com.livestreaming.common.R.string.a_002));
                if (type == Constants.GOODS_TYPE_OUT) {
                    RouteUtil.forwardGoodsDetailOutSide(mContext, goodsId, false);
                } else {
                    RouteUtil.forwardGoodsDetail(mContext, goodsId, false, "0", mToUid);
                }
            }
        };
        mLocationClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImMessageBean.LocationMsgBean locationBean  = (ImMessageBean.LocationMsgBean) v.getTag(R.id.btn_location);
                if (locationBean == null) {
                    return;
                }

                List<Integer> list = new ArrayList<>();
                /*if (CommonAppConfig.isAppExist(Constants.PACKAGE_NAME_GAODE_MAP)) {
                    list.add(com.livestreaming.common.R.string.map_gaode);
                }
                if (CommonAppConfig.isAppExist(Constants.PACKAGE_NAME_BAIDU_MAP)) {
                    list.add(com.livestreaming.common.R.string.map_baidu);
                }
                if (CommonAppConfig.isAppExist(Constants.PACKAGE_NAME_TX_MAP)) {
                    list.add(com.livestreaming.common.R.string.map_tencent);
                }*/
                if (list.size() == 0) {
                    ToastUtil.show(com.livestreaming.common.R.string.a_141);
                    return;
                }
                final String lat = String.valueOf(locationBean.getLat());
                final String lng = String.valueOf(locationBean.getLng());
                String addressName = "";
                try {
                    addressName = JSON.parseObject(locationBean.getAddress()).getString("name");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                final String address = addressName;


                DialogUitl.showStringArrayDialog(mContext, list.toArray(new Integer[list.size()]), new DialogUitl.StringArrayDialogCallback() {
                    @Override
                    public void onItemClick(String text, int tag) {
                        if (tag == com.livestreaming.common.R.string.map_gaode) {
                            String url = StringUtil.contact(
                                    "amapuri://route/plan?sname=我的位置&slat=",
                                    String.valueOf(CommonAppConfig.getInstance().getLat()),
                                    "&slon=",
                                    String.valueOf(CommonAppConfig.getInstance().getLng()),
                                    "&dlat=", lat,
                                    "&dlon=", lng,
                                    "&dname=", address,
                                    "&dev=0"
                            );
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setPackage(Constants.PACKAGE_NAME_GAODE_MAP);
                            intent.setData(Uri.parse(url));
                            mContext.startActivity(intent);

                        } else if (tag == com.livestreaming.common.R.string.map_baidu) {
                            String url = StringUtil.contact(
                                    "baidumap://map/direction?origin=name:我的位置|latlng:",
                                    String.valueOf(CommonAppConfig.getInstance().getLat()), ",",
                                    String.valueOf(CommonAppConfig.getInstance().getLng()),
                                    "&destination=name:", address, "|latlng:", lat, ",", lng,
                                    "&mode=driving&coord_type=wgs84&src=", CommonAppConfig.PACKAGE_NAME
                            );
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setPackage(Constants.PACKAGE_NAME_BAIDU_MAP);
                            intent.setData(Uri.parse(url));
                            mContext.startActivity(intent);
                        } else if (tag == com.livestreaming.common.R.string.map_tencent) {
                            String url = StringUtil.contact(
                                    "qqmap://map/routeplan?type=drive&policy=0&from=我的位置&fromcoord=",
                                    String.valueOf(CommonAppConfig.getInstance().getLat()), ",",
                                    String.valueOf(CommonAppConfig.getInstance().getLng()),
                                    "&to=", address,
                                    "&tocoord=", lat, ",", lng,
                                    "&referer=", CommonAppConfig.getInstance().getTxMapAppKey()
                            );
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setPackage(Constants.PACKAGE_NAME_TX_MAP);
                            intent.setData(Uri.parse(url));
                            mContext.startActivity(intent);
                        }
                    }
                });


            }
        };
    }

    /**
     * 停止语音动画
     */
    public void stopVoiceAnim() {
        if (mChatVoiceLayout != null) {
            mChatVoiceLayout.cancelAnim();
        }
        mChatVoiceLayout = null;
        if (mRecyclerView != null) {
            mRecyclerView.setLayoutFrozen(false);
        }
    }

    @Override
    public int getItemViewType(int position) {
        ImMessageBean msg = mList.get(position);
        if (msg.isRevoked()) {
            return TYPE_PROMPT;
        }
        switch (msg.getType()) {
            case ImMessageBean.TYPE_TEXT:
                if (msg.isSelf()) {
                    return TYPE_TEXT_RIGHT;
                } else {
                    return TYPE_TEXT_LEFT;
                }
            case ImMessageBean.TYPE_IMAGE:
                if (msg.isSelf()) {
                    return TYPE_IMAGE_RIGHT;
                } else {
                    return TYPE_IMAGE_LEFT;
                }
            case ImMessageBean.TYPE_SOUND:
                if (msg.isSelf()) {
                    return TYPE_VOICE_RIGHT;
                } else {
                    return TYPE_VOICE_LEFT;
                }
            case ImMessageBean.TYPE_LOCATION:
                if (msg.isSelf()) {
                    return TYPE_LOCATION_RIGHT;
                } else {
                    return TYPE_LOCATION_LEFT;
                }
            case ImMessageBean.TYPE_GOODS:
                if (msg.isSelf()) {
                    return TYPE_GOODS_RIGHT;
                } else {
                    return TYPE_GOODS_LEFT;
                }
        }
        return 0;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_TEXT_LEFT:
                return new TextVh(mInflater.inflate(R.layout.item_chat_text_left, parent, false));
            case TYPE_TEXT_RIGHT:
                return new SelfTextVh(mInflater.inflate(R.layout.item_chat_text_right, parent, false));
            case TYPE_IMAGE_LEFT:
                return new ImageVh(mInflater.inflate(R.layout.item_chat_image_left, parent, false));
            case TYPE_IMAGE_RIGHT:
                return new SelfImageVh(mInflater.inflate(R.layout.item_chat_image_right, parent, false));
            case TYPE_VOICE_LEFT:
                return new VoiceVh(mInflater.inflate(R.layout.item_chat_voice_left, parent, false));
            case TYPE_VOICE_RIGHT:
                return new SelfVoiceVh(mInflater.inflate(R.layout.item_chat_voice_right, parent, false));
            case TYPE_LOCATION_LEFT:
                return new LocationVh(mInflater.inflate(R.layout.item_chat_location_left, parent, false));
            case TYPE_LOCATION_RIGHT:
                return new SelfLocationVh(mInflater.inflate(R.layout.item_chat_location_right, parent, false));
            case TYPE_GOODS_LEFT:
                return new GoodsVh(mInflater.inflate(R.layout.item_chat_goods_left, parent, false));
            case TYPE_GOODS_RIGHT:
                return new GoodsVh(mInflater.inflate(R.layout.item_chat_goods_right, parent, false));
            case TYPE_PROMPT:
                return new PromptVh(mInflater.inflate(R.layout.item_chat_prompt, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder vh, int position) {

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder vh, int position, List payloads) {
        if (vh instanceof Vh) {
            Object payload = payloads.size() > 0 ? payloads.get(0) : null;
            ((Vh) vh).setData(mList.get(position), position, payload);
        } else if (vh instanceof PromptVh) {
            ((PromptVh) vh).setData(mList.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        mRecyclerView = recyclerView;
        mLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
    }

    public void insertItem(ImMessageBean bean) {
        if (mList != null && bean != null) {
            long msgTime = bean.getTimestamp();
            if (!ImDateUtil.isCloseEnough(msgTime, mLastMsgTime)) {
                bean.setShowTimeString(ImDateUtil.getTimestampString(msgTime));
                mLastMsgTime = msgTime;
            }
            int size = mList.size();
            mList.add(bean);
            notifyItemInserted(size);
            int lastItemPosition = mLayoutManager.findLastCompletelyVisibleItemPosition();
            if (lastItemPosition != size - 1) {
                mRecyclerView.smoothScrollToPosition(size);
            } else {
                mRecyclerView.scrollToPosition(size);
            }
        }
    }

    public void updateSendStatus(ImMessageBean imMsgBean, int sendStatus) {
        for (int i = 0, size = mList.size(); i < size; i++) {
            if (mList.get(i)==imMsgBean) {
                mList.get(i).setStatus(sendStatus);
                notifyItemChanged(i, Constants.PAYLOAD);
                break;
            }
        }
    }


    public ImChatImageBean getChatImageBean(ImMessageBean imMessageBean) {
        List<ImMessageBean> list = new ArrayList<>();
        int imagePosition = 0;
        for (int i = 0, size = mList.size(); i < size; i++) {
            ImMessageBean bean = mList.get(i);
            if (bean.getType() == ImMessageBean.TYPE_IMAGE) {
                list.add(bean);
                if (bean == imMessageBean) {
                    imagePosition = list.size() - 1;
                }
            }
        }
        return new ImChatImageBean(list, imagePosition);
    }

    public void setList(List<ImMessageBean> list) {
        if (mList != null && list != null && list.size() > 0) {
            mList.clear();
            mList.addAll(list);
            notifyDataSetChanged();
        }
    }

    public void scrollToBottom() {
        if (mList.size() > 0 && mLayoutManager != null) {
            mLayoutManager.scrollToPositionWithOffset(mList.size() - 1, -DpUtil.dp2px(20));
        }
    }

    public long getLastMsgTime() {
        return mLastMsgTime;
    }

    public void setLastMsgTime(long lastMsgTime) {
        mLastMsgTime = lastMsgTime;
    }


    class Vh extends RecyclerView.ViewHolder {
        ImageView mAvatar;
        TextView mTime;

        public Vh(View itemView) {
            super(itemView);
            mAvatar = (ImageView) itemView.findViewById(R.id.avatar);
            mTime = (TextView) itemView.findViewById(R.id.time);
        }

        void setData(ImMessageBean bean, int position, Object payload) {
            if (payload == null) {
                if (bean.isSelf()) {
                    ImgLoader.display(mContext, mUserAvatar, mAvatar);
                } else {
                    ImgLoader.display(mContext, mToUserAvatar, mAvatar);
                }
                String showTimeString = bean.getShowTimeString();
                if (TextUtils.isEmpty(showTimeString)) {
                    if (mTime.getVisibility() != View.GONE) {
                        mTime.setVisibility(View.GONE);
                    }
                } else {
                    if (mTime.getVisibility() != View.VISIBLE) {
                        mTime.setVisibility(View.VISIBLE);
                    }
                    mTime.setText(showTimeString);
                }
            }
        }
    }

    class TextVh extends Vh {

        TextView mText;

        public TextVh(View itemView) {
            super(itemView);
            mText = (TextView) itemView.findViewById(R.id.text);
        }

        @Override
        public void setData(ImMessageBean bean, int position, Object payload) {
            super.setData(bean, position, payload);
            if (payload == null) {
                String text = bean.getTextMsgContent();
                if (!TextUtils.isEmpty(text)) {
                    mText.setText(FaceTextUtil.renderChatMessage(text));
                }
            }
        }
    }

    class SelfTextVh extends TextVh {

        View mFailIcon;
        View mLoading;
        View mBubble;

        public SelfTextVh(View itemView) {
            super(itemView);
            mFailIcon = itemView.findViewById(R.id.icon_fail);
            mLoading = itemView.findViewById(R.id.loading);
            mBubble = itemView.findViewById(R.id.bubble);
            mBubble.setOnLongClickListener(mOnLongClickListener);
        }

        @Override
        public void setData(ImMessageBean bean, int position, Object payload) {
            super.setData(bean, position, payload);
            mBubble.setTag(R.id.btn_copy, bean);
            if (bean.getStatus() == ImMessageBean.STATUS_SEND_ING) {
                if (mLoading.getVisibility() != View.VISIBLE) {
                    mLoading.setVisibility(View.VISIBLE);
                }
            } else {
                if (mLoading.getVisibility() == View.VISIBLE) {
                    mLoading.setVisibility(View.INVISIBLE);
                }
            }
            if (bean.getStatus() == ImMessageBean.STATUS_SEND_FAIL) {
                if (mFailIcon.getVisibility() != View.VISIBLE) {
                    mFailIcon.setVisibility(View.VISIBLE);
                }
            } else {
                if (mFailIcon.getVisibility() == View.VISIBLE) {
                    mFailIcon.setVisibility(View.INVISIBLE);
                }
            }
        }
    }

    class ImageVh extends Vh {

        MyImageView mImg;

        public ImageVh(View itemView) {
            super(itemView);
            mImg = (MyImageView) itemView.findViewById(R.id.img);
            mImg.setOnClickListener(mOnImageClickListener);
        }

        @Override
        public void setData(ImMessageBean bean, int position, Object payload) {
            super.setData(bean, position, payload);
            if (payload == null) {
                mImg.setImageDrawable(null);
                mImg.setImMessageBean(bean);
                ImMessageBean.ImageMsgBean imageMsgBean = bean.getImageBean();
                if (imageMsgBean == null) {
                    return;
                }
                ImgLoader.DrawableCallback drawableCallback = new ImgLoader.DrawableCallback() {
                    @Override
                    public void onLoadSuccess(Drawable drawable) {
                        if (mImg != null) {
                            float w = drawable.getIntrinsicWidth();
                            float h = drawable.getIntrinsicHeight();
                            int targetHeight = (int) (h / w * mDp120);
                            ViewGroup.LayoutParams lp = mImg.getLayoutParams();
                            if (lp.height != targetHeight) {
                                lp.height = targetHeight;
                                mImg.requestLayout();
                            }
                            mImg.setImageDrawable(drawable);
                        }
                    }

                    @Override
                    public void onLoadFailed() {

                    }
                };
                String localPath = imageMsgBean.getLocalPath();
                if (!TextUtils.isEmpty(localPath)) {
                    ImgLoader.displayDrawable(mContext, new File(localPath), drawableCallback);
                } else {
                    ImgLoader.displayDrawable(mContext, imageMsgBean.getThumbImageUrl(), drawableCallback);
                }
            }
        }
    }

    class SelfImageVh extends ImageVh {

        View mFailIcon;
        View mLoading;

        public SelfImageVh(View itemView) {
            super(itemView);
            mFailIcon = itemView.findViewById(R.id.icon_fail);
            mLoading = itemView.findViewById(R.id.loading);
            mImg.setOnLongClickListener(mOnLongClickListener);
        }

        @Override
        public void setData(ImMessageBean bean, int position, Object payload) {
            super.setData(bean, position, payload);
            mImg.setTag(R.id.btn_copy, bean);
            if (bean.getStatus() == ImMessageBean.STATUS_SEND_ING) {
                if (mLoading.getVisibility() != View.VISIBLE) {
                    mLoading.setVisibility(View.VISIBLE);
                }
            } else {
                if (mLoading.getVisibility() == View.VISIBLE) {
                    mLoading.setVisibility(View.INVISIBLE);
                }
            }
            if (bean.getStatus() == ImMessageBean.STATUS_SEND_FAIL) {
                if (mFailIcon.getVisibility() != View.VISIBLE) {
                    mFailIcon.setVisibility(View.VISIBLE);
                }
            } else {
                if (mFailIcon.getVisibility() == View.VISIBLE) {
                    mFailIcon.setVisibility(View.INVISIBLE);
                }
            }
        }
    }

    class VoiceVh extends Vh {

        TextView mDuration;
        View mRedPoint;
        ChatVoiceLayout mChatVoiceLayout;

        public VoiceVh(View itemView) {
            super(itemView);
            mRedPoint = itemView.findViewById(R.id.red_point);
            mDuration = (TextView) itemView.findViewById(R.id.duration);
            mChatVoiceLayout = itemView.findViewById(R.id.voice);
            mChatVoiceLayout.setOnClickListener(mOnVoiceClickListener);
        }

        @Override
        public void setData(ImMessageBean bean, int position, Object payload) {
            super.setData(bean, position, payload);
            ImMessageBean.SoundMsgBean soundMsgBean = bean.getSoundBean();
            if (soundMsgBean == null) {
                return;
            }
            if (payload == null) {
                mDuration.setText(soundMsgBean.getDuration() + "s");
                mChatVoiceLayout.setTag(position);
                mChatVoiceLayout.setImMessageBean(bean);
                mChatVoiceLayout.setDuration(soundMsgBean.getDuration());
            }
            if (soundMsgBean.isRead()) {
                if (mRedPoint.getVisibility() == View.VISIBLE) {
                    mRedPoint.setVisibility(View.INVISIBLE);
                }
            } else {
                if (mRedPoint.getVisibility() != View.VISIBLE) {
                    mRedPoint.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    class SelfVoiceVh extends Vh {

        TextView mDuration;
        ChatVoiceLayout mChatVoiceLayout;
        View mFailIcon;
        View mLoading;
        View mBubble;

        public SelfVoiceVh(View itemView) {
            super(itemView);
            mDuration = (TextView) itemView.findViewById(R.id.duration);
            mChatVoiceLayout = itemView.findViewById(R.id.voice);
            mChatVoiceLayout.setOnClickListener(mOnVoiceClickListener);
            mFailIcon = itemView.findViewById(R.id.icon_fail);
            mLoading = itemView.findViewById(R.id.loading);
            mBubble = itemView.findViewById(R.id.bubbleLayout);
            mBubble.setOnLongClickListener(mOnLongClickListener);
            mChatVoiceLayout.setOnLongClickListener(mOnLongClickListener);
        }

        @Override
        public void setData(ImMessageBean bean, int position, Object payload) {
            super.setData(bean, position, payload);
            ImMessageBean.SoundMsgBean soundMsgBean = bean.getSoundBean();
            if (soundMsgBean == null) {
                return;
            }
            if (payload == null) {
                mBubble.setTag(R.id.btn_copy, bean);
                mDuration.setText(soundMsgBean.getDuration() + "s");
                mChatVoiceLayout.setTag(position);
                mChatVoiceLayout.setTag(R.id.btn_copy, bean);
                mChatVoiceLayout.setImMessageBean(bean);
                mChatVoiceLayout.setDuration(soundMsgBean.getDuration());
            }
            if (bean.getStatus() == ImMessageBean.STATUS_SEND_ING) {
                if (mLoading.getVisibility() != View.VISIBLE) {
                    mLoading.setVisibility(View.VISIBLE);
                }
            } else {
                if (mLoading.getVisibility() == View.VISIBLE) {
                    mLoading.setVisibility(View.INVISIBLE);
                }
            }
            if (bean.getStatus() == ImMessageBean.STATUS_SEND_FAIL) {
                if (mFailIcon.getVisibility() != View.VISIBLE) {
                    mFailIcon.setVisibility(View.VISIBLE);
                }
            } else {
                if (mFailIcon.getVisibility() == View.VISIBLE) {
                    mFailIcon.setVisibility(View.INVISIBLE);
                }
            }
        }
    }


    class LocationVh extends Vh {

        TextView mTitle;
        TextView mAddress;
        ImageView mMap;
        View mGroup;


        public LocationVh(View itemView) {
            super(itemView);
            mTitle = (TextView) itemView.findViewById(R.id.title);
            mAddress = (TextView) itemView.findViewById(R.id.address);
            mMap = (ImageView) itemView.findViewById(R.id.map);
            mGroup = itemView.findViewById(R.id.group);
            mGroup.setOnClickListener(mLocationClickListener);
        }

        @Override
        public void setData(ImMessageBean bean, int position, Object payload) {
            super.setData(bean, position, payload);
            if (payload == null) {
                ImMessageBean.LocationMsgBean locationBean = bean.getLocationBean();
                mGroup.setTag(R.id.btn_location, locationBean);
                if (locationBean == null) {
                    return;
                }
                try {
                    JSONObject obj = JSON.parseObject(locationBean.getAddress());
                    mTitle.setText(obj.getString("name"));
                    mAddress.setText(obj.getString("info"));
                } catch (Exception e) {
                    mTitle.setText("");
                    mAddress.setText("");
                }
                int zoom = 16;
                double lat = locationBean.getLat();
                double lng = locationBean.getLng();
                //腾讯地图生成静态图接口
                String sign = MD5Util.getMD5("/ws/staticmap/v2/?center=" + lat + "," + lng + "&key=" + mTxMapAppKey + "&scale=2&size=200*120&zoom=" + zoom + mTxMapAppSecret);

                String staticMapUrl = "http://apis.map.qq.com/ws/staticmap/v2/?center=" + lat + "," + lng + "&size=200*120&scale=2&zoom=" + zoom + "&key=" + mTxMapAppKey + "&sig=" + sign;
                ImgLoader.display(mContext, staticMapUrl, mMap);
            }

        }
    }

    class SelfLocationVh extends LocationVh {

        View mFailIcon;
        View mLoading;


        public SelfLocationVh(View itemView) {
            super(itemView);
            mFailIcon = itemView.findViewById(R.id.icon_fail);
            mLoading = itemView.findViewById(R.id.loading);
            mGroup.setOnLongClickListener(mOnLongClickListener);

        }

        @Override
        public void setData(ImMessageBean bean, int position, Object payload) {
            super.setData(bean, position, payload);
            mGroup.setTag(R.id.btn_copy, bean);
            if (bean.getStatus() == ImMessageBean.STATUS_SEND_ING) {
                if (mLoading.getVisibility() != View.VISIBLE) {
                    mLoading.setVisibility(View.VISIBLE);
                }
            } else {
                if (mLoading.getVisibility() == View.VISIBLE) {
                    mLoading.setVisibility(View.INVISIBLE);
                }
            }
            if (bean.getStatus() == ImMessageBean.STATUS_SEND_FAIL) {
                if (mFailIcon.getVisibility() != View.VISIBLE) {
                    mFailIcon.setVisibility(View.VISIBLE);
                }
            } else {
                if (mFailIcon.getVisibility() == View.VISIBLE) {
                    mFailIcon.setVisibility(View.INVISIBLE);
                }
            }

        }
    }


    class GoodsVh extends Vh {

        View mBtnGoods;
        ImageView mGoodsThumb;
        TextView mGoodsPrice;
        TextView mGoodsTitle;
        TextView mGoodsSaleNum;

        public GoodsVh(View itemView) {
            super(itemView);
            mBtnGoods = itemView.findViewById(R.id.btn_goods);
            mGoodsThumb = itemView.findViewById(R.id.goods_thumb);
            mGoodsPrice = itemView.findViewById(R.id.goods_price);
            mGoodsTitle = itemView.findViewById(R.id.goods_title);
            mGoodsSaleNum = itemView.findViewById(R.id.goods_sale_num);
            mBtnGoods.setOnClickListener(mGoodsClickListener);
        }

        @Override
        public void setData(ImMessageBean bean, int position, Object payload) {
            super.setData(bean, position, payload);
            if (payload == null) {
                ImMessageBean.GoodsMsgBean goodsBean = bean.getGoodsBean();
                if (goodsBean == null) {
                    return;
                }
                String goodsId = goodsBean.getGoodsId();
                if (!TextUtils.isEmpty(goodsId)) {
                    CommonHttpUtil.getGoodsInfo(goodsId, new HttpCallback() {
                        @Override
                        public void onSuccess(int code, String msg, String[] info) {
                            JSONObject obj = JSON.parseObject(info[0]);
                            JSONObject goodsInfo = obj.getJSONObject("goods_info");
                            mBtnGoods.setTag(com.livestreaming.common.R.string.a_001, goodsInfo.getString("id"));
                            mBtnGoods.setTag(com.livestreaming.common.R.string.a_002, goodsInfo.getString("type"));
                            List<String> thumbs = JSON.parseArray(goodsInfo.getString("thumbs_format"), String.class);
                            if (thumbs != null && thumbs.size() > 0) {
                                if (mGoodsThumb != null) {
                                    ImgLoader.display(mContext, thumbs.get(0), mGoodsThumb);
                                }
                            }
                            if (mGoodsTitle != null) {
                                mGoodsTitle.setText(goodsInfo.getString("name"));
                            }
                            JSONArray specArray = goodsInfo.getJSONArray("specs_format");
                            if (specArray != null && specArray.size() > 0) {
                                JSONObject specObj = specArray.getJSONObject(0);
                                if (mGoodsPrice != null) {
                                    mGoodsPrice.setText(specObj.getString("price"));
                                }
                            }
                            if (mGoodsSaleNum != null) {
                                mGoodsSaleNum.setText(String.format(WordUtil.getString(com.livestreaming.common.R.string.mall_114), goodsInfo.getString("sale_nums")));
                            }
                        }
                    });
                }
            }
        }
    }

    class PromptVh extends RecyclerView.ViewHolder {

        TextView mTextView;

        public PromptVh(@NonNull View itemView) {
            super(itemView);
            mTextView = (TextView) itemView;
        }

        void setData(ImMessageBean bean) {
            mTextView.setText(bean.isSelf() ? com.livestreaming.common.R.string.chat_msg_prompt_0 : com.livestreaming.common.R.string.chat_msg_prompt_1);
        }
    }


    public void setActionListener(ActionListener actionListener) {
        mActionListener = actionListener;
    }

    public interface ActionListener {
        void onImageClick(MyImageView imageView, int x, int y);

        void onVoiceStartPlay(File voiceFile);

        void onVoiceStopPlay();
    }

    public void release() {
        if (mAnimator != null) {
            mAnimator.cancel();
        }
        mActionListener = null;
        mOnImageClickListener = null;
        mOnVoiceClickListener = null;
    }

    public void clear() {
        if (mList != null) {
            mList.clear();
        }
        notifyDataSetChanged();
    }

    public void onPromptMessage(String msgId) {
        if (mList == null || mList.size() == 0) {
            return;
        }
        for (int i = 0, size = mList.size(); i < size; i++) {
            ImMessageBean bean = mList.get(i);
            if (bean.getMsgId().equals(msgId)) {
                onPromptVoiceMessage(bean);
                bean.setRevoked(true);
                notifyItemChanged(i);
                break;
            }
        }
    }


    /**
     * 撤回语音消息的时候要停止播放语音
     */
    private void onPromptVoiceMessage(ImMessageBean bean) {
        if (bean.getType() == ImMessageBean.TYPE_SOUND) {
            if (mChatVoiceLayout != null) {
                if (mChatVoiceLayout.getImMessageBean() == bean) {//同一个消息对象
                    mChatVoiceLayout.cancelAnim();
                    if (mRecyclerView != null) {
                        mRecyclerView.setLayoutFrozen(false);
                    }
                    mChatVoiceLayout = null;
                    if (mActionListener != null) {
                        mActionListener.onVoiceStopPlay();
                    }
                }
            }
        }
    }

}
