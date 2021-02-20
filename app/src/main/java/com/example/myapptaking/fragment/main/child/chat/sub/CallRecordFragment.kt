package com.example.myapptaking.fragment.main.child.chat.sub

import android.os.Bundle
import com.example.framework.base.BaseUIFragment
import com.example.myapptaking.R

class CallRecordFragment : BaseUIFragment(),LabInfo  {

    companion object{
        fun newInstance(): CallRecordFragment {
            val args = Bundle()
            val fragment = CallRecordFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override var labelTitle =R.string.text_chat_tab_title_2

    override fun getLayout(): Any {
         return R.layout.fragment_call_record
    }
}