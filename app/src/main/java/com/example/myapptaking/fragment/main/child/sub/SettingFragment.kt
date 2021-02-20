package com.example.myapptaking.fragment.main.child.sub

import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import cn.bmob.v3.exception.BmobException
import cn.bmob.v3.listener.FindListener
import cn.bmob.v3.listener.SaveListener
import cn.bmob.v3.listener.UpdateListener
import com.example.framework.base.BaseLazyDataBindingFragment
import com.example.framework.bmob.BmobManager
import com.example.framework.bmob.PrivateSet
import com.example.framework.utils.CommonUtils
import com.example.framework.utils.LogUtils
import com.example.framework.view.LodingView
import com.example.myapptaking.R
import com.example.myapptaking.databinding.FragmentSettingBinding

class SettingFragment : BaseLazyDataBindingFragment<FragmentSettingBinding>(),
    CompoundButton.OnCheckedChangeListener {

    companion object {
        fun newInstance(): SettingFragment {
            val args = Bundle()
            val fragment = SettingFragment()
            fragment.arguments = args
            return fragment
        }
    }

    private var mLodingView: LodingView? = null

    //是否选中
    private var isCheck = false

    //当前ID
    private var currentId = ""

    override fun layoutId(): Int {
        return R.layout.fragment_setting
    }


    override fun initView(view: View) {
        val mToolbar: Toolbar = view.findViewById(R.id.mToolbar)
        mToolbar.setTitle(R.string.text_me_item_title_6)
        mToolbar.setNavigationOnClickListener { onBackPressedSupport() }

        viewBinder.swKillContact.setOnCheckedChangeListener(this)
    }


    override fun lazyInit() {
        mLodingView = LodingView(_mActivity)
        queryPrivateSet()
    }

    /**
     * 查询私有库
     */
    private fun queryPrivateSet() {
        BmobManager.instance?.queryPrivateSet(object : FindListener<PrivateSet?>() {
            override fun done(list: List<PrivateSet?>?, e: BmobException?) {
                if (e == null) {
                    if (CommonUtils.isEmpty(list)) {
                        val data = list ?: emptyList()
                        for (set in data) {

                            if (set?.userId == BmobManager.instance?.user?.objectId) {
                                currentId = set?.objectId.toString()
                                //我存在表中
                                isCheck = true
                                break
                            }
                        }
                        LogUtils.i("currentId:$currentId")
                        viewBinder.swKillContact.isChecked = isCheck

                    }
                }
            }
        })
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        when (buttonView?.id) {
            R.id.sw_kill_contact -> {
                if(isCheck!=isChecked){
                    isCheck = isChecked
                    if (isCheck) {
                        //添加
                        addPrivateSet()
                    } else {
                        //删除
                        delPrivateSet()
                    }
                }

            }
        }
    }

    /**
     * 删除
     */
    private fun delPrivateSet() {
        LogUtils.i("delPrivateSet:$currentId")
        mLodingView?.show(getString(R.string.text_private_set_close_ing))
        BmobManager.instance?.delPrivateSet(currentId, object : UpdateListener() {
            override fun done(e: BmobException?) {
                mLodingView?.hide()
                if (e == null) {
                    Toast.makeText(
                        _mActivity,
                        R.string.text_private_set_fail,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })
    }

    /**
     * 添加
     */
    private fun addPrivateSet() {
        mLodingView?.show(getString(R.string.text_private_set_open_ing))
        BmobManager.instance?.addPrivateSet(object : SaveListener<String?>() {
            override fun done(s: String?, e: BmobException?) {
                mLodingView?.hide()
                if (e == null) {
                    currentId = s ?: ""
                    Toast.makeText(
                        _mActivity,
                        R.string.text_private_set_succeess,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })
    }

}