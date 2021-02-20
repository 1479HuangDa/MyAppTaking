package com.example.myapptaking.fragment.main.child.view.media_chat

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import cn.bmob.v3.exception.BmobException
import cn.bmob.v3.listener.FindListener
import com.example.framework.bmob.BmobManager
import com.example.framework.bmob.IMUser
import com.example.framework.cloud.CloudManager
import com.example.framework.db.CallRecord
import com.example.framework.db.LitePalHelper
import com.example.framework.helper.GlideHelper
import com.example.framework.helper.WindowHelper
import com.example.framework.utils.CommonUtils
import com.example.myapptaking.R
import de.hdodenhof.circleimageview.CircleImageView

/**
 *
 *音频视图（全屏）
 * */
class FullScreenChatAudioView : MediaChatView, View.OnClickListener {


    //是否小窗口显示本地视频
    private var isSmallShowLocal = false

    //头像
    private var audio_iv_photo: CircleImageView? = null

    //状态
    private var audio_tv_status: TextView? = null

    //录音图片
    private var audio_iv_recording: ImageView? = null

    //录音按钮
    private var audio_ll_recording: LinearLayout? = null

    //接听图片
    private var audio_iv_answer: ImageView? = null

    //接听按钮
    private var audio_ll_answer: LinearLayout? = null

    //挂断图片
    private var audio_iv_hangup: ImageView? = null

    //挂断按钮
    private var audio_ll_hangup: LinearLayout? = null

    //免提图片
    private var audio_iv_hf: ImageView? = null

    //免提按钮
    private var audio_ll_hf: LinearLayout? = null

    //最小化
    private var audio_iv_small: ImageView? = null

    //通话ID
    var callId: String = ""

    //呼叫端的ID
    var callUserId: String = ""

    private var isRecording = false

    private var isHF = false

    private var mFullScreenChatAudio: FullScreenChatAudio? = null

    fun setFullScreenChatAudio(mFullScreenChatAudio: FullScreenChatAudio?) {
        this.mFullScreenChatAudio = mFullScreenChatAudio
    }

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
        inflate(context, R.layout.layout_chat_audio, this)

        audio_iv_photo = findViewById(R.id.audio_iv_photo)
        audio_tv_status = findViewById(R.id.audio_tv_status)
        audio_iv_recording = findViewById(R.id.audio_iv_recording)
        audio_ll_recording = findViewById(R.id.audio_ll_recording)
        audio_iv_answer = findViewById(R.id.audio_iv_answer)
        audio_ll_answer = findViewById(R.id.audio_ll_answer)
        audio_iv_hangup = findViewById(R.id.audio_iv_hangup)
        audio_ll_hangup = findViewById(R.id.audio_ll_hangup)
        audio_iv_hf = findViewById(R.id.audio_iv_hf)
        audio_ll_hf = findViewById(R.id.audio_ll_hf)
        audio_iv_small = findViewById(R.id.audio_iv_small)

        audio_ll_recording?.setOnClickListener(this)
        audio_ll_answer?.setOnClickListener(this)
        audio_ll_hangup?.setOnClickListener(this)
        audio_ll_hf?.setOnClickListener(this)
        audio_iv_small?.setOnClickListener(this)

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.audio_ll_recording -> {
                //录音
                if (isRecording) {
                    isRecording = false
                    CloudManager.getInstance().stopAudioRecording()
                    audio_iv_recording?.setImageResource(R.drawable.img_recording)
                } else {
                    isRecording = true
                    //录音
                    CloudManager.getInstance().startAudioRecording(
                        "/sdcard/Meet/" + System.currentTimeMillis() + ".wav"
                    )
                    audio_iv_recording?.setImageResource(R.drawable.img_recording_p)
                }
            }
            R.id.audio_ll_answer -> {
                //接听

                CloudManager.getInstance().acceptCall(callId)
            }
            R.id.audio_ll_hangup -> {

                //挂断
                CloudManager.getInstance().hangUpCall(callId)

                if (isRecording) {
                    isRecording = false
                    CloudManager.getInstance().stopAudioRecording()
                    audio_iv_recording?.setImageResource(R.drawable.img_recording)
                }

                if (isHF) {
                    isHF = false
                    CloudManager.getInstance().setEnableSpeakerphone(isHF)
                    audio_iv_hf?.setImageResource(if (isHF) R.drawable.img_hf_p else R.drawable.img_hf)

                }

            }
            R.id.audio_ll_hf -> {
                //免提
                isHF = !isHF
                CloudManager.getInstance().setEnableSpeakerphone(isHF)
                audio_iv_hf?.setImageResource(if (isHF) R.drawable.img_hf_p else R.drawable.img_hf)
            }
            R.id.audio_iv_small -> {

                //最小化
                mFullScreenChatAudio?.toSmall()

            }
        }
    }

    fun updateWindowInfo(mType: Int) {
        when (mType) {
            CallingTo -> updateWindowInfo(
                recording = false,
                answer = false,
                hangup = true,
                hf = false,
                small = false
            )
            Receiver -> updateWindowInfo(
                recording = false,
                answer = true,
                hangup = true,
                hf = false,
                small = false
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
                            audio_iv_photo
                        )
                        if (mType == CallingTo) {
                            audio_tv_status?.text =
                                context.getString(R.string.text_service_calling, imUser?.nickName)
                        } else if (mType == Receiver) {
                            audio_tv_status?.text =
                                context.getString(R.string.text_service_call_ing, imUser?.nickName)
                        }
                    }
                }
            }
        })

    }

    //更新个人信息
    fun updateWindowInfo(
        recording: Boolean, answer: Boolean, hangup: Boolean, hf: Boolean,
        small: Boolean
    ) {

        // 录音 接听 挂断 免提 最小化
        audio_ll_recording?.visibility = if (recording) VISIBLE else GONE
        audio_ll_answer?.visibility = if (answer) VISIBLE else GONE
        audio_ll_hangup?.visibility = if (hangup) VISIBLE else GONE
        audio_ll_hf?.visibility = if (hf) VISIBLE else GONE
        audio_iv_small?.visibility = if (small) VISIBLE else GONE
    }

    fun setTime(time: String) {
        audio_tv_status?.text = time
    }

    /**
     * 保存音频记录
     *
     * @param id
     * @param callStatus
     */
    fun saveAudioRecord(id: String, callStatus: Int) {
        LitePalHelper.getInstance()
            .saveCallRecord(id, CallRecord.MEDIA_TYPE_AUDIO, callStatus)
    }

    interface FullScreenChatAudio {
        fun toSmall()
    }

}