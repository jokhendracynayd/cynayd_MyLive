package com.livestreaming.live.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.livestreaming.common.Constants;
import com.livestreaming.common.adapter.RefreshAdapter;
import com.livestreaming.live.R;
import com.livestreaming.live.activity.LiveActivity;
import com.livestreaming.live.bean.LiveVoiceLinkMicBean;
import com.livestreaming.live.utils.LiveIconUtil;
import com.bumptech.glide.Glide;

import java.util.List;

public class LiveVideoLinkMicAdapter extends RefreshAdapter<LiveVoiceLinkMicBean> {

    private Drawable mDrawable0;
    private Drawable mDrawable1;
    private View.OnClickListener mOnClickListener;


    public LiveVideoLinkMicAdapter(Context context, List<LiveVoiceLinkMicBean> list) {
        super(context, list);
        mDrawable0 = ContextCompat.getDrawable(context, R.mipmap.ic_live_voice_4);
        mDrawable1 = ContextCompat.getDrawable(context, R.mipmap.ic_live_voice_5);
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
        return new Vh(mInflater.inflate(R.layout.item_live_video_link_mic, viewGroup, false));
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

        View mViewEmpty;
        TextView mName;
        ImageView mImgMute;
        ImageView mFace;
        ImageView cover_avatar;
        private TextView votes;
        private LinearLayout votes_layout;

        public Vh(@NonNull View itemView) {
            super(itemView);
            mViewEmpty = itemView.findViewById(R.id.view_empty);
            mName = itemView.findViewById(R.id.name);
            mImgMute = itemView.findViewById(R.id.img_mute);
            mFace = itemView.findViewById(R.id.face);
            votes = itemView.findViewById(R.id.votes);
            cover_avatar = itemView.findViewById(R.id.cover_avatar);
            votes_layout = itemView.findViewById(R.id.votes_layout);
            itemView.setOnClickListener(mOnClickListener);
        }

        void setData(LiveVoiceLinkMicBean bean, Object payload) {
            if (payload == "room_votes") {
                if (bean.getVotes() != null&&!bean.getVotes().isEmpty()&&!bean.getVotes().equals("0")) {
                    votes_layout.setVisibility(View.VISIBLE);
                    votes.setText(handleVotes(bean.getVotes()));
                } else {
                    votes_layout.setVisibility(View.GONE);
                }
            }
            if (payload == "muted") {
                mImgMute.setVisibility(View.VISIBLE);
                return;
            }
            if (payload == "cam_change") {
                if (bean.cam_status == 2) {
                    Glide.with(cover_avatar.getContext()).load(bean.getAvatar()).into(cover_avatar);
                    cover_avatar.setVisibility(View.VISIBLE);
                    mViewEmpty.setVisibility(View.GONE);
                } else {
                    cover_avatar.setVisibility(View.GONE);
                    mViewEmpty.setVisibility(View.GONE);
                }
                return;
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
            if (bean.isEmpty()) {
                if (payload == null) {
                    itemView.setTag(bean);
                    mFace.setImageDrawable(null);
                    if (mViewEmpty.getVisibility() != View.VISIBLE) {
                        mViewEmpty.setVisibility(View.VISIBLE);
                    }
                    if (mImgMute.getVisibility() != View.INVISIBLE) {
                        mImgMute.setVisibility(View.INVISIBLE);
                    }
                    if (mName.getVisibility() != View.INVISIBLE) {
                        mName.setVisibility(View.INVISIBLE);
                    }
                }
            } else {
                if (payload == null) {
                    itemView.setTag(bean);
                    if (mViewEmpty.getVisibility() != View.INVISIBLE) {
                        mViewEmpty.setVisibility(View.INVISIBLE);
                    }
                    if (mName.getVisibility() != View.VISIBLE) {
                        mName.setVisibility(View.VISIBLE);
                    }
                    mName.setText(bean.getUserName());
                    if (bean.getVotes() != null&&!bean.getVotes().isEmpty()&&!bean.getVotes().equals("0")) {
                        votes_layout.setVisibility(View.VISIBLE);
                        votes.setText(handleVotes(bean.getVotes()));
                    } else {
                        votes_layout.setVisibility(View.GONE);
                    }
                    mFace.setImageDrawable(null);
                    if (mImgMute.getVisibility() != View.VISIBLE) {
                        mImgMute.setVisibility(View.VISIBLE);
                    }
                }
                mImgMute.setImageDrawable(bean.getStatus() == Constants.VOICE_CTRL_CLOSE ? mDrawable0 : mDrawable1);
                if (bean.cam_status == 2) {
                    Glide.with(cover_avatar.getContext()).load(bean.getAvatar()).into(cover_avatar);
                    cover_avatar.setVisibility(View.VISIBLE);
                    mViewEmpty.setVisibility(View.GONE);
                } else {
                    cover_avatar.setVisibility(View.GONE);
                    mViewEmpty.setVisibility(View.GONE);
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
                }else {
                    return ""+votes_;
                }

            } catch (Exception e) {
                return "0";
            }
        }
    }
}
