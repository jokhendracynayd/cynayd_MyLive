package com.livestreaming.live.dialog;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.livestreaming.common.adapter.RefreshAdapter;
import com.livestreaming.common.custom.CommonRefreshView;
import com.livestreaming.common.dialog.AbsDialogFragment;
import com.livestreaming.common.http.HttpCallback;
import com.livestreaming.common.interfaces.OnItemClickListener;
import com.livestreaming.common.utils.DpUtil;
import com.livestreaming.common.utils.ToastUtil;
import com.livestreaming.live.R;
import com.livestreaming.live.activity.LiveAnchorActivity;
import com.livestreaming.live.adapter.LivePkAdapter;
import com.livestreaming.live.bean.LivePkBean;
import com.livestreaming.live.http.LiveHttpConsts;
import com.livestreaming.live.http.LiveHttpUtil;

import java.util.Arrays;
import java.util.List;

/**
 * Created by cxf on 2018/11/15.
 * 主播连麦列表弹窗
 */

public class LiveLinkMicListDialogFragment extends AbsDialogFragment implements OnItemClickListener<LivePkBean>, View.OnClickListener, LiveLinkMicPkSearchDialog.ActionListener {

    private CommonRefreshView mRefreshView;
    private CommonRefreshView mSearchRefreshView;
    private LivePkAdapter mAdapter;
    private LivePkAdapter mSearchAdapter;
    private View mSearchView;
    private EditText mEditText;
    private LiveLinkMicPkSearchDialog mLivePkSearchDialog;
    private Handler mHandler;
    private String mKey;
    private InputMethodManager imm;
    private TextView tabAll;
    private TextView tabFollow;
    private int selectedTab;


    @Override
    protected int getLayoutId() {
        return R.layout.dialog_live_pk;
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
        params.height = DpUtil.dp2px(300);
        params.gravity = Gravity.BOTTOM;
        window.setAttributes(params);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                searchOnLineAnchor();
            }
        };
        mRootView.findViewById(R.id.btn_close).setOnClickListener(this);
        mRootView.findViewById(R.id.btn_search).setOnClickListener(this);
        tabAll = mRootView.findViewById(R.id.all_list_tab_tv);
        tabFollow = mRootView.findViewById(R.id.follow_list_tab_tv);
        mRefreshView = mRootView.findViewById(R.id.refreshView);
        selectedTab = 0;
        tabAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedTab = 0;
                onChangeTab(0);
            }
        });
        tabFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedTab = 1;
                onChangeTab(1);
            }
        });

        mRefreshView.setEmptyLayoutId(R.layout.view_no_data_live_pk);
        mRefreshView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        mRefreshView.setDataHelper(new CommonRefreshView.DataHelper<LivePkBean>() {
            @Override
            public RefreshAdapter<LivePkBean> getAdapter() {
                if (mAdapter == null) {
                    mAdapter = new LivePkAdapter(mContext);
                    mAdapter.setOnItemClickListener(LiveLinkMicListDialogFragment.this);
                }
                return mAdapter;
            }

            @Override
            public void loadData(int p, HttpCallback callback) {
                if (selectedTab == 0) {
                    LiveHttpUtil.getLivePkList(p, callback);
                } else {
                    LiveHttpUtil.getLiveFollowList(p, callback);
                }
            }

            @Override
            public List<LivePkBean> processData(String[] info) {
                return JSON.parseArray(Arrays.toString(info), LivePkBean.class);
            }

            @Override
            public void onRefreshSuccess(List<LivePkBean> list, int listCount) {

            }

            @Override
            public void onRefreshFailure() {

            }

            @Override
            public void onLoadMoreSuccess(List<LivePkBean> loadItemList, int loadItemCount) {

            }

            @Override
            public void onLoadMoreFailure() {

            }
        });
        mRefreshView.initData();
    }

    private void onChangeTab(int i) {
        if (i == 0) {
            tabAll.setTextColor(ContextCompat.getColor(mContext, com.livestreaming.common.R.color.white));
            tabAll.setBackground(ContextCompat.getDrawable(mContext, R.drawable.bg_btn_confirm));
            tabFollow.setTextColor(ContextCompat.getColor(mContext, com.livestreaming.common.R.color.black1));
            tabFollow.setBackground(ContextCompat.getDrawable(mContext, com.livestreaming.common.R.drawable.back_transparent));

        } else {
            tabFollow.setTextColor(ContextCompat.getColor(mContext, com.livestreaming.common.R.color.white));
            tabFollow.setBackground(ContextCompat.getDrawable(mContext, R.drawable.bg_btn_confirm));
            tabAll.setTextColor(ContextCompat.getColor(mContext, com.livestreaming.common.R.color.black1));
            tabAll.setBackground(ContextCompat.getDrawable(mContext, com.livestreaming.common.R.drawable.back_transparent));

        }
        if (mAdapter != null)
            mAdapter.clearData();

        if (mRefreshView != null)
            mRefreshView.initData();

    }


    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_close) {
            dismiss();

        } else if (i == R.id.btn_search) {
            showSearchDialog();

        } else if (i == R.id.btn_back) {
            hideSearchDialog();

        }
    }

    private View initSearchView() {
        imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        View v = LayoutInflater.from(mContext).inflate(R.layout.dialog_live_pk_search, null);
        mEditText = (EditText) v.findViewById(R.id.edit);
        mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
                    }
                    searchOnLineAnchor();
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
                LiveHttpUtil.cancel(LiveHttpConsts.LIVE_PK_SEARCH_ANCHOR);
                if (mHandler != null) {
                    mHandler.removeCallbacksAndMessages(null);
                }
                if (!TextUtils.isEmpty(s)) {
                    if (mHandler != null) {
                        mHandler.sendEmptyMessageDelayed(0, 500);
                    }
                } else {
                    mKey = null;
                    if (mSearchAdapter != null) {
                        mSearchAdapter.clearData();
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        v.findViewById(R.id.btn_back).setOnClickListener(this);
        mSearchRefreshView = v.findViewById(R.id.refreshView);
        mSearchRefreshView.setEmptyLayoutId(R.layout.view_no_data_live_pk_search);
        mSearchRefreshView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        mSearchRefreshView.setDataHelper(new CommonRefreshView.DataHelper<LivePkBean>() {
            @Override
            public RefreshAdapter<LivePkBean> getAdapter() {
                if (mSearchAdapter == null) {
                    mSearchAdapter = new LivePkAdapter(mContext);
                    mSearchAdapter.setOnItemClickListener(LiveLinkMicListDialogFragment.this);
                }
                return mSearchAdapter;
            }

            @Override
            public void loadData(int p, HttpCallback callback) {
                LiveHttpUtil.livePkSearchAnchor(mKey, p, callback);
            }

            @Override
            public List<LivePkBean> processData(String[] info) {
                return JSON.parseArray(Arrays.toString(info), LivePkBean.class);
            }

            @Override
            public void onRefreshSuccess(List<LivePkBean> list, int listCount) {

            }

            @Override
            public void onRefreshFailure() {

            }

            @Override
            public void onLoadMoreSuccess(List<LivePkBean> loadItemList, int loadItemCount) {

            }

            @Override
            public void onLoadMoreFailure() {

            }
        });
        return v;
    }

    /**
     * 显示搜索弹窗
     */
    private void showSearchDialog() {
        if (mSearchView == null) {
            mSearchView = initSearchView();
        }
        mLivePkSearchDialog = new LiveLinkMicPkSearchDialog(mRootView, mSearchView, this);
        mLivePkSearchDialog.show();
        if (mHandler != null) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (imm != null && mEditText != null) {
                        imm.showSoftInput(mEditText, InputMethodManager.SHOW_FORCED);
                    }
                }
            }, 300);
        }
    }

    /**
     * 隐藏搜索弹窗
     */
    private void hideSearchDialog() {
        if (mLivePkSearchDialog != null) {
            mLivePkSearchDialog.dismiss();
        }
    }

    /**
     * 搜索弹窗消失的回调
     */
    @Override
    public void onSearchDialogDismiss() {
        LiveHttpUtil.cancel(LiveHttpConsts.LIVE_PK_SEARCH_ANCHOR);
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        if (mEditText != null) {
            mEditText.setText("");
        }
        if (mSearchAdapter != null) {
            mSearchAdapter.clearData();
        }
    }

    /**
     * 搜索在线主播
     */
    private void searchOnLineAnchor() {
        String key = mEditText.getText().toString().trim();
        if (TextUtils.isEmpty(key)) {
            ToastUtil.show(com.livestreaming.common.R.string.content_empty);
            return;
        }
        LiveHttpUtil.cancel(LiveHttpConsts.LIVE_PK_SEARCH_ANCHOR);
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        mKey = key;
        if (mSearchRefreshView != null) {
            mSearchRefreshView.initData();
        }
    }

    @Override
    public void onDestroy() {
        if (mContext != null) {
            ((LiveAnchorActivity) mContext).hideLinkMicAnchorWindow2();
        }
        if (imm != null) {
            imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
        }
        imm = null;
        LiveHttpUtil.cancel(LiveHttpConsts.GET_LIVE_PK_LIST);
        LiveHttpUtil.cancel(LiveHttpConsts.LIVE_PK_SEARCH_ANCHOR);
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        mHandler = null;
        super.onDestroy();
    }


    /**
     * 点击邀请连麦
     */
    @Override
    public void onItemClick(LivePkBean bean, int position) {
        if (System.currentTimeMillis() - bean.getStartTime() * 1000 < 30000) {
            ToastUtil.show(com.livestreaming.common.R.string.live_link_mic_6);
            return;
        }
        if (imm != null) {
            imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
        }
        imm = null;
        if (mLivePkSearchDialog != null) {
            mLivePkSearchDialog.dismiss();
        }
        mLivePkSearchDialog = null;
        ((LiveAnchorActivity) mContext).linkMicAnchorApply(bean.getUid(), bean.getStream());
        dismiss();
    }

}
