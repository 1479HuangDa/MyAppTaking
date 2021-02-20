package com.example.myapptaking.fragment.main.child.sub.add_fried

import android.Manifest
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.widget.Toolbar
import cn.bmob.v3.exception.BmobException
import cn.bmob.v3.listener.FindListener
import com.chad.library.adapter.base.BaseQuickAdapter
import com.example.framework.base.MultiListFragment
import com.example.framework.bmob.BmobManager
import com.example.framework.bmob.IMUser
import com.example.framework.manager.KeyWordManager
import com.example.framework.model.CommonBean
import com.example.framework.recycler.multi22.JssBaseMultiAdapter
import com.example.framework.utils.CommonUtils
import com.example.framework.utils.LogUtils
import com.example.myapptaking.R
import com.example.myapptaking.fragment.main.child.model.AddFriendModel
import com.example.myapptaking.fragment.main.child.sub.ContactFriendFragment
import com.example.myapptaking.fragment.main.child.sub.UserInfoFragment
import com.example.myapptaking.fragment.main.child.sub.add_fried.provider.AddFriendTypeContentProvider
import com.example.myapptaking.fragment.main.child.sub.add_fried.provider.AddFriendTypeTitleProvider
import com.example.myapptaking.fragment.main.child.sub.chat.model.ChatPanelEntity
import com.permissionx.guolindev.PermissionX
import ikidou.reflect.TypeBuilder
import java.lang.reflect.Type

/**
 * FileName: AddFriendActivity
 * Founder: HuangDa
 * Profile: 添加好友
 */
class AddFriedFragment : MultiListFragment<AddFriendModel>(), View.OnClickListener {

    companion object {

        fun newInstance(): AddFriedFragment {
            val args = Bundle()
            val fragment = AddFriedFragment()
            fragment.arguments = args
            return fragment
        }
    }

    /**
     * 1.模拟用户数据
     * 2.根据条件查询
     * 3.推荐好友
     */

    private var et_phone: EditText? = null
    private var iv_search: ImageView? = null
    private var ll_to_contact: LinearLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isEnableLoadMore = false
    }


    override fun initView(view: View) {
        super.initView(view)
        mHeader?.removeAllViews()
        addHeader(R.layout.fragment_add_fried_header)

        list_empty_view?.removeAllViews()
        addEmptyView(R.layout.layout_empty_view)

        val mToolbar: Toolbar? = view.findViewById(R.id.mToolbar)
        mToolbar?.setTitle(R.string.text_user_add_friend)
        mToolbar?.setNavigationOnClickListener { onBackPressedSupport() }

        et_phone = view.findViewById(R.id.et_phone)
        ll_to_contact = view.findViewById(R.id.ll_to_contact)
        iv_search = view.findViewById(R.id.iv_search)

        ll_to_contact?.setOnClickListener(this)
        iv_search?.setOnClickListener(this)

    }


    override fun  addOnListItemProvider(mAdapter: JssBaseMultiAdapter<AddFriendModel?>?) {
        mAdapter?.addItemProvider(AddFriendTypeTitleProvider())
        mAdapter?.addItemProvider(AddFriendTypeContentProvider())
    }


    override fun onItemClick(adapter: BaseQuickAdapter<*, *>, view: View, position: Int) {
        super.onItemClick(adapter, view, position)
        val item = mAdapter?.getItem(position)
        start(UserInfoFragment.newInstance(item?.userId))
    }

    override fun netRequest() {

//        if (!check(false)) return
//
//        queryPhoneUser()
        pushUser("")
    }

//    override fun getListType(): Type {
//        return TypeBuilder.newInstance(CommonBean::class.java)
//            .beginSubType(List::class.java)
//            .addTypeParam(AddFriendModel::class.java).endSubType().build()
//    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ll_to_contact -> {
                PermissionX.init(this)
                    .permissions(listOf(Manifest.permission.READ_CONTACTS))
                    .onExplainRequestReason { scope, deniedList ->
                        val msg = "获取联系人权限"
                        val positive = "确定"
                        val negative = "取消"
                        scope.showRequestReasonDialog(deniedList, msg, positive, negative)
                    }.onForwardToSettings { scope, deniedList ->
                        val msg = "获取联系人权限"
                        val positive = "确定"
                        val negative = "取消"
                        scope.showForwardToSettingsDialog(deniedList, msg, positive, negative)
                    }.request { allGranted, _, deniedList ->
                        if (!allGranted || deniedList.isNotEmpty()) {
                            Toast.makeText(_mActivity, "获取联系人权限失败", Toast.LENGTH_SHORT).show()
                        } else {
                            start(ContactFriendFragment.newInstance())
                        }

                    }
            }
            R.id.iv_search -> {
                KeyWordManager.getInstance().hideKeyWord(_mActivity)
                queryPhoneUser()
            }
        }
    }

    private fun check(show: Boolean): Boolean {
        //1.获取电话号码
        val phone = et_phone?.text.toString().trim { it <= ' ' }
        if (TextUtils.isEmpty(phone)) {
            if (show) {
                Toast.makeText(
                    _mActivity, R.string.text_login_phone_null,
                    Toast.LENGTH_SHORT
                ).show()
            }
            onFailed()
            return false
        }

        //2.过滤自己
        val phoneNumber = BmobManager.instance?.user?.mobilePhoneNumber
        LogUtils.i("phoneNumber:$phoneNumber")
        if (phone == phoneNumber) {
            if (show) {
                Toast.makeText(
                    _mActivity,
                    R.string.text_add_friend_no_me,
                    Toast.LENGTH_SHORT
                ).show()
            }
            onFailed()
            return false
        }

        return true
    }

    private fun queryPhoneUser() {
        if (!check(true)) return

        val phone = et_phone?.text.toString().trim { it <= ' ' }

        //3.查询
        BmobManager.instance?.queryPhoneUser(phone, object : FindListener<IMUser?>() {
            override fun done(list: List<IMUser?>, e: BmobException?) {
                KeyWordManager.getInstance().hideKeyWord(_mActivity)
                if (e != null) {
                    onFailed()
                    return
                }
                if (CommonUtils.isEmpty(list)) {
                    val imUser = list[0]

                    //每次你查询有数据的话则清空

                    mAdapter?.clears()

                    addTitle(R.string.text_add_friend_title)
                    addContent(imUser)
                    mAdapter?.notifyDataSetChanged()

                    if (mSwipeRefreshLayout?.isRefreshing==true) {
                        mSwipeRefreshLayout?.isRefreshing = false
                    }

                    //推荐
                    pushUser(phone)
                } else {
                    //显示空数据
                    onFailed()
                }
            }
        })
    }

    /**
     * 推荐好友
     *
     * @param phone 过滤所查询的电话号码
     */
    private fun pushUser(phone: String) {
        //查询所有的好友 取100个
        BmobManager.instance?.queryAllUser(object : FindListener<IMUser?>() {
            override fun done(list: List<IMUser?>, e: BmobException?) {
                if (e == null) {
                    if (CommonUtils.isEmpty(list)) {
                        addTitle(R.string.text_add_friend_content)
                        val num = if (list.size <= 100) list.size else 100
                        for (i in 0 until num) {
                            //也不能自己推荐给自己
                            val phoneNumber = BmobManager.instance?.user?.mobilePhoneNumber
                            if (list[i]?.mobilePhoneNumber == phoneNumber) {
                                //跳过本次循环
                                continue
                            }
                            //也不能查询到所查找的好友
                            if (list[i]?.mobilePhoneNumber == phone) {
                                //跳过本次循环
                                continue
                            }
                            addContent(list[i])
                        }
                        mAdapter?.notifyDataSetChanged()
                        if (mSwipeRefreshLayout?.isRefreshing==true) {
                            mSwipeRefreshLayout?.isRefreshing = false
                        }
                    }
                } else {
                    onFailed()
                }
            }
        })
    }

    private fun addTitle(@StringRes mTitle: Int) {
        val model = AddFriendModel()
        model.type = AddFriendModel.TYPE_TITLE
        model.title = _mActivity.getString(mTitle)
        mData.add(model)
    }

    private fun addContent(imUser: IMUser?) {
        val model = AddFriendModel()
        model.type = AddFriendModel.TYPE_CONTENT
        model.userId = imUser?.objectId
        model.photo = imUser?.photo
        model.isSex = imUser?.isSex ?: false
        model.age = imUser?.age ?: 0
        model.nickName = imUser?.nickName
        model.desc = imUser?.desc
        mData.add(model)
    }

    override val listType: Type
        get() = TypeBuilder.newInstance(CommonBean::class.java)
            .beginSubType(List::class.java)
            .addTypeParam(AddFriendModel::class.java).endSubType().build()

}