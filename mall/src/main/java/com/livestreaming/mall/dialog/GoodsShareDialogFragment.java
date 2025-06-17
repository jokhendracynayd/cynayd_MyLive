package com.livestreaming.mall.dialog;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.livestreaming.common.Constants;
import com.livestreaming.common.dialog.AbsDialogFragment;
import com.livestreaming.common.utils.RouteUtil;
import com.livestreaming.common.utils.WordUtil;
import com.livestreaming.mall.R;

public class GoodsShareDialogFragment extends AbsDialogFragment implements View.OnClickListener {

    private String mGoodsId;

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_goods_share;
    }

    @Override
    protected int getDialogStyle() {
        return com.livestreaming.common.R.style.dialog;
    }

    @Override
    protected boolean canCancel() {
        return true;
    }

    @Override
    protected void setWindowAttributes(Window window) {
        window.setWindowAnimations(com.livestreaming.common.R.style.bottomToTopAnim);
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.BOTTOM;
        window.setAttributes(params);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle == null) {
            return;
        }
        mGoodsId = bundle.getString(Constants.MALL_GOODS_ID);
        String tip = bundle.getString(Constants.TIP);
        TextView priceYong = findViewById(R.id.price_yong);
        if(!TextUtils.isEmpty(tip)){
            priceYong.setText(String.format(WordUtil.getString(com.livestreaming.common.R.string.a_071), tip));
        }else{
            priceYong.setVisibility(View.GONE);
        }
        findViewById(R.id.btn_share_active).setOnClickListener(this);
        findViewById(R.id.btn_share_friend).setOnClickListener(this);
        findViewById(R.id.btn_cancel).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_share_active) {
            dismiss();
            RouteUtil.forwardActivePub(mContext, mGoodsId);
        } else if (i == R.id.btn_share_friend) {
            dismiss();
            RouteUtil.forwardShareGoods(mContext, mGoodsId);
        } else if (i == R.id.btn_cancel) {
            dismiss();
        }
    }
}
