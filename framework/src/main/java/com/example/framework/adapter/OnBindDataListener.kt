package com.example.framework.adapter

//绑定数据
interface OnBindDataListener<T> {
    fun onBindViewHolder(model: T, viewHolder: CommonViewHolder?, type: Int, position: Int)
    fun getLayoutId(type: Int): Int
}