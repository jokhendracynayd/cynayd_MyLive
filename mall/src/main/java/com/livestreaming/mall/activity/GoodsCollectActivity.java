package com.livestreaming.mall.activity;

import android.app.Dialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.view.View;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.livestreaming.common.activity.AbsActivity;
import com.livestreaming.common.adapter.RefreshAdapter;
import com.livestreaming.common.bean.GoodsBean;
import com.livestreaming.common.custom.CommonRefreshView;
import com.livestreaming.common.http.HttpCallback;
import com.livestreaming.common.utils.DialogUitl;
import com.livestreaming.common.utils.ToastUtil;
import com.livestreaming.common.utils.WordUtil;
import com.livestreaming.mall.R;
import com.livestreaming.mall.adapter.GoodsCollectAdapter;
import com.livestreaming.mall.http.MallHttpConsts;
import com.livestreaming.mall.http.MallHttpUtil;

import java.util.Arrays;
import java.util.List;

/**
 * 商品收藏
 */
public class GoodsCollectActivity extends AbsActivity implements View.OnClickListener {

    private CommonRefreshView mRefreshView;
    private GoodsCollectAdapter mAdapter;
    private TextView mBtnOption;
    private boolean mCanDelete;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_goods_collect;
    }

    @Override
    protected void main() {
        setTitle(WordUtil.getString(com.livestreaming.common.R.string.mall_394));
        mBtnOption = findViewById(R.id.btn_option);
        mBtnOption.setOnClickListener(this);
        mRefreshView = findViewById(R.id.refreshView);
        mRefreshView.setEmptyLayoutId(R.layout.view_no_data_goods_collect);
        mRefreshView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        mRefreshView.setDataHelper(new CommonRefreshView.DataHelper<GoodsBean>() {
            @Override
            public RefreshAdapter<GoodsBean> getAdapter() {
                if (mAdapter == null) {
                    mAdapter = new GoodsCollectAdapter(mContext);
                }
                return mAdapter;
            }

            @Override
            public void loadData(int p, HttpCallback callback) {
                MallHttpUtil.getGoodsCollect(p, callback);
            }

            @Override
            public List<GoodsBean> processData(String[] info) {
                return JSON.parseArray(Arrays.toString(info), GoodsBean.class);
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mRefreshView != null) {
            mRefreshView.initData();
        }
    }

    @Override
    protected void onDestroy() {
        MallHttpUtil.cancel(MallHttpConsts.GET_GOODS_COLLECT);
        MallHttpUtil.cancel(MallHttpConsts.DELETE_GOODS_COLLECT);
        super.onDestroy();
    }

    public void setCanDelete(boolean canDelete) {
        mCanDelete = canDelete;
        if (mBtnOption != null) {
            mBtnOption.setText(canDelete ? com.livestreaming.common.R.string.delete : com.livestreaming.common.R.string.manage);
        }
    }

    @Override
    public void onClick(View v) {
        if (mAdapter != null) {
            if (mCanDelete) {
                new DialogUitl.Builder(mContext)
                        .setContent(WordUtil.getString(com.livestreaming.common.R.string.a_083))
                        .setCancelable(true)
                        .setIsHideTitle(true)
                        .setBackgroundDimEnabled(true)
                        .setClickCallback(new DialogUitl.SimpleCallback2() {
                            @Override
                            public void onCancelClick() {
                                mAdapter.setShowCheck(false);
                                setCanDelete(false);
                            }

                            @Override
                            public void onConfirmClick(Dialog dialog, String content) {
                                setCanDelete(false);
                                MallHttpUtil.deleteGoodsCollect(mAdapter.getCheckedId(), new HttpCallback() {
                                    @Override
                                    public void onSuccess(int code, String msg, String[] info) {
                                        if (code == 0) {
                                            if (mRefreshView != null) {
                                                mRefreshView.initData();
                                            }
                                        }
                                        ToastUtil.show(msg);
                                    }
                                });
                            }


                        })
                        .build()
                        .show();


            } else {
                mAdapter.toggleShowCheck();
            }
        }
    }
}
