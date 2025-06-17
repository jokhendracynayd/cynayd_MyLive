package com.livestreaming.live.adapter

import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.livestreaming.common.adapter.RefreshAdapter
import com.livestreaming.common.glide.ImgLoader
import com.livestreaming.live.R
import com.livestreaming.live.bean.LiveUserGiftBean

/**
 * Created by http://www.yunbaokj.com on 2023/6/29.
 */
class LivePkUserDialogAdapter(
    context: Context,val isRight: Boolean =false
) : RefreshAdapter<LiveUserGiftBean>(context) {

    private val mOnItemClick = View.OnClickListener {
        (it.tag as? LiveUserGiftBean)?.let {
            mOnItemClickListener?.onItemClick(it, 0)
        }
    }

    private val colorLeft = Color.parseColor("#EB0090FF")
    private val colorRight = Color.parseColor("#FF3A89")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return Vh(mInflater.inflate(R.layout.item_pk_contriutor_item, parent, false))
    }

    override fun onBindViewHolder(vh: RecyclerView.ViewHolder, position: Int) {
        (vh as Vh).setData(mList[position], position)
    }

    private inner class Vh(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        val mNum: TextView
        val mAvatar: ImageView
        val mName: TextView
        val contribution: TextView


        init {
            itemView.setOnClickListener(mOnItemClick)
            mNum = itemView.findViewById(R.id.num)
            mAvatar = itemView.findViewById(R.id.avatar)
            mName = itemView.findViewById(R.id.name)
            contribution = itemView.findViewById(R.id.contribution)
        }

        fun setData(bean: LiveUserGiftBean, position: Int) {
            itemView.setTag(bean)
            mNum.text = (position + 1).toString()
            contribution.text = ""+calcContriburion(Integer.parseInt(bean.contribution));
            if(isRight){
                mNum.setTextColor(colorRight)
                contribution.setTextColor(colorRight)
            }else{
                mNum.setTextColor(colorLeft)
                contribution.setTextColor(colorLeft)
            }
            ImgLoader.displayAvatar(mContext, bean.avatar, mAvatar)
            mName.text = bean.userNiceName
        }
    }

    private fun calcContriburion(contribution: Int): String {
        return if(contribution in 1000..99998){
            "${(contribution/1000)}K"
        }else if(contribution>99999){
            "${(contribution/1000000)}M"
        }else{
            "$contribution"
        }
    }
}