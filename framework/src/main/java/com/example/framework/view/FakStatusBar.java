package com.example.framework.view;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.example.framework.global.AppGlobals;
import com.gyf.immersionbar.ImmersionBar;


public class FakStatusBar extends View {
    public FakStatusBar(Context context) {
        super(context);
    }

    public FakStatusBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FakStatusBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Context context = getContext();
        int height =0;
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
              height = ImmersionBar.getStatusBarHeight(activity);

        }
        setMeasuredDimension(widthMeasureSpec, height);
    }
}
