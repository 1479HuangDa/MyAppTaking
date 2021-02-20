package com.example.myapptaking.fragment.guide

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import cn.bmob.v3.exception.BmobException
import cn.bmob.v3.listener.SaveListener
import com.example.framework.base.BaseLazyDataBindingFragment
import com.example.framework.bmob.BmobManager
import com.example.framework.bmob.IMUser
import com.example.framework.entity.Constants
import com.example.framework.manager.KeyWordManager
import com.example.framework.utils.LogUtils
import com.example.framework.utils.SpUtils
import com.example.myapptaking.R
import com.example.myapptaking.databinding.FragmentPasswordLoginBinding
import com.example.myapptaking.fragment.main.MainFragment

class PasswordLoginFragment : BaseLazyDataBindingFragment<FragmentPasswordLoginBinding>(),
    View.OnClickListener {

    companion object {
        fun newInstance(): PasswordLoginFragment {
            val args = Bundle()
            val fragment = PasswordLoginFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun lazyInit() {

    }

    override fun initView(view: View) {
        viewBinder.onClick = this
    }


    override fun layoutId(): Int {
        return R.layout.fragment_password_login
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_login -> {
                val phone: String = viewBinder.etPhone.text.toString().trim { it <= ' ' }
                if (TextUtils.isEmpty(phone)) {
                    Toast.makeText(
                        _mActivity,
                        _mActivity.getString(R.string.text_login_phone_null),
                        Toast.LENGTH_SHORT
                    ).show()
                    return
                }

                val password: String = viewBinder.etPassword.text.toString().trim { it <= ' ' }
                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(_mActivity,
                        _mActivity.getString(R.string.text_login_pw_null),
                        Toast.LENGTH_SHORT
                    ).show()
                    return
                }

                BmobManager.instance
                    ?.loginByAccount(phone, password, object : SaveListener<IMUser?>() {
                        override fun done(imUser: IMUser?, e: BmobException?) {
                            KeyWordManager.getInstance().hideKeyWord(_mActivity)
                            if (e == null) {
                                //登陆成功
                                SpUtils.getInstance().putString(Constants.SP_PHONE, phone)
                                startWithPop(MainFragment.newInstance())
//                                startActivity(
//                                    Intent(
//                                        this@TestLoginActivity, MainActivity::class.java
//                                    )
//                                )
//                                ActivityHelper.getInstance().exit()
//                                finish()
                            } else {
                                LogUtils.e("Login Error:$e")
                                if (e.errorCode == 101) {
                                    Toast.makeText(
                                        _mActivity,
                                        _mActivity.getString(R.string.text_test_login_fail),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    })
            }
        }
    }
}