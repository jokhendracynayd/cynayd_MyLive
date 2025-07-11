package com.livestreaming.video.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.EditText;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.livestreaming.common.CommonAppConfig;
import com.livestreaming.common.Constants;
import com.livestreaming.common.activity.AbsActivity;
import com.livestreaming.common.adapter.RefreshAdapter;
import com.livestreaming.common.bean.UserBean;
import com.livestreaming.common.custom.CommonRefreshView;
import com.livestreaming.common.custom.InterceptFrameLayout;
import com.livestreaming.common.custom.MyLinearLayout3;
import com.livestreaming.common.http.HttpCallback;
import com.livestreaming.common.interfaces.ActivityResultCallback;
import com.livestreaming.common.interfaces.OnItemClickListener;
import com.livestreaming.common.utils.ActivityResultUtil;
import com.livestreaming.common.utils.DialogUitl;
import com.livestreaming.common.utils.L;
import com.livestreaming.common.utils.StringUtil;
import com.livestreaming.common.utils.ToastUtil;
import com.livestreaming.common.utils.WordUtil;
import com.livestreaming.common.views.AbsViewHolder;
import com.livestreaming.common.views.InputViewHolder;
import com.livestreaming.video.R;
import com.livestreaming.video.activity.AtFriendActivity;
import com.livestreaming.video.adapter.VideoCommentAdapter;
import com.livestreaming.video.bean.VideoCommentBean;
import com.livestreaming.video.custom.AtEditText;
import com.livestreaming.video.event.VideoCommentEvent;
import com.livestreaming.video.http.VideoHttpConsts;
import com.livestreaming.video.http.VideoHttpUtil;

import org.greenrobot.eventbus.EventBus;

import java.util.Arrays;
import java.util.List;

/**
 * Created by cxf on 2018/12/3.
 * 视频评论相关
 */

public class VideoCommentViewHolder extends AbsViewHolder implements View.OnClickListener, OnItemClickListener<VideoCommentBean>, VideoCommentAdapter.ActionListener {

    private View mRoot;
    private MyLinearLayout3 mBottom;
    private CommonRefreshView mRefreshView;
    private TextView mCommentNum;
    private VideoCommentAdapter mVideoCommentAdapter;
    private String mVideoId;
    private String mVideoUid;
    private String mCommentString;
    private ObjectAnimator mShowAnimator;
    private ObjectAnimator mHideAnimator;
    private boolean mAnimating;
    private boolean mNeedRefresh;//是否需要刷新
    private InputViewHolder mInputViewHolder;
    private VideoCommentBean mVideoCommentBean;
    private boolean mShowComment;
    private AtEditText mAtEditText;

    public VideoCommentViewHolder(Context context, ViewGroup parentView) {
        super(context, parentView);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_video_comment;
    }

    @Override
    public void init() {
        mRoot = findViewById(R.id.root);
        mBottom = (MyLinearLayout3) findViewById(R.id.bottom);
        int height = mBottom.getHeight2();
        mBottom.setTranslationY(height);
        mShowAnimator = ObjectAnimator.ofFloat(mBottom, "translationY", 0);
        mHideAnimator = ObjectAnimator.ofFloat(mBottom, "translationY", height);
        mShowAnimator.setDuration(200);
        mHideAnimator.setDuration(200);
        TimeInterpolator interpolator = new AccelerateDecelerateInterpolator();
        mShowAnimator.setInterpolator(interpolator);
        mHideAnimator.setInterpolator(interpolator);
        AnimatorListenerAdapter animatorListener = new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                mAnimating = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mAnimating = false;
                if (animation == mHideAnimator) {
                    if (mRoot != null && mRoot.getVisibility() == View.VISIBLE) {
                        mRoot.setVisibility(View.INVISIBLE);
                    }
                } else if (animation == mShowAnimator) {
                    if (mNeedRefresh) {
                        mNeedRefresh = false;
                        if (mRefreshView != null) {
                            mRefreshView.initData();
                        }
                    }
                }
            }
        };
        mShowAnimator.addListener(animatorListener);
        mHideAnimator.addListener(animatorListener);

        findViewById(R.id.root).setOnClickListener(this);
        findViewById(R.id.btn_close).setOnClickListener(this);
        mCommentString = WordUtil.getString(com.livestreaming.common.R.string.video_comment);
        mCommentNum = (TextView) findViewById(R.id.comment_num);
        mRefreshView = (CommonRefreshView) findViewById(R.id.refreshView);
        mRefreshView.setEmptyLayoutId(R.layout.view_no_data_comment);
        mRefreshView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false) {
            @Override
            public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
                try {
                    super.onLayoutChildren(recycler, state);
                } catch (Exception e) {
                    L.e("onLayoutChildren------>" + e.getMessage());
                }
            }
        });

        mRefreshView.setDataHelper(new CommonRefreshView.DataHelper<VideoCommentBean>() {
            @Override
            public RefreshAdapter<VideoCommentBean> getAdapter() {
                if (mVideoCommentAdapter == null) {
                    mVideoCommentAdapter = new VideoCommentAdapter(mContext);
                    mVideoCommentAdapter.setVideoUid(mVideoUid);
                    mVideoCommentAdapter.setOnItemClickListener(VideoCommentViewHolder.this);
                    mVideoCommentAdapter.setActionListener(VideoCommentViewHolder.this);
                }
                return mVideoCommentAdapter;
            }

            @Override
            public void loadData(int p, HttpCallback callback) {
                if (!TextUtils.isEmpty(mVideoId)) {
                    VideoHttpUtil.getVideoCommentList(mVideoId, p, callback);
                }
            }

            @Override
            public List<VideoCommentBean> processData(String[] info) {
                JSONObject obj = JSON.parseObject(info[0]);
                String commentNum = obj.getString("comments");
                EventBus.getDefault().post(new VideoCommentEvent(mVideoId, commentNum));
                if (mCommentNum != null) {
                    mCommentNum.setText(StringUtil.contact(commentNum, " ", mCommentString));
                }
                List<VideoCommentBean> list = JSON.parseArray(obj.getString("commentlist"), VideoCommentBean.class);
                for (VideoCommentBean bean : list) {
                    if (bean != null) {
                        bean.setParentNode(true);
                    }
                }
                return list;
            }

            @Override
            public void onRefreshSuccess(List<VideoCommentBean> list, int listCount) {

            }

            @Override
            public void onRefreshFailure() {

            }

            @Override
            public void onLoadMoreSuccess(List<VideoCommentBean> loadItemList, int loadItemCount) {

            }

            @Override
            public void onLoadMoreFailure() {

            }
        });
        mInputViewHolder = new InputViewHolder(mContext,
                (ViewGroup) findViewById(R.id.input_container),
                false, R.layout.view_input_video_comment, com.livestreaming.common.R.layout.view_input_face
        );
        mInputViewHolder.addToParent();
        mInputViewHolder.setActionListener(new InputViewHolder.ActionListener() {
            @Override
            public void onSendClick(String text) {
                sendComment(text);
            }
        });
        mInputViewHolder.setOnClickListener(R.id.btn_at, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickAt();
            }
        });
        EditText editText = mInputViewHolder.getEditText();
        if (editText instanceof AtEditText) {
            mAtEditText = (AtEditText) editText;
            mAtEditText.setActionListener(new AtEditText.ActionListener() {
                @Override
                public void onAtClick() {
                    clickAt();
                }

                @Override
                public void onContainsUid() {
                    ToastUtil.show(com.livestreaming.common.R.string.video_at_tip_1);
                }

                @Override
                public void onContainsName() {
                    ToastUtil.show(com.livestreaming.common.R.string.video_at_tip_2);
                }
            });
        }

        InterceptFrameLayout groupIntercept = findViewById(R.id.group_intercept);
        groupIntercept.setOnInterceptListener(new InterceptFrameLayout.OnInterceptListener() {
            @Override
            public boolean onInterceptCall() {
                if (mInputViewHolder != null) {
                    boolean hasHide = mInputViewHolder.hideKeyBoardFaceMore();
                    if (hasHide && !mShowComment) {
                        hideBottom();
                    }
                    return hasHide;
                }
                return false;
            }
        });
    }


    private void clickAt() {
        mInputViewHolder.hideKeyBoard();
        chooseAtUser();
    }

    /**
     * 选择要@的人
     */
    public void chooseAtUser() {
        ActivityResultUtil.startActivityForResult(((AbsActivity) mContext), new Intent(mContext, AtFriendActivity.class),
                new ActivityResultCallback() {
                    @Override
                    public void onSuccess(Intent intent) {
                        if (intent != null) {
                            UserBean userBean = intent.getParcelableExtra(Constants.USER_BEAN);
                            if (userBean != null) {
                                if (mAtEditText != null) {
                                    mAtEditText.addAtSpan(userBean.getId(), userBean.getUserNiceName());
                                }
                            }
                        }
                    }
                });
    }


    public void setVideoInfo(String videoId, String videoUid) {
        if (!TextUtils.isEmpty(videoId) && !TextUtils.isEmpty(videoUid)) {
            if (!TextUtils.isEmpty(mVideoId) && !mVideoId.equals(videoId)) {
                if (mVideoCommentAdapter != null) {
                    mVideoCommentAdapter.clearData();
                    mVideoCommentAdapter.setVideoUid(videoUid);
                }
            }
            if (!videoId.equals(mVideoId)) {
                mNeedRefresh = true;
            }
            mVideoId = videoId;
            mVideoUid = videoUid;
        }

    }

    public void showBottom(boolean showComment, boolean openFace, boolean openKeyBoard) {
        mShowComment = showComment;
        if (showComment) {
            if (mRoot != null && mRoot.getVisibility() != View.VISIBLE) {
                mRoot.setVisibility(View.VISIBLE);
            }
            if (mBottom != null && mBottom.getVisibility() != View.VISIBLE) {
                mBottom.setVisibility(View.VISIBLE);
            }
            if (!mAnimating) {
                if (mShowAnimator != null) {
                    mShowAnimator.start();
                }
            }
        } else {
            if (mRoot != null && mRoot.getVisibility() != View.VISIBLE) {
                mRoot.setVisibility(View.VISIBLE);
            }
            if (mBottom != null && mBottom.getVisibility() == View.VISIBLE) {
                mBottom.setVisibility(View.INVISIBLE);
            }
        }
        mVideoCommentBean = null;
        if (mInputViewHolder != null) {
            mInputViewHolder.clearEditText();
            mInputViewHolder.setDefaultHint();
            if (!showComment) {
                if (openFace) {
                    mInputViewHolder.showFace();
                } else if (openKeyBoard) {
                    mInputViewHolder.showKeyBoard();
                }
            }
        }

    }

    private void hideBottom() {
        if (mShowComment) {
            if (!mAnimating) {
                if (mHideAnimator != null) {
                    mHideAnimator.start();
                }
            }
        } else {
            if (mRoot != null && mRoot.getVisibility() == View.VISIBLE) {
                mRoot.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.root || i == R.id.btn_close) {
            hideBottom();
        }
    }


    public void release() {
        if (mShowAnimator != null) {
            mShowAnimator.cancel();
        }
        mShowAnimator = null;
        if (mHideAnimator != null) {
            mHideAnimator.cancel();
        }
        mHideAnimator = null;
        VideoHttpUtil.cancel(VideoHttpConsts.GET_VIDEO_COMMENT_LIST);
        VideoHttpUtil.cancel(VideoHttpConsts.SET_COMMENT_LIKE);
        VideoHttpUtil.cancel(VideoHttpConsts.GET_COMMENT_REPLY);
        VideoHttpUtil.cancel(VideoHttpConsts.SET_COMMENT);
        VideoHttpUtil.cancel(VideoHttpConsts.DELETE_COMMENT);
    }

    @Override
    public void onItemClick(VideoCommentBean bean, int position) {
        if (bean != null && !TextUtils.isEmpty(bean.getUid()) && bean.getUid().equals(CommonAppConfig.getInstance().getUid())) {
            ToastUtil.show(com.livestreaming.common.R.string.video_cannot_apply_self);
            return;
        }
        mVideoCommentBean = bean;
        if (mInputViewHolder != null) {
            if (bean != null) {
                UserBean replyUserBean = bean.getUserBean();//要回复的人
                if (replyUserBean != null) {
                    mInputViewHolder.setEditHint(StringUtil.contact(WordUtil.getString(com.livestreaming.common.R.string.video_comment_reply_2), replyUserBean.getUserNiceName()));
                }
            }
            mInputViewHolder.showKeyBoard();
        }
    }


    /**
     * 发表评论
     */
    private void sendComment(String content) {
        if (TextUtils.isEmpty(mVideoId) || TextUtils.isEmpty(mVideoUid)) {
            return;
        }
        if (TextUtils.isEmpty(content)) {
            ToastUtil.show(com.livestreaming.common.R.string.content_empty);
            return;
        }
        String toUid = mVideoUid;
        String commentId = "0";
        String parentId = "0";
        if (mVideoCommentBean != null) {
            toUid = mVideoCommentBean.getUid();
            commentId = mVideoCommentBean.getCommentId();
            parentId = mVideoCommentBean.getId();
        }
        String atInfo = mAtEditText.getAtUserInfo();
        VideoHttpUtil.setComment(toUid, mVideoId, content, commentId, parentId, atInfo, new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0) {
                    if (info.length > 0) {
                        JSONObject obj = JSON.parseObject(info[0]);
                        String commentNum = obj.getString("comments");
                        EventBus.getDefault().post(new VideoCommentEvent(mVideoId, commentNum));
                        ToastUtil.show(msg);
                        if (mInputViewHolder != null) {
                            mInputViewHolder.hideKeyBoardFaceMore();
                            mInputViewHolder.clearEditText();
                            mInputViewHolder.setDefaultHint();
                        }
                        mVideoCommentBean = null;
                        mNeedRefresh = true;
                        hideBottom();
                    }
                } else {
                    ToastUtil.show(msg);
                }
            }

            @Override
            public boolean showLoadingDialog() {
                return true;
            }

            @Override
            public Dialog createLoadingDialog() {
                return DialogUitl.loadingDialog(mContext);
            }
        });
    }


    @Override
    public void onExpandClicked(final VideoCommentBean commentBean) {
        final VideoCommentBean parentNodeBean = commentBean.getParentNodeBean();
        if (parentNodeBean == null) {
            return;
        }
        VideoHttpUtil.getCommentReply(parentNodeBean.getId(), parentNodeBean.getChildPage(), new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0) {
                    List<VideoCommentBean> list = JSON.parseArray(Arrays.toString(info), VideoCommentBean.class);
                    if (list == null || list.size() == 0) {
                        return;
                    }
                    if (parentNodeBean.getChildPage() == 1) {
                        if (list.size() > 1) {
                            list = list.subList(1, list.size());
                        }
                    }
                    for (VideoCommentBean bean : list) {
                        bean.setParentNodeBean(parentNodeBean);
                    }
                    List<VideoCommentBean> childList = parentNodeBean.getChildList();
                    if (childList != null) {
                        childList.addAll(list);
                        if (childList.size() < parentNodeBean.getReplyNum()) {
                            parentNodeBean.setChildPage(parentNodeBean.getChildPage() + 1);
                        }
                        if (mVideoCommentAdapter != null) {
                            mVideoCommentAdapter.insertReplyList(commentBean, list.size());
                        }
                    }
                }
            }
        });
    }

    @Override
    public void onCollapsedClicked(VideoCommentBean commentBean) {
        VideoCommentBean parentNodeBean = commentBean.getParentNodeBean();
        if (parentNodeBean == null) {
            return;
        }
        List<VideoCommentBean> childList = parentNodeBean.getChildList();
        VideoCommentBean node0 = childList.get(0);
        int orignSize = childList.size();
        parentNodeBean.removeChild();
        parentNodeBean.setChildPage(1);
        if (mVideoCommentAdapter != null) {
            mVideoCommentAdapter.removeReplyList(node0, orignSize - childList.size());
        }
    }

    /**
     * 长按事件
     */
    @Override
    public void onItemLongClick(final VideoCommentBean bean) {
        String uid = CommonAppConfig.getInstance().getUid();
        Integer[] arr = null;
        //是否是自己的视频
        boolean isSelfVideo = !TextUtils.isEmpty(mVideoUid) && mVideoUid.equals(uid);
        //是否是自己的评论
        String commentUid = bean.getUid();
        boolean isSelfComment = !TextUtils.isEmpty(commentUid) && commentUid.equals(uid);
        if (isSelfVideo || isSelfComment) {
            arr = new Integer[]{com.livestreaming.common.R.string.copy, com.livestreaming.common.R.string.delete};
        } else {
            arr = new Integer[]{com.livestreaming.common.R.string.copy};
        }
        if (arr == null) {
            return;
        }
        DialogUitl.showStringArrayDialog(mContext, arr, new DialogUitl.StringArrayDialogCallback() {
            @Override
            public void onItemClick(String text, int tag) {
                if (tag == com.livestreaming.common.R.string.copy) {//复制评论
                    UserBean u = bean.getUserBean();
                    if (u != null) {
                        String content = StringUtil.contact("@", u.getUserNiceName(), ": ", bean.getContent());
                        ClipboardManager cm = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clipData = ClipData.newPlainText("text", content);
                        cm.setPrimaryClip(clipData);
                        ToastUtil.show(com.livestreaming.common.R.string.copy_success);
                    }
                } else if (tag == com.livestreaming.common.R.string.delete) {
                    //删除评论
                    VideoHttpUtil.deleteComment(bean.getVideoId(), bean.getId(), bean.getUid(), new HttpCallback() {

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
            }
        });
    }

    public void onKeyBoardHeightChanged(int keyboardHeight) {
        if (mInputViewHolder != null) {
            mInputViewHolder.onKeyBoardHeightChanged(keyboardHeight);
        }
    }

    public boolean canBack() {
        if (mInputViewHolder != null && mInputViewHolder.hideKeyBoardFaceMore()) {
            return false;
        }
        return true;
    }


}
