package com.example.myapptaking.fragment.guide

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import cn.bmob.v3.exception.BmobException
import cn.bmob.v3.listener.LogInListener
import cn.bmob.v3.listener.QueryListener
import com.example.framework.base.BaseUIFragment
import com.example.framework.bmob.BmobManager
import com.example.framework.bmob.IMUser
import com.example.framework.entity.Constants
import com.example.framework.manager.DialogManager
import com.example.framework.utils.LogUtils
import com.example.framework.utils.SpUtils
import com.example.framework.view.DialogView
import com.example.framework.view.LodingView
import com.example.framework.view.TouchPictureV
import com.example.myapptaking.R
import com.example.myapptaking.fragment.main.MainFragment


class LoginFragment : BaseUIFragment(), View.OnClickListener {

    companion object {
        fun newInstance(): LoginFragment {
            val args = Bundle()
            val fragment = LoginFragment()
            fragment.arguments = args
            return fragment
        }
    }

    /**
     * 1.点击发送的按钮，弹出一个提示框，图片验证码，验证通过之后
     * 2.!发送验证码，@同时按钮变成不可点击，@按钮开始倒计时，倒计时结束，@按钮可点击，@文字变成“发送”
     * 3.通过手机号码和验证码进行登录
     * 4.登录成功之后获取本地对象
     */
    private var et_phone: EditText? = null
    private var et_code: EditText? = null
    private var btn_send_code: Button? = null
    private var btn_login: Button? = null

    private var mCodeView: DialogView? = null
    private var mPictureV: TouchPictureV? = null

    private var tv_test_login: TextView? = null

    private var mLodingView: LodingView? = null

    private val H_TIME = 1001

    //60s倒计时
    private var TIME = 60

    private var mHandler: Handler = Handler { message ->
        handleMessage(message)
        false
    }

    @SuppressLint("SetTextI18n")
    fun handleMessage(message: Message) {
        when (message.what) {
            H_TIME -> {
                TIME--
                btn_send_code?.text = TIME.toString() + "s"
                if (TIME > 0) {
                    mHandler.sendEmptyMessageDelayed(H_TIME, 1000)
                } else {
                    btn_send_code?.isEnabled = true
                    btn_send_code?.text = _mActivity.getString(R.string.text_login_send)
                    TIME = 60
                }
            }
        }
    }

    override fun getLayout(): Any {
        return R.layout.fragment_login
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view)
    }

    private fun initView(view: View) {
        initDialogView()

        tv_test_login = view.findViewById(R.id.tv_test_login)
        tv_test_login?.setOnClickListener(this)

        et_phone = view.findViewById(R.id.et_phone)
        et_code = view.findViewById(R.id.et_code)
        btn_send_code = view.findViewById(R.id.btn_send_code)
        btn_login = view.findViewById(R.id.btn_login)

        btn_send_code?.setOnClickListener(this)
        btn_login?.setOnClickListener(this)

        val phone = SpUtils.getInstance().getString(Constants.SP_PHONE, "")
        if (!TextUtils.isEmpty(phone)) {
            et_phone?.setText(phone)
        }
    }

    private fun initDialogView() {
        mLodingView = LodingView(_mActivity)

        mCodeView = DialogManager.getInstance().initView(_mActivity, R.layout.dialog_code_view)
        mPictureV = mCodeView?.findViewById(R.id.mPictureV)
        mPictureV?.setViewResultListener {
            DialogManager.getInstance().hide(mCodeView)
            sendSMS()
        }
    }

    /**
     * 发送短信验证码
     */
    private fun sendSMS() {

        //1.获取手机号码
        val phone = et_phone?.text.toString().trim { it <= ' ' }
        if (TextUtils.isEmpty(phone)) {
            Toast.makeText(
                _mActivity, R.string.text_login_phone_null,
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        //2.请求短信验证码
        BmobManager.instance?.requestSMS(phone, object : QueryListener<Int?>() {
            override fun done(integer: Int?, e: BmobException?) {
                if (e == null) {
                    btn_send_code?.isEnabled = false
                    mHandler.sendEmptyMessage(H_TIME)
                    Toast.makeText(
                        _mActivity, R.string.text_user_resuest_succeed,
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    LogUtils.e("SMS:$e")
                    Toast.makeText(
                        _mActivity, R.string.text_user_resuest_fail,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_send_code -> DialogManager.getInstance().show(mCodeView)
            R.id.btn_login -> login()
            R.id.tv_test_login -> start(PasswordLoginFragment.newInstance())
        }
    }

    private fun login() {

        //1.判断手机号码和验证码不为空
        val phone = et_phone?.text.toString().trim { it <= ' ' }
        if (TextUtils.isEmpty(phone)) {
            Toast.makeText(
                _mActivity, R.string.text_login_phone_null,
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val code = et_code?.text.toString().trim { it <= ' ' }
        if (TextUtils.isEmpty(code)) {
            Toast.makeText(
                _mActivity, R.string.text_login_code_null,
                Toast.LENGTH_SHORT
            ).show()
            return
        }


        //显示LodingView
        mLodingView?.show(_mActivity.getString(R.string.text_login_now_login_text))
        BmobManager.instance?.signOrLoginByMobilePhone(
            phone,
            code,
            object : LogInListener<IMUser>() {
                override fun done(imUser: IMUser, e: BmobException?) {
                    mLodingView?.hide()
                    when {
                        e == null -> {
                            //登陆成功

                            //把手机号码保存下来
                            SpUtils.getInstance().putString(Constants.SP_PHONE, phone)
                            startWithPop(MainFragment.newInstance())
                        }
                        e.errorCode == 207 -> {
                            Toast.makeText(
                                _mActivity,
                                getString(R.string.text_login_code_error),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        else -> {
                            Toast.makeText(_mActivity, "ERROR:$e", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }
            })
    }
}

