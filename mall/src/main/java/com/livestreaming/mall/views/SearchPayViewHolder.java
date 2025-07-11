package com.livestreaming.mall.views;

import android.content.Context;
import android.os.Handler;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.livestreaming.common.adapter.RefreshAdapter;
import com.livestreaming.common.custom.CommonRefreshView;
import com.livestreaming.common.http.HttpCallback;
import com.livestreaming.common.interfaces.OnItemClickListener;
import com.livestreaming.common.views.AbsCommonViewHolder;
import com.livestreaming.mall.R;
import com.livestreaming.mall.activity.GoodsSearchActivity;
import com.livestreaming.mall.adapter.SearchPayAdapter;
import com.livestreaming.mall.bean.SearchPayBean;
import com.livestreaming.mall.http.MallHttpConsts;
import com.livestreaming.mall.http.MallHttpUtil;

import java.util.Arrays;
import java.util.List;

/**
 * 搜索付费内容
 */
public class SearchPayViewHolder extends AbsCommonViewHolder implements OnItemClickListener<SearchPayBean> {

    private CommonRefreshView mRefreshView;
    private SearchPayAdapter mAdapter;
    private EditText mEditText;
    private Handler mHandler;
    private String mKey;

    public SearchPayViewHolder(Context context, ViewGroup parentView, Handler handler) {
        super(context, parentView);
        mHandler = handler;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_search_pay;
    }

    @Override
    public void init() {
        mEditText = findViewById(R.id.edit);
        mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    MallHttpUtil.cancel(MallHttpConsts.SEARCH_PAY_LIST);
                    if (mHandler != null) {
                        mHandler.removeCallbacksAndMessages(null);
                    }
                    search();
                    return true;
                }
                return false;
            }
        });
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                MallHttpUtil.cancel(MallHttpConsts.SEARCH_PAY_LIST);
                if (mHandler != null) {
                    mHandler.removeCallbacksAndMessages(null);
                    mHandler.sendEmptyMessageDelayed(0, 500);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        mRefreshView = findViewById(R.id.refreshView);
        mRefreshView.setEmptyLayoutId(R.layout.view_no_data_pay_pub);
        mRefreshView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        mRefreshView.setDataHelper(new CommonRefreshView.DataHelper<SearchPayBean>() {
            @Override
            public RefreshAdapter<SearchPayBean> getAdapter() {
                if (mAdapter == null) {
                    mAdapter = new SearchPayAdapter(mContext);
                    mAdapter.setOnItemClickListener(SearchPayViewHolder.this);
                }
                return mAdapter;
            }

            @Override
            public void loadData(int p, HttpCallback callback) {
                MallHttpUtil.searchPayList(mKey, p, callback);
            }

            @Override
            public List<SearchPayBean> processData(String[] info) {
                return JSON.parseArray(Arrays.toString(info), SearchPayBean.class);
            }

            @Override
            public void onRefreshSuccess(List<SearchPayBean> list, int listCount) {

            }

            @Override
            public void onRefreshFailure() {

            }

            @Override
            public void onLoadMoreSuccess(List<SearchPayBean> loadItemList, int loadItemCount) {

            }

            @Override
            public void onLoadMoreFailure() {

            }
        });
    }

    @Override
    public void loadData() {
        if (isFirstLoadData()) {
            search();
        }
    }

    public SearchPayBean getCheckedBean() {
        if (mAdapter != null) {
            return mAdapter.getCheckedBean();
        }
        return null;
    }

    public void cancelCheck() {
        if (mAdapter != null) {
            mAdapter.cancelChecked();
        }
    }

    public void search() {
        mKey = mEditText.getText().toString().trim();
        if (mRefreshView != null) {
            mRefreshView.initData();
        }
    }

    @Override
    public void onDestroy() {
        mHandler = null;
        super.onDestroy();
    }

    @Override
    public void onItemClick(SearchPayBean bean, int position) {
        ((GoodsSearchActivity) mContext).cancelCheckGoods();
        ((GoodsSearchActivity) mContext).cancelCheckPlatGoods();
    }
}
