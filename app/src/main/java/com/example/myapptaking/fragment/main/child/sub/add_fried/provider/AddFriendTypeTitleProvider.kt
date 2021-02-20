package com.example.myapptaking.fragment.main.child.sub.add_fried.provider

import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.example.framework.recycler.JssBaseViewHolder
import com.example.framework.recycler.multi22.Basic.JssBaseItemProvider
import com.example.framework.recycler.multi22.JssMultiAdapterProvider
import com.example.framework.recycler.multi22.JssNewMultiItemEntity
import com.example.myapptaking.R
import com.example.myapptaking.fragment.main.child.model.AddFriendModel

class AddFriendTypeTitleProvider : JssBaseItemProvider<AddFriendModel?, JssBaseViewHolder>() {

    override val itemViewType=AddFriendModel.TYPE_TITLE

    override val layoutId= R.layout.layout_search_title_item

    override fun convert(helper: JssBaseViewHolder?, item: AddFriendModel?) {
        helper?.setText(R.id.tv_title, item?.title)
    }

}