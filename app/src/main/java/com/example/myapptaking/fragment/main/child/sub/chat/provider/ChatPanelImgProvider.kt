package com.example.myapptaking.fragment.main.child.sub.chat.provider

import com.example.framework.recycler.JssBaseViewHolder
import com.example.framework.recycler.multi22.Basic.JssBaseItemProvider
import com.example.framework.recycler.multi22.JssNewMultiItemEntity
import com.example.myapptaking.R
import com.example.myapptaking.fragment.main.child.sub.chat.model.ChatPanelEntity
import com.example.myapptaking.fragment.main.child.sub.chat.model.ChatPanelImgEntity
import com.example.myapptaking.fragment.main.child.sub.chat.model.IMAGE

class ChatPanelImgProvider : JssBaseItemProvider<ChatPanelEntity?, JssBaseViewHolder>() {

    override val itemViewType = IMAGE

    override val layoutId = R.layout.chat_panel_img_provider

    override fun convert(helper: JssBaseViewHolder?, item: ChatPanelEntity?) {
        val realItem=item as ChatPanelImgEntity
        helper?.setImageNetUrl(
            R.id.iv_left_photo,
            realItem.target?.targetUserPhoto,
            R.drawable.img_glide_load_error
        )
            ?.setImageNetUrl(R.id.iv_left_img, realItem.msg.remoteUri.toString())
            ?.setImageNetUrl(R.id.iv_right_img, realItem.msg.remoteUri.toString())
            ?.setImageNetUrl(
                R.id.iv_right_photo,
                realItem.imUser.photo,
                R.drawable.img_glide_load_error
            )
            ?.setViewVisible(R.id.iv_left_layout,realItem.isReceived)
            ?.setViewVisible(R.id.iv_right_layout, !realItem.isReceived)
    }
}