package com.livestreaming.game.views;


import android.app.Activity;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.Nullable;

import com.alibaba.fastjson.JSON;
import com.livestreaming.common.dialog.AbsDialogFragment;
import com.livestreaming.game.R;
import com.livestreaming.game.bean.GameBaishunConfigDTO;
import com.livestreaming.game.bean.GameBaishunDTO;

import org.json.JSONObject;


/**
 * 游戏
 */
public class LiveGamesDialog extends AbsDialogFragment {
    private WebView webView;
    public String url;
    public float scan = 0.8f;
    public GameBaishunDTO gameAction;
    public  GameBaishunConfigDTO mGameBaisConfig;

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        webView.loadUrl("about:blank");
        webView.destroy();

    }

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_game_feishu;
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

        window.setGravity(Gravity.BOTTOM);
        WindowManager.LayoutParams lp = window.getAttributes();
        DisplayMetrics d = getContext().getResources().getDisplayMetrics(); // 获取屏幕宽、高用
        lp.width = (int) (d.widthPixels * 1.0); // 宽度设置为屏幕的1.0
        lp.height = (int) (d.heightPixels * scan); // 高度设置为屏幕的1.0
        window.setAttributes(lp);
        window.setLayout(lp.width, lp.height);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        webView = this.findViewById(R.id.webView);
        webView.setBackgroundColor(0);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        //设置优先加载缓存
        settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setDefaultTextEncodingName("UTF-8");

        settings.setSupportZoom(true);
        settings.setUseWideViewPort(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            settings.setMediaPlaybackRequiresUserGesture(false);
        }
        //设置5.0以上允许加载http和https混合的⻚⾯
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        settings.setDatabasePath(requireActivity().getFilesDir().getAbsolutePath());
        webView.addJavascriptInterface(new NativeBridge(requireActivity()), "NativeBridge");


        // 拦截链接
        webView.setWebViewClient(new WebViewClient() {
            private boolean isContainCloudFront(WebView view, String url) {
                if (!TextUtils.isEmpty(url)) {
                    if (url.contains("cloudfront")) {
                        view.loadUrl(url);
                        return true;
                    }
                }
                return false;
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (isContainCloudFront(view, url)) {
                    return true;
                }
                return super.shouldOverrideUrlLoading(view, url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request != null && request.getUrl() != null ? request.getUrl().toString() : "";
                if (isContainCloudFront(view, url)) {
                    return true;
                }
                return super.shouldOverrideUrlLoading(view, request);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // 网页加载完成后执行的操作
                Log.d("WebView", "Page finished loading: " + url);
                // 在这里可以添加任何你想在加载完成后执行的代码

            }
        });
        webView.loadUrl(url);
    }
    /**
     * 2 * MainActivity
     * 3 * 调⽤js
     * 4
     */
    public void callJs(final String str) {
        WebView webview = this.webView;
        webview.post(new Runnable() {
            @Override
            public void run() {
                webView.loadUrl("javascript:" + str);
                Log.d("BSGAME", "游戏调⽤getConfig:"+str);
            }
        });
    }
    public class NativeBridge {
        public Activity activity;
        NativeBridge(Activity context) {
            this.activity = context;
        }

        @JavascriptInterface
        public void getConfig(String params) {
            Log.d("BSGAME", "游戏调⽤getConfig");
            try {
                JSONObject obj = new JSONObject(params);
                String jsFunName = obj.optString("jsCallback");
                String str = jsFunName + "(" + JSON.toJSONString(mGameBaisConfig)
                        + ")";
                callJs(str);
            } catch (Exception ex) {
                Log.e("getConfig ERROR", ex.getMessage());
            }
        }

        @JavascriptInterface
        public void destroy(String params) {
            Log.d("BSGAME", "游戏调⽤destroy");
            //关闭游戏 TODO 客⼾端
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // 关闭游戏，用户在游戏大厅点击关闭按钮
                    dismiss();
                }
            });
        }

        @JavascriptInterface
        public void gameRecharge(String params) {
            Log.d("BSGAME", "游戏调⽤gameRecharge");
            //拉起充值商城 TODO 客⼾端
        }

        @JavascriptInterface
        public void gameLoaded(String params) {
            Log.d("BSGAME", "游戏调⽤gameLoaded");
            //游戏加载完毕 TODO 客⼾端

        }

    }
    /**
     * 原生调用JS
     */
   /* public void nativeToJs() {
        this.webView.post(new Runnable() {
            @Override
            public void run() {
                //通知游戏刷新金币
                webView.loadUrl("javascript:HttpTool.NativeToJs('recharge')");
            }
        });
    }*/

}
