package com.livestreaming.live.bean;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.alibaba.fastjson.annotation.JSONField;
import com.livestreaming.common.utils.WordUtil;
import com.livestreaming.live.R;

import java.util.ArrayList;

/**
 * Created by cxf on 2017/8/9.
 */

public class LiveBean implements Parcelable {
    private String uid;
    private String avatar;
    private String avatarThumb;
    private String userNiceName;
    private String title;
    private String city;
    private String stream;
    private String pull;
    private String thumb;
    private String nums;
    private String country_img;

    public String getCountry_img() {
        return country_img;
    }

    public void setCountry_img(String country_img) {
        this.country_img = country_img;
    }

    private int sex;
    private int have_guests;

    public int getHave_guests() {
        return have_guests;
    }

    public void setHave_guests(int have_guests) {
        this.have_guests = have_guests;
    }

    public int getIsPk() {
        return isPk;
    }

    public void setIsPk(int isPk) {
        this.isPk = isPk;
    }

    private int isPk;
    private String distance;
    private int levelAnchor;
    private int type;
    private String typeVal;
    private String goodNum;//主播的靓号
    private int gameAction;//正在进行的游戏的标识
    private String game;
    private int isshop;
    private int mRecommend;
    private int mIsVoice;
    private int anyway;//1横屏 0竖屏
    private String mFrame;
    private int total_like;

    public int getTotal_like() {
        return total_like;
    }

    public void setTotal_like(int total_like) {
        this.total_like = total_like;
    }

    public ArrayList<LiveBean>headItems=new ArrayList<>();

    @JSONField(name = "guests")
    public ArrayList<ItemGuest> getGuests() {
        return guests;
    }
    @JSONField(name = "guests")
    public void setGuests(ArrayList<ItemGuest> guests) {
        this.guests = guests;
    }

    private ArrayList<ItemGuest> guests;


    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    @JSONField(name = "avatar_thumb")
    public String getAvatarThumb() {
        return avatarThumb;
    }

    @JSONField(name = "avatar_thumb")
    public void setAvatarThumb(String avatarThumb) {
        this.avatarThumb = avatarThumb;
    }

    @JSONField(name = "user_nickname")
    public String getUserNiceName() {
        return userNiceName;
    }

    @JSONField(name = "user_nickname")
    public void setUserNiceName(String userNiceName) {
        this.userNiceName = userNiceName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStream() {
        return stream;
    }

    public void setStream(String stream) {
        this.stream = stream;
    }

    public String getPull() {
        return pull;
    }

    public void setPull(String pull) {
        this.pull = pull;
    }

    public String getThumb() {
        return thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }


    public String getNums() {
        return nums;
    }

    public void setNums(String nums) {
        this.nums = nums;
    }

    @JSONField(name = "level_anchor")
    public int getLevelAnchor() {
        return levelAnchor;
    }

    @JSONField(name = "level_anchor")
    public void setLevelAnchor(int levelAnchor) {
        this.levelAnchor = levelAnchor;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @JSONField(name = "type_val")
    public String getTypeVal() {
        return typeVal;
    }

    @JSONField(name = "type_val")
    public void setTypeVal(String typeVal) {
        this.typeVal = typeVal;
    }

    @JSONField(name = "goodnum")
    public String getGoodNum() {
        return goodNum;
    }

    @JSONField(name = "goodnum")
    public void setGoodNum(String goodNum) {
        this.goodNum = goodNum;
    }

    @JSONField(name = "game_action")
    public int getGameAction() {
        return gameAction;
    }

    @JSONField(name = "game_action")
    public void setGameAction(int gameAction) {
        this.gameAction = gameAction;
    }

    public String getGame() {
        return game;
    }

    public void setGame(String game) {
        this.game = game;
    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    /**
     * 显示靓号
     */
    public String getLiangNameTip() {
        if (!TextUtils.isEmpty(this.goodNum) && !"0".equals(this.goodNum)) {
            return WordUtil.getString(com.livestreaming.common.R.string.live_liang) + ":" + this.goodNum;
        }
        return "ID:" + this.uid;
    }

    public LiveBean() {

    }


    public int getIsshop() {
        return isshop;
    }

    public void setIsshop(int isshop) {
        this.isshop = isshop;
    }

    @JSONField(name = "isrecommend")
    public int getRecommend() {
        return mRecommend;
    }
    @JSONField(name = "isrecommend")
    public void setRecommend(int recommend) {
        mRecommend = recommend;
    }
    @JSONField(name = "live_type")
    public int getIsVoice() {
        return mIsVoice;
    }
    @JSONField(name = "live_type")
    public void setIsVoice(int isVoice) {
        mIsVoice = isVoice;
    }

    public boolean isVoiceRoom(){
        return mIsVoice==1;
    }

    public int getAnyway() {
        return anyway;
    }

    public void setAnyway(int anyway) {
        this.anyway = anyway;
    }

    private LiveBean(Parcel in) {
        this.uid = in.readString();
        this.avatar = in.readString();
        this.avatarThumb = in.readString();
        this.userNiceName = in.readString();
        this.sex = in.readInt();
        this.title = in.readString();
        this.city = in.readString();
        this.stream = in.readString();
        this.pull = in.readString();
        this.thumb = in.readString();
        this.nums = in.readString();
        this.distance = in.readString();
        this.levelAnchor = in.readInt();
        this.type = in.readInt();
        this.typeVal = in.readString();
        this.goodNum = in.readString();
        this.gameAction = in.readInt();
        this.game = in.readString();
        this.isshop = in.readInt();
        this.mRecommend = in.readInt();
        this.mIsVoice = in.readInt();
        this.anyway = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.uid);
        dest.writeString(this.avatar);
        dest.writeString(this.avatarThumb);
        dest.writeString(this.userNiceName);
        dest.writeInt(this.sex);
        dest.writeString(this.title);
        dest.writeString(this.city);
        dest.writeString(this.stream);
        dest.writeString(this.pull);
        dest.writeString(this.thumb);
        dest.writeString(this.nums);
        dest.writeString(this.distance);
        dest.writeInt(this.levelAnchor);
        dest.writeInt(this.type);
        dest.writeString(this.typeVal);
        dest.writeString(this.goodNum);
        dest.writeInt(this.gameAction);
        dest.writeString(this.game);
        dest.writeInt(this.isshop);
        dest.writeInt(this.mRecommend);
        dest.writeInt(this.mIsVoice);
        dest.writeInt(this.anyway);
    }

    public static final Creator<LiveBean> CREATOR = new Creator<LiveBean>() {
        @Override
        public LiveBean[] newArray(int size) {
            return new LiveBean[size];
        }

        @Override
        public LiveBean createFromParcel(Parcel in) {
            return new LiveBean(in);
        }
    };

    @Override
    public String toString() {
        return "uid: " + uid + " , userNiceName: " + userNiceName + " ,playUrl: " + pull;
    }

    public String getFrame() {
        return mFrame;
    }
    public void setFrame(String f) {
        mFrame=f;
    }

    private int isTop;
    public int getTrendId() {
        return isTop;
    }
    public void setIsTop(int isTop) {
        this.isTop=isTop;
    }
}
