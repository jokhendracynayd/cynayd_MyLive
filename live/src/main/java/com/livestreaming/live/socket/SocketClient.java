package com.livestreaming.live.socket;


import android.graphics.PointF;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.livestreaming.common.CommonAppConfig;
import com.livestreaming.common.Constants;
import com.livestreaming.common.bean.GoodsBean;
import com.livestreaming.common.bean.UserBean;
import com.livestreaming.common.utils.L;
import com.livestreaming.common.utils.LanguageUtil;
import com.livestreaming.common.utils.StringUtil;
import com.livestreaming.common.utils.ToastUtil;
import com.livestreaming.common.utils.WordUtil;
import com.livestreaming.live.R;
import com.livestreaming.live.bean.GlobalGiftBean;
import com.livestreaming.live.bean.LiveBuyGuardMsgBean;
import com.livestreaming.live.bean.LiveChatBean;
import com.livestreaming.live.bean.LiveDanMuBean;
import com.livestreaming.live.bean.LiveEnterRoomBean;
import com.livestreaming.live.bean.LiveGiftPrizePoolWinBean;
import com.livestreaming.live.bean.LiveLuckGiftWinBean;
import com.livestreaming.live.bean.LiveReceiveGiftBean;
import com.livestreaming.live.bean.LiveUserGiftBean;
import com.livestreaming.live.bean.LiveVoiceControlBean;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;

import java.lang.ref.WeakReference;
import java.util.List;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * Created by cxf on 2018/10/9.
 */

public class SocketClient {

    private static final String TAG = "socket";
    private Socket mSocket;
    private String mLiveUid;
    private String mStream;
    private SocketHandler mSocketHandler;

    public SocketClient(String url, SocketMessageListener listener) {
        if (!TextUtils.isEmpty(url)) {
            try {
                IO.Options option = new IO.Options();
                option.forceNew = true;
                option.reconnection = true;
                option.reconnectionDelay = 2000;
                mSocket = IO.socket(url, option);
                mSocket.on(Socket.EVENT_CONNECT, mConnectListener);//连接成功
                mSocket.on(Socket.EVENT_DISCONNECT, mDisConnectListener);//断开连接
                mSocket.on(Socket.EVENT_CONNECT_ERROR, mErrorListener);//连接错误
                mSocket.on("connect_timeout", mTimeOutListener);//连接超时
                mSocket.on("reconnect", mReConnectListener);//重连
                mSocket.on(Constants.SOCKET_CONN, onConn);//连接socket消息
                mSocket.on(Constants.SOCKET_BROADCAST, onBroadcast);//接收服务器广播的具体业务逻辑相关的消息

                mSocketHandler = new SocketHandler(listener);
            } catch (Exception e) {
                L.e(TAG, "socket url 异常--->" + e.getMessage());
            }
        }
    }


    public void connect(String liveuid, String stream) {
        mLiveUid = liveuid;
        mStream = stream;
        if (mSocket != null) {
            mSocket.connect();
        }
        if (mSocketHandler != null) {
            mSocketHandler.setLiveUid(liveuid);
        }
    }

    public void disConnect() {
        if (mSocket != null) {
            mSocket.close();
            mSocket.off();
        }
        if (mSocketHandler != null) {
            mSocketHandler.release();
        }
        mSocketHandler = null;
        mLiveUid = null;
        mStream = null;
    }

    /**
     * 向服务发送连接消息
     */
    private void conn() {
        org.json.JSONObject data = new org.json.JSONObject();
        try {
            data.put("uid", CommonAppConfig.getInstance().getUid());
            data.put("token", CommonAppConfig.getInstance().getToken());
            data.put("mobileid", CommonAppConfig.getInstance().getDeviceId());
            data.put("liveuid", mLiveUid);
            data.put("roomnum", mLiveUid);
            data.put("stream", mStream);
            mSocket.emit("conn", data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private Emitter.Listener mConnectListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            L.e(TAG, "--onConnect-->");
            conn();
        }
    };

    private Emitter.Listener mReConnectListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            L.e(TAG, "--reConnect-->");
            //conn();
        }
    };

    private Emitter.Listener mDisConnectListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            if (mSocketHandler != null) {
                mSocketHandler.sendEmptyMessage(Constants.SOCKET_WHAT_DISCONN);
            }
        }
    };
    private Emitter.Listener mErrorListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            L.e(TAG, "--onConnectError-->" + mStream);
        }
    };

    private Emitter.Listener mTimeOutListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            L.e(TAG, "--onConnectTimeOut-->");
        }
    };

    private Emitter.Listener onConn = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            if (mSocketHandler != null) {
                try {
                    String s = ((JSONArray) args[0]).getString(0);
                    L.e(TAG, "--onConn-->" + s);
                    Message msg = Message.obtain();
                    msg.what = Constants.SOCKET_WHAT_CONN;
                    msg.obj = s.equals("ok");
                    mSocketHandler.sendMessage(msg);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private Emitter.Listener onBroadcast = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            if (mSocketHandler != null) {
                try {
                    JSONArray array = (JSONArray) args[0];
                    for (int i = 0; i < array.length(); i++) {
                        Message msg = Message.obtain();
                        msg.what = Constants.SOCKET_WHAT_BROADCAST;
                        msg.obj = array.getString(i);
                        if (mSocketHandler != null) {
                            mSocketHandler.sendMessage(msg);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
    };


    public void send(SocketSendBean bean) {
        if (mSocket != null) {
            mSocket.emit(Constants.SOCKET_SEND, bean.create());
        }
    }

    private static class SocketHandler extends Handler {

        private SocketMessageListener mListener;
        private String mLiveUid;

        public SocketHandler(SocketMessageListener listener) {
            mListener = new WeakReference<>(listener).get();
        }

        public void setLiveUid(String liveUid) {
            mLiveUid = liveUid;
        }

        @Override
        public void handleMessage(Message msg) {
            if (mListener == null) {
                return;
            }
            try {
                switch (msg.what) {
                    case Constants.SOCKET_WHAT_CONN:
                        mListener.onConnect((Boolean) msg.obj);
                        break;
                    case Constants.SOCKET_WHAT_BROADCAST:
                        processBroadcast((String) msg.obj);
                        break;
                    case Constants.SOCKET_WHAT_DISCONN:
                        mListener.onDisConnect();
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        private String getLanguageCt(JSONObject map) {
            String ct = null;
            String lang = LanguageUtil.getInstance().getLanguage();
            if (Constants.LANG_EN.equals(lang)) {
                ct = map.getString(Constants.SOCKET_CT_EN);
            } else if (Constants.LANG_ZH.equals(lang)) {
                ct = map.getString(Constants.SOCKET_CT_ZH);
            } else {
                ct = map.getString(Constants.SOCKET_CT_AR);
            }
            if (TextUtils.isEmpty(ct)) {
                ct = map.getString("ct");
            }
            return ct;
        }


        private void processBroadcast(String socketMsg) {
            L.e("收到socket--->" + socketMsg);
            if (Constants.SOCKET_STOP_PLAY.equals(socketMsg)) {
                mListener.onSuperCloseLive();//超管关闭房间
                return;
            }
            SocketReceiveBean received = JSON.parseObject(socketMsg, SocketReceiveBean.class);
            JSONObject map = received.getMsg().getJSONObject(0);
            switch (map.getString("_method_")) {
                case Constants.SOCKET_SYSTEM://系统消息
                    systemChatMessage(getLanguageCt(map));
                    break;
                case Constants.SOCKET_KICK://踢人
                    systemChatMessage(getLanguageCt(map));
                    mListener.onKick(map.getString("touid"));
                    break;
                case Constants.SOCKET_SHUT_UP://禁言
                    String ct = getLanguageCt(map);
                    systemChatMessage(ct);
                    mListener.onShutUp(map.getString("touid"), ct);
                    break;
                case Constants.SOCKET_SEND_MSG://文字消息，点亮，用户进房间，这种混乱的设计是因为服务器端逻辑就是这样设计的,客户端无法自行修改
                    String msgtype = map.getString("msgtype");
                    if ("2".equals(msgtype)) {//发言，点亮
                        if ("409002".equals(received.getRetcode())) {
                            ToastUtil.show(com.livestreaming.common.R.string.live_you_are_shut);
                            return;
                        }
                        LiveChatBean chatBean = new LiveChatBean();
                        chatBean.setId(map.getString("uid"));
                        chatBean.setUserNiceName(map.getString("uname"));
                        chatBean.setLevel(map.getIntValue("level"));
                        chatBean.avatar = map.getString("uhead");
                        chatBean.frame = map.getString("uframe");
                        chatBean.setAnchor(map.getIntValue("isAnchor") == 1);
                        chatBean.setManager(map.getIntValue("usertype") == Constants.SOCKET_USER_TYPE_ADMIN);
                        int heart = map.getIntValue("heart");
                        chatBean.setHeart(heart);
                        if (heart > 0) {
                            chatBean.setType(LiveChatBean.LIGHT);
                        }
                        chatBean.setContent(getLanguageCt(map));
                        chatBean.setLiangName(map.getString("liangname"));
                        chatBean.setVipType(map.getIntValue("vip_type"));
                        chatBean.setGuardType(map.getIntValue("guard_type"));
                        
                        // Handle reply data
                        if (map.containsKey("message_id")) {
                            chatBean.setMessageId(map.getString("message_id"));
                        }
                        if (map.containsKey("timestamp")) {
                            chatBean.setTimestamp(map.getLongValue("timestamp"));
                        }
                        if (map.containsKey("reply_to_id")) {
                            chatBean.setReplyToId(map.getString("reply_to_id"));
                            chatBean.setReplyToUserId(map.getString("reply_to_user_id"));
                            chatBean.setReplyToUserName(map.getString("reply_to_user_name"));
                            chatBean.setReplyToContent(map.getString("reply_to_content"));
                            chatBean.setReplyToAvatar(map.getString("reply_to_avatar"));
                        }
                        
                        mListener.onChat(chatBean);
                    } else if ("0".equals(msgtype)) {//用户进入房间
                        JSONObject obj = JSON.parseObject(map.getString("ct"));
                        LiveUserGiftBean u = JSON.toJavaObject(obj, LiveUserGiftBean.class);
                        if (CommonAppConfig.getInstance().isLogin() && CommonAppConfig.getInstance().getUid().equals(u.getId())) {
                            UserBean userBean = CommonAppConfig.getInstance().getUserBean();
                            userBean.setLevel(u.getLevel());
                        }
                        UserBean.Vip vip = new UserBean.Vip();
                        int vipType = obj.getIntValue("vip_type");
                        vip.setType(vipType);
                        u.setVip(vip);
                        UserBean.Car car = new UserBean.Car();
                        car.setId(obj.getIntValue("car_id"));
                        car.setSwf(obj.getString("car_swf"));
                        car.setSwftime(obj.getFloatValue("car_swftime"));
                        String carWords = obj.getString("car_words");
                        if (LanguageUtil.isEn()) {
                            carWords = " " + obj.getString("words_en");
                        }
                        car.setWords(carWords);
                        u.setCar(car);
                        UserBean.Liang liang = new UserBean.Liang();
                        String liangName = obj.getString("liangname");
                        liang.setName(liangName);
                        u.setLiang(liang);
                        LiveChatBean chatBean = new LiveChatBean();
                        String uhead = map.getString("uhead");
                        // Ensure avatar is not null or empty
                        if (uhead != null && !uhead.trim().isEmpty()) {
                            chatBean.avatar = uhead;
                        } else if (u.getAvatar() != null && !u.getAvatar().trim().isEmpty()) {
                            // Fallback to user avatar if uhead is empty
                            chatBean.avatar = u.getAvatar();
                        }
                        chatBean.frame = map.getString("uframe");
                        chatBean.setType(LiveChatBean.ENTER_ROOM);
                        chatBean.setId(u.getId());
                        chatBean.setUserNiceName(u.getUserNiceName());
                        chatBean.setLevel(u.getLevel());
                        chatBean.setVipType(vipType);
                        chatBean.setLiangName(liangName);
                        chatBean.setManager(obj.getIntValue("usertype") == Constants.SOCKET_USER_TYPE_ADMIN);
                        chatBean.setContent(WordUtil.getString(com.livestreaming.common.R.string.live_enter_room));
                        chatBean.setGuardType(obj.getIntValue("guard_type"));

                        Log.e("testEnteree_play_1", "..........................................");
                        mListener.onEnterRoom(new LiveEnterRoomBean(u, chatBean));
                    }
                    break;
                case Constants.SOCKET_LIGHT://飘心

                    int action2 = map.getIntValue("action");
                    if (action2 == 3) {
                        if ("409002".equals(received.getRetcode())) {
                            ToastUtil.show(com.livestreaming.common.R.string.live_you_are_shut);
                            return;
                        }
                        LiveChatBean chatBean = new LiveChatBean();
                        chatBean.setId(map.getString("uid"));
                        chatBean.setUserNiceName(map.getString("uname"));
                        chatBean.setLevel(map.getIntValue("level"));
                        chatBean.avatar = map.getString("uhead");
                        chatBean.frame = map.getString("uframe");
                        chatBean.setAnchor(map.getIntValue("isAnchor") == 1);
                        chatBean.setManager(map.getIntValue("usertype") == Constants.SOCKET_USER_TYPE_ADMIN);
                        int heart = map.getIntValue("heart");
                        chatBean.setHeart(heart);
                        if (heart > 0) {
                            chatBean.setType(LiveChatBean.LIGHT);
                        }
                        chatBean.setContent(getLanguageCt(map));
                        chatBean.setLiangName(map.getString("liangname"));
                        chatBean.setVipType(map.getIntValue("vip_type"));
                        chatBean.setGuardType(map.getIntValue("guard_type"));
                        mListener.onChat(chatBean);
                    } else {
                        mListener.onLight();
                    }
                    break;
                case Constants.SOCKET_SEND_GIFT://send a gift
                    LiveReceiveGiftBean receiveGiftBean = JSON.parseObject(map.getString("ct"), LiveReceiveGiftBean.class);
                    receiveGiftBean.setAvatar(map.getString("uhead"));
                    receiveGiftBean.setUserNiceName(map.getString("uname"));

                    if (receiveGiftBean.getType() == 3) {//hand drawn gifts
                        List<PointF> list = JSON.parseArray(map.getString("paintedPath"), PointF.class);
                        receiveGiftBean.setPointList(list);
                        receiveGiftBean.setDrawWidth(map.getFloatValue("paintedWidth"));
                        receiveGiftBean.setDrawHeight(map.getFloatValue("paintedHeight"));
                    }

                    LiveChatBean chatBean = new LiveChatBean();
                    chatBean.setUserNiceName(receiveGiftBean.getUserNiceName());
                    chatBean.setLevel(receiveGiftBean.getLevel());
                    chatBean.setId(map.getString("uid"));
                    chatBean.avatar = map.getString("uhead");
                    chatBean.frame = map.getString("uframe");
                    chatBean.setLiangName(map.getString("liangname"));
                    chatBean.setVipType(map.getIntValue("vip_type"));
                    chatBean.setType(LiveChatBean.GIFT);

                    if (map.getIntValue("ifpk") == 1) {
                        if (!TextUtils.isEmpty(mLiveUid)) {
                            if (mLiveUid.equals(map.getString("roomnum"))) {
                                mListener.onSendGift(receiveGiftBean, chatBean);
                                mListener.onSendGiftPk(map.getLongValue("pktotal1"), map.getLongValue("pktotal2"));
                            } else {
                                mListener.onSendGiftPk(map.getLongValue("pktotal2"), map.getLongValue("pktotal1"));
                            }
                        }
                    } else {
                        mListener.onSendGift(receiveGiftBean, chatBean);
                    }
                    break;
                case Constants.SOCKET_SEND_BARRAGE://发弹幕
                    LiveDanMuBean liveDanMuBean = JSON.parseObject(map.getString("ct"), LiveDanMuBean.class);
                    liveDanMuBean.setAvatar(map.getString("uhead"));
                    liveDanMuBean.setAvatar(map.getString("uframe"));
                    liveDanMuBean.setUserNiceName(map.getString("uname"));
                    mListener.onSendDanMu(liveDanMuBean);
                    break;
                case Constants.SOCKET_LEAVE_ROOM://离开房间
                    UserBean u = JSON.parseObject(map.getString("ct"), UserBean.class);
                    
                    // Create a chat message for user leaving the room
                    LiveChatBean leaveChatBean = new LiveChatBean();
                    String uhead = map.getString("uhead");
                    // Ensure avatar is not null or empty
                    if (uhead != null && !uhead.trim().isEmpty()) {
                        leaveChatBean.avatar = uhead;
                    } else if (u.getAvatar() != null && !u.getAvatar().trim().isEmpty()) {
                        // Fallback to user avatar if uhead is empty
                        leaveChatBean.avatar = u.getAvatar();
                    }
                    leaveChatBean.frame = map.getString("uframe");
                    leaveChatBean.setType(LiveChatBean.ENTER_ROOM); // Reuse ENTER_ROOM type for leave room messages
                    leaveChatBean.setId(u.getId());
                    leaveChatBean.setUserNiceName(u.getUserNiceName());
                    leaveChatBean.setLevel(u.getLevel());
                    leaveChatBean.setVipType(map.getIntValue("vip_type"));
                    leaveChatBean.setLiangName(map.getString("liangname"));
                    leaveChatBean.setManager(map.getIntValue("usertype") == Constants.SOCKET_USER_TYPE_ADMIN);
                    leaveChatBean.setContent(WordUtil.getString(com.livestreaming.common.R.string.live_leave_room));
                    leaveChatBean.setGuardType(map.getIntValue("guard_type"));
                    
                    // Send both the leave room event and the chat message
                    mListener.onChat(leaveChatBean);
                    mListener.onLeaveRoom(u);

                    break;
                case Constants.SOCKET_LIVE_END://主播关闭直播
                    int action = map.getIntValue("action");
                    if (action == 18) {
                        mListener.onLiveEnd();
                    } else if (action == 19) {
                        mListener.onAnchorInvalid();
                    }
                    break;
                case Constants.SOCKET_CHANGE_LIVE://主播切换计时收费类型
                    mListener.onChangeTimeCharge(map.getIntValue("type_val"));
                    break;
                case Constants.SOCKET_UPDATE_VOTES:
                    mListener.onUpdateVotes(map.getString("uid"), map.getString("votes"), map.getIntValue("isfirst"));
                    break;
                case Constants.SOCKET_FAKE_FANS:
                    if (map.containsKey("ct")) {
                        JSONObject obj = map.getJSONObject("ct");
                        if (obj != null) {
                            try {
                                String s = obj.getJSONObject("data").getJSONArray("info").getJSONObject(0).getString("list");
                                L.e("僵尸粉--->" + s);
                                List<LiveUserGiftBean> list = JSON.parseArray(s, LiveUserGiftBean.class);
                                mListener.addFakeFans(list);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    break;
                case Constants.SOCKET_SET_ADMIN://设置或取消管理员
                    systemChatMessage(getLanguageCt(map));
                    mListener.onSetAdmin(map.getString("touid"), map.getIntValue("action"));
                    break;
                case Constants.SOCKET_BUY_GUARD://购买守护
                    LiveBuyGuardMsgBean buyGuardMsgBean = new LiveBuyGuardMsgBean();
                    buyGuardMsgBean.setUid(map.getString("uid"));
                    buyGuardMsgBean.setUserName(map.getString("uname"));
                    buyGuardMsgBean.setVotes(map.getString("votestotal"));
                    buyGuardMsgBean.setGuardNum(map.getIntValue("guard_nums"));
                    buyGuardMsgBean.setGuardType(map.getIntValue("guard_type"));
                    mListener.onBuyGuard(buyGuardMsgBean);
                    break;
                case Constants.SOCKET_VOICE_ROOM://语音聊天室
                    processVoiceRoom(map);
                    break;
                case Constants.SOCKET_LINK_MIC://连麦
                    processLinkMic(map);
                    break;
                case Constants.SOCKET_LINK_MIC_ANCHOR://主播连麦
                    processLinkMicAnchor(map);
                    break;
                case Constants.SOCKET_LINK_MIC_PK://主播PK
                    processAnchorLinkMicPk(map);
                    break;
                case Constants.SOCKET_RED_PACK://红包消息
                    String uid = map.getString("uid");
                    if (TextUtils.isEmpty(uid)) {
                        return;
                    }
                    LiveChatBean liveChatBean = new LiveChatBean();
                    liveChatBean.setType(LiveChatBean.RED_PACK);
                    liveChatBean.setId(uid);
                    String name = uid.equals(mLiveUid) ? WordUtil.getString(com.livestreaming.common.R.string.live_anchor) : map.getString("uname");
                    if (LanguageUtil.isZh()) {
                        liveChatBean.setContent(StringUtil.contact(name, getLanguageCt(map)));
                    } else if (LanguageUtil.isEn()) {
                        liveChatBean.setContent(StringUtil.contact(name, " ", getLanguageCt(map)));
                    } else {
                        liveChatBean.setContent(StringUtil.contact(name, " ", getLanguageCt(map)));
                    }
                    mListener.onRedPack(liveChatBean);
                    break;

                case Constants.SOCKET_LUCK_WIN://幸运礼物中奖
                    mListener.onLuckGiftWin(map.toJavaObject(LiveLuckGiftWinBean.class));
                    break;

                case Constants.SOCKET_PRIZE_POOL_WIN://奖池中奖
                    mListener.onPrizePoolWin(map.toJavaObject(LiveGiftPrizePoolWinBean.class));
                    break;
                case Constants.SOCKET_PRIZE_POOL_UP://奖池升级
                    mListener.onPrizePoolUp(map.getString("uplevel"));
                    break;

                case Constants.SOCKET_GIFT_GLOBAL://全站礼物
                    mListener.onGlobalGift(map.toJavaObject(GlobalGiftBean.class));
                    break;
                case Constants.SOCKET_LIVE_GOODS_SHOW:
                    if (map.getIntValue("action") == 1) {
                        GoodsBean goodsbean = new GoodsBean();
                        goodsbean.setId(map.getString("goodsid"));
                        goodsbean.setThumb(map.getString("goods_thumb"));
                        goodsbean.setName(map.getString("goods_name"));
                        goodsbean.setPriceNow(map.getString("goods_price"));
                        goodsbean.setType(map.getIntValue("goods_type"));
                        mListener.onLiveGoodsShow(goodsbean);
                    } else {
                        mListener.onLiveGoodsShow(null);
                    }
                    break;
                case Constants.SOCKET_LIVE_GOODS_FLOAT:
                    mListener.onLiveGoodsFloat(map.getString("uname"));
                    break;
                case Constants.SOCKET_LIVE_WARNING:
                    mListener.onLiveRoomWarning(getLanguageCt(map));
                    break;
                case Constants.SOCKET_XQTB_WIN://星球探宝中奖
                    mListener.onGameXqtbWin(map.getString("list"));
                    break;
                case Constants.SOCKET_LUCKPAN_WIN://幸运大转盘中奖
                    mListener.onGameLuckpanWin(map.getString("list"));
                    break;
                //游戏socket
                case Constants.SOCKET_GAME_ZJH://游戏 智勇三张
                    if (CommonAppConfig.GAME_ENABLE) {
                        mListener.onGameZjh(map);
                    }
                    break;
                case Constants.SOCKET_GAME_ZP://游戏 幸运转盘
                    if (CommonAppConfig.GAME_ENABLE) {
                        mListener.onGameZp(map);
                    }
                    break;
                case Constants.PK_BOUNS:
                    mListener.updatePkBouns(map.getIntValue("value"), map.getIntValue("uid"), map.getIntValue("completed"));
                    break;
                case Constants.SENDGIFT_LIVE:
                    action = map.getIntValue("action");
                    switch (action) {
                        case 2:
                            mListener.onSendGiftLiveResponse(map.getString("coin"), map.getIntValue("level"), map.getIntValue("type"));
                            break;
                    }
                    break;

                case Constants.SockitLiveLike:
                    action = map.getIntValue("action");
                    switch (action) {
                        case 2:
                            if (map.getIntValue("ifpk") == 1) {
                                if (mLiveUid.equals(map.getString("roomnum"))) {
                                    mListener.onSendGiftPk(map.getLongValue("pktotal1"), map.getLongValue("pktotal2"));
                                } else {
                                    mListener.onSendGiftPk(map.getLongValue("pktotal2"), map.getLongValue("pktotal1"));
                                }
                            }
                            mListener.onTotalLikesCount(map.getIntValue("total"));

                            break;
                    }
                    break;
            }
        }


        /**
         * 接收到系统消息，显示在聊天栏中
         */
        private void systemChatMessage(String content) {
            LiveChatBean bean = new LiveChatBean();
            bean.setContent(content);
            bean.setType(LiveChatBean.SYSTEM);
            mListener.onChat(bean);
        }

        /**
         * 处理观众与主播连麦逻辑
         */
        private void processLinkMic(JSONObject map) {
            int action = map.getIntValue("action");
            switch (action) {
                case 1://主播收到观众连麦的申请
                    UserBean u = new UserBean();
                    u.setId(map.getString("uid"));
                    u.setUserNiceName(map.getString("uname"));
                    u.setAvatar(map.getString("uhead"));
                    u.setFrame(map.getString("mFrame"));
                    u.setSex(map.getIntValue("sex"));
                    u.setLevel(map.getIntValue("level"));
                    mListener.onAudienceApplyLinkMic(u);
                    break;
                case 2://观众收到主播同意连麦的消息
                    mListener.onAnchorAcceptLinkMic(map.getString("touid"));
                    break;
                case 3://观众收到主播拒绝连麦的消息
                    if (map.getString("touid").equals(CommonAppConfig.getInstance().getUid())) {
                        mListener.onAnchorRefuseLinkMic();
                    }
                    break;
                case 4://所有人收到连麦观众发过来的流地址
                    String uid = map.getString("uid");
                    if (!TextUtils.isEmpty(uid) && !uid.equals(CommonAppConfig.getInstance().getUid())) {
                        mListener.onAudienceSendLinkMicUrl(uid, map.getString("uname"), map.getString("playurl"));
                    }
                    break;
                case 5://连麦观众自己断开连麦
                    mListener.onAudienceCloseLinkMic(map.getString("uid"), map.getString("uname"));
                    break;
                case 6://主播断开已连麦观众的连麦
                    mListener.onAnchorCloseLinkMic(map.getString("touid"), map.getString("uname"));
                    break;
                case 7://已申请连麦的观众收到主播繁忙的消息
                    if (map.getString("touid").equals(CommonAppConfig.getInstance().getUid())) {
                        mListener.onAnchorBusy();
                    }
                    break;
                case 8://已申请连麦的观众收到主播无响应的消息
                    if (map.getString("touid").equals(CommonAppConfig.getInstance().getUid())) {
                        mListener.onAnchorNotResponse();
                    }
                    break;
                case 9://所有人收到已连麦的观众退出直播间消息
                    mListener.onAudienceLinkMicExitRoom(map.getString("touid"));
                    break;
                case 12:
                    mListener.onGestCloseOpenCam(
                            map.getString("touid"),
                            map.getString("avatar"),
                            map.getInteger("isOn")
                    );
                    break;
                case 13:
                    mListener.handleMicGests(
                            map.getString("touid"),
                            map.getInteger("isOn")
                    );
                    break;
                case 14:
                    mListener.onAnchorCloseLive();
                    break;

                case 16:
                    UserBean bean = new Gson().fromJson(map.getString("bean"), UserBean.class);
                    int isAgree = map.getIntValue("isAgree");
                    mListener.onAdminAgreeUpLinkMicc(bean, isAgree);
                    break;
                case 17:
                    LiveVoiceControlBean beann = new Gson().fromJson(map.getString("bean"), LiveVoiceControlBean.class);
                    mListener.onAdminControlMicc(beann);
                    break;
                case 18:
                    LiveVoiceControlBean beann2 = new Gson().fromJson(map.getString("bean"), LiveVoiceControlBean.class);
                    mListener.onAdminCloseMicc(beann2);
                    break;

                case 19:
                    mListener.onUpdateGuestIncome(map.getIntValue("uid"), map.getIntValue("income"));
                    break;
            }
        }

        /**
         * 处理主播与主播连麦逻辑
         *
         * @param map
         */
        private void processLinkMicAnchor(JSONObject map) {
            int action = map.getIntValue("action");
            switch (action) {
                case 1://收到其他主播连麦的邀请的回调
                    UserBean u = new UserBean();
                    u.setId(map.getString("uid"));
                    u.setUserNiceName(map.getString("uname"));
                    u.setAvatar(map.getString("uhead"));
                    u.setFrame(map.getString("mFrame"));
                    u.setSex(map.getIntValue("sex"));
                    u.setLevel(map.getIntValue("level"));
                    u.setLevelAnchor(map.getIntValue("level_anchor"));
                    mListener.onLinkMicAnchorApply(u, map.getString("stream"));
                    break;
                case 3://对方主播拒绝连麦的回调
                    mListener.onLinkMicAnchorRefuse();
                    break;
                case 4://Everyone receives a callback from the other host’s streaming address
                    mListener.onLinkMicAnchorPlayUrl(map.getString("uid"), map.getString("pkuid"), map.getString("tostream"), map.getString("pkpull")
                            , map.getIntValue("room_user"), map.getIntValue("pk_user"));
                    break;
                case 5://断开连麦的回调
                    mListener.onLinkMicAnchorClose();
                    break;
                case 7://对方主播正在忙的回调
                    mListener.onLinkMicAnchorBusy();
                    break;
                case 8://对方主播无响应的回调
                    mListener.onLinkMicAnchorNotResponse();
                    break;
                case 9://对方主播正在游戏
                    mListener.onlinkMicPlayGaming();
                    break;
                case 10:
                    mListener.onAnchorBusy();
                    break;
            }
        }

        /**
         * 处理主播与主播PK逻辑
         *
         * @param map
         */
        private void processAnchorLinkMicPk(JSONObject map) {
            int action = map.getIntValue("action");
            int msg_type = map.getIntValue("msgtype");
            switch (action) {
                case 1://收到对方主播PK回调
                    UserBean u = new UserBean();
                    u.setId(map.getString("uid"));
                    u.setUserNiceName(map.getString("uname"));
                    u.setAvatar(map.getString("uhead"));
                    u.setFrame(map.getString("mFrame"));
                    u.setSex(map.getIntValue("sex"));
                    u.setLevel(map.getIntValue("level"));
                    u.setLevelAnchor(map.getIntValue("level_anchor"));
                    mListener.onLinkMicPkApply(u, map.getString("stream"));
                    break;
                case 3://对方主播拒绝PK的回调
                    mListener.onLinkMicPkRefuse();
                    break;
                case 4://所有人收到PK开始址的回调
                    mListener.onLinkMicPkStart(map.getString("pkuid"));
                    break;
                case 5://PK时候断开连麦的回调
                    mListener.onLinkMicPkClose();
                    break;
                case 7://对方主播正在忙的回调
                    mListener.onLinkMicPkBusy();
                    break;
                case 8://对方主播无响应的回调
                    mListener.onLinkMicPkNotResponse();
                    break;
                case 9://pk结束的回调
                    mListener.onLinkMicPkEnd(map.getString("win_uid"), map.getIntValue("win_count"));
                    break;
                case 12://pk结束的回调
                    mListener.onAncorCancelPk(map.getString("win_uid"), map.getIntValue("win_count"));
                    break;
                case 13:
                    mListener.onPkBouns(
                            map.getIntValue("duration"),
                            map.getIntValue("start_after"),
                            map.getIntValue("x"),
                            map.getIntValue("mission_type"),
                            map.getIntValue("target_value"),
                            map.getIntValue("double_sec")
                    );
                    break;
                case 15:
                    Log.e("test_double_total_points_1", "........................");
                    mListener.handleDoubleTotalPoints(map.getIntValue("totalPoints"), map.getIntValue("uid"));
                    break;
                case 16:

                    mListener.handleWhenEnterRoomDouble(
                            map.getIntValue("uid"),
                            map.getIntValue("x"),
                            map.getIntValue("mission_type"),
                            map.getIntValue("duration"),
                            map.getIntValue("start_after"),
                            map.getIntValue("double_sec"),
                            map.getIntValue("currentTime"),
                            map.getIntValue("state"),
                            map.getIntValue("target_value"),
                            map.getIntValue("targetValueDefault"),
                            map.getIntValue("completed")
                    );
                    break;
            }
        }

        /**
         * 处理语音聊天室逻辑
         */
        private void processVoiceRoom(JSONObject map) {
            int action = map.getIntValue("action");
            switch (action) {
                case 1://主播收到观众的上麦申请
                    mListener.onVoiceRoomApplyUpMic();
                    break;
                case 2://观众收到主播同意或拒绝上麦的消息
                    mListener.onVoiceRoomHandleApply(map.getString("touid"), map.getString("toname"), map.getString("avatar"), map.getIntValue("position"), map.getString("frame"));
                    break;
                case 3://所有人收到某人下麦的消息
                    mListener.onVoiceRoomDownMic(map.getString("uid"), map.getIntValue("type"));
                    break;
                case 4://主播控制麦位 闭麦开麦禁麦等
                    mListener.onControlMicPosition(map.getString("uid"), map.getIntValue("position"), map.getIntValue("status"));
                    break;
                case 5://观众上麦后推流成功，把自己的播放地址广播给所有人
                    mListener.onVoiceRoomPushSuccess(map.getString("uid"), map.getString("pull"), map.getString("user_stream"));
                    break;
                case 6://收到上麦观众发送表情的消息
                    mListener.onVoiceRoomFace(map.getString("uid"), map.getIntValue("face"));
                    break;
                case 7://收到上麦观众发送表情的消息
                    int type = map.getIntValue("cam_type");
                    if (type == 1) {
                        mListener.onVoiceRoomUserCam(map.getString("uid"), map.getIntValue("type"), false);
                    } else if (type == 2) {
                        mListener.onLiveHostSwitchCam(map.getIntValue("type"), map.getString("uid"), map.getString("thumb"));
                    }
                    break;
                case 12://change background
                    mListener.onRoomBackgroundChanged(map.getString("backUrl"));
                    break;

                    case 10://change enabled request
                    mListener.onChangeRoomGuetEnabled(map.getIntValue("isEnabled"));
                    break;
                case 14://change enabled request
                    mListener.onUserUpVoiceLinkMicDirect(new Gson().fromJson(map.getString("user"),UserBean.class));
                    break;
            }
        }


        public void release() {
            mListener = null;
        }
    }
}

