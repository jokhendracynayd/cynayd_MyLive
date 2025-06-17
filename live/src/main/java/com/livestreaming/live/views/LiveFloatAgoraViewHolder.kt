package com.livestreaming.live.views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.widget.FrameLayout
import android.widget.ImageView
import com.alibaba.fastjson.JSON
import com.livestreaming.common.CommonAppConfig
import com.livestreaming.common.CommonAppContext
import com.livestreaming.common.utils.DpUtil
import com.livestreaming.common.utils.L
import com.livestreaming.common.utils.ToastUtil
import com.livestreaming.common.utils.WordUtil
import com.livestreaming.live.R
import com.livestreaming.live.activity.LiveAudienceActivity
import com.livestreaming.live.bean.LiveAudienceFloatWindowData
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig
import io.agora.rtc2.RtcEngineEx
import io.agora.rtc2.video.VideoCanvas
import jp.wasabeef.glide.transformations.BlurTransformation

/**
 * Created by http://www.yunbaokj.com on 2023/6/27.
 */
class LiveFloatAgoraViewHolder(context: Context?, parentView: ViewGroup?, vararg args: Any?) :
    LiveFloatViewHolder(context, parentView, *args) {
    override fun getLayoutId() = R.layout.view_live_float_agora

    private var myCoverImage: BitmapDrawable? = null
    private var mPlayContainer: View? = null
    private var mLiveView: TextureView? = null
    private var mOtherView: TextureView? = null
    private var cover: ImageView? = null
    private var mEngine: RtcEngineEx? = null
    private var mLiveData: LiveAudienceFloatWindowData? = null
    private val mHandler = Handler(Looper.getMainLooper())
    private var mAnchorWidth: Float = 0f
    private var mAnchorHeight: Float = 0f

    override fun init() {
        val onClick = View.OnClickListener {
            when (it.id) {
                R.id.btn_close -> {
                    stopPlay()
                    mActionListener?.onCloseClick()
                }
            }
        }
        findViewById<View>(R.id.btn_close).setOnClickListener(onClick)
        mPlayContainer = findViewById(R.id.play_container)
        mLiveView = findViewById<TextureView>(R.id.live_view)
        mOtherView = findViewById<TextureView>(R.id.other_view)
        cover = findViewById<ImageView>(R.id.cover)

        val engineEventHandler = object : IRtcEngineEventHandler() {
            override fun onVideoSizeChanged(
                source: Constants.VideoSourceType?,
                uid: Int,
                width: Int,
                height: Int,
                rotation: Int
            ) {
                L.e(
                    "LiveFloatAgoraViewHolder",
                    "IRtcEngineEventHandler---onVideoSizeChanged---source:$source uid: $uid width: $width height: $height rotation: $rotation"
                )
                if (uid == mLiveData!!.liveUid) {
                    if (rotation / 90 % 2 == 0) {
                        mAnchorWidth = width.toFloat()
                        mAnchorHeight = height.toFloat()
                    } else {
                        mAnchorWidth = height.toFloat()
                        mAnchorHeight = width.toFloat()
                    }
                    if (mLiveData!!.pkUid > 0 || mLiveData!!.linkMicAudienceUid > 0) {
                        if (mLiveData!!.pkUid > 0) {
                            onAnchorLinkMicChangeSize(true)
                        } else {
                            mHandler.post {
                                val vw = mAnchorWidth
                                val vh = mAnchorHeight
                                mPlayContainer?.apply {
                                    val lp = layoutParams
                                    if (vw < vh) {
                                        lp.width = DpUtil.dp2px(100)
                                        lp.height = (lp.width * vh / vw).toInt()
                                    } else {
                                        lp.height = DpUtil.dp2px(100)
                                        lp.width = (lp.height * vw / vh).toInt()
                                    }
                                    mActionListener?.onVideoSizeChanged(lp.width, lp.height)
                                    requestLayout()
                                }
                            }
                            onAudLinkMicChangeSize(true)
                        }
                    } else {
                        mHandler.post {
                            val vw = mAnchorWidth
                            val vh = mAnchorHeight
                            mPlayContainer?.apply {
                                val lp = layoutParams
                                if (vw < vh) {
                                    lp.width = DpUtil.dp2px(100)
                                    lp.height = (lp.width * vh / vw).toInt()
                                } else {
                                    lp.height = DpUtil.dp2px(100)
                                    lp.width = (lp.height * vw / vh).toInt()
                                }
                                mActionListener?.onVideoSizeChanged(lp.width, lp.height)
                                requestLayout()
                            }
                        }
                    }
                }
            }

            override fun onUserJoined(uid: Int, elapsed: Int) {
                L.e(
                    "LiveFloatAgoraViewHolder",
                    "IRtcEngineEventHandler---onUserJoined--->uid: $uid"
                )
                if (uid == mLiveData!!.liveUid) {
                    mHandler.post {
                        mEngine!!.setupRemoteVideo(
                            VideoCanvas(
                                mLiveView,
                                VideoCanvas.RENDER_MODE_HIDDEN,
                                uid
                            )
                        )
                    }
                } else {
                    mHandler.post {
                        mEngine!!.setupRemoteVideo(
                            VideoCanvas(
                                mOtherView,
                                VideoCanvas.RENDER_MODE_HIDDEN,
                                uid
                            )
                        )
                    }
                }
            }

            override fun onUserOffline(uid: Int, reason: Int) {
                L.e(
                    "LiveFloatAgoraViewHolder",
                    "IRtcEngineEventHandler---onUserOffline--->uid: $uid"
                )
                //handleCover(true)
                if (uid == mLiveData!!.liveUid) {
                    stopPlay()
                    mActionListener?.onCloseClick()
                } else {
                    mEngine!!.setupRemoteVideo(
                        VideoCanvas(
                            null,
                            VideoCanvas.RENDER_MODE_HIDDEN,
                            uid
                        )
                    )
                }
            }

            override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
                L.e(
                    "LiveFloatAgoraViewHolder",
                    "IRtcEngineEventHandler---onJoinChannelSuccess--->channel: $channel uid: $uid"
                )
            }

            override fun onError(err: Int) {
                L.e("LiveFloatAgoraViewHolder", "IRtcEngineEventHandler---onError--->$err")
            }

            override fun onLeaveChannel(stats: RtcStats?) {
                L.e("LiveFloatAgoraViewHolder", "IRtcEngineEventHandler---onLeaveChannel--->")
            }

            override fun onUserMuteVideo(uid: Int, muted: Boolean) {
                super.onUserMuteVideo(uid, muted)
                mHandler.post {
                    if (muted) {
                        handleCover(true)
                    } else {
                        cover?.visibility = View.INVISIBLE
                    }
                }
            }

            override fun onStreamMessage(uid: Int, streamId: Int, data: ByteArray?) {
                if (uid == mLiveData!!.liveUid && data != null) {
                    try {
                        val msg = String(data)
                        L.e(
                            "LiveFloatAgoraViewHolder",
                            "IRtcEngineEventHandler---onStreamMessage-->msg：$msg"
                        )
                        val obj = JSON.parseObject(msg)
                        val method = obj.getString("method")
                        when (method) {
                            "kick" -> {
                                val touid = obj.getString("touid")
                                if (touid == CommonAppConfig.getInstance().uid) {
                                    stopPlay()
                                    mActionListener?.onCloseClick()
                                    ToastUtil.show(WordUtil.getString(com.livestreaming.common.R.string.live_kicked_2))
                                }
                            }

                            "LiveConnect" -> {//主播连麦
                                val pkuid = obj.getIntValue("pkuid")
                                onAnchorLinkMicChangeSize(pkuid > 0)
                            }

                            "ConnectVideo" -> {
                                val uid = obj.getIntValue("uid")
                                onAudLinkMicChangeSize(uid > 0)
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
        val config = RtcEngineConfig().apply {
            mContext = CommonAppContext.getInstance()
            mAppId = CommonAppConfig.getInstance().getConfig().getAgoraAppId()
            mChannelProfile = Constants.CHANNEL_PROFILE_LIVE_BROADCASTING
            mAudioScenario = Constants.AUDIO_PROFILE_MUSIC_STANDARD
            mEventHandler = engineEventHandler
        }
        try {
            mEngine = (RtcEngine.create(config) as RtcEngineEx).apply {
                setClientRole(Constants.CLIENT_ROLE_AUDIENCE)
                enableVideo()
            }
        } catch (e: Exception) {
            mEngine = null
        }
    }

    /**
     * 观众与主播连麦的时候调整UI布局
     */
    private fun onAudLinkMicChangeSize(isLinkMic: Boolean) {
        mHandler.post {
            if (isLinkMic) {
                mOtherView?.apply {
                    visibility = View.VISIBLE
                    val vw = mAnchorWidth
                    val vh = mAnchorHeight
                    val lp = layoutParams as FrameLayout.LayoutParams
                    if (vw < vh) {
                        lp.width = DpUtil.dp2px(40)
                        lp.height = (lp.width * vh / vw).toInt()
                    } else {
                        lp.height = DpUtil.dp2px(40)
                        lp.width = (lp.height * vw / vh).toInt()
                    }
                    lp.gravity = Gravity.RIGHT or Gravity.BOTTOM
                    requestLayout()
                }
            } else {
                mOtherView?.visibility = View.GONE
            }
        }
    }

    /**
     * 主播与主播连麦的时候调整UI布局
     */
    private fun onAnchorLinkMicChangeSize(isLinkMic: Boolean) {
        mHandler.post {
            if (isLinkMic) {
                mPlayContainer?.apply {
                    val vw = mAnchorWidth
                    val vh = mAnchorHeight
                    val lp = layoutParams
                    if (vw < vh) {
                        val w = DpUtil.dp2px(100)
                        lp.width = w * 2
                        lp.height = (w * vh / vw).toInt()
                    } else {
                        lp.height = DpUtil.dp2px(100)
                        lp.width = (lp.height * vw / vh).toInt() * 2
                    }
                    mActionListener?.onVideoSizeChanged(lp.width, lp.height)
                    requestLayout()
                }
                mLiveView?.apply {
                    val vw = mAnchorWidth
                    val vh = mAnchorHeight
                    val lp = layoutParams as FrameLayout.LayoutParams
                    if (vw < vh) {
                        lp.width = DpUtil.dp2px(100)
                        lp.height = (lp.width * vh / vw).toInt()
                    } else {
                        lp.height = DpUtil.dp2px(100)
                        lp.width = (lp.height * vw / vh).toInt()
                    }
                    lp.gravity = Gravity.LEFT or Gravity.TOP
                    requestLayout()
                }
                mOtherView?.apply {
                    visibility = View.VISIBLE
                    val vw = mAnchorWidth
                    val vh = mAnchorHeight
                    val lp = layoutParams as FrameLayout.LayoutParams
                    if (vw < vh) {
                        lp.width = DpUtil.dp2px(100)
                        lp.height = (lp.width * vh / vw).toInt()
                    } else {
                        lp.height = DpUtil.dp2px(100)
                        lp.width = (lp.height * vw / vh).toInt()
                    }
                    lp.gravity = Gravity.RIGHT or Gravity.TOP
                    requestLayout()
                }
            } else {
                mOtherView?.visibility = View.GONE
                mPlayContainer?.apply {
                    val vw = mAnchorWidth
                    val vh = mAnchorHeight
                    val lp = layoutParams
                    if (vw < vh) {
                        lp.width = DpUtil.dp2px(100)
                        lp.height = (lp.width * vh / vw).toInt()
                    } else {
                        lp.height = DpUtil.dp2px(100)
                        lp.width = (lp.height * vw / vh).toInt()
                    }
                    mActionListener?.onVideoSizeChanged(lp.width, lp.height)
                    requestLayout()
                }
                mLiveView?.apply {
                    val lp = layoutParams
                    lp.width = LayoutParams.MATCH_PARENT
                    lp.height = LayoutParams.MATCH_PARENT
                    requestLayout()
                }
            }
        }
    }


    fun startPlayAgora(liveData: LiveAudienceFloatWindowData?) {
        L.e("LiveFloatAgoraViewHolder", "startPlayAgora--->liveData:$liveData")
        mLiveData = liveData
        if (mEngine != null && mLiveData != null) {
            L.e("LiveFloatAgoraViewHolder", "joinChannel--->")
            val option = ChannelMediaOptions().apply {
                channelProfile = Constants.CHANNEL_PROFILE_LIVE_BROADCASTING
                clientRoleType = Constants.CLIENT_ROLE_AUDIENCE
                autoSubscribeAudio = true
                autoSubscribeVideo = true
                publishMicrophoneTrack = false
                publishCameraTrack = true
            }
            mEngine!!.joinChannel(
                mLiveData!!.agoraToken,
                mLiveData!!.stream,
                CommonAppConfig.getInstance().uid.toInt(),
                option
            )
        }
    }

    private fun handleCover(muted: Boolean) {
        mLiveView?.apply {
            val lp = layoutParams
            lp.width = LayoutParams.MATCH_PARENT
            lp.height = LayoutParams.MATCH_PARENT
            requestLayout()
        }
        mPlayContainer?.visibility = View.VISIBLE
        cover!!.visibility = View.VISIBLE
        if (LiveAudienceActivity.liveThumb != null) {
            Glide.with(contentView.context).asBitmap()
                .load(LiveAudienceActivity.liveThumb)
                .into(object : CustomTarget<Bitmap?>() {

                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap?>?
                    ) {
                        myCoverImage = BitmapDrawable(
                            cover!!.context.resources, resource
                        )
                        cover?.setImageBitmap(resource)
                        Glide.with(cover!!.context)
                            .load(myCoverImage) // Load the BitmapDrawable
                            .apply(
                                RequestOptions.bitmapTransform(
                                    BlurTransformation(
                                        25,
                                        3
                                    )
                                )
                            ) // Apply blur transformation
                            .into(cover!!)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                    }
                })
        }

    }


    override fun setMute(mute: Boolean) {
        mEngine?.muteAllRemoteAudioStreams(mute)
    }


    override fun stopPlay() {
        mEngine?.apply {
            leaveChannel()
            mHandler.post { RtcEngine.destroy() }
        }
        mEngine = null
    }
}