package com.livestreaming.live.dialog

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.fastjson.JSON
import com.livestreaming.common.Constants
import com.livestreaming.common.adapter.RefreshAdapter
import com.livestreaming.common.custom.CommonRefreshView
import com.livestreaming.common.custom.CommonRefreshView.DataHelper
import com.livestreaming.common.dialog.AbsDialogFragment
import com.livestreaming.common.http.HttpCallback
import com.livestreaming.common.interfaces.OnItemClickListener
import com.livestreaming.common.utils.ScreenDimenUtil
import com.livestreaming.live.R
import com.livestreaming.live.activity.LiveActivity
import com.livestreaming.live.adapter.LivePkUserDialogAdapter
import com.livestreaming.live.bean.LiveUserGiftBean
import com.livestreaming.live.http.LiveHttpUtil

/**
 * Created by http://www.yunbaokj.com on 2023/6/29.
 */
class LivePkUserListDialog : AbsDialogFragment(), OnItemClickListener<LiveUserGiftBean> {
    override fun getLayoutId() = R.layout.pk_contriputors_list_dialog

    override fun getDialogStyle() = com.livestreaming.common.R.style.dialog

    override fun canCancel() = true

    private var isRight=false

    override fun setWindowAttributes(window: Window?) {
        window?.apply {
            setWindowAnimations(com.livestreaming.common.R.style.bottomToTopAnim)
            attributes.apply {
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = (ScreenDimenUtil.getInstance().screenHeight * 0.6f).toInt()
                gravity = Gravity.BOTTOM
            }
        }
    }

    private var mAdapter: LivePkUserDialogAdapter? = null
    private var mLiveUid: String? = null

    private val colorLeft = Color.parseColor("#EB0090FF")
    private val colorRight = Color.parseColor("#FF3A89")

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        arguments?.apply {
            mLiveUid = getString(Constants.LIVE_UID)
            isRight=getInt(Constants.IS_RIGHT)==1
        }
        findViewById<View>(R.id.btn_close).setOnClickListener {
            dismiss()
        }
        if(isRight){
            findViewById<TextView>(R.id.contributors_rank_title).setTextColor(colorRight)
            findViewById<ConstraintLayout>(R.id.root).setBackgroundResource(R.drawable.bk_back_right)
        }else{
            findViewById<TextView>(R.id.contributors_rank_title).setTextColor(colorLeft)
            findViewById<ConstraintLayout>(R.id.root).setBackgroundResource(R.drawable.bk_back_left)
        }
        findViewById<CommonRefreshView>(R.id.refreshView).apply {
            setLayoutManager(LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false))
            setDataHelper(object : DataHelper<LiveUserGiftBean> {
                override fun getAdapter(): RefreshAdapter<LiveUserGiftBean> {
                    return mAdapter ?: LivePkUserDialogAdapter(mContext,isRight).also {
                        mAdapter = it
                        it.setOnItemClickListener(this@LivePkUserListDialog)
                    }
                }

                override fun loadData(p: Int, callback: HttpCallback) {
                    LiveHttpUtil.getLivePKContributorList(mLiveUid, callback)
                }

                override fun processData(info: Array<out String>): MutableList<LiveUserGiftBean> {
                    return JSON.parseArray(info.contentToString(), LiveUserGiftBean::class.java)
                }

                override fun onRefreshFailure() {
                }

                override fun onLoadMoreFailure() {
                }

                override fun onLoadMoreSuccess(
                    loadItemList: MutableList<LiveUserGiftBean>?,
                    loadItemCount: Int
                ) {
                }

                override fun onRefreshSuccess(
                    list: MutableList<LiveUserGiftBean>?,
                    listCount: Int
                ) {
                }

            })
            initData()
        }
    }

    override fun onItemClick(bean: LiveUserGiftBean?, position: Int) {
        (mContext as? LiveActivity)?.showUserDialog(bean?.id)
    }


}