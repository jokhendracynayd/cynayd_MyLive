package com.livestreaming.common.utils;

import com.livestreaming.common.CommonAppConfig;
import com.livestreaming.common.http.CommonHttpConsts;
import com.livestreaming.common.http.FileDownloadCallback;
import com.livestreaming.common.interfaces.CommonCallback;

import java.io.File;

/**
 * Created by cxf on 2018/10/17.
 */

public class GifCacheUtil {

    public static void getFile(String fileName, String url, final CommonCallback<File> commonCallback) {
        if (commonCallback == null) {
            return;
        }
        File dir = new File(CommonAppConfig.GIF_PATH);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(dir, fileName);
        if (file.exists()) {
            commonCallback.callback(file);
        } else {
            new DownloadUtil(CommonHttpConsts.DOWNLOAD_GIF).download(dir, fileName, url, new FileDownloadCallback() {

                @Override
                public void onSuccess(File file) {
                    commonCallback.callback(file);
                }

                @Override
                public void onError(Throwable e) {
                    commonCallback.callback(null);
                }
            });
        }
    }

}
