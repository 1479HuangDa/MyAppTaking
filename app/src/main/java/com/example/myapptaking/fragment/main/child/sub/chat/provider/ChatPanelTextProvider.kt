package com.example.myapptaking.fragment.main.child.sub.chat.provider

import com.example.framework.recycler.JssBaseViewHolder
import com.example.framework.recycler.multi22.Basic.JssBaseItemProvider
import com.example.framework.recycler.multi22.JssNewMultiItemEntity
import com.example.myapptaking.R
import com.example.myapptaking.fragment.main.child.sub.chat.model.ChatPaneTextEntity
import com.example.myapptaking.fragment.main.child.sub.chat.model.ChatPanelEntity
import com.example.myapptaking.fragment.main.child.sub.chat.model.ChatPanelEntityImp
import com.example.myapptaking.fragment.main.child.sub.chat.model.TEXT
import io.rong.message.TextMessage

class ChatPanelTextProvider : JssBaseItemProvider<ChatPanelEntity?, JssBaseViewHolder>() {

    override val itemViewType = TEXT

    override val layoutId = R.layout.chat_panel_text_provider

    override fun convert(helper: JssBaseViewHolder?, item: ChatPanelEntity?) {

        val realItem = item as ChatPanelEntityImp<TextMessage>

        helper?.setImageNetUrl(
            R.id.iv_left_photo,
            realItem.target?.targetUserPhoto,
            R.drawable.img_glide_load_error
        )
            ?.setText(R.id.iv_left_text, realItem.msg.content)
            ?.setText(R.id.iv_right_text, realItem.msg.content)
            ?.setImageNetUrl(
                R.id.iv_right_photo,
                realItem.imUser.photo,
                R.drawable.img_glide_load_error
            )
            ?.setViewVisible(R.id.iv_left_layout,realItem.isReceived)
            ?.setViewVisible(R.id.iv_right_layout, !realItem.isReceived)
    }
}