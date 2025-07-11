package com.livestreaming.main.activity;

import android.content.Intent;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.livestreaming.common.CommonAppConfig;
import com.livestreaming.common.Constants;
import com.livestreaming.common.activity.AbsActivity;
import com.livestreaming.common.bean.UserBean;
import com.livestreaming.common.http.HttpCallback;
import com.livestreaming.common.utils.ToastUtil;
import com.livestreaming.common.utils.WordUtil;
import com.livestreaming.main.R;
import com.livestreaming.main.http.MainHttpConsts;
import com.livestreaming.main.http.MainHttpUtil;

/**
 * Created by cxf on 2018/9/29.
 * 设置昵称
 */

public class EditNameActivity extends AbsActivity implements View.OnClickListener {

    private EditText mEditText;
    private View mBtnClear;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_edit_name;
    }

    @Override
    protected void main() {
        setTitle(WordUtil.getString(com.livestreaming.common.R.string.edit_profile_update_nickname));
        mEditText = (EditText) findViewById(R.id.edit);
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (mBtnClear != null) {
                    if (TextUtils.isEmpty(s)) {
                        if (mBtnClear.getVisibility() == View.VISIBLE) {
                            mBtnClear.setVisibility(View.INVISIBLE);
                        }
                    } else {
                        if (mBtnClear.getVisibility() != View.VISIBLE) {
                            mBtnClear.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mBtnClear = findViewById(R.id.btn_clear);
        mBtnClear.setOnClickListener(this);
        findViewById(R.id.btn_save).setOnClickListener(this);
        String content = getIntent().getStringExtra(Constants.NICK_NAME);
        if (!TextUtils.isEmpty(content)) {
            mEditText.setText(content);
            mEditText.setSelection(content.length());
        }
    }

    private void clear() {
        if (mEditText != null) {
            mEditText.setText(null);
        }
    }

    private void save() {
        final String content = mEditText.getText().toString().trim();
        if (TextUtils.isEmpty(content)) {
            ToastUtil.show(com.livestreaming.common.R.string.edit_profile_name_empty);
            return;
        }
        MainHttpUtil.updateFields("{\"user_nickname\":\"" + content + "\"}", new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0) {
                    if (info.length > 0) {
                        JSONObject obj = JSON.parseObject(info[0]);
                        ToastUtil.show(obj.getString("msg"));
                        UserBean u = CommonAppConfig.getInstance().getUserBean();
                        if (u != null) {
                            u.setUserNiceName(content);
                        }
                        Intent intent = getIntent();
                        intent.putExtra(Constants.NICK_NAME, content);
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                } else {
                    ToastUtil.show(msg);
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (!canClick()) {
            return;
        }
        int id = v.getId();
        if (id == R.id.btn_clear) {
            clear();
        } else if (id == R.id.btn_save) {
            save();
        }
    }


    @Override
    protected void onDestroy() {
        MainHttpUtil.cancel(MainHttpConsts.UPDATE_FIELDS);
        super.onDestroy();
    }
}
