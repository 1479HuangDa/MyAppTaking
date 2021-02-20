package com.example.framework.base

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.framework.event.EventManager
import com.example.framework.event.MessageEvent
import me.yokeyword.fragmentation.ISupportFragment
import me.yokeyword.fragmentation.SupportFragment
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


open abstract class BaseUIFragment : SupportFragment() {

    companion object {
        //申请窗口权限的Code
        const val PERMISSION_WINDOW_REQUEST_CODE = 1001
    }

    abstract fun getLayout(): Any

    protected open var rootView: View? = null

    protected open var mViewCreated = false

    //fragment回退是否处理
    protected open var mSuperBackPressedSupport = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventManager.register(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (rootView?.parent != null) {
            val parent = rootView?.parent as ViewGroup
            parent.removeView(rootView)
        } else {
            val layout: Any = getLayout()
            rootView = when (layout) {
                is Int -> {
                    val layoutId = getLayout() as Int
                    inflater.inflate(layoutId, container, false)
                }
                is View -> {
                    getLayout() as View
                }
                else -> {
                    throw ClassCastException("type of setLayout() must be int or View!")
                }
            }
        }

        mViewCreated = true
        return rootView
    }


    //    @Override
    //    public FragmentAnimator onCreateFragmentAnimator() {
    //        return new DefaultHorizontalAnimator();
    //    }
    override fun start(toFragment: ISupportFragment?) {
        val parentFragment = parentFragment
        if (parentFragment is BaseUIFragment) {
            val fragment: BaseUIFragment? =
                parentFragment as BaseUIFragment?
            fragment?.start(toFragment)
        } else {
            super.start(toFragment)
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: MessageEvent) {
        handMessageEvent(event)
//        when (event.type) {
//            EventManager.EVENT_REFRE_TOKEN_STATUS -> checkToken()
//        }
    }

    protected open fun handMessageEvent(event: MessageEvent) {

    }


    /**
     * 判断窗口权限
     *
     * @return
     */
    open fun checkWindowPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(_mActivity)
        } else true
    }

    /**
     * 请求窗口权限
     */
    open fun requestWindowPermissions() {
        Toast.makeText(_mActivity, "申请窗口权限", Toast.LENGTH_SHORT).show()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + _mActivity.packageName)
            )
            this.startActivityForResult(intent, PERMISSION_WINDOW_REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PERMISSION_WINDOW_REQUEST_CODE) {
            Toast.makeText(_mActivity, "窗口权限", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onBackPressedSupport(): Boolean {
        val childFragmentManager = childFragmentManager
        val manager = _mActivity.supportFragmentManager
        val backStackEntryCount = manager.backStackEntryCount
        val childBackStackEntryCount = childFragmentManager.backStackEntryCount
        if (mSuperBackPressedSupport) {
            when {
                childBackStackEntryCount > 1 -> {
                    popChild()
                }
                backStackEntryCount > 1 -> {
                    pop()
                }
                else -> {
                    val parentFragment = parentFragment
                    if (parentFragment is ISupportFragment) {
                        return (parentFragment as ISupportFragment).onBackPressedSupport()
                    }
                    mSuperBackPressedSupport = false
                }
            }
            //            return mSuperBackPressedSupport || super.onBackPressedSupport();
        } else {
            val parentFragment = parentFragment
            if (parentFragment is ISupportFragment) {
                return (parentFragment as ISupportFragment).onBackPressedSupport()
            }
            pop()
            return true
        }
        return mSuperBackPressedSupport
    }

    override fun onDestroy() {
        super.onDestroy()
        EventManager.unregister(this)
    }

}