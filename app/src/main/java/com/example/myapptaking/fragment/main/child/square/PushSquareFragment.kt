package com.example.myapptaking.fragment.main.child.square

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import cn.bmob.v3.datatype.BmobFile
import cn.bmob.v3.exception.BmobException
import cn.bmob.v3.listener.SaveListener
import cn.bmob.v3.listener.UploadFileListener
import com.example.framework.base.BaseLazyDataBindingFragment
import com.example.framework.bmob.BmobManager
import com.example.framework.bmob.SquareSet
import com.example.framework.helper.FileHelper
import com.example.framework.view.LodingView
import com.example.myapptaking.R
import com.example.myapptaking.databinding.FragmentPushSquareBinding
import java.io.File

class PushSquareFragment:BaseLazyDataBindingFragment<FragmentPushSquareBinding>(),
    View.OnClickListener, Toolbar.OnMenuItemClickListener {

    companion object{
        fun newInstance(): PushSquareFragment {
            val args = Bundle()
            val fragment = PushSquareFragment()
            fragment.arguments = args
            return fragment
        }
    }

    //要上传的文件
    private var uploadFile: File? = null

    //媒体类型
    private var MediaType = SquareSet.PUSH_TEXT

    private var mLodingView: LodingView? = null

    var mPushSquareListener:PushSquareListener?=null

    override fun layoutId(): Int {
        return R.layout.fragment_push_square
    }

    override fun initView(view: View) {

        mLodingView = LodingView(_mActivity)
        mLodingView?.setLodingText(R.string.text_push_ing)

         viewBinder.onViewClick=this

        val mToolbar: Toolbar=view.findViewById(R.id.mToolbar)
        mToolbar.setTitle(R.string.text_square_psuh)
        mToolbar.setNavigationOnClickListener { onBackPressedSupport() }
        mToolbar.inflateMenu(R.menu.input_menu)
        mToolbar.setOnMenuItemClickListener(this)

        //输入框监听

        viewBinder.etContent.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            @SuppressLint("SetTextI18n")
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

                viewBinder.tvContentSize.text = "${s.length}/140"
            }

            override fun afterTextChanged(s: Editable) {}
        })
    }

    override fun lazyInit() {

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.iv_error -> {

                viewBinder.llMediaType.visibility = View.VISIBLE
                viewBinder.llMedia.visibility = View.GONE
                uploadFile = null
                MediaType = SquareSet.PUSH_TEXT
            }
            R.id.ll_camera -> FileHelper.getInstance().toCamera(this)
            R.id.ll_ablum -> FileHelper.getInstance().toAlbum(this)
            R.id.ll_music -> FileHelper.getInstance().toMusic(this)
            R.id.ll_video -> FileHelper.getInstance().toVideo(this)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            when (requestCode) {
                FileHelper.CAMEAR_REQUEST_CODE -> uploadFile = FileHelper.getInstance().tempFile
                FileHelper.ALBUM_REQUEST_CODE, FileHelper.MUSIC_REQUEST_CODE, FileHelper.VIDEO_REQUEST_CODE -> if (data != null) {
                    val uri = data.data
                    val path =
                        FileHelper.getInstance().getRealPathFromURI(_mActivity, uri)
                    if (!TextUtils.isEmpty(path)) {
                        if (path.endsWith(".jpg")
                            || path.endsWith(".png")
                            || path.endsWith(".jpeg")
                        ) {
                            MediaType = SquareSet.PUSH_IMAGE
                            //图片

                            viewBinder.tvMediaPath.setText(R.string.text_push_type_img)
                        } else if (path.endsWith("mp3")) {
                            MediaType = SquareSet.PUSH_MUSIC
                            //音乐
                            viewBinder.tvMediaPath.setText(R.string.text_push_type_music)
                        } else if (path.endsWith("mp4") ||
                            path.endsWith("wav") ||
                            path.endsWith("avi")
                        ) {
                            MediaType = SquareSet.PUSH_VIDEO
                            //视频
                            viewBinder.tvMediaPath.setText(R.string.text_push_type_video)

                        }
                        uploadFile = File(path)

                        viewBinder.llMediaType.visibility = View.GONE

                        viewBinder.llMedia.visibility = View.VISIBLE
                    }
                }
            }
        }

    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {

        when (item?.itemId) {
            R.id.menu_input -> inputSquare()
        }

        return true
    }

    private fun inputSquare() {
        val content: String = viewBinder.etContent.text.toString().trim { it <= ' ' }
        if (TextUtils.isEmpty(content) && uploadFile == null) {
            Toast.makeText(_mActivity, R.string.text_push_ed_null, Toast.LENGTH_SHORT).show()
            return
        }
        mLodingView?.show()
        if (uploadFile != null) {
            //上传文件
            val bmobFile = BmobFile(uploadFile)
            bmobFile.uploadblock(object : UploadFileListener() {
                override fun done(e: BmobException?) {
                    if (e == null) {
                        push(content, bmobFile.fileUrl)
                    }
                }
            })
        } else {
            push(content, "")
        }
    }

    /**
     * 发表
     *
     * @param content
     * @param path
     */
    private fun push(content: String, path: String) {

        BmobManager.instance
            ?.pushSquare(MediaType, content, path, object : SaveListener<String?>() {
                override fun done(s: String?, e: BmobException?) {
                    mLodingView?.hide()
                    if (e == null) {
                        mPushSquareListener?.onSuccess()
                        onBackPressedSupport()

                    } else {
                        Toast.makeText(
                            _mActivity,
                            R.string.text_push_fail,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            })
    }

    override fun onDestroy() {
        super.onDestroy()
        mPushSquareListener=null
    }

    interface PushSquareListener{
        fun onSuccess()
    }

}