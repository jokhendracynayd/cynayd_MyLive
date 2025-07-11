package com.livestreaming.live.utils;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import androidx.core.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.StyleSpan;
import android.widget.TextView;

import com.livestreaming.common.CommonAppConfig;
import com.livestreaming.common.CommonAppContext;
import com.livestreaming.common.Constants;
import com.livestreaming.common.bean.LevelBean;
import com.livestreaming.common.custom.VerticalImageSpan;
import com.livestreaming.common.glide.ImgLoader;
import com.livestreaming.common.utils.DpUtil;
import com.livestreaming.common.utils.StringUtil;
import com.livestreaming.common.utils.WordUtil;
import com.livestreaming.live.R;
import com.livestreaming.live.bean.LiveChatBean;

/**
 * Created by cxf on 2018/10/11.
 */

public class LiveTextRender {

    private static StyleSpan sBoldSpan;
    private static StyleSpan sNormalSpan;
    private static ForegroundColorSpan sWhiteColorSpan;
    private static ForegroundColorSpan sGlobalColorSpan;
    private static AbsoluteSizeSpan sFontSizeSpan;
    private static AbsoluteSizeSpan sFontSizeSpan2;
    private static AbsoluteSizeSpan sFontSizeSpan3;

    static {
        sBoldSpan = new StyleSpan(Typeface.BOLD);
        sNormalSpan = new StyleSpan(Typeface.NORMAL);
        sWhiteColorSpan = new ForegroundColorSpan(0xffffffff);
        sGlobalColorSpan = new ForegroundColorSpan(0xffff5878);
        sFontSizeSpan = new AbsoluteSizeSpan(17, true);
        sFontSizeSpan2 = new AbsoluteSizeSpan(12, true);
        sFontSizeSpan3 = new AbsoluteSizeSpan(14, true);
    }


    /**
     * 生成前缀
     */
    private static SpannableStringBuilder createPrefix(Drawable levelDrawable, LiveChatBean bean) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        int index = 0;
        if (levelDrawable != null) {
            builder.append("  ");
            levelDrawable.setBounds(0, 0, DpUtil.dp2px(28), DpUtil.dp2px(14));
            builder.setSpan(new VerticalImageSpan(levelDrawable), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            index = 2;
        }
        if (bean.getVipType() != 0) {//vip图标
            Drawable vipDrawable = ContextCompat.getDrawable(CommonAppContext.getInstance(), R.mipmap.icon_live_chat_vip);
            if (vipDrawable != null) {
                builder.append("  ");
                vipDrawable.setBounds(0, 0, DpUtil.dp2px(28), DpUtil.dp2px(14));
                builder.setSpan(new VerticalImageSpan(vipDrawable), index, index + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                index += 2;
            }
        }
        if (bean.isManager()) {//直播间管理员图标
            Drawable drawable = ContextCompat.getDrawable(CommonAppContext.getInstance(), R.drawable.icon_live_chat_m);
            if (drawable != null) {
                builder.append("  ");
                drawable.setBounds(0, 0, DpUtil.dp2px(17), DpUtil.dp2px(14));
                builder.setSpan(new VerticalImageSpan(drawable), index, index + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                index += 2;
            }
        }
        if (bean.getGuardType() != Constants.GUARD_TYPE_NONE) {//守护图标
            Drawable drawable = ContextCompat.getDrawable(CommonAppContext.getInstance(),
                    bean.getGuardType() == Constants.GUARD_TYPE_YEAR ? R.drawable.icon_live_chat_guard_2 : R.drawable.icon_live_chat_guard_1);
            if (drawable != null) {
                builder.append("  ");
                drawable.setBounds(0, 0, DpUtil.dp2px(16), DpUtil.dp2px(16));
                builder.setSpan(new VerticalImageSpan(drawable), index, index + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                index += 2;
            }
        }
        if (!TextUtils.isEmpty(bean.getLiangName())) {//靓号图标
            Drawable drawable = ContextCompat.getDrawable(CommonAppContext.getInstance(), R.drawable.icon_live_chat_liang);
            if (drawable != null) {
                builder.append("  ");
                drawable.setBounds(0, 0, DpUtil.dp2px(17), DpUtil.dp2px(14));
                builder.setSpan(new VerticalImageSpan(drawable), index, index + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        return builder;
    }

    /**
     * 产品规定，进场消息不允许添加管理员图标,
     * 所以 我只能复制一份上面的代码。。。。
     */
    private static SpannableStringBuilder createPrefix2(Drawable levelDrawable, LiveChatBean bean) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        int index = 0;
        if (levelDrawable != null) {
            builder.append("  ");
            levelDrawable.setBounds(0, 0, DpUtil.dp2px(28), DpUtil.dp2px(14));
            builder.setSpan(new VerticalImageSpan(levelDrawable), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            index = 2;
        }
        if (bean.getVipType() != 0) {//vip图标
            Drawable vipDrawable = ContextCompat.getDrawable(CommonAppContext.getInstance(), R.mipmap.icon_live_chat_vip);
            if (vipDrawable != null) {
                builder.append("  ");
                vipDrawable.setBounds(0, 0, DpUtil.dp2px(28), DpUtil.dp2px(14));
                builder.setSpan(new VerticalImageSpan(vipDrawable), index, index + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                index += 2;
            }
        }
        if (bean.getGuardType() != Constants.GUARD_TYPE_NONE) {//守护图标
            Drawable drawable = ContextCompat.getDrawable(CommonAppContext.getInstance(),
                    bean.getGuardType() == Constants.GUARD_TYPE_YEAR ? R.drawable.icon_live_chat_guard_2 : R.drawable.icon_live_chat_guard_1);
            if (drawable != null) {
                builder.append("  ");
                drawable.setBounds(0, 0, DpUtil.dp2px(16), DpUtil.dp2px(16));
                builder.setSpan(new VerticalImageSpan(drawable), index, index + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                index += 2;
            }
        }
        if (!TextUtils.isEmpty(bean.getLiangName())) {//靓号图标
            Drawable drawable = ContextCompat.getDrawable(CommonAppContext.getInstance(), R.drawable.icon_live_chat_liang);
            if (drawable != null) {
                builder.append("  ");
                drawable.setBounds(0, 0, DpUtil.dp2px(17), DpUtil.dp2px(14));
                builder.setSpan(new VerticalImageSpan(drawable), index, index + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        return builder;
    }

    public static void render(Context context, final TextView textView, final LiveChatBean bean) {
        final LevelBean levelBean = CommonAppConfig.getInstance().getLevel(bean.getLevel());
        if (levelBean == null) {
            return;
        }
        ImgLoader.displayDrawable(context, levelBean.getThumb(), new ImgLoader.DrawableCallback() {
            @Override
            public void onLoadSuccess(Drawable drawable) {
                if (textView != null) {
                    SpannableStringBuilder builder = createPrefix(drawable, bean);
                    int color = 0;
                    if (bean.isAnchor()) {
                        color = 0xffff5878;
                    } else {
                        color = Color.parseColor(levelBean.getColor());
                    }
                    switch (bean.getType()) {
                        case LiveChatBean.GIFT:
                            builder = renderGift(color, builder, bean);
                            break;
                        default:
                            builder = renderChat(color, builder, bean);
                            break;
                    }
                    textView.setText(builder);
                }
            }

            @Override
            public void onLoadFailed() {
                if (textView != null) {
                    SpannableStringBuilder builder = createPrefix(null, bean);
                    int color = 0;
                    if (bean.isAnchor()) {
                        color = 0xffff5878;
                    } else {
                        color = Color.parseColor(levelBean.getColor());
                    }
                    switch (bean.getType()) {
                        case LiveChatBean.GIFT:
                            builder = renderGift(color, builder, bean);
                            break;
                        default:
                            builder = renderChat(color, builder, bean);
                            break;
                    }
                    textView.setText(builder);
                }
            }
        });
    }

    /**
     * 渲染普通聊天消息
     */
    private static SpannableStringBuilder renderChat(int color, SpannableStringBuilder builder, LiveChatBean bean) {
        int length = builder.length();
        String name = bean.getUserNiceName();
        if (bean.getType() != LiveChatBean.ENTER_ROOM) {//产品规定，进场消息不允许加冒号
            name += "：";
        }
        builder.append(name);
        builder.setSpan(new ForegroundColorSpan(color), length, length + name.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append(bean.getContent());
        if (bean.getType() == LiveChatBean.LIGHT) {
            Drawable heartDrawable = ContextCompat.getDrawable(CommonAppContext.getInstance(), LiveIconUtil.getLiveLightIcon(bean.getHeart()));
            if (heartDrawable != null) {
                builder.append(" ");
                heartDrawable.setBounds(0, 0, DpUtil.dp2px(16), DpUtil.dp2px(16));
                length = builder.length();
                builder.setSpan(new VerticalImageSpan(heartDrawable), length - 1, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        return builder;
    }

    /**
     * 渲染送礼物消息
     */
    private static SpannableStringBuilder renderGift(int color, SpannableStringBuilder builder, LiveChatBean bean) {
        int length = builder.length();
        String name = bean.getUserNiceName() + "：";
        builder.append(name);
        builder.setSpan(new ForegroundColorSpan(color), length, length + name.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        length = builder.length();
        String content = bean.getContent();
        builder.append(content);
        builder.setSpan(sGlobalColorSpan, length, length + content.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return builder;
    }


    /**
     * 渲染用户进入房间消息
     */
    public static void renderEnterRoom(Context context, final TextView textView, final LiveChatBean bean) {
        final LevelBean levelBean = CommonAppConfig.getInstance().getLevel(bean.getLevel());
        if (levelBean == null) {
            return;
        }
        ImgLoader.displayDrawable(context, levelBean.getThumb(), new ImgLoader.DrawableCallback() {
            @Override
            public void onLoadSuccess(Drawable drawable) {
                if (textView != null) {
                    SpannableStringBuilder builder = createPrefix2(drawable, bean);
                    int start = builder.length();
                    String name = bean.getUserNiceName() + " ";
                    builder.append(name);
                    //int end = start + name.length();
                    //builder.setSpan(sWhiteColorSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    //builder.setSpan(sFontSizeSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    //builder.setSpan(sBoldSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    builder.append(bean.getContent());
                    textView.setText(builder);
                }
            }

            @Override
            public void onLoadFailed() {
                if (textView != null) {
                    SpannableStringBuilder builder = createPrefix2(null, bean);
                    int start = builder.length();
                    String name = bean.getUserNiceName() + " ";
                    builder.append(name);
//                    int end = start + name.length();
//                    builder.setSpan(sWhiteColorSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//                    builder.setSpan(sFontSizeSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//                    builder.setSpan(sBoldSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    builder.append(bean.getContent());
                    textView.setText(builder);
                }
            }

        });
    }

    public static SpannableStringBuilder renderGiftInfo(String giftName, String toName) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        String s1 = "";
        if (TextUtils.isEmpty(toName)) {
            s1 = WordUtil.getString(com.livestreaming.common.R.string.live_send_gift_2);
        } else {
            s1 = String.format(WordUtil.getString(com.livestreaming.common.R.string.live_send_gift_7), toName);
        }
        String content = StringUtil.contact(s1, " ", giftName);
        int index1 = s1.length();
        builder.append(content);
        builder.setSpan(sGlobalColorSpan, index1, content.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return builder;
    }


    public static SpannableStringBuilder renderGiftCount(int count) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        String s = String.valueOf(count);
        builder.append(s);
        for (int i = 0, length = s.length(); i < length; i++) {
            String c = String.valueOf(s.charAt(i));
            if (StringUtil.isInt(c)) {
                int icon = LiveIconUtil.getGiftCountIcon(Integer.parseInt(c));
                Drawable drawable = ContextCompat.getDrawable(CommonAppContext.getInstance(), icon);
                if (drawable != null) {
                    drawable.setBounds(0, 0, DpUtil.dp2px(24), DpUtil.dp2px(32));
                    builder.setSpan(new ImageSpan(drawable), i, i + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
        }
        return builder;
    }

    /**
     * 渲染直播间用户弹窗数据
     */
    public static CharSequence renderLiveUserDialogData(long num) {
        if (num < 10000) {
            return String.valueOf(num);
        }
        SpannableStringBuilder builder = new SpannableStringBuilder();
        String wan = " " + WordUtil.getString(com.livestreaming.common.R.string.num_wan);
        String s = StringUtil.toWan2(num) + wan;
        builder.append(s);
        int index2 = s.length();
        int index1 = index2 - wan.length();
        builder.setSpan(sNormalSpan, index1, index2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.setSpan(sFontSizeSpan2, index1, index2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return builder;
    }


}
