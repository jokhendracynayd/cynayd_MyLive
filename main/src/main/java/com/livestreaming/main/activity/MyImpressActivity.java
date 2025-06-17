package com.livestreaming.main.activity;

import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.livestreaming.common.activity.AbsActivity;
import com.livestreaming.common.http.HttpCallback;
import com.livestreaming.common.utils.WordUtil;
import com.livestreaming.live.bean.ImpressBean;
import com.livestreaming.live.custom.MyTextView;
import com.livestreaming.main.R;
import com.livestreaming.main.http.MainHttpConsts;
import com.livestreaming.main.http.MainHttpUtil;

import java.util.Arrays;
import java.util.List;

/**
 * Created by cxf on 2018/10/15.
 * 我收到的主播印象
 */

public class MyImpressActivity extends AbsActivity {

    private LinearLayout mGroup;
    private TextView mTip;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_my_impress;
    }

    @Override
    protected void main() {
        setTitle(WordUtil.getString(com.livestreaming.common.R.string.impress));
        mGroup = (LinearLayout) findViewById(R.id.group);
        mTip = (TextView) findViewById(R.id.tip);
        MainHttpUtil.getMyImpress(new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0) {
                    if (info.length > 0) {
                        List<ImpressBean> list = JSON.parseArray(Arrays.toString(info), ImpressBean.class);
                        int line = 0;
                        int fromIndex = 0;
                        boolean hasNext = true;
                        LayoutInflater inflater = LayoutInflater.from(mContext);
                        while (hasNext) {
                            LinearLayout linearLayout = (LinearLayout) inflater.inflate(com.livestreaming.live.R.layout.view_impress_line, mGroup, false);
                            int endIndex = line % 2 == 0 ? fromIndex + 3 : fromIndex + 2;
                            if (endIndex >= list.size()) {
                                endIndex = list.size();
                                hasNext = false;
                            }
                            for (int i = fromIndex; i < endIndex; i++) {
                                MyTextView item = (MyTextView) inflater.inflate(com.livestreaming.live.R.layout.view_impress_item, linearLayout, false);
                                ImpressBean bean = list.get(i);
                                bean.setCheck(1);
                                item.setBean(bean, true);
                                linearLayout.addView(item);
                            }
                            fromIndex = endIndex;
                            line++;
                            mGroup.addView(linearLayout);
                        }
                    } else {
                        mTip.setText(WordUtil.getString(com.livestreaming.common.R.string.impress_tip_3));
                    }
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        MainHttpUtil.cancel(MainHttpConsts.GET_MY_IMPRESS);
        super.onDestroy();
    }
}
