package com.example.myapptaking.fragment.main.child.square.provider

import android.content.Context
import android.os.Handler
import android.os.Message
import android.view.View
import com.bumptech.glide.request.RequestOptions
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.example.framework.bmob.SquareSet
import com.example.framework.manager.MediaPlayerManager
import com.example.framework.recycler.JssBaseViewHolder
import com.example.framework.recycler.multi22.Basic.JssBaseItemProvider
import com.example.framework.utils.TimeUtils
import com.example.myapptaking.R
import com.example.myapptaking.fragment.main.child.model.SquareModel
import com.example.myapptaking.fragment.main.child.square.view.SquareMusicFloatingView
import com.example.myapptaking.fragment.main.child.sub.UserInfoFragment
import java.text.SimpleDateFormat

class SquareMusicProvider(mContext: Context) : JssBaseItemProvider<SquareModel?, JssBaseViewHolder>() {

    companion object{
        //更新进度
        private const val UPDATE_POS = 1235
    }

    override val itemViewType: Int
        get() = SquareSet.PUSH_MUSIC

    override val layoutId: Int
        get() = R.layout.provider_square_music

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

    private var mSquareMusicFloatingView: SquareMusicFloatingView? = null

    private val mHandler = Handler { msg ->
        when (msg.what) {
            UPDATE_POS -> {
                mSquareMusicFloatingView?.showMusicPros(mMusicManager)
            }
        }
        false
    }

    //播放
    private var mMusicManager: MediaPlayerManager? = null

    //音乐是否在播放
    private var isMusicPlay = false

    init {
        mSquareMusicFloatingView = SquareMusicFloatingView(mContext)

        mSquareMusicFloatingView?.setOnClickListener {
            hideMusicWindow()
        }

        mMusicManager = MediaPlayerManager()
        mMusicManager?.setOnComplteionListener { isMusicPlay = false }

        mMusicManager?.setOnProgressListener { progress, pos ->
            val message = Message()
            message.what = UPDATE_POS
            message.arg1 = progress
            mHandler.sendMessage(message)
        }
    }

    override fun onViewHolderCreated(viewHolder: JssBaseViewHolder, viewType: Int) {
        super.onViewHolderCreated(viewHolder, viewType)
        addChildClickViewIds(R.id.iv_photo, R.id.ll_music)
    }

    override fun convert(helper: JssBaseViewHolder?, item: SquareModel?) {
        val imUser = item?.imUser
        val model = item?.squareSet



        helper?.setImageNetUrl(
            R.id.iv_photo,
            imUser?.photo,
            R.drawable.img_glide_load_error,
            RequestOptions()
                .override(50)
        )
            ?.setText(R.id.tv_nickname, imUser?.nickName)
            ?.setText(
                R.id.tv_square_age,
                context.getString(R.string.text_search_age, imUser?.age.toString())
            )
            ?.setText(R.id.tv_square_constellation, imUser?.constellation)
            ?.setViewVisible(
                R.id.tv_square_constellation,
                imUser?.constellation?.isNotEmpty() == true
            )
            ?.setText(
                R.id.tv_square_hobby,
                context.getString(R.string.text_squate_love, imUser?.hobby)
            )
            ?.setViewVisible(
                R.id.tv_square_hobby,
                imUser?.hobby?.isNotEmpty() == true
            )
            ?.setText(R.id.tv_square_status, imUser?.status)
            ?.setViewVisible(
                R.id.tv_square_status,
                imUser?.status?.isNotEmpty() == true
            )
            ?.setText(R.id.tv_time, dateFormat.format(model?.pushTime))
            ?.setText(R.id.tv_text, model?.text)
            ?.setViewVisible(
                R.id.tv_text,
                model?.text?.isNotEmpty() == true
            )
    }

    override fun onChildClick(
        helper: BaseViewHolder,
        view: View,
        data: SquareModel?,
        position: Int
    ) {
        super.onChildClick(helper, view, data, position)

        val attachFragment = getAdapter()?.getAttachFragment()
        when (view.id) {
            R.id.iv_photo -> {
                val model = data?.squareSet
                attachFragment?.start(UserInfoFragment.newInstance(model?.userId))
            }
            R.id.ll_music -> {
                val model = data?.squareSet
                when {
                    attachFragment?.checkWindowPermissions() == false -> {
                        attachFragment.requestWindowPermissions()
                    }
                    mMusicManager?.isPlaying==true -> {
                        //播放音乐
                        hideMusicWindow()

                    }
                    else -> {
                        if (isMusicPlay) {
                            mMusicManager?.continuePlay()
                        } else {
                            mMusicManager?.startPlay(model?.mediaUrl)
                            isMusicPlay = true
                        }
                        mSquareMusicFloatingView?.showMusicWindow(mMusicManager)
                    }
                }
            }
        }

    }

    private fun hideMusicWindow() {
        mMusicManager?.pausePlay()
        mSquareMusicFloatingView?.hideMusicWindow()
    }
}