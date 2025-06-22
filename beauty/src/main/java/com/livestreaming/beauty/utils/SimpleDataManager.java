package com.livestreaming.beauty.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

import com.alibaba.fastjson.JSONObject;
import com.livestreaming.beauty.bean.MeiYanValueBean;
import com.livestreaming.beauty.interfaces.IBeautyEffectListener;
import com.livestreaming.common.CommonAppContext;
import com.livestreaming.common.http.CommonHttpUtil;
import com.livestreaming.common.utils.L;
import com.livestreaming.common.utils.SpUtil;

/**
 * 简版美颜 数据管理类
 */
public class SimpleDataManager {

    private static final String TAG = "SimpleDataManager";
    private static SimpleDataManager sInstance;

    private SimpleSPUtil mSPUtil;
    private int mMeiBai;
    private int mMoPi;
    private int mHongRun;
    private int mBrightness;
    private int mSharpness;
    private int mFaceSlim;
    private int mBigEye;
    private int mJaw;
    private int mEyeDistance;
    private int mFaceShape;
    private int mEyeBrow;
    private int mEyeCorner;
    private int mMouthShape;
    private int mNoseLift;
    private int mForehead;
    private int mLongNose;
    private int mFaceShave;
    private int mEyeAlat;
    private int mFilterId;
    private IBeautyEffectListener mMeiYanChangedListener;

    private SimpleDataManager() {
        mSPUtil = new SimpleSPUtil();
        mMeiBai = mSPUtil.getMeiBai();
        mMoPi = mSPUtil.getMoPi();
        mHongRun = mSPUtil.getHongRun();
        mBrightness = mSPUtil.getBrightness();
        mSharpness = mSPUtil.getSharpness();
        mFaceSlim = mSPUtil.getFaceSlim();
        mBigEye = mSPUtil.getBigEye();
        mJaw = mSPUtil.getJaw();
        mEyeDistance = mSPUtil.getEyeDistance();
        mFaceShape = mSPUtil.getFaceShape();
        mEyeBrow = mSPUtil.getEyeBrow();
        mEyeCorner = mSPUtil.getEyeCorner();
        mMouthShape = mSPUtil.getMouthShape();
        mNoseLift = mSPUtil.getNoseLift();
        mForehead = mSPUtil.getForehead();
        mLongNose = mSPUtil.getLongNose();
        mFaceShave = mSPUtil.getFaceShave();
        mEyeAlat = mSPUtil.getEyeAlat();
        mFilterId = mSPUtil.getFilterId();
    }

    public static SimpleDataManager getInstance() {
        if (sInstance == null) {
            synchronized (SimpleDataManager.class) {
                if (sInstance == null) {
                    sInstance = new SimpleDataManager();
                }
            }
        }
        return sInstance;
    }

    public void setMeiYanChangedListener(IBeautyEffectListener meiYanChangedListener) {
        mMeiYanChangedListener = meiYanChangedListener;
    }

    public int getMeiBai() {
        return mMeiBai;
    }

    public int getMoPi() {
        return mMoPi;
    }

    public int getHongRun() {
        return mHongRun;
    }
    
    public int getBrightness() {
        return mBrightness;
    }
    
    public int getSharpness() {
        return mSharpness;
    }
    
    public int getFaceSlim() {
        return mFaceSlim;
    }
    
    public int getBigEye() {
        return mBigEye;
    }
    
    public int getJaw() {
        return mJaw;
    }
    
    public int getEyeDistance() {
        return mEyeDistance;
    }
    
    public int getFaceShape() {
        return mFaceShape;
    }
    
    public int getEyeBrow() {
        return mEyeBrow;
    }
    
    public int getEyeCorner() {
        return mEyeCorner;
    }
    
    public int getMouthShape() {
        return mMouthShape;
    }
    
    public int getNoseLift() {
        return mNoseLift;
    }
    
    public int getForehead() {
        return mForehead;
    }
    
    public int getLongNose() {
        return mLongNose;
    }
    
    public int getFaceShave() {
        return mFaceShave;
    }
    
    public int getEyeAlat() {
        return mEyeAlat;
    }

    public void setMeiBai(int meiBai) {
        mMeiBai = meiBai;
        if (mMeiYanChangedListener != null) {
            mMeiYanChangedListener.onMeiYanChanged(meiBai, true, mMoPi, false, mHongRun, false);
        }
    }

    public void setMoPi(int moPi) {
        mMoPi = moPi;
        if (mMeiYanChangedListener != null) {
            mMeiYanChangedListener.onMeiYanChanged(mMeiBai, false, moPi, true, mHongRun, false);
        }
    }

    public void setHongRun(int hongRun) {
        mHongRun = hongRun;
        if (mMeiYanChangedListener != null) {
            mMeiYanChangedListener.onMeiYanChanged(mMeiBai, false, mMoPi, false, hongRun, true);
        }
    }
    
    public void setBrightness(int brightness) {
        mBrightness = brightness;
        if (mMeiYanChangedListener != null) {
            mMeiYanChangedListener.onAdvancedBeautyChanged("brightness", brightness);
        }
    }
    
    public void setSharpness(int sharpness) {
        mSharpness = sharpness;
        if (mMeiYanChangedListener != null) {
            mMeiYanChangedListener.onAdvancedBeautyChanged("sharpness", sharpness);
        }
    }
    
    public void setFaceSlim(int faceSlim) {
        mFaceSlim = faceSlim;
        if (mMeiYanChangedListener != null) {
            mMeiYanChangedListener.onAdvancedBeautyChanged("face_slim", faceSlim);
        }
    }
    
    public void setBigEye(int bigEye) {
        mBigEye = bigEye;
        if (mMeiYanChangedListener != null) {
            mMeiYanChangedListener.onAdvancedBeautyChanged("big_eye", bigEye);
        }
    }
    
    public void setJaw(int jaw) {
        mJaw = jaw;
        if (mMeiYanChangedListener != null) {
            mMeiYanChangedListener.onAdvancedBeautyChanged("jaw", jaw);
        }
    }
    
    public void setEyeDistance(int eyeDistance) {
        mEyeDistance = eyeDistance;
        if (mMeiYanChangedListener != null) {
            mMeiYanChangedListener.onAdvancedBeautyChanged("eye_distance", eyeDistance);
        }
    }
    
    public void setFaceShape(int faceShape) {
        mFaceShape = faceShape;
        if (mMeiYanChangedListener != null) {
            mMeiYanChangedListener.onAdvancedBeautyChanged("face_shape", faceShape);
        }
    }
    
    public void setEyeBrow(int eyeBrow) {
        mEyeBrow = eyeBrow;
        if (mMeiYanChangedListener != null) {
            mMeiYanChangedListener.onAdvancedBeautyChanged("eye_brow", eyeBrow);
        }
    }
    
    public void setEyeCorner(int eyeCorner) {
        mEyeCorner = eyeCorner;
        if (mMeiYanChangedListener != null) {
            mMeiYanChangedListener.onAdvancedBeautyChanged("eye_corner", eyeCorner);
        }
    }
    
    public void setMouthShape(int mouthShape) {
        mMouthShape = mouthShape;
        if (mMeiYanChangedListener != null) {
            mMeiYanChangedListener.onAdvancedBeautyChanged("mouse_lift", mouthShape);
        }
    }
    
    public void setNoseLift(int noseLift) {
        mNoseLift = noseLift;
        if (mMeiYanChangedListener != null) {
            mMeiYanChangedListener.onAdvancedBeautyChanged("nose_lift", noseLift);
        }
    }
    
    public void setForehead(int forehead) {
        mForehead = forehead;
        if (mMeiYanChangedListener != null) {
            mMeiYanChangedListener.onAdvancedBeautyChanged("forehead_lift", forehead);
        }
    }
    
    public void setLongNose(int longNose) {
        mLongNose = longNose;
        if (mMeiYanChangedListener != null) {
            mMeiYanChangedListener.onAdvancedBeautyChanged("lengthen_noseLift", longNose);
        }
    }
    
    public void setFaceShave(int faceShave) {
        mFaceShave = faceShave;
        if (mMeiYanChangedListener != null) {
            mMeiYanChangedListener.onAdvancedBeautyChanged("face_shave", faceShave);
        }
    }
    
    public void setEyeAlat(int eyeAlat) {
        mEyeAlat = eyeAlat;
        if (mMeiYanChangedListener != null) {
            mMeiYanChangedListener.onAdvancedBeautyChanged("eye_alat", eyeAlat);
        }
    }

    public void setFilter(int filterId) {
        mFilterId = filterId;
        if (mMeiYanChangedListener != null) {
            Bitmap bitmap = null;
            if (filterId == 0) {
                bitmap = null;
            } else {
                bitmap = BitmapFactory.decodeResource(CommonAppContext.getInstance().getResources(), filterId);
            }
            mMeiYanChangedListener.onFilterChanged(filterId);
        }
    }

    public void saveBeautyValue() {
        // Save to local storage
        mSPUtil.setMeiBai(mMeiBai);
        mSPUtil.setMoPi(mMoPi);
        mSPUtil.setHongRun(mHongRun);
        mSPUtil.setBrightness(mBrightness);
        mSPUtil.setSharpness(mSharpness);
        mSPUtil.setFaceSlim(mFaceSlim);
        mSPUtil.setBigEye(mBigEye);
        mSPUtil.setJaw(mJaw);
        mSPUtil.setEyeDistance(mEyeDistance);
        mSPUtil.setFaceShape(mFaceShape);
        mSPUtil.setEyeBrow(mEyeBrow);
        mSPUtil.setEyeCorner(mEyeCorner);
        mSPUtil.setMouthShape(mMouthShape);
        mSPUtil.setNoseLift(mNoseLift);
        mSPUtil.setForehead(mForehead);
        mSPUtil.setLongNose(mLongNose);
        mSPUtil.setFaceShave(mFaceShave);
        mSPUtil.setEyeAlat(mEyeAlat);
        mSPUtil.setFilterId(mFilterId);
        
        // Create beauty parameters object for server sync
        MeiYanValueBean valueBean = new MeiYanValueBean();
        valueBean.setMeiBai(mMeiBai);
        valueBean.setMoPi(mMoPi);
        valueBean.setHongRun(mHongRun);
        valueBean.setLiangDu(mBrightness); // Map brightness to liangdu
        valueBean.setDaYan(mBigEye);       // Map bigEye to dayan
        valueBean.setYanJu(mEyeDistance);  // Map eyeDistance to yanju
        valueBean.setShouLian(mFaceSlim);  // Map faceSlim to shoulian
        valueBean.setXiaBa(mJaw);          // Map jaw to xiaba
        valueBean.setETou(mFaceShape);     // Map faceShape to etou
        valueBean.setMeiMao(mEyeBrow);     // Map eyeBrow to meimao
        valueBean.setYanJiao(mEyeCorner);  // Map eyeCorner to yanjiao
        valueBean.setZuiXing(mMouthShape); // Map mouthShape to zuixing
        valueBean.setShouBi(mNoseLift);    // Map noseLift to shoubi
        valueBean.setETou(mForehead);      // Map forehead to etou
        valueBean.setChangBi(mLongNose);   // Map longNose to changbi
        valueBean.setXueLian(mFaceShave);  // Map faceShave to xuelian
        valueBean.setKaiYanJiao(mEyeAlat); // Map eyeAlat to kaiyanjiao
        
        // Save to server
        com.livestreaming.common.http.CommonHttpUtil.setBeautyValue(com.alibaba.fastjson.JSON.toJSONString(valueBean));
    }

    /**
     * Load beauty values from the server
     * 
     * @param fromUid The user ID to load beauty values from (for audience viewing an anchor)
     */
    public void loadBeautyValue(String fromUid) {
        com.livestreaming.common.http.CommonHttpUtil.getBeautyValue(new com.livestreaming.common.http.HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0 && info.length > 0) {
                    try {
                        MeiYanValueBean valueBean = com.alibaba.fastjson.JSON.parseObject(info[0], MeiYanValueBean.class);
                        
                        // Map values from server to SimpleDataManager properties
                        mMeiBai = valueBean.getMeiBai();
                        mMoPi = valueBean.getMoPi();
                        mHongRun = valueBean.getHongRun();
                        mBrightness = valueBean.getLiangDu();    // Map liangdu to brightness
                        mBigEye = valueBean.getDaYan();          // Map dayan to bigEye
                        mEyeDistance = valueBean.getYanJu();     // Map yanju to eyeDistance
                        mFaceSlim = valueBean.getShouLian();     // Map shoulian to faceSlim
                        mJaw = valueBean.getXiaBa();             // Map xiaba to jaw
                        mFaceShape = valueBean.getETou();        // Map etou to faceShape
                        
                        // Apply the beauty effects
                        if (mMeiYanChangedListener != null) {
                            // Apply base beauty parameters
                            mMeiYanChangedListener.onMeiYanChanged(mMeiBai, true, mMoPi, true, mHongRun, true);
                            
                            // Apply advanced beauty parameters
                            mMeiYanChangedListener.onAdvancedBeautyChanged("brightness", mBrightness);
                            mMeiYanChangedListener.onAdvancedBeautyChanged("sharpness", mMoPi); // Using moPi for sharpness
                            mMeiYanChangedListener.onAdvancedBeautyChanged("face_slim", mFaceSlim);
                            mMeiYanChangedListener.onAdvancedBeautyChanged("big_eye", mBigEye);
                            mMeiYanChangedListener.onAdvancedBeautyChanged("jaw", mJaw);
                            mMeiYanChangedListener.onAdvancedBeautyChanged("eye_distance", mEyeDistance);
                            mMeiYanChangedListener.onAdvancedBeautyChanged("face_shape", mFaceShape);
                        }
                        
                        // Save to local storage too
                        saveToLocalStorage();
                        
                    } catch (Exception e) {
                        com.livestreaming.common.utils.L.e("SimpleDataManager", "Error parsing beauty values: " + e.getMessage());
                    }
                }
            }
        });
    }
    
    /**
     * Save values only to local storage (not to server)
     */
    private void saveToLocalStorage() {
        mSPUtil.setMeiBai(mMeiBai);
        mSPUtil.setMoPi(mMoPi);
        mSPUtil.setHongRun(mHongRun);
        mSPUtil.setBrightness(mBrightness);
        mSPUtil.setSharpness(mSharpness);
        mSPUtil.setFaceSlim(mFaceSlim);
        mSPUtil.setBigEye(mBigEye);
        mSPUtil.setJaw(mJaw);
        mSPUtil.setEyeDistance(mEyeDistance);
        mSPUtil.setFaceShape(mFaceShape);
        mSPUtil.setEyeBrow(mEyeBrow);
        mSPUtil.setEyeCorner(mEyeCorner);
        mSPUtil.setMouthShape(mMouthShape);
        mSPUtil.setNoseLift(mNoseLift);
        mSPUtil.setForehead(mForehead);
        mSPUtil.setLongNose(mLongNose);
        mSPUtil.setFaceShave(mFaceShave);
        mSPUtil.setEyeAlat(mEyeAlat);
        mSPUtil.setFilterId(mFilterId);
    }

    /**
     * Load beauty values from the server for audience
     * This method is called when an audience member joins a live stream
     */
    public void loadBeautyValueForAudience() {
        com.livestreaming.common.http.CommonHttpUtil.getBeautyValue(new com.livestreaming.common.http.HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0 && info.length > 0) {
                    try {
                        MeiYanValueBean valueBean = com.alibaba.fastjson.JSON.parseObject(info[0], MeiYanValueBean.class);
                        
                        // Map values from server to SimpleDataManager properties
                        mMeiBai = valueBean.getMeiBai();
                        mMoPi = valueBean.getMoPi();
                        mHongRun = valueBean.getHongRun();
                        mBrightness = valueBean.getLiangDu();    // Map liangdu to brightness
                        mBigEye = valueBean.getDaYan();          // Map dayan to bigEye
                        mEyeDistance = valueBean.getYanJu();     // Map yanju to eyeDistance
                        mFaceSlim = valueBean.getShouLian();     // Map shoulian to faceSlim
                        mJaw = valueBean.getXiaBa();             // Map xiaba to jaw
                        mFaceShape = valueBean.getETou();        // Map etou to faceShape
                        
                        // Apply the beauty effects
                        if (mMeiYanChangedListener != null) {
                            // Apply base beauty parameters
                            mMeiYanChangedListener.onMeiYanChanged(mMeiBai, true, mMoPi, true, mHongRun, true);
                            
                            // Apply advanced beauty parameters
                            mMeiYanChangedListener.onAdvancedBeautyChanged("brightness", mBrightness);
                            mMeiYanChangedListener.onAdvancedBeautyChanged("sharpness", mSharpness);
                            mMeiYanChangedListener.onAdvancedBeautyChanged("face_slim", mFaceSlim);
                            mMeiYanChangedListener.onAdvancedBeautyChanged("big_eye", mBigEye);
                            mMeiYanChangedListener.onAdvancedBeautyChanged("jaw", mJaw);
                            mMeiYanChangedListener.onAdvancedBeautyChanged("eye_distance", mEyeDistance);
                            mMeiYanChangedListener.onAdvancedBeautyChanged("face_shape", mFaceShape);
                        }
                        
                    } catch (Exception e) {
                        com.livestreaming.common.utils.L.e("SimpleDataManager", "Error parsing beauty values: " + e.getMessage());
                    }
                }
            }
        });
    }

    /**
     * 简版美颜保存数据的时候 使用的是SharedPreferences
     */
    public static class SimpleSPUtil {

        private static final String BEAUTY_SIMPLE_SP = "beauty_simple";
        private static final String BEAUTY_SIMPLE_MEI_BAI = "beauty_simple_mei_bai";
        private static final String BEAUTY_SIMPLE_MO_PI = "beauty_simple_mo_pi";
        private static final String BEAUTY_SIMPLE_HONG_RUN = "beauty_simple_hong_run";
        private static final String BEAUTY_SIMPLE_BRIGHTNESS = "beauty_simple_brightness";
        private static final String BEAUTY_SIMPLE_SHARPNESS = "beauty_simple_sharpness";
        private static final String BEAUTY_SIMPLE_FACE_SLIM = "beauty_simple_face_slim";
        private static final String BEAUTY_SIMPLE_BIG_EYE = "beauty_simple_big_eye";
        private static final String BEAUTY_SIMPLE_JAW = "beauty_simple_jaw";
        private static final String BEAUTY_SIMPLE_EYE_DISTANCE = "beauty_simple_eye_distance";
        private static final String BEAUTY_SIMPLE_FACE_SHAPE = "beauty_simple_face_shape";
        private static final String BEAUTY_SIMPLE_FILTER_ID = "beauty_simple_filter_id";
        private static final String BEAUTY_SIMPLE_EYE_BROW = "beauty_simple_eye_brow";
        private static final String BEAUTY_SIMPLE_EYE_CORNER = "beauty_simple_eye_corner";
        private static final String BEAUTY_SIMPLE_MOUTH_SHAPE = "beauty_simple_mouth_shape";
        private static final String BEAUTY_SIMPLE_NOSE_LIFT = "beauty_simple_nose_lift";
        private static final String BEAUTY_SIMPLE_FOREHEAD = "beauty_simple_forehead";
        private static final String BEAUTY_SIMPLE_LONG_NOSE = "beauty_simple_long_nose";
        private static final String BEAUTY_SIMPLE_FACE_SHAVE = "beauty_simple_face_shave";
        private static final String BEAUTY_SIMPLE_EYE_ALAT = "beauty_simple_eye_alat";

        public void setMeiBai(int meiBai) {
            SpUtil.getInstance().setIntValue(BEAUTY_SIMPLE_MEI_BAI, meiBai);
        }

        public int getMeiBai() {
            return SpUtil.getInstance().getIntValue(BEAUTY_SIMPLE_MEI_BAI, 3);
        }

        public void setMoPi(int moPi) {
            SpUtil.getInstance().setIntValue(BEAUTY_SIMPLE_MO_PI, moPi);
        }

        public int getMoPi() {
            return SpUtil.getInstance().getIntValue(BEAUTY_SIMPLE_MO_PI, 3);
        }

        public void setHongRun(int hongRun) {
            SpUtil.getInstance().setIntValue(BEAUTY_SIMPLE_HONG_RUN, hongRun);
        }

        public int getHongRun() {
            return SpUtil.getInstance().getIntValue(BEAUTY_SIMPLE_HONG_RUN, 3);
        }
        
        public void setBrightness(int brightness) {
            SpUtil.getInstance().setIntValue(BEAUTY_SIMPLE_BRIGHTNESS, brightness);
        }

        public int getBrightness() {
            return SpUtil.getInstance().getIntValue(BEAUTY_SIMPLE_BRIGHTNESS, 3);
        }
        
        public void setSharpness(int sharpness) {
            SpUtil.getInstance().setIntValue(BEAUTY_SIMPLE_SHARPNESS, sharpness);
        }

        public int getSharpness() {
            return SpUtil.getInstance().getIntValue(BEAUTY_SIMPLE_SHARPNESS, 3);
        }
        
        public void setFaceSlim(int faceSlim) {
            SpUtil.getInstance().setIntValue(BEAUTY_SIMPLE_FACE_SLIM, faceSlim);
        }

        public int getFaceSlim() {
            return SpUtil.getInstance().getIntValue(BEAUTY_SIMPLE_FACE_SLIM, 3);
        }
        
        public void setBigEye(int bigEye) {
            SpUtil.getInstance().setIntValue(BEAUTY_SIMPLE_BIG_EYE, bigEye);
        }

        public int getBigEye() {
            return SpUtil.getInstance().getIntValue(BEAUTY_SIMPLE_BIG_EYE, 3);
        }
        
        public void setJaw(int jaw) {
            SpUtil.getInstance().setIntValue(BEAUTY_SIMPLE_JAW, jaw);
        }

        public int getJaw() {
            return SpUtil.getInstance().getIntValue(BEAUTY_SIMPLE_JAW, 3);
        }
        
        public void setEyeDistance(int eyeDistance) {
            SpUtil.getInstance().setIntValue(BEAUTY_SIMPLE_EYE_DISTANCE, eyeDistance);
        }

        public int getEyeDistance() {
            return SpUtil.getInstance().getIntValue(BEAUTY_SIMPLE_EYE_DISTANCE, 3);
        }
        
        public void setFaceShape(int faceShape) {
            SpUtil.getInstance().setIntValue(BEAUTY_SIMPLE_FACE_SHAPE, faceShape);
        }

        public int getFaceShape() {
            return SpUtil.getInstance().getIntValue(BEAUTY_SIMPLE_FACE_SHAPE, 3);
        }

        public void setFilterId(int filterId) {
            SpUtil.getInstance().setIntValue(BEAUTY_SIMPLE_FILTER_ID, filterId);
        }

        public int getFilterId() {
            return SpUtil.getInstance().getIntValue(BEAUTY_SIMPLE_FILTER_ID, 0);
        }

        public void setEyeBrow(int eyeBrow) {
            SpUtil.getInstance().setIntValue(BEAUTY_SIMPLE_EYE_BROW, eyeBrow);
        }

        public int getEyeBrow() {
            return SpUtil.getInstance().getIntValue(BEAUTY_SIMPLE_EYE_BROW, 3);
        }

        public void setEyeCorner(int eyeCorner) {
            SpUtil.getInstance().setIntValue(BEAUTY_SIMPLE_EYE_CORNER, eyeCorner);
        }

        public int getEyeCorner() {
            return SpUtil.getInstance().getIntValue(BEAUTY_SIMPLE_EYE_CORNER, 3);
        }

        public void setMouthShape(int mouthShape) {
            SpUtil.getInstance().setIntValue(BEAUTY_SIMPLE_MOUTH_SHAPE, mouthShape);
        }

        public int getMouthShape() {
            return SpUtil.getInstance().getIntValue(BEAUTY_SIMPLE_MOUTH_SHAPE, 3);
        }

        public void setNoseLift(int noseLift) {
            SpUtil.getInstance().setIntValue(BEAUTY_SIMPLE_NOSE_LIFT, noseLift);
        }

        public int getNoseLift() {
            return SpUtil.getInstance().getIntValue(BEAUTY_SIMPLE_NOSE_LIFT, 3);
        }

        public void setForehead(int forehead) {
            SpUtil.getInstance().setIntValue(BEAUTY_SIMPLE_FOREHEAD, forehead);
        }

        public int getForehead() {
            return SpUtil.getInstance().getIntValue(BEAUTY_SIMPLE_FOREHEAD, 3);
        }

        public void setLongNose(int longNose) {
            SpUtil.getInstance().setIntValue(BEAUTY_SIMPLE_LONG_NOSE, longNose);
        }

        public int getLongNose() {
            return SpUtil.getInstance().getIntValue(BEAUTY_SIMPLE_LONG_NOSE, 3);
        }

        public void setFaceShave(int faceShave) {
            SpUtil.getInstance().setIntValue(BEAUTY_SIMPLE_FACE_SHAVE, faceShave);
        }

        public int getFaceShave() {
            return SpUtil.getInstance().getIntValue(BEAUTY_SIMPLE_FACE_SHAVE, 3);
        }

        public void setEyeAlat(int eyeAlat) {
            SpUtil.getInstance().setIntValue(BEAUTY_SIMPLE_EYE_ALAT, eyeAlat);
        }

        public int getEyeAlat() {
            return SpUtil.getInstance().getIntValue(BEAUTY_SIMPLE_EYE_ALAT, 3);
        }

    }

}
