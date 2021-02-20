package com.example.myapptaking.fragment.main.child

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.example.framework.base.BaseUIFragment
import com.example.framework.bmob.BmobManager.Companion.instance
import com.example.framework.event.MessageEvent
import com.example.framework.helper.GlideHelper
import com.example.library_common.view.bottombar.BottomBorInfo
import com.example.myapptaking.R
import com.example.myapptaking.fragment.main.child.sub.*
import de.hdodenhof.circleimageview.CircleImageView


/**
 * FileName: MeFragment
 * Founder: LiuGuiLin
 * Profile: 我的
 */
class MeFragment : BaseUIFragment(), BottomBorInfo, View.OnClickListener {

    companion object {
        fun newInstance(): MeFragment {
            val args = Bundle()
            val fragment = MeFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun getLayout(): Any {
        return R.layout.fragment_me
    }

    override val title = R.string.text_main_me

    override val icon = R.drawable.img_me


    private var iv_me_photo: CircleImageView? = null
    private var tv_nickname: TextView? = null
    private var ll_me_info: LinearLayout? = null
    private var ll_new_friend: LinearLayout? = null
    private var ll_private_set: LinearLayout? = null
    private var ll_share: LinearLayout? = null
    private var ll_setting: LinearLayout? = null
    private var ll_notice: LinearLayout? = null

    private var tv_server_status: TextView? = null


    override fun refreshData() {

    }

    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)
        initView()
    }

    private fun initView() {
        iv_me_photo = rootView?.findViewById(R.id.iv_me_photo)
        tv_nickname = rootView?.findViewById(R.id.tv_nickname)
        ll_me_info = rootView?.findViewById(R.id.ll_me_info)
        ll_new_friend = rootView?.findViewById(R.id.ll_new_friend)
        ll_private_set = rootView?.findViewById(R.id.ll_private_set)
        ll_share = rootView?.findViewById(R.id.ll_share)
        ll_setting = rootView?.findViewById(R.id.ll_setting)
        ll_notice = rootView?.findViewById(R.id.ll_notice)
        tv_server_status = rootView?.findViewById(R.id.tv_server_status)

        ll_me_info?.setOnClickListener(this)
        ll_new_friend?.setOnClickListener(this)
        ll_private_set?.setOnClickListener(this)
        ll_share?.setOnClickListener(this)
        ll_setting?.setOnClickListener(this)
        ll_notice?.setOnClickListener(this)
        loadMeInfo()
    }


    /**
     * 加载我的个人信息
     */
    private fun loadMeInfo() {
        val imUser = instance?.user
        GlideHelper.loadSmollUrl(activity, imUser?.photo, 100, 100, iv_me_photo)
        tv_nickname?.text = imUser?.nickName
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ll_me_info ->
                //个人信息
               start(MeInfoFragment.newInstance())
            R.id.ll_new_friend ->
                //新朋友
                start(NewFriendFragment.newInstance())

            R.id.ll_private_set ->
                //隐私设置
                start(PrivateSetFragment.newInstance())

            R.id.ll_share ->
                //分享
                start(ShareImgFragment.newInstance())

            R.id.ll_notice ->
                //通知
                start(NoticeFragment.newInstance())

            R.id.ll_setting ->
                //设置
                start(SettingFragment.newInstance())

        }
    }

    override fun handMessageEvent(event: MessageEvent) {
//        super.handMessageEvent(event)
        if (isSupportVisible){
            loadMeInfo()
        }

    }
}