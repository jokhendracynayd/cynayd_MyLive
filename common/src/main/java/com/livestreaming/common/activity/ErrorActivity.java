package com.livestreaming.common.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.livestreaming.common.CommonAppContext;
import com.livestreaming.common.R;
import com.livestreaming.common.utils.ToastUtil;



public class ErrorActivity extends AbsActivity {

    public static void forward(String title, String errorInfo) {
        Intent intent = new Intent(CommonAppContext.getInstance(), ErrorActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("title", title);
        intent.putExtra("error", errorInfo);
        CommonAppContext.getInstance().startActivity(intent);
    }

    private TextView mTextView;
    private String mErrorInfo;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_error;
    }

    @Override
    protected void main() {
        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        mErrorInfo = intent.getStringExtra("error");
        setTitle(title);
        mTextView = (TextView) findViewById(R.id.text);
        mTextView.setText(mErrorInfo);
        findViewById(R.id.btn_copy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copyError();
            }
        });
    }

    private void copyError() {
        if (TextUtils.isEmpty(mErrorInfo)) {
            return;
        }
        ClipboardManager clipboardManager = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("text", mErrorInfo);
        clipboardManager.setPrimaryClip(clipData);
        ToastUtil.show(R.string.copy_success);
    }
}
