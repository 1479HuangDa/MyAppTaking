package com.example.myapptaking.fragment.main.child.sub

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import cn.bmob.v3.exception.BmobException
import com.example.framework.base.BaseUIFragment
import com.example.framework.bmob.BmobManager
import com.example.framework.bmob.BmobManager.OnUploadPhotoListener
import com.example.framework.event.EventManager
import com.example.framework.helper.FileHelper
import com.example.framework.manager.DialogManager
import com.example.framework.utils.LogUtils
import com.example.framework.view.DialogView
import com.example.framework.view.LodingView
import com.example.myapptaking.R
import com.permissionx.guolindev.PermissionX
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File


class FirstUploadFragment : BaseUIFragment(), View.OnClickListener {


    companion object {
        fun newInstance(): FirstUploadFragment {
            val args = Bundle()
            val fragment = FirstUploadFragment()
            fragment.arguments = args
            return fragment
        }
    }

    private var uploadFile: File? = null

    //圆形头像
    private var iv_photo: CircleImageView? = null
    private var et_nickname: EditText? = null
    private var btn_upload: Button? = null

    private var tv_camera: TextView? = null
    private var tv_ablum: TextView? = null
    private var tv_cancel: TextView? = null

    private var mLodingView: LodingView? = null
    private var mPhotoSelectView: DialogView? = null


    override fun getLayout(): Any {
        return R.layout.fragment_first_upload
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view)
    }

    private fun initView(view: View) {
        initPhotoView();
        iv_photo = view.findViewById(R.id.iv_photo)
        et_nickname = view.findViewById(R.id.et_nickname)
        btn_upload = view.findViewById(R.id.btn_upload)

        iv_photo?.setOnClickListener(this)
        btn_upload?.setOnClickListener(this)

        btn_upload?.isEnabled = false

        et_nickname?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                if (charSequence.isNotEmpty()) {
                    btn_upload?.isEnabled = uploadFile != null
                } else {
                    btn_upload?.isEnabled = false
                }
            }

            override fun afterTextChanged(editable: Editable) {}
        })
    }

    private fun initPhotoView() {
        mLodingView = LodingView(_mActivity)
        mLodingView?.setLodingText(getString(R.string.text_upload_photo_loding))

        mPhotoSelectView = DialogManager.getInstance()
            .initView(_mActivity, R.layout.dialog_select_photo, Gravity.BOTTOM)

        tv_camera = mPhotoSelectView?.findViewById(R.id.tv_camera)
        tv_camera?.setOnClickListener(this)
        tv_ablum = mPhotoSelectView?.findViewById(R.id.tv_ablum)
        tv_ablum?.setOnClickListener(this)
        tv_cancel = mPhotoSelectView?.findViewById(R.id.tv_cancel)
        tv_cancel?.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tv_camera -> {
                DialogManager.getInstance().hide(mPhotoSelectView)

                PermissionX.init(this)
                    .permissions(listOf(Manifest.permission.CAMERA))
                    .onExplainRequestReason { scope, deniedList ->
                        val msg = "获取相机权限"
                        val positive = "确定"
                        val negative = "取消"
                        scope.showRequestReasonDialog(deniedList, msg, positive, negative)
                    }.onForwardToSettings { scope, deniedList ->
                        val msg = "获取相机权限"
                        val positive = "确定"
                        val negative = "取消"
                        scope.showForwardToSettingsDialog(deniedList, msg, positive, negative)
                    }.request { allGranted, _, deniedList ->
                        if (!allGranted || deniedList.isNotEmpty()) {
                            Toast.makeText(_mActivity, "获取相机权限失败", Toast.LENGTH_SHORT).show()
                        } else {
                            //跳转到相机
                            FileHelper.getInstance().toCamera(this)
                        }

                    }


            }
            R.id.tv_ablum -> {
                DialogManager.getInstance().hide(mPhotoSelectView)
                //跳转到相册
                FileHelper.getInstance().toAlbum(this)
            }
            R.id.tv_cancel -> DialogManager.getInstance().hide(mPhotoSelectView)
            R.id.iv_photo ->                 //显示选择提示框
                DialogManager.getInstance().show(mPhotoSelectView)
            R.id.btn_upload -> uploadPhoto()
        }
    }

    private fun uploadPhoto() {
        //如果条件没有满足，是走不到这里的
        //如果条件没有满足，是走不到这里的
        val nickName = et_nickname?.text.toString().trim { it <= ' ' }
        mLodingView?.show()

        BmobManager.instance?.uploadFirstPhoto(nickName, uploadFile, object : OnUploadPhotoListener {
            override fun OnUpdateDone() {
                mLodingView?.hide()
                EventManager.post(EventManager.EVENT_REFRE_TOKEN_STATUS)
                pop()
            }

            override fun OnUpdateFail(e: BmobException?) {
                mLodingView?.hide()
                LogUtils.i(e?.message)
                Toast.makeText(_mActivity, e?.message, Toast.LENGTH_SHORT).show()
            }
        })

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        LogUtils.i("requestCode:$requestCode")
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == FileHelper.CAMEAR_REQUEST_CODE) {
                try {
                    FileHelper.getInstance().startPhotoZoom(this, FileHelper.getInstance().tempFile)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else if (requestCode == FileHelper.ALBUM_REQUEST_CODE) {
                val uri: Uri? = data?.data
                val path = FileHelper.getInstance().getRealPathFromURI(_mActivity, uri)
                if (path.isNotEmpty()) {
                    uploadFile = File(path)
                    try {
                        FileHelper.getInstance().startPhotoZoom(_mActivity, uploadFile)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            } else if (requestCode == FileHelper.CAMERA_CROP_RESULT) {
                LogUtils.i("CAMERA_CROP_RESULT")
                uploadFile = File(FileHelper.getInstance().cropPath)
                LogUtils.i("uploadPhotoFile:" + uploadFile?.path)
            }
            //设置头像
            if (uploadFile != null) {
                val mBitmap = BitmapFactory.decodeFile(uploadFile?.path)
                iv_photo?.setImageBitmap(mBitmap)

                //判断当前的输入框
                val nickName = et_nickname?.text.toString().trim { it <= ' ' }
                btn_upload?.isEnabled = nickName.isNotEmpty()
            }
        }
    }
}