package com.example.framework.base

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseQuickAdapter.AnimationType
import com.chad.library.adapter.base.listener.OnItemClickListener
import com.chad.library.adapter.base.listener.OnLoadMoreListener
import com.example.framework.R
import com.example.framework.cache.DataCacheModule
import com.example.framework.cache.JssCacheManager
import com.example.framework.global.AppGlobals
import com.example.framework.model.CommonBean
import com.example.framework.recycler.multi22.JssBaseMultiAdapter
import com.example.framework.recycler.multi22.JssNewMultiItemEntity
import com.example.framework.utils.NetworkUtil
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.lang.reflect.Type
import java.util.*

abstract class MultiListFragment<T : JssNewMultiItemEntity?> : LazyFragment(), OnItemClickListener,
    OnRefreshListener, OnLoadMoreListener {

    private var Save_TAG = ""

    protected open var mLayoutRoot: LinearLayout? = null

    protected var mHeader: LinearLayout? = null

    protected open var mFooter: LinearLayout? = null

    protected open var mRecyclerView: RecyclerView? = null

    protected open var mAdapter: JssBaseMultiAdapter<T>? = null

    protected open var isAlwaysRefresh = true

    protected var isEnableLoadMore = true

    protected open var isAddDividerItemDecoration = true

    protected var mSwipeRefreshLayout: SwipeRefreshLayout? = null

    protected var mData = mutableListOf<T>()

    protected open var pageIndex = 1

    protected var list_empty_view: ViewGroup? = null

    protected open var delayedTime = 1000

    protected open var content_frame: FrameLayout? = null

    protected open var isInitialRefresh = true

    protected open var gson = GsonBuilder().create()

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        if (mAdapter == null) return

        val bean = CommonBean<List<T>>()
        //
        bean.data = mAdapter?.data
        if (TextUtils.isEmpty(Save_TAG)) {
            Save_TAG = mAdapter?.data?.javaClass?.simpleName +
                    "-" +
                    bean.hashCode() +
                    "-" //                    + JssUserManager.getUserToken().getUserId().hashCode()
        }
        val dataCacheModule = DataCacheModule()
        val data = bean.data
        val beanList = dataCacheModule.beanList
        beanList.addAll(data)
        outState.putString("Save_TAG", Save_TAG)
        JssCacheManager.save(Save_TAG, dataCacheModule)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if (savedInstanceState != null && mAdapter != null) {

//            String data = savedInstanceState.getString(TAG);
            JssCacheManager.getCache(Save_TAG) { o: Any ->
                Log.d("Save_TAG", "data: $o")
                if (o is DataCacheModule) {
//                    String data = (String) o;
                    mAdapter?.clears()
                    val beanList = o.beanList
                    for (i in beanList.indices) {
                        mAdapter?.addData(beanList[i] as T)
                    }
                    if (mAdapter?.data?.size ?: 0 > 0) {
                        setIsLoad(true)
                    }
                }
            }
        }
    }

    override fun getLayout(): Any {
        return R.layout.fragment_simple_list
    }

    override fun initView(view: View) {
        content_frame = view.findViewById(R.id.content_frame)
        mLayoutRoot = view.findViewById(R.id.layout_root)
        mHeader = view.findViewById(R.id.header)
        mRecyclerView = view.findViewById(R.id.mRecyclerView)
        mFooter = view.findViewById(R.id.footer)
        val layoutManager = layoutManager
        mRecyclerView?.layoutManager = layoutManager
        if (isAddDividerItemDecoration) {
            val dividerItemDecoration = addItemDecoration()
            val itemDecorationCount = mRecyclerView?.itemDecorationCount ?: 0
            if (itemDecorationCount > 0) {
                for (i in 0 until itemDecorationCount) {
                    mRecyclerView?.removeItemDecorationAt(i)
                }
            }
            mRecyclerView?.addItemDecoration(dividerItemDecoration)
        }
        mRecyclerView?.setHasFixedSize(true)
        if (mAdapter == null) {
            mAdapter = adapter
            addHeaderAndFooterInList()
            mAdapter?.footerWithEmptyEnable = true
            mAdapter?.headerWithEmptyEnable = true
            mAdapter?.headerViewAsFlow = true
            mAdapter?.footerViewAsFlow = true

        }

        mAdapter?.attachFragment(this)
        mRecyclerView?.adapter = mAdapter
        if (mAdapter?.jssRecyclerView == null && mRecyclerView != null) {
            mAdapter?.recyclerView = mRecyclerView as RecyclerView
        }
        mAdapter?.setOnItemClickListener(this)
        if (isEnableLoadMore) {
            mAdapter?.loadMoreModule?.setOnLoadMoreListener(this)
        }
        mAdapter?.loadMoreModule?.isEnableLoadMore = isEnableLoadMore
        mAdapter?.setAnimationWithDefault(AnimationType.AlphaIn)
        mSwipeRefreshLayout = view.findViewById(R.id.mSwipeRefreshLayout)
        val color = ContextCompat.getColor(AppGlobals.getApplication(), R.color.PrimaryColor)
        mSwipeRefreshLayout?.setColorSchemeColors(color)
        mSwipeRefreshLayout?.setOnRefreshListener(this)
        list_empty_view = LayoutInflater.from(_mActivity)
            .inflate(R.layout.list_empty_view, view as ViewGroup, false) as ViewGroup
        mAdapter?.setEmptyView(list_empty_view!!)
    }

    protected fun addHeader(@LayoutRes layoutId: Int) {
        LayoutInflater.from(_mActivity).inflate(layoutId, mHeader, true)
    }

    protected fun addContentFrame(@LayoutRes layoutId: Int) {
        LayoutInflater.from(_mActivity).inflate(layoutId, content_frame, true)
    }

    protected fun addEmptyView(@LayoutRes layoutId: Int) {
        LayoutInflater.from(_mActivity).inflate(layoutId, list_empty_view, true)
    }

    protected fun addFooter(@LayoutRes layoutId: Int) {
        LayoutInflater.from(_mActivity).inflate(layoutId, mFooter, true)
    }

    protected abstract fun addOnListItemProvider(mAdapter: JssBaseMultiAdapter<T?>?)

    protected val adapter: JssBaseMultiAdapter<T>
        get() = object : JssBaseMultiAdapter<T>(mData) {
            override fun addListItemProvider(mAdapter: JssBaseMultiAdapter<T?>?) {
                addOnListItemProvider(mAdapter)
            }
        }

    protected abstract fun netRequest()

    protected open fun addItemDecoration(): ItemDecoration {
        return DividerItemDecoration(_mActivity, LinearLayoutManager.VERTICAL)
    }

    protected open val layoutManager: RecyclerView.LayoutManager
        get() = LinearLayoutManager(_mActivity, LinearLayoutManager.VERTICAL, false)

    protected open fun addHeaderAndFooterInList() {}

    override fun onRefresh() {
        pageIndex = 1
        mAdapter?.isUseEmpty = false

        mAdapter?.notifyDataSetChanged()
        mAdapter?.clears()
        mSwipeRefreshLayout?.postDelayed({ netRequest() }, delayedTime.toLong())
    }

    override fun onItemClick(adapter: BaseQuickAdapter<*, *>, view: View, position: Int) {}

    override fun onLoadMore() {
        pageIndex++
        mSwipeRefreshLayout?.postDelayed({ netRequest() }, delayedTime.toLong())
    }

    override fun lazyInit() {
        if (mSwipeRefreshLayout == null || mAdapter == null) return
        mAdapter?.loadMoreModule?.isEnableLoadMore = true
        mSwipeRefreshLayout?.isRefreshing = isInitialRefresh
        onRefresh()
    }

    override fun onDestroy() {
        super.onDestroy()
        mAdapter?.clears()
        mAdapter?.detachFragment()
    }

    @Throws(Exception::class)
    protected fun parseDate(data: String?) {
        val listType = listType
        val bean: CommonBean<List<T>> = gson.fromJson(data, listType)
        val contentObject = bean.data
        loadListDate(contentObject)
    }

    protected abstract val listType: Type

    protected open fun loadListDate(collection: Collection<T?>?) {
        if (collection == null || collection.isEmpty()) {
            if (pageIndex == 1) {
                onFailed()
            } else {
                mAdapter?.loadMoreModule?.loadMoreEnd(false)
            }
            return
        }
        if (pageIndex == 1) {
            mAdapter?.replaceData(collection)
            val loadMoreModule = mAdapter?.loadMoreModule
            loadMoreModule?.isEnableLoadMoreIfNotFullPage = true

        } else {
            mAdapter?.addData(collection)
        }
        mAdapter?.loadMoreModule?.loadMoreComplete()
        if (mSwipeRefreshLayout?.isRefreshing == true) {
            mSwipeRefreshLayout?.isRefreshing = false
        }
        mSwipeRefreshLayout?.isEnabled = isAlwaysRefresh
        mAdapter?.loadMoreModule?.checkDisableLoadMoreIfNotFullPage()
    }

    protected fun onFailed() {
        if (mAdapter?.data?.size ?: 0 <= 0) {
            preEmptyView()
            mAdapter?.isUseEmpty = true
        } else {
            mAdapter?.loadMoreModule?.loadMoreFail()

        }
        mAdapter?.notifyDataSetChanged()
        if (mSwipeRefreshLayout?.isRefreshing == true) {
            mSwipeRefreshLayout?.isRefreshing = false
        }
    }

    protected open fun preEmptyView() {
        val textView: TextView? = list_empty_view?.findViewById(R.id.textView)
        if (!NetworkUtil.isNetAvailable(_mActivity)) {
            textView?.text = "网络异常"
        } else {
            textView?.text = "暂无数据"
        }
    }
}