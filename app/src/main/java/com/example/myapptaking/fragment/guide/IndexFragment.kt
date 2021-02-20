package com.example.myapptaking.fragment.guide

import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.view.View
import com.example.framework.base.BaseUIFragment
import com.example.framework.bmob.BmobManager
import com.example.framework.entity.Constants.SP_IS_FIRST_APP
import com.example.framework.entity.Constants.SP_TOKEN
import com.example.framework.utils.SpUtils
import com.example.myapptaking.R
import com.example.myapptaking.fragment.main.MainFragment

class IndexFragment : BaseUIFragment() {

    companion object {

        const val SKIP_MAIN = 1000

        fun newInstance(): IndexFragment {
            val args = Bundle()
            val fragment = IndexFragment()
            fragment.arguments = args
            return fragment
        }
    }

    private val handle = Handler(Handler.Callback {
        when (it.what) {
            SKIP_MAIN -> startMainPage()
        }
        return@Callback false
    })


    private fun startMainPage() {
        val isFirstApp = SpUtils.getInstance().getBoolean(SP_IS_FIRST_APP, true)
        val token = SpUtils.getInstance().getString(SP_TOKEN, "")
        when {
            isFirstApp -> {
                startWithPop(GuideFragment.newInstance())
                SpUtils.getInstance().putBoolean(SP_IS_FIRST_APP, false)
            }
            TextUtils.isEmpty(token) -> {
                if (BmobManager.instance?.isLogin == false) {
                    startWithPop(LoginFragment.newInstance())
                } else {
                    startWithPop(MainFragment.newInstance())
                }

            }
            else -> {
                startWithPop(MainFragment.newInstance())
            }
        }

    }


    override fun getLayout(): Any {
        return R.layout.fragment_index
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handle.sendEmptyMessageDelayed(SKIP_MAIN, 2 * 1000)
    }


    /**
     * 优化
     * 冷启动经过的步骤：
     * 1.第一次安装，加载应用程序并且启动
     * 2.启动后显示一个空白的窗口 getWindow()
     * 3.启动/创建了我们的应用进程
     *
     * App内部：
     * 1.创建App对象/Application对象
     * 2.启动主线程(Main/UI Thread)
     * 3.创建应用入口/LAUNCHER
     * 4.填充ViewGroup中的View
     * 5.绘制View measure -> layout -> draw
     *
     * 优化手段：
     * 1.视图优化
     *   1.设置主题透明
     *   2.设置启动图片
     * 2.代码优化
     *   1.优化Application
     *   2.布局的优化，不需要繁琐的布局
     *   3.阻塞UI线程的操作
     *   4.加载Bitmap/大图
     *   5.其他的一个占用主线程的操作
     *
     *
     * 检测App Activity的启动时间
     * 1.Shell
     *   ActivityManager -> adb shell am start -S -W com.imooc.meet/com.imooc.meet.ui.IndexActivity
     *   ThisTime: 478ms 最后一个Activity的启动耗时
     *   TotalTime: 478ms 启动一连串Activity的总耗时
     *   WaitTime: 501ms 应用创建的时间 + TotalTime
     *   应用创建时间： WaitTime - TotalTime（501 - 478 = 23ms）
     * 2.Log
     *   Android 4.4 开始，ActivityManager增加了Log TAG = displayed
     */
}