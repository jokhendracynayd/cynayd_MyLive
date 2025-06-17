package com.livestreaming.live.interfaces;

import androidx.annotation.Nullable;

import com.livestreaming.live.bean.LiveUserGiftBean;

import java.util.List;

public interface OnListChangedListener {
   public void onUserListChanged(@Nullable List<LiveUserGiftBean> list,@Nullable  String pkObjString);

    void onMicVotesChanged(String micListVotes);
}
