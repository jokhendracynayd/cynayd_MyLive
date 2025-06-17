package com.livestreaming.live.socket;

import com.livestreaming.common.CommonAppConfig;
import com.livestreaming.common.Constants;
import com.livestreaming.common.bean.UserBean;
import com.livestreaming.live.bean.LiveVoiceControlBean;
import com.google.gson.Gson;

/**
 * Created by cxf on 2018/10/25.
 */

public class SocketLinkMicUtil {

    /**
     * 观众申请连麦
     */
    public static void audienceApplyLinkMic(SocketClient client) {
        if (client == null) {
            return;
        }
        UserBean u = CommonAppConfig.getInstance().getUserBean();
        if (u == null) {
            return;
        }
        client.send(new SocketSendBean()
                .param("_method_", Constants.SOCKET_LINK_MIC)
                .param("action", 1)
                .param("msgtype", 10)
                .param("uid", u.getId())
                .param("uname", u.getUserNiceName())
                .param("level", u.getLevel())
                .param("sex", u.getSex())
                .param("mFrame", u.getFrame())
                .param("uhead", u.getAvatar())
                .param("ct", ""));
    }

    /**
     * 主播同意观众连麦请求
     */
    public static void anchorAcceptLinkMic(SocketClient client, String toUid) {
        if (client == null) {
            return;
        }
        UserBean u = CommonAppConfig.getInstance().getUserBean();
        if (u == null) {
            return;
        }
        client.send(new SocketSendBean()
                .param("_method_", Constants.SOCKET_LINK_MIC)
                .param("action", 2)
                .param("msgtype", 10)
                .param("uid", u.getId())
                .param("uname", u.getUserNiceName())
                .param("level", u.getLevel())
                .param("touid", toUid)
                .param("ct", ""));
    }


    /**
     * 主播拒绝观众连麦请求
     */
    public static void anchorRefuseLinkMic(SocketClient client, String touid) {
        if (client == null) {
            return;
        }
        UserBean u = CommonAppConfig.getInstance().getUserBean();
        if (u == null) {
            return;
        }
        client.send(new SocketSendBean()
                .param("_method_", Constants.SOCKET_LINK_MIC)
                .param("action", 3)
                .param("msgtype", 10)
                .param("uid", u.getId())
                .param("uname", u.getUserNiceName())
                .param("level", u.getLevel())
                .param("touid", touid)
                .param("ct", ""));
    }

    /**
     * 主播同意连麦后，观众把自己的流地址发送给主播
     */
    public static void audienceSendLinkMicUrl(SocketClient client, String playUrl) {
        if (client == null) {
            return;
        }
        UserBean u = CommonAppConfig.getInstance().getUserBean();
        if (u == null) {
            return;
        }
        client.send(new SocketSendBean()
                .param("_method_", Constants.SOCKET_LINK_MIC)
                .param("action", 4)
                .param("msgtype", 10)
                .param("uid", u.getId())
                .param("ct", "")
                .param("uname", u.getUserNiceName())
                .param("level", u.getLevel())
                .param("playurl", playUrl));
    }

    /**
     * 观众断开连麦
     */
    public static void audienceCloseLinkMic(SocketClient client) {
        if (client == null) {
            return;
        }
        UserBean u = CommonAppConfig.getInstance().getUserBean();
        if (u == null) {
            return;
        }
        client.send(new SocketSendBean()
                .param("_method_", Constants.SOCKET_LINK_MIC)
                .param("action", 5)
                .param("msgtype", 10)
                .param("uid", u.getId())
                .param("ct", "")
                .param("uname", u.getUserNiceName()));
    }
    public static void audienceCloseLinkMic(SocketClient client,String uid,String uname) {
        if (client == null) {
            return;
        }
        client.send(new SocketSendBean()
                .param("_method_", Constants.SOCKET_LINK_MIC)
                .param("action", 5)
                .param("msgtype", 10)
                .param("uid", uid)
                .param("ct", "")
                .param("uname", uname));
    }

    /**
     * 主播断开某人的连麦
     */
    public static void anchorCloseLinkMic(SocketClient client, String touid, String uname) {
        if (client == null) {
            return;
        }
        client.send(new SocketSendBean()
                .param("_method_", Constants.SOCKET_LINK_MIC)
                .param("action", 6)
                .param("msgtype", 10)
                .param("touid", touid)
                .param("ct", "")
                .param("uname", uname));
    }


    /**
     * 主播正在忙
     */
    public static void anchorBusy(SocketClient client, String touid) {
        if (client == null) {
            return;
        }
        client.send(new SocketSendBean()
                .param("_method_", Constants.SOCKET_LINK_MIC)
                .param("action", 7)
                .param("msgtype", 10)
                .param("touid", touid));
    }

    /**
     * 主播未响应
     */
    public static void anchorNotResponse(SocketClient client, String touid) {
        if (client == null) {
            return;
        }
        client.send(new SocketSendBean()
                .param("_method_", Constants.SOCKET_LINK_MIC)
                .param("action", 8)
                .param("msgtype", 10)
                .param("touid", touid));
    }

    public static void onMeAsGestCloseOpenCam(SocketClient client,boolean b,int position) {
        UserBean u = CommonAppConfig.getInstance().getUserBean();
        if (u == null) {
            return;
        }
        client.send(new SocketSendBean()
                .param("_method_", Constants.SOCKET_LINK_MIC)
                .param("action", 12)
                .param("msgtype", 10)
                .param("touid", u.getId())
                .param("avatar", u.getAvatar())
                .param("isOn", b?1:0));
    }
    public static void onGestsCloseOpenMic(SocketClient client,boolean b,int position) {
        UserBean u = CommonAppConfig.getInstance().getUserBean();
        if (u == null) {
            return;
        }
        client.send(new SocketSendBean()
                .param("_method_", Constants.SOCKET_LINK_MIC)
                .param("action", 13)
                .param("msgtype", 10)
                .param("touid", u.getId())
                .param("avatar", u.getAvatar())
                .param("isOn", b?1:0));
    }

    public static void onAnchoreCloseLive(SocketClient mSocketClient) {
        mSocketClient.send(new SocketSendBean()
                .param("_method_", Constants.SOCKET_LINK_MIC)
                .param("action", 14)
                .param("msgtype", 10));
    }

    public static void onAdminAgreeUpLinkMic(SocketClient mSocketClient,UserBean bean, int isAgree) {
        mSocketClient.send(new SocketSendBean()
                .param("_method_", Constants.SOCKET_LINK_MIC)
                .param("action", 16)
                .param("bean", new Gson().toJson(bean))
                .param("isAgree", isAgree));
    }

    public static void onAdminControlClick(SocketClient mSocketClient, LiveVoiceControlBean bean) {
        mSocketClient.send(new SocketSendBean()
                .param("_method_", Constants.SOCKET_LINK_MIC)
                .param("action", 17)
                .param("bean", new Gson().toJson(bean)));
    }

    public static void onAdminControlCloseVoiceMic(SocketClient mSocketClient, LiveVoiceControlBean bean) {
        mSocketClient.send(new SocketSendBean()
                .param("_method_", Constants.SOCKET_LINK_MIC)
                .param("action", 18)
                .param("bean", new Gson().toJson(bean)));
    }

    public static void sendUpdateGuestIncome(SocketClient mSocketClient, int uid, int income) {
        mSocketClient.send(new SocketSendBean()
                .param("_method_", Constants.SOCKET_LINK_MIC)
                .param("action", 19)
                .param("uid", uid)
                .param("income", income));
    }
}
