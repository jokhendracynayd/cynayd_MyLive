package com.livestreaming.video.dialog;

//import com.livestreaming.common.mob.MobBean;
import com.livestreaming.video.R;

/**
 * Created by cxf on 2018/10/19.
 * 视频分享弹窗
 */

/*public class VideoShareDialogFragment extends AbsDialogFragment implements OnItemClickListener<MobBean> {

    private RecyclerView mRecyclerView;
    private RecyclerView mRecyclerView2;
    private VideoBean mVideoBean;


    @Override
    protected int getLayoutId() {
        return R.layout.dialog_video_share;
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
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.BOTTOM;
        window.setAttributes(params);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle == null) {
            return;
        }
        mVideoBean = bundle.getParcelable(Constants.VIDEO_BEAN);
        if (mVideoBean == null) {
            return;
        }
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
        mRecyclerView2 = (RecyclerView) findViewById(R.id.recyclerView_2);
        mRecyclerView2.setHasFixedSize(true);
        mRecyclerView2.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
        List<MobBean> list = null;
        ConfigBean configBean = CommonAppConfig.getInstance().getConfig();
        if (configBean != null) {
            list = MobBean.getVideoShareTypeList(configBean.getVideoShareTypes());
        }
        if (list != null) {
            VideoShareAdapter adapter = new VideoShareAdapter(mContext, list);
            adapter.setOnItemClickListener(this);
            mRecyclerView.setAdapter(adapter);
        }
        List<MobBean> list2 = new ArrayList<>();
        MobBean linkBean = new MobBean();
        linkBean.setType(Constants.LINK);
        linkBean.setName(com.livestreaming.common.R.string.copy_link);
        linkBean.setIcon1(R.mipmap.icon_share_video_link);
        list2.add(linkBean);
        MobBean reportBean = new MobBean();
        if (mVideoBean.getUid().equals(CommonAppConfig.getInstance().getUid())) {//自己的视频
            reportBean.setType(Constants.DELETE);
            reportBean.setName(com.livestreaming.common.R.string.delete);
            reportBean.setIcon1(R.mipmap.icon_share_video_delete);
        } else {
            reportBean.setType(Constants.REPORT);
            reportBean.setName(com.livestreaming.common.R.string.report);
            reportBean.setIcon1(R.mipmap.icon_share_video_report);
        }
        list2.add(reportBean);
        MobBean saveBean = new MobBean();
        saveBean.setType(Constants.SAVE);
        saveBean.setName(com.livestreaming.common.R.string.download);
        saveBean.setIcon1(R.mipmap.icon_share_video_save);
        list2.add(saveBean);
        VideoShareAdapter adapter2 = new VideoShareAdapter(mContext, list2);
        adapter2.setOnItemClickListener(this);
        mRecyclerView2.setAdapter(adapter2);
    }

    @Override
    public void onItemClick(MobBean bean, int position) {
        if (!canClick()) {
            return;
        }
        dismiss();
        switch (bean.getType()) {
            case Constants.LINK://复制链接
                ((AbsVideoPlayActivity) mContext).copyLink(mVideoBean);
                break;
            case Constants.REPORT://举报
                VideoReportActivity.forward(mContext, mVideoBean.getId());
                break;
            case Constants.SAVE://保存
                ((AbsVideoPlayActivity) mContext).downloadVideo(mVideoBean);
                break;
            case Constants.DELETE://删除
                ((AbsVideoPlayActivity) mContext).deleteVideo(mVideoBean);
                break;
            default:
                ((AbsVideoPlayActivity) mContext).shareVideoPage(bean.getType(), mVideoBean);
                break;
        }
    }

}*/
