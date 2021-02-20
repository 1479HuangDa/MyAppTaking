package com.example.myapptaking.fragment.main.child

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import cn.bmob.v3.exception.BmobException
import cn.bmob.v3.listener.FindListener
import com.example.framework.base.LazyFragment
import com.example.framework.bmob.BmobManager
import com.example.framework.bmob.IMUser
import com.example.framework.cloud.CloudManager
import com.example.framework.utils.CommonUtils
import com.example.framework.utils.LogUtils
import com.example.library_common.view.bottombar.BottomBorInfo
import com.example.myapptaking.R
import com.example.myapptaking.fragment.main.child.adapter.CloudTagAdapter
import com.example.myapptaking.fragment.main.child.model.StarModel
import com.example.myapptaking.fragment.main.child.sub.UserInfoFragment
import com.example.myapptaking.fragment.main.child.sub.add_fried.AddFriedFragment
import com.moxun.tagcloudlib.view.TagCloudView


/**
 * FileName: StarFragment
 * Founder: LiuGuiLin
 * Profile: 星球
 */
class StarFragment : LazyFragment(), BottomBorInfo, View.OnClickListener {
    private var tv_star_title: TextView? = null
    private var iv_camera: ImageView? = null
    private var iv_add: ImageView? = null

    private var mCloudView: TagCloudView? = null

    private var ll_random: LinearLayout? = null
    private var ll_soul: LinearLayout? = null
    private var ll_fate: LinearLayout? = null
    private var ll_love: LinearLayout? = null


    private var tv_null_text: TextView? = null
    private var tv_null_cancel: TextView? = null

    //连接状态
    private var tv_connect_status: TextView? = null

    private var mTagAdapter: CloudTagAdapter?=null

    companion object {
        fun newInstance(): StarFragment {
            val args = Bundle()
            val fragment = StarFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override val title = R.string.text_main_star

    override val icon = R.drawable.img_star

    private val mStarList = mutableListOf<StarModel>()

    private val mQrCodeCallbackListener=object : QrCodeFragment.QrCodeCallbackListener{
        override fun onResult(result: String) {

            LogUtils.i("result：$result")

            //Meet#c7a9b4794f
            if (!TextUtils.isEmpty(result)) {
                //是我们自己的二维码
                if (result.startsWith("Meet")) {
                    val split = result.split("#").toTypedArray()
                    //LogUtils.i("split:" + split.toString());
                    if (split.size >= 2) {
                        try {
                            start(UserInfoFragment.newInstance(split[1]))
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                } else {
                    Toast.makeText(
                        _mActivity,
                        R.string.text_toast_error_qrcode,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(
                    _mActivity,
                    R.string.text_toast_error_qrcode,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    }

    override fun getLayout(): Any {
        return R.layout.fragment_star
    }

    override fun initView(view: View) {

        iv_camera = view.findViewById(R.id.iv_camera)
        iv_add = view.findViewById(R.id.iv_add)
        tv_connect_status = view.findViewById(R.id.tv_connect_status)

        tv_star_title = view.findViewById(R.id.tv_star_title)

        mCloudView = view.findViewById(R.id.mCloudView)

        ll_random = view.findViewById(R.id.ll_random)
        ll_soul = view.findViewById(R.id.ll_soul)
        ll_fate = view.findViewById(R.id.ll_fate)
        ll_love = view.findViewById(R.id.ll_love)

        iv_camera?.setOnClickListener(this)
        iv_add?.setOnClickListener(this)

        ll_random?.setOnClickListener(this)
        ll_soul?.setOnClickListener(this)
        ll_fate?.setOnClickListener(this)
        ll_love?.setOnClickListener(this)


        mTagAdapter =CloudTagAdapter(_mActivity, mStarList)
        mCloudView?.setAdapter(mTagAdapter)
        mCloudView?.setOnTagClickListener { _, _, position ->
            startUserInfo(mStarList[position].userId)
//            Toast.makeText(_mActivity, mStarList[position].nickName, Toast.LENGTH_SHORT).show()
        }

//        for (i in 0..10){
//            val model0=StarModel()
//            model0.nickName="model_$i"
//            model0.icon=R.drawable.img_star_icon_3
//            mList.add(model0)
//        }
//        mTagAdapter?.notifyDataSetChanged()

    }

    private fun startUserInfo(userId: String?) {
        start(UserInfoFragment.newInstance(userId))
    }

    override fun refreshData() {
        lazyInit()
    }

    override fun lazyInit() {
        loadStarUser()
    }

    /**
     * 加载星球用户
     */
    private fun loadStarUser() {
        /**
         * 我们从用户库中取抓取一定的好友进行匹配
         */
        BmobManager.instance?.queryAllUser(object : FindListener<IMUser?>() {
            override fun done(list: List<IMUser?>?, e: BmobException?) {
                LogUtils.i("done")
                if (e == null) {
                    if (CommonUtils.isEmpty(list)) {
                        if (mStarList.size > 0) {
                            mStarList.clear()
                        }

//                        mAllUserList = list

                        //这里是所有的用户 只适合我们现在的小批量
                        var index = 50
                        if (list?.size ?: 0 <= 50) {
                            index = list?.size ?: index
                        }
                        //直接填充
                        for (i in 0 until index) {
                            val imUser: IMUser? = list?.get(i)
                            mStarList.add(
                                mStarUser(
                                    imUser?.objectId,
                                    imUser?.nickName,
                                    imUser?.photo
                                )
                            )

                        }
                        LogUtils.i("done...")
                        //当请求数据已经加载出来的时候判断是否连接服务器
                        if (CloudManager.getInstance().isConnect) {
                            //已经连接，并且星球加载，则隐藏
                            tv_connect_status?.visibility = View.GONE
                        }
                        mTagAdapter?.notifyDataSetChanged()
                    }
                }
            }
        })
    }

    private fun mStarUser(userId: String?, nickName: String?, photoUrl: String?):StarModel {
        val model = StarModel()
        model.userId = userId
        model.nickName = nickName
        model.photoUrl = photoUrl
        return model
    }


    override fun onClick(v: View?) {
        when(v?.id){
            R.id.iv_camera -> {
                //扫描
                val newInstance = QrCodeFragment.newInstance()
                newInstance.mQrCodeCallbackListener = mQrCodeCallbackListener
                start(newInstance)
            }
            R.id.iv_add -> {
                //添加好友
                start(AddFriedFragment.newInstance())
            }
            R.id.ll_random -> {
                //随机匹配
            }
            R.id.ll_soul -> {
                //灵魂匹配
            }
            R.id.ll_fate -> {
                //缘分匹配
            }
            R.id.ll_love -> {
                //恋爱匹配

            }
        }
    }


}