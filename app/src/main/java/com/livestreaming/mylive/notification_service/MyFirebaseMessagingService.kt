package com.livestreaming.mylive.notification_service;

import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.alibaba.fastjson.JSONObject
import com.livestreaming.common.http.HttpCallback
import com.livestreaming.live.bean.LiveBean
import com.livestreaming.main.dialog.TopBoxDialog
import com.livestreaming.main.http.MainHttpUtil
import com.livestreaming.mylive.activity.LauncherActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.livestreaming.common.R


class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        MainHttpUtil.sendAndSaveFirebaseToken(token, object : HttpCallback() {
            override fun onSuccess(code: Int, msg: String, info: Array<String>) {
            }
        })
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        handleNotificationMsg(remoteMessage)
    }

    private fun handleNotificationMsg(remoteMessage: RemoteMessage) {
        val prefs =
            applicationContext.getSharedPreferences("notification_settings", MODE_PRIVATE)
        val isBoxNotificationEnabled = prefs.getInt("box_notif", 1)
        val isBackNotificationEnabled = prefs.getInt("back_notif", 1)
        if (remoteMessage.data.isNotEmpty()) {

            val activity = AppLifecycleTracker.currentActivity
            if (activity != null && !activity.isFinishing && !activity.isDestroyed) {
                if (remoteMessage.data.containsKey("type")) {
                    var type: Int? = remoteMessage.data.get("type")?.toInt()
                    if (type != null) {
                        if (type == 1) {
                            if (isBoxNotificationEnabled == 1) {
                                Handler(Looper.getMainLooper()).post {
                                    var name = remoteMessage.data.get("nickname").toString()
                                    var room_name: LiveBean = Gson().fromJson(
                                        remoteMessage.data.get("frame").toString(),
                                        LiveBean::class.java
                                    )
                                    var avatar = remoteMessage.data.get("avatar").toString()
                                    var box_value = remoteMessage.data.get("box_value").toString()
                                    TopBoxDialog(
                                        activity
                                    ).showDialog(
                                        name + " " + activity.getString(com.livestreaming.common.R.string.send_box_in) + " " + room_name.title + " " + activity.getString(
                                            com.livestreaming.common.R.string.with_value
                                        ) + box_value, avatar, room_name
                                    )
                                }
                            }
                        } else if (type == 10) {
                            if (isBackNotificationEnabled == 1) {
                                Log.e("testnotf", Gson().toJson(remoteMessage.data))
                                var name = remoteMessage.data.get("name").toString()
                                var img = remoteMessage.data.get("img").toString()
                                var id = remoteMessage.data.get("uid").toString()
                                showFollowNotif(name, img, id)
                            }
                        } else {
                            if (isBackNotificationEnabled == 1) {
                                showNotif(remoteMessage)
                            }
                        }
                    }
                }
            } else {
                if (remoteMessage.data.containsKey("type")) {
                    var type: Int? = remoteMessage.data.get("type")?.toInt()
                    if (type != null) {
                        if (type == 1) {
                            if (isBoxNotificationEnabled == 1) {
                                var name = remoteMessage.data["nickname"]
                                var img = remoteMessage.data["avatar"]
                                var boxValue = remoteMessage.data["box_value"]
                                var roomBeanJson = remoteMessage.data["frame"]
                                showBoxNotification(name, img, boxValue, roomBeanJson)
                            }
                        } else
                            if (type == 10) {
                                if (isBackNotificationEnabled == 1) {
                                    Log.e("testnotf", Gson().toJson(remoteMessage.data))
                                    var name = remoteMessage.data.get("name").toString()
                                    var img = remoteMessage.data.get("img").toString()
                                    var id = remoteMessage.data.get("uid").toString()
                                    showFollowNotif(name, img, id)
                                }
                            } else if (type == 11) {
                                Log.e("testnotf", Gson().toJson(remoteMessage.data))
                                var data = remoteMessage.data.get("room_data").toString()
                                showLiveNotification(JSONObject.parseObject(data, LiveBean::class.java))
                            } else {
                                if (isBackNotificationEnabled == 1) {
                                    showNotif(remoteMessage)
                                }
                            }
                    }
                } else if (isBackNotificationEnabled == 1) {
                    showNotif(remoteMessage)
                }
            }
        } else {
            if (isBackNotificationEnabled == 1) {
                if (remoteMessage.notification != null) {
                    val title = remoteMessage.notification!!.title
                    if (title != null) {
                        NotificationUtils.sendNotification(
                            this, title,
                            remoteMessage.notification!!.body, Intent(
                                this,
                                LauncherActivity::class.java
                            )
                        )
                    }
                }
            }
        }
    }

    private fun showLiveNotification(fromJson: LiveBean?) {
        NotificationUtils.sendNotification(
            this,
            getString(R.string.live),
            fromJson?.userNiceName + " " + getString(R.string.live_start),
            fromJson?.avatar ?: "",
            getIntent(
                mapOf<String, String>(Pair("room", Gson().toJson(fromJson)), Pair("type", "11"))
            )
        )
    }

    private fun showBoxNotification(
        name: String?,
        img: String?,
        boxValue: String?,
        roomBeanJson: String?
    ) {
        NotificationUtils.sendNotification(
            this,
            getString(R.string.box_gift),
            name + " " + getString(R.string.send_box_in) + " " + JSONObject.parseObject(
                roomBeanJson
            ).getString("userNiceName") + " " + getString(
                R.string.with_value
            ) + " " + boxValue,
            img,
            getIntent(
                mapOf<String, String>(Pair("room", roomBeanJson ?: ""), Pair("type", "1"))
            )
        )

    }

    private fun showFollowNotif(name: String, img: String, id: String) {
        NotificationUtils.sendNotification(
            this,
            getString(R.string.follow),
            name + " " + getString(R.string.follow_you),
            img,
            getIntent(
                mapOf<String, String>(Pair("uid", id), Pair("type", "10"))
            )
        )
    }

    private fun showNotif(remoteMessage: RemoteMessage) {
        NotificationUtils.sendNotification(
            this,
            remoteMessage.data["title"],
            remoteMessage.data["body"], getIntent(remoteMessage.data)
        )
    }

    private fun getIntent(dataObject: Map<String, String>): Intent {
        val intent = Intent(this, LauncherActivity::class.java)
        intent.putExtra("notification_object", Gson().toJson(dataObject))
        return intent
    }

}
