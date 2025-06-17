package com.livestreaming.live.dialog;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.livestreaming.common.dialog.AbsDialogFragment;
import com.livestreaming.common.interfaces.OnItemClickListener;
import com.livestreaming.common.utils.SpUtil;
import com.livestreaming.game.R;
import com.livestreaming.game.adapter.GameAdapter;
import com.livestreaming.game.bean.GameBaishunConfigDTO;
import com.livestreaming.game.bean.GameBaishunDTO;
import com.livestreaming.game.util.GamePresenter;

import java.util.List;

/**
 * Created by cxf on 2018/10/31.
 */

public class GameDialogFragment extends AbsDialogFragment implements OnItemClickListener<GameBaishunDTO>, View.OnClickListener {

    private GamePresenter mGamePresenter;
    private GameBaishunConfigDTO mGameBaisConfig;
    RecyclerView recyclerView;
    public boolean isInLive=false;

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_game;
    }

    @Override
    protected int getDialogStyle() {
        return com.livestreaming.common.R.style.dialog2;
    }

    @Override
    protected boolean canCancel() {
        return true;
    }

    @Override
    protected void setWindowAttributes(Window window) {
        window.setWindowAnimations(com.livestreaming.common.R.style.bottomToTopAnim);
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.BOTTOM;
        window.setAttributes(params);
    }


    public void setGamePresenter(GamePresenter gamePresenter) {
        mGamePresenter = gamePresenter;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mRootView.findViewById(R.id.btn_back).setOnClickListener(this);
        mGameBaisConfig = JSON.parseObject(SpUtil.getInstance().getStringValue("baishun_game_config"),GameBaishunConfigDTO.class);
        List<GameBaishunDTO> GameList = JSON.parseArray(SpUtil.getInstance().getStringValue("game_baishun"), GameBaishunDTO.class);
        if (GameList.size() > 0) {
            recyclerView = mRootView.findViewById(R.id.recyclerView);
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new GridLayoutManager(mRootView.getContext(), 3, GridLayoutManager.VERTICAL, false));
            GameAdapter adapter = new GameAdapter(mRootView.getContext(), GameList);
            adapter.setOnItemClickListener(this);
            recyclerView.setAdapter(adapter);
        }else{
            dismiss();
        }
    }

    @Override
    public void onItemClick(GameBaishunDTO gameAction, int position) {
        com.livestreaming.game.views.LiveGamesDialog dialog=new com.livestreaming.game.views.LiveGamesDialog();
        dialog.url=gameAction.getUrl();
        if(!isInLive){
            mGameBaisConfig.setGameMode(3);
            dialog.scan=1.0f;
        }
        dialog.mGameBaisConfig=mGameBaisConfig;
        if(isInLive) {
            dialog.scan = 0.62f;
        }
        dialog.show(getChildFragmentManager(),"");
//        dismiss();
//        if (mGamePresenter != null) {
//            mGamePresenter.startGame(gameAction);
//        }
    }

    @Override
    public void onDestroy() {
        mGamePresenter = null;
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_back) {
            dismiss();

        }
    }
}
