package com.example.framework.adapter

//绑定多类型的数据
interface OnMoreBindDataListener<T> : OnBindDataListener<T> {
    fun getItemType(position: Int): Int
}