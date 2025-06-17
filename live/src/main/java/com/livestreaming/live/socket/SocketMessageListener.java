package com.livestreaming.live.socket;

import com.alibaba.fastjson.JSONObject;
import com.livestreaming.common.bean.GoodsBean;
import com.livestreaming.common.bean.UserBean;
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

import java.util.List;

/**
 * Created by cxf on 2017/8/22.
 * 直播间通用的socket逻辑
 */

public interface SocketMessageListener {

    /**
     * 直播间  连接成功socket后调用
     */
    void onConnect(boolean successConn);

    /**
     * 直播间  自己的socket断开
     */
    void onDisConnect();

    /**
     * 直播间  收到聊天消息
     */
    void onChat(LiveChatBean bean);

    /**
     * 直播间  收到飘心消息
     */
    void onLight();

    /**
     * 直播间  收到用户进房间消息
     */
    void onEnterRoom(LiveEnterRoomBean bean);

    /**
     * 直播间  收到用户离开房间消息
     */
    void onLeaveRoom(UserBean bean);

    /**
     * 直播间  收到送礼物消息
     *
     * @param bean 礼物信息
     */
    void onSendGift(LiveReceiveGiftBean bean, LiveChatBean chatBean);

    /**
     * @param leftGift  左边的映票数
     * @param rightGift 右边的映票数
     */

    void onSendGiftPk(long leftGift, long rightGift);

    /**
     * 直播间  收到弹幕消息
     */
    void onSendDanMu(LiveDanMuBean bean);

    /**
     * 直播间  观众收到直播结束消息
     */
    void onLiveEnd();

    /**
     * 直播间  主播登录失效
     */
    void onAnchorInvalid();

    /**
     * 直播间  超管关闭直播间
     */
    void onSuperCloseLive();

    /**
     * 直播间  踢人
     */
    void onKick(String touid);

    /**
     * 直播间  禁言
     */
    void onShutUp(String touid, String content);

    /**
     * 直播间  设置或取消管理员
     */
    void onSetAdmin(String toUid, int isAdmin);

    /**
     * 直播间  主播切换计时收费或更改计时收费价格的时候执行
     */
    void onChangeTimeCharge(int typeVal);

    /**
     * 直播间  主购买守护的时候，更新主播映票数
     */
    void onUpdateVotes(String uid, String addVotes, int first);

    /**
     * 直播间  添加僵尸粉
     */
    void addFakeFans(List<LiveUserGiftBean> list);

    /**
     * 直播间  收到购买守护消息
     */
    void onBuyGuard(LiveBuyGuardMsgBean bean);

    /**
     * 直播间 收到红包消息
     */
    void onRedPack(LiveChatBean liveChatBean);

    /***********************以下是观众与主播连麦*********************************/

    /**
     * 连麦  主播收到观众申请连麦的回调
     */
    void onAudienceApplyLinkMic(UserBean u);

    /**
     * 连麦  主播同意连麦的回调
     */
    void onAnchorAcceptLinkMic(String touid);

    /**
     * 连麦  观众收到主播拒绝连麦的回调
     */
    void onAnchorRefuseLinkMic();

    /**
     * 连麦  所有人收到连麦观众发过来的播流地址的回调
     */
    void onAudienceSendLinkMicUrl(String uid, String uname, String playUrl);

    /**
     * 连麦  所有人收到主播关闭连麦的回调
     */
    void onAnchorCloseLinkMic(String touid, String uname);

    /**
     * 连麦  所有人收到已连麦观众关闭连麦的回调
     */
    void onAudienceCloseLinkMic(String uid, String uname);

    /**
     * 连麦  观众申请连麦时，收到主播无响应的回调
     */
    void onAnchorNotResponse();

    /**
     * 连麦  观众申请连麦时，收到主播正在忙的回调
     */
    void onAnchorBusy();

    /**
     * 连麦  已连麦用户退出直播间的回调
     */
    void onAudienceLinkMicExitRoom(String touid);

    /***********************以下是主播连麦*********************************/

    /**
     * 主播与主播连麦  主播收到其他主播发过来的连麦申请的回调
     *
     * @param u      对方主播的信息
     * @param stream 对方主播的stream
     */
    void onLinkMicAnchorApply(UserBean u, String stream);

    /**
     * 主播与主播连麦 所有人收到对方主播的播流地址的回调
     *
     * @param playUrl 对方主播的播流地址
     */
    void onLinkMicAnchorPlayUrl(String uid,String pkUid,String pkStream, String playUrl,int win1,int win2);

    /**
     * 主播与主播连麦  断开连麦pk的回调
     */
    void onLinkMicAnchorClose();

    /**
     * 主播与主播连麦  对方主播拒绝连麦pk的回调
     */
    void onLinkMicAnchorRefuse();

    /**
     * 主播与主播连麦  对方主播正在忙的回调
     */
    void onLinkMicAnchorBusy();

    /**
     * 主播与主播连麦  对方主播无响应的回调
     */
    void onLinkMicAnchorNotResponse();

    /**
     * 主播与主播连麦  对方主播正在玩游戏
     */
    void onlinkMicPlayGaming();

    /***********************以下是主播PK*********************************/
    /**
     * 主播与主播PK  主播收到对方主播发过来的PK申请的回调
     *
     * @param u      对方主播的信息
     * @param stream 对方主播的stream
     */
    void onLinkMicPkApply(UserBean u, String stream);

    /**
     * 主播与主播PK 所有人收到PK开始的回调
     */
    void onLinkMicPkStart(String pkUid);

    /**
     * 主播与主播PK  断开连麦pk的回调
     */
    void onLinkMicPkClose();

    /**
     * 主播与主播PK  对方主播拒绝pk的回调
     */
    void onLinkMicPkRefuse();

    /**
     * 主播与主播PK   对方主播正在忙的回调
     */
    void onLinkMicPkBusy();

    /**
     * 主播与主播PK   对方主播无响应的回调
     */
    void onLinkMicPkNotResponse();

    /**
     * 主播与主播PK   所有人收到PK结果的回调
     */
    void onLinkMicPkEnd(String winUid,int win_count);


    /**
     * 幸运礼物中奖
     */
    void onLuckGiftWin(LiveLuckGiftWinBean bean);

    /**
     * 奖池中奖
     */
    void onPrizePoolWin(LiveGiftPrizePoolWinBean bean);

    /**
     * 奖池升级
     */
    void onPrizePoolUp(String level);


    /**
     * 全站礼物
     */
    void onGlobalGift(GlobalGiftBean bean);

    /**
     * 直播间商品展示
     */
    void onLiveGoodsShow(GoodsBean bean);

    /**
     * 直播间购物飘屏
     */
    void onLiveGoodsFloat(String userName);


    /**
     * 语音聊天室--主播收到观众申请上麦
     */
    void onVoiceRoomApplyUpMic();


    /**
     * 语音聊天室--观众收到主播同意或拒绝上麦的消息
     *
     * @param toUid    上麦的人的uid
     * @param toName   上麦的人的name
     * @param toAvatar 上麦的人的头像
     * @param position 上麦的人的麦位，从0开始 -1表示拒绝上麦
     * @param frame
     */
    void onVoiceRoomHandleApply(String toUid, String toName, String toAvatar, int position, String frame);

    /**
     * 语音聊天室--所有人收到某人下麦的消息
     *
     * @param uid  下麦的人的uid
     * @param type 0自己主动下麦  1被主播下麦  2被管理员下麦
     */
    void onVoiceRoomDownMic(String uid, int type);


    /**
     * 语音聊天室--主播控制麦位 闭麦开麦禁麦等
     *
     * @param uid      被操作人的uid
     * @param position 麦位
     * @param status   麦位的状态 -1 关麦；  0无人； 1开麦 ； 2 禁麦；
     */
    void onControlMicPosition(String uid, int position, int status);


    /**
     * 语音聊天室--观众上麦后推流成功，把自己的播放地址广播给所有人
     *
     * @param uid        上麦观众的uid
     * @param pull       上麦观众的播流地址
     * @param userStream 上麦观众的流名，主播混流用
     */
    void onVoiceRoomPushSuccess(String uid, String pull, String userStream);


    /**
     * 语音聊天室--收到上麦观众发送表情的消息
     *
     * @param uid       上麦观众的uid
     * @param faceIndex 表情标识
     */
    void onVoiceRoomFace(String uid, int faceIndex);
    void onVoiceRoomUserCam(String uid, int type,boolean isFromU);


    /**
     * 直播间警告
     *
     * @param warningTip 警告提示语
     */
    void onLiveRoomWarning(String warningTip);

    /**
     * 星球探宝中奖
     */
    void onGameXqtbWin(String json);
    /**
     * 幸运大转盘中奖
     */
    void onGameLuckpanWin(String json);
   void  onUserUpVoiceLinkMicDirect(UserBean bean);

/***********************以下是游戏*********************************/
    /**
     * 游戏  智勇三张 游戏的回调
     */
    void onGameZjh(JSONObject obj);

    /**
     * 游戏  幸运转盘 游戏的回调
     */
    void onGameZp(JSONObject obj);

    void onLiveHostSwitchCam(int type,String uid,String thumb);

    void onGestCloseOpenCam(String touid, String avatar, Integer isOn);
    void handleMicGests(String touid,Integer isOn);

    void onAnchorCloseAudianceLinkMic(String uid, String uname);

    void onAnchorCloseLive();

    void onRoomBackgroundChanged(String backUrl);

    void onAdminAgreeUpLinkMicc(UserBean bean, int isAgree);

    void onAdminControlMicc(LiveVoiceControlBean beann);

    void onAdminCloseMicc(LiveVoiceControlBean beann2);

    void onUpdateGuestIncome(int uid, int income);

    void onTotalLikesCount(int total);

    void onAncorCancelPk(String winUid, int winCount);

    void onPkBouns(int duration, int startAfter, int x, int messionName,int target_value,int targetDuration);

    void updatePkBouns(int value, int uid, int completed);

    void onSendGiftLiveResponse(String coin, int level, int type);

    void handleDoubleTotalPoints(int totalPoints, int uid);

    void handleWhenEnterRoomDouble(int uid, int x, int missionType, int duration, int startAfter, int doubleSec, int currentTime, int state,int target_value,int targetValueDefault,int completed);

    void onChangeRoomGuetEnabled(int isEnabled);
}
