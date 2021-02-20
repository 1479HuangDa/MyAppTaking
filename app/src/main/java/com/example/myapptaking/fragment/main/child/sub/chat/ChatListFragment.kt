package com.example.myapptaking.fragment.main.child.sub.chat

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.effective.android.panel.PanelSwitchHelper
import com.example.framework.base.BaseUIFragment
import com.example.framework.base.MultiListFragment
import com.example.framework.bmob.BmobManager
import com.example.framework.bmob.IMUser
import com.example.framework.cloud.CloudManager
import com.example.framework.event.EventManager
import com.example.framework.event.MessageEvent
import com.example.framework.gson.TextBean
import com.example.framework.manager.MapManager
import com.example.framework.model.CommonBean
import com.example.framework.recycler.multi22.JssBaseMultiAdapter
import com.example.framework.utils.CommonUtils
import com.example.framework.utils.LogUtils
import com.example.myapptaking.R
import com.example.myapptaking.fragment.main.child.location.LocationFragment
import com.example.myapptaking.fragment.main.child.sub.ImagePreviewFragment
import com.example.myapptaking.fragment.main.child.sub.chat.model.*
import com.example.myapptaking.fragment.main.child.sub.chat.provider.ChatPanelImgProvider
import com.example.myapptaking.fragment.main.child.sub.chat.provider.ChatPanelLocationProvider
import com.example.myapptaking.fragment.main.child.sub.chat.provider.ChatPanelTextProvider
import com.example.myapptaking.fragment.main.child.sub.chat.uitl.ChatItemTouchListener
import ikidou.reflect.TypeBuilder
import io.rong.imlib.RongIMClient
import io.rong.imlib.model.Message
import io.rong.message.ImageMessage
import io.rong.message.LocationMessage
import io.rong.message.TextMessage
import java.lang.reflect.Type


class ChatListFragment : MultiListFragment<ChatPanelEntity?>() {

    companion object {


        fun newInstance(mTarget: ChatWithTargetModel?): ChatListFragment {
            val args = Bundle()
            args.putSerializable("mTarget", mTarget)

            val fragment = ChatListFragment()
            fragment.arguments = args
            return fragment
        }
    }

    private var mTarget: ChatWithTargetModel? = null

    private var iMUser: IMUser? = null


    var mHelper: PanelSwitchHelper? = null

    var unfilledHeight: UnfilledHeight? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mTarget = arguments?.getSerializable("mTarget") as ChatWithTargetModel

        isEnableLoadMore = false
        isAddDividerItemDecoration = false
        mSuperBackPressedSupport = false

        iMUser = BmobManager.instance?.user
    }

    override fun initView(view: View) {
        super.initView(view)
        list_empty_view?.removeAllViews()
        addEmptyView(R.layout.layout_empty_view)
        mRecyclerView?.overScrollMode = View.OVER_SCROLL_NEVER

        mRecyclerView?.addOnItemTouchListener(
            ChatItemTouchListener(mHelper)
        )

        mRecyclerView?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager
                if (layoutManager is LinearLayoutManager) {
                    val childCount = recyclerView.childCount
                    if (childCount > 0) {
                        val lastChildView = recyclerView.getChildAt(childCount - 1)
                        val bottom = lastChildView.bottom
                        val listHeight: Int =
                            recyclerView.height - recyclerView.paddingBottom
                        unfilledHeight?.setUnfilledHeight(listHeight - bottom)

                    }
                }
            }
        })
    }

    override fun addOnListItemProvider(mTAdapter: JssBaseMultiAdapter<ChatPanelEntity?>?) {
        mTAdapter?.addItemProvider(ChatPanelImgProvider())
        mTAdapter?.addItemProvider(ChatPanelTextProvider())
        mTAdapter?.addItemProvider(ChatPanelLocationProvider())
    }

    override fun netRequest() {
        queryMessage()
    }

    /**
     * 查询聊天记录
     */
    private fun queryMessage() {
        CloudManager.getInstance().getHistoryMessages(
            mTarget?.targetUserId,
            object : RongIMClient.ResultCallback<List<Message?>?>() {
                override fun onSuccess(messages: List<Message?>?) {
                    if (CommonUtils.isEmpty(messages)) {
                        try {
                            parsingListMessage(messages)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            onFailed()
                        }
                    } else {
                        queryRemoteMessage()
                    }
                }

                override fun onError(errorCode: RongIMClient.ErrorCode) {
                    LogUtils.e("errorCode:$errorCode")
                }
            })
    }

    /**
     * 解析历史记录
     *
     * @param messages
     */
    private fun parsingListMessage(messages: List<Message?>?) {

        //倒序
//        messages?.reversed()

        val mList = messages?.reversed() ?: emptyList()

        //遍历
        for (e in mList) {

            val mEntity = parsingMessage(e)

            if (mEntity != null) {

                mData.add(mEntity)
                mAdapter?.notifyDataSetChanged()
            }
        }

        if (mData.isEmpty()) {
            onFailed()
        }

        if (mSwipeRefreshLayout?.isRefreshing == true) {
            mSwipeRefreshLayout?.isRefreshing = false
        }

        scrollToBottom()
    }


    private fun parsingMessage(e: Message?): ChatPanelEntity? {
        val content = e?.content

        var mEntity: ChatPanelEntity? = null

        when (e?.objectName) {

            CloudManager.MSG_TEXT_NAME -> {
                val textMessage = content as TextMessage
                try {
                    val textBean = gson.fromJson(textMessage.content, TextBean::class.java)
                    if (textBean.type == CloudManager.TYPE_TEXT) {
                        mEntity = ChatPaneTextEntity()
                        textMessage.content = textBean.msg
                        mEntity.msg = textMessage
                    }

                } catch (e: java.lang.Exception) {
                    e.message
                }

            }
            CloudManager.MSG_IMAGE_NAME -> {
                val imageMessage = content as ImageMessage
                val remoteUri = imageMessage.remoteUri?.toString()
                if (remoteUri?.isNotEmpty() == true) {
                    mEntity = ChatPanelImgEntity()
                    mEntity.msg = imageMessage
                }

            }
            CloudManager.MSG_LOCATION_NAME -> {
                mEntity = ChatPaneLocationEntity()
                mEntity.msg = content as LocationMessage
            }
        }
        mEntity?.senderUserId = e?.senderUserId
        mEntity?.target = mTarget
        mEntity?.iMUser = iMUser
        return mEntity
    }


    /**
     * 查询服务器历史记录
     */
    private fun queryRemoteMessage() {
        CloudManager.getInstance().getRemoteHistoryMessages(
            mTarget?.targetUserId,
            object : RongIMClient.ResultCallback<List<Message?>?>() {
                override fun onSuccess(messages: List<Message?>?) {
                    if (CommonUtils.isEmpty(messages)) {
                        try {
                            parsingListMessage(messages)
                        } catch (e: java.lang.Exception) {
                            e.printStackTrace()
                            onFailed()
                        }
                    } else {
                        onFailed()
                    }
                }

                override fun onError(errorCode: RongIMClient.ErrorCode) {
                    LogUtils.e("errorCode:$errorCode")
                    onFailed()
                }
            })
    }

    override fun handMessageEvent(event: MessageEvent) {
//        super.handMessageEvent(event)
        if (event.userId != mTarget?.targetUserId) {
            return
        }
        when (event.type) {
            EventManager.FLAG_SEND_TEXT,
            EventManager.FLAG_SEND_IMAGE,
            EventManager.FLAG_SEND_MESSAGE_SUCCEED,
            EventManager.FLAG_SEND_LOCATION -> {
                val msg = event.msg
                val mEntity = parsingMessage(msg)
                if (mEntity != null) {
                    mAdapter?.addData(mEntity)
                }
                scrollToBottom()
            }

        }

    }

    override fun onItemClick(adapter: BaseQuickAdapter<*, *>, view: View, position: Int) {
        super.onItemClick(adapter, view, position)
        val item = mAdapter?.getItem(position)
        when (item?.itemType) {
            LOCATION -> {
                val realItem = item as ChatPaneLocationEntity
                val parentFragment = parentFragment
                val msg = realItem.msg
                val la = msg.lat
                val lo = msg.lng
                if (parentFragment is BaseUIFragment) {
                    LocationFragment.startRes(
                        parentFragment, false ,la, lo,
                        MapManager.getInstance().getMapUrl(la, lo)
                    )

                } else {
                    LocationFragment.startRes(
                        this, false,la, lo,
                        MapManager.getInstance().getMapUrl(la, lo)
                    )
                }

            }
            IMAGE->{
                val realItem=item as ChatPanelImgEntity
                val msg = realItem.msg
                start(ImagePreviewFragment.newInstance(true,msg?.remoteUri.toString()))
            }
        }
    }


    private fun scrollToBottom() {
        if (mData.isEmpty()) return
        val lastPos = mData.size - 1
        mRecyclerView?.scrollToPosition(lastPos)
    }

    override val listType: Type
        get() = TypeBuilder.newInstance(CommonBean::class.java)
            .beginSubType(List::class.java)
            .addTypeParam(ChatPanelEntity::class.java)
            .build()

    interface UnfilledHeight {
        fun setUnfilledHeight(newUnfilledHeight: Int)

    }
}