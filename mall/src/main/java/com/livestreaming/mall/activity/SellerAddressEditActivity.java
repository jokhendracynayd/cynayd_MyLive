package com.livestreaming.mall.activity;

import android.content.Intent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.livestreaming.common.CommonAppConfig;
import com.livestreaming.common.Constants;
import com.livestreaming.common.activity.AbsActivity;
import com.livestreaming.common.http.HttpCallback;
import com.livestreaming.common.utils.ToastUtil;
import com.livestreaming.common.utils.WordUtil;
import com.livestreaming.mall.R;
import com.livestreaming.mall.http.MallHttpConsts;
import com.livestreaming.mall.http.MallHttpUtil;


/**
 * 卖家 编辑退货地址
 */
public class SellerAddressEditActivity extends AbsActivity implements View.OnClickListener {

    EditText first_name;
    EditText last_name;
    EditText phone_number;
    EditText zipcode_value;
    EditText house_number;
    EditText full_address;
    EditText city_value;
    EditText street_apartment;
    private String countryName = "", countryCode = "";

    @Override
    protected int getLayoutId() {
        return R.layout.address_edit_add_seller_layout;
    }

    @Override
    protected void main() {
        super.main();
        setTitle(WordUtil.getString(com.livestreaming.common.R.string.mall_154));
        findViewById(R.id.country_select).setOnClickListener(this);
        findViewById(R.id.save_button).setOnClickListener(this);
        first_name = findViewById(R.id.first_name);
        last_name = findViewById(R.id.last_name);
        phone_number = findViewById(R.id.phone_number);
        zipcode_value = findViewById(R.id.zipcode_value);
        house_number = findViewById(R.id.house_number);
        full_address = findViewById(R.id.full_address);
        street_apartment = findViewById(R.id.street_apartment);
        city_value = findViewById(R.id.city_value);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101 && resultCode == RESULT_OK && data != null) {
            String code = data.getStringExtra(Constants.TO_NAME);
            countryName = data.getStringExtra("country_name");
            countryCode = code;
            ((TextView) findViewById(R.id.country_name)).setText(countryName);
            ((TextView)findViewById(R.id.code)).setText(countryCode);
        }
    }
   /* private void modifyAddress() {
        String name = mName.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            ToastUtil.show(com.livestreaming.common.R.string.mall_150);
            return;
        }
        String phoneNum = mPhoneNum.getText().toString().trim();
        if (TextUtils.isEmpty(phoneNum)) {
            ToastUtil.show(com.livestreaming.common.R.string.mall_151);
            return;
        }
        if (phoneNum != null) {
            if (!phoneNum.contains(mCountryCode)) {
                phoneNum = mCountryCode + phoneNum;
            }
        }
        String address = mAddress.getText().toString().trim();
        if (TextUtils.isEmpty(address)) {
            ToastUtil.show(com.livestreaming.common.R.string.mall_153);
            return;
        }
        if (mProvinceVal == null || mProvinceVal == "") {
            mProvinceVal = mArea.toString();
            mCityVal = mArea.toString();
            mZoneVal = mArea.toString();
        }
        MallHttpUtil.buyerModifyAddress(
                name,
                phoneNum,
                mProvinceVal,
                mCityVal,
                mZoneVal,
                address,
                mCheckBox.isChecked() ? "1" : "0",
                mBean.getId(),
                new HttpCallback() {
                    @Override
                    public void onSuccess(int code, String msg, String[] info) {
                        if (code == 0) {
                            setResult(RESULT_OK);
                            finish();
                        }
                        ToastUtil.show(msg);
                    }
                }
        );
    }*/


    /**
     * 删除收货地址
     */
//    private void deleteAddress() {
//        if (mBean == null) {
//            return;
//        }
//        new DialogUitl.Builder(mContext)
//                .setContent(WordUtil.getString(com.livestreaming.common.R.string.mall_163))
//                .setCancelable(true)
//                .setBackgroundDimEnabled(true)
//                .setClickCallback(new DialogUitl.SimpleCallback() {
//                    @Override
//                    public void onConfirmClick(Dialog dialog, String content) {
//                        MallHttpUtil.buyerDeleteAddress(mBean.getId(), new HttpCallback() {
//                            @Override
//                            public void onSuccess(int code, String msg, String[] info) {
//                                if (code == 0) {
//                                    setResult(RESULT_OK);
//                                    finish();
//                                }
//                                ToastUtil.show(msg);
//                            }
//                        });
//                    }
//                })
//                .build()
//                .show();
//    }
    @Override
    protected void onDestroy() {
        MallHttpUtil.cancel(MallHttpConsts.BUYER_ADD_ADDRESS);
        MallHttpUtil.cancel(MallHttpConsts.BUYER_DELETE_ADDRESS);
        MallHttpUtil.cancel(MallHttpConsts.BUYER_MODIFY_ADDRESS);
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.country_select) {
            selectCountry();
        } else if (id == R.id.save_button) {
            saveAddress();
        }

    }

    private void saveAddress() {
        if (isValidAddress()) {
            MallHttpUtil.modifyRefundAddress(
                    first_name.getText() + " " + last_name.getText(),
                    countryCode + phone_number.getText(),
                    countryName,
                    city_value.getText().toString(),
                    countryName,
                    full_address.getText().toString(),
                    zipcode_value.getText().toString(),
                    Integer.parseInt(house_number.getText().toString()),
                    street_apartment.getText().toString(),
            new HttpCallback() {
                @Override
                public void onSuccess(int code, String msg, String[] info) {
                    if (code == 0) {
                        setResult(RESULT_OK);
                        finish();
                    }
                    ToastUtil.show(msg);
                }
            }
            );
        }
    }

    private boolean isValidAddress() {

        if (countryName == null || countryName.isEmpty()) {
            ToastUtil.show(getString(com.livestreaming.common.R.string.please_select_country));
            return false;
        }
        if (first_name.getText() == null || first_name.getText().toString().isEmpty()) {
            ToastUtil.show(getString(com.livestreaming.common.R.string.please_enter_first_name));
            return false;
        }
        if (last_name.getText() == null || last_name.getText().toString().isEmpty()) {
            ToastUtil.show(getString(com.livestreaming.common.R.string.please_ener_last_name));
            return false;
        }
        if (phone_number.getText() == null || phone_number.getText().toString().isEmpty()) {
            ToastUtil.show(getString(com.livestreaming.common.R.string.please_enter_phone_number));
            return false;
        }

        if (zipcode_value.getText() == null || zipcode_value.getText().toString().isEmpty()) {
            ToastUtil.show(getString(com.livestreaming.common.R.string.please_enter_zip_code));
            return false;
        }
        if (house_number.getText() == null || house_number.getText().toString().isEmpty()) {
            ToastUtil.show(getString(com.livestreaming.common.R.string.please_enter_house_number));
            return false;
        }
        int houseNumber;
        try{
            houseNumber=Integer.parseInt(house_number.getText().toString());
        }catch (Exception e){
            ToastUtil.show(getString(com.livestreaming.common.R.string.please_enter_house_number));
            return false;
        }
        if (full_address.getText() == null || full_address.getText().toString().isEmpty()) {
            ToastUtil.show(getString(com.livestreaming.common.R.string.please_enter_full_address));
            return false;
        }
        if (city_value.getText() == null || city_value.getText().toString().isEmpty()) {
            ToastUtil.show(getString(com.livestreaming.common.R.string.please_enter_city));
            return false;
        }
        if (street_apartment.getText() == null || street_apartment.getText().toString().isEmpty()) {
            ToastUtil.show(getString(com.livestreaming.common.R.string.please_enter_street_apartment));
            return false;
        }
        return true;
    }

    private void selectCountry() {
        Intent intent = new Intent();
        intent.setClassName(CommonAppConfig.PACKAGE_NAME, "com.livestreaming.main.activity.ChooseCountryActivity");
        startActivityForResult(intent, 101);
    }
}
