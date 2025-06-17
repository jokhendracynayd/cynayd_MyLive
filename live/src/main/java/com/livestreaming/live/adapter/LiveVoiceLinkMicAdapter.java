package com.livestreaming.live.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.livestreaming.common.Constants;
import com.livestreaming.common.adapter.RefreshAdapter;
import com.livestreaming.common.glide.ImgLoader;
import com.livestreaming.common.utils.WordUtil;
import com.livestreaming.live.R;
import com.livestreaming.live.activity.LiveActivity;
import com.livestreaming.live.bean.LiveVoiceLinkMicBean;
import com.livestreaming.live.utils.LiveIconUtil;

import java.util.List;

public class LiveVoiceLinkMicAdapter extends RefreshAdapter<LiveVoiceLinkMicBean> {

    private String mNoString;
    private int mColor0;
    private int mColor1;
    private Drawable mDrawable0;
    private Drawable mDrawable1;
    private View.OnClickListener mOnClickListener;


    public LiveVoiceLinkMicAdapter(Context context, List<LiveVoiceLinkMicBean> list) {
        super(context, list);
        mNoString = WordUtil.getString(com.livestreaming.common.R.string.a_38);
        mColor0 = ContextCompat.getColor(context, com.livestreaming.common.R.color.gray3);
        mColor1 = ContextCompat.getColor(context, com.livestreaming.common.R.color.white);
        mDrawable0 = ContextCompat.getDrawable(context, R.mipmap.ic_live_voice_0);
        mDrawable1 = ContextCompat.getDrawable(context, R.mipmap.ic_live_voice_1);
        mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LiveVoiceLinkMicBean bean = (LiveVoiceLinkMicBean) v.getTag();
                if (!bean.isEmpty()) {
                    ((LiveActivity) mContext).showUserDialog(bean.getUid());
                }
            }
        };
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new Vh(mInflater.inflate(R.layout.item_live_voice_link_mic, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder vh, int i) {
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder vh, int position, @NonNull List payloads) {
        Object payload = payloads.size() > 0 ? payloads.get(0) : null;
        ((Vh) vh).setData(mList.get(position), payload);
    }

    class Vh extends RecyclerView.ViewHolder {

        ImageView mImgStatus;
        ImageView mAvatar;
        ImageView mFrame;

        TextView mName;
        TextView votes;
        ImageView mImgMute;
        ImageView mFace;
        ImageView ivSpeak;
        LinearLayout votes_layout;

        public Vh(@NonNull View itemView) {
            super(itemView);
            mImgStatus = itemView.findViewById(R.id.img_status);
            ivSpeak = itemView.findViewById(R.id.speak_iv);
            mAvatar = itemView.findViewById(R.id.avatar);
            mFrame = itemView.findViewById(R.id.svga);
            mName = itemView.findViewById(R.id.name);
            mImgMute = itemView.findViewById(R.id.img_mute);
            mFace = itemView.findViewById(R.id.face);
            votes = itemView.findViewById(R.id.votes);
            votes_layout = itemView.findViewById(R.id.votes_layout);
            itemView.setOnClickListener(mOnClickListener);
        }

        void setData(LiveVoiceLinkMicBean bean, Object payload) {
            if (bean == null) {
                return;
            }
            if (payload == "room_votes") {
                if (bean.getVotes() != null && !bean.getVotes().isEmpty() && !bean.getVotes().equals("0")) {
                    votes_layout.setVisibility(View.VISIBLE);
                    votes.setText(handleVotes(bean.getVotes()));
                } else {
                    votes_layout.setVisibility(View.GONE);
                }
            }
            if (Constants.VOICE_FACE.equals(payload)) {
                if (bean.getFaceIndex() == -1) {
                    mFace.setImageDrawable(null);
                } else {
                    int res = LiveIconUtil.getVoiceRoomFaceRes(bean.getFaceIndex());
                    if (res > 0) {
                        mFace.setImageResource(res);
                    } else {
                        mFace.setImageDrawable(null);
                    }
                }
                return;
            }
            if (payload == "speak") {
                if (bean.speak) {
                    if (ivSpeak.getVisibility() != View.VISIBLE) {
                        ivSpeak.setVisibility(View.VISIBLE);
                    }
                } else {
                    if (ivSpeak.getVisibility() != View.INVISIBLE) {
                        ivSpeak.setVisibility(View.INVISIBLE);
                    }
                }
                return;
            }
            if (bean.isEmpty()) {
                if (payload == null) {
                    itemView.setTag(bean);
                    mAvatar.setImageDrawable(null);
                    mFrame.setImageDrawable(null);
                    mName.setText(mNoString);
                    if (bean.getVotes() != null && !bean.getVotes().isEmpty() && !bean.getVotes().equals("0")) {
                        votes_layout.setVisibility(View.VISIBLE);
                        votes.setText(handleVotes(bean.getVotes()));
                    } else {
                        votes_layout.setVisibility(View.GONE);
                    }
                    mName.setTextColor(mColor0);
                    mFace.setImageDrawable(null);
                }
                if (bean.getStatus() == Constants.VOICE_CTRL_BAN) {
                    mImgStatus.setImageDrawable(mDrawable1);
                } else {
                    mImgStatus.setImageDrawable(mDrawable0);
                }
                if (mImgMute.getVisibility() == View.VISIBLE) {
                    mImgMute.setVisibility(View.INVISIBLE);
                }
            } else {
                if (payload == null) {
                    itemView.setTag(bean);
                    ImgLoader.displayAvatar(mContext, bean.getAvatar(), mAvatar);
                    if (bean.getFrame() != null) {
                        ImgLoader.loadSvga(mContext, bean.getFrame(), mFrame);
                    } else {
                        mFrame.setImageDrawable(null);
                    }
                    mName.setText(bean.getUserName());
                    if (bean.getVotes() != null && !bean.getVotes().isEmpty() && !bean.getVotes().equals("0")) {
                        votes_layout.setVisibility(View.VISIBLE);
                        votes.setText(handleVotes(bean.getVotes()));
                    } else {
                        votes_layout.setVisibility(View.GONE);
                    }
                    mName.setTextColor(mColor1);
                    mFace.setImageDrawable(null);
                }
                mImgStatus.setImageDrawable(mDrawable0);
                if (bean.getStatus() == Constants.VOICE_CTRL_CLOSE) {
                    if (mImgMute.getVisibility() != View.VISIBLE) {
                        mImgMute.setVisibility(View.VISIBLE);
                    }
                } else if (bean.getStatus() == Constants.VOICE_CTRL_OPEN) {
                    if (mImgMute.getVisibility() == View.VISIBLE) {
                        mImgMute.setVisibility(View.INVISIBLE);
                    }
                }
            }

        }

        private String handleVotes(String votes) {
            try {
                int votes_ = Integer.parseInt(votes);
                if (votes_ >= 1000 && votes_ < 1000000) {
                    return "" + votes_ / 1000 + "K";
                } else if (votes_ >= 1000000) {
                    return "" + votes_ / 1000000 + "M";
                } else {
                    return "" + votes_;
                }

            } catch (Exception e) {
                return "0";
            }
        }
    }
}
