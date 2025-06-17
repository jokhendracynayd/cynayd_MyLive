package com.livestreaming.common.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by cxf on 2018/7/19.
 */

public class DateFormatUtil {

    private static SimpleDateFormat sFormat;
    private static SimpleDateFormat sFormat2;
    private static SimpleDateFormat sFormat3;

    static {
        sFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.US);
        sFormat2 = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US);
        sFormat3 = new SimpleDateFormat("MM.dd-HH:mm:ss", Locale.US);
    }


    public static String getCurTimeString() {
        return sFormat.format(new Date());
    }

    public static String getVideoCurTimeString() {
        return sFormat2.format(new Date());
    }

    public static String getCurTimeString2() {
        return sFormat3.format(new Date());
    }
}
