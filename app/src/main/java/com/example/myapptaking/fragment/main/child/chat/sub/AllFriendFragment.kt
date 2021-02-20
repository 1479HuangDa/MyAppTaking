package com.example.myapptaking.fragment.main.child.chat.sub

import android.os.Bundle
import android.view.View
import cn.bmob.v3.exception.BmobException
import cn.bmob.v3.listener.FindListener
import com.chad.library.adapter.base.BaseQuickAdapter
import com.example.framework.base.SimpleListFragment
import com.example.framework.bmob.BmobManager
import com.example.framework.bmob.Friend
import com.example.framework.bmob.IMUser
import com.example.framework.event.EventManager
import com.example.framework.event.MessageEvent
import com.example.framework.model.CommonBean
import com.example.framework.recycler.JssBaseViewHolder
import com.example.framework.utils.LogUtils
import com.example.myapptaking.R
import com.example.myapptaking.fragment.main.child.model.AllFriendModel
import com.example.myapptaking.fragment.main.child.sub.UserInfoFragment
import ikidou.reflect.TypeBuilder
import java.lang.reflect.Type

/**
 * FileName: AllFriendFragment
 * Founder: HuangDa
 * Profile: 所有联系人
 */
class AllFriendFragment : SimpleListFragment<AllFriendModel>(), LabInfo {

    companion object {
        fun newInstance(): AllFriendFragment {
            val args = Bundle()
            val fragment = AllFriendFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isEnableLoadMore = false
    }

    override var labelTitle = R.string.text_chat_tab_title_3

    override fun getItemLayout(): Int {
        return R.layout.fragment_all_friend
    }

    override fun initView(view: View) {
        super.initView(view)
        list_empty_view.removeAllViews()
        addEmptyView(R.layout.layout_empty_view)
    }

    override fun convertItem(helper: JssBaseViewHolder?, item: AllFriendModel?) {
        helper?.setImageNetUrl(R.id.iv_photo, item?.url, R.drawable.img_glide_load_error)
            ?.setText(R.id.tv_nickname, item?.nickName)
            ?.setImageResource(
                R.id.iv_sex,
                if (item?.isSex == true)
                    R.drawable.img_boy_icon
                else
                    R.drawable.img_girl_icon
            )
            ?.setText(R.id.tv_desc, item?.desc)

    }

    override fun onItemClick(adapter: BaseQuickAdapter<*, *>, view: View, position: Int) {
        super.onItemClick(adapter, view, position)
        val item = mAdapter.getItem(position)
        start(UserInfoFragment.newInstance(item?.userId))
    }

    override fun getListType(): Type {
        return TypeBuilder.newInstance(CommonBean::class.java)
            .beginSubType(List::class.java)
            .addTypeParam(AllFriendModel::class.java).endSubType().build()
    }

    override fun netRequest() {
        queryMyFriends()
    }

    override fun handMessageEvent(event: MessageEvent) {
        when (event.type) {
            EventManager.FLAG_UPDATE_FRIEND_LIST -> if (!mSwipeRefreshLayout.isRefreshing) {
                queryMyFriends()
            }
        }
    }

    private fun queryMyFriends() {
        BmobManager.instance?.queryMyFriends(object : FindListener<Friend?>() {
            override fun done(list: MutableList<Friend?>?, e: BmobException?) {
                if (mSwipeRefreshLayout.isRefreshing) {
                    mSwipeRefreshLayout.isRefreshing = false
                }
                mData.clear()
                if (e == null && list?.isNotEmpty() == true) {
                    val listIds = mutableListOf<String>()
                    for (item in list) {
                        listIds.add(item?.friendUser?.objectId?:"")
                    }
                    LogUtils.i("list:" + list.size)
                    LogUtils.i("listIds = $listIds")
                    BmobManager.instance?.queryObjectIdUsers(listIds,
                        object : FindListener<IMUser?>() {
                            override fun done(list_0: MutableList<IMUser?>?, e_0: BmobException?) {
                                LogUtils.i("e_0 = ${e_0?.message}")
                                LogUtils.i("list_0 = ${list_0.toString()}")
                                if (list_0?.isNotEmpty() == true) {
                                    LogUtils.i("list_0:" + list_0.size)
                                    for (imUser in list_0) {
                                        val model = AllFriendModel()
                                        model.userId = imUser?.objectId
                                        model.url = imUser?.photo
                                        model.nickName = imUser?.nickName
                                        model.isSex = imUser?.isSex ?: true
                                        model.desc =
                                            _mActivity.getString(R.string.text_all_friend_desc) + imUser?.desc
                                        mData.add(model)
                                    }
                                    mAdapter.notifyDataSetChanged()
                                    if (mData.isEmpty()) {
                                        onFailed()
                                    }
                                } else {
                                    LogUtils.i(e?.message)
                                    onFailed()
                                }
                            }

                        })
                } else {
                    LogUtils.i(e?.message)
                    onFailed()
                }
            }
        })
    }
}