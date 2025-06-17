package com.livestreaming.common.activity;

import android.content.Intent;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.livestreaming.common.CommonAppConfig;
import com.livestreaming.common.Constants;
import com.livestreaming.common.R;
import com.livestreaming.common.adapter.ChooseLocationAdapter;
import com.livestreaming.common.adapter.RefreshAdapter;
import com.livestreaming.common.bean.TxLocationPoiBean;
import com.livestreaming.common.custom.CommonRefreshView;
import com.livestreaming.common.event.LocationEvent;
import com.livestreaming.common.http.HttpCallback;
import com.livestreaming.common.interfaces.OnItemClickListener;
import com.livestreaming.common.utils.LocationUtil;
import com.livestreaming.common.utils.WordUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

/**
 * 选择位置地址
 */
public class ChooseLocationActivity extends AbsActivity implements OnItemClickListener<TxLocationPoiBean> {

    private CommonRefreshView mRefreshView;
    private ChooseLocationAdapter mAdapter;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_choose_location;
    }

    @Override
    protected void main() {
        setTitle(WordUtil.getString(R.string.location_1));
        mRefreshView = findViewById(R.id.refreshView);
        mRefreshView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        mRefreshView.setDataHelper(new CommonRefreshView.DataHelper<TxLocationPoiBean>() {
            @Override
            public RefreshAdapter<TxLocationPoiBean> getAdapter() {
                if (mAdapter == null) {
                    mAdapter = new ChooseLocationAdapter(mContext);
                    mAdapter.setOnItemClickListener(ChooseLocationActivity.this);
                }
                return mAdapter;
            }

            @Override
            public void loadData(int p, HttpCallback callback) {
                double lng = CommonAppConfig.getInstance().getLng();
                double lat = CommonAppConfig.getInstance().getLat();
                if (lng == 0 || lat == 0) {
                    return;
                }
             //   CommonHttpUtil.getAddressInfoByTxLocaitonSdk(lng, lat, 1, p, CommonHttpConsts.GET_MAP_INFO, callback);
            }

            @Override
            public List<TxLocationPoiBean> processData(String[] info) {
                JSONObject obj = JSON.parseObject(info[0]);
                if (obj != null) {
                    return JSON.parseArray(obj.getString("pois"), TxLocationPoiBean.class);
                }
                return null;
            }

            @Override
            public void onRefreshSuccess(List<TxLocationPoiBean> list, int listCount) {

            }

            @Override
            public void onRefreshFailure() {

            }

            @Override
            public void onLoadMoreSuccess(List<TxLocationPoiBean> loadItemList, int loadItemCount) {

            }

            @Override
            public void onLoadMoreFailure() {

            }
        });
        if (CommonAppConfig.getInstance().getLat() == 0 || CommonAppConfig.getInstance().getLng() == 0) {
            EventBus.getDefault().register(this);
            if (hasLocationPermission()) {
                LocationUtil.getInstance().startLocation();
            } else {
                checkLocationPermission(new Runnable() {
                    @Override
                    public void run() {
                        LocationUtil.getInstance().startLocation();
                    }
                });
            }
        } else {
            if (mRefreshView != null) {
                mRefreshView.initData();
            }
        }
    }


    @Override
    public void onItemClick(TxLocationPoiBean bean, int position) {
        Intent intent = new Intent();
        intent.putExtra(Constants.CHOOSE_LOCATION, bean != null ? bean.getTitle() : "");
        setResult(RESULT_OK, intent);
        finish();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLocationEvent(LocationEvent e) {
        if (mRefreshView != null) {
            mRefreshView.initData();
        }
    }

    @Override
    protected void onDestroy() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        super.onDestroy();
    }
}
