package com.example.myapptaking.fragment.main.child.square.view

import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.example.framework.helper.WindowHelper
import com.example.framework.manager.MediaPlayerManager
import com.example.framework.utils.AnimUtils
import com.example.framework.utils.TimeUtils
import com.example.myapptaking.R

class SquareMusicFloatingView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), View.OnTouchListener {

    private var iv_music_photo: ImageView? = null
    private var pb_music_pos: ProgressBar? = null
    private var tv_music_cur: TextView? = null
    private var tv_music_all: TextView? = null

    var lpMusicParams: WindowManager.LayoutParams? = null

    //是否移动
    private var isMove = false

    //是否拖拽
    private var isDrag = false
    private var mLastX = 0
    private var mLastY = 0


    //属性动画
    private var objAnimMusic: ObjectAnimator? = null

    init {
        inflate(context, R.layout.layout_square_music_item, this)

        //初始化View
        iv_music_photo = findViewById(R.id.iv_music_photo)
        pb_music_pos = findViewById(R.id.pb_music_pos)
        tv_music_cur = findViewById(R.id.tv_music_cur)
        tv_music_all = findViewById(R.id.tv_music_all)

        objAnimMusic = AnimUtils.rotation(iv_music_photo)

        lpMusicParams = WindowHelper.getInstance().createLayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            Gravity.TOP or Gravity.START
        )

        setOnTouchListener(this)
    }

    fun hideMusicWindow(){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            objAnimMusic?.pause()
        }
        WindowHelper.getInstance().hideView(this)
    }

    /**
     * 显示窗口
     */
     fun showMusicWindow(mMusicManager: MediaPlayerManager?) {
        val duration=mMusicManager?.duration ?:0

        pb_music_pos?.max = duration
        tv_music_all?.text = TimeUtils.formatDuring(duration * 1L)
        objAnimMusic?.start()
        WindowHelper.getInstance().showView(this, lpMusicParams)
    }

    fun showMusicPros(mMusicManager: MediaPlayerManager?){
        val currentPosition = mMusicManager?.currentPosition?:0
        pb_music_pos?.progress = currentPosition
        tv_music_all?.text = TimeUtils.formatDuring(currentPosition * 1L)
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        val mStartX = event!!.rawX.toInt()
        val mStartY = event.rawY.toInt()
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                isMove = false
                isDrag = false
                mLastX = event.rawX.toInt()
                mLastY = event.rawY.toInt()
            }
            MotionEvent.ACTION_MOVE -> {

                //偏移量
                val dx = mStartX - mLastX
                val dy = mStartY - mLastY
                if (isMove) {
                    isDrag = true
                } else {
                    if (dx == 0 && dy == 0) {
                        isMove = false
                    } else {
                        isMove = true
                        isDrag = true
                    }
                }

                //移动
                lpMusicParams!!.x += dx
                lpMusicParams!!.y += dy

                //重置坐标
                mLastX = mStartX
                mLastY = mStartY

                //WindowManager addView removeView updateView
                WindowHelper.getInstance().updateView(this, lpMusicParams)
            }
        }
        return isDrag
    }

}