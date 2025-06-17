package com.livestreaming.live.bean;

/**
 * 创建日期：2024/8/20 16:08
 *
 * @author hongchen
 * @version 1.0
 * 包名： com.livestreaming.live.bean
 * 类说明：百顺游戏配置参数
 */
public class GameBaishunConfigDTO {
    private String appChannel;
    private String appId;
    private String userId;
    private String code;
    private String roomId;
    private Integer gameMode;
    private Integer language;
    private GameConfigDTO gameConfig;
    private Integer gsp;

    public String getAppChannel() {
        return appChannel;
    }

    public void setAppChannel(String appChannel) {
        this.appChannel = appChannel;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public Integer getGameMode() {
        return gameMode;
    }

    public void setGameMode(Integer gameMode) {
        this.gameMode = gameMode;
    }

    public Integer getLanguage() {
        return language;
    }

    public void setLanguage(Integer language) {
        this.language = language;
    }

    public GameConfigDTO getGameConfig() {
        return gameConfig;
    }

    public void setGameConfig(GameConfigDTO gameConfig) {
        this.gameConfig = gameConfig;
    }

    public Integer getGsp() {
        return gsp;
    }

    public void setGsp(Integer gsp) {
        this.gsp = gsp;
    }

    public static class GameConfigDTO {
        private Integer sceneMode;
        private String currencyIcon;

        public Integer getSceneMode() {
            return sceneMode;
        }

        public void setSceneMode(Integer sceneMode) {
            this.sceneMode = sceneMode;
        }

        public String getCurrencyIcon() {
            return currencyIcon;
        }

        public void setCurrencyIcon(String currencyIcon) {
            this.currencyIcon = currencyIcon;
        }
    }
}
