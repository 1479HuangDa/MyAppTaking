package com.example.framework.cloud;

import android.content.Context;
import android.net.Uri;
import android.widget.Toast;

import com.example.framework.R;
import com.example.framework.event.EventManager;
import com.example.framework.event.MessageEvent;
import com.example.framework.utils.FileUtil;
import com.example.framework.utils.LogUtils;
import com.example.framework.utils.PermissionWrapper;
import com.example.framework.utils.audio.AudioRecordHelper;

import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.rongcloud.rtc.api.RCRTCConfig;
import cn.rongcloud.rtc.api.RCRTCEngine;
import cn.rongcloud.rtc.api.stream.RCRTCAudioStreamConfig;
import cn.rongcloud.rtc.base.RCRTCParamsType;
import io.rong.calllib.IRongCallListener;
import io.rong.calllib.IRongReceivedCallListener;
import io.rong.calllib.RongCallClient;
import io.rong.calllib.RongCallCommon;
import io.rong.imlib.IRongCallback;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.message.ImageMessage;
import io.rong.message.LocationMessage;
import io.rong.message.TextMessage;
import me.yokeyword.fragmentation.SupportActivity;

public class CloudManager {

    //Url
    public static final String TOKEN_URL = "http://api-cn.ronghub.com/user/getToken.json";
    //Key
    public static final String CLOUD_KEY = "pvxdm17jpww0r";
    public static final String CLOUD_SECRET = "Sv5q7QNvbj";


    //ObjectName
    public static final String MSG_TEXT_NAME = "RC:TxtMsg";
    public static final String MSG_IMAGE_NAME = "RC:ImgMsg";
    public static final String MSG_LOCATION_NAME = "RC:LBSMsg";

    //Msg Type

    //普通消息
    public static final String TYPE_TEXT = "TYPE_TEXT";
    //添加好友消息
    public static final String TYPE_ADD_FRIEND = "TYPE_ADD_FRIEND";
    //同意添加好友的消息
    public static final String TYPE_ARGEED_FRIEND = "TYPE_ARGEED_FRIEND";

    //来电铃声
    public static final String callAudioPath = "http://downsc.chinaz.net/Files/DownLoad/sound1/201501/5363.wav";
    //挂断铃声
    public static final String callAudioHangup = "http://downsc.chinaz.net/Files/DownLoad/sound1/201501/5351.wav";

    private static volatile CloudManager mInstnce = null;

    private AudioRecordHelper audioRecordHelper = null;

    private CloudManager() {

    }

    public static CloudManager getInstance() {
        if (mInstnce == null) {
            synchronized (CloudManager.class) {
                if (mInstnce == null) {
                    mInstnce = new CloudManager();
                }
            }
        }
        return mInstnce;
    }


    /**
     * 免提开关
     *
     * @param enabled
     */
    public void setEnableSpeakerphone(boolean enabled) {
        RongCallClient.getInstance().setEnableSpeakerphone(enabled);
    }

    /**
     * 挂断
     *
     * @param callId
     */
    public void hangUpCall(String callId) {
        LogUtils.i("hangUpCall:" + callId);
        RongCallClient.getInstance().hangUpCall(callId);
    }


    /**
     * 接听
     *
     * @param callId
     */
    public void acceptCall(String callId) {
        LogUtils.i("acceptCall:" + callId);
        RongCallClient.getInstance().acceptCall(callId);
    }

    /**
     * 开启录音
     *
     * @param filename
     */
    public void startAudioRecording(String filename) {
        if (audioRecordHelper != null) return;
        if (filename.contains(".")) {
            filename = filename.substring(0, filename.lastIndexOf("."));
        }

        if (filename.contains("/")) {
            filename = filename.substring(filename.lastIndexOf("/"), filename.length() - 1);
        }

        File file = FileUtil.createFile("meet", filename);
        audioRecordHelper = new AudioRecordHelper(file, null);
        audioRecordHelper.recordAsync();
//        RongCallClient.getInstance().
//        RongCallClient.getInstance().startAudioRecording(filePath);
    }

    /**
     * 关闭录音
     */
    public void stopAudioRecording() {
        if (audioRecordHelper != null) {
            audioRecordHelper.stop(false);
            audioRecordHelper = null;
        }

//        RongCallClient.getInstance().stopAudioRecording();
    }


    /**
     * 监听通话状态
     *
     * @param listener
     */
    public void setVoIPCallListener(IRongCallListener listener) {
        if (null == listener) {
            return;
        }
        RongCallClient.getInstance().setVoIPCallListener(listener);
    }

    /**
     * 检查设备是否可用通话
     *
     * @param mContext
     */
    public boolean isVoIPEnabled(Context mContext) {
        if (!RongCallClient.getInstance().isVoIPEnabled(mContext)) {
            Toast.makeText(mContext, R.string.text_devices_not_supper_audio, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }


    /**
     * 查询本地的会话记录
     *
     * @param callback
     */
    public void getConversationList(RongIMClient.ResultCallback<List<Conversation>> callback) {
        RongIMClient.getInstance().getConversationList(callback);
    }


    /**
     * 监听音频通话
     *
     * @param listener
     */
    public void setReceivedCallListener(IRongReceivedCallListener listener) {
        if (null == listener) {
            return;
        }
        RongCallClient.setReceivedCallListener(listener);
    }


    /**
     * 初始化SDK
     *
     * @param mContext
     */
    public void initCloud(Context mContext) {
        RongIMClient.init(mContext, CLOUD_KEY);

        RCRTCConfig rcrtcConfig = RCRTCConfig.Builder.create()

                //采样率
                .setAudioSampleRate(16000)

                //码率
                .setAudioBitrate(30)

                //音源设置
                .setAudioSource(android.media.MediaRecorder.AudioSource.VOICE_COMMUNICATION)

                 //立体声
                .enableStereo(false)

                //是否启用麦克风
                .enableMicrophone(true)
                .build();

        RCRTCEngine.getInstance().init(mContext, rcrtcConfig);





    }

    public void setAudioConfig(){
        RCRTCAudioStreamConfig audioStreamConfig= RCRTCAudioStreamConfig.Builder.create()

                //回声消除模式
                .setEchoCancel(RCRTCParamsType.AECMode.AEC_MODE2)

                //设置回音消除滤波器开关
                .enableEchoFilter(false)

                //噪声抑制方案
                .setNoiseSuppression(RCRTCParamsType.NSMode.NS_MODE3)

                //噪声抑制级别
                .setNoiseSuppressionLevel(RCRTCParamsType.NSLevel.NS_HIGH)
                .build();
        RCRTCEngine.getInstance().getDefaultAudioStream().setAudioConfig(audioStreamConfig);
    }


    /**
     * 连接融云服务
     *
     * @param token
     */
    public void connect(String token) {
        LogUtils.i("connect");
        RongIMClient.connect(token, new RongIMClient.ConnectCallback() {
//            @Override
//            public void onTokenIncorrect() {
//                LogUtils.e("Token Error");
//            }

            @Override
            public void onSuccess(String s) {
                LogUtils.e("连接成功：" + s);
                sendConnectStatus(true);
//                sendTextMessage("hellow","9bd150d29b");
            }

            @Override
            public void onError(RongIMClient.ConnectionErrorCode connectionErrorCode) {

            }

            @Override
            public void onDatabaseOpened(RongIMClient.DatabaseOpenStatus errorCode) {
                if (RongIMClient.ConnectionErrorCode.RC_CONN_TOKEN_INCORRECT.getValue() == errorCode.getValue()) {
                    //从 APP 服务获取新 token，并重连
                } else {
                    //无法连接 IM 服务器，请根据相应的错误码作出对应处理
                    sendConnectStatus(false);
                }
            }
//
//            @Override
//            public void onError(RongIMClient.ErrorCode errorCode) {
//                LogUtils.e("连接失败：" + errorCode);
//                sendConnectStatus(false);
//            }
        });
    }

    private void sendConnectStatus(boolean b) {

    }

    /**
     * 加载本地的历史记录
     *
     * @param targetId
     * @param callback
     */
    public void getHistoryMessages(String targetId, RongIMClient.ResultCallback<List<Message>> callback) {
        RongIMClient.getInstance().getHistoryMessages(Conversation.ConversationType.PRIVATE
                , targetId, -1, 1000, callback);
    }


    /**
     * 获取服务器的历史记录
     *
     * @param targetId
     * @param callback
     */
    public void getRemoteHistoryMessages(String targetId, RongIMClient.ResultCallback<List<Message>> callback) {
        RongIMClient.getInstance().getRemoteHistoryMessages(Conversation.ConversationType.PRIVATE
                , targetId, 0, 20, callback);
    }


    /**
     * 断开连接
     */
    public void disconnect() {
        RongIMClient.getInstance().disconnect();
    }

    /**
     * 退出登录
     */
    public void logout() {
        RongIMClient.getInstance().logout();
    }

    /**
     * 监听连接状态
     *
     * @param listener
     */
    public void setConnectionStatusListener(RongIMClient.ConnectionStatusListener listener) {
        RongIMClient.setConnectionStatusListener(listener);
    }


    /**
     * 接收消息的监听器
     *
     * @param listener
     */
    public void setOnReceiveMessageListener(RongIMClient.OnReceiveMessageListener listener) {
        RongIMClient.setOnReceiveMessageListener(listener);
    }


    /**
     * 发送消息的结果回调
     */
    private IRongCallback.ISendMessageCallback iSendMessageCallback
            = new IRongCallback.ISendMessageCallback() {

        @Override
        public void onAttached(Message message) {
            // 消息成功存到本地数据库的回调
        }

        @Override
        public void onSuccess(Message message) {
            // 消息发送成功的回调
            LogUtils.i("sendMessage onSuccess" + message.toString());
            MessageEvent event = new MessageEvent(EventManager.FLAG_SEND_MESSAGE_SUCCEED);
            event.setMsg(message);
            event.setUserId(message.getTargetId());
            EventManager.post(event);
        }

        @Override
        public void onError(Message message, RongIMClient.ErrorCode errorCode) {
            // 消息发送失败的回调
            LogUtils.e("sendMessage onError:" + errorCode);
            MessageEvent event = new MessageEvent(EventManager.FLAG_SEND_MESSAGE_ERROR);
            event.setMsg(message);
            EventManager.post(event);
        }
    };

    /**
     * 发送文本消息
     * 一个手机 发送
     * 另外一个手机 接收
     *
     * @param msg
     * @param targetId
     */
    private void sendTextMessage(String msg, String targetId) {
        LogUtils.i("sendTextMessage");
        TextMessage textMessage = TextMessage.obtain(msg);
        RongIMClient.getInstance().sendMessage(
                Conversation.ConversationType.PRIVATE,
                targetId,
                textMessage,
                null,
                null,
                iSendMessageCallback
        );
    }


    private RongIMClient.SendImageMessageCallback sendImageMessageCallback = new RongIMClient.SendImageMessageCallback() {
        @Override
        public void onAttached(Message message) {
            LogUtils.i("onAttached");
        }

        @Override
        public void onError(Message message, RongIMClient.ErrorCode errorCode) {
            LogUtils.i("onError:" + errorCode);
            LogUtils.e("sendMessage onError:" + errorCode);
            MessageEvent event = new MessageEvent(EventManager.FLAG_SEND_MESSAGE_ERROR);
            event.setMsg(message);
            EventManager.post(event);
        }

        @Override
        public void onSuccess(Message message) {
            LogUtils.i("onSuccess");
            // 消息发送成功的回调
            LogUtils.i("sendMessage onSuccess" + message.toString());
            MessageEvent event = new MessageEvent(EventManager.FLAG_SEND_MESSAGE_SUCCEED);
            event.setMsg(message);
            event.setUserId(message.getTargetId());
            EventManager.post(event);
        }

        @Override
        public void onProgress(Message message, int i) {
            LogUtils.i("onProgress:" + i);
        }
    };

    /**
     * 发送图片消息
     *
     * @param targetId 对方ID
     * @param file     文件
     */
    public void sendImageMessage(String targetId, File file) {
        ImageMessage imageMessage = ImageMessage.obtain(Uri.fromFile(file), Uri.fromFile(file), true);
        RongIMClient.getInstance().sendImageMessage(
                Conversation.ConversationType.PRIVATE,
                targetId,
                imageMessage,
                null,
                null,
                sendImageMessageCallback);
    }


    /**
     * 发送位置信息
     *
     * @param mTargetId
     * @param lat
     * @param lng
     * @param poi
     */
    public void sendLocationMessage(String mTargetId, double lat, double lng, String poi) {
        LocationMessage locationMessage = LocationMessage.obtain(lat, lng, poi, null);
        io.rong.imlib.model.Message message = io.rong.imlib.model.Message.obtain(
                mTargetId, Conversation.ConversationType.PRIVATE, locationMessage);
        RongIMClient.getInstance().sendLocationMessage(message,
                null, null, iSendMessageCallback);
    }

    /**
     * 发送文本消息
     *
     * @param msg
     * @param type
     * @param targetId
     */
    public void sendTextMessage(String msg, String type, String targetId) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("msg", msg);
            //如果没有这个Type 就是一条普通消息
            jsonObject.put("type", type);
            sendTextMessage(jsonObject.toString(), targetId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 是否连接
     *
     * @return
     */
    public boolean isConnect() {
        return RongIMClient.getInstance().getCurrentConnectionStatus()
                == RongIMClient.ConnectionStatusListener.ConnectionStatus.CONNECTED;
    }


    /**
     * 拨打视频/音频
     *
     * @param targetId
     * @param type
     */
    public void startCall(Context mContext, String targetId, RongCallCommon.CallMediaType type) {
        //检查设备可用
        if (!isVoIPEnabled(mContext)) {
            return;
        }
        if(!isConnect()){
            Toast.makeText(mContext, R.string.text_server_status, Toast.LENGTH_SHORT).show();
            return;
        }
        List<String> userIds = new ArrayList<>();
        userIds.add(targetId);
        RongCallClient.getInstance().startCall(
                Conversation.ConversationType.PRIVATE,
                targetId,
                userIds,
                null,
                type,
                null);
    }

    /**
     * 音频
     *
     * @param targetId
     */
    public void startAudioCall(Context _mContext, @Nullable String targetId) {

        startCall(_mContext, targetId, RongCallCommon.CallMediaType.AUDIO);
    }


    /**
     * 视频
     *
     * @param targetId
     */
    public void startVideoCall(@Nullable Context _mContext, @Nullable String targetId) {
        startCall(_mContext, targetId, RongCallCommon.CallMediaType.VIDEO);
    }
}
