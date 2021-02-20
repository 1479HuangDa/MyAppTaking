package com.example.myapptaking.fragment.main.child.sub

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.framework.base.BaseLazyDataBindingFragment
import com.example.framework.helper.FileHelper
import com.example.framework.helper.GlideHelper
import com.example.myapptaking.R
import com.example.myapptaking.databinding.FragmentImagePreviewBinding
import java.io.File

class ImagePreviewFragment : BaseLazyDataBindingFragment<FragmentImagePreviewBinding>(),
    View.OnClickListener {

    companion object {

        /**
         * @param isUrl
         * @param url
         */

        fun newInstance(isUrl: Boolean, url: String?): ImagePreviewFragment {
            val args = Bundle()
            args.putBoolean("isUrl", isUrl)
            args.putString("url", url)
            val fragment = ImagePreviewFragment()
            fragment.arguments = args
            return fragment
        }
    }

    //图片地址
    private var url: String = ""

    private var isUrl: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        url = arguments?.getString("url")?:""
        isUrl = arguments?.getBoolean("isUrl")?:false
    }

    override fun layoutId(): Int {
        return R.layout.fragment_image_preview
    }


    override fun initView(view: View) {
        viewBinder.onViewClick=this
    }

    override fun lazyInit() {
        if (isUrl) {
            GlideHelper.loadUrl(this, url, viewBinder.photoView)
        } else {
            GlideHelper.loadFile(this, File(url), viewBinder.photoView)
        }
    }

    override fun onClick(v: View?) {
         when(v?.id){
             R.id.iv_back -> {
                 onBackPressedSupport()
             }
             R.id.tv_download -> {
                 Toast.makeText(
                     _mActivity,
                     R.string.text_iv_pre_downloading,
                     Toast.LENGTH_SHORT
                 ).show()
                 GlideHelper.loadUrlToBitmap(this, url
                 ) { resource ->
                     if (resource != null) {
                         FileHelper.getInstance()
                             .saveBitmapToAlbum(_mActivity, resource)
                     } else {
                         Toast.makeText(
                             _mActivity,
                             R.string.text_iv_pre_save_fail,
                             Toast.LENGTH_SHORT
                         ).show()
                     }
                 }
             }
         }
    }


}