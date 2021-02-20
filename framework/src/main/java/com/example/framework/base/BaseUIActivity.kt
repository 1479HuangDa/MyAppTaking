package com.example.framework.base

import android.Manifest
import android.os.Bundle
import android.widget.Toast
import com.example.framework.helper.FileHelper
import com.gyf.immersionbar.ImmersionBar
import com.permissionx.guolindev.PermissionX
import me.yokeyword.fragmentation.SupportActivity
import me.yokeyword.fragmentation.anim.DefaultHorizontalAnimator
import me.yokeyword.fragmentation.anim.FragmentAnimator

open class BaseUIActivity : SupportActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        SystemUI.fixSystemUI(this)
        ImmersionBar.with(this)
            //状态栏字体是深色，不写默认为亮色
            .statusBarDarkFont(true)
            .navigationBarColor(android.R.color.white)
            .init()




    }


    override fun onCreateFragmentAnimator(): FragmentAnimator {

        // 设置横向(和安卓4.x动画相同)
        return DefaultHorizontalAnimator()
    }



}