package com.livestreaming.game.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.livestreaming.common.glide.ImgLoader;
import com.livestreaming.game.R;
import com.livestreaming.game.bean.GameBaishunDTO;
import com.livestreaming.common.interfaces.OnItemClickListener;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by cxf on 2018/10/31.
 */

public class GameAdapter extends RecyclerView.Adapter<GameAdapter.Vh> {

    private List<GameBaishunDTO> mList;
    private LayoutInflater mInflater;
    private View.OnClickListener mOnClickListener;
    private OnItemClickListener<GameBaishunDTO> mOnItemClickListener;
    private Context context;
    public GameAdapter(Context context, List<GameBaishunDTO> list) {
        mList = list;
        this.context = context;
        if (mList == null) {
            mList = new ArrayList<>();
        }

    }


    public void setOnItemClickListener(OnItemClickListener<GameBaishunDTO> onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
        mInflater = LayoutInflater.from(context);
        mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Object tag = v.getTag();
                if (tag != null && mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick((GameBaishunDTO) tag, 0);
                }
            }
        };
    }

    @NonNull
    @Override
    public Vh onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Vh(mInflater.inflate(R.layout.item_game, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Vh vh, int position) {
        vh.setData(mList.get(position));
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
            mIcon = itemView.findViewById(R.id.icon);
            mName = itemView.findViewById(R.id.name);
            itemView.setOnClickListener(mOnClickListener);
        }

        void setData(GameBaishunDTO gameAction) {
            itemView.setTag(gameAction);
            ImgLoader.display(context,gameAction.getIcon(),mIcon);
            mName.setText(gameAction.getName());
        }
    }
}
