package com.livestreaming.common.glide;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.widget.ImageView;

import com.livestreaming.common.CommonAppContext;
import com.bumptech.glide.Glide;

import java.io.File;

/**
 * Created by cxf on 2017/8/9.
 */

public class ImgLoader {
    private static final ImgLoaderStrategy mImgLoaderStrategy;

    static {
        mImgLoaderStrategy = new CoilImgLoader();
    }

    public static void loadSvga (Context context, String url,ImageView y) {
        if(url==null){
            return;
        }
        if(url.isEmpty()||url.isBlank()){
            return;
        }
         Glide.with(CommonAppContext.getInstance()).load(url).into(y);
    }

    public static void loadSvga (Context context, int res,ImageView y) {
         Glide.with(CommonAppContext.getInstance()).load(res).into(y);
    }
    public static void display(Context context, String url, ImageView imageView) {
      //  mImgLoaderStrategy.display(context, url, imageView);
        try {
            Glide.with(context).load(url).into(imageView);
        }catch (Exception e){

        }
    }


    public static void displayAvatar(Context context, String url, ImageView imageView) {
        try {
            Glide.with(context).load(url).into(imageView);
        }catch (Exception e){

        }
    }


    public static void display(Context context, File file, ImageView imageView) {
        mImgLoaderStrategy.display(context, file, imageView);
    }

    public static void display(Context context, Uri uri, ImageView imageView) {
        mImgLoaderStrategy.display(context, uri, imageView);
    }

    public static void display(Context context, int res, ImageView imageView) {
        mImgLoaderStrategy.display(context, res, imageView);
    }

    /**
     * 显示视频封面缩略图
     */
    public static void displayVideoThumb(Context context, String videoPath, ImageView imageView) {
        mImgLoaderStrategy.displayVideoThumb(context, videoPath, imageView);
    }

    /**
     * 显示视频封面缩略图
     */
    public static void displayVideoThumb(Context context, Uri uri, ImageView imageView) {
        mImgLoaderStrategy.displayVideoThumb(context, uri, imageView);
    }

    /**
     * 显示视频封面缩略图
     */
    public static void displayVideoThumb(Context context, File file, ImageView imageView) {
        mImgLoaderStrategy.displayVideoThumb(context, file, imageView);
    }

    public static void displayDrawable(Context context, String url, final DrawableCallback callback) {
        mImgLoaderStrategy.displayDrawable(context, url, callback);
    }


    public static void displayDrawable(Context context, File file, final DrawableCallback callback) {
        mImgLoaderStrategy.displayDrawable(context, file, callback);
    }


    /**
     * 显示模糊的毛玻璃图片
     */
    public static void displayBlur(Context context, String url, ImageView imageView) {
        mImgLoaderStrategy.displayBlur(context, url, imageView);
    }

    public static String getCacheSize(){
        return mImgLoaderStrategy.getCacheSize();
    }

    public static void clearImageCache(){
        mImgLoaderStrategy.clearImageCache();
    }

    public interface DrawableCallback {
        void onLoadSuccess(Drawable drawable);

        void onLoadFailed();
    }


}
