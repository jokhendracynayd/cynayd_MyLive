package com.livestreaming.video.dialog;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.livestreaming.common.adapter.RefreshAdapter;
import com.livestreaming.common.custom.CommonRefreshView;
import com.livestreaming.common.http.HttpCallback;
import com.livestreaming.video.R;
import com.livestreaming.video.adapter.MusicAdapter;
import com.livestreaming.video.bean.MusicBean;
import com.livestreaming.video.http.VideoHttpConsts;
import com.livestreaming.video.http.VideoHttpUtil;
import com.livestreaming.video.interfaces.VideoMusicActionListener;

import java.util.Arrays;
import java.util.List;

/**
 * Created by cxf on 2018/12/7.
 */

public class VideoMusicClassDialog extends PopupWindow implements View.OnClickListener {

    private Context mContext;
    private View mParent;
    private String mTitle;
    private String mClassId;
    private CommonRefreshView mRefreshView;
    private MusicAdapter mAdapter;
    private VideoMusicActionListener mActionListener;

    public VideoMusicClassDialog(Context context, View parent, String title, String classId, VideoMusicActionListener actionListener) {
        mContext = context;
        mParent = parent;
        mTitle = title;
        mClassId = classId;
        mActionListener = actionListener;
        setContentView(initView());
        setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
        setBackgroundDrawable(new ColorDrawable());
        setOutsideTouchable(true);
        setClippingEnabled(false);
        setFocusable(true);
        setAnimationStyle(com.livestreaming.common.R.style.leftToRightAnim);
        setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss() {
                VideoHttpUtil.cancel(VideoHttpConsts.GET_MUSIC_LIST);
                if (mAdapter != null) {
                    mAdapter.setActionListener(null);
                }
                if (mActionListener != null) {
                    mActionListener.onStopMusic();
                }
                mActionListener = null;
            }
        });
    }


    private View initView() {
        View v = LayoutInflater.from(mContext).inflate(R.layout.view_video_music_class, null);
        TextView title = v.findViewById(R.id.title);
        if (!TextUtils.isEmpty(mTitle)) {
            title.setText(mTitle);
        }
        mRefreshView = v.findViewById(R.id.refreshView);
        mRefreshView.setEmptyLayoutId(R.layout.view_no_data_music_class);
        mRefreshView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));

        mRefreshView.setDataHelper(new CommonRefreshView.DataHelper<MusicBean>() {
            @Override
            public RefreshAdapter<MusicBean> getAdapter() {
                if (mAdapter == null) {
                    mAdapter = new MusicAdapter(mContext);
                    mAdapter.setActionListener(new VideoMusicActionListener() {
                        @Override
                        public void onPlayMusic(MusicAdapter adapter, MusicBean bean, int position) {
                            if (mActionListener != null) {
                                mActionListener.onPlayMusic(adapter, bean, position);
                            }
                        }

                        @Override
                        public void onStopMusic() {
                            if (mActionListener != null) {
                                mActionListener.onStopMusic();
                            }
                        }

                        @Override
                        public void onUseClick(MusicBean bean) {
                            if (mActionListener != null) {
                                mActionListener.onUseClick(bean);
                            }
                            dismiss();
                        }

                        @Override
                        public void onCollect(MusicAdapter adapter, int musicId, int isCollect) {
                            if (mActionListener != null) {
                                mActionListener.onCollect(adapter, musicId, isCollect);
                            }
                        }
                    });
                }
                return mAdapter;
            }

            @Override
            public void loadData(int p, HttpCallback callback) {
                if (!TextUtils.isEmpty(mClassId)) {
                    VideoHttpUtil.getMusicList(mClassId, p, callback);
                }
            }

            @Override
            public List<MusicBean> processData(String[] info) {
                return JSON.parseArray(Arrays.toString(info), MusicBean.class);
            }

            @Override
            public void onRefreshSuccess(List<MusicBean> list, int listCount) {

            }

            @Override
            public void onRefreshFailure() {

            }

            @Override
            public void onLoadMoreSuccess(List<MusicBean> loadItemList, int loadItemCount) {

            }

            @Override
            public void onLoadMoreFailure() {

            }
        });
        v.findViewById(R.id.btn_close).setOnClickListener(this);
        return v;
    }

    public void show() {
        showAtLocation(mParent, Gravity.BOTTOM, 0, 0);
        if (mRefreshView != null) {
            mRefreshView.initData();
        }
    }

    @Override
    public void onClick(View v) {
        dismiss();
    }

}
