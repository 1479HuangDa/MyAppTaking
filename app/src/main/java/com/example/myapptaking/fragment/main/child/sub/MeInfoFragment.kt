package com.example.myapptaking.fragment.main.child.sub

import android.os.Bundle
import com.example.framework.base.BaseUIFragment
import com.example.myapptaking.R

class MeInfoFragment:BaseUIFragment() {

    companion object{
        fun newInstance():MeInfoFragment {
            val args = Bundle()

            val fragment = MeInfoFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun getLayout(): Any {
         return R.layout.fragment_me_info
    }
}