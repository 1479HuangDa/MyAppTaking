package com.example.myapptaking.fragment.main.child.chat.sub

import android.os.Bundle
import android.view.View
import cn.bmob.v3.exception.BmobException
import cn.bmob.v3.listener.FindListener
import com.chad.library.adapter.base.BaseQuickAdapter
import com.example.framework.base.SimpleListFragment
import com.example.framework.bmob.BmobManager
import com.example.framework.bmob.IMUser
import com.example.framework.cloud.CloudManager
import com.example.framework.gson.TextBean
import com.example.framework.model.CommonBean
import com.example.framework.recycler.JssBaseViewHolder
import com.example.framework.utils.CommonUtils
import com.example.framework.utils.LogUtils
import com.example.myapptaking.R
import com.example.myapptaking.fragment.main.child.model.ChatRecordModel
import com.example.myapptaking.fragment.main.child.sub.chat.ChatPanelFragment
import com.google.gson.Gson
import ikidou.reflect.TypeBuilder
import io.rong.imlib.RongIMClient
import io.rong.imlib.model.Conversation
import io.rong.message.TextMessage
import java.lang.reflect.Type
import java.text.SimpleDateFormat

class ChatRecordFragment : SimpleListFragment<ChatRecordModel>(), LabInfo {

    companion object {
        fun newInstance(): ChatRecordFragment {
            val args = Bundle()
            val fragment = ChatRecordFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override var labelTitle = R.string.text_chat_tab_title_1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mSuperBackPressedSupport=false
        isEnableLoadMore=false
    }


    override fun getItemLayout(): Int {
        return R.layout.fragment_chat_record
    }

    override fun convertItem(helper: JssBaseViewHolder?, item: ChatRecordModel?) {
        helper?.setImageNetUrl(R.id.iv_photo, item?.url, R.drawable.img_glide_load_error)
            ?.setText(R.id.tv_nickname, item?.nickName)
            ?.setText(R.id.tv_content, item?.endMsg)
            ?.setText(R.id.tv_time, item?.time)
            ?.setText(R.id.tv_un_read, "${item?.unReadSize}")
            ?.setViewVisible(R.id.tv_un_read,item?.unReadSize?:0 > 0)
    }

    override fun getListType(): Type {
        return TypeBuilder.newInstance(CommonBean::class.java)
            .beginSubType(List::class.java)
            .addTypeParam(ChatRecordModel::class.java).endSubType().build()
    }

    override fun netRequest() {
        queryChatRecord()
    }

    override fun onItemClick(adapter: BaseQuickAdapter<*, *>, view: View, position: Int) {
        super.onItemClick(adapter, view, position)
        val item = mAdapter.getItem(position)
        start(
            ChatPanelFragment.newInstance(
                item?.userId,
                item?.nickName,
                item?.url
            )
        )
    }

    private fun queryChatRecord() {
        CloudManager.getInstance()
            .getConversationList(object : RongIMClient.ResultCallback<List<Conversation?>?>() {
                override fun onSuccess(conversations: List<Conversation?>?) {
                    LogUtils.i("onSuccess")
                    mSwipeRefreshLayout.isRefreshing = false
                    if (CommonUtils.isEmpty(conversations)) {
                        if (mData.size > 0) {
                            mData.clear()
                        }
                        val conversationLs = conversations ?: emptyList()

                        for (conversation in conversationLs) {

                            val id = conversation?.targetId

                            //查询对象的信息
                            BmobManager.instance
                                ?.queryObjectIdUser(id, object : FindListener<IMUser?>() {
                                    override fun done(list: List<IMUser?>?, e: BmobException?) {
                                        if (e == null) {
                                            if (CommonUtils.isEmpty(list)) {
                                                val imUser: IMUser? = list?.get(0)
                                                val chatRecordModel = ChatRecordModel()

                                                chatRecordModel.userId = imUser?.objectId

                                                chatRecordModel.url = imUser?.photo

                                                chatRecordModel.nickName = imUser?.nickName

                                                chatRecordModel.time = SimpleDateFormat("HH:mm:ss")
                                                    .format(conversation?.receivedTime)

                                                chatRecordModel.unReadSize =
                                                    conversation?.unreadMessageCount ?: 0

                                                val objectName = conversation?.objectName

                                                if (objectName == CloudManager.MSG_TEXT_NAME) {
                                                    val textMessage =
                                                        conversation.latestMessage as TextMessage
                                                    val msg = textMessage.content
                                                    val bean: TextBean =
                                                        gson.fromJson(msg, TextBean::class.java)
                                                    if (bean.type
                                                            .equals(CloudManager.TYPE_TEXT)
                                                    ) {
                                                        chatRecordModel.endMsg = bean.getMsg()
                                                        mData.add(chatRecordModel)
                                                    }
                                                } else if (objectName == CloudManager.MSG_IMAGE_NAME) {
                                                    chatRecordModel.endMsg =
                                                        _mActivity.getString(R.string.text_chat_record_img)
                                                    mData.add(chatRecordModel)
                                                } else if (objectName == CloudManager.MSG_LOCATION_NAME) {
                                                    chatRecordModel.endMsg =
                                                        _mActivity.getString(R.string.text_chat_record_location)
                                                    mData.add(chatRecordModel)
                                                }
                                                mAdapter.notifyDataSetChanged()

                                                if (mData.isEmpty()) {
                                                    onFailed()
                                                }

                                                if (mSwipeRefreshLayout.isRefreshing) {
                                                    mSwipeRefreshLayout.isRefreshing = false
                                                }
                                            }
                                        } else {
                                            onFailed()
                                        }
                                    }
                                })
                        }
                    } else {
                        onFailed()
                    }
                }

                override fun onError(errorCode: RongIMClient.ErrorCode) {
                    LogUtils.i("onError$errorCode")
                    onFailed()
                }
            })
    }
}