package com.livestreaming.main.views;

import android.content.Context;

import androidx.core.content.ContextCompat;

import android.text.TextUtils;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.livestreaming.common.views.AbsViewHolder;
import com.livestreaming.common.Constants;
import com.livestreaming.main.R;
import com.livestreaming.main.activity.CashActivity;
import com.livestreaming.common.http.HttpCallback;
import com.livestreaming.main.http.MainHttpUtil;
import com.livestreaming.common.utils.DpUtil;
import com.livestreaming.common.utils.ToastUtil;

/**
 * Created by cxf on 2018/10/22.
 */

public class CashAccountViewHolder extends AbsViewHolder implements View.OnClickListener {

    private TextView mBtnChooseType;
    private boolean mShowed;
    private LayoutInflater mInflater;
    private SparseIntArray mSparseIntArray;
    private int mKey;
    private View mGroup1;
    private View mGroup2;
    private View mGroup3;
    private View mGroup4;
    private View mGroup5;
    private EditText mEditAliAccount;
    private EditText mEditAliName;
    private EditText mEditWxAccount;
    private EditText mEditBankName;
    private EditText mEditBankAccount;
    private EditText mEditBankUserName;
    private HttpCallback mAddAccountCallback;

    public CashAccountViewHolder(Context context, ViewGroup parentView) {
        super(context, parentView);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_add_cash_account;
    }

    @Override
    public void init() {
        mSparseIntArray = new SparseIntArray();
        mSparseIntArray.put(Constants.CASH_ACCOUNT_ALI, com.livestreaming.common.R.string.vodaphone_cash);
        mSparseIntArray.put(Constants.CASH_ACCOUNT_WX, com.livestreaming.common.R.string.paypal);
        mSparseIntArray.put(Constants.CASH_ACCOUNT_BANK, com.livestreaming.common.R.string.cash_type_bank);
        mSparseIntArray.put(Constants.CASH_ACCOUNT_USDT, com.livestreaming.common.R.string.USDT);
        mSparseIntArray.put(Constants.CASH_ACCOUNT_BY_HAND, com.livestreaming.common.R.string.by_hand);
        mKey = Constants.CASH_ACCOUNT_ALI;
        mInflater = LayoutInflater.from(mContext);
        findViewById(R.id.root).setOnClickListener(this);
        findViewById(R.id.btn_confirm).setOnClickListener(this);
        mBtnChooseType = (TextView) findViewById(R.id.btn_choose_type);
        mBtnChooseType.setOnClickListener(this);
        mGroup1 = findViewById(R.id.input_group_1);
        mGroup2 = findViewById(R.id.input_group_2);
        mGroup3 = findViewById(R.id.input_group_3);
        mGroup4 = findViewById(R.id.input_group_4);
        mGroup5 = findViewById(R.id.input_group_5);
        mEditAliAccount = (EditText) findViewById(R.id.input_ali_account);
        mEditAliName = (EditText) findViewById(R.id.input_ali_name);
        mEditWxAccount = (EditText) findViewById(R.id.input_wx_account);
        mEditBankName = (EditText) findViewById(R.id.input_bank_name);
        mEditBankAccount = (EditText) findViewById(R.id.input_bank_account);
        mEditBankUserName = (EditText) findViewById(R.id.input_bank_user_name);
        mAddAccountCallback = new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
//                if (code == 0 && info.length > 0) {
//                    CashAccountBean bean = JSON.parseObject(info[0], CashAccountBean.class);
//                    ((CashActivity) mContext).insertAccount(bean);
//                }
                if (code == 0) {
                    if (mContext != null) {
                        ((CashActivity) mContext).loadData();
                    }
                }
                ToastUtil.show(msg);
            }
        };
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.root) {
            removeFromParent();

        } else if (i == R.id.btn_choose_type) {
            chooseType();

        } else if (i == R.id.btn_confirm) {
            addCashAccount();

        }
    }

    @Override
    public void addToParent() {
        super.addToParent();
        mShowed = true;
    }

    @Override
    public void removeFromParent() {
        super.removeFromParent();
        mShowed = false;
    }

    public boolean isShowed() {
        return mShowed;
    }

    private void addCashAccount() {
        String account = null;
        String name = null;
        String bank = null;
        if (mKey == Constants.CASH_ACCOUNT_ALI) {
            account = mEditAliAccount.getText().toString().trim();
            if (TextUtils.isEmpty(account)) {
                ToastUtil.show(com.livestreaming.common.R.string.enter_your_number);
                return;
            }
            name = mEditAliName.getText().toString().trim();
            if (TextUtils.isEmpty(name)) {
                ToastUtil.show(com.livestreaming.common.R.string.cash_input_ali_name);
                return;
            }
            mEditAliAccount.setText("");
            mEditAliName.setText("");
        } else if (mKey == Constants.CASH_ACCOUNT_WX) {
            account = mEditWxAccount.getText().toString().trim();
            if (TextUtils.isEmpty(account)) {
                ToastUtil.show(com.livestreaming.common.R.string.cash_input_wx_account);
                return;
            }
            mEditWxAccount.setText("");
        } else if (mKey == Constants.CASH_ACCOUNT_BANK) {
            bank = mEditBankName.getText().toString().trim();
            if (TextUtils.isEmpty(bank)) {
                ToastUtil.show(com.livestreaming.common.R.string.cash_input_bank_name);
                return;
            }
            account = mEditBankAccount.getText().toString().trim();
            if (TextUtils.isEmpty(account)) {
                ToastUtil.show(com.livestreaming.common.R.string.cash_input_bank_account);
                return;
            }
            name = mEditBankUserName.getText().toString().trim();
            if (TextUtils.isEmpty(name)) {
                ToastUtil.show(com.livestreaming.common.R.string.cash_input_bank_user_name);
                return;
            }
            mEditBankName.setText("");
            mEditBankAccount.setText("");
            mEditBankUserName.setText("");
        } else if (mKey == Constants.CASH_ACCOUNT_USDT) {
            account = ((EditText) mGroup4.findViewById(R.id.input_wallet_number)).getText().toString().trim();
            if (TextUtils.isEmpty(account)) {
                ToastUtil.show(com.livestreaming.common.R.string.enter_wallet_number);
                return;
            }
            name = ((EditText) mGroup4.findViewById(R.id.input_account_name)).getText().toString().trim();
            if (TextUtils.isEmpty(name)) {
                ToastUtil.show(com.livestreaming.common.R.string.enter_your_name);
                return;
            }
            ((EditText) mGroup4.findViewById(R.id.input_wallet_number)).setText("");
            ((EditText) mGroup4.findViewById(R.id.input_account_name)).setText("");
        } else if (mKey == Constants.CASH_ACCOUNT_BY_HAND) {
            account = ((EditText) mGroup5.findViewById(R.id.input_number)).getText().toString().trim();
            if (TextUtils.isEmpty(account)) {
                ToastUtil.show(com.livestreaming.common.R.string.enter_your_number);
                return;
            }
            name = ((EditText) mGroup5.findViewById(R.id.input_account_namee)).getText().toString().trim();
            if (TextUtils.isEmpty(name)) {
                ToastUtil.show(com.livestreaming.common.R.string.enter_your_name);
                return;
            }

            ((EditText) mGroup5.findViewById(R.id.input_number)).setText("");
            ((EditText) mGroup5.findViewById(R.id.input_account_namee)).setText("");
        }
        removeFromParent();
        MainHttpUtil.addCashAccount(account, name, bank, mKey, mAddAccountCallback);
    }

    private void chooseType() {
        View v = mInflater.inflate(R.layout.view_cash_type_pop, null);
        final PopupWindow popupWindow = new PopupWindow(v, DpUtil.dp2px(120), ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(ContextCompat.getDrawable(mContext, R.drawable.bg_pop_cash));
        TextView btn1 = v.findViewById(R.id.btn_1);
        TextView btn2 = v.findViewById(R.id.btn_2);
        TextView btn3 = v.findViewById(R.id.btn_3);
        TextView btn4 = v.findViewById(R.id.btn_4);
        if (mKey == Constants.CASH_ACCOUNT_ALI) {
            btn1.setTag(mSparseIntArray.keyAt(1));
            btn1.setText(mSparseIntArray.valueAt(1));
            btn2.setTag(mSparseIntArray.keyAt(2));
            btn2.setText(mSparseIntArray.valueAt(2));
            btn3.setTag(mSparseIntArray.keyAt(3));
            btn3.setText(mSparseIntArray.valueAt(3));
            btn4.setTag(mSparseIntArray.keyAt(4));
            btn4.setText(mSparseIntArray.valueAt(4));
        } else if (mKey == Constants.CASH_ACCOUNT_WX) {
            btn1.setTag(mSparseIntArray.keyAt(0));
            btn1.setText(mSparseIntArray.valueAt(0));
            btn2.setTag(mSparseIntArray.keyAt(2));
            btn2.setText(mSparseIntArray.valueAt(2));
            btn3.setTag(mSparseIntArray.keyAt(3));
            btn3.setText(mSparseIntArray.valueAt(3));
            btn4.setTag(mSparseIntArray.keyAt(4));
            btn4.setText(mSparseIntArray.valueAt(4));
        } else if (mKey == Constants.CASH_ACCOUNT_BANK) {
            btn1.setTag(mSparseIntArray.keyAt(0));
            btn1.setText(mSparseIntArray.valueAt(0));
            btn2.setTag(mSparseIntArray.keyAt(1));
            btn2.setText(mSparseIntArray.valueAt(1));
            btn3.setTag(mSparseIntArray.keyAt(3));
            btn3.setText(mSparseIntArray.valueAt(3));
            btn4.setTag(mSparseIntArray.keyAt(4));
            btn4.setText(mSparseIntArray.valueAt(4));
        } else if (mKey == Constants.CASH_ACCOUNT_USDT) {
            btn1.setTag(mSparseIntArray.keyAt(0));
            btn1.setText(mSparseIntArray.valueAt(0));
            btn2.setTag(mSparseIntArray.keyAt(1));
            btn2.setText(mSparseIntArray.valueAt(1));
            btn3.setTag(mSparseIntArray.keyAt(2));
            btn3.setText(mSparseIntArray.valueAt(2));
            btn4.setTag(mSparseIntArray.keyAt(4));
            btn4.setText(mSparseIntArray.valueAt(4));
        } else if (mKey == Constants.CASH_ACCOUNT_BY_HAND) {
            btn1.setTag(mSparseIntArray.keyAt(0));
            btn1.setText(mSparseIntArray.valueAt(0));
            btn2.setTag(mSparseIntArray.keyAt(1));
            btn2.setText(mSparseIntArray.valueAt(1));
            btn3.setTag(mSparseIntArray.keyAt(2));
            btn3.setText(mSparseIntArray.valueAt(2));
            btn4.setTag(mSparseIntArray.keyAt(3));
            btn4.setText(mSparseIntArray.valueAt(3));
        }
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
                Object tag = v.getTag();
                if (tag != null) {
                    int key = (int) tag;
                    mBtnChooseType.setText(mSparseIntArray.get(key));
                    mKey = key;
                    switch (key) {
                        case Constants.CASH_ACCOUNT_ALI:
                            if (mGroup1.getVisibility() != View.VISIBLE) {
                                mGroup1.setVisibility(View.VISIBLE);
                            }
                            if (mGroup2.getVisibility() == View.VISIBLE) {
                                mGroup2.setVisibility(View.GONE);
                            }
                            if (mGroup3.getVisibility() == View.VISIBLE) {
                                mGroup3.setVisibility(View.GONE);
                            }
                            if (mGroup4.getVisibility() == View.VISIBLE) {
                                mGroup4.setVisibility(View.GONE);
                            }
                            if (mGroup5.getVisibility() == View.VISIBLE) {
                                mGroup5.setVisibility(View.GONE);
                            }
                            break;
                        case Constants.CASH_ACCOUNT_WX:
                            if (mGroup1.getVisibility() == View.VISIBLE) {
                                mGroup1.setVisibility(View.GONE);
                            }
                            if (mGroup2.getVisibility() != View.VISIBLE) {
                                mGroup2.setVisibility(View.VISIBLE);
                            }
                            if (mGroup3.getVisibility() == View.VISIBLE) {
                                mGroup3.setVisibility(View.GONE);
                            }
                            if (mGroup4.getVisibility() == View.VISIBLE) {
                                mGroup4.setVisibility(View.GONE);
                            }
                            if (mGroup5.getVisibility() == View.VISIBLE) {
                                mGroup5.setVisibility(View.GONE);
                            }
                            break;
                        case Constants.CASH_ACCOUNT_BANK:
                            if (mGroup1.getVisibility() == View.VISIBLE) {
                                mGroup1.setVisibility(View.GONE);
                            }
                            if (mGroup2.getVisibility() == View.VISIBLE) {
                                mGroup2.setVisibility(View.GONE);
                            }
                            if (mGroup3.getVisibility() != View.VISIBLE) {
                                mGroup3.setVisibility(View.VISIBLE);
                            }
                            if (mGroup4.getVisibility() == View.VISIBLE) {
                                mGroup4.setVisibility(View.GONE);
                            }
                            if (mGroup5.getVisibility() == View.VISIBLE) {
                                mGroup5.setVisibility(View.GONE);
                            }
                            break;
                        case Constants.CASH_ACCOUNT_USDT:
                            if (mGroup1.getVisibility() == View.VISIBLE) {
                                mGroup1.setVisibility(View.GONE);
                            }
                            if (mGroup2.getVisibility() == View.VISIBLE) {
                                mGroup2.setVisibility(View.GONE);
                            }
                            if (mGroup3.getVisibility() == View.VISIBLE) {
                                mGroup3.setVisibility(View.GONE);
                            }
                            if (mGroup4.getVisibility() != View.VISIBLE) {
                                mGroup4.setVisibility(View.VISIBLE);
                            }
                            if (mGroup5.getVisibility() == View.VISIBLE) {
                                mGroup5.setVisibility(View.GONE);
                            }
                            break;
                        case Constants.CASH_ACCOUNT_BY_HAND:
                            if (mGroup1.getVisibility() == View.VISIBLE) {
                                mGroup1.setVisibility(View.GONE);
                            }
                            if (mGroup2.getVisibility() == View.VISIBLE) {
                                mGroup2.setVisibility(View.GONE);
                            }
                            if (mGroup3.getVisibility() == View.VISIBLE) {
                                mGroup3.setVisibility(View.GONE);
                            }
                            if (mGroup4.getVisibility() == View.VISIBLE) {
                                mGroup4.setVisibility(View.GONE);
                            }
                            if (mGroup5.getVisibility() != View.VISIBLE) {
                                mGroup5.setVisibility(View.VISIBLE);
                            }
                            break;
                    }
                }
            }
        };
        btn1.setOnClickListener(onClickListener);
        btn2.setOnClickListener(onClickListener);
        btn3.setOnClickListener(onClickListener);
        btn4.setOnClickListener(onClickListener);
        popupWindow.showAsDropDown(mBtnChooseType);
    }


}
