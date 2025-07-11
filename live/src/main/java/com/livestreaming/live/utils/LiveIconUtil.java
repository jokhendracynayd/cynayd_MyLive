package com.livestreaming.live.utils;

import android.util.SparseIntArray;

import com.livestreaming.live.R;

import java.util.Arrays;
import java.util.List;

/**
 * Created by cxf on 2018/10/11.
 */

public class LiveIconUtil {
    private static SparseIntArray sLiveLightMap;//飘心动画图片
    private static SparseIntArray sLiveGiftCountMap;//送礼物数字
    private static List<Integer> sLinkMicPkAnim;//连麦pk帧动画
    private static SparseIntArray sVoiceRoomFace;//语音直播间表情

    static {
        sLiveLightMap = new SparseIntArray();
        sLiveLightMap.put(1, R.mipmap.icon_live_light_1);
        sLiveLightMap.put(2, R.mipmap.icon_live_light_2);
        sLiveLightMap.put(3, R.mipmap.icon_live_light_3);
        sLiveLightMap.put(4, R.mipmap.icon_live_light_4);
        sLiveLightMap.put(5, R.mipmap.icon_live_light_5);
        sLiveLightMap.put(6, R.mipmap.icon_live_light_6);

        sLiveGiftCountMap = new SparseIntArray();
        sLiveGiftCountMap.put(0, R.mipmap.icon_live_gift_count_0);
        sLiveGiftCountMap.put(1, R.mipmap.icon_live_gift_count_1);
        sLiveGiftCountMap.put(2, R.mipmap.icon_live_gift_count_2);
        sLiveGiftCountMap.put(3, R.mipmap.icon_live_gift_count_3);
        sLiveGiftCountMap.put(4, R.mipmap.icon_live_gift_count_4);
        sLiveGiftCountMap.put(5, R.mipmap.icon_live_gift_count_5);
        sLiveGiftCountMap.put(6, R.mipmap.icon_live_gift_count_6);
        sLiveGiftCountMap.put(7, R.mipmap.icon_live_gift_count_7);
        sLiveGiftCountMap.put(8, R.mipmap.icon_live_gift_count_8);
        sLiveGiftCountMap.put(9, R.mipmap.icon_live_gift_count_9);

        sLinkMicPkAnim = Arrays.asList(
                R.drawable.pk01,
                R.drawable.pk02,
                R.drawable.pk03,
                R.drawable.pk04,
                R.drawable.pk05,
                R.drawable.pk06,
                R.drawable.pk07,
                R.drawable.pk08,
                R.drawable.pk09,
                R.drawable.pk10,
                R.drawable.pk11,
                R.drawable.pk12,
                R.drawable.pk13,
                R.drawable.pk14,
                R.drawable.pk15,
                R.drawable.pk16,
                R.drawable.pk17,
                R.drawable.pk18,
                R.drawable.pk19
        );

        createVoiceRoomFace();


    }

    private static void createVoiceRoomFace() {
        sVoiceRoomFace = new SparseIntArray();
        sVoiceRoomFace.put(0, R.mipmap.ic_live_voice_face_00);
        sVoiceRoomFace.put(1, R.mipmap.ic_live_voice_face_01);
        sVoiceRoomFace.put(2, R.mipmap.ic_live_voice_face_02);
        sVoiceRoomFace.put(3, R.mipmap.ic_live_voice_face_03);
        sVoiceRoomFace.put(4, R.mipmap.ic_live_voice_face_04);
        sVoiceRoomFace.put(5, R.mipmap.ic_live_voice_face_05);
        sVoiceRoomFace.put(6, R.mipmap.ic_live_voice_face_06);
        sVoiceRoomFace.put(7, R.mipmap.ic_live_voice_face_07);
        sVoiceRoomFace.put(8, R.mipmap.ic_live_voice_face_08);
        sVoiceRoomFace.put(9, R.mipmap.ic_live_voice_face_09);
        sVoiceRoomFace.put(10, R.mipmap.ic_live_voice_face_10);
        sVoiceRoomFace.put(11, R.mipmap.ic_live_voice_face_11);
        sVoiceRoomFace.put(12, R.mipmap.ic_live_voice_face_12);
        sVoiceRoomFace.put(13, R.mipmap.ic_live_voice_face_13);
        sVoiceRoomFace.put(14, R.mipmap.ic_live_voice_face_14);
        sVoiceRoomFace.put(15, R.mipmap.ic_live_voice_face_15);
    }


    public static int getLiveLightIcon(int key) {
        if (key > 6 || key < 1) {
            key = 1;
        }
        return sLiveLightMap.get(key);
    }

    public static int getGiftCountIcon(int key) {
        return sLiveGiftCountMap.get(key);
    }

    public static List<Integer> getLinkMicPkAnim() {
        return sLinkMicPkAnim;
    }

    public static SparseIntArray getVoiceRoomFace() {
        if (sVoiceRoomFace == null) {
            createVoiceRoomFace();
        }
        return sVoiceRoomFace;
    }

    public static int getVoiceRoomFaceRes(int key) {
        if (key >= 0 && key < 16) {
            return sVoiceRoomFace.get(key);
        }
        return -1;
    }
}
