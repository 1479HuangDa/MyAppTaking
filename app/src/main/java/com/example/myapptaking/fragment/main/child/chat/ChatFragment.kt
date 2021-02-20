package com.example.myapptaking.fragment.main.child.chat

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.example.framework.base.BaseUIFragment
import com.example.library_common.view.bottombar.BottomBorInfo
import com.example.myapptaking.R
import com.example.myapptaking.fragment.main.child.chat.sub.AllFriendFragment
import com.example.myapptaking.fragment.main.child.chat.sub.CallRecordFragment
import com.example.myapptaking.fragment.main.child.chat.sub.ChatRecordFragment
import com.example.myapptaking.fragment.main.child.chat.sub.LabInfo
import com.google.android.material.tabs.TabLayout

/**
 * FileName: ChatFragment
 * Founder: LiuGuiLin
 * Profile: 聊天
 */
class ChatFragment : BaseUIFragment(), BottomBorInfo, TabLayout.OnTabSelectedListener {

    companion object {
        fun newInstance(): ChatFragment {
            val args = Bundle()
            val fragment = ChatFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun getLayout(): Any {
        return R.layout.fragment_chat
    }

    override val title = R.string.text_main_chat

    override val icon = R.drawable.img_chat

    private var mTabLayout: TabLayout? = null

    private var mViewPager: ViewPager? = null

    private var mPagerAdapter: FragmentStatePagerAdapter?=null

    private var subPage= listOf<BaseUIFragment>(
        ChatRecordFragment.newInstance(),
        CallRecordFragment.newInstance(),
        AllFriendFragment.newInstance(),
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view)
    }

    private fun initView(view: View) {
        mTabLayout = view.findViewById(R.id.mTabLayout)
        mViewPager = view.findViewById(R.id.mViewPager)

        mPagerAdapter=object :FragmentStatePagerAdapter(
            childFragmentManager,
            BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
        ){
            override fun getCount(): Int {
                return subPage.size
            }

            override fun getItem(position: Int): BaseUIFragment {
                 return subPage[position]
            }

            override fun getPageTitle(position: Int): CharSequence {
               val labInfo=getItem(position) as LabInfo
                return _mActivity.getString(labInfo.labelTitle)
            }
        }
        mViewPager?.adapter=mPagerAdapter
        mTabLayout?.setupWithViewPager(mViewPager)
        mTabLayout?.addOnTabSelectedListener(this)


        //默认第一个选中
        defTabStyle(mTabLayout?.getTabAt(mTabLayout?.selectedTabPosition?:0), 20)
    }

    override fun refreshData() {

    }

    override fun onTabSelected(tab: TabLayout.Tab?) {
        defTabStyle(tab, 20)
    }

    override fun onTabUnselected(tab: TabLayout.Tab?) {
        tab?.customView = null
    }

    override fun onTabReselected(tab: TabLayout.Tab?) {

    }

    /**
     * 设置Tab样式
     *
     * @param tab
     * @param size
     */
    private fun defTabStyle(tab: TabLayout.Tab?, size: Int) {
        val view: View = LayoutInflater.from(activity).inflate(R.layout.layout_tab_text, null)
        val tv_tab = view.findViewById<TextView>(R.id.tv_tab)
        tv_tab.text = tab?.text
        tv_tab.setTextColor(Color.WHITE)
        tv_tab.textSize = size.toFloat()
        tab?.customView = tv_tab
    }

    override fun onDestroy() {
        super.onDestroy()
        mTabLayout?.removeOnTabSelectedListener(this)
    }
}