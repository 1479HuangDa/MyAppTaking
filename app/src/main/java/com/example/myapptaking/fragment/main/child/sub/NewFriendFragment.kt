package com.example.myapptaking.fragment.main.child.sub

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.arch.core.executor.ArchTaskExecutor
import cn.bmob.v3.exception.BmobException
import cn.bmob.v3.listener.FindListener
import cn.bmob.v3.listener.SaveListener
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemChildClickListener
import com.example.framework.base.SimpleListFragment
import com.example.framework.bmob.BmobManager
import com.example.framework.bmob.IMUser
import com.example.framework.cloud.CloudManager
import com.example.framework.db.LitePalHelper
import com.example.framework.db.NewFriend
import com.example.framework.event.EventManager
import com.example.framework.model.CommonBean
import com.example.framework.recycler.JssBaseViewHolder
import com.example.myapptaking.R
import ikidou.reflect.TypeBuilder
import java.lang.reflect.Type


/**
 * FileName: NewFriendActivity
 * Founder: LiuGuiLin
 * Profile: 新朋友
 */
class NewFriendFragment : SimpleListFragment<IMUser?>(), OnItemChildClickListener {

    companion object {

        const val Path = "NewFriendFragment"

        fun newInstance(): NewFriendFragment {
            val args = Bundle()

            val fragment = NewFriendFragment()
            fragment.arguments = args
            return fragment
        }
    }

    /**
     * 1.查询好友的申请列表
     * 2.通过适配器显示出来
     * 3.如果同意则添加对方为自己的好友
     * 4.并且发送给对方自定义的消息
     * 5.对方将我添加到好友列表
     */
//
    private var mmNewFriendMap = mutableMapOf<String, NewFriend?>()

    /**
     * 实际上这种问题还不是最高效的
     * 因为通过ID获取ImUser是存在网络延迟的
     * 我们可以通过另一种方式处理
     * 看ll_yes的点击事件
     */

    /**
     * 更新Item
     *
     * @param position
     * @param i
     */
    private fun updateItem(position: Int, i: Int) {
        val item = mData[position]
        val objectId=item?.objectId?:""
        val newFriend = mmNewFriendMap[objectId]
//
        //更新数据库
        LitePalHelper.getInstance().updateNewFriend(newFriend?.id, i)
//
        //更新本地的数据源
        newFriend?.isAgree = i
//
        mmNewFriendMap[objectId] = newFriend

        mAdapter?.notifyItemChanged(position)
    }




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isAlwaysRefresh = false
        isEnableLoadMore = false
    }

    override fun initView(view: View) {
        super.initView(view)
        mHeader.removeAllViews()
        addHeader(R.layout.comment_header_layout)
        val mToolbar: Toolbar? = mHeader.findViewById(R.id.mToolbar)
        mToolbar?.setTitle(R.string.text_user_info_add_friend)
        mToolbar?.setNavigationOnClickListener { onBackPressedSupport() }
        mAdapter.addChildClickViewIds(R.id.ll_yes, R.id.ll_no)
        mAdapter.setOnItemChildClickListener(this)
    }

    override fun getItemLayout(): Int {
        return R.layout.layout_new_friend_item
    }

    override fun convertItem(helper: JssBaseViewHolder?, item: IMUser?) {
        val newFriend = mmNewFriendMap[item?.objectId]
        helper?.setImageNetUrl(R.id.iv_photo, item?.photo, R.drawable.img_glide_load_error)
            ?.setImageResource(
                R.id.iv_sex,
                if (item?.isSex == true)
                    R.drawable.img_boy_icon
                else
                    R.drawable.img_girl_icon
            )
            ?.setText(R.id.tv_nickname, item?.nickName)
            ?.setText(
                R.id.tv_age,
                _mActivity.getString(R.string.text_search_age,item?.age.toString())
//                "${item?.age} ${_mActivity.getString(R.string.text_search_age)}"
            )
            ?.setText(R.id.tv_desc, item?.desc)
            ?.setText(R.id.tv_msg, newFriend?.msg)
            ?.setText(
                R.id.tv_result,
                when (newFriend?.isAgree) {
                    0 -> _mActivity.getString(R.string.text_new_friend_agree)
                    1 -> _mActivity.getString(R.string.text_new_friend_no_agree)
                    else -> ""
                }
            )
            ?.setVisible(R.id.ll_agree, newFriend?.isAgree == -1)
            ?.setVisible(R.id.tv_result, newFriend?.isAgree == 0 || newFriend?.isAgree == 1)

    }

    override fun getListType(): Type {
        return TypeBuilder.newInstance(CommonBean::class.java)
            .beginSubType(List::class.java)
            .addTypeParam(IMUser::class.java)
            .build()
    }

    override fun netRequest() {
        queryNewFriend()
    }

    /**
     * 查询新朋友
     */
    @SuppressLint("RestrictedApi")
    private fun queryNewFriend() {


        /**
         * 在子线程中获取好友申请列表然后在主线程中更新我们的UI
         * RxJava 线程调度
         */

        ArchTaskExecutor.getIOThreadExecutor().execute {
            val mNewFriendList = LitePalHelper.getInstance().queryNewFriend()
            val objectIds = mutableListOf<String>()
            val size = mNewFriendList.size
            mmNewFriendMap.clear()
            for (i in 0 until size) {
                val id = mNewFriendList[i]?.id ?: ""
                objectIds.add(id)
                mmNewFriendMap[id] = mNewFriendList[i]
            }

            ArchTaskExecutor.getMainThreadExecutor().execute {

                BmobManager.instance?.queryObjectIdUsers(objectIds,
                    object : FindListener<IMUser?>() {
                        override fun done(p0: MutableList<IMUser?>?, e: BmobException?) {
                            mData.clear()
                            if (e == null) {
                                val sizeQ = p0?.size ?: 0
                                for (i in 0 until sizeQ) {
                                    mData.add(p0?.get(i))
                                }
                                mAdapter?.notifyDataSetChanged()
                            }
                            if (mData.size <= 0) {
                                onFailed()
                            }

                            if (mSwipeRefreshLayout.isRefreshing){
                                mSwipeRefreshLayout.isRefreshing=false
                            }
                        }

                    })
            }
        }

    }

    override fun onItemChildClick(adapter: BaseQuickAdapter<*, *>, view: View, position: Int) {
        when (view.id) {
            R.id.ll_yes -> {
                //同意
                /**
                 * 1.同意则刷新当前的Item
                 * 2.将好友添加到自己的好友列表
                 * 3.通知对方我已经同意了
                 * 4.对方将我添加到好友列表
                 * 5.刷新好友列表
                 */
                val item = mAdapter.getItem(position)
                updateItem(position, 0)
                BmobManager.instance?.addFriend(item, object : SaveListener<String?>() {
                    override fun done(s: String?, e: BmobException?) {
                        if (e == null) {
                            //保存成功
                            //通知对方
                            CloudManager.getInstance().sendTextMessage(
                                "",
                                CloudManager.TYPE_ARGEED_FRIEND, item?.objectId
                            )
                            //刷新好友列表
                            EventManager.post(EventManager.FLAG_UPDATE_FRIEND_LIST)
                        }
                    }
                })
            }
            R.id.ll_no -> {
                //拒绝
                updateItem(position, 1)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mmNewFriendMap.clear()

    }

}