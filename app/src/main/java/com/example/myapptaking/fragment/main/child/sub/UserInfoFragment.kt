package com.example.myapptaking.fragment.main.child.sub

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.bmob.v3.exception.BmobException
import cn.bmob.v3.listener.FindListener
import com.example.framework.adapter.CommonAdapter
import com.example.framework.adapter.CommonViewHolder
import com.example.framework.adapter.OnBindDataListener
import com.example.framework.base.BaseUIFragment
import com.example.framework.bmob.BmobManager
import com.example.framework.bmob.Friend
import com.example.framework.bmob.IMUser
import com.example.framework.cloud.CloudManager
import com.example.framework.helper.GlideHelper
import com.example.framework.manager.DialogManager
import com.example.framework.utils.CommonUtils
import com.example.framework.utils.PermissionWrapper
import com.example.framework.view.DialogView
import com.example.myapptaking.R
import com.example.myapptaking.fragment.main.child.model.UserInfoModel
import com.example.myapptaking.fragment.main.child.sub.chat.ChatPanelFragment
import de.hdodenhof.circleimageview.CircleImageView
import kotlin.reflect.KParameter


/**
 * 1.根据传递过来的ID 查询用户信息 并且显示
 *   - 普通的信息
 *   - 构建一个RecyclerView 宫格
 * 2.建立好友关系模型
 *   与我有关系的是好友，
 *   1.在我的好友列表中
 *   2.同意了我的好友申请 BmobObject 建表
 *   3.查询所有的Friend表，其中user对应自己的列都是我的好友
 * 3.实现添加好友的提示框
 * 4.发送添加好友的消息
 *   1.自定义消息类型
 *   2.自定义协议
 *   发送文本消息 Content, 我们对文本进行处理：增加Json 定义一个标记来显示了
 *   点击提示框的发送按钮去发送
 * 5.接收好友的消息
 */

/**
 * FileName: UserInfoActivity
 * Founder: LiuGuiLin
 * Profile: 用户信息
 */
class UserInfoFragment : BaseUIFragment(), View.OnClickListener {

    companion object {
        fun newInstance(userId: String?): UserInfoFragment {
            val args = Bundle()
            args.putString("userId", userId)
            val fragment = UserInfoFragment()
            fragment.arguments = args
            return fragment
        }
    }

    private var userId: String? = ""


    private var mAddFriendDialogView: DialogView? = null
    private var et_msg: EditText? = null
    private var tv_cancel: TextView? = null
    private var tv_add_friend: TextView? = null

    private var ll_back: RelativeLayout? = null

    private var iv_user_photo: CircleImageView? = null
    private var tv_nickname: TextView? = null
    private var tv_desc: TextView? = null

    private var mUserInfoView: RecyclerView? = null
    private var mUserInfoAdapter: CommonAdapter<UserInfoModel>? = null
    private val mUserInfoList = mutableListOf<UserInfoModel>()

    private var btn_add_friend: Button? = null
    private var btn_chat: Button? = null
    private var btn_audio_chat: Button? = null
    private var btn_video_chat: Button? = null

    private var ll_is_friend: LinearLayout? = null

    //个人信息颜色
    private val mColor =
        intArrayOf(-0x77e16f01, -0x77ff0081, -0x77002900, -0x77009cb9, -0x770f7f80, -0x77bf1f30)


    private var imUser: IMUser? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //获取用户ID
        userId = arguments?.getString("userId")
    }

    override fun getLayout(): Any {
        return R.layout.fragment_user_info
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view)
    }

    private fun initView(view: View) {

        initAddFriendDialog()

        ll_back = view.findViewById(R.id.ll_back)
        iv_user_photo = view.findViewById(R.id.iv_user_photo)
        tv_nickname = view.findViewById(R.id.tv_nickname)
        tv_desc = view.findViewById(R.id.tv_desc)
        mUserInfoView = view.findViewById(R.id.mUserInfoView)
        btn_add_friend = view.findViewById(R.id.btn_add_friend)
        btn_chat = view.findViewById(R.id.btn_chat)
        btn_audio_chat = view.findViewById(R.id.btn_audio_chat)
        btn_video_chat = view.findViewById(R.id.btn_video_chat)
        ll_is_friend = view.findViewById(R.id.ll_is_friend)

        ll_back?.setOnClickListener(this)
        btn_add_friend?.setOnClickListener(this)
        btn_chat?.setOnClickListener(this)
        btn_audio_chat?.setOnClickListener(this)
        btn_video_chat?.setOnClickListener(this)
        iv_user_photo?.setOnClickListener(this)

        //列表
        mUserInfoAdapter = CommonAdapter<UserInfoModel>(
            mUserInfoList,
            object : OnBindDataListener<UserInfoModel?> {
                override fun onBindViewHolder(
                    model: UserInfoModel?,
                    viewHolder: CommonViewHolder?,
                    type: Int,
                    position: Int
                ) {
                    //viewHolder.setBackgroundColor(R.id.ll_bg, model.getBgColor());
                    viewHolder?.getView<View>(R.id.ll_bg)
                        ?.setBackgroundColor(model?.bgColor ?: R.color.PrimaryColor)

                    viewHolder?.setText(R.id.tv_type, model?.title)
                    viewHolder?.setText(R.id.tv_content, model?.content)
                }

                override fun getLayoutId(type: Int): Int {
                    return R.layout.layout_user_info_item
                }
            })
        mUserInfoView?.layoutManager = GridLayoutManager(_mActivity, 3)
        mUserInfoView?.adapter = mUserInfoAdapter

        queryUserInfo()
    }

    @SuppressLint("SetTextI18n")
    private fun initAddFriendDialog() {
        mAddFriendDialogView =
            DialogManager.getInstance().initView(_mActivity, R.layout.dialog_send_friend)

        et_msg = mAddFriendDialogView?.findViewById(R.id.et_msg)
        tv_cancel = mAddFriendDialogView?.findViewById(R.id.tv_cancel)
        tv_add_friend = mAddFriendDialogView?.findViewById(R.id.tv_add_friend)

        et_msg?.setText("${_mActivity.getString(R.string.text_me_info_tips)}  ${BmobManager.instance?.user?.nickName}")

        tv_cancel?.setOnClickListener(this)
        tv_add_friend?.setOnClickListener(this)
    }

    /**
     * 查询用户信息
     */
    private fun queryUserInfo() {
        if (TextUtils.isEmpty(userId)) {
            return
        }

        //查询用户信息
        BmobManager.instance?.queryObjectIdUser(userId, object : FindListener<IMUser?>() {
            override fun done(list: List<IMUser?>?, e: BmobException?) {
                if (e == null) {
                    if (CommonUtils.isEmpty(list)) {
                        imUser = list?.get(0)
                        updateUserInfo(imUser)
                    }
                }
            }
        })

        if (userId == BmobManager.instance?.user?.objectId) {
            btn_add_friend?.visibility = View.GONE
            ll_is_friend?.visibility = View.GONE
            return
        }

        //判断好友关系
        BmobManager.instance?.queryMyFriends(object : FindListener<Friend?>() {
            override fun done(list: List<Friend?>?, e: BmobException?) {
                if (e == null) {
                    if (CommonUtils.isEmpty(list)) {
                        //你有一个好友列表
                        val size = list?.size ?: 0
                        for (i in 0 until size) {
                            val friend = list?.get(i)
                            //判断这个对象中的id是否跟我目前的userId相同
                            if (friend?.friendUser?.objectId == userId) {
                                //你们是好友关系
                                btn_add_friend?.visibility = View.GONE
                                ll_is_friend?.visibility = View.VISIBLE
                            }
                        }
                    }
                }
            }
        })
    }

    /**
     * 更新用户信息
     *
     * @param imUser
     */
    private fun updateUserInfo(imUser: IMUser?) {
        //设置基本属性
        GlideHelper.loadUrl(
            _mActivity, imUser?.photo,
            iv_user_photo
        )
        tv_nickname?.text = imUser?.nickName
        tv_desc?.text = imUser?.desc

        //性别 年龄 生日 星座 爱好 单身状态
        addUserInfoModel(
            mColor[0],
            getString(R.string.text_me_info_sex),
            if (imUser?.isSex == true)
                _mActivity.getString(R.string.text_me_info_boy)
            else
                _mActivity.getString(R.string.text_me_info_girl)
        )
        addUserInfoModel(
            mColor[1],
            _mActivity.getString(R.string.text_me_info_age),
            _mActivity.getString(R.string.text_search_age, imUser?.age.toString())
//            "${imUser?.age} ${_mActivity.getString(R.string.text_search_age)}"
        )
        addUserInfoModel(
            mColor[2],
            _mActivity.getString(R.string.text_me_info_birthday), imUser?.birthday
        )
        addUserInfoModel(
            mColor[3],
            _mActivity.getString(R.string.text_me_info_constellation),
            imUser?.constellation
        )
        addUserInfoModel(
            mColor[4],
            _mActivity.getString(R.string.text_me_info_hobby), imUser?.hobby
        )
        addUserInfoModel(
            mColor[5],
            _mActivity.getString(R.string.text_me_info_status), imUser?.status
        )

        //刷新数据
        mUserInfoAdapter?.notifyDataSetChanged()
    }

    /**
     * 添加数据
     *
     * @param color
     * @param title
     * @param content
     */
    private fun addUserInfoModel(color: Int, title: String?, content: String?) {
        val model = UserInfoModel()
        model.bgColor = color
        model.title = title
        model.content = content
        mUserInfoList.add(model)
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tv_add_friend -> {
                var msg = et_msg?.text.toString().trim { it <= ' ' }
                if (TextUtils.isEmpty(msg)) {
                    msg = _mActivity.getString(R.string.text_user_info_add_friend)
                }
                CloudManager.getInstance().sendTextMessage(
                    msg,
                    CloudManager.TYPE_ADD_FRIEND, userId
                )
                DialogManager.getInstance().hide(mAddFriendDialogView)
                Toast.makeText(
                    _mActivity,
                    _mActivity.getString(R.string.text_user_resuest_succeed),
                    Toast.LENGTH_SHORT
                ).show()
            }
            R.id.tv_cancel -> DialogManager.getInstance().hide(mAddFriendDialogView)
            R.id.ll_back -> onBackPressedSupport()
            R.id.iv_user_photo -> {
                start(ImagePreviewFragment.newInstance(true, imUser?.photo))
            }
            R.id.btn_add_friend -> DialogManager.getInstance().show(mAddFriendDialogView)
            R.id.btn_chat -> {
                start(
                    ChatPanelFragment.newInstance(
                        userId,
                        imUser?.nickName,
                        imUser?.photo
                    )
                )

            }
            R.id.btn_audio_chat ->                 //窗口权限
                if (!checkWindowPermissions()) {
                    requestWindowPermissions()
                } else {
                    PermissionWrapper.requiredPermission(this) {
                        if (it) {
                            CloudManager.getInstance().startAudioCall(_mActivity, userId)
                        }
                    }

                }
            R.id.btn_video_chat -> if (!checkWindowPermissions()) {
                requestWindowPermissions()
            } else {
                PermissionWrapper.requiredPermission(this) {
                    CloudManager.getInstance().startVideoCall(_mActivity, userId)
                }

            }
        }
    }
}