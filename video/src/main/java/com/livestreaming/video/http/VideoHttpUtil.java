package com.livestreaming.video.http;

import android.text.TextUtils;

import com.livestreaming.common.CommonAppConfig;
import com.livestreaming.common.http.CommonHttpUtil;
import com.livestreaming.common.http.HttpCallback;
import com.livestreaming.common.http.HttpClient;
import com.livestreaming.common.utils.MD5Util;

/**
 * Created by cxf on 2018/9/17.
 */

public class VideoHttpUtil {

    private static final String VIDEO_SALT = "#2hgfk85cm23mk58vncsark";

    /**
     * 取消网络请求
     */
    public static void cancel(String tag) {
        HttpClient.getInstance().cancel(tag);
    }

    /**
     * 获取首页视频列表
     */
    public static void getHomeVideoList(int p, HttpCallback callback) {
        HttpClient.getInstance().get("Video.GetVideoList", VideoHttpConsts.GET_HOME_VIDEO_LIST)
                .params("uid", CommonAppConfig.getInstance().getUid())
                .params("token", CommonAppConfig.getInstance().getToken())
                .params("p", p)
                .execute(callback);
    }

    /**
     * 获取首页视频分类列表
     */
    public static void getHomeVideoClassList(int videoClassId, int p, HttpCallback callback) {
        HttpClient.getInstance().get("Video.getClassVideo", VideoHttpConsts.GET_HOME_VIDEO_CLASS_LIST)
                .params("uid", CommonAppConfig.getInstance().getUid())
                .params("token", CommonAppConfig.getInstance().getToken())
                .params("videoclassid", videoClassId)
                .params("p", p)
                .execute(callback);
    }


    /**
     * 视频点赞
     */
    public static void setVideoLike(String tag, String videoid, HttpCallback callback) {
        HttpClient.getInstance().get("Video.AddLike", tag)
                .params("uid", CommonAppConfig.getInstance().getUid())
                .params("token", CommonAppConfig.getInstance().getToken())
                .params("videoid", videoid)
                .execute(callback);
    }

    /**
     * 获取视频评论
     */
    public static void getVideoCommentList(String videoid, int p, HttpCallback callback) {
        HttpClient.getInstance().get("Video.GetComments", VideoHttpConsts.GET_VIDEO_COMMENT_LIST)
                .params("uid", CommonAppConfig.getInstance().getUid())
                .params("token", CommonAppConfig.getInstance().getToken())
                .params("videoid", videoid)
                .params("p", p)
                .execute(callback);
    }

    /**
     * 评论点赞
     */
    public static void setCommentLike(String commentid, HttpCallback callback) {
        HttpClient.getInstance().get("Video.addCommentLike", VideoHttpConsts.SET_COMMENT_LIKE)
                .params("commentid", commentid)
                .params("uid", CommonAppConfig.getInstance().getUid())
                .params("token", CommonAppConfig.getInstance().getToken())
                .execute(callback);
    }

    /**
     * 发表评论
     */
    public static void setComment(String toUid, String videoId, String content, String commentId, String parentId,String atInfo, HttpCallback callback) {
        HttpClient.getInstance().get("Video.setComment", VideoHttpConsts.SET_COMMENT)
                .params("uid", CommonAppConfig.getInstance().getUid())
                .params("token", CommonAppConfig.getInstance().getToken())
                .params("touid", toUid)
                .params("videoid", videoId)
                .params("commentid", commentId)
                .params("parentid", parentId)
                .params("content", content)
                .params("at_info", atInfo)
                .execute(callback);
    }


    /**
     * 获取评论回复
     */
    public static void getCommentReply(String commentid, int p, HttpCallback callback) {
        HttpClient.getInstance().get("Video.getReplys", VideoHttpConsts.GET_COMMENT_REPLY)
                .params("commentid", commentid)
                .params("uid", CommonAppConfig.getInstance().getUid())
                .params("token", CommonAppConfig.getInstance().getToken())
                .params("p", p)
                .execute(callback);
    }

    /**
     * 获取视频音乐分类列表
     */
    public static void getMusicClassList(HttpCallback callback) {
        HttpClient.getInstance().get("Music.classify_list", VideoHttpConsts.GET_MUSIC_CLASS_LIST)
                .execute(callback);
    }

    /**
     * 获取热门视频音乐列表
     */
    public static void getHotMusicList(int p, HttpCallback callback) {
        HttpClient.getInstance().get("Music.hotLists", VideoHttpConsts.GET_HOT_MUSIC_LIST)
                .params("uid", CommonAppConfig.getInstance().getUid())
                .params("token", CommonAppConfig.getInstance().getToken())
                .params("p", p)
                .execute(callback);
    }

    /**
     * 音乐收藏
     */
    public static void setMusicCollect(int muiscId, HttpCallback callback) {
        HttpClient.getInstance().get("Music.collectMusic", VideoHttpConsts.SET_MUSIC_COLLECT)
                .params("uid", CommonAppConfig.getInstance().getUid())
                .params("token", CommonAppConfig.getInstance().getToken())
                .params("musicid", muiscId)
                .execute(callback);
    }

    /**
     * 音乐收藏列表
     */
    public static void getMusicCollectList(int p, HttpCallback callback) {
        HttpClient.getInstance().get("Music.getCollectMusicLists", VideoHttpConsts.GET_MUSIC_COLLECT_LIST)
                .params("uid", CommonAppConfig.getInstance().getUid())
                .params("token", CommonAppConfig.getInstance().getToken())
                .params("p", p)
                .execute(callback);
    }

    /**
     * 获取具体分类下的音乐列表
     */
    public static void getMusicList(String classId, int p, HttpCallback callback) {
        HttpClient.getInstance().get("Music.music_list", VideoHttpConsts.GET_MUSIC_LIST)
                .params("uid", CommonAppConfig.getInstance().getUid())
                .params("token", CommonAppConfig.getInstance().getToken())
                .params("classify", classId)
                .params("p", p)
                .execute(callback);
    }


    /**
     * 搜索音乐
     */
    public static void videoSearchMusic(String key, int p, HttpCallback callback) {
        HttpClient.getInstance().get("Music.searchMusic", VideoHttpConsts.VIDEO_SEARCH_MUSIC)
                .params("uid", CommonAppConfig.getInstance().getUid())
                .params("token", CommonAppConfig.getInstance().getToken())
                .params("key", key)
                .params("p", p)
                .execute(callback);
    }


    /**
     * 上传视频，获取七牛云token的接口
     */

    public static void getQiNiuToken(HttpCallback callback) {
        HttpClient.getInstance().get("Video.getQiniuToken", VideoHttpConsts.GET_QI_NIU_TOKEN)
                .execute(callback);
    }


    /**
     * 短视频上传信息
     *
     * @param title   短视频标题
     * @param thumb   短视频封面图url
     * @param href    短视频视频url
     * @param musicId 背景音乐Id
     * @param type    绑定的内容类型 0 没绑定 1 商品 2 付费内容
     */
    public static void saveUploadVideoInfo(
            String title,
            String thumb,
            String href,
            String waterUrl,
            String goodsId,
            int musicId,
            boolean openLocation,
//            int videoClassId,
            int type,
            double videoRatio,
            HttpCallback callback) {
        HttpClient.getInstance().get("Video.setVideo", VideoHttpConsts.SAVE_UPLOAD_VIDEO_INFO)
                .params("uid", CommonAppConfig.getInstance().getUid())
                .params("token", CommonAppConfig.getInstance().getToken())
                .params("lat", openLocation ? String.valueOf(CommonAppConfig.getInstance().getLat()) : "")
                .params("lng", openLocation ? String.valueOf(CommonAppConfig.getInstance().getLng()) : "")
                .params("city", openLocation ? CommonAppConfig.getInstance().getCity() : "")
                .params("title", title)
                .params("thumb", thumb)
                .params("href", href)
                .params("href_w", waterUrl)
                .params("music_id", musicId)
                .params("goodsid", goodsId)
//                .params("classid", videoClassId)
                .params("type", type)
                .params("anyway", videoRatio == 0 ? "1.778" : String.format("%.3f", videoRatio))
                .execute(callback);
    }


    /**
     * 获取某人发布的视频
     */
    public static void getHomeVideo(String toUid, int p, HttpCallback callback) {
        HttpClient.getInstance().get("Video.getHomeVideo", VideoHttpConsts.GET_HOME_VIDEO)
                .params("uid", CommonAppConfig.getInstance().getUid())
                .params("token", CommonAppConfig.getInstance().getToken())
                .params("touid", toUid)
                .params("p", p)
                .execute(callback);
    }

    /**
     * 获取喜欢的视频列表
     */
    public static void getLikeVideo(int p, HttpCallback callback) {
        HttpClient.getInstance().get("Video.getLikeVideos", VideoHttpConsts.GET_LIKE_VIDEO)
                .params("uid", CommonAppConfig.getInstance().getUid())
                .params("token", CommonAppConfig.getInstance().getToken())
                .params("p", p)
                .execute(callback);
    }

    /**
     * 获取举报内容列表
     */
    public static void getVideoReportList(HttpCallback callback) {
        HttpClient.getInstance().get("Video.getReportContentlist", VideoHttpConsts.GET_VIDEO_REPORT_LIST)
                .execute(callback);
    }

    /**
     * 举报视频接口
     */
    public static void videoReport(String videoId, String reportId, String content, HttpCallback callback) {
        HttpClient.getInstance().get("Video.report", VideoHttpConsts.VIDEO_REPORT)
                .params("uid", CommonAppConfig.getInstance().getUid())
                .params("token", CommonAppConfig.getInstance().getToken())
                .params("videoid", videoId)
                .params("type", reportId)
                .params("content", content)
                .execute(callback);
    }

    /**
     * 删除自己的视频
     */
    public static void videoDelete(String videoid, HttpCallback callback) {
        HttpClient.getInstance().get("Video.del", VideoHttpConsts.VIDEO_DELETE)
                .params("uid", CommonAppConfig.getInstance().getUid())
                .params("token", CommonAppConfig.getInstance().getToken())
                .params("videoid", videoid)
                .execute(callback);
    }

    /**
     * 分享视频
     */
    public static void setVideoShare(String videoid, HttpCallback callback) {
        String uid = CommonAppConfig.getInstance().getUid();
        String s = MD5Util.getMD5(uid + "-" + videoid + "-" + VIDEO_SALT);
        HttpClient.getInstance().get("Video.addShare", VideoHttpConsts.SET_VIDEO_SHARE)
                .params("uid", uid)
                .params("token", CommonAppConfig.getInstance().getToken())
                .params("videoid", videoid)
                .params("random_str", s)
                .execute(callback);
    }

    /**
     * 发布视频的时候是否显示 关联商品
     */
    public static void getConcatGoods(HttpCallback callback) {
        HttpClient.getInstance().get("Video.getCon", VideoHttpConsts.GET_CONCAT_GOODS)
                .params("uid", CommonAppConfig.getInstance().getUid())
                .execute(callback);
    }


    /**
     * 开始观看视频的时候请求这个接口
     */
    public static void videoWatchStart(String videoUid, String videoId) {
        if(!CommonAppConfig.getInstance().isLogin()){
            return;
        }
        String uid = CommonAppConfig.getInstance().getUid();
        if (TextUtils.isEmpty(uid) || uid.equals(videoUid)) {
            return;
        }
        VideoHttpUtil.cancel(VideoHttpConsts.VIDEO_WATCH_START);
        String s = MD5Util.getMD5(uid + "-" + videoId + "-" + VIDEO_SALT);
        HttpClient.getInstance().get("Video.addView", VideoHttpConsts.VIDEO_WATCH_START)
                .params("uid", uid)
                .params("token", CommonAppConfig.getInstance().getToken())
                .params("videoid", videoId)
                .params("random_str", s)
                .execute(CommonHttpUtil.NO_CALLBACK);
    }


    /**
     * 完整观看完视频后请求这个接口
     */
    public static void videoWatchEnd(String videoUid, String videoId) {
        if(!CommonAppConfig.getInstance().isLogin()){
            return;
        }
        String uid = CommonAppConfig.getInstance().getUid();
        if (TextUtils.isEmpty(uid) || uid.equals(videoUid)) {
            return;
        }
        VideoHttpUtil.cancel(VideoHttpConsts.VIDEO_WATCH_END);
        String s = MD5Util.getMD5(uid + "-" + videoId + "-" + VIDEO_SALT);
        HttpClient.getInstance().get("Video.setConversion", VideoHttpConsts.VIDEO_WATCH_END)
                .params("uid", uid)
                .params("token", CommonAppConfig.getInstance().getToken())
                .params("videoid", videoId)
                .params("random_str", s)
                .execute(CommonHttpUtil.NO_CALLBACK);
    }


    /**
     * 每日任务--开始观看视频
     */
    public static void startWatchVideo() {
        if(!CommonAppConfig.getInstance().isLogin()){
            return;
        }
        HttpClient.getInstance().get("Video.startWatchVideo", VideoHttpConsts.START_WATCH_VIDEO)
                .params("uid", CommonAppConfig.getInstance().getUid())
                .params("token", CommonAppConfig.getInstance().getToken())
                .execute(CommonHttpUtil.NO_CALLBACK);
    }

    /**
     * 每日任务--结束观看视频
     */
    public static void endWatchVideo() {
        HttpClient.getInstance().get("Video.endWatchVideo", VideoHttpConsts.END_WATCH_VIDEO)
                .params("uid", CommonAppConfig.getInstance().getUid())
                .params("token", CommonAppConfig.getInstance().getToken())
                .execute(CommonHttpUtil.NO_CALLBACK);
    }


    /**
     * 删除评论
     */
    public static void deleteComment(String videoId, String commentid, String commentUid, HttpCallback callback) {
        HttpClient.getInstance().get("Video.delComments", VideoHttpConsts.DELETE_COMMENT)
                .params("videoid", videoId)
                .params("commentid", commentid)
                .params("commentuid", commentUid)
                .params("uid", CommonAppConfig.getInstance().getUid())
                .params("token", CommonAppConfig.getInstance().getToken())
                .execute(callback);
    }


    /**
     * 获取@好友列表
     */
    public static void getAtFriendList( int p, HttpCallback callback) {
        HttpClient.getInstance().get("User.getFollowsList", VideoHttpConsts.GET_AT_FRIEND_LIST)
                .params("uid", CommonAppConfig.getInstance().getUid())
                .params("touid", CommonAppConfig.getInstance().getUid())
                .params("p", p)
                .execute(callback);
    }

    /**
     * 获取视频信息
     */
    public static void getVideoInfo(String videoId, HttpCallback callback) {
        HttpClient.getInstance().get("Video.getVideo", VideoHttpConsts.GET_VIDEO_INFO)
                .params("uid", CommonAppConfig.getInstance().getUid())
                .params("token", CommonAppConfig.getInstance().getToken())
                .params("videoid", videoId)
                .execute(callback);
    }



}




