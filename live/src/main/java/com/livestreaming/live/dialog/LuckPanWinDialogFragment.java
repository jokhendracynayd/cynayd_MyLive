package com.livestreaming.live.dialog;

import android.os.Bundle;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.livestreaming.live.bean.TurntableGiftBean;
import com.livestreaming.common.dialog.AbsDialogFragment;
import com.livestreaming.common.utils.DpUtil;
import com.livestreaming.live.R;
import com.livestreaming.live.adapter.LuckPanWinAdapter;

import java.util.List;

/**
 * Created by cxf on 2019/8/27.
 */

public class LuckPanWinDialogFragment extends AbsDialogFragment implements View.OnClickListener {

    private RecyclerView mRecyclerView;
    private List<TurntableGiftBean>turntableResultGiftBeans;

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_luck_pan_win;
    }

    @Override
    protected int getDialogStyle() {
        return com.livestreaming.common.R.style.dialog;
    }

    @Override
    protected boolean canCancel() {
        return true;
    }

    @Override
    protected void setWindowAttributes(Window window) {
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = DpUtil.dp2px(300);
        params.height = DpUtil.dp2px(280);
        params.gravity = Gravity.CENTER;
        window.setAttributes(params);
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        findViewById(R.id.btn_confirm).setOnClickListener(this);

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        int size = turntableResultGiftBeans.size();
        mRecyclerView.setLayoutManager(new GridLayoutManager(mContext, size >= 4 ? 4 : size, GridLayoutManager.VERTICAL, false));
        LuckPanWinAdapter adapter = new LuckPanWinAdapter(mContext, turntableResultGiftBeans);
        mRecyclerView.setAdapter(adapter);
    }


    public void setTurntableResultGiftBeans(List<TurntableGiftBean> turntableResultGiftBeans) {
        this.turntableResultGiftBeans = turntableResultGiftBeans;
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_confirm) {
            dismiss();
        }
    }
}
