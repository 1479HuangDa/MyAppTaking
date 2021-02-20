package com.example.myapptaking.fragment.main.child.sub.chat

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.effective.android.panel.PanelSwitchHelper
import com.example.framework.base.BaseLazyDataBindingFragment
import com.example.framework.cloud.CloudManager
import com.example.framework.glidmodule.GlideApp
import com.example.framework.global.AppGlobals
import com.example.framework.helper.FileHelper
import com.example.framework.manager.KeyWordManager
import com.example.framework.manager.MapManager
import com.example.framework.utils.LogUtils
import com.example.framework.utils.PixUtils
import com.example.myapptaking.R
import com.example.myapptaking.databinding.FragmentChatPanelBinding
import com.example.myapptaking.fragment.main.child.location.LocationNewFragment
import com.example.myapptaking.fragment.main.child.sub.chat.model.ChatWithTargetModel
import com.permissionx.guolindev.PermissionX
import com.permissionx.guolindev.callback.ExplainReasonCallback
import com.permissionx.guolindev.callback.ForwardToSettingsCallback
import com.permissionx.guolindev.request.ExplainScope
import com.permissionx.guolindev.request.ForwardScope
import java.io.File


/**
 * FileName: ChatActivity
 * Founder: LiuGuiLin
 * Profile: 聊天
 */
class ChatPanelFragment : BaseLazyDataBindingFragment<FragmentChatPanelBinding>(),
    View.OnClickListener {

    companion object {

        const val Path = "ChatPanelFragment"

        private const val LOCATION_REQUEST_CODE = 1888

        private const val CHAT_INFO_REQUEST_CODE = 1889

        fun newInstance(userId: String?, nickName: String?, photo: String?): ChatPanelFragment {
            val args = Bundle()
            args.putString("userId", userId)
            args.putString("nickName", nickName)
            args.putString("photo", photo)

            val fragment = ChatPanelFragment()
            fragment.arguments = args
            return fragment
        }
    }

    //对方用户信息
    private var yourUserId: String? = null
    private var yourUserName: String? = null
    private var yourUserPhoto: String? = null

    private var mChatList: ChatListFragment? = null

    private var mHelper: PanelSwitchHelper? = null

    //图片文件
    private var uploadFile: File? = null

    private var unfilledHeight = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        yourUserId = arguments?.getString("userId")
        yourUserName = arguments?.getString("nickName")
        yourUserPhoto = arguments?.getString("photo")
    }


    override fun layoutId(): Int {
        return R.layout.fragment_chat_panel
    }


    override fun lazyInit() {
//        mChatList=ChatListFragment.newInstance(yourUserId, yourUserName, yourUserPhoto)
        mChatList = ChatListFragment.newInstance(
            ChatWithTargetModel(
                yourUserId, yourUserName, yourUserPhoto
            )
        )
        mChatList?.mHelper = mHelper
        mChatList?.unfilledHeight = object : ChatListFragment.UnfilledHeight {


            override fun setUnfilledHeight(newUnfilledHeight: Int) {
                unfilledHeight = newUnfilledHeight
            }

        }




        loadRootFragment(R.id.list_panel, mChatList)
    }

    override fun initView(view: View) {

        val mToolbar: Toolbar = view.findViewById(R.id.mToolbar)
        mToolbar.title = yourUserName
        mToolbar.titleMarginStart = PixUtils.dp2px(10)
        mToolbar.setNavigationOnClickListener { onBackPressedSupport() }
        GlideApp.with(this)
            .load(yourUserPhoto)
            .circleCrop()
            .override(PixUtils.dp2px(32))
            .into(object : CustomTarget<Drawable>() {
                override fun onResourceReady(
                    resource: Drawable,
                    transition: Transition<in Drawable>?
                ) {
                    mToolbar.logo = resource
                }

                override fun onLoadCleared(placeholder: Drawable?) {

                }
            })

        if (mHelper == null) {
            mHelper = PanelSwitchHelper.Builder(this)
                .contentScrollOutsideEnable(true)

                .addContentScrollMeasurer {
                    getScrollDistance {
                        it - unfilledHeight
                    }

                    getScrollViewId { R.id.list_panel }
                }
                .build()
        }

        viewBinder.chatPanelFooterOpe.onMsgClick = this
    }


    override fun onBackPressedSupport(): Boolean {
        if (mHelper?.hookSystemBackByPanelSwitcher() == true) {
            return true
        }

        pop()

        return true
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_send_msg -> {
                val inputText: String =
                    viewBinder.chatPanelFooterOpe.etInputMsg.text.toString().trim { it <= ' ' }
                if (TextUtils.isEmpty(inputText)) {
                    return
                }
                CloudManager.getInstance().sendTextMessage(
                    inputText,
                    CloudManager.TYPE_TEXT, yourUserId
                )
                viewBinder.chatPanelFooterOpe.etInputMsg.setText("")
                KeyWordManager.getInstance().hideKeyWord(_mActivity)
            }

            R.id.ll_camera -> {

                PermissionX.init(this)
                    .permissions(Manifest.permission.CAMERA)
                    .onExplainRequestReason(ExplainReasonCallback { scope: ExplainScope, deniedList: List<String?>? ->
                        val msg = "获取相机权限"
                        val positive = "确定"
                        val negative = "取消"
                        scope.showRequestReasonDialog(deniedList, msg, positive, negative)
                    })
                    .onForwardToSettings(ForwardToSettingsCallback { scope: ForwardScope, deniedList: List<String?>? ->
                        val msg = "获取相机权限"
                        val positive = "确定"
                        val negative = "取消"
                        scope.showForwardToSettingsDialog(deniedList, msg, positive, negative)
                    })
                    .request { allGranted: Boolean, grantedList: List<String?>?, deniedList: List<String?> ->
                        if (!allGranted || !deniedList.isEmpty()) {
                            Toast.makeText(
                                AppGlobals.getApplication(),
                                "获取相机权限失败", Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            //跳转到相机
                            FileHelper.getInstance().toCamera(this)
                        }
                    }

//                FileHelper.getInstance().toCamera(this)
            }
            R.id.ll_location -> {
//                LocationFragment.startRes(
//                    this,
//                    false,
//                    0.0,
//                    0.0,
//                    "",
//                    LOCATION_REQUEST_CODE
//                )
                LocationNewFragment.startRes(this,LOCATION_REQUEST_CODE)
//                start(LocationNewFragment.newInstance())
            }
            R.id.ll_pic -> {

                PermissionX.init(this)
                    .permissions(
                        listOf(
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        )
                    )
                    .onExplainRequestReason { scope: ExplainScope, deniedList: List<String?>? ->
                        val msg = "获取内部存储权限"
                        val positive = "确定"
                        val negative = "取消"
                        scope.showRequestReasonDialog(deniedList, msg, positive, negative)
                    }
                    .onForwardToSettings { scope: ForwardScope, deniedList: List<String?>? ->
                        val msg = "获取内部存储权限"
                        val positive = "确定"
                        val negative = "取消"
                        scope.showForwardToSettingsDialog(deniedList, msg, positive, negative)
                    }
                    .request { allGranted: Boolean, grantedList: List<String?>?, deniedList: List<String?> ->
                        if (!allGranted || !deniedList.isEmpty()) {
                            Toast.makeText(
                                AppGlobals.getApplication(),
                                "获取内部存储权限失败", Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            //跳转到相机
                            FileHelper.getInstance().toAlbum(this)
                        }
                    }

//                FileHelper.getInstance().toAlbum(this)
            }
        }
    }

    override fun onFragmentResult(requestCode: Int, resultCode: Int, data: Bundle?) {
        super.onFragmentResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == LOCATION_REQUEST_CODE) {
                val la: Double = data?.getDouble("la")?:0.0
                val lo: Double = data?.getDouble("lo")?:0.0
                val address: String = data?.getString("address") ?:""

                LogUtils.i("la:$la")
                LogUtils.i("lo:$lo")
                LogUtils.i("address:$address")

                if (TextUtils.isEmpty(address)) {
                    MapManager.getInstance()
                        .poi2address(
                            la, lo
                        ) {  //发送位置消息
                            CloudManager.getInstance()
                                .sendLocationMessage(yourUserId, la, lo, it)
//                            addLocation(1, la, lo, address)
                        }
                } else {
                    //发送位置消息
                    CloudManager.getInstance().sendLocationMessage(yourUserId, la, lo, address)
//                    addLocation(1, la, lo, address)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == FileHelper.CAMEAR_REQUEST_CODE) {
                uploadFile = FileHelper.getInstance().tempFile
            } else if (requestCode == FileHelper.ALBUM_REQUEST_CODE) {
                val uri = data?.data
                if (uri != null) {
                    //String path = uri.getPath();
                    //获取真实的地址
                    val path = FileHelper.getInstance().getRealPathFromURI(_mActivity, uri)
                    LogUtils.e("path:$path");
                    if (!TextUtils.isEmpty(path)) {
                        uploadFile = File(path)
                    }
                }
            } else  if (requestCode == CHAT_INFO_REQUEST_CODE) {
                onBackPressedSupport()
            }
            if (uploadFile != null) {
                //发送图片消息
                CloudManager.getInstance().sendImageMessage(yourUserId, uploadFile)
                //更新列表
//                addImage(1, uploadFile)
                uploadFile = null
            }

        }
    }


}