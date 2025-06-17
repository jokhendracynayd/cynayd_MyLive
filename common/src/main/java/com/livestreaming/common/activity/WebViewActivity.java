package com.livestreaming.common.activity;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.livestreaming.common.CommonAppConfig;
import com.livestreaming.common.Constants;
import com.livestreaming.common.R;
import com.livestreaming.common.activity.AbsActivity;
import com.livestreaming.common.interfaces.ActivityResultCallback;
import com.livestreaming.common.interfaces.ImageResultCallback;
import com.livestreaming.common.utils.ActivityResultUtil;
import com.livestreaming.common.utils.DpUtil;
import com.livestreaming.common.utils.L;
import com.livestreaming.common.utils.MediaUtil;
import com.livestreaming.common.utils.RouteUtil;
import com.livestreaming.common.utils.ToastUtil;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class WebViewActivity extends AbsActivity {

    private ProgressBar mProgressBar;
    private WebView mWebView;
    LinearLayout rootView;
    private final int CODE_AUTH = 8001;//我的认证
    private final int CODE_FAMILY = 8002;//我的家族

    private ValueCallback<Uri[]> fileBackCalback;
    private ActivityResultLauncher<String[]> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permissions -> {
                MediaUtil.callBackForImageSelectionAlboum(WebViewActivity.this, false, new ImageResultCallback() {
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
    private String token = "";

    @Override
    protected int getLayoutId() {
        return R.layout.activity_webview;
    }

    @Override
    protected void main() {
        String url = CommonAppConfig.getHtmlUrl(getIntent().getStringExtra(Constants.URL));
        token = Objects.requireNonNull(getIntent().getExtras()).getString("header", "");
        rootView = (LinearLayout) findViewById(com.livestreaming.common.R.id.rootView);
        mWebView = findViewById(R.id.webView);
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect rect = new Rect();
                rootView.getWindowVisibleDisplayFrame(rect);

                int screenHeight = rootView.getRootView().getHeight();
                int keypadHeight = screenHeight - rect.bottom;
                adjustWebViewForKeyboard(keypadHeight > screenHeight * 0.15);
            }
        });
        mProgressBar = (ProgressBar) findViewById(com.livestreaming.common.R.id.progressbar);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.topMargin = DpUtil.dp2px(1);
        mWebView.setLayoutParams(params);
        mWebView.setOverScrollMode(View.OVER_SCROLL_NEVER);
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
                        copy(content);
                    }
                } else if (url.startsWith(Constants.TEL_PREFIX)) {
                    String phoneNum = url.substring(Constants.TEL_PREFIX.length());
                    if (!TextUtils.isEmpty(phoneNum)) {
                        callPhone(phoneNum);
                    }
                } else if (url.startsWith(Constants.AUTH_PREFIX)) {
                    RouteUtil.forwardAuth(WebViewActivity.this, CODE_AUTH);
                } else if (url.startsWith("https://donalive.net/redirection/")) {
                    url = url.replace("https://donalive.net/redirection/", "");
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    try {
                        view.getContext().startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(view.getContext(), "WhatsApp not installed", Toast.LENGTH_SHORT).show();
                    }
                    return true; // Prevent WebView from loading this URL
                } else {
                    view.loadUrl(url);
                }
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                setTitle(view.getTitle());
                setTitle(view.getTitle());
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
                WebViewActivity.this.fileBackCalback = filePathCallback;
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
                    MediaUtil.getImageByAlumb(WebViewActivity.this, false, new ImageResultCallback() {
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
        webSettings.setDomStorageEnabled(true);
        mWebView.addJavascriptInterface(new WebAppInterface(mContext), "Android");
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        if (!token.isEmpty() && !token.isBlank()) {
            url += "?" + "token=" + token;
            Log.e("chatUrl", url);
            mWebView.loadUrl(url);

            // saveToLocalStorage("token",token);
        } else {
            mWebView.loadUrl(url);
        }
    }

    public void saveToLocalStorage(String key, String value) {
        // Save key-value pair to localStorage
        // This method can be called from JavaScript
        mWebView.evaluateJavascript("LocalStorage.setItem('" + key + "', '" + value + "');", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                Log.e("logintoken", value);
            }
        });
    }

    private Map<String, String> createCustomHeaders() {

        Map<String, String> headers = new HashMap<>();
        headers.put("authorization", "Bearer " + token);
        return headers;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        switch (requestCode) {
            case CODE_AUTH://我的认证
            case CODE_FAMILY://我的家族
                if (resultCode == RESULT_OK) {
                    finish();
                }
                break;
        }
    }

    private void adjustWebViewForKeyboard(boolean keyboardOpen) {
        if (mWebView != null) {
            if (keyboardOpen) {
                // تعيين ارتفاع WebView إلى قيمة أقل لدفعه للأعلى
                int newHeight = (int) (rootView.getHeight() * 0.46); // 50% من ارتفاع الشاشة
                mWebView.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        newHeight));
            } else {
                // إعادة تعيين ارتفاع WebView عند إغلاق لوحة المفاتيح
                mWebView.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT));
            }
        }
    }

    protected boolean canGoBack() {
        return mWebView != null && mWebView.canGoBack();
    }

    @Override
    public void onBackPressed() {
        if (canGoBack()) {
            mWebView.goBack();
        } else {
            mWebView.clearCache(true);
            mWebView.destroy();
            mWebView = null;
            finish();
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

    public static void forward(Context context, String url, String header) {
        Intent intent = new Intent(context, WebViewActivity.class);
        intent.putExtra(Constants.URL, url);
        intent.putExtra("header", header);
        context.startActivity(intent);
    }

    // private void onCreateFamily() {
    //     RouteUtil.forwardCreateFamily();
    //}

    @Override
    protected void onDestroy() {
        if (mWebView != null) {
            ViewGroup parent = (ViewGroup) mWebView.getParent();
            if (parent != null) {
                parent.removeView(mWebView);
            }
            mWebView.clearCache(true);
            mWebView.destroy();
            mWebView = null;
        }
        super.onDestroy();
    }

    /**
     * 复制到剪贴板
     */
    private void copy(String content) {
        ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("text", content);
        cm.setPrimaryClip(clipData);
        ToastUtil.show(R.string.copy_success);
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

    public class WebAppInterface {
        Context mContext;

        WebAppInterface(Context c) {
            mContext = c;
        }

        @JavascriptInterface
        public void sendValueToAndroid(String value) {
//            Log.e("Action", value);
//            if (value.equals("addNewFamily")) {
//                onCreateFamily();
//            }
        }
    }
    private void onCreateFamily() {
        RouteUtil.forwardCreateFamily();
    }


}
