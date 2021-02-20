package com.example.myapptaking.fragment.main.child.sub.add_fried.provider

import com.example.framework.recycler.JssBaseViewHolder
import com.example.framework.recycler.multi22.Basic.JssBaseItemProvider
import com.example.framework.recycler.multi22.JssNewMultiItemEntity
import com.example.myapptaking.R
import com.example.myapptaking.fragment.main.child.model.AddFriendModel

class AddFriendTypeContentProvider : JssBaseItemProvider<AddFriendModel?, JssBaseViewHolder>() {

    override val itemViewType = AddFriendModel.TYPE_CONTENT

    override val layoutId = R.layout.layout_search_user_item


    override fun convert(helper: JssBaseViewHolder?, item: AddFriendModel?) {
        //设置头像
        helper?.setImageNetUrl(R.id.iv_photo, item?.photo,R.drawable.img_glide_load_ing)
            //设置性别
            ?.setImageResource(
                R.id.iv_sex,
                if (item?.isSex == true)
                    R.drawable.img_boy_icon
                else
                    R.drawable.img_girl_icon
            )
            //设置昵称
            ?.setText(
                R.id.tv_nickname,
                "${item?.nickName}"
            )
            //年龄
            ?.setText(
                R.id.tv_age,
                context.getString(R.string.text_search_age,item?.age.toString())
//                "${item?.age} ${context.getString(R.string.text_search_age)} "
            )
            //设置描述
            ?.setText(R.id.tv_desc,item?.desc)

    }
}