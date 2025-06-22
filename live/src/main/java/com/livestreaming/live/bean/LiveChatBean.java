package com.livestreaming.live.bean;

import com.alibaba.fastjson.annotation.JSONField;
import java.io.Serializable;

/**
 * Created by cxf on 2017/8/22.
 */

public class LiveChatBean implements Serializable {

    public static final int NORMAL = 0;
    public static final int SYSTEM = 1;
    public static final int GIFT = 2;
    public static final int ENTER_ROOM = 3;
    public static final int LIGHT = 4;
    public static final int RED_PACK = 5;

    private String id;
    private String userNiceName;
    private int level;
    private String content;
    public String avatar=null;
    public String frame=null;
    private int heart;
    private int type; //0是普通消息  1是系统消息 2是礼物消息
    private String liangName;
    private int vipType;
    private int guardType;
    private boolean anchor;
    private boolean manager;
    
    // Reply functionality fields
    private String replyToId;           // ID of the message being replied to
    private String replyToUserId;       // User ID who sent the original message
    private String replyToUserName;     // Username who sent the original message
    private String replyToContent;      // Content of the original message being replied to
    private String replyToAvatar;       // Avatar of user being replied to
    private long timestamp;             // Timestamp for message ordering
    private String messageId;           // Unique message ID

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @JSONField(name = "user_nickname")
    public String getUserNiceName() {
        return userNiceName;
    }

    @JSONField(name = "user_nickname")
    public void setUserNiceName(String userNiceName) {
        this.userNiceName = userNiceName;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getHeart() {
        return heart;
    }

    public void setHeart(int heart) {
        this.heart = heart;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @JSONField(name = "liangname")
    public String getLiangName() {
        return liangName;
    }

    @JSONField(name = "liangname")
    public void setLiangName(String liangName) {
        if(!"0".equals(liangName)){
            this.liangName = liangName;
        }
    }

    public boolean isAnchor() {
        return anchor;
    }

    public void setAnchor(boolean anchor) {
        this.anchor = anchor;
    }

    @JSONField(name = "vip_type")
    public int getVipType() {
        return vipType;
    }

    @JSONField(name = "vip_type")
    public void setVipType(int vipType) {
        this.vipType = vipType;
    }

    public boolean isManager() {
        return manager;
    }

    public void setManager(boolean manager) {
        this.manager = manager;
    }

    @JSONField(name = "guard_type")
    public int getGuardType() {
        return guardType;
    }

    @JSONField(name = "guard_type")
    public void setGuardType(int guardType) {
        this.guardType = guardType;
    }

    // Reply functionality getters and setters
    
    @JSONField(name = "reply_to_id")
    public String getReplyToId() {
        return replyToId;
    }

    @JSONField(name = "reply_to_id")
    public void setReplyToId(String replyToId) {
        this.replyToId = replyToId;
    }

    @JSONField(name = "reply_to_user_id")
    public String getReplyToUserId() {
        return replyToUserId;
    }

    @JSONField(name = "reply_to_user_id")
    public void setReplyToUserId(String replyToUserId) {
        this.replyToUserId = replyToUserId;
    }

    @JSONField(name = "reply_to_user_name")
    public String getReplyToUserName() {
        return replyToUserName;
    }

    @JSONField(name = "reply_to_user_name")
    public void setReplyToUserName(String replyToUserName) {
        this.replyToUserName = replyToUserName;
    }

    @JSONField(name = "reply_to_content")
    public String getReplyToContent() {
        return replyToContent;
    }

    @JSONField(name = "reply_to_content")
    public void setReplyToContent(String replyToContent) {
        this.replyToContent = replyToContent;
    }

    @JSONField(name = "reply_to_avatar")
    public String getReplyToAvatar() {
        return replyToAvatar;
    }

    @JSONField(name = "reply_to_avatar")
    public void setReplyToAvatar(String replyToAvatar) {
        this.replyToAvatar = replyToAvatar;
    }

    @JSONField(name = "timestamp")
    public long getTimestamp() {
        return timestamp;
    }

    @JSONField(name = "timestamp")
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @JSONField(name = "message_id")
    public String getMessageId() {
        return messageId;
    }

    @JSONField(name = "message_id")
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    /**
     * Check if this message is a reply to another message
     */
    public boolean isReply() {
        return replyToId != null && !replyToId.isEmpty();
    }
}
