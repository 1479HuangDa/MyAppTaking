package com.example.framework.manager;

import android.annotation.TargetApi;
import android.app.AppOpsManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.core.app.NotificationCompat;


import com.example.framework.R;
import com.example.framework.utils.SpUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * FileName: NotificationHelper
 * Founder: LiuGuiLin
 * Profile: 通知栏管理
 */
public class NotificationHelper {

    private static final String CHECK_OP_NO_THROW = "checkOpNoThrow";

    private static final String OP_POST_NOTIFICATION = "OP_POST_NOTIFICATION";


    //添加好友
    public static final String CHANNEL_ADD_FRIEND = "add_friend";
    //同意好友
    public static final String CHANNEL_AGREED_FRIEND = "agreed_friend";
    //消息
    public static final String CHANNEL_MESSAGE = "message";

    private static NotificationHelper mInstance = null;

    private Context mContext;
    private NotificationManager notificationManager;

    private List<String> mIdList = new ArrayList<>();

    private NotificationHelper() {

    }

    public static NotificationHelper getInstance() {
        if (mInstance == null) {
            synchronized (NotificationHelper.class) {
                if (mInstance == null) {
                    mInstance = new NotificationHelper();
                }
            }
        }
        return mInstance;
    }

    public void createChannel(Context mContext) {
        this.mContext = mContext;
        notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            String channelId = CHANNEL_ADD_FRIEND;
            String channelName = mContext.getString(R.string.text_chat_add_friend_channl);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            createNotificationChannel(channelId, channelName, importance);

            channelId = CHANNEL_AGREED_FRIEND;
            channelName = mContext.getString(R.string.text_chat_argeed_friend_channl);
            importance = NotificationManager.IMPORTANCE_HIGH;
            createNotificationChannel(channelId, channelName, importance);

            channelId = CHANNEL_MESSAGE;
            channelName = mContext.getString(R.string.text_chat_friend_msg_channl);
            importance = NotificationManager.IMPORTANCE_HIGH;
            createNotificationChannel(channelId, channelName, importance);
        }
    }

    private void createNotificationChannel(String channelId, String channelName, int importance) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
            notificationManager.createNotificationChannel(channel);
        }
    }


    @TargetApi(Build.VERSION_CODES.KITKAT)
    public boolean isNotificationEnabled() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ///< 8.0手机以上
            if (((NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE)).getImportance() == NotificationManager.IMPORTANCE_NONE) {
                return false;
            }
        }

        AppOpsManager mAppOps = (AppOpsManager) mContext.getSystemService(Context.APP_OPS_SERVICE);
        ApplicationInfo appInfo = mContext.getApplicationInfo();
        String pkg = mContext.getApplicationContext().getPackageName();
        int uid = appInfo.uid;

        Class appOpsClass;
        try {
            appOpsClass = Class.forName(AppOpsManager.class.getName());
            Method checkOpNoThrowMethod = appOpsClass.getMethod(CHECK_OP_NO_THROW, Integer.TYPE, Integer.TYPE,
                    String.class);
            Field opPostNotificationValue = appOpsClass.getDeclaredField(OP_POST_NOTIFICATION);

            int value = (Integer) opPostNotificationValue.get(Integer.class);
            return ((Integer) checkOpNoThrowMethod.invoke(mAppOps, value, uid, pkg) == AppOpsManager.MODE_ALLOWED);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    /**
     * 通知权限申请
     */
    public void requestNotify() {
        /**
         * 跳到通知栏设置界面
         * @param context
         */
        ApplicationInfo appInfo = mContext.getApplicationInfo();
        String pkg = mContext.getApplicationContext().getPackageName();
        int uid = appInfo.uid;
        try {
            Intent intent = new Intent();
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                //这种方案适用于 API 26, 即8.0（含8.0）以上可以用
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, pkg);
                intent.putExtra(Settings.EXTRA_CHANNEL_ID, uid);

            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                //这种方案适用于 API21——25，即 5.0——7.1 之间的版本可以使用
                intent.putExtra("app_package", pkg);
                intent.putExtra("app_uid", uid);


            } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {

                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.setData(Uri.parse("package:" + pkg));

            } else {
                intent.setAction(Settings.ACTION_SETTINGS);
                intent.setData(Uri.fromParts("package", pkg, null));
            }
            mContext.startActivity(intent);
        } catch (Exception e) {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setData(Uri.parse("package:" + pkg));
            mContext.startActivity(intent);

        }
    }


    /**
     * 发送通知
     *
     * @param objectid  发送人ID
     * @param channelId 渠道ID
     * @param title     标题
     * @param text      内容
     * @param mBitmap   头像
     * @param intent    跳转目标
     */
    private void pushNotification(String objectid, String channelId, String title, String text, Bitmap mBitmap, PendingIntent intent) {
        //对开关进行限制
        boolean isTips = SpUtils.getInstance().getBoolean("isTips", true);
        if (!isTips) {
            return;
        }

        Notification notification = new NotificationCompat.Builder(mContext, channelId)
                .setContentTitle(title)
                .setContentText(text)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.icon_message)
                .setLargeIcon(mBitmap)
                .setContentIntent(intent)
                .setAutoCancel(true)
                .build();

        if (!mIdList.contains(objectid)) {
            mIdList.add(objectid);
        }
        notificationManager.notify(mIdList.indexOf(objectid), notification);
    }

    /**
     * 发送添加好友的通知
     */
    public void pushAddFriendNotification(String objectid, String title, String text, Bitmap mBitmap, PendingIntent intent) {
        pushNotification(objectid, CHANNEL_ADD_FRIEND, title, text, mBitmap, intent);
    }

    /**
     * 发送同意好友的通知
     */
    public void pushArgeedFriendNotification(String objectid, String title, String text, Bitmap mBitmap, PendingIntent intent) {
        pushNotification(objectid, CHANNEL_AGREED_FRIEND, title, text, mBitmap, intent);
    }

    /**
     * 发送消息的通知
     */
    public void pushMessageNotification(String objectid, String title, String text, Bitmap mBitmap, PendingIntent intent) {
        pushNotification(objectid, CHANNEL_MESSAGE, title, text, mBitmap, intent);
    }
}
