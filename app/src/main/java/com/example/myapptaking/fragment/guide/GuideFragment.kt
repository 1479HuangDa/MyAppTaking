package com.example.myapptaking.fragment.guide

import android.animation.ObjectAnimator
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.example.framework.base.BasePageAdapter
import com.example.framework.base.BaseUIFragment
import com.example.framework.manager.MediaPlayerManager
import com.example.framework.utils.AnimUtils
import com.example.myapptaking.R


class GuideFragment : BaseUIFragment(), View.OnClickListener {

    companion object {
        fun newInstance(): GuideFragment {
            val args = Bundle()
            val fragment = GuideFragment()
            fragment.arguments = args
            return fragment
        }
    }

    private lateinit var iv_music_switch: ImageView

    private lateinit var tv_guide_skip: TextView

    private var iv_guide_point_1: ImageView? = null
    private var iv_guide_point_2: ImageView? = null
    private var iv_guide_point_3: ImageView? = null
    private var mViewPager: ViewPager? = null

    /**
     * 1.ViewPager : 适配器|帧动画播放
     * 2.小圆点的逻辑
     * 3.歌曲的播放
     * 4.属性动画旋转
     * 5.跳转
     */
    private lateinit var view1: View
    private lateinit var view2: View
    private lateinit var view3: View

    private var mPageList = mutableListOf<View>()
    private var mPageAdapter: BasePageAdapter? = null

    private var iv_guide_star: ImageView? = null
    private var iv_guide_night: ImageView? = null
    private var iv_guide_smile: ImageView? = null

    private var mGuideMusic: MediaPlayerManager? = null

    private var mAnim: ObjectAnimator? = null


    override fun getLayout(): Any {
        return R.layout.fragment_guide
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view)
    }

    private fun initView(view: View) {
        iv_music_switch = view.findViewById(R.id.iv_music_switch)
        tv_guide_skip = view.findViewById(R.id.tv_guide_skip)

        iv_guide_point_1 = view.findViewById(R.id.iv_guide_point_1)
        iv_guide_point_2 = view.findViewById(R.id.iv_guide_point_2)
        iv_guide_point_3 = view.findViewById(R.id.iv_guide_point_3)
        mViewPager = view.findViewById(R.id.mViewPager)

        iv_music_switch.setOnClickListener(this)

        tv_guide_skip.setOnClickListener(this)

        view1 = View.inflate(_mActivity, R.layout.layout_pager_guide_1, null)
        view2 = View.inflate(_mActivity, R.layout.layout_pager_guide_2, null)
        view3 = View.inflate(_mActivity, R.layout.layout_pager_guide_3, null)

        mPageList.add(view1)
        mPageList.add(view2)
        mPageList.add(view3)

        //预加载
        mViewPager?.offscreenPageLimit = mPageList.size

        mPageAdapter = BasePageAdapter(mPageList)
        mViewPager?.adapter = mPageAdapter


        //帧动画
        iv_guide_star = view1.findViewById(R.id.iv_guide_star)
        iv_guide_night = view2.findViewById(R.id.iv_guide_night)
        iv_guide_smile = view3.findViewById(R.id.iv_guide_smile)


        //播放帧动画
        val animStar = iv_guide_star?.background as AnimationDrawable
        animStar.start()

        val animNight = iv_guide_night?.background as AnimationDrawable
        animNight.start()

        val animSmile = iv_guide_smile?.background as AnimationDrawable
        animSmile.start()


        //小圆点逻辑
        mViewPager?.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                seletePoint(position)
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })

        //歌曲的逻辑
        startMusic()
    }

    /**
     * 播放音乐
     */
    private fun startMusic() {
        mGuideMusic = MediaPlayerManager()
        mGuideMusic?.setLooping(true)
        val file = resources.openRawResourceFd(R.raw.guide)
//        val file = resources.openRawResourceFd(R.raw.beep)
        mGuideMusic?.startPlay(file)

        mGuideMusic?.setOnComplteionListener { mGuideMusic?.startPlay(file) }

        //旋转动画
        mAnim = AnimUtils.rotation(iv_music_switch)
        mAnim?.start()
    }

    private fun seletePoint(position: Int) {
        when (position) {
            0 -> {
                iv_guide_point_1?.setImageResource(R.drawable.img_guide_point_p)
                iv_guide_point_2?.setImageResource(R.drawable.img_guide_point)
                iv_guide_point_3?.setImageResource(R.drawable.img_guide_point)
            }
            1 -> {
                iv_guide_point_1?.setImageResource(R.drawable.img_guide_point)
                iv_guide_point_2?.setImageResource(R.drawable.img_guide_point_p)
                iv_guide_point_3?.setImageResource(R.drawable.img_guide_point)
            }
            2 -> {
                iv_guide_point_1?.setImageResource(R.drawable.img_guide_point)
                iv_guide_point_2?.setImageResource(R.drawable.img_guide_point)
                iv_guide_point_3?.setImageResource(R.drawable.img_guide_point_p)
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.iv_music_switch ->  {
                if (mGuideMusic?.MEDIA_STATUS == MediaPlayerManager.MEDIA_STATUS_PAUSE) {
                    mAnim?.start()
                    mGuideMusic?.continuePlay()
                    iv_music_switch.setImageResource(R.drawable.img_guide_music)
                } else if (mGuideMusic?.MEDIA_STATUS == MediaPlayerManager.MEDIA_STATUS_PLAY) {

                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                        mAnim?.pause()
                    }
                    mGuideMusic?.pausePlay()
                    iv_music_switch.setImageResource(R.drawable.img_guide_music_off)
                }
            }
            R.id.tv_guide_skip -> {
                startWithPop(LoginFragment.newInstance())
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mGuideMusic?.stopPlay()
    }

}