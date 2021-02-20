package com.example.myapptaking.fragment.main.child.view.media_chat

import android.content.Context
import android.util.AttributeSet
import android.view.SurfaceView
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import cn.bmob.v3.exception.BmobException
import cn.bmob.v3.listener.FindListener
import com.example.framework.bmob.BmobManager
import com.example.framework.bmob.IMUser
import com.example.framework.cloud.CloudManager
import com.example.framework.db.CallRecord
import com.example.framework.db.LitePalHelper
import com.example.framework.helper.GlideHelper
import com.example.framework.utils.CommonUtils
import com.example.myapptaking.R
import de.hdodenhof.circleimageview.CircleImageView

class ChatVideoView : MediaChatView, View.OnClickListener {


    //摄像类
    var mLocalView: SurfaceView? = null

    var mRemoteView: SurfaceView? = null

    //大窗口
    private var video_big_video: RelativeLayout? = null

    //小窗口
    private var video_small_video: RelativeLayout? = null

    //头像
    private var video_iv_photo: CircleImageView? = null

    //昵称
    private var video_tv_name: TextView? = null

    //状态
    private var video_tv_status: TextView? = null

    //个人信息窗口
    private var video_ll_info: LinearLayout? = null

    //时间
    private var video_tv_time: TextView? = null

    //接听
    private var video_ll_answer: LinearLayout? = null

    //挂断
    private var video_ll_hangup: LinearLayout? = null

    //通话ID
    var callId: String = ""

    //呼叫端的ID
    var callUserId: String = ""

    //是否小窗口显示本地视频
    private var isSmallShowLocal = false

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
        inflate(context, R.layout.layout_chat_video, this)


        video_big_video = findViewById(R.id.video_big_video)
        video_small_video = findViewById(R.id.video_small_video)
        video_iv_photo = findViewById(R.id.video_iv_photo)
        video_tv_name = findViewById(R.id.video_tv_name)
        video_tv_status = findViewById(R.id.video_tv_status)
        video_ll_info = findViewById(R.id.video_ll_info)
        video_tv_time = findViewById(R.id.video_tv_time)
        video_ll_answer = findViewById(R.id.video_ll_answer)
        video_ll_hangup = findViewById(R.id.video_ll_hangup)

        video_ll_answer?.setOnClickListener(this)
        video_ll_hangup?.setOnClickListener(this)
        video_small_video?.setOnClickListener(this)

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.video_ll_answer -> {

                //接听
                CloudManager.getInstance().acceptCall(callId)
            }

            R.id.video_ll_hangup -> {

                //挂断
                CloudManager.getInstance().hangUpCall(callId)
            }
            R.id.video_small_video -> {
                //小窗切换
                isSmallShowLocal = !isSmallShowLocal
                updateVideoView()
            }

        }
    }

    fun updateWindowInfo(mType: Int) {
        when (mType) {
            CallingTo -> updateWindowInfo(
                info = true,
                small = false,
                big = true,
                answer = false,
                hangup = true,
                time = false
            )
            Receiver -> updateWindowInfo(
                info = true,
                small = false,
                big = false,
                answer = true,
                hangup = true,
                time = false
            )
        }

        //加载信息
        BmobManager.instance?.queryObjectIdUser(callUserId, object : FindListener<IMUser?>() {
            override fun done(list: List<IMUser?>?, e: BmobException?) {
                if (e == null) {
                    if (CommonUtils.isEmpty(list)) {
                        val imUser: IMUser? = list?.get(0)
                        GlideHelper.loadUrl(
                            context,
                            imUser?.photo,
                            video_iv_photo
                        )
                        video_tv_name?.text = imUser?.nickName
                        if (mType == CallingTo) {
                            video_tv_status?.text = context.getString(
                                R.string.text_service_video_calling,
                                imUser?.nickName
                            )
                        } else if (mType == Receiver) {
                            video_tv_status?.text = context.getString(
                                R.string.text_service_call_video_ing,
                                imUser?.nickName
                            )
                        }
                    }
                }
            }
        })

    }

    //更新个人信息
    fun updateWindowInfo(
        info: Boolean, small: Boolean,
        big: Boolean, answer: Boolean, hangup: Boolean,
        time: Boolean
    ) {
        // 个人信息 小窗口  接听  挂断 时间
        video_ll_info?.visibility = if (info) VISIBLE else GONE
        video_small_video?.visibility = if (small) VISIBLE else GONE
        video_big_video?.visibility = if (big) VISIBLE else GONE
        video_ll_answer?.visibility = if (answer) VISIBLE else GONE
        video_ll_hangup?.visibility = if (hangup) VISIBLE else GONE
        video_tv_time?.visibility = if (time) VISIBLE else GONE
    }

    //显示摄像头
    fun setLocalView(mLocalView: SurfaceView?) {
        this.mLocalView = mLocalView
        video_big_video?.addView(mLocalView)
    }

    fun setLocalView22(mLocalView: SurfaceView?) {
        this.mLocalView = mLocalView

    }


    fun setTime(time: String) {
        video_tv_time?.text = time
    }

    /**
     * 保存视频记录
     *
     * @param id
     * @param callStatus
     */
    fun saveVideoRecord(id: String, callStatus: Int) {
        LitePalHelper.getInstance()
            .saveCallRecord(id, CallRecord.MEDIA_TYPE_VIDEO, callStatus)
    }

    /**
     * 更新视频流
     */
    fun updateVideoView() {
        video_big_video?.removeAllViews()
        video_small_video?.removeAllViews()
        if (isSmallShowLocal) {
            if (mLocalView != null) {
                video_small_video?.addView(mLocalView)
                mLocalView?.setZOrderOnTop(true)
            }
            if (mRemoteView != null) {
                video_big_video?.addView(mRemoteView)
                mRemoteView?.setZOrderOnTop(false)
            }
        } else {
            if (mLocalView != null) {
                video_big_video?.addView(mLocalView)
                mLocalView?.setZOrderOnTop(false)
            }
            if (mRemoteView != null) {
                video_small_video?.addView(mRemoteView)
                mRemoteView?.setZOrderOnTop(true)
            }
        }
    }

}