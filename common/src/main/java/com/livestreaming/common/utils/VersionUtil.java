package com.livestreaming.common.utils;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import androidx.fragment.app.DialogFragment;
import android.text.TextUtils;

import com.livestreaming.common.CommonAppConfig;
import com.livestreaming.common.CommonAppContext;
import com.livestreaming.common.R;
import com.livestreaming.common.activity.AbsActivity;
import com.livestreaming.common.bean.ConfigBean;
import com.livestreaming.common.dialog.NotCancelableDialog;

/**
 * Created by cxf on 2017/10/9.
 */

public class VersionUtil {

    private static String sVersion;

    /**
     * 是否是最新版本
     */
    public static boolean isLatest(String version) {
        if (TextUtils.isEmpty(version)) {
            return true;
        }
        String curVersion = getVersion();
        if (TextUtils.isEmpty(curVersion)) {
            return true;
        }
        return true;// curVersion.equals(version);
    }


    public static void showDialog(final Context context, ConfigBean configBean, final String downloadUrl) {
        if (configBean.getForceUpdate() == 0) {
            DialogUitl.Builder builder = new DialogUitl.Builder(context);
            builder.setTitle(WordUtil.getString(R.string.version_update))
                    .setContent(configBean.getUpdateDes())
                    .setConfrimString(WordUtil.getString(R.string.version_immediate_use))
                    .setCancelString(WordUtil.getString(R.string.version_not_update))
                    .setCancelable(true)
                    .setClickCallback(new DialogUitl.SimpleCallback() {
                        @Override
                        public void onConfirmClick(Dialog dialog, String content) {
                            if (!TextUtils.isEmpty(downloadUrl)) {
                                try {
                                    Intent intent = new Intent();
                                    intent.setAction("android.intent.action.VIEW");
                                    intent.setData(Uri.parse(downloadUrl));
                                    context.startActivity(intent);
                                } catch (Exception e) {
                                    ToastUtil.show(R.string.version_download_url_error);
                                }
                            } else {
                                ToastUtil.show(R.string.version_download_url_error);
                            }
                        }
                    })
                    .build()
                    .show();
        } else {
            NotCancelableDialog dialog = new NotCancelableDialog();
            dialog.setTitle(WordUtil.getString(R.string.version_update));
            dialog.setContent(configBean.getUpdateDes());
            dialog.setConfirmString(WordUtil.getString(R.string.version_immediate_use));
            dialog.setActionListener(new NotCancelableDialog.ActionListener() {
                @Override
                public void onConfirmClick(Context context, DialogFragment dialog) {
                    ConfigBean configBean = CommonAppConfig.getInstance().getConfig();
                    String downloadUrl = configBean.getDownloadApkUrl();
                    if (!TextUtils.isEmpty(downloadUrl)) {
                        try {
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse(downloadUrl));
                            context.startActivity(intent);
                        } catch (Exception e) {
                            ToastUtil.show(R.string.version_download_url_error);
                        }
                    } else {
                        ToastUtil.show(R.string.version_download_url_error);
                    }
                }
            });
            dialog.show(((AbsActivity) context).getSupportFragmentManager(), "VersionUpdateDialog");
        }

    }

    /**
     * 获取版本号
     */
    public static String getVersion() {
        if (TextUtils.isEmpty(sVersion)) {
            try {
                PackageManager manager = CommonAppContext.getInstance().getPackageManager();
                PackageInfo info = manager.getPackageInfo(CommonAppConfig.PACKAGE_NAME, 0);
                sVersion = info.versionName;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return sVersion;
    }

}
