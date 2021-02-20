package com.example.myapptaking.fragment.main

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import com.example.framework.base.BaseUIFragment
import com.example.framework.bmob.BmobManager
import com.example.framework.bmob.BmobManager.Companion.instance
import com.example.framework.entity.Constants
import com.example.framework.event.EventManager
import com.example.framework.event.MessageEvent
import com.example.framework.gson.TokenBean
import com.example.framework.manager.DialogManager
import com.example.framework.manager.HttpManager
import com.example.framework.manager.NotificationHelper
import com.example.framework.utils.LogUtils
import com.example.framework.utils.PermissionWrapper
import com.example.framework.utils.SpUtils
import com.example.framework.view.DialogView
import com.example.framework.view.bottombar.BottomBar
import com.example.framework.view.bottombar.BottomBarTab
import com.example.library_common.view.bottombar.BottomBorInfo
import com.example.myapptaking.R
import com.example.myapptaking.fragment.main.child.MeFragment
import com.example.myapptaking.fragment.main.child.square.SquareFragment
import com.example.myapptaking.fragment.main.child.StarFragment
import com.example.myapptaking.fragment.main.child.chat.ChatFragment
import com.example.myapptaking.fragment.main.child.sub.chat.ChatPanelFragment
import com.example.myapptaking.fragment.main.child.sub.FirstUploadFragment
import com.example.myapptaking.fragment.main.child.sub.NewFriendFragment
import com.example.myapptaking.service.CloudService
import com.google.gson.Gson
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers


class MainFragment:BaseUIFragment() {

    companion object{
        fun newInstance(): MainFragment {
            val args = Bundle()
            val fragment = MainFragment()
            fragment.arguments = args
            return fragment
        }
    }
    private var childFragment = arrayOf<BaseUIFragment>(
        StarFragment.newInstance(),
        SquareFragment.newInstance(),
        ChatFragment.newInstance(),
        MeFragment.newInstance(),
    )
    private var isLoadSubPage = false

    private lateinit var mBottomBar: BottomBar

    private var mUploadView: DialogView? = null

    //第一次按下时间
    private var firstClick: Long = 0


    private var disposable: Disposable? = null

    override fun getLayout(): Any {

        return R.layout.fragment_main
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mBottomBar=view.findViewById(R.id.mBottomBar)

        if (!isLoadSubPage){
            isLoadSubPage=true
            loadSubPage()
        }
        //检查TOKEN
        checkToken()



//        SimulationData.testData()
    }


    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)
        if (!NotificationHelper.getInstance().isNotificationEnabled){
            Toast.makeText(_mActivity,"请打开通知权限",Toast.LENGTH_LONG).show()
            NotificationHelper.getInstance().requestNotify()
        }

        PermissionWrapper.requiredPermission(this) {

        }
    }

    override fun onNewBundle(args: Bundle?) {
        super.onNewBundle(args)
        val page_path = args?.getString(Constants.PAGE_PATH)
        if (page_path?.isNotEmpty()==true){
            when(page_path){
                NewFriendFragment.Path -> {
                    start(NewFriendFragment.newInstance(), SINGLETASK)
                }
                ChatPanelFragment.Path -> {
                    val userId = args.getString(Constants.INTENT_USER_ID)
                    val nickName = args.getString(Constants.INTENT_USER_NAME)
                    val photo = args.getString(Constants.INTENT_USER_PHOTO)
                    start(
                        ChatPanelFragment.newInstance(userId,nickName,photo
                        ), SINGLETASK)
                }
            }
        }
    }



    /**
     * 检查TOKEN
     */
    private fun checkToken() {
        LogUtils.i("checkToken")
        if (mUploadView != null) {
            DialogManager.getInstance().hide(mUploadView)
        }

        //获取TOKEN 需要三个参数 1.用户ID 2.头像地址 3.昵称
        val token = SpUtils.getInstance().getString(Constants.SP_TOKEN, "")
        if (!TextUtils.isEmpty(token)) {
            startCloudService()
        } else {
            //1.有三个参数
            val tokenPhoto = BmobManager.instance?.user?.tokenPhoto
            val tokenName = BmobManager.instance?.user?.tokenNickName
            if (!TextUtils.isEmpty(tokenPhoto) && !TextUtils.isEmpty(tokenName)) {
                //创建Token
                createToken()
            } else {
                //创建上传提示框
                createUploadDialog()
            }
        }
    }

    /**
     * 启动云服务去连接融云服务
     */
    private fun startCloudService() {
        LogUtils.i("startCloudService")
        _mActivity.startService(Intent(_mActivity, CloudService::class.java))
        //检查更新
//        UpdateHelper(this).updateApp(null)
    }

    private fun createToken() {
        LogUtils.i("createToken")
        if (instance?.user == null) {
            Toast.makeText(_mActivity, "登录异常", Toast.LENGTH_SHORT).show()
            return
        }
        /**
         * 1.去融云后台获取Token
         * 2.连接融云
         */
        /**
         * 1.去融云后台获取Token
         * 2.连接融云
         */
        val map: HashMap<String, String> = HashMap()
        map["userId"] = instance!!.user.objectId
        map["name"] = instance!!.user.tokenNickName
        map["portraitUri"] = instance!!.user.tokenPhoto

        //通过OkHttp请求Token
        //线程调度

        //通过OkHttp请求Token
        //线程调度
        disposable =
            Observable.create { emitter: ObservableEmitter<String?> ->
                //执行请求过程
                val json = HttpManager.getInstance().postCloudToken(map)
                LogUtils.i("json:$json")
                emitter.onNext(json)
                emitter.onComplete()
            }.subscribeOn(Schedulers.newThread())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe { s -> parsingCloudToken(s) }
    }

    private fun parsingCloudToken(s: String?) {
        try {
            LogUtils.i("parsingCloudToken:$s")
            val tokenBean: TokenBean = Gson().fromJson(s, TokenBean::class.java)
            if (tokenBean.code == 200) {
                if (!TextUtils.isEmpty(tokenBean.token)) {
                    //保存Token
                    SpUtils.getInstance().putString(Constants.SP_TOKEN, tokenBean.token)
                    startCloudService()
                }
            } else if (tokenBean.code == 2007) {
                Toast.makeText(_mActivity, "注册人数已达上限，请替换成自己的Key", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            LogUtils.i("parsingCloudToken:$e")
        }
    }

    private fun createUploadDialog() {
        mUploadView = DialogManager.getInstance().initView(_mActivity, R.layout.dialog_first_upload)

        //外部点击不能消息
        mUploadView?.setCancelable(false)
        val ivGoUpload: ImageView ?= mUploadView?.findViewById(R.id.iv_go_upload)
        ivGoUpload?.setOnClickListener {
            DialogManager.getInstance().hide(mUploadView)
            start(FirstUploadFragment.newInstance())
        }
        DialogManager.getInstance().show(mUploadView)
    }



    private fun loadSubPage() {
        childFragment.forEachIndexed { _, it ->
            val bottomBor = it as BottomBorInfo
            mBottomBar.addItem(
                BottomBarTab(_mActivity)
                    .setPageTag(it)
                    .setContent(bottomBor.title, bottomBor.icon)
            )
        }

        loadMultipleRootFragment(R.id.fl_container, 0, *childFragment)

        mBottomBar.setOnTabSelectedListener(object : BottomBar.OnTabSelectedListener {
            override fun onTabReselected(position: Int) {
                val nextFragment = childFragment[position]
                val mBottomBorInfo = nextFragment as BottomBorInfo

                mBottomBorInfo.refreshData()
            }

            override fun onTabUnselected(position: Int) {

            }

            override fun onTabSelected(position: Int, prePosition: Int) {
                val nextFragment = childFragment[position]

                showHideFragment(nextFragment, childFragment[prePosition])

//                val mBottomBorInfo = nextFragment as BottomBorInfo

//                mBottomBorInfo.refreshData()
            }
        })
    }

    override fun handMessageEvent(event: MessageEvent) {
        when (event.type) {
            EventManager.EVENT_REFRE_TOKEN_STATUS -> checkToken()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (disposable != null) {
            if (disposable?.isDisposed==false) {
                disposable?.dispose()
            }
        }
    }

    override fun onBackPressedSupport(): Boolean {
        AppExit()
        return true
    }

    private fun AppExit() {
        if (System.currentTimeMillis() - this.firstClick > 2000L) {
            this.firstClick = System.currentTimeMillis();
            Toast.makeText(_mActivity, R.string.text_main_exit, Toast.LENGTH_LONG).show();
            return
        }
        _mActivity. finish()
    }


}