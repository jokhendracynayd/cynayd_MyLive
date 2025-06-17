package com.livestreaming.mall.dialog;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.livestreaming.common.Constants;
import com.livestreaming.common.custom.RatingBar;
import com.livestreaming.common.dialog.AbsDialogFragment;
import com.livestreaming.common.http.HttpCallback;
import com.livestreaming.common.utils.DpUtil;
import com.livestreaming.common.utils.ToastUtil;
import com.livestreaming.mall.R;
import com.livestreaming.mall.activity.PayContentDetailActivity;
import com.livestreaming.mall.http.MallHttpConsts;
import com.livestreaming.mall.http.MallHttpUtil;

/**
 * 付费内容 评价
 */
public class PayCommentDialogFragment extends AbsDialogFragment implements RatingBar.OnRatingChangedListener, View.OnClickListener {

    private RatingBar mStar;
    private TextView mTipText;
    private int[] mTips;
    private String mGoodsId;

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_pay_comment;
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
        params.height = DpUtil.dp2px(240);
        params.gravity = Gravity.BOTTOM;
        window.setAttributes(params);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mGoodsId = bundle.getString(Constants.MALL_GOODS_ID);
        }
        mStar = findViewById(R.id.rating_bar);
        mStar.setOnRatingChangedListener(this);
        mTipText = findViewById(R.id.tip);
        mTips = new int[]{com.livestreaming.common.R.string.mall_360, com.livestreaming.common.R.string.mall_361, com.livestreaming.common.R.string.mall_362, com.livestreaming.common.R.string.mall_363, com.livestreaming.common.R.string.mall_364};
        findViewById(R.id.btn_submit).setOnClickListener(this);
    }

    @Override
    public void onRatingChanged(int curCount, int maxCount) {
        if (mTipText != null) {
            if (curCount == 0) {
                mTipText.setText(null);
            } else {
                mTipText.setText(mTips[curCount - 1]);
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (TextUtils.isEmpty(mGoodsId)) {
            return;
        }
        int starCount = mStar.getFillCount();
        if (starCount == 0) {
            return;
        }
        MallHttpUtil.commentPayContent(mGoodsId, starCount, new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0) {
                    ((PayContentDetailActivity) mContext).getData();
                    dismiss();
                }
                ToastUtil.show(msg);
            }
        });
    }

    @Override
    public void onDestroy() {
        MallHttpUtil.cancel(MallHttpConsts.COMMENT_PAY_CONTENT);
        mContext = null;
        super.onDestroy();
    }
}
