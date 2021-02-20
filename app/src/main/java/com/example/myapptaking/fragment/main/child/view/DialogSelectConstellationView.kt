package com.example.myapptaking.fragment.main.child.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amap.api.services.core.PoiItem
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemClickListener
import com.example.framework.recycler.JssBaseQuickAdapter
import com.example.framework.recycler.JssBaseViewHolder
import com.example.myapptaking.R


class DialogSelectConstellationView : FrameLayout, OnItemClickListener {

    private var tv_cancel: TextView? = null

    private var mConstellationnView: RecyclerView? = null

    private var mAdapter: JssBaseQuickAdapter<PoiItem?>? = null

    var mViewCallBack: DialogSelectConstellationViewCallBack? = null

    var selectedPosition: Int = -1

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(context)
    }


    private fun init(context: Context) {
        inflate(context, R.layout.dialog_select_constellation, this)
        tv_cancel = findViewById(R.id.tv_cancel)
        mConstellationnView = findViewById(R.id.mConstellationnView)
        mConstellationnView?.layoutManager = LinearLayoutManager(context)
        mConstellationnView?.addItemDecoration(
            DividerItemDecoration(
                context, DividerItemDecoration.VERTICAL
            )
        )
        mAdapter = object : JssBaseQuickAdapter<PoiItem?>(R.layout.layout_me_age_item) {
            override fun convert(holder: JssBaseViewHolder?, item: PoiItem?) {
                super.convert(holder, item)
                holder?.setText(R.id.tv_age_text, item.toString())
            }
        }
        mConstellationnView?.adapter = mAdapter
        mAdapter?.setOnItemClickListener(this)
        tv_cancel?.setOnClickListener {
            mViewCallBack?.onCancel()
        }
    }

    override fun onItemClick(adapter: BaseQuickAdapter<*, *>, view: View, position: Int) {
        val item = mAdapter?.getItem(position)
        selectedPosition = position
        mViewCallBack?.onItemSelected(item)
    }

    interface DialogSelectConstellationViewCallBack {
        fun onCancel()
        fun onItemSelected(item: PoiItem?)
    }


    fun addData(ls: List<PoiItem?>?) {
        mAdapter?.clears()
        mAdapter?.addData(ls ?: emptyList())
        mAdapter?.notifyDataSetChanged()
    }

}