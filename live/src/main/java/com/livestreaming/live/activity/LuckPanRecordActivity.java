package com.livestreaming.live.activity;

import android.app.Dialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.view.View;

import com.livestreaming.common.activity.AbsActivity;
import com.livestreaming.common.adapter.RefreshAdapter;
import com.livestreaming.common.custom.CommonRefreshView;
import com.livestreaming.common.http.HttpCallback;
import com.livestreaming.common.utils.DialogUitl;
import com.livestreaming.common.utils.JsonUtil;
import com.livestreaming.common.utils.WordUtil;
import com.livestreaming.live.R;
import com.livestreaming.live.adapter.LuckPanRecordAdapter;
import com.livestreaming.live.bean.LuckPanBean;
import com.livestreaming.live.http.LiveHttpConsts;
import com.livestreaming.live.http.LiveHttpUtil;

import java.util.Arrays;
import java.util.List;

/**
 * Created by 云豹科技 on 2022/3/2.
 */
public class LuckPanRecordActivity extends AbsActivity implements View.OnClickListener {


    private CommonRefreshView mRefreshView;
    private LuckPanRecordAdapter mAdapter;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_luck_pan_record;
    }

    @Override
    protected void main() {
        setTitle(WordUtil.getString(com.livestreaming.common.R.string.pan_win_record));
        findViewById(R.id.btn_clear).setOnClickListener(this);
        mRefreshView = findViewById(R.id.refreshView);
        mRefreshView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        mRefreshView.setDataHelper(new CommonRefreshView.DataHelper<LuckPanBean>() {
            @Override
            public RefreshAdapter<LuckPanBean> getAdapter() {
                if (mAdapter == null) {
                    mAdapter = new LuckPanRecordAdapter(mContext, false);
                }
                return mAdapter;
            }

            @Override
            public void loadData(int p, HttpCallback callback) {
                LiveHttpUtil.getTurnRecord(0, p, callback);
            }

            @Override
            public List<LuckPanBean> processData(String[] info) {
                return JsonUtil.getJsonToList(Arrays.toString(info), LuckPanBean.class);
            }

            @Override
            public void onRefreshSuccess(List<LuckPanBean> list, int listCount) {

            }

            @Override
            public void onRefreshFailure() {

            }

            @Override
            public void onLoadMoreSuccess(List<LuckPanBean> loadItemList, int loadItemCount) {

            }

            @Override
            public void onLoadMoreFailure() {

            }
        });
        mRefreshView.initData();
    }

    @Override
    protected void onDestroy() {
        LiveHttpUtil.cancel(LiveHttpConsts.GET_WIN);
        LiveHttpUtil.cancel(LiveHttpConsts.CLEAR_TURN_RECORD);
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        if (mAdapter == null || mAdapter.getItemCount() == 0) {
            return;
        }
        new DialogUitl.Builder(mContext)
                .setContent(WordUtil.getString(com.livestreaming.common.R.string.a_101))
                .setCancelable(true)
                .setIsHideTitle(true)
                .setBackgroundDimEnabled(true)
                .setClickCallback(new DialogUitl.SimpleCallback() {
                    @Override
                    public void onConfirmClick(Dialog dialog, String content) {
                        LiveHttpUtil.clearTurnRecord(new HttpCallback() {
                            @Override
                            public void onSuccess(int code, String msg, String[] info) {
                                if (code == 0) {
                                    if (mRefreshView != null) {
                                        mRefreshView.initData();
                                    }
                                }
                            }
                        });
                    }
                }).build().show();

    }
}
