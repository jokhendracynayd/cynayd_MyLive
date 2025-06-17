package com.livestreaming.live.socket;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.livestreaming.common.CommonAppConfig;
import com.livestreaming.common.Constants;
import com.livestreaming.common.bean.UserBean;
import com.livestreaming.live.bean.ChangeRoomBackBean;

/**
 * Created by cxf on 2018/11/16.
 * 语音聊天室
 */

public class SocketVoiceRoomUtil {

    /**
     * 用户发起上麦申请
     */
    public static void applyMicUp(SocketClient client) {
        if (client == null) {
            return;
        }
        client.send(new SocketSendBean()
                .param("_method_", Constants.SOCKET_VOICE_ROOM)
                .param("action", 1));
    }


    /**
     * 主播同意或拒绝用户的上麦申请
     *
     * @param position 上麦的位置 从0开始 -1表示拒绝
     */
    public static void handleMicUpApply(SocketClient client, UserBean bean, int position) {
        if (client == null) {
            return;
        }
        client.send(new SocketSendBean()
                .param("_method_", Constants.SOCKET_VOICE_ROOM)
                .param("action", 2)
                .param("touid", bean.getId())
                .param("toname", bean.getUserNiceName())
                .param("avatar", bean.getAvatar())
                .param("frame", bean.getFrame())
                .param("position", position)
        );
    }

    /**
     * 观众下麦
     *
     * @param uid 下麦的人的uid
     */
    public static void userDownMic(SocketClient client, String uid, int type) {
        if (client == null) {
            return;
        }
        client.send(new SocketSendBean()
                .param("_method_", Constants.SOCKET_VOICE_ROOM)
                .param("action", 3)
                .param("uid", uid)
                .param("type", type)
        );
    }

    public static void changeRoomBack(SocketClient client, ChangeRoomBackBean bean) {
        if (client == null) {
            return;
        }
        client.send(new SocketSendBean()
                .param("_method_", Constants.SOCKET_VOICE_ROOM)
                .param("action", 12)
                .param("msgtype", 0)
                .param("backUrl", bean.getThumb()));
    }

    public static void userCamera(SocketClient client, String uid, int type, int cam_type) {
        if (client == null) {
            return;
        }
        client.send(new SocketSendBean()
                .param("_method_", Constants.SOCKET_VOICE_ROOM)
                .param("action", 7)
                .param("uid", uid)
                .param("cam_type", cam_type)
                .param("type", type)
        );
    }

    public static void userCameraLive(SocketClient client, String pkUid, String uid, int type, int cam_type, String thumb) {
        if (client == null) {
            return;
        }
        if (pkUid != null && !pkUid.isEmpty()) {
            client.send(new SocketSendBean()
                    .param("_method_", Constants.SOCKET_VOICE_ROOM)
                    .param("action", 7)
                    .param("uid", uid)
                    .param("cam_type", cam_type)
                    .param("type", type)
                    .param("thumb", thumb)
                    .param("pk_roomnum", pkUid)
            );
        } else {
            client.send(new SocketSendBean()
                    .param("_method_", Constants.SOCKET_VOICE_ROOM)
                    .param("action", 7)
                    .param("uid", uid)
                    .param("cam_type", cam_type)
                    .param("type", type)
                    .param("thumb", thumb)
            );
        }
    }


    /**
     * 主播控制麦位 闭麦开麦禁麦等
     *
     * @param uid      被操作人的uid
     * @param position 麦位
     * @param status   麦位的状态 -1 关麦；  0无人； 1开麦 ； 2 禁麦；
     */
    public static void controlMicPosition(SocketClient client, String uid, int position, int status) {
        if (client == null) {
            return;
        }
        client.send(new SocketSendBean()
                .param("_method_", Constants.SOCKET_VOICE_ROOM)
                .param("action", 4)
                .param("uid", TextUtils.isEmpty(uid) ? "0" : uid)
                .param("position", position)
                .param("status", status)
        );
    }

    /**
     * 观众上麦后推流成功，把自己的播放地址广播给所有人
     */
    public static void userPushSuccess(SocketClient client, String pull, String userStream) {
        if (client == null) {
            return;
        }
        client.send(new SocketSendBean()
                .param("_method_", Constants.SOCKET_VOICE_ROOM)
                .param("action", 5)
                .param("uid", CommonAppConfig.getInstance().getUid())
                .param("pull", pull)
                .param("user_stream", userStream)
        );
    }

    /**
     * 发送表情
     *
     * @param faceIndex 表情标识
     */
    public static void voiceRoomSendFace(SocketClient client, int faceIndex) {
        if (client == null) {
            return;
        }
        client.send(new SocketSendBean()
                .param("_method_", Constants.SOCKET_VOICE_ROOM)
                .param("action", 6)
                .param("uid", CommonAppConfig.getInstance().getUid())
                .param("face", faceIndex));
    }


    public static void enableDisableRequestGuest(SocketClient client, boolean isEnabled) {
        if (client == null) {
            return;
        }
        client.send(new SocketSendBean()
                .param("_method_", Constants.SOCKET_VOICE_ROOM)
                .param("action", 10)
                .param("uid", CommonAppConfig.getInstance().getUid())
                .param("isEnabled", isEnabled ? 1 : 0));
    }

    public static void onUserUpMicDirect(SocketClient client) {
        if (client == null) {
            return;
        }
        client.send(new SocketSendBean()
                .param("_method_", Constants.SOCKET_VOICE_ROOM)
                .param("action", 14)
                .param("user", new Gson().toJson(CommonAppConfig.getInstance().getUserBean())));
    }
}
