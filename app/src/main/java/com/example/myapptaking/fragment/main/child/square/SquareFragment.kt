package com.example.myapptaking.fragment.main.child.square

import android.os.Bundle
import android.view.View
import cn.bmob.v3.exception.BmobException
import cn.bmob.v3.listener.FindListener
import com.example.framework.base.MultiListFragment
import com.example.framework.bmob.BmobManager
import com.example.framework.bmob.IMUser
import com.example.framework.bmob.SquareSet
import com.example.framework.model.CommonBean
import com.example.framework.recycler.multi22.JssBaseMultiAdapter
import com.example.framework.utils.CommonUtils
import com.example.library_common.view.bottombar.BottomBorInfo
import com.example.myapptaking.R
import com.example.myapptaking.fragment.main.child.model.SquareModel
import com.example.myapptaking.fragment.main.child.square.provider.SquareImageProvider
import com.example.myapptaking.fragment.main.child.square.provider.SquareMusicProvider
import com.example.myapptaking.fragment.main.child.square.provider.SquareTextProvider
import com.example.myapptaking.fragment.main.child.square.provider.SquareVideoProvider
import ikidou.reflect.TypeBuilder
import java.lang.reflect.Type

/**
 * FileName: SquareFragment
 * Founder: LiuGuiLin
 * Profile: 广场
 */
class SquareFragment : MultiListFragment<SquareModel>(), BottomBorInfo, View.OnClickListener {

    companion object {
        fun newInstance(): SquareFragment {
            val args = Bundle()
            val fragment = SquareFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override val title = R.string.text_main_square

    override val icon = R.drawable.img_square

    private var mSquareSetMap = mutableMapOf<String, MutableList<SquareSet?>?>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isEnableLoadMore = false
    }

    override fun initView(view: View) {
        super.initView(view)
        mRecyclerView?.overScrollMode = View.OVER_SCROLL_NEVER
        addHeader(R.layout.fragment_square_header)
        content_frame?.visibility = View.VISIBLE
        content_frame?.removeAllViews()
        addContentFrame(R.layout.fragment_square_content_frame)
        val iv_push: View? = content_frame?.findViewById(R.id.iv_push)
        content_frame?.isClickable = false
        iv_push?.setOnClickListener(this)
    }

    override fun refreshData() {
        onRefresh()
    }

    override fun addOnListItemProvider(mAdapter: JssBaseMultiAdapter<SquareModel?>?) {

        mAdapter?.addItemProvider(SquareVideoProvider())
        mAdapter?.addItemProvider(SquareTextProvider())
        mAdapter?.addItemProvider(SquareMusicProvider(_mActivity))
        mAdapter?.addItemProvider(SquareImageProvider())
    }

    override fun netRequest() {
        loadSquare()
    }


    private fun loadSquare() {
        if (mSwipeRefreshLayout?.isRefreshing == false) {
            mSwipeRefreshLayout?.isRefreshing = true
        }

        BmobManager.instance?.queryAllSquare(object : FindListener<SquareSet?>() {
            override fun done(list: List<SquareSet?>?, e: BmobException?) {


                if (e == null) {
                    if (CommonUtils.isEmpty(list)) {
                        //倒序
                        val mList = list?.reversed() ?: emptyList()
                        val mUserIdList = mutableListOf<String>()
                        mSquareSetMap.clear()
                        mList.forEach {
                            val userIdStr = it?.userId ?: ""
                            mUserIdList.add(userIdStr)
                            var mSquareSetList: MutableList<SquareSet?>? = mutableListOf()
                            if (mSquareSetMap[userIdStr] != null) {
                                mSquareSetList = mSquareSetMap[userIdStr]
                            }
                            mSquareSetList?.add(it)
                            mSquareSetMap[userIdStr] = mSquareSetList
                        }
                        BmobManager.instance?.queryObjectIdUsers(mUserIdList,
                            object : FindListener<IMUser?>() {
                                override fun done(p0: MutableList<IMUser?>?, p1: BmobException?) {
                                    if (p1 == null) {
                                        if (CommonUtils.isEmpty(p0)) {

                                            val mSquareModelList = mutableListOf<SquareModel>()

                                            p0?.forEach { mUser ->

                                                val objectIdStr = mUser?.objectId ?: ""

                                                val mSquareSetList = mSquareSetMap[objectIdStr]

                                                mSquareSetList?.forEach { mSquareSet ->
                                                    mSquareModelList.add(
                                                        SquareModel(
                                                            mSquareSet,
                                                            mUser
                                                        )
                                                    )
                                                }

                                            }
                                            loadListDate(mSquareModelList)
                                        } else {
                                            onFailed()
                                        }
                                    } else {
                                        onFailed()
                                    }
                                }

                            })
//                       loadListDate(mList)
                    } else {
                        onFailed()
                    }
                } else {
                    onFailed()
                }
            }
        })

    }

    override val listType: Type
        get() = TypeBuilder.newInstance(CommonBean::class.java)
            .beginSubType(List::class.java)
            .addTypeParam(SquareSet::class.java)
            .build()

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.iv_push -> {
                val newInstance = PushSquareFragment.newInstance()
                newInstance.mPushSquareListener = mPushSquareListener
                start(newInstance)
            }
        }
    }


    private var mPushSquareListener = object : PushSquareFragment.PushSquareListener {
        override fun onSuccess() {
            //刷新
            loadSquare()
        }

    }
}