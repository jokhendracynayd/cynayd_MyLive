package com.livestreaming.common.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * Created by cxf on 2018/9/26.
 */

public class MyRelativeLayout6 extends RelativeLayout {

    public MyRelativeLayout6(Context context) {
        super(context);
    }

    public MyRelativeLayout6(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyRelativeLayout6(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        float widthSize = MeasureSpec.getSize(widthMeasureSpec);
        heightMeasureSpec = MeasureSpec.makeMeasureSpec((int) (widthSize * 13 / 9), MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
