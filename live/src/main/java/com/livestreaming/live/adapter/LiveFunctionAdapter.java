package com.livestreaming.live.adapter;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.livestreaming.common.CommonAppConfig;
import com.livestreaming.common.Constants;
import com.livestreaming.im.utils.ImMessageUtil;
import com.livestreaming.im.utils.ImUnReadCount;
import com.livestreaming.live.R;
import com.livestreaming.live.activity.LiveActivity;
import com.livestreaming.live.activity.LiveAnchorActivity;
import com.livestreaming.live.bean.LiveFunctionBean;
import com.livestreaming.common.interfaces.OnItemClickListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cxf on 2018/10/9.
 */

public class LiveFunctionAdapter extends RecyclerView.Adapter<LiveFunctionAdapter.Vh> {

    private List<LiveFunctionBean> mList;
    private LayoutInflater mInflater;
    private View.OnClickListener mOnClickListener;
    private OnItemClickListener<Integer> mOnItemClickListener;
    private String mImUnReadCount;

    public LiveFunctionAdapter(Context context, boolean hasGame, boolean openFlash, boolean taskSwitch, boolean luckPanSwitch, boolean hasFace, boolean screenRecord, boolean isLinkMic, int isMicOpen) {
        mList = new ArrayList<>();
        if (((LiveActivity) context).isChatRoom()) {
            if (context instanceof LiveAnchorActivity) {
                //mList.add(new LiveFunctionBean(Constants.LIVE_FUNC_MUSIC, R.mipmap.icon_live_func_music, R.string.live_music));
                mList.add(new LiveFunctionBean(Constants.LIVE_FUNC_SHARE, R.mipmap.icon_live_func_share, com.livestreaming.common.R.string.live_share));
//                if(CommonAppConfig.getInstance().getUserBean().getVip().getType()!=0) {
//                  }
                //amit123
//                mList.add(new LiveFunctionBean(Constants.LIVE_MP3_PLAYER, R.mipmap.icon_live_func_music, com.livestreaming.common.R.string.music));

                mList.add(new LiveFunctionBean(Constants.LIVE_FUNC_RED_PACK, R.mipmap.icon_live_func_rp, com.livestreaming.common.R.string.live_red_pack));

                if (taskSwitch) {
                    mList.add(new LiveFunctionBean(Constants.LIVE_FUNC_TASK, R.mipmap.icon_live_func_task, com.livestreaming.common.R.string.daily_task));
                }
                mList.add(new LiveFunctionBean(Constants.ChangeRoomBack, R.drawable.ic_change_bg, com.livestreaming.common.R.string.change_background));

//                mList.add(new LiveFunctionBean(Constants.LIVE_FUNC_LUCK, R.mipmap.icon_live_func_luck, R.string.a_002));

            } else {
                mList.add(new LiveFunctionBean(Constants.LIVE_FUNC_SHARE, R.mipmap.icon_live_func_share, com.livestreaming.common.R.string.live_share));
                if (!CommonAppConfig.getInstance().isTeenagerType()) {
                    if (luckPanSwitch) {
                        mList.add(new LiveFunctionBean(Constants.LIVE_FUNC_PAN, R.mipmap.icon_live_luck_pan, com.livestreaming.common.R.string.a_003));
                    }
//                    if(CommonAppConfig.getInstance().getUserBean().getVip().getType()!=0) {
//                       }
                    mList.add(new LiveFunctionBean(Constants.LIVE_FUNC_RED_PACK, R.mipmap.icon_live_func_rp, com.livestreaming.common.R.string.live_red_pack));

                    if (taskSwitch) {
                        mList.add(new LiveFunctionBean(Constants.LIVE_FUNC_TASK, R.mipmap.icon_live_func_task, com.livestreaming.common.R.string.daily_task));
                    }
                }
                if (hasFace) {
                    mList.add(new LiveFunctionBean(Constants.LIVE_FUNC_FACE, R.mipmap.icon_live_voice_face, com.livestreaming.common.R.string.表情));
                }
              //  mList.add(new LiveFunctionBean(Constants.LIVE_FUNC_MSG, R.mipmap.icon_live_func_msg, com.livestreaming.common.R.string.pri_msg));
                mList.add(new LiveFunctionBean(Constants.Mall, R.drawable.ic_malll, com.livestreaming.common.R.string.main_mall));
                mList.add(new LiveFunctionBean(Constants.BackBag, R.drawable.ic_backbag, com.livestreaming.common.R.string.video_music_favorite));

//                mList.add(new LiveFunctionBean(Constants.LIVE_FUNC_LUCK, R.mipmap.icon_live_func_luck, R.string.a_002));
            }
        } else {
            if (context instanceof LiveAnchorActivity) {
                if (!screenRecord) {
                    mList.add(new LiveFunctionBean(Constants.LIVE_FUNC_BEAUTY, R.mipmap.icon_live_func_beauty, com.livestreaming.common.R.string.live_beauty));
                    mList.add(new LiveFunctionBean(Constants.LIVE_FUNC_CAMERA, R.mipmap.icon_live_func_camera, com.livestreaming.common.R.string.live_camera));
                    mList.add(new LiveFunctionBean(Constants.LIVE_FUNC_FLASH, openFlash ? R.mipmap.icon_live_func_flash : R.mipmap.icon_live_func_flash_1, com.livestreaming.common.R.string.live_flash));
                }
//                mList.add(new LiveFunctionBean(Constants.LIVE_FUNC_MUSIC, R.mipmap.icon_live_func_music, com.livestreaming.common.R.string.live_music));
                mList.add(new LiveFunctionBean(Constants.LIVE_MP3_PLAYER, R.mipmap.icon_live_func_music, com.livestreaming.common.R.string.music));
                mList.add(new LiveFunctionBean(Constants.LIVE_FUNC_SHARE, R.mipmap.icon_live_func_share, com.livestreaming.common.R.string.live_share));
                mList.add(new LiveFunctionBean(Constants.LIVE_FUNC_LINK_MIC_AUD, isLinkMic ? R.mipmap.icon_live_func_linkmic_anc_1
                        : R.mipmap.icon_live_func_linkmic_anc_0, com.livestreaming.common.R.string.用户连麦));

//                if (hasGame) {//含有游戏
//                    mList.add(new LiveFunctionBean(Constants.LIVE_FUNC_GAME, R.mipmap.icon_live_func_game, com.livestreaming.common.R.string.live_game));
//                }
//                if(CommonAppConfig.getInstance().getUserBean().getVip().getType()!=0) {
//                    }
                mList.add(new LiveFunctionBean(Constants.LIVE_FUNC_RED_PACK, R.mipmap.icon_live_func_rp, com.livestreaming.common.R.string.live_red_pack));

                //mList.add(new LiveFunctionBean(Constants.LIVE_FUNC_LINK_MIC, R.drawable.pk_set, com.livestreaming.common.R.string.live_pk_invite));
                if (!screenRecord) {
                    mList.add(new LiveFunctionBean(Constants.LIVE_FUNC_MIRROR, R.mipmap.icon_live_func_jx, com.livestreaming.common.R.string.live_mirror));
                }
                if (isMicOpen == 1) {
                    mList.add(new LiveFunctionBean(Constants.LIVE_MIC_CON, R.mipmap.icon_live_mic_open, com.livestreaming.common.R.string.open));
                } else if (isMicOpen == 0) {
                    mList.add(new LiveFunctionBean(Constants.LIVE_MIC_CON, R.mipmap.icon_live_mic_close, com.livestreaming.common.R.string.close));
                }

            } else {
             //   mList.add(new LiveFunctionBean(Constants.LIVE_FUNC_MSG, R.mipmap.icon_live_func_msg, com.livestreaming.common.R.string.pri_msg));
                mList.add(new LiveFunctionBean(Constants.LIVE_FUNC_SHARE, R.mipmap.icon_live_func_share, com.livestreaming.common.R.string.live_share));
                if(CommonAppConfig.getInstance().getUserBean().getVip().getType()!=0) {
                  }
                mList.add(new LiveFunctionBean(Constants.LIVE_FUNC_RED_PACK, R.mipmap.icon_live_func_rp, com.livestreaming.common.R.string.live_red_pack));

//                mList.add(new LiveFunctionBean(Constants.LIVE_FUNC_LINK_MIC_AUD, R.mipmap.icon_live_func_linkmic_aud,
//                        com.livestreaming.common.Constants.isMeInLinkMic ? com.livestreaming.common.R.string.live_link_mic_close : com.livestreaming.common.R.string.live_link_mic));
                if (com.livestreaming.common.Constants.isMeInLinkMic) {
                    if (com.livestreaming.common.Constants.isMyMicOpen) {
                        mList.add(new LiveFunctionBean(Constants.CLOSE_MY_MIC, R.mipmap.icon_live_mic_open, com.livestreaming.common.R.string.open));
                    } else {
                        mList.add(new LiveFunctionBean(Constants.OPEN_MY_MIC, R.mipmap.icon_live_mic_close, com.livestreaming.common.R.string.close));
                    }
                    if (com.livestreaming.common.Constants.isMyCamOpen) {
                        mList.add(new LiveFunctionBean(Constants.CLOSE_MY_CAM, com.livestreaming.video.R.mipmap.icon_video_camera, com.livestreaming.common.R.string.open));
                    } else {
                        mList.add(new LiveFunctionBean(Constants.OPEN_MY_CAM, com.livestreaming.video.R.mipmap.icon_video_camera_close, com.livestreaming.common.R.string.close));
                    }
                }
                mList.add(new LiveFunctionBean(Constants.Mall, R.drawable.ic_malll, com.livestreaming.common.R.string.main_mall));
                mList.add(new LiveFunctionBean(Constants.BackBag, R.drawable.ic_backbag, com.livestreaming.common.R.string.video_music_favorite));
            }

        }
        mInflater = LayoutInflater.from(context);
        mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Object tag = v.getTag();
                if (tag != null) {
                    int functionID = (int) tag;
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(functionID, 0);
                    }
                }
            }
        };
        ImUnReadCount unReadCount = ImMessageUtil.getInstance().getUnReadMsgCount();
        mImUnReadCount = unReadCount != null ? unReadCount.getLiveRoomUnReadCount() : "0";
    }

    public void setOnItemClickListener(OnItemClickListener<Integer> onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    @Override
    public int getItemViewType(int position) {
        return mList.get(position).getID();
    }

    @NonNull
    @Override
    public Vh onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == Constants.LIVE_FUNC_MSG) {
            return new MsgVh(mInflater.inflate(R.layout.item_live_function_msg, parent, false));
        }
        return new Vh(mInflater.inflate(R.layout.item_live_function, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Vh vh, int position, @NonNull List<Object> payloads) {
        Object payload = payloads.size() > 0 ? payloads.get(0) : null;
        vh.setData(mList.get(position), payload);
    }

    @Override
    public void onBindViewHolder(@NonNull Vh vh, int position) {

    }

    public void updateImUnReadCount(String imUnReadCount) {
        mImUnReadCount = imUnReadCount;
        for (int i = 0; i < mList.size(); i++) {
            if (mList.get(i).getID() == Constants.LIVE_FUNC_MSG) {
                notifyItemChanged(i, Constants.PAYLOAD);
                break;
            }
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    class Vh extends RecyclerView.ViewHolder {

        ImageView mIcon;
        TextView mName;

        public Vh(View itemView) {
            super(itemView);
            mIcon = (ImageView) itemView.findViewById(R.id.icon);
            mName = (TextView) itemView.findViewById(R.id.name);
            itemView.setOnClickListener(mOnClickListener);
        }

        void setData(LiveFunctionBean bean, Object payload) {
            if (payload == null) {
                itemView.setTag(bean.getID());
                mIcon.setImageResource(bean.getIcon());
                mName.setText(bean.getName());
            }
        }
    }

    class MsgVh extends Vh {

        TextView mRedPoint;

        public MsgVh(View itemView) {
            super(itemView);
            mRedPoint = itemView.findViewById(R.id.red_point);
        }

        void setData(LiveFunctionBean bean, Object payload) {
            super.setData(bean, payload);
            if (mRedPoint != null) {
                if ("0".equals(mImUnReadCount)) {
                    if (mRedPoint.getVisibility() == View.VISIBLE) {
                        mRedPoint.setVisibility(View.INVISIBLE);
                    }
                } else {
                    if (mRedPoint.getVisibility() != View.VISIBLE) {
                        mRedPoint.setVisibility(View.VISIBLE);
                    }
                }
                mRedPoint.setText(mImUnReadCount);
            }
        }
    }
}
