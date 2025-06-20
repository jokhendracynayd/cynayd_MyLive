package com.livestreaming.common

enum class ShareApplication(val value: String, val packageName: String) {
    WHATSAPP(value = Constants.MOB_WHATSAPP,"com.whatsapp"),
    FACEBOOK(value = Constants.MOB_FACEBOOK, "com.facebook.katana"),
    TWITTER(value = Constants.MOB_TWITTER, "com.twitter.android");

    companion object {
        fun getFromValue(value: String) : ShareApplication? {
            return ShareApplication.entries.firstOrNull{ it.value == value }
        }
    }
}