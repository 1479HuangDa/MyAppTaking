package com.example.framework.bmob

import android.content.Context
import cn.bmob.v3.Bmob
import cn.bmob.v3.BmobQuery
import cn.bmob.v3.BmobSMS
import cn.bmob.v3.BmobUser
import cn.bmob.v3.datatype.BmobFile
import cn.bmob.v3.exception.BmobException
import cn.bmob.v3.listener.*
import com.example.framework.utils.CommonUtils
import com.example.framework.utils.LogUtils
import java.io.File

class BmobManager private constructor() {

    companion object {
        private const val BMOB_SDK_ID = "8cfc57f122b9ff2f2e02221a10ddf48f"
        private const val BMOB_NEW_DOMAIN = "http://sdk.cilc.cloud/8/"

        @Volatile
        private var mInstance: BmobManager? = null

        @kotlin.jvm.JvmStatic
        val instance: BmobManager?
            get() {
                if (mInstance == null) {
                    synchronized(BmobManager::class.java) {
                        if (mInstance == null) {
                            mInstance = BmobManager()
                        }
                    }
                }
                return mInstance
            }
    }

    /**
     * 初始化Bmob
     *
     * @param mContext
     */
    fun initBmob(mContext: Context?) {
        //如果Bmob绑定独立域名，则需要在初始化之前重置
        Bmob.resetDomain(BMOB_NEW_DOMAIN)
        Bmob.initialize(mContext, BMOB_SDK_ID)
    }

    /**
     * 是否登录
     *
     * @return
     */
    val isLogin: Boolean
        get() = BmobUser.isLogin()

    /**
     * 获取本地对象
     *
     * @return
     */
    val user: IMUser
        get() = BmobUser.getCurrentUser(IMUser::class.java)

    /**
     * 同步控制台信息至本地缓存
     */
    fun fetchUserInfo(listener: FetchUserInfoListener<BmobUser?>?) {
        BmobUser.fetchUserInfo(listener)
    }

    /**
     * 发送短信验证码
     *
     * @param phone    手机号码
     * @param listener 回调
     */
    fun requestSMS(phone: String?, listener: QueryListener<Int?>?) {
        BmobSMS.requestSMSCode(phone, "", listener)
    }

    /**
     * 通过手机号码注册或者登陆
     *
     * @param phone    手机号码
     * @param code     短信验证码
     * @param listener 回调
     */
    fun signOrLoginByMobilePhone(phone: String?, code: String?, listener: LogInListener<IMUser>?) {
        BmobUser.signOrLoginByMobilePhone(phone, code, listener)

    }

    fun LogOut() = BmobUser.logOut()

    /**
     * 账号密码登录
     *
     * @param userName
     * @param pw
     * @param listener
     */
    fun loginByAccount(userName: String?, pw: String?, listener: SaveListener<IMUser?>) {
        val imUser = IMUser()
        imUser.username = userName
        imUser.setPassword(pw)
        imUser.login(listener)
    }

    /**
     * 上传头像
     *
     * @param nickName
     * @param file
     * @param listener
     */
    fun uploadFirstPhoto(nickName: String?, file: File?, listener: OnUploadPhotoListener) {
        /**
         * 1.上传文件拿到地址
         * 2.更新用户信息
         */
        val imUser = user
        //独立域名 设置 - 应用配置 - 独立域名 一年/100
        //免费的办法： 使用我的Key
        //你如果怕数据冲突
        //解决办法：和我的类名不一样即可
        val bmobFile = BmobFile(file)
        bmobFile.uploadblock(object : UploadFileListener() {
            override fun done(e: BmobException?) {
                if (e == null) {
                    //上传成功
                    imUser.nickName = nickName
                    imUser.photo = bmobFile.fileUrl
                    imUser.tokenNickName = nickName
                    imUser.tokenPhoto = bmobFile.fileUrl

                    //更新用户信息
                    imUser.update(object : UpdateListener() {
                        override fun done(e: BmobException?) {
                            if (e == null) {
                                listener.OnUpdateDone()
                            } else {
                                listener.OnUpdateFail(e)
                            }
                        }
                    })
                } else {
                    listener.OnUpdateFail(e)
                }
            }
        })
    }

    interface OnUploadPhotoListener {
        fun OnUpdateDone()
        fun OnUpdateFail(e: BmobException?)
    }

    /**
     * 根据电话号码查询用户
     *
     * @param phone
     */
    fun queryPhoneUser(phone: String?, listener: FindListener<IMUser?>?) {
        baseQuery("mobilePhoneNumber", phone, listener)
    }

    fun queryPhoneUsers(phone: List<String>?, listener: FindListener<IMUser?>?) {
        baseQuerys("mobilePhoneNumber", phone, listener)
    }

    /**
     * 根据objectId查询用户
     *
     * @param objectId
     * @param listener
     */
    fun queryObjectIdUser(objectId: String?, listener: FindListener<IMUser?>?) {
        baseQuery("objectId", objectId, listener)
    }

    fun queryObjectIdUsers(objectIds: List<String>?, listener: FindListener<IMUser?>?) {
        baseQuerys("objectId", objectIds, listener)
    }


    /**
     * 查询我的好友
     *
     * @param listener
     */
    fun queryMyFriends(listener: FindListener<Friend?>) {
        val query = BmobQuery<Friend>()
        query.addWhereEqualTo("user", user)
        query.findObjects(listener)
    }

    /**
     * 查询所有的用户
     *
     * @param listener
     */
    fun queryAllUser(listener: FindListener<IMUser?>) {
        val query = BmobQuery<IMUser>()
        query.findObjects(listener)
    }

    /**
     * 查询所有的广场的数据
     *
     * @param listener
     */
    fun queryAllSquare(listener: FindListener<SquareSet?>) {
        val query = BmobQuery<SquareSet>()
        query.setLimit(500)
        query.findObjects(listener)
    }

    /**
     * 查询私有库
     *
     * @param listener
     */
    fun queryPrivateSet(listener: FindListener<PrivateSet?>) {
        val query = BmobQuery<PrivateSet>()
        query.findObjects(listener)
    }

    /**
     * 查询缘分池
     *
     * @param listener
     */
    fun queryFateSet(listener: FindListener<FateSet>?) {
        val query = BmobQuery<FateSet>()
        query.findObjects(listener)
    }

    /**
     * 查询基类
     *
     * @param key
     * @param values
     * @param listener
     */
    fun baseQuery(key: String?, values: String?, listener: FindListener<IMUser?>?) {
        val query = BmobQuery<IMUser?>()
        query.addWhereEqualTo(key, values)
        query.findObjects(listener)
    }

    fun baseQuerys(key: String?, values: List<String>?, listener: FindListener<IMUser?>?) {
        val query = BmobQuery<IMUser?>()
//        query.addWhereEqualTo(key, values)
        query.addWhereContainedIn(key, values)
        query.findObjects(listener)
    }

    /**
     * 添加好友
     *
     * @param imUser
     * @param listener
     */
    fun addFriend(imUser: IMUser?, listener: SaveListener<String?>?) {
        val friend = Friend()
        friend.user = user
        friend.friendUser = imUser
        friend.save(listener)
    }

    /**
     * 添加私有库
     *
     * @param listener
     */
    fun addPrivateSet(listener: SaveListener<String?>?) {
        val set = PrivateSet()
        set.userId = user.objectId
        set.phone = user.mobilePhoneNumber
        set.save(listener)
    }

    /**
     * 添加到缘分池中
     *
     * @param listener
     */
    fun addFateSet(listener: SaveListener<String?>?) {
        val set = FateSet()
        set.userId = user.objectId
        set.save(listener)
    }

    /**
     * 删除缘分池
     *
     * @param id
     * @param listener
     */
    fun delFateSet(id: String?, listener: UpdateListener?) {
        val set = FateSet()
        set.objectId = id
        set.delete(listener)
    }

    /**
     * 删除私有库
     *
     * @param id
     * @param listener
     */
    fun delPrivateSet(id: String?, listener: UpdateListener?) {
        val set = PrivateSet()
        set.objectId = id
        set.delete(listener)
    }

    /**
     * 发布广场
     *
     * @param mediaType 媒体类型
     * @param text      文本
     * @param path      路径
     */
    fun pushSquare(mediaType: Int, text: String?, path: String?, listener: SaveListener<String?>?) {
        val squareSet = SquareSet()
        squareSet.userId = user.objectId
        squareSet.pushTime = System.currentTimeMillis()
        squareSet.text = text
        squareSet.mediaUrl = path
        squareSet.pushType = mediaType
        squareSet.save(listener)
    }

    /**
     * 通过ID添加好友
     *
     * @param id
     * @param listener
     */
    fun addFriend(id: String?, listener: SaveListener<String?>?) {
        queryObjectIdUser(id, object : FindListener<IMUser?>() {
            override fun done(list: List<IMUser?>, e: BmobException?) {
                if (e == null) {
                    if (CommonUtils.isEmpty(list)) {
                        val imUser = list[0]
                        addFriend(imUser, listener)
                    }
                }
            }
        })
    }

    /**
     * 删除好友
     *
     * @param id
     * @param listener
     */
    fun deleteFriend(id: String, listener: UpdateListener?) {
        /**
         * 从自己的好友列表中删除
         * 如果需要，也可以从对方好友中删除
         */
        queryMyFriends(object : FindListener<Friend?>() {
            override fun done(list: List<Friend?>, e: BmobException?) {
                if (e == null) {
                    if (CommonUtils.isEmpty(list)) {
                        for (i in list.indices) {
                            if (list[i]!!.friendUser.objectId == id) {
                                val friend = Friend()
                                friend.objectId = list[i]!!.objectId
                                friend.delete(listener)
                            }
                        }
                    }
                }
            }
        })
    }

    fun addUpdateSet() {
        val updateSet = UpdateSet()
        updateSet.versionCode = 2
        updateSet.path = "---"
        updateSet.desc = "---"
        updateSet.save(object : SaveListener<String>() {
            override fun done(s: String, e: BmobException) {
                LogUtils.i("s:" + s + "e:" + e.toString())
            }
        })
    }

    /**
     * 查询更新
     *
     * @param listener
     */
    fun queryUpdateSet(listener: FindListener<UpdateSet>?) {
        val bmobQuery = BmobQuery<UpdateSet>()
        bmobQuery.findObjects(listener)
    }


}