package com.example.myapptaking.fragment.main.child.sub

import android.os.Bundle
import com.example.framework.base.BaseUIFragment
import com.example.myapptaking.R

class PrivateSetFragment:BaseUIFragment() {

    companion object{
        fun newInstance(): PrivateSetFragment {
            val args = Bundle()
            val fragment = PrivateSetFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun getLayout(): Any {
         return R.layout.fragment_private_set
    }
}