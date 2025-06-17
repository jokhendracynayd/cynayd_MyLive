package com.livestreaming.live.activity;

import com.livestreaming.common.Constants;

import java.util.HashMap;
import java.util.Map;

public class MobConst {

    public static final Map<String, String> MAP;

    static {
        MAP = new HashMap<>();
        MAP.put(Constants.MOB_FACEBOOK, "facebook");
        MAP.put(Constants.MOB_TWITTER, "Twitter");
    }

}
