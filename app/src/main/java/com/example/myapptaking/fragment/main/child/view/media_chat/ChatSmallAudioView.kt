package com.example.myapptaking.fragment.main.child.view.media_chat

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import com.example.framework.helper.WindowHelper
import com.example.myapptaking.R
import java.lang.Math.abs

/**
 *
 *音频视图（小屏）
 * */
class ChatSmallAudioView : MediaChatView, View.OnTouchListener {

    private var mListener:ChatSmallAudioViewListener?=null

    fun setListener(mListener:ChatSmallAudioViewListener?){
        this.mListener=mListener
    }

    //是否移动
    private var isMove = false

    //是否拖拽
    private var isDrag = false
    private var mLastX = 0
    private var mLastY = 0

    //最小化的音频View
    var lpSmallView: WindowManager.LayoutParams? = null

    //时间
    private var mSmallTime: TextView? = null

    constructor(context: Context) : super(context) {
        initView(context)
    }


    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initView(context)
    }


    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        initView(context)
    }

    private fun initView(context: Context) {
        lpSmallView = WindowHelper.getInstance()
            .createLayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                Gravity.TOP or Gravity.START
            )
        inflate(context, R.layout.layout_chat_small_audio, this)

        mSmallTime=findViewById(R.id.mSmallTime)

        setOnTouchListener(this)
    }

    fun setTime(time: String){
        mSmallTime?.text = time
    }


    interface ChatSmallAudioViewListener{
       fun onClick()
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {

        /**
         * OnTouch 和 OnClick 点击冲突
         * 如何判断是点击 还是 移动
         * 通过点击下的坐标 - 落地的坐标 如果移动则说明是移动 如果 = 0 ，那说明没有移动则是点击
         */
        val mStartX = event?.rawX?.toInt()
        val mStartY = event?.rawY?.toInt()
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                isMove = false
                isDrag = false
                mLastX = event.rawX.toInt()
                mLastY = event.rawY.toInt()
            }
            MotionEvent.ACTION_MOVE -> {

                //偏移量
                val dx = mStartX?.minus(mLastX)?:0
                val dy = mStartY?.minus(mLastY)?:0
                if (isMove) {
                    isDrag = true
                } else {
                    if (kotlin.math.abs(dx) <= 10 && kotlin.math.abs(dy) <= 10) {
                        isMove = false
                    } else {
                        isMove = true
                        isDrag = true
                    }
                }

                //移动
                lpSmallView?.x = lpSmallView?.x?.plus(dx)
                lpSmallView?.y = lpSmallView?.y?.plus(dy)

                //重置坐标
                mLastX = mStartX?:0
                mLastY = mStartY?:0
                WindowHelper.getInstance().updateView(this, lpSmallView)
            }
        }
        return isDrag

    }

}