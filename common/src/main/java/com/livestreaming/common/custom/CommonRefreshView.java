package com.livestreaming.common.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.scwang.smart.refresh.footer.ClassicsFooter;
import com.scwang.smart.refresh.header.ClassicsHeader;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;
import com.livestreaming.common.R;
import com.livestreaming.common.adapter.RefreshAdapter;
import com.livestreaming.common.http.HttpCallback;
import com.livestreaming.common.utils.ToastUtil;

import java.util.List;

/**
 * Created by cxf on 2018/6/7.
 */

public class CommonRefreshView extends FrameLayout implements View.OnClickListener {

    private Context mContext;
    private DataHelper mDataHelper;
    private int mLayoutRes;
    private View mContentView;
    private SmartRefreshLayout mSmartRefreshLayout;
    private ClassicsHeader mHeader;
    private ClassicsFooter mFooter;
    private RecyclerView mRecyclerView;
    private FrameLayout mEmptyLayout;//没有数据的View
    private View mLoadFailureView;//加载失败View
    private boolean mRefreshEnable;//下拉刷新是否可用
    private boolean mLoadMoreEnable;//上拉加载是否可用
    private int mPageCount;//页数
    private int mItemCount;//每页的Item个数
    private int mTextColor;

    public CommonRefreshView(Context context) {
        this(context, null);
    }

    public CommonRefreshView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CommonRefreshView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.CommonRefreshView);
        mRefreshEnable = ta.getBoolean(R.styleable.CommonRefreshView_crv_refreshEnable, true);
        mLoadMoreEnable = ta.getBoolean(R.styleable.CommonRefreshView_crv_loadMoreEnable, true);
        mLayoutRes = ta.getResourceId(R.styleable.CommonRefreshView_crv_layout, R.layout.view_refresh_default);
        mItemCount = ta.getInteger(R.styleable.CommonRefreshView_crv_itemCount, 20);
        mTextColor = ta.getColor(R.styleable.CommonRefreshView_crv_textColor, ContextCompat.getColor(context, R.color.textColor));
        ta.recycle();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(mLayoutRes, this, false);
        mContentView = view;
        addView(view);
        mSmartRefreshLayout = (SmartRefreshLayout) view.findViewById(R.id.refreshLayout);
        mEmptyLayout = (FrameLayout) view.findViewById(R.id.no_data_container);
        mLoadFailureView = view.findViewById(R.id.load_failure);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mSmartRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                refresh();
            }
        });

        mSmartRefreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshlayout) {
                loadMore();
            }
        });
        mSmartRefreshLayout.setEnableRefresh(mRefreshEnable);
        mSmartRefreshLayout.setEnableLoadMore(mLoadMoreEnable);
        View btnReload = view.findViewById(R.id.btn_reload);
        if (btnReload != null) {
            btnReload.setOnClickListener(this);
        }
        mHeader = findViewById(R.id.header);
        mHeader.setAccentColor(mTextColor);
        mFooter = findViewById(R.id.footer);
        mFooter.setAccentColor(mTextColor);
        mFooter.setTextSizeTitle(14);
    }

    private HttpCallback mRefreshCallback = new HttpCallback() {

        private int mDataCount;
        private boolean mSuccess;

        @Override
        public void onStart() {
            mDataCount = 0;
            mSuccess = false;
        }

        @Override
        public void onSuccess(int code, String msg, String[] info) {
            if (mDataHelper == null) {
                return;
            }
            if (mLoadFailureView != null && mLoadFailureView.getVisibility() == View.VISIBLE) {
                mLoadFailureView.setVisibility(View.INVISIBLE);
            }
            RefreshAdapter adapter = null;
            RecyclerView.Adapter recyclerViewAdapter = mRecyclerView.getAdapter();
            if (recyclerViewAdapter != null && recyclerViewAdapter instanceof RefreshAdapter) {
                adapter = (RefreshAdapter) recyclerViewAdapter;
            } else {
                adapter = mDataHelper.getAdapter();
                if (adapter == null) {
                    return;
                }
                mRecyclerView.setAdapter(adapter);
            }
            if (code != 0) {
                ToastUtil.show(msg);
                return;
            }
            if (info != null) {
                List list = mDataHelper.processData(info);
                if (list == null) {
                    return;
                }
                mDataCount = list.size();
                if (mDataCount > 0) {
                    if (mEmptyLayout != null && mEmptyLayout.getVisibility() == View.VISIBLE) {
                        mEmptyLayout.setVisibility(View.INVISIBLE);
                    }
                    adapter.refreshData(list);
                } else {
                    adapter.clearData();
                    if (mEmptyLayout != null && mEmptyLayout.getVisibility() != View.VISIBLE) {
                        mEmptyLayout.setVisibility(View.VISIBLE);
                    }
                }
            } else {
                if (adapter != null) {
                    adapter.clearData();
                }
                if (mEmptyLayout != null && mEmptyLayout.getVisibility() != View.VISIBLE) {
                    mEmptyLayout.setVisibility(View.VISIBLE);
                }
            }
            mDataHelper.onRefreshSuccess(adapter.getList(), adapter.getItemCount());
            mSuccess = true;
        }


        @Override
        public void onError() {
            if (mEmptyLayout != null && mEmptyLayout.getVisibility() == View.VISIBLE) {
                mEmptyLayout.setVisibility(View.INVISIBLE);
            }
            if (mLoadFailureView != null) {
                if (mLoadFailureView.getVisibility() != View.VISIBLE) {
                    if (mRecyclerView != null) {
                        RecyclerView.Adapter adapter = mRecyclerView.getAdapter();
                        if (adapter != null && adapter.getItemCount() > 0) {
                            ToastUtil.show(R.string.load_failure);
                        } else {
                            mLoadFailureView.setVisibility(View.VISIBLE);
                        }
                    } else {
                        mLoadFailureView.setVisibility(View.VISIBLE);
                    }
                } else {
                    ToastUtil.show(R.string.load_failure);
                }
            }
            if (mDataHelper != null) {
                mDataHelper.onRefreshFailure();
            }
        }

        @Override
        public void onFinish() {
            if (mSmartRefreshLayout != null) {
                mSmartRefreshLayout.finishRefresh(mSuccess);
                if (mSuccess) {
                    if (mDataHelper instanceof DataHelperAdvance) {
                        boolean hasNextPage = ((DataHelperAdvance) mDataHelper).hasNextPage();
                        mSmartRefreshLayout.setNoMoreData(!hasNextPage);
                    } else {
                        mSmartRefreshLayout.setNoMoreData(mDataCount < mItemCount);
                    }
                }
            }
        }
    };


    private HttpCallback mLoadMoreCallback = new HttpCallback() {

        private int mDataCount;
        private boolean mSuccess;

        @Override
        public void onStart() {
            mDataCount = 0;
            mSuccess = false;
        }


        @Override
        public void onSuccess(int code, String msg, String[] info) {
            if (mDataHelper == null) {
                mPageCount--;
                return;
            }
            if (code != 0) {
                ToastUtil.show(msg);
                mPageCount--;
                return;
            }
            if (mLoadFailureView != null && mLoadFailureView.getVisibility() == View.VISIBLE) {
                mLoadFailureView.setVisibility(View.INVISIBLE);
            }
            if (info != null) {
                List list = mDataHelper.processData(info);
                if (list == null) {
                    mPageCount--;
                    return;
                }
                mDataCount = list.size();
                RefreshAdapter adapter = (RefreshAdapter) mRecyclerView.getAdapter();
                if (mDataCount > 0) {
                    if (adapter != null) {
                        adapter.insertList(list);
                    }
                } else {
                    mPageCount--;
                }
                mDataHelper.onLoadMoreSuccess(list, mDataCount);
                mSuccess = true;
            } else {
                mPageCount--;
            }
        }

        @Override
        public void onError() {
            super.onError();
            mPageCount--;
            if (mDataHelper != null) {
                mDataHelper.onLoadMoreFailure();
            }
        }

        @Override
        public void onFinish() {
            if (mSmartRefreshLayout != null) {
                if (mSuccess) {
                    if (mDataHelper instanceof DataHelperAdvance) {
                        boolean hasNextPage = ((DataHelperAdvance) mDataHelper).hasNextPage();
                        if(hasNextPage){
                            mSmartRefreshLayout.finishLoadMore(true);
                        }else{
                            mSmartRefreshLayout.finishLoadMoreWithNoMoreData();
                        }
                    } else {
                        if (mDataCount < mItemCount) {
                            mSmartRefreshLayout.finishLoadMoreWithNoMoreData();
                        } else {
                            mSmartRefreshLayout.finishLoadMore(true);
                        }
                    }
                }else{
                    mSmartRefreshLayout.finishLoadMore(false);
                }
            }
        }
    };

    public <T> void setDataHelper(DataHelper<T> dataHelper) {
        mDataHelper = dataHelper;
    }

    public void setLayoutManager(RecyclerView.LayoutManager layoutManager) {
        mRecyclerView.setLayoutManager(layoutManager);
    }

    public void setItemDecoration(ItemDecoration itemDecoration) {
        mRecyclerView.addItemDecoration(itemDecoration);
    }


    public void showLoading() {
        mPageCount = 1;
        if (mSmartRefreshLayout != null) {
            mSmartRefreshLayout.autoRefreshAnimationOnly();
        }
        if (mEmptyLayout != null && mEmptyLayout.getVisibility() == VISIBLE) {
            mEmptyLayout.setVisibility(INVISIBLE);
        }
        if (mLoadFailureView != null && mLoadFailureView.getVisibility() == VISIBLE) {
            mLoadFailureView.setVisibility(INVISIBLE);
        }
    }

    public void showEmpty() {
        if (mEmptyLayout != null && mEmptyLayout.getVisibility() != VISIBLE) {
            mEmptyLayout.setVisibility(VISIBLE);
        }
    }

    public void hideEmpty() {
        if (mEmptyLayout != null && mEmptyLayout.getVisibility() == VISIBLE) {
            mEmptyLayout.setVisibility(INVISIBLE);
        }
    }

    public void hideLoadFailure() {
        if (mLoadFailureView != null && mLoadFailureView.getVisibility() == VISIBLE) {
            mLoadFailureView.setVisibility(INVISIBLE);
        }
    }


    public void initData() {
        refresh();
    }

    private void refresh() {
        if (mLoadMoreEnable && mSmartRefreshLayout != null) {
            mSmartRefreshLayout.resetNoMoreData();
        }
        if (mDataHelper != null) {
            mPageCount = 1;
            mDataHelper.loadData(mPageCount, mRefreshCallback);
        }
    }

    private void loadMore() {
        if (mDataHelper != null) {
            mPageCount++;
            mDataHelper.loadData(mPageCount, mLoadMoreCallback);
        }
    }

    public int getPageCount() {
        return mPageCount;
    }

    public void setPageCount(int pageCount) {
        mPageCount = pageCount;
    }


    public int getItemCount() {
        return mItemCount;
    }

    public void setItemCount(int itemCount) {
        mItemCount = itemCount;
    }

    public void setRefreshEnable(boolean enable) {
        if (mSmartRefreshLayout != null) {
            mSmartRefreshLayout.setEnableRefresh(enable);
        }
    }

    public void setLoadMoreEnable(boolean enable) {
        if (mSmartRefreshLayout != null) {
            mSmartRefreshLayout.setEnableLoadMore(enable);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_reload) {
            refresh();
        }
    }


    public interface DataHelper<T> {
        RefreshAdapter<T> getAdapter();

        void loadData(int p, HttpCallback callback);

        List<T> processData(String[] info);

        /**
         * 下拉刷新成功
         *
         * @param list      Adapter的全部数据的List
         * @param listCount Adapter的全部数据的个数
         */
        void onRefreshSuccess(List<T> list, int listCount);

        /**
         * 下拉刷新失败
         */
        void onRefreshFailure();

        /**
         * 上拉加载成功
         *
         * @param loadItemList  本次加载到的数据
         * @param loadItemCount 加载到的数据个数
         */
        void onLoadMoreSuccess(List<T> loadItemList, int loadItemCount);

        /**
         * 加载失败
         */
        void onLoadMoreFailure();
    }


    public interface DataHelperAdvance<T> extends DataHelper<T> {
        /**
         * 是否有下一页数据
         */
        boolean hasNextPage();
    }

    /**
     * 空数据的布局
     */
    public void setEmptyLayoutId(int noDataLayoutId) {
        if (mEmptyLayout != null) {
            mEmptyLayout.removeAllViews();
            View v = LayoutInflater.from(mContext).inflate(noDataLayoutId, mEmptyLayout, false);
            LayoutParams params = (LayoutParams) v.getLayoutParams();
            params.gravity = Gravity.CENTER;
            v.setLayoutParams(params);
            mEmptyLayout.addView(v);
        }
    }

    /**
     * 空数据的布局
     */
    public void setEmptyLayoutView(View noDataView) {
        if (mEmptyLayout != null) {
            mEmptyLayout.removeAllViews();
            mEmptyLayout.addView(noDataView);
        }
    }

    /**
     * 空数据的布局
     */
    public void clearEmptyLayoutView() {
        if (mEmptyLayout != null) {
            mEmptyLayout.removeAllViews();
        }
    }

    public ViewGroup getmEmptyLayout() {
        return mEmptyLayout;
    }


    public View getContentView() {
        return mContentView;
    }

    public void setRecyclerViewAdapter(RefreshAdapter adapter) {
        if (mRecyclerView != null) {
            mRecyclerView.setAdapter(adapter);
        }
    }
    public void setRecyclerViewAdapter(RecyclerView.Adapter adapter) {
        if (mRecyclerView != null) {
            mRecyclerView.setAdapter(adapter);
        }
    }


    public RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

}
