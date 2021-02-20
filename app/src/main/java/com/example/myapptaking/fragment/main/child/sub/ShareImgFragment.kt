package com.example.myapptaking.fragment.main.child.sub

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import com.example.framework.base.BaseLazyDataBindingFragment
import com.example.framework.bmob.BmobManager
import com.example.framework.helper.FileHelper
import com.example.framework.helper.GlideHelper
import com.example.framework.view.LodingView
import com.example.myapptaking.R
import com.example.myapptaking.databinding.FragmentShareImgBinding
import com.uuzuche.lib_zxing.activity.CodeUtils

class ShareImgFragment : BaseLazyDataBindingFragment<FragmentShareImgBinding>(),
    View.OnClickListener {

    companion object {
        fun newInstance(): ShareImgFragment {
            val args = Bundle()

            val fragment = ShareImgFragment()
            fragment.arguments = args
            return fragment
        }
    }

    private var mLodingView: LodingView? = null

    override fun layoutId(): Int {
        return R.layout.fragment_share_img
    }


    override fun initView(view: View) {
       val mToolbar: Toolbar =view.findViewById(R.id.mToolbar)
        mToolbar.setTitle(R.string.text_me_item_title_5)
        mToolbar.setNavigationOnClickListener { onBackPressedSupport() }

        viewBinder.onViewClick = this
    }

    override fun lazyInit() {

        mLodingView = LodingView(_mActivity)
        mLodingView?.setLodingText(R.string.text_shar_save_ing)

        loadInfo()
    }

    /**
     * 加载个人信息
     */
    private fun loadInfo() {
        val imUser = BmobManager.instance?.user

        GlideHelper.loadUrl(this, imUser?.photo, viewBinder.ivPhoto)

        viewBinder.tvName.text = imUser?.nickName?.trim()

        viewBinder.tvSex.setText(
            if (imUser?.isSex == true)
                R.string.text_me_info_boy
            else
                R.string.text_me_info_girl
        )

        viewBinder.tvAge.text =
            _mActivity.getString(R.string.text_search_age, imUser?.age.toString())

        viewBinder.tvPhone.text = imUser?.mobilePhoneNumber

        viewBinder.tvDesc.text = imUser?.desc

        createQRCode(imUser?.objectId)
    }


    /**
     * 创建二维码
     */
    private fun createQRCode(userId: String?) {
        /**
         * View的绘制
         */

        viewBinder.ivQrcode.post {
            val textContent = "Meet#$userId"
            val mBitmap = CodeUtils.createImage(
                textContent,
                viewBinder.ivQrcode.width, viewBinder.ivQrcode.height, null
            )
            viewBinder.ivQrcode.setImageBitmap(mBitmap)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ll_download -> {


                /**
                 * 1.View截图
                 * 2.创建一个Bitmap
                 * 3.保存到相册
                 */
                mLodingView?.show()


                /**
                 * setDrawingCacheEnabled
                 * 保留我们的绘制副本
                 * 1.重新测量
                 * 2.重新布局
                 * 3.得到我们的DrawingCache
                 * 4.转换成Bitmap
                 */

                viewBinder.llContent.isDrawingCacheEnabled = true

                viewBinder.llContent.measure(
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                )

                viewBinder.llContent.layout(
                    0, 0, viewBinder.llContent.measuredWidth,
                    viewBinder.llContent.measuredHeight
                )

                val mBitmap: Bitmap = viewBinder.llContent.drawingCache

                FileHelper.getInstance().saveBitmapToAlbum(_mActivity, mBitmap)

                mLodingView?.hide()
            }
        }
    }


}