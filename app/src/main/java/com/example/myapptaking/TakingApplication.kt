package com.example.myapptaking

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex
import com.example.framework.FrameWork
import com.example.framework.utils.SpUtils

class TakingApplication :Application(){

    /**
     * Application的优化
     * 1.必要的组件在程序主页去初始化
     * 2.如果组件一定要在App中初始化，那么尽可能的延时
     * 3.非必要的组件，子线程中初始化
     */

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun onCreate() {
        super.onCreate()
        FrameWork.getInstance().initFramework(this)
    }

}