package com.example.myapptaking.fragment.main.child

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import com.example.framework.base.BaseLazyDataBindingFragment
import com.example.framework.helper.FileHelper
import com.example.myapptaking.R
import com.example.myapptaking.databinding.FragmentQrCodeBinding
import com.uuzuche.lib_zxing.activity.CaptureFragment
import com.uuzuche.lib_zxing.activity.CodeUtils
import com.uuzuche.lib_zxing.activity.CodeUtils.AnalyzeCallback

class QrCodeFragment : BaseLazyDataBindingFragment<FragmentQrCodeBinding>(), View.OnClickListener {

    companion object {

        private const val REQUEST_IMAGE = 1234

        fun newInstance(): QrCodeFragment {
            val args = Bundle()

            val fragment = QrCodeFragment()
            fragment.arguments = args
            return fragment
        }
    }


    var mQrCodeCallbackListener: QrCodeCallbackListener? = null

    private val analyzeCallback: AnalyzeCallback = object : AnalyzeCallback {
        override fun onAnalyzeSuccess(mBitmap: Bitmap, result: String) {
            mQrCodeCallbackListener?.onResult(result)
        }

        override fun onAnalyzeFailed() {
            mQrCodeCallbackListener?.onResult("")
        }
    }

    //是否打开闪光灯
    private var isOpenLight = false

    override fun lazyInit() {
        initQrCode()
    }

    /**
     * 初始化二维码
     */
    private fun initQrCode() {
        val captureFragment = CaptureFragment()
        CodeUtils.setFragmentArgs(captureFragment, R.layout.layout_qrcode)
        captureFragment.analyzeCallback = analyzeCallback
        childFragmentManager.beginTransaction()
            .replace(R.id.fl_my_container, captureFragment).commit()
    }

    override fun initView(view: View) {
        viewBinder.onViewClick = this
    }

    override fun layoutId(): Int {
        return R.layout.fragment_qr_code
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.iv_flashlight -> {
                try {
                    isOpenLight = !isOpenLight
                    CodeUtils.isLightEnable(isOpenLight)
                    viewBinder.ivFlashlight.setImageResource(
                        if (isOpenLight)
                            R.drawable.img_flashlight_p
                        else
                            R.drawable.img_flashlight
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            R.id.iv_to_ablum -> {
                openAlbum()
            }
            R.id.iv_back -> {
                onBackPressedSupport()
            }
        }
    }

    /**
     * 打开相册
     */
    private fun openAlbum() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE) {
            if (data != null) {
                val uri = data.data
                val path: String = FileHelper.getInstance()
                    .getRealPathFromURI(_mActivity, uri)
                try {
                    CodeUtils.analyzeBitmap(path, object : AnalyzeCallback {
                        override fun onAnalyzeSuccess(mBitmap: Bitmap, result: String) {
                            analyzeCallback.onAnalyzeSuccess(mBitmap, result)
                        }

                        override fun onAnalyzeFailed() {
                            analyzeCallback.onAnalyzeFailed()
                        }
                    })
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onBackPressedSupport(): Boolean {
        pop()
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        mQrCodeCallbackListener = null
    }

    interface QrCodeCallbackListener {
        fun onResult(result: String)
    }
}