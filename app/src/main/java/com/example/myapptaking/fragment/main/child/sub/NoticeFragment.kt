package com.example.myapptaking.fragment.main.child.sub

import android.os.Bundle
import com.example.framework.base.BaseUIFragment
import com.example.myapptaking.R

class NoticeFragment:BaseUIFragment() {

    companion object{
        fun newInstance(): NoticeFragment {
            val args = Bundle()
            val fragment = NoticeFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun getLayout(): Any {
        return R.layout.fragment_notice
    }
}