package com.example.framework.utils;

import android.util.DisplayMetrics;

import com.example.framework.global.AppGlobals;

public class PixUtils {

    public static int dp2px(int dpValue) {
        DisplayMetrics metrics = AppGlobals.getApplication().getResources().getDisplayMetrics();
        return (int) (metrics.density * dpValue + 0.5f);
    }

    /**
     * 将sp值转换为px值，保证文字大小不变
     *

     *            （DisplayMetrics类中属性scaledDensity）
     * @return
     */
    public static int sp2px( float spValue) {
        final float fontScale =  AppGlobals.getApplication().getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    public static int getScreenWidth() {
        DisplayMetrics metrics = AppGlobals.getApplication().getResources().getDisplayMetrics();
        return metrics.widthPixels;
    }

    public static int getScreenHeight() {
        DisplayMetrics metrics = AppGlobals.getApplication().getResources().getDisplayMetrics();
        return metrics.heightPixels;
    }
}