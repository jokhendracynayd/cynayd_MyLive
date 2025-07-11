package com.livestreaming.live.floatwindow;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import androidx.annotation.RequiresApi;

import com.livestreaming.common.utils.ToastUtil;
import com.livestreaming.live.R;
import com.livestreaming.live.views.LiveChatRoomPlayUtil;

/**
 * 用于在内部自动申请权限
 */

public class FloatPermissionActivity extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestAlertWindowPermission();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestAlertWindowPermission() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, 10001);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 10001) {
            if (FloatWindowPermission.getInstance().hasPermissionOnActivityResult(this)) {
                FloatWindowUtil.getInstance().show();
            } else {
                ToastUtil.show(com.livestreaming.common.R.string.permission_float_window_refused);
                LiveChatRoomPlayUtil.getInstance().setKeepAlive(false);
                LiveChatRoomPlayUtil.getInstance().release();
            }
        }
        finish();
    }

}
