package com.livestreaming.im.activity;

import android.content.Context;
import android.content.Intent;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.alibaba.fastjson.JSON;
import com.livestreaming.common.activity.AbsActivity;
import com.livestreaming.common.adapter.RefreshAdapter;
import com.livestreaming.common.custom.CommonRefreshView;
import com.livestreaming.common.http.HttpCallback;
import com.livestreaming.common.utils.WordUtil;
import com.livestreaming.im.R;
import com.livestreaming.im.adapter.ImMsgAtAdapter;
import com.livestreaming.im.bean.VideoImMsgBean;
import com.livestreaming.im.http.ImHttpConsts;
import com.livestreaming.im.http.ImHttpUtil;

import java.util.Arrays;
import java.util.List;

/**
 * Created by 云豹科技 on 2022/1/18.
 */
public class ImMsgAtActivity extends AbsActivity {

    public static void forward(Context context) {
        context.startActivity(new Intent(context, ImMsgAtActivity.class));
    }

    private CommonRefreshView mRefreshView;
    private ImMsgAtAdapter mAdapter;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_im_msg;
    }


    @Override
    protected void main() {
        setTitle(WordUtil.getString(com.livestreaming.common.R.string.a_087));
        mRefreshView = findViewById(R.id.refreshView);
        mRefreshView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        mRefreshView.setDataHelper(new CommonRefreshView.DataHelper<VideoImMsgBean>() {
            @Override
            public RefreshAdapter<VideoImMsgBean> getAdapter() {
                if (mAdapter == null) {
                    mAdapter = new ImMsgAtAdapter(mContext);
                }
                return mAdapter;
            }

            @Override
            public void loadData(int p, HttpCallback callback) {
                ImHttpUtil.getImAtList(p, callback);
            }

            @Override
            public List<VideoImMsgBean> processData(String[] info) {
                return JSON.parseArray(Arrays.toString(info), VideoImMsgBean.class);
            }

            @Override
            public void onRefreshSuccess(List<VideoImMsgBean> list, int listCount) {

            }

            @Override
            public void onRefreshFailure() {

            }

            @Override
            public void onLoadMoreSuccess(List<VideoImMsgBean> loadItemList, int loadItemCount) {

            }

            @Override
            public void onLoadMoreFailure() {

            }
        });
        mRefreshView.initData();
    }


    @Override
    protected void onDestroy() {
        ImHttpUtil.cancel(ImHttpConsts.GET_IM_AT_LISTS);
        super.onDestroy();
    }
}
