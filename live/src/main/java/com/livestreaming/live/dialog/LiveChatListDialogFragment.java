package com.livestreaming.live.dialog;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.CLIPBOARD_SERVICE;
import static io.agora.utils2.internal.CommonUtility.getSystemService;

import android.Manifest;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;

import com.livestreaming.common.CommonAppConfig;
import com.livestreaming.common.Constants;
import com.livestreaming.common.activity.WebViewActivity;
import com.livestreaming.common.dialog.AbsDialogFragment;
import com.livestreaming.common.interfaces.ImageResultCallback;
import com.livestreaming.common.utils.DpUtil;
import com.livestreaming.common.utils.L;
import com.livestreaming.common.utils.MediaUtil;
import com.livestreaming.common.utils.RouteUtil;
import com.livestreaming.common.utils.ToastUtil;
import com.livestreaming.live.R;
import com.livestreaming.live.activity.LiveActivity;

import java.io.File;

/**
 * Created by cxf on 2018/10/24.
 * 直播间私信聊天列表
 */

public class LiveChatListDialogFragment extends AbsDialogFragment {

    private ProgressBar mProgressBar;
    private WebView mWebView;
    private final int CODE_AUTH = 8001;//我的认证
    private final int CODE_FAMILY = 8002;//我的家族

    private ValueCallback<Uri[]> fileBackCalback;

    @Override
    protected int getLayoutId() {
        return com.livestreaming.im.R.layout.dialog_live_chat;
    }

    @Override
    protected int getDialogStyle() {
        return com.livestreaming.common.R.style.dialog2;
    }

    @Override
    protected boolean canCancel() {
        return true;
    }

    @Override
    protected void setWindowAttributes(Window window) {
        window.setWindowAnimations(com.livestreaming.common.R.style.bottomToTopAnim);
        window.setGravity(Gravity.BOTTOM);
        WindowManager.LayoutParams lp = window.getAttributes();
        try {
            DisplayMetrics d = getContext().getResources().getDisplayMetrics(); // 获取屏幕宽、高用
            lp.width = (int) (d.widthPixels * 1.0); // 宽度设置为屏幕的1.0
            lp.height = (int) (d.heightPixels * 0.8); // 高度设置为屏幕的1.0
            window.setAttributes(lp);
            window.setLayout(lp.width, lp.height);
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        } catch (Exception e) {

        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                    // Handle the back press here
                    onBackPressed();
                    return true;
                } else {
                    return false;
                }
            }
        });
        return dialog;
    }



    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getArguments() == null) return;
        String url = CommonAppConfig.getHtmlUrl(getArguments().getString(Constants.URL));
        mRootView = (LinearLayout) findViewById(com.livestreaming.common.R.id.rootView);
        mWebView = findViewById(R.id.webView);
        mWebView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                mRootView.getWindowVisibleDisplayFrame(r);
                int heightDiff = mRootView.getRootView().getHeight() - (r.bottom - r.top);

                // Check if the keyboard is open
                if (heightDiff > 200) { // Threshold for keyboard height
                    // Keyboard is open
                    adjustLayoutForKeyboardOpen();
                } else {
                    // Keyboard is closed
                    resetLayoutHeight();
                }
            }
        });
        mProgressBar = (ProgressBar) findViewById(com.livestreaming.common.R.id.progressbar);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.topMargin = DpUtil.dp2px(1);
        mWebView.setLayoutParams(params);
        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setSupportZoom(false);
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                L.e("H5-------->" + url);
                if (url.startsWith(Constants.COPY_PREFIX)) {
                    String content = url.substring(Constants.COPY_PREFIX.length());
                    if (!TextUtils.isEmpty(content)) {
                        //copy(content);
                    }
                } else if (url.startsWith(Constants.TEL_PREFIX)) {
                    String phoneNum = url.substring(Constants.TEL_PREFIX.length());
                    if (!TextUtils.isEmpty(phoneNum)) {
                        callPhone(phoneNum);
                    }
                } else if (url.startsWith(Constants.AUTH_PREFIX)) {
                    RouteUtil.forwardAuth((LiveActivity) mContext, CODE_AUTH);
                } else {
                    view.loadUrl(url);
                }
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {

            }
        });
        mWebView.setWebChromeClient(new WebChromeClient() {

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100) {
                    mProgressBar.setVisibility(View.GONE);
                } else {
                    mProgressBar.setProgress(newProgress);
                }
            }

            @Override
            public boolean onShowFileChooser(WebView webView,
                                             final ValueCallback<Uri[]> filePathCallback,
                                             FileChooserParams fileChooserParams) {
                LiveChatListDialogFragment.this.fileBackCalback = filePathCallback;
                if (Build.VERSION.SDK_INT > 33) {
                    String[] permissions = new String[]{
                            Manifest.permission.READ_MEDIA_IMAGES,
                            Manifest.permission.READ_MEDIA_AUDIO,
                            Manifest.permission.READ_MEDIA_VIDEO,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED,
                            Manifest.permission.CAMERA,
                            Manifest.permission.RECORD_AUDIO
                    };
                    requestPermissionLauncher.launch(permissions);
                } else if (Build.VERSION.SDK_INT == 33) {
                    String[] permissions = new String[]{
                            Manifest.permission.READ_MEDIA_IMAGES,
                            Manifest.permission.READ_MEDIA_AUDIO,
                            Manifest.permission.READ_MEDIA_VIDEO,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.CAMERA,
                            Manifest.permission.RECORD_AUDIO
                    };
                    requestPermissionLauncher.launch(permissions);
                } else {
                    MediaUtil.getImageByAlumb((LiveActivity) mContext, false, new ImageResultCallback() {
                        @Override
                        public void beforeCamera() {

                        }

                        @Override
                        public void onSuccess(File file) {
                            filePathCallback.onReceiveValue(new Uri[]{Uri.fromFile(file)});
                        }

                        @Override
                        public void onFailure() {
                            filePathCallback.onReceiveValue(null);
                        }
                    });
                }
                return true;
            }

        });
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        mWebView.loadUrl(url);
    }
    private void adjustLayoutForKeyboardOpen() {
        int newHeight = (int) (mRootView.getHeight() * 0.8); // 60% of screen height
        mWebView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                newHeight));
    }

    private void resetLayoutHeight() {
        mWebView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
    }
    /**
     * 2 * MainActivity
     * 3 * 调⽤js
     * 4
     */

    private ActivityResultLauncher<String[]> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permissions -> {
                MediaUtil.callBackForImageSelectionAlboum((LiveActivity) mContext, false, new ImageResultCallback() {
                    @Override
                    public void beforeCamera() {

                    }

                    @Override
                    public void onSuccess(File file) {
                        fileBackCalback.onReceiveValue(new Uri[]{Uri.fromFile(file)});
                    }

                    @Override
                    public void onFailure() {
                        fileBackCalback.onReceiveValue(null);
                    }
                });
            });


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        switch (requestCode) {
            case CODE_AUTH://我的认证
            case CODE_FAMILY://我的家族
                if (resultCode == RESULT_OK) {
                    dismiss();
                }
                break;
        }
    }


    protected boolean canGoBack() {
        return mWebView != null && mWebView.canGoBack();
    }

    public void onBackPressed() {
        if (isNeedExitActivity()) {
            dismiss();
        } else {
            if (canGoBack()) {
                mWebView.goBack();
            } else {
                dismiss();
            }
        }
    }


    private boolean isNeedExitActivity() {
        if (mWebView != null) {
            String url = mWebView.getUrl();
            if (!TextUtils.isEmpty(url)) {
                return url.contains("appapi/Auth/succ");//身份认证成功页面

            }
        }
        return false;
    }

    public static void forward(Context context, String url) {
        Intent intent = new Intent(context, WebViewActivity.class);
        intent.putExtra(Constants.URL, url);
        context.startActivity(intent);
    }


    @Override
    public void onDestroy() {
        if (mWebView != null) {
            ViewGroup parent = (ViewGroup) mWebView.getParent();
            if (parent != null) {
                parent.removeView(mWebView);
            }
            mWebView.destroy();
        }
        super.onDestroy();
    }

    /**
     * 复制到剪贴板
     */
    private void copy(String content) {
        ClipboardManager cm = (ClipboardManager) getSystemService(mContext, CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("text", content);
        cm.setPrimaryClip(clipData);
        ToastUtil.show(com.livestreaming.common.R.string.copy_success);
    }

    /**
     * 拨打电话
     */
    private void callPhone(String phoneNum) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        Uri data = Uri.parse("tel:" + phoneNum);
        intent.setData(data);
        startActivity(intent);
    }


}
