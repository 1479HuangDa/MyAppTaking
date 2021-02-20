package com.example.myapptaking.fragment.main.child.sub.chat.provider

import com.example.framework.manager.MapManager
import com.example.framework.recycler.JssBaseViewHolder
import com.example.framework.recycler.multi22.Basic.JssBaseItemProvider
import com.example.framework.recycler.multi22.JssNewMultiItemEntity
import com.example.myapptaking.R
import com.example.myapptaking.fragment.main.child.sub.chat.model.ChatPaneLocationEntity
import com.example.myapptaking.fragment.main.child.sub.chat.model.ChatPanelEntity
import com.example.myapptaking.fragment.main.child.sub.chat.model.ChatPanelImgEntity
import com.example.myapptaking.fragment.main.child.sub.chat.model.LOCATION

class ChatPanelLocationProvider : JssBaseItemProvider<ChatPanelEntity?, JssBaseViewHolder>(){

    override val itemViewType =LOCATION

    override val layoutId= R.layout.chat_panel_location_provider


    override fun convert(helper: JssBaseViewHolder?, item: ChatPanelEntity?) {
        val realItem=item as ChatPaneLocationEntity
        val la=realItem.msg.lat
        val lo=realItem.msg.lng
        helper?.setImageNetUrl(
            R.id.iv_left_photo,
            realItem.target?.targetUserPhoto,
            R.drawable.img_glide_load_error
        )
            ?.setImageNetUrl(R.id.iv_left_img, MapManager.getInstance().getMapUrl(la, lo))
            ?.setImageNetUrl(R.id.iv_right_img, MapManager.getInstance().getMapUrl(la, lo))
            ?.setImageNetUrl(
                R.id.iv_right_photo,
                realItem.imUser.photo,
                R.drawable.img_glide_load_error
            )
            ?.setText(R.id.iv_left_address, realItem.msg?.poi)
            ?.setText(R.id.tv_right_address, realItem.msg?.poi)
            ?.setViewVisible(R.id.iv_left_layout,realItem.isReceived)
            ?.setViewVisible(R.id.iv_right_layout, !realItem.isReceived)
    }


}