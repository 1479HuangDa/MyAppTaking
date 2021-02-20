package com.example.myapptaking.fragment.main.child.sub

import android.database.Cursor
import android.os.Bundle
import android.provider.ContactsContract
import android.view.View
import androidx.appcompat.widget.Toolbar
import cn.bmob.v3.exception.BmobException
import cn.bmob.v3.listener.FindListener
import com.chad.library.adapter.base.BaseQuickAdapter
import com.example.framework.base.SimpleListFragment
import com.example.framework.bmob.BmobManager
import com.example.framework.bmob.IMUser
import com.example.framework.bmob.PrivateSet
import com.example.framework.model.CommonBean
import com.example.framework.recycler.JssBaseViewHolder
import com.example.framework.utils.CommonUtils
import com.example.framework.utils.LogUtils
import com.example.myapptaking.R
import com.example.myapptaking.fragment.main.child.model.AddFriendModel
import ikidou.reflect.TypeBuilder
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.lang.reflect.Type

/**
 * FileName: ContactFirendActivity
 * Founder: LiuGuiLin
 * Profile: 从通讯录导入
 */
class ContactFriendFragment : SimpleListFragment<AddFriendModel>() {

    companion object {
        fun newInstance(): ContactFriendFragment {
            val args = Bundle()
            val fragment = ContactFriendFragment()
            fragment.arguments = args
            return fragment
        }
    }

    private var disposable: Disposable? = null

    //    private var mContactView: RecyclerView? = null
    private val mContactMap = mutableMapOf<String, String>()
//
//    private var mContactAdapter: CommonAdapter<AddFriendModel>? = null
//    private val mList = mutableListOf<AddFriendModel>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isEnableLoadMore = false
    }


    override fun initView(view: View) {
        super.initView(view)
        mHeader.removeAllViews()
        addHeader(R.layout.comment_header_layout)
        val mToolbar: Toolbar? = view.findViewById(R.id.mToolbar)
        mToolbar?.setTitle(R.string.text_add_friend_contact)
        mToolbar?.setNavigationOnClickListener { onBackPressedSupport() }

        list_empty_view.removeAllViews()
        addEmptyView(R.layout.layout_empty_view)
    }


    override fun convertItem(helper: JssBaseViewHolder?, item: AddFriendModel?) {
        //设置头像
        helper?.setImageNetUrl(R.id.iv_photo, item?.photo, R.drawable.img_glide_load_error)
            //设置性别
            ?.setImageResource(
                R.id.iv_sex,
                if (item?.isSex == true)
                    R.drawable.img_boy_icon
                else
                    R.drawable.img_girl_icon
            )
            //设置昵称
            ?.setText(R.id.tv_nickname, item?.nickName)
            //年龄
            ?.setText(
                R.id.tv_age,
                _mActivity.getString(R.string.text_search_age,item?.age.toString())
//                "${item?.age}${_mActivity.getString(R.string.text_search_age)}"
            )
            //设置描述
            ?.setText(R.id.tv_desc, item?.desc)
            ?.setText(R.id.tv_contact_name, item?.contactName)
            ?.setText(R.id.tv_contact_phone, item?.contactPhone)
            ?.setViewVisible(R.id.ll_contact_info, item?.isContact == true)

    }

    override fun getListType(): Type {
        return TypeBuilder.newInstance(CommonBean::class.java)
            .beginSubType(List::class.java)
            .addTypeParam(AddFriendModel::class.java).endSubType().build()
    }

    override fun onDestroy() {
        super.onDestroy()
        destroyDisposable()
    }

    private fun destroyDisposable() {
        if (disposable != null) {
            if (disposable?.isDisposed == false) {
                disposable?.dispose()
            }
        }
    }

    override fun onBackPressedSupport(): Boolean {
        destroyDisposable()
        return super.onBackPressedSupport()
    }

    override fun getItemLayout(): Int {
        return R.layout.layout_search_user_item
    }

    override fun netRequest() {
        loadUser()
    }

    override fun onItemClick(adapter: BaseQuickAdapter<*, *>, view: View, position: Int) {
        super.onItemClick(adapter, view, position)
        val item = mAdapter.getItem(position)
        start(UserInfoFragment.newInstance(item?.userId))
    }


    /**
     * 加载用户
     */
    private fun loadUser() {


        /**
         * 1.拿到用户的联系人列表
         * 2.查询我们的PrivateSet
         * 3.过滤一遍联系人列表
         * 4.去显示
         */
        /**
         * 1.拿到用户的联系人列表
         * 2.查询我们的PrivateSet
         * 3.过滤一遍联系人列表
         * 4.去显示
         */

//        //加载联系人
//        loadContact()
//
//        //查询我们的PrivateSet
//
//        BmobManager.instance?.queryPrivateSet(object : FindListener<PrivateSet?>() {
//            override fun done(list: List<PrivateSet?>?, e: BmobException?) {
//                if (e == null) {
//                    emitter.onNext(list)
//                    emitter.onComplete()
//                }
//            }
//        })

        disposable = Observable.create<List<PrivateSet?>?> { emitter ->

            //加载联系人
            loadContact()

            //查询我们的PrivateSet
            BmobManager.instance?.queryPrivateSet(object : FindListener<PrivateSet?>() {
                override fun done(list: List<PrivateSet?>?, e: BmobException?) {
                    if (e == null) {
                        emitter.onNext(list ?: emptyList())
                        emitter.onComplete()
                    }else{
                        emitter.onNext( emptyList())
                        emitter.onComplete()
                    }
                }
            })
        }
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { privateSets ->
                fixprivateSets(privateSets)
                //这里判断无数据
                if (mSwipeRefreshLayout.isRefreshing) {
                    mSwipeRefreshLayout.isRefreshing = false
                }
            }
    }

    /**
     * 解析私有库的内容进行联系人过滤
     *
     * @param privateSets
     */
    private fun fixprivateSets(privateSets: List<PrivateSet?>?) {

        LogUtils.i("fixprivateSets:" + privateSets?.size)

        val userListPhone: MutableList<String?> = ArrayList()

        if (CommonUtils.isEmpty(privateSets)) {
            val size = privateSets?.size ?: 0
            for (i in 0 until size) {
                val sets: PrivateSet? = privateSets?.get(i)
                val phone = sets?.phone
                userListPhone.add(phone)
            }
        }

        //拿到了后台所有字段的电话号码
        if (mContactMap.isNotEmpty()) {
            val values = mutableListOf<String>()
            for ((key, value) in mContactMap) {
                //过滤：判断你当前的号码在私有库是否存在
                if (userListPhone.contains(value)) {
                    continue
                }
                LogUtils.i("load...")
                values.add(value)
                BmobManager.instance?.queryPhoneUser(value, object : FindListener<IMUser?>() {
                    override fun done(list: List<IMUser?>, e: BmobException?) {
                        if (e == null) {
                            if (CommonUtils.isEmpty(list)) {
                                val imUser = list[0]
                                addContent(imUser, key, value)
                            }
                        }
                    }
                })
            }


        }
    }

    /**
     * 添加内容
     *
     * @param imUser
     */
    private fun addContent(imUser: IMUser?, name: String, phone: String) {
        val model = AddFriendModel()
        model.type = AddFriendModel.TYPE_CONTENT
        model.userId = imUser?.objectId
        model.photo = imUser?.photo
        model.isSex = imUser?.isSex ?: true
        model.age = imUser?.age ?: 0
        model.nickName = imUser?.nickName
        model.desc = imUser?.desc

        model.isContact = true
        model.contactName = name
        model.contactPhone = phone

        mData.add(model)
        mAdapter?.notifyDataSetChanged()
    }

    /**
     * 加载联系人
     */
    private fun loadContact() {
        val cursor: Cursor? = _mActivity.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null
        )
        var name: String
        var phone: String
        while (cursor?.moveToNext() == true) {
            name = cursor.getString(
                cursor.getColumnIndex(
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
                )
            )
            phone = cursor.getString(
                cursor.getColumnIndex(
                    ContactsContract.CommonDataKinds.Phone.NUMBER
                )
            )
            LogUtils.i("name:$name phone:$phone")
            phone = phone.replace(" ", "").replace("-", "")
            mContactMap[name] = phone
        }

    }
}