package com.livestreaming.video.activity;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.ViewGroup;

import com.livestreaming.common.Constants;
import com.livestreaming.common.utils.L;
import com.livestreaming.video.R;
import com.livestreaming.video.bean.VideoBean;
import com.livestreaming.video.http.VideoHttpUtil;
import com.livestreaming.video.utils.VideoStorge;
import com.livestreaming.video.views.VideoScrollViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cxf on 2018/11/26.
 */

public class VideoPlayActivity extends AbsVideoPlayActivity {

    public static void forward(Context context, int position, String videoKey, int page) {
        Intent intent = new Intent(context, VideoPlayActivity.class);
        intent.putExtra(Constants.VIDEO_POSITION, position);
        intent.putExtra(Constants.VIDEO_KEY, videoKey);
        intent.putExtra(Constants.VIDEO_PAGE, page);
        context.startActivity(intent);
    }


    public static void forwardSingle(Context context, VideoBean videoBean) {
        if (videoBean == null) {
            return;
        }
        List<VideoBean> list = new ArrayList<>();
        list.add(videoBean);
        VideoStorge.getInstance().put(Constants.VIDEO_SINGLE, list);
        Intent intent = new Intent(context, VideoPlayActivity.class);
        intent.putExtra(Constants.VIDEO_POSITION, 0);
        intent.putExtra(Constants.VIDEO_KEY, Constants.VIDEO_SINGLE);
        intent.putExtra(Constants.VIDEO_PAGE, 1);
        context.startActivity(intent);
    }


    @Override
    protected int getLayoutId() {
        return R.layout.activity_video_play;
    }

    @Override
    protected boolean isStatusBarWhite() {
        return true;
    }

    @Override
    protected void main() {
        super.main();
        Intent intent = getIntent();
        mVideoKey = intent.getStringExtra(Constants.VIDEO_KEY);
        if (TextUtils.isEmpty(mVideoKey)) {
            return;
        }
        int position = intent.getIntExtra(Constants.VIDEO_POSITION, 0);
        int page = intent.getIntExtra(Constants.VIDEO_PAGE, 1);
        mVideoScrollViewHolder = new VideoScrollViewHolder(mContext, (ViewGroup) findViewById(R.id.container), position, mVideoKey, page);
        mVideoScrollViewHolder.addToParent();
        mVideoScrollViewHolder.subscribeActivityLifeCycle();
        VideoHttpUtil.startWatchVideo();
    }

    @Override
    public void onBackPressed() {
        if (mVideoCommentViewHolder != null && !mVideoCommentViewHolder.canBack()) {
            return;
        }
        VideoHttpUtil.endWatchVideo();
        release();
        super.onBackPressed();
    }


    @Override
    protected void onDestroy() {
        release();
        super.onDestroy();
        L.e("VideoPlayActivity------->onDestroy");
    }


}
