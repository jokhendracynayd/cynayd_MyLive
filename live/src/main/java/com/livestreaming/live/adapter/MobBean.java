package com.livestreaming.live.adapter;

import com.livestreaming.common.Constants;

import java.util.ArrayList;
import java.util.List;

public class MobBean {

    private String mType;
    private int mIcon1;
    private int mIcon2;
    private int mName;
    private boolean mChecked;

    public String getType() {
        return mType;
    }

    public void setType(String type) {
        mType = type;
    }

    public int getName() {
        return mName;
    }

    public void setName(int name) {
        mName = name;
    }

    public int getIcon1() {
        return mIcon1;
    }

    public void setIcon1(int icon1) {
        mIcon1 = icon1;
    }

    public int getIcon2() {
        return mIcon2;
    }

    public void setIcon2(int icon2) {
        mIcon2 = icon2;
    }

    public boolean isChecked() {
        return mChecked;
    }

    public void setChecked(boolean checked) {
        mChecked = checked;
    }


    /**
     * 登录类型
     */
    public static List<MobBean> getLoginTypeList(String[] types) {
        List<MobBean> list = new ArrayList<>();
        if (types != null) {
            for (String type : types) {
                MobBean bean = new MobBean();
                bean.setType(type);
                switch (type) {
                    case Constants.MOB_QQ:
                        bean.setIcon1(com.livestreaming.common.R.mipmap.icon_login_qq);
                        break;
                    case Constants.MOB_WX:
                        bean.setIcon1(com.livestreaming.common.R.mipmap.icon_login_wx);
                        break;
                    case Constants.MOB_FACEBOOK:
                        bean.setIcon1(com.livestreaming.common.R.mipmap.icon_login_fb);
                        break;
                    case Constants.MOB_TWITTER:
                        bean.setIcon1(com.livestreaming.common.R.mipmap.icon_login_tt);
                        break;
                }
                list.add(bean);
            }
        }
        return list;
    }

    /**
     * 开播前分享类型
     */
    public static List<MobBean> getLiveReadyShareTypeList(String[] types) {
        List<MobBean> list = new ArrayList<>();
        if (types != null) {
            for (String type : types) {
                MobBean bean = new MobBean();
                bean.setType(type);
                switch (type) {
                    case Constants.MOB_QQ:
                        bean.setIcon1(com.livestreaming.common.R.mipmap.icon_share_qq_1);
                        bean.setIcon2(com.livestreaming.common.R.mipmap.icon_share_qq_2);
                        break;
                    case Constants.MOB_QZONE:
                        bean.setIcon1(com.livestreaming.common.R.mipmap.icon_share_qzone_1);
                        bean.setIcon2(com.livestreaming.common.R.mipmap.icon_share_qzone_2);
                        break;
                    case Constants.MOB_WX:
                        bean.setIcon1(com.livestreaming.common.R.mipmap.icon_share_wx_1);
                        bean.setIcon2(com.livestreaming.common.R.mipmap.icon_share_wx_2);
                        break;
                    case Constants.MOB_WX_PYQ:
                        bean.setIcon1(com.livestreaming.common.R.mipmap.icon_share_pyq_1);
                        bean.setIcon2(com.livestreaming.common.R.mipmap.icon_share_pyq_2);
                        break;
                    case Constants.MOB_FACEBOOK:
                        bean.setIcon1(com.livestreaming.common.R.mipmap.icon_share_fb_1);
                        bean.setIcon2(com.livestreaming.common.R.mipmap.icon_share_fb_2);
                        break;
                    case Constants.MOB_TWITTER:
                        bean.setIcon1(com.livestreaming.common.R.mipmap.icon_share_tt_1);
                        bean.setIcon2(com.livestreaming.common.R.mipmap.icon_share_tt_2);
                        break;
                }
                list.add(bean);
            }
        }
        return list;
    }


    /**
     * 直播间分享类型
     */
    public static List<MobBean> getLiveShareTypeList(String[] types) {
        List<MobBean> list = new ArrayList<>();
        MobBean bean1 = new MobBean();
        bean1.setType(Constants.MOB_WHATSAPP);
        bean1.setIcon1(com.livestreaming.common.R.drawable.ic_whatsapp);
        bean1.setName(com.livestreaming.common.R.string.mob_wa);

        MobBean bean2 = new MobBean();
        bean2.setType(Constants.MOB_FACEBOOK);
        bean2.setIcon1(com.livestreaming.common.R.mipmap.icon_share_fb_3);
        bean2.setName(com.livestreaming.common.R.string.mob_fb);

        MobBean bean3 = new MobBean();
        bean3.setType(Constants.MOB_TWITTER);
        bean3.setIcon1(com.livestreaming.common.R.mipmap.icon_share_tt_3);
        bean3.setName(com.livestreaming.common.R.string.mob_tt);

        list.add(bean1);
        list.add(bean2);
        list.add(bean3);
        return list;
    }


    /**
     * 视频分享类型
     */
    public List<MobBean> getVideoShareTypeList(String[] types) {
        List<MobBean> list = new ArrayList<>();
        if (types != null) {
            for (String type : types) {
                MobBean bean = new MobBean();
                bean.setType(type);
                switch (type) {
                    case Constants.MOB_QQ:
                        bean.setIcon1(com.livestreaming.common.R.mipmap.icon_share_qq_3);
                        bean.setIcon2(com.livestreaming.common.R.mipmap.icon_share_qq_4);
                        bean.setName(com.livestreaming.common.R.string.mob_qq);
                        break;
                    case Constants.MOB_QZONE:
                        bean.setIcon1(com.livestreaming.common.R.mipmap.icon_share_qzone_3);
                        bean.setIcon2(com.livestreaming.common.R.mipmap.icon_share_qzone_4);
                        bean.setName(com.livestreaming.common.R.string.mob_qzone);
                        break;
                    case Constants.MOB_WX:
                        bean.setIcon1(com.livestreaming.common.R.mipmap.icon_share_wx_3);
                        bean.setIcon2(com.livestreaming.common.R.mipmap.icon_share_wx_4);
                        bean.setName(com.livestreaming.common.R.string.mob_wx);
                        break;
                    case Constants.MOB_WX_PYQ:
                        bean.setIcon1(com.livestreaming.common.R.mipmap.icon_share_pyq_3);
                        bean.setIcon2(com.livestreaming.common.R.mipmap.icon_share_pyq_4);
                        bean.setName(com.livestreaming.common.R.string.mob_wx_pyq);
                        break;
                    case Constants.MOB_FACEBOOK:
                        bean.setIcon1(com.livestreaming.common.R.mipmap.icon_share_fb_3);
                        bean.setIcon2(com.livestreaming.common.R.mipmap.icon_share_fb_4);
                        bean.setName(com.livestreaming.common.R.string.mob_fb);
                        break;
                    case Constants.MOB_TWITTER:
                        bean.setIcon1(com.livestreaming.common.R.mipmap.icon_share_tt_3);
                        bean.setIcon2(com.livestreaming.common.R.mipmap.icon_share_tt_4);
                        bean.setName(com.livestreaming.common.R.string.mob_tt);
                        break;
                }
                list.add(bean);
            }
        }
        return list;
    }
}
