package com.example.framework.manager;

import android.content.Context;
import android.view.Gravity;
import android.view.View;

import com.example.framework.R;
import com.example.framework.view.DialogView;

/**
 * FileName: DialogManager
 * Founder: LiuGuiLin
 * Profile: 提示框管理类
 */
public class DialogManager {

    private static volatile DialogManager mInstance = null;

    private DialogManager() {

    }

    public static DialogManager getInstance() {
        if (mInstance == null) {
            synchronized (DialogManager.class) {
                if (mInstance == null) {
                    mInstance = new DialogManager();
                }
            }
        }
        return mInstance;
    }

    public DialogView initView(Context mContext, int layout) {
        return new DialogView(mContext, layout, R.style.Theme_Dialog, Gravity.CENTER);
    }

    public DialogView initView(Context mContext, int layout, int gravity) {
        return new DialogView(mContext, layout, R.style.Theme_Dialog, gravity);
    }

    public DialogView initView(Context mContext, View mView) {
        return new DialogView(mContext, mView, R.style.Theme_Dialog, Gravity.CENTER);
    }

    public DialogView initView(Context mContext, View mView, int gravity) {
        return new DialogView(mContext, mView, R.style.Theme_Dialog, gravity);
    }

    public void show(DialogView view) {
        if (view != null) {
            if (!view.isShowing()) {
                view.show();
            }
        }
    }

    public void hide(DialogView view) {
        if (view != null) {
            if (view.isShowing()) {
                view.dismiss();
            }

        }
    }
}
