package com.livestreaming.live.dialog;

import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.livestreaming.common.Constants;
import com.livestreaming.common.adapter.RefreshAdapter;
import com.livestreaming.common.bean.GoodsBean;
import com.livestreaming.common.custom.CommonRefreshView;
import com.livestreaming.common.dialog.AbsDialogFragment;
import com.livestreaming.common.http.HttpCallback;
import com.livestreaming.common.utils.DialogUitl;
import com.livestreaming.common.utils.DpUtil;
import com.livestreaming.common.utils.StringUtil;
import com.livestreaming.common.utils.WordUtil;
import com.livestreaming.live.R;
import com.livestreaming.live.activity.LiveAnchorActivity;
import com.livestreaming.live.adapter.LiveShopAdapter;
import com.livestreaming.live.http.LiveHttpConsts;
import com.livestreaming.live.http.LiveHttpUtil;

import java.util.List;

/**
 * Created by cxf on 2019/8/29.
 */

public class LiveShopDialogFragment extends AbsDialogFragment implements View.OnClickListener, LiveShopAdapter.ActionListener {

    private CommonRefreshView mRefreshView;
    private LiveShopAdapter mAdapter;
    private TextView mTitle;
    private String mLiveUid;
    private int mGoodsNum;

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_live_shop;
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
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = DpUtil.dp2px(320);
        params.gravity = Gravity.BOTTOM;
        window.setAttributes(params);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mLiveUid = bundle.getString(Constants.LIVE_UID);
        }
        mTitle = (TextView) findViewById(R.id.title);
        findViewById(R.id.btn_add).setOnClickListener(this);
        mRefreshView = (CommonRefreshView) findViewById(R.id.refreshView);
        mRefreshView.setEmptyLayoutId(R.layout.view_no_data_shop);
        mRefreshView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        mRefreshView.setDataHelper(new CommonRefreshView.DataHelper<GoodsBean>() {
            @Override
            public RefreshAdapter<GoodsBean> getAdapter() {
                if (mAdapter == null) {
                    mAdapter = new LiveShopAdapter(mContext);
                    mAdapter.setActionListener(LiveShopDialogFragment.this);
                }
                return mAdapter;
            }

            @Override
            public void loadData(int p, HttpCallback callback) {
                LiveHttpUtil.getSale(p, mLiveUid, callback);
            }

            @Override
            public List<GoodsBean> processData(String[] info) {
                JSONObject obj = JSON.parseObject(info[0]);
                mGoodsNum = obj.getIntValue("nums");
                mTitle.setText(StringUtil.contact(WordUtil.getString(com.livestreaming.common.R.string.goods_tip_17), " ", String.valueOf(mGoodsNum)));
                return JSON.parseArray(obj.getString("list"), GoodsBean.class);
            }

            @Override
            public void onRefreshSuccess(List<GoodsBean> list, int listCount) {

            }

            @Override
            public void onRefreshFailure() {

            }

            @Override
            public void onLoadMoreSuccess(List<GoodsBean> loadItemList, int loadItemCount) {

            }

            @Override
            public void onLoadMoreFailure() {

            }
        });
        mRefreshView.initData();
    }


    @Override
    public void onDeleteSuccess() {
        if (mTitle != null) {
            mGoodsNum--;
            if (mGoodsNum < 0) {
                mGoodsNum = 0;
            }
            mTitle.setText(StringUtil.contact(WordUtil.getString(com.livestreaming.common.R.string.goods_tip_17), " ", String.valueOf(mGoodsNum)));
        }
    }

    @Override
    public void onGoodsShowChanged(GoodsBean bean) {
        if (mContext != null) {
            ((LiveAnchorActivity) mContext).sendLiveGoodsShow(bean);
        }
        dismiss();
    }


    @Override
    public void onDestroy() {
        mContext = null;
        if (mAdapter != null) {
            mAdapter.setActionListener(null);
        }
        LiveHttpUtil.cancel(LiveHttpConsts.GET_SALE);
        LiveHttpUtil.cancel(LiveHttpConsts.SET_SALE);
        LiveHttpUtil.cancel(LiveHttpConsts.SET_LIVE_GOODS_SHOW);
        super.onDestroy();
    }


    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_add) {
            if (mContext != null) {
                DialogUitl.showStringArrayDialog(mContext, new Integer[]{
                        com.livestreaming.common. R.string.mall_404, com.livestreaming.common.R.string.mall_406
                }, new DialogUitl.StringArrayDialogCallback() {
                    @Override
                    public void onItemClick(String text, int tag) {
                        if (tag == com.livestreaming.common.R.string.mall_406) {
                            ((LiveAnchorActivity) mContext).forwardAddPlatGoods();
                            dismiss();
                        } else if (tag == com.livestreaming.common.R.string.mall_404) {
                            ((LiveAnchorActivity) mContext).forwardAddGoods();
                            dismiss();
                        }
                    }
                });
            }
        }
    }


}
