package com.livestreaming.live.dialog;

import android.os.Bundle;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONObject;
import com.livestreaming.common.dialog.AbsDialogFragment;
import com.livestreaming.common.http.HttpCallback;
import com.livestreaming.live.R;
import com.livestreaming.live.adapter.BackChangeAdapter;
import com.livestreaming.live.bean.ChangeRoomBackBean;
import com.livestreaming.live.http.LiveHttpUtil;

import java.util.ArrayList;

/**
 * Created by cxf on 2018/10/9.
 */

public class LiveChooseBackgroundDialogFragment extends AbsDialogFragment {


    public OnBackChangeDoneListener onBackChangedDone;
    RecyclerView recyclerView;
    BackChangeAdapter adapter;

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_room_backs;
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
//        params.y = DpUtil.dp2px(50);
        window.setAttributes(params);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        recyclerView = (RecyclerView) mRootView.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(mContext, 2, GridLayoutManager.VERTICAL, false));
        adapter = new BackChangeAdapter(new ArrayList<ChangeRoomBackBean>(), new OnBackChangeDoneListener() {
            @Override
            public void onClick(ChangeRoomBackBean bean) {
                onBackChangedDone.onClick(bean);
                dismiss();
            }
        });
        recyclerView.setAdapter(adapter);
        LiveHttpUtil.getUserRoomBackgrounds(new HttpCallback() {

            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (info != null && info.length > 0) {
                    handleBacks(info);
                }
            }
        });
    }

    private void handleBacks(String[] info) {
        ArrayList<ChangeRoomBackBean> list = new ArrayList<>();

        for (String obj : info
        ) {
            JSONObject object = JSONObject.parseObject(obj);
            if(object!=null) {
                ChangeRoomBackBean bean = new ChangeRoomBackBean();
                bean.setId(object.getInteger("id"));
                bean.setThumb(object.getString("thumb"));
                bean.setSwf(object.getString("swf"));
                list.add(bean);
            }
        }
        adapter.setList(list);
    }


    public interface OnBackChangeDoneListener {
        public void onClick(ChangeRoomBackBean bean);
    }

}
