package com.livestreaming.main.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.livestreaming.common.activity.WebViewActivity;
import com.livestreaming.common.dialog.AbsDialogFragment;
import com.livestreaming.common.interfaces.CommonCallback;
import com.livestreaming.common.utils.DpUtil;
import com.livestreaming.common.utils.LanguageUtil;
import com.livestreaming.common.utils.WordUtil;
import com.livestreaming.main.R;

/**
 * 全功能模式弹窗提示
 */
public class FullFunctionDialogFragment extends AbsDialogFragment implements View.OnClickListener {

    private TextView mTitle;
    private TextView mContent;
    private CommonCallback<Boolean> mOnConfirmClick;


    @Override
    protected int getLayoutId() {
        return R.layout.dialog_full_function;
    }

    @Override
    protected int getDialogStyle() {
        return com.livestreaming.common.R.style.dialog;
    }

    @Override
    protected boolean canCancel() {
        return false;
    }

    @Override
    protected void setWindowAttributes(Window window) {
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = DpUtil.dp2px(280);
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.CENTER;
        window.setAttributes(params);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        return true;
                    }
                    return false;
                }
            });
        }
        findViewById(R.id.btn_cancel).setOnClickListener(this);
        findViewById(R.id.btn_confirm).setOnClickListener(this);
        mTitle = findViewById(R.id.title);
        mContent = findViewById(R.id.content);

        if (mTitle != null) {
            mTitle.setText(WordUtil.getString(com.livestreaming.common.R.string.login_tip_001));
        }
        String content = WordUtil.getString(com.livestreaming.common.R.string.login_tip_002);
        String loginTip = WordUtil.getString(com.livestreaming.common.R.string.login_tip_003);

        SpannableString spannableContent = new SpannableString(content);
        SpannableString spannableTip = new SpannableString(loginTip);
        JSONArray msgArray = new JSONArray();
        JSONObject obj0 = new JSONObject();
        obj0.put("title", WordUtil.getString(com.livestreaming.common.R.string.login_tip_004));
        obj0.put("url", LanguageUtil.isZh() ? "file:///android_asset/yszc.html" : "file:///android_asset/yszc.html_en");
        JSONObject obj1 = new JSONObject();
        obj1.put("title", WordUtil.getString(com.livestreaming.common.R.string.login_tip_005));
        obj1.put("url", LanguageUtil.isZh() ? "file:///android_asset/fwxy.html" : "file:///android_asset/fwxy_en.html");
        msgArray.add(obj0);
        msgArray.add(obj1);

        for (int i = 0, size = msgArray.size(); i < size; i++) {

            final JSONObject msgItem = msgArray.getJSONObject(i);
            String title = msgItem.getString("title");
            ClickableSpan clickableSpan = new ClickableSpan() {

                @Override
                public void updateDrawState(TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setColor(0xff3399ee);
                    ds.setUnderlineText(false);
                }

                @Override
                public void onClick(View widget) {
                    WebViewActivity.forward(mContext, msgItem.getString("url"));
                }
            };


            int startIndex = content.indexOf(title);
            if (startIndex >= 0) {
                int endIndex = startIndex + title.length();
                spannableContent.setSpan(clickableSpan, startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }


            int startIndex2 = loginTip.indexOf(title);
            if (startIndex2 >= 0) {
                int endIndex2 = startIndex2 + title.length();
                spannableTip.setSpan(clickableSpan, startIndex2, endIndex2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

        }

        mContent.setText(spannableContent);
        mContent.setMovementMethod(LinkMovementMethod.getInstance());//不设置 没有点击事件
        mContent.setHighlightColor(Color.TRANSPARENT); //设置点击后的颜色为透明


    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_cancel) {
            dismiss();
        } else if (id == R.id.btn_confirm) {
            dismiss();
            if (mOnConfirmClick != null) {
                mOnConfirmClick.callback(true);
            }
        }
    }


    public void setAgreeCallback(CommonCallback<Boolean> agreeCallback) {
        mOnConfirmClick = agreeCallback;
    }

    @Override
    public void onDestroy() {
        mContext = null;
        mOnConfirmClick = null;
        super.onDestroy();
    }
}
