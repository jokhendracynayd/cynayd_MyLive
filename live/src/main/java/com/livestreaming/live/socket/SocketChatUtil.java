package com.livestreaming.live.socket;

import android.text.TextUtils;
import android.util.Log;

import com.livestreaming.common.CommonAppConfig;
import com.livestreaming.common.Constants;
import com.livestreaming.common.bean.GoodsBean;
import com.livestreaming.common.bean.UserBean;
import com.livestreaming.common.utils.StringUtil;
import com.livestreaming.common.utils.WordFilterUtil;
import com.livestreaming.common.utils.WordUtil;
import com.livestreaming.live.R;
import com.livestreaming.live.bean.LiveChatBean;

import java.util.ArrayList;

/**
 * Created by cxf on 2018/10/9.
 * 直播间发言
 */

public class SocketChatUtil {


    public static void sendChatMessage(SocketClient client, String content, boolean isAnchor, int userType, int guardType) {
        sendChatMessage(client, content, null, isAnchor, userType, guardType);
    }

    /**
     * 发言
     */
    public static void sendChatMessage(SocketClient client, String content, String contentEn, boolean isAnchor, int userType, int guardType) {
        if (client == null) {
            return;
        }
        UserBean u = CommonAppConfig.getInstance().getUserBean();
        if (u == null) {
            return;
        }
        content = WordFilterUtil.getInstance().filter(content);
        SocketSendBean socketSendBean = new SocketSendBean()
                .param("_method_", Constants.SOCKET_SEND_MSG)
                .param("action", 0)
                .param("msgtype", 2)
                .param("usertype", userType)
                .param("isAnchor", isAnchor ? 1 : 0)
                .param("level", u.getLevel())
                .param("uname", u.getUserNiceName())
                .param("uhead", u.getAvatar())
                .param("uframe", u.getFrame())
                .param("uid", u.getId())
                .param("liangname", u.getGoodName())
                .param("vip_type", u.getVip().getType())
                .param("guard_type", guardType)
                .param("ct", content);
        if (!TextUtils.isEmpty(contentEn)) {
            socketSendBean.param("ct_en", contentEn);
        }
        client.send(socketSendBean);
    }

    /**
     * 发送回复消息
     */
    public static void sendReplyMessage(SocketClient client, String content, LiveChatBean replyToMessage, boolean isAnchor, int userType, int guardType) {
        if (client == null || replyToMessage == null) {
            return;
        }
        UserBean u = CommonAppConfig.getInstance().getUserBean();
        if (u == null) {
            return;
        }
        content = WordFilterUtil.getInstance().filter(content);
        
        // Generate unique message ID
        String messageId = System.currentTimeMillis() + "_" + u.getId();
        
        SocketSendBean socketSendBean = new SocketSendBean()
                .param("_method_", Constants.SOCKET_SEND_MSG)
                .param("action", 0)
                .param("msgtype", 2)
                .param("usertype", userType)
                .param("isAnchor", isAnchor ? 1 : 0)
                .param("level", u.getLevel())
                .param("uname", u.getUserNiceName())
                .param("uhead", u.getAvatar())
                .param("uframe", u.getFrame())
                .param("uid", u.getId())
                .param("liangname", u.getGoodName())
                .param("vip_type", u.getVip().getType())
                .param("guard_type", guardType)
                .param("ct", content)
                .param("message_id", messageId)
                .param("timestamp", String.valueOf(System.currentTimeMillis()))
                // Reply specific parameters
                .param("reply_to_id", replyToMessage.getMessageId())
                .param("reply_to_user_id", replyToMessage.getId())
                .param("reply_to_user_name", replyToMessage.getUserNiceName())
                .param("reply_to_content", replyToMessage.getContent())
                .param("reply_to_avatar", replyToMessage.avatar);
                
        client.send(socketSendBean);
    }

    /**
     * 点亮
     */
    public static void sendLightMessage(SocketClient client, int heart, int guardType) {
        if (!CommonAppConfig.getInstance().isLogin()) {
            return;
        }
        if (client == null) {
            return;
        }
        UserBean u = CommonAppConfig.getInstance().getUserBean();
        if (u == null) {
            return;
        }
        client.send(new SocketSendBean()
                .param("_method_", Constants.SOCKET_SEND_MSG)
                .param("action", 0)
                .param("msgtype", 2)
                .param("level", u.getLevel())
                .param("uname", u.getUserNiceName())
                .param("uid", u.getId())
                .param("liangname", u.getGoodName())
                .param("vip_type", u.getVip().getType())
                .param("heart", heart)
                .param("uhead", u.getAvatar())
                .param("uframe", u.getFrame())
                .param("guard_type", guardType)
                .param(Constants.SOCKET_CT_ZH, "I light up")
                .param(Constants.SOCKET_CT_EN, "I light up")
        );

    }

    /**
     * 发送飘心消息
     */
    public static void sendFloatHeart(SocketClient client) {
        if (client == null) {
            return;
        }
        client.send(new SocketSendBean()
                .param("_method_", Constants.SOCKET_LIGHT)
                .param("action", 2)
                .param("msgtype", 0)
                .param("ct", ""));
    }

    /**
     * 发送弹幕消息
     */
    public static void sendDanmuMessage(SocketClient client, String danmuToken) {
        if (client == null) {
            return;
        }
        UserBean u = CommonAppConfig.getInstance().getUserBean();
        if (u == null) {
            return;
        }
        client.send(new SocketSendBean()
                .param("_method_", Constants.SOCKET_SEND_BARRAGE)
                .param("action", 7)
                .param("msgtype", 1)
                .param("level", u.getLevel())
                .param("uname", u.getUserNiceName())
                .param("uid", u.getId())
                .param("uhead", u.getAvatar())
                .param("uframe", u.getFrame())
                .param("ct", danmuToken));
    }

    /**
     * 发送礼物消息
     */
    public static void sendGiftMessage(SocketClient client, int giftType, String giftToken, String liveUid, String liveName) {
        sendGiftMessage(client, giftType, giftToken, liveUid, liveName, "", 0, 0);
    }

    public static void sendGiftMessage(SocketClient client, int giftType, String giftToken, String liveUid, String liveName, String paintedPath, int paintedWidth, int paintedHeight) {
        if (client == null) {
            return;
        }
        UserBean u = CommonAppConfig.getInstance().getUserBean();
        if (u == null) {
            return;
        }
        client.send(new SocketSendBean()
                .param("_method_", Constants.SOCKET_SEND_GIFT)
                .param("action", 0)
                .param("msgtype", 1)
                .param("level", u.getLevel())
                .param("uname", u.getUserNiceName())
                .param("uid", u.getId())
                .param("uhead", u.getAvatar())
                .param("uframe", u.getFrame())
                .param("evensend", giftType)
                .param("liangname", u.getGoodName())
                .param("vip_type", u.getVip().getType())
                .param("ct", giftToken)
                .param("roomnum", liveUid)
                .param("livename", liveName)
                .paramJsonArray("paintedPath", paintedPath)
                .param("paintedWidth", paintedWidth)
                .param("paintedHeight", paintedHeight)
        );
    }

    public static void sendGiftMessage(SocketClient client, int giftType, String giftToken, String liveUid, String liveName, ArrayList<GoodsBean> goodsBeanList) {
        if (client == null) {
            return;
        }
        UserBean u = CommonAppConfig.getInstance().getUserBean();
        if (u == null) {
            return;
        }
        client.send(new SocketSendBean()
                .param("_method_", Constants.SOCKET_SEND_GIFT)
                .param("action", 0)
                .param("msgtype", 1)
                .param("level", u.getLevel())
                .param("uname", u.getUserNiceName())
                .param("uid", u.getId())
                .param("uhead", u.getAvatar())
                .param("uframe", u.getFrame())
                .param("evensend", giftType)
                .param("liangname", u.getGoodName())
                .param("vip_type", u.getVip().getType())
                .param("ct", giftToken)
                .param("roomnum", liveUid)
                .param("livename", liveName)
                .paramJsonArray("goods_list", new com.google.gson.Gson().toJson(goodsBeanList))
        );
    }

    /**
     * 主播或管理员 踢人
     */
    public static void sendKickMessage(SocketClient client, String toUid, String toName) {
        if (client == null) {
            return;
        }
        UserBean u = CommonAppConfig.getInstance().getUserBean();
        if (u == null) {
            return;
        }
        client.send(new SocketSendBean()
                .param("_method_", Constants.SOCKET_KICK)
                .param("action", 2)
                .param("msgtype", 4)
                .param("level", u.getLevel())
                .param("uname", u.getUserNiceName())
                .param("uid", u.getId())
                .param("touid", toUid)
                .param("toname", toName)
                .param(Constants.SOCKET_CT_ZH, StringUtil.contact(toName, "was kicked out of the room"))
                .param(Constants.SOCKET_CT_EN, StringUtil.contact(toName, " was kicked out of the room"))
        );
    }


    /**
     * 主播或管理员 禁言
     */
    public static void sendShutUpMessage(SocketClient client, String toUid, String toName, int type) {
        if (client == null) {
            return;
        }
        UserBean u = CommonAppConfig.getInstance().getUserBean();
        if (u == null) {
            return;
        }
        client.send(new SocketSendBean()
                .param("_method_", Constants.SOCKET_SHUT_UP)
                .param("action", 1)
                .param("msgtype", 4)
                .param("level", u.getLevel())
                .param("uname", u.getUserNiceName())
                .param("uid", u.getId())
                .param("touid", toUid)
                .param("toname", toName)
                .param(Constants.SOCKET_CT_ZH, StringUtil.contact(toName, type == 0 ? " is permanently banned" : " has been banned from this site"))
                .param(Constants.SOCKET_CT_EN, StringUtil.contact(toName, type == 0 ? " is permanently banned" : " has been banned from this site"))
        );
    }

    /**
     * 设置或取消管理员消息
     */
    public static void sendSetAdminMessage(SocketClient client, int action, String toUid, String toName) {
        if (client == null) {
            return;
        }
        UserBean u = CommonAppConfig.getInstance().getUserBean();
        if (u == null) {
            return;
        }
        String s = action == 1 ? WordUtil.getString(com.livestreaming.common.R.string.live_set_admin) : WordUtil.getString(com.livestreaming.common.R.string.live_set_admin_cancel);
        client.send(new SocketSendBean()
                .param("_method_", Constants.SOCKET_SET_ADMIN)
                .param("action", action)
                .param("msgtype", 1)
                .param("uname", u.getUserNiceName())
                .param("uid", u.getId())
                .param("touid", toUid)
                .param("toname", toName)
                .param(Constants.SOCKET_CT_ZH, StringUtil.contact(toName, action == 1 ? " is set as administrator" : " was removed as administrator"))
                .param(Constants.SOCKET_CT_EN, StringUtil.contact(toName, action == 1 ? " is set as administrator" : " was removed as administrator"))
        );
    }

    /**
     * 超管关闭直播间
     */
    public static void superCloseRoom(SocketClient client) {
        if (client == null) {
            return;
        }
        client.send(new SocketSendBean()
                .param("_method_", Constants.SOCKET_STOP_LIVE)
                .param("action", 19)
                .param("msgtype", 1)
                .param("ct", ""));
    }

    /**
     * 发系统消息
     */
    public static void sendSystemMessage(SocketClient client, String content, String contentEn) {
        if (client == null) {
            return;
        }
        UserBean u = CommonAppConfig.getInstance().getUserBean();
        if (u == null) {
            return;
        }
        client.send(new SocketSendBean()
                .param("_method_", Constants.SOCKET_SYSTEM)
                .param("action", 13)
                .param("msgtype", 4)
                .param("level", u.getLevel())
                .param("uname", u.getUserNiceName())
                .param("uid", u.getId())
                .param(Constants.SOCKET_CT_ZH, content)
                .param(Constants.SOCKET_CT_EN, contentEn)
        );
    }


    /**
     * 获取僵尸粉
     */
    public static void getFakeFans(SocketClient client) {
        if (client == null) {
            return;
        }
        client.send(new SocketSendBean()
                .param("_method_", Constants.SOCKET_FAKE_FANS)
                .param("action", "")
                .param("msgtype", ""));
    }


    /**
     * 更新主播映票数
     */
    public static void sendUpdateVotesMessage(SocketClient client, int votes, int first) {
        if (client == null) {
            return;
        }
        client.send(new SocketSendBean()
                .param("_method_", Constants.SOCKET_UPDATE_VOTES)
                .param("action", 1)
                .param("msgtype", 26)
                .param("votes", votes)
                .param("uid", CommonAppConfig.getInstance().getUid())
                .param("isfirst", first)
                .param("ct", ""));
    }

    /**
     * 更新主播映票数
     */
    public static void sendUpdateVotesMessage(SocketClient client, int votes) {
        sendUpdateVotesMessage(client, votes, 0);
    }

    /**
     * 发送购买守护成功消息
     */
    public static void sendBuyGuardMessage(SocketClient client, String votes, int guardNum, int guardType) {
        if (client == null) {
            return;
        }
        UserBean u = CommonAppConfig.getInstance().getUserBean();
        if (u == null) {
            return;
        }
        client.send(new SocketSendBean()
                .param("_method_", Constants.SOCKET_BUY_GUARD)
                .param("action", 0)
                .param("msgtype", 0)
                .param("uid", u.getId())
                .param("uname", u.getUserNiceName())
                .param("uhead", u.getAvatar())
                .param("uframe", u.getFrame())
                .param("votestotal", votes)
                .param("guard_nums", guardNum)
                .param("guard_type", guardType));
    }

    /**
     * 发送发红包成功消息
     */
    public static void sendRedPackMessage(SocketClient client) {
        if (client == null) {
            return;
        }
        UserBean u = CommonAppConfig.getInstance().getUserBean();
        if (u == null) {
            return;
        }
        client.send(new SocketSendBean()
                .param("_method_", Constants.SOCKET_RED_PACK)
                .param("action", 0)
                .param("msgtype", 0)
                .param("uid", u.getId())
                .param("uname", u.getUserNiceName())
                .param(Constants.SOCKET_CT_ZH, "Red envelopes are distributed in the live broadcast room! Go and grab it~")
                .param(Constants.SOCKET_CT_EN, "give out red envelopes in the live broadcast room! Go and grab it~")
        );

    }


    /**
     * 直播间购物飘屏
     */
    public static void sendBuyMessage(SocketClient client, ArrayList<GoodsBean> goodsBeanList) {
        if (client == null) {
            return;
        }
        UserBean u = CommonAppConfig.getInstance().getUserBean();
        if (u == null) {
            return;
        }
        client.send(new SocketSendBean()
                .param("_method_", Constants.SOCKET_LIVE_GOODS_FLOAT)
                .param("action", 0)
                .param("msgtype", 0)
                .param("uid", u.getId())
                .param("uname", u.getUserNiceName())
                .param("uhead", u.getAvatar())
                .param("uframe", u.getFrame())
                .paramJsonArray("goods_list", new com.google.gson.Gson().toJson(goodsBeanList))
        );

    }

    /**
     * 直播间购物飘屏
     */
    public static void liveGoodsFloat(SocketClient client) {
        if (client == null) {
            return;
        }
        UserBean u = CommonAppConfig.getInstance().getUserBean();
        if (u == null) {
            return;
        }
        client.send(new SocketSendBean()
                .param("_method_", Constants.SOCKET_LIVE_GOODS_FLOAT)
                .param("action", 0)
                .param("msgtype", 0)
                .param("uid", u.getId())
                .param("uname", u.getUserNiceName())
                .param("ct", "")
        );

    }


    /**
     * 发送展示直播间商品的消息
     */
    public static void sendLiveGoodsShow(SocketClient client, GoodsBean bean) {
        if (client == null) {
            return;
        }
        if (bean == null) {
            client.send(new SocketSendBean()
                    .param("_method_", Constants.SOCKET_LIVE_GOODS_SHOW)
                    .param("action", 0)
                    .param("msgtype", 0)
            );
        } else {
            client.send(new SocketSendBean()
                    .param("_method_", Constants.SOCKET_LIVE_GOODS_SHOW)
                    .param("action", 1)
                    .param("msgtype", 0)
                    .param("goodsid", bean.getId())
                    .param("goods_thumb", bean.getThumb())
                    .param("goods_name", bean.getName())
                    .param("goods_price", bean.getPriceNow())
                    .param("goods_type", bean.getType())
            );
        }

    }

    /**
     * 星球探宝中奖
     */
    public static void gameXqtbWin(SocketClient client, String list) {
        if (client == null) {
            return;
        }
        UserBean u = CommonAppConfig.getInstance().getUserBean();
        if (u == null) {
            return;
        }
        client.send(new SocketSendBean()
                .param("_method_", Constants.SOCKET_XQTB_WIN)
                .paramJsonArray("list", list)
        );

    }

    /**
     * 幸运大转盘中奖
     */
    public static void gameLuckpanWin(SocketClient client, String list) {
        if (client == null) {
            return;
        }
        UserBean u = CommonAppConfig.getInstance().getUserBean();
        if (u == null) {
            return;
        }
        client.send(new SocketSendBean()
                .param("_method_", Constants.SOCKET_LUCKPAN_WIN)
                .paramJsonArray("list", list)
        );

    }

    public static void sendLightsMessage(SocketClient mSocketClient, int clickCount, int isPk) {
        UserBean u = CommonAppConfig.getInstance().getUserBean();
        if (u == null) {
            return;
        }
        mSocketClient.send(new SocketSendBean()
                .param("_method_", Constants.SOCKET_LIGHT)
                .param("action", 3)
                .param("msgtype", 2)
                .param("level", u.getLevel())
                .param("uname", u.getUserNiceName())
                .param("uid", u.getId())
                .param("liangname", u.getGoodName())
                .param("vip_type", u.getVip().getType())
                .param("uhead", u.getAvatar())
                .param("uframe", u.getFrame())
                .param(Constants.SOCKET_CT_ZH, u.getUserNiceName() + " light up " + clickCount + " Times")
                .param(Constants.SOCKET_CT_EN, u.getUserNiceName() + " light up " + clickCount + " Times"));
    }

    public static void sendTotalLights(SocketClient mSocketClient, int total) {
        mSocketClient.send(new SocketSendBean()
                .param("_method_", Constants.SOCKET_LIGHT)
                .param("action", 4)
                .param("msgtype", 2)
                .param("total", total));
    }

    public static void liveLike(SocketClient client,String mLiveUid, String mStream, int clickCount) {
        try {
            client.send(new SocketSendBean()
                    .param("_method_", Constants.SockitLiveLike)
                    .param("action", 1)
                    .param("msgtype", 0)
                    .param("liveuid", mLiveUid)
                    .param("stream", mStream)
                    .param("likes", clickCount)
            );
        }catch (Exception e){

        }
    }
}
