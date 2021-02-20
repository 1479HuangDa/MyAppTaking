package com.example.myapptaking.service;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.view.SurfaceView;

import androidx.annotation.Nullable;
import androidx.arch.core.executor.ArchTaskExecutor;

import com.example.framework.bmob.BmobManager;
import com.example.framework.bmob.IMUser;
import com.example.framework.cloud.CloudManager;
import com.example.framework.db.CallRecord;
import com.example.framework.db.LitePalHelper;
import com.example.framework.db.NewFriend;
import com.example.framework.entity.Constants;
import com.example.framework.event.EventManager;
import com.example.framework.event.MessageEvent;
import com.example.framework.gson.TextBean;
import com.example.framework.helper.GlideHelper;
import com.example.framework.helper.WindowHelper;
import com.example.framework.manager.MediaPlayerManager;
import com.example.framework.manager.NotificationHelper;
import com.example.framework.utils.CommonUtils;
import com.example.framework.utils.LogUtils;
import com.example.framework.utils.SpUtils;
import com.example.framework.utils.TimeUtils;
import com.example.myapptaking.MainActivity;
import com.example.myapptaking.R;
import com.example.myapptaking.fragment.main.child.sub.chat.ChatPanelFragment;
import com.example.myapptaking.fragment.main.child.sub.NewFriendFragment;
import com.example.myapptaking.fragment.main.child.view.media_chat.ChatSmallAudioView;
import com.example.myapptaking.fragment.main.child.view.media_chat.ChatVideoView;
import com.example.myapptaking.fragment.main.child.view.media_chat.FullScreenChatAudioView;
import com.example.myapptaking.fragment.main.child.view.media_chat.MediaChatView;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.List;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import io.rong.calllib.IRongCallListener;
import io.rong.calllib.IRongReceivedCallListener;
import io.rong.calllib.RongCallClient;
import io.rong.calllib.RongCallCommon;
import io.rong.calllib.RongCallSession;
import io.rong.imlib.model.Message;
import io.rong.message.ImageMessage;
import io.rong.message.LocationMessage;
import io.rong.message.TextMessage;

public class CloudService extends Service implements FullScreenChatAudioView.FullScreenChatAudio {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private ChatSmallAudioView mSmallAudioView;

    private ChatVideoView mFullVideoView;

    private FullScreenChatAudioView mFullAudioView;

    //拨打状态
    private int isCallTo = 0;
    //接听状态
    private int isReceiverTo = 0;
    //拨打还是接听
    private boolean isCallOrReceiver = true;

    private boolean CallConnected = false;
    //计时
    private static final int H_TIME_WHAT = 1000;

    //通话时间
    private int callTimer = 0;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case H_TIME_WHAT:
                    callTimer++;
                    String time = TimeUtils.formatDuring(callTimer * 1000);

                    mSmallAudioView.setTime(time);
                    mFullVideoView.setTime(time);
                    mFullAudioView.setTime(time);

                    mHandler.sendEmptyMessageDelayed(H_TIME_WHAT, 1000);
                    break;
            }
            return false;
        }
    });

    //媒体类
    private MediaPlayerManager mAudioCallMedia;
    private MediaPlayerManager mAudioHangupMedia;

    private final Gson gson = new Gson();

    @Override
    public void onCreate() {
        super.onCreate();

        initService();
        initWindow();
        linkCloudServer();
    }

    private void initWindow() {
        //音频
        mFullAudioView = new FullScreenChatAudioView(this);
        mFullAudioView.setFullScreenChatAudio(this);


        //视频
        mFullVideoView = new ChatVideoView(this);

        createSmallAudioView();
    }

    /**
     * 创建最小化的音频窗口
     */
    private void createSmallAudioView() {
        mSmallAudioView = new ChatSmallAudioView(this);
        mSmallAudioView.setOnClickListener(v -> {
            //最大化
            WindowHelper.getInstance().hideView(mSmallAudioView);
            WindowHelper.getInstance().showView(mFullAudioView);
        });
    }

    private void initService() {
        //来电铃声
        mAudioCallMedia = new MediaPlayerManager();
        //挂断铃声
        mAudioHangupMedia = new MediaPlayerManager();

        //无限循环
//        mAudioCallMedia.setLooping(true);
        mAudioCallMedia.setOnComplteionListener(mp -> {
                    if (!CallConnected) {
                        mAudioCallMedia.startPlay(CloudManager.callAudioPath);
                    }
                }
        );
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;

    }

    private void linkCloudServer() {
        //获取Token
        String token = SpUtils.getInstance().getString(Constants.SP_TOKEN, "");
        LogUtils.e("token:" + token);
        //连接服务
        CloudManager.getInstance().connect(token);
        //接收消息
        CloudManager.getInstance().setOnReceiveMessageListener((message, i) -> {
            parsingImMessage(message);
            return false;
        });


        //监听通话
        CloudManager.getInstance().setReceivedCallListener(new IRongReceivedCallListener() {
            @Override
            public void onReceivedCall(RongCallSession rongCallSession) {
                LogUtils.i("rongCallSession");
                CallConnected = false;
                //检查设备可用
                if (!CloudManager.getInstance().isVoIPEnabled(CloudService.this)) {
                    return;
                }

                /**
                 * 1.获取拨打和接通的ID
                 * 2.来电的话播放铃声
                 * 3.加载个人信息去填充
                 * 4.显示Window
                 */

                //呼叫端的ID
                String callUserId = rongCallSession.getCallerUserId();

                //通话ID
                String callId = rongCallSession.getCallId();

                //播放来电铃声
                mAudioCallMedia.startPlay(CloudManager.callAudioPath);


                if (rongCallSession.getMediaType().equals(RongCallCommon.CallMediaType.AUDIO)) {

                    //通话ID
                    mFullAudioView.setCallId(callId);

                    //呼叫端的ID
                    mFullAudioView.setCallUserId(callUserId);

                    //更新个人信息
                    mFullAudioView.updateWindowInfo(MediaChatView.Receiver);


                    WindowHelper.getInstance().showView(mFullAudioView);
                } else if (rongCallSession.getMediaType().equals(RongCallCommon.CallMediaType.VIDEO)) {

                    //通话ID
                    mFullVideoView.setCallId(callId);

                    //呼叫端的ID
                    mFullVideoView.setCallUserId(callUserId);

                    //更新个人信息
                    mFullVideoView.updateWindowInfo(MediaChatView.Receiver);

                    WindowHelper.getInstance().showView(mFullVideoView);
                }

                isReceiverTo = 1;

                isCallOrReceiver = false;
            }

            @Override
            public void onCheckPermission(RongCallSession rongCallSession) {
                LogUtils.i("onCheckPermission:" + rongCallSession.toString());
                RongCallClient.getInstance().onPermissionGranted();
            }
        });

        //监听通话状态
        CloudManager.getInstance().setVoIPCallListener(new IRongCallListener() {

            //电话拨出
            @Override
            public void onCallOutgoing(RongCallSession rongCallSession, SurfaceView surfaceView) {
                LogUtils.i("onCallOutgoing");

                CallConnected = false;

                isCallOrReceiver = true;

                isCallTo = 1;

                mAudioCallMedia.startPlay(CloudManager.callAudioPath);

                String targetId = rongCallSession.getTargetId();
                //更新信息
//                updateWindowInfo(1, rongCallSession.getMediaType(), targetId);

                //通话ID
                String callId = rongCallSession.getCallId();

                if (rongCallSession.getMediaType().equals(RongCallCommon.CallMediaType.AUDIO)) {

                    mFullAudioView.setCallId(callId);

                    mFullAudioView.setCallUserId(targetId);

                    //更新信息
                    mFullAudioView.updateWindowInfo(MediaChatView.CallingTo);

                    WindowHelper.getInstance().showView(mFullAudioView);
                } else if (rongCallSession.getMediaType().equals(RongCallCommon.CallMediaType.VIDEO)) {

                    mFullVideoView.setCallId(callId);

                    mFullVideoView.setCallUserId(targetId);

                    //更新信息
                    mFullVideoView.updateWindowInfo(MediaChatView.CallingTo);

                    WindowHelper.getInstance().showView(mFullVideoView);
//                    //显示摄像头
                    mFullVideoView.setLocalView(surfaceView);

//                    mLocalView = surfaceView;
//                    video_big_video.addView(mLocalView);
                }

            }

            //已建立通话
            @Override
            public void onCallConnected(RongCallSession rongCallSession, SurfaceView surfaceView) {
                LogUtils.i("onCallConnected");

                CloudManager.getInstance().setAudioConfig();

                CallConnected = true;

                /**
                 * 1.开始计时
                 * 2.关闭铃声
                 * 3.更新按钮
                 */

                isCallTo = 2;
                isReceiverTo = 2;

                //关闭铃声
                if (mAudioCallMedia.isPlaying()) {
                    mAudioCallMedia.stopPlay();
                }

                //开始计时
                mHandler.sendEmptyMessage(H_TIME_WHAT);

                if (rongCallSession.getMediaType().equals(RongCallCommon.CallMediaType.AUDIO)) {
                    mFullAudioView.updateWindowInfo(true, false, true, true, true);

                } else if (rongCallSession.getMediaType().equals(RongCallCommon.CallMediaType.VIDEO)) {
                    mFullVideoView.updateWindowInfo(false, true, true, false, true, true);
                    mFullVideoView.setLocalView22(surfaceView);

                }

            }

            //通话结束
            @Override
            public void onCallDisconnected(RongCallSession rongCallSession, RongCallCommon.CallDisconnectedReason callDisconnectedReason) {
                LogUtils.i("onCallDisconnected");

                String callUserId = rongCallSession.getCallerUserId();
                String recevierId = rongCallSession.getTargetId();

                //关闭计时
                mHandler.removeMessages(H_TIME_WHAT);

                //铃声挂断
                mAudioCallMedia.pausePlay();

                //播放挂断铃声
                mAudioHangupMedia.startPlay(CloudManager.callAudioHangup);

                //重置计时器
                callTimer = 0;

                if (rongCallSession.getMediaType().equals(RongCallCommon.CallMediaType.AUDIO)) {
                    if (isCallOrReceiver) {
                        if (isCallTo == 1) {
                            //代表只拨打，但是并没有接通
                            mFullAudioView.saveAudioRecord(recevierId, CallRecord.CALL_STATUS_DIAL);
                        } else if (isCallTo == 2) {
                            mFullAudioView.saveAudioRecord(recevierId, CallRecord.CALL_STATUS_ANSWER);
                        }
                    } else {
                        if (isReceiverTo == 1) {
                            mFullAudioView.saveAudioRecord(callUserId, CallRecord.CALL_STATUS_UN_ANSWER);
                        } else if (isReceiverTo == 2) {
                            mFullAudioView.saveAudioRecord(callUserId, CallRecord.CALL_STATUS_ANSWER);
                        }
                    }

                } else if (rongCallSession.getMediaType().equals(RongCallCommon.CallMediaType.VIDEO)) {
                    if (isCallOrReceiver) {
                        if (isCallTo == 1) {
                            //代表只拨打，但是并没有接通
                            mFullVideoView.saveVideoRecord(recevierId, CallRecord.CALL_STATUS_DIAL);
                        } else if (isCallTo == 2) {
                            mFullVideoView.saveVideoRecord(recevierId, CallRecord.CALL_STATUS_ANSWER);
                        }
                    } else {
                        if (isReceiverTo == 1) {
                            mFullVideoView.saveVideoRecord(callUserId, CallRecord.CALL_STATUS_UN_ANSWER);
                        } else if (isReceiverTo == 2) {
                            mFullVideoView.saveVideoRecord(callUserId, CallRecord.CALL_STATUS_ANSWER);
                        }
                    }
                }

                //如果出现异常,可能无法退出
                WindowHelper.getInstance().hideView(mFullAudioView);
                WindowHelper.getInstance().hideView(mSmallAudioView);
                WindowHelper.getInstance().hideView(mFullVideoView);

                isCallTo = 0;
                isReceiverTo = 0;
            }

            //被叫端正在响铃
            @Override
            public void onRemoteUserRinging(String s) {

            }

            //被叫端加入通话
            @Override
            public void onRemoteUserJoined(String s, RongCallCommon.CallMediaType callMediaType, int i, SurfaceView surfaceView) {
                //子线程
//                MessageEvent event = new MessageEvent(EventManager.FLAG_SEND_CAMERA_VIEW);
//                event.setmSurfaceView(surfaceView);
//                EventManager.post(event);
                mHandler.post(() -> {
                    mFullVideoView.setMRemoteView(surfaceView);
                    mFullVideoView.updateVideoView();
                });
            }

            //通话中的某一个参与者邀请好友加入
            @Override
            public void onRemoteUserInvited(String s, RongCallCommon.CallMediaType callMediaType) {

            }

            //通话中的远程参与者离开
            @Override
            public void onRemoteUserLeft(String s, RongCallCommon.CallDisconnectedReason callDisconnectedReason) {

            }

            //媒体切换
            @Override
            public void onMediaTypeChanged(String s, RongCallCommon.CallMediaType callMediaType, SurfaceView surfaceView) {

            }

            //发生错误
            @Override
            public void onError(RongCallCommon.CallErrorCode callErrorCode) {

            }

            //远程端摄像头发生变化
            @Override
            public void onRemoteCameraDisabled(String s, boolean b) {

            }

            //远程端麦克风发生变化
            @Override
            public void onRemoteMicrophoneDisabled(String s, boolean b) {

            }

            //接收丢包率
            @Override
            public void onNetworkReceiveLost(String s, int i) {

            }

            //发送丢包率
            @Override
            public void onNetworkSendLost(int i, int i1) {

            }

            //收到视频第一帧
            @Override
            public void onFirstRemoteVideoFrame(String s, int i, int i1) {

            }

            @Override
            public void onAudioLevelSend(String s) {

            }

            @Override
            public void onAudioLevelReceive(HashMap<String, String> hashMap) {

            }

            @Override
            public void onRemoteUserPublishVideoStream(String s, String s1, String s2, SurfaceView surfaceView) {

            }

            @Override
            public void onRemoteUserUnpublishVideoStream(String s, String s1, String s2) {

            }
        });

    }

    private void parsingImMessage(Message message) {
        LogUtils.i(message.toString());

        String objectName = message.getObjectName();

        if (objectName.equals(CloudManager.MSG_TEXT_NAME)) {
            TextMessage textMessage = (TextMessage) message.getContent();
            String content = textMessage.getContent();
            TextBean textBean = null;
            try {
                textBean = gson.fromJson(content, TextBean.class);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (null == textBean) {
                //系统调试消息
                MessageEvent event = new MessageEvent(EventManager.FLAG_SEND_TEXT);
                event.setText(content);
                event.setUserId(message.getSenderUserId());
                EventManager.post(event);
                pushSystem(message.getSenderUserId(), 1, 0, 0, content);
                return;
            }

            String type = textBean.getType();
            switch (type) {
                case CloudManager.TYPE_TEXT:
                    //普通消息
                    MessageEvent event = new MessageEvent(EventManager.FLAG_SEND_TEXT);
                    event.setUserId(message.getSenderUserId());
                    event.setMsg(message);
                    EventManager.post(event);
                    pushSystem(message.getSenderUserId(), 1, 0, 0, textBean.getMsg());
                    break;
                case CloudManager.TYPE_ADD_FRIEND:
                    //添加好友消息,存入本地数据库
                    saveNewFriend(textBean.getMsg(), message.getSenderUserId());
//                    LitePalHelper.getInstance()
//                            .saveNewFriend(textBean.getMsg(),message.getSenderUserId());

                    break;
                case CloudManager.TYPE_ARGEED_FRIEND:
                    //同意添加好友的消息
                    BmobManager manager = BmobManager.getInstance();
                    if (manager == null) return;
                    manager.addFriend(message.getSenderUserId(), new SaveListener<String>() {
                        @Override
                        public void done(String s, BmobException e) {
                            if (e == null) {
                                pushSystem(message.getSenderUserId(), 0, 1, 0, "");
                                //2.刷新好友列表
                                EventManager.post(EventManager.FLAG_UPDATE_FRIEND_LIST);
                            }
                        }
                    });
                    break;
            }
        } else if (objectName.equals(CloudManager.MSG_IMAGE_NAME)) {
            try {
                ImageMessage imageMessage = (ImageMessage) message.getContent();
                String url = imageMessage.getRemoteUri().toString();
                if (!TextUtils.isEmpty(url)) {
                    LogUtils.i("url:" + url);
                    MessageEvent event = new MessageEvent(EventManager.FLAG_SEND_IMAGE);
                    event.setImgUrl(url);
                    event.setUserId(message.getSenderUserId());
                    event.setMsg(message);
                    EventManager.post(event);
                    pushSystem(message.getSenderUserId(), 1, 0, 0, getString(R.string.text_chat_record_img));
                }
            } catch (Exception e) {
                LogUtils.e("e." + e.toString());
                e.printStackTrace();
            }
        } else if (objectName.equals(CloudManager.MSG_LOCATION_NAME)) {
            LocationMessage locationMessage = (LocationMessage) message.getContent();
            LogUtils.e("locationMessage:" + locationMessage.toString());
            MessageEvent event = new MessageEvent(EventManager.FLAG_SEND_LOCATION);
            event.setLa(locationMessage.getLat());
            event.setLo(locationMessage.getLng());
            event.setUserId(message.getSenderUserId());
            event.setAddress(locationMessage.getPoi());
            event.setMsg(message);
            EventManager.post(event);
            pushSystem(message.getSenderUserId(), 1, 0, 0, getString(R.string.text_chat_record_location));
        }
    }

    /**
     * 保存新朋友
     *
     * @param msg
     * @param senderUserId
     */
    @SuppressLint("RestrictedApi")
    private void saveNewFriend(String msg, String senderUserId) {
        pushSystem(senderUserId, 0, 0, 0, msg);
        ArchTaskExecutor.getIOThreadExecutor().execute(() -> {
            List<NewFriend> newFriends = LitePalHelper.getInstance().queryNewFriend();
            int index = -100;
            for (int j = 0; j < newFriends.size(); j++) {
                NewFriend newFriend = newFriends.get(j);
                if (senderUserId.equals(newFriend.getId())) {
                    index = j;
                    break;
                }
            }
            //防止重复添加
            if (index < 0) {
                LitePalHelper.getInstance().saveNewFriend(msg, senderUserId);
            } else {
                NewFriend newFriend = newFriends.get(index);
                newFriend.setIsAgree(-1);
                newFriend.save();
//                LitePalHelper.getInstance().updateNewFriend(senderUserId,-1);
            }
        });

    }

    /**
     * @param id          发消息id
     * @param type        0：特殊消息 1：聊天消息
     * @param friendType  0: 添加好友请求 1：同意好友请求
     * @param messageType 0：文本  1：图片 2：位置
     */
    private void pushSystem(final String id, final int type, final int friendType, final int messageType, final String msgText) {
        LogUtils.i("pushSystem");
        BmobManager instance = BmobManager.getInstance();
        if (instance == null) return;
        instance.queryObjectIdUser(id, new FindListener<IMUser>() {
            @Override
            public void done(List<IMUser> list, BmobException e) {
                if (e == null) {
                    if (CommonUtils.isEmpty(list)) {
                        IMUser imUser = list.get(0);
                        String text = "";
                        if (type == 0) {
                            switch (friendType) {
                                case 0:
                                    text = imUser.getNickName() + getString(R.string.text_server_noti_send_text);
                                    break;
                                case 1:
                                    text = imUser.getNickName() + getString(R.string.text_server_noti_receiver_text);
                                    break;
                            }
                        } else if (type == 1) {
                            switch (messageType) {
                                case 0:
                                    text = msgText;
                                    break;
                                case 1:
                                    text = getString(R.string.text_chat_record_img);
                                    break;
                                case 2:
                                    text = getString(R.string.text_chat_record_location);
                                    break;
                            }
                        }
                        pushBitmap(type, friendType, imUser, imUser.getNickName(), text, imUser.getPhoto());
                    }
                }
            }
        });
    }

    /**
     * 发送通知
     *
     * @param type       0：特殊消息 1：聊天消息
     * @param friendType 0: 添加好友请求 1：同意好友请求
     * @param imUser     用户对象
     * @param title      标题
     * @param text       内容
     * @param url        头像Url
     */
    private void pushBitmap(
            final int type,
            final int friendType,
            final IMUser imUser,
            final String title,
            final String text,
            String url
    ) {
        LogUtils.i("pushBitmap");
        GlideHelper.loadUrlToBitmap(this, url, new GlideHelper.OnGlideBitmapResultListener() {
            @Override
            public void onResourceReady(Bitmap resource) {
                if (type == 0) {
                    if (friendType == 0) {
                        Intent intent = new Intent(CloudService.this, MainActivity.class);
                        intent.putExtra(Constants.PAGE_PATH, NewFriendFragment.Path);
                        PendingIntent pi = PendingIntent.getActivities(CloudService.this, 0, new Intent[]{intent}, PendingIntent.FLAG_CANCEL_CURRENT);
                        NotificationHelper.getInstance().pushAddFriendNotification(imUser.getObjectId(), title, text, resource, pi);
                    } else if (friendType == 1) {
                        Intent intent = new Intent(CloudService.this, MainActivity.class);
                        PendingIntent pi = PendingIntent.getActivities(CloudService.this, 0, new Intent[]{intent}, PendingIntent.FLAG_CANCEL_CURRENT);
                        NotificationHelper.getInstance().pushArgeedFriendNotification(imUser.getObjectId(), title, text, resource, pi);
                    }
                } else if (type == 1) {
                    Intent intent = new Intent(CloudService.this, MainActivity.class);
                    intent.putExtra(Constants.PAGE_PATH, ChatPanelFragment.Path);
                    intent.putExtra(Constants.INTENT_USER_ID, imUser.getObjectId());
                    intent.putExtra(Constants.INTENT_USER_NAME, imUser.getNickName());
                    intent.putExtra(Constants.INTENT_USER_PHOTO, imUser.getPhoto());
                    PendingIntent pi = PendingIntent.getActivities(CloudService.this, 0, new Intent[]{intent}, PendingIntent.FLAG_CANCEL_CURRENT);
                    NotificationHelper.getInstance().pushMessageNotification(imUser.getObjectId(), title, text, resource, pi);
                }
            }
        });
    }

    @Override
    public void toSmall() {
        WindowHelper.getInstance().hideView(mFullAudioView);
        WindowHelper.getInstance().showView(mSmallAudioView, mSmallAudioView.getLpSmallView());
    }
}
