package com.livestreaming.common.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;

import com.yariksoffice.lingver.Lingver;
import com.livestreaming.common.R;
import com.livestreaming.common.interfaces.ActivityResultCallback;
import com.livestreaming.common.interfaces.PermissionCallback;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;



public class ProcessFragment extends Fragment {

    private Context mContext;
    private PermissionCallback mPermissionCallback;
    private ActivityResultCallback mActivityResultCallback;
    private Map<String, Boolean> mMap;
    private ActivityResultLauncher<String[]> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permissions -> {
                boolean isAllGranted = true;
                mMap=permissions;
                if(permissions.values().contains(false)){
                    isAllGranted = false;
                }
                if (mPermissionCallback != null) {
                    mPermissionCallback.onAllGranted();
                }
                if (mPermissionCallback != null) {
                    HashMap<String,Boolean> hashMap=new HashMap<>();
                    hashMap.putAll(mMap);
                    mPermissionCallback.onResult(hashMap);
                }
                mPermissionCallback = null;
            });

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Lingver.getInstance().setLocale(requireContext(), Locale.getDefault());
        mContext = getActivity();
    }

    /**
     * 申请权限
     */
    public void requestPermissions(PermissionCallback callback, String... permissions) {
        if (callback == null || permissions == null || permissions.length == 0) {
            return;
        }
        boolean isAllGranted = true;
        if (mMap == null) {
            mMap = new LinkedHashMap<>();
        } else {
            mMap.clear();
        }
        for (String permission : permissions) {
            boolean isGranted = ContextCompat.checkSelfPermission(mContext, permission) == PackageManager.PERMISSION_GRANTED;
            mMap.put(permission, isGranted);
            if (!isGranted) {
                isAllGranted = false;
            }
        }
        if (isAllGranted) {
            callback.onAllGranted();
            HashMap<String,Boolean> hashMap=new HashMap<>();
            hashMap.putAll(mMap);
            callback.onResult(hashMap);
            mPermissionCallback = null;
        } else {
            mPermissionCallback = callback;
            requestPermissionLauncher.launch(permissions);
        }
    }


    public void startActivityForResult(Intent intent, ActivityResultCallback callback) {
        mActivityResultCallback = callback;
        super.startActivityForResult(intent, 0);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mActivityResultCallback != null) {
            mActivityResultCallback.onResult(resultCode, data);
            if (resultCode == -1) {//RESULT_OK
                mActivityResultCallback.onSuccess(data);
            } else {
                mActivityResultCallback.onFailure();
            }
        }
    }

}
