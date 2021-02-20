package com.example.framework.utils

import android.Manifest
import android.app.Activity
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.example.framework.global.AppGlobals
import com.example.framework.helper.FileHelper
import com.permissionx.guolindev.PermissionX

object PermissionWrapper {


    //申明所需权限
    private val mStrPermission = arrayOf(
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.CAMERA,
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.CALL_PHONE,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    fun requiredPermission(activity: FragmentActivity,listener:(isSucceed:Boolean)->Unit){
        PermissionX.init(activity)
            .permissions(mStrPermission.asList())
            .onExplainRequestReason { scope, deniedList ->
                val msg = "获取应用权限"
                val positive = "确定"
                val negative = "取消"
                scope.showRequestReasonDialog(deniedList, msg, positive, negative)
            }.onForwardToSettings { scope, deniedList ->
                val msg = "获取应用权限"
                val positive = "确定"
                val negative = "取消"
                scope.showForwardToSettingsDialog(deniedList, msg, positive, negative)
            }.request { allGranted, _, deniedList ->
                if (!allGranted || deniedList.isNotEmpty()) {
                    Toast.makeText(activity, "获取应用权限失败", Toast.LENGTH_SHORT).show()
                    listener(false)
                } else {

                    listener(true)

                }

            }
    }

    fun requiredPermission(mFragment: Fragment, listener:(isSucceed:Boolean)->Unit){
        PermissionX.init(mFragment)
            .permissions(mStrPermission.asList())
            .onExplainRequestReason { scope, deniedList ->
                val msg = "获取应用权限"
                val positive = "确定"
                val negative = "取消"
                scope.showRequestReasonDialog(deniedList, msg, positive, negative)
            }.onForwardToSettings { scope, deniedList ->
                val msg = "获取应用权限"
                val positive = "确定"
                val negative = "取消"
                scope.showForwardToSettingsDialog(deniedList, msg, positive, negative)
            }.request { allGranted, _, deniedList ->
                if (!allGranted || deniedList.isNotEmpty()) {
                    Toast.makeText(AppGlobals.getApplication(), "获取应用权限失败", Toast.LENGTH_SHORT).show()
                    listener(false)
                } else {

                    listener(true)

                }

            }
    }

}