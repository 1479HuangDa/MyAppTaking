package com.example.framework.base

import android.os.Bundle
import android.view.View
import androidx.annotation.Nullable
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

abstract class LazyFragment : BaseUIFragment() {

    protected var isCallOnViewCreated = false

    /**
     * 是否执行懒加载
     */
    protected var isLoaded = false

    /**
     * 当前Fragment是否对用户可见
     */
    private var isVisibleToUser = false

    /**
     * 当使用ViewPager+Fragment形式会调用该方法时，setUserVisibleHint会优先Fragment生命周期函数调用，
     * 所以这个时候就,会导致在setUserVisibleHint方法执行时就执行了懒加载，
     * 而不是在onResume方法实际调用的时候执行懒加载。所以需要这个变量
     */
    protected var isCallResume = false

    /**
     * 是否调用了setUserVisibleHint方法。处理show+add+hide模式下，默认可见 Fragment 不调用
     * onHiddenChanged 方法，进而不执行懒加载方法的问题。
     */
    private var isCallUserVisibleHint = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!isCallOnViewCreated) {
            initView(view)
            isCallOnViewCreated = true
        }
    }


    override fun onResume() {
        super.onResume()
        isCallResume = true
        if (!isCallUserVisibleHint) isVisibleToUser = !isHidden
        judgeLazyInit()
    }

//    override fun onSupportVisible() {
//        super.onSupportVisible()
//        isCallResume = true
//        if (!isCallUserVisibleHint) isVisibleToUser = !isHidden
//        judgeLazyInit()
//    }


    private fun judgeLazyInit() {
        if (!isLoaded && isVisibleToUser && isCallResume) {
            lazyInit()
            isLoaded = true
        }
    }

    fun setIsLoad(isLoaded: Boolean) {
        this.isLoaded = isLoaded
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        isVisibleToUser = !hidden
        judgeLazyInit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        isLoaded = false
        isVisibleToUser = false
        isCallUserVisibleHint = false
        isCallResume = false
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        this.isVisibleToUser = isVisibleToUser
        isCallUserVisibleHint = true
        judgeLazyInit()
    }

    abstract fun lazyInit()

    abstract fun initView(view: View)
}