package com.example.framework;

import android.content.Context;

import com.example.framework.bmob.BmobManager;
import com.example.framework.cloud.CloudManager;
import com.example.framework.helper.WindowHelper;
import com.example.framework.manager.KeyWordManager;
import com.example.framework.manager.MapManager;
import com.example.framework.manager.NotificationHelper;
import com.example.framework.utils.LogUtils;
import com.example.framework.utils.SpUtils;

import org.litepal.LitePal;

import io.reactivex.plugins.RxJavaPlugins;

public class FrameWork {

    private volatile static FrameWork frameWork;

    private FrameWork() {

    }

    public static FrameWork getInstance() {
        if (frameWork == null) {
            synchronized (FrameWork.class) {
                if (frameWork == null) {
                    frameWork = new FrameWork();
                }
            }
        }
        return frameWork;
    }

    /**
     * 初始化框架 Model
     *
     * @param mContext
     */
    public void initFramework(Context mContext) {
        LogUtils.i("initFramework");
        SpUtils.getInstance().initSp(mContext);
        BmobManager.getInstance().initBmob(mContext);
        CloudManager.getInstance().initCloud(mContext);
        LitePal.initialize(mContext);
        KeyWordManager.getInstance().initManager(mContext);
        NotificationHelper.getInstance().createChannel(mContext);
        MapManager.getInstance().initMap(mContext);
        WindowHelper.getInstance().initWindow(mContext);
//        MapManager.getInstance().initMap(mContext);
//        WindowHelper.getInstance().initWindow(mContext);
//        CrashReport.initCrashReport(mContext, BUGLY_KEY, BuildConfig.LOG_DEBUG);
//        ZXingLibrary.initDisplayOpinion(mContext);
//        NotificationHelper.getInstance().createChannel(mContext);
//        KeyWordManager.getInstance().initManager(mContext);
//
//        //全局捕获RxJava异常
//        RxJavaPlugins.setErrorHandler(new Consumer<Throwable>() {
//            @Override
//            public void accept(Throwable throwable) throws Exception {
//                LogUtils.e("RxJava：" + throwable.toString());
//            }
//        });
    }


}

