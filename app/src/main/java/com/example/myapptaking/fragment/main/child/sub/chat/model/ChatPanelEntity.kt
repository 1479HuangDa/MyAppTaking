package com.example.myapptaking.fragment.main.child.sub.chat.model

import com.example.framework.bmob.IMUser
import com.example.framework.recycler.multi22.JssNewMultiItemEntity

interface ChatPanelEntity : JssNewMultiItemEntity {

    //对方用户信息
     var target: ChatWithTargetModel?

     var iMUser: IMUser?

    val isReceived: Boolean

    var senderUserId:String?

}