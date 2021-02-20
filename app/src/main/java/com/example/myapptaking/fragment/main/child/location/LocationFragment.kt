package com.example.myapptaking.fragment.main.child.location

import android.app.Activity
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.BitmapDescriptorFactory
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.MarkerOptions
import com.amap.api.maps.model.MyLocationStyle
import com.amap.api.services.core.PoiItem
import com.amap.api.services.poisearch.PoiResult
import com.amap.api.services.poisearch.PoiSearch
import com.example.framework.base.BaseLazyDataBindingFragment
import com.example.framework.base.BaseUIFragment
import com.example.framework.global.AppGlobals
import com.example.framework.manager.DialogManager
import com.example.framework.manager.MapManager
import com.example.framework.utils.LogUtils
import com.example.framework.view.DialogView
import com.example.framework.view.LodingView
import com.example.myapptaking.R
import com.example.myapptaking.databinding.FragmentLocationBinding
import com.example.myapptaking.fragment.main.child.view.DialogSelectConstellationView
import com.permissionx.guolindev.PermissionX

class LocationFragment : BaseLazyDataBindingFragment<FragmentLocationBinding>(),
    DialogSelectConstellationView.DialogSelectConstellationViewCallBack, View.OnClickListener,
    PoiSearch.OnPoiSearchListener, Toolbar.OnMenuItemClickListener {


    companion object {

        fun startRes(
            fragment: BaseUIFragment,
            isShow: Boolean,
            la: Double,
            lo: Double,
            address: String,
            requestCode: Int
        ) {
            PermissionX.init(fragment)
                .permissions(listOf(android.Manifest.permission.ACCESS_FINE_LOCATION))
                .onExplainRequestReason { scope, deniedList ->
                    val msg = "获取定位权限"
                    val positive = "确定"
                    val negative = "取消"
                    scope.showRequestReasonDialog(deniedList, msg, positive, negative)
                }.onForwardToSettings { scope, deniedList ->
                    val msg = "获取定位权限"
                    val positive = "确定"
                    val negative = "取消"
                    scope.showForwardToSettingsDialog(deniedList, msg, positive, negative)
                }.request { allGranted, _, deniedList ->
                    if (!allGranted || deniedList.isNotEmpty()) {
                        Toast.makeText(AppGlobals.getApplication(), "获取定位权限失败", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        val location = newInstance(isShow, la, lo, address)
                        fragment.startForResult(location, requestCode)
                    }

                }
        }

        fun startRes(
            fragment: BaseUIFragment,
            isShow: Boolean,
            la: Double,
            lo: Double,
            address: String
        ) {
            PermissionX.init(fragment)
                .permissions(listOf(android.Manifest.permission.ACCESS_FINE_LOCATION))
                .onExplainRequestReason { scope, deniedList ->
                    val msg = "获取定位权限"
                    val positive = "确定"
                    val negative = "取消"
                    scope.showRequestReasonDialog(deniedList, msg, positive, negative)
                }.onForwardToSettings { scope, deniedList ->
                    val msg = "获取定位权限"
                    val positive = "确定"
                    val negative = "取消"
                    scope.showForwardToSettingsDialog(deniedList, msg, positive, negative)
                }.request { allGranted, _, deniedList ->
                    if (!allGranted || deniedList.isNotEmpty()) {
                        Toast.makeText(AppGlobals.getApplication(), "获取定位权限失败", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        val location = newInstance(isShow, la, lo, address)
                        fragment.start(location)

                    }

                }
        }

        fun newInstance(
            isShow: Boolean,
            la: Double,
            lo: Double,
            address: String,
        ): LocationFragment {
            val args = Bundle()
            args.putBoolean("isShow", isShow)
            args.putDouble("la", la)
            args.putDouble("lo", lo)
            args.putString("address", address)

            val fragment = LocationFragment()
            fragment.arguments = args
            return fragment
        }
    }

    private var mToolbar: Toolbar? = null

    private var mLodingView: LodingView? = null

    private var mPoiView: DialogView? = null

    private var mConstellationView: DialogSelectConstellationView? = null

    private var aMap: AMap? = null

    private var query: PoiSearch.Query? = null

    private var poiSearch: PoiSearch? = null

    private var ILa = 0.0

    private var ILo = 0.0

    private var IAddress: String? = null

    private var isShow = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ILa = arguments?.getDouble("la") ?: 0.0
        ILo = arguments?.getDouble("lo") ?: 0.0
        IAddress = arguments?.getString("IAddress")
        isShow = arguments?.getBoolean("isShow") ?: false
    }


    override fun lazyInit() {


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBinder.mMapView.onCreate(savedInstanceState)


    }

    /**
     * 更新地点
     *
     * @param la
     * @param lo
     * @param address
     */
    private fun updatePoi(la: Double, lo: Double, address: String?) {
        aMap?.isMyLocationEnabled = true
//        if ((mToolbar?.menu?.size() ?: 0) <= 0) {
//            mToolbar?.inflateMenu(R.menu.location_menu)
//        }
//        supportInvalidateOptionsMenu()
        val inflate = View.inflate(context,R.layout.round_location_layout, null)
        //显示位置
        val latLng = LatLng(la, lo)
        aMap?.clear()
        aMap?.addMarker(
            MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.fromView(inflate))
                .title(_mActivity.getString(R.string.text_location))
                .snippet(address)
        )

        aMap?.moveCamera(
            CameraUpdateFactory.changeLatLng(latLng
            )
        )
    }

    override fun initView(view: View) {

        mToolbar = view.findViewById(R.id.mToolbar)

        mToolbar?.setNavigationOnClickListener { onBackPressedSupport() }

        initPoiView()

        viewBinder.onViewClick = this

        if (aMap == null) {
            aMap = viewBinder.mMapView.map
        }


        val myLocationStyle = MyLocationStyle()
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_SHOW)
        myLocationStyle.strokeColor(Color.TRANSPARENT)
        myLocationStyle.showMyLocation(false)
        myLocationStyle.radiusFillColor(Color.TRANSPARENT)
        val inflate = View.inflate(context,R.layout.round_location_layout, null)
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromView(inflate))
        myLocationStyle.interval(2000)
        aMap?.myLocationStyle = myLocationStyle
        aMap?.uiSettings?.isMyLocationButtonEnabled = true

        aMap?.isMyLocationEnabled = true

        //缩放
        aMap?.moveCamera(CameraUpdateFactory.zoomTo(17f))

        if (!isShow) {
            //如果不显示 则作为展示类地图 接收外界传递的地址显示
            updatePoi(ILa, ILo, IAddress)
        } else {
            mToolbar?.inflateMenu(R.menu.location_menu)
            mToolbar?.setOnMenuItemClickListener(this)
        }

        aMap?.setOnMyLocationChangeListener { location: Location ->
            LogUtils.i("location:$location")
            LogUtils.i("location.getExtras:" + location.extras.toString())
            try {
                if (location.extras.getInt("errorCode") == 7) {
                    Toast.makeText(_mActivity, "Key错误", Toast.LENGTH_SHORT).show()
                }
//                updatePoi(location.altitude,location.longitude,location.extras.getString("address"))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun initPoiView() {

        mLodingView = LodingView(_mActivity)

        mLodingView?.setLodingText(getString(R.string.text_location_search))

        mConstellationView = DialogSelectConstellationView(_mActivity)

        mConstellationView?.mViewCallBack = this

        mPoiView = DialogManager.getInstance().initView(
            _mActivity, mConstellationView, Gravity.BOTTOM
        )
        mPoiView?.setCancelable(false)


    }

    override fun layoutId(): Int {
        return R.layout.fragment_location
    }

    override fun onCancel() {
        DialogManager.getInstance().hide(mPoiView)
    }

    override fun onItemSelected(item: PoiItem?) {

        DialogManager.getInstance().hide(mPoiView)

        /**
         * 已知条件：地址
         * 地址 转换 经纬度
         */
        MapManager.getInstance()
            .address2poi(
                item.toString()
            ) { la, lo, address ->
                ILa = la
                ILo = lo
                IAddress = address
                updatePoi(la, lo, address)
            }
    }

    override fun onResume() {
        super.onResume()
        viewBinder.mMapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        viewBinder.mMapView.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        viewBinder.mMapView.onSaveInstanceState(outState)
    }


    override fun onLowMemory() {
        super.onLowMemory()
        viewBinder.mMapView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewBinder.mMapView.onDestroy()
        mConstellationView?.mViewCallBack = null
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.iv_poi -> {
                val keyWord: String = viewBinder.etSearch.text.toString().trim { it <= ' ' }
                if (TextUtils.isEmpty(keyWord)) {
                    return
                }
                poiSearch(keyWord)
            }
        }
    }

    /**
     * 关键字POI搜索
     *
     * @param keyWord
     */
    private fun poiSearch(keyWord: String) {
        mLodingView?.show()
        query = PoiSearch.Query(keyWord, "", "")
        query?.pageSize = 6
        query?.pageNum = 1
        poiSearch = PoiSearch(_mActivity, query)
        poiSearch?.setOnPoiSearchListener(this)
        poiSearch?.searchPOIAsyn()
    }

    override fun onPoiSearched(poiResult: PoiResult?, i: Int) {

        //得到搜索的结果
        mLodingView?.hide()
        mConstellationView?.addData(poiResult?.pois)
        DialogManager.getInstance().show(mPoiView)
    }

    override fun onPoiItemSearched(p0: PoiItem?, p1: Int) {

    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {

        when (item?.itemId) {
            R.id.menu_send -> {
                val bundle = Bundle()
                if (mConstellationView?.selectedPosition ?: 0 > 0) {
                    //直接点击
                    bundle.putDouble("la", ILa)
                    bundle.putDouble("lo", ILo)
                    bundle.putString("address", IAddress)
                } else {

                    //直接点击
                    bundle.putDouble("la", aMap?.myLocation?.latitude ?: 0.0)
                    bundle.putDouble("lo", aMap?.myLocation?.longitude ?: 0.0)
                    bundle.putString("address", aMap?.myLocation?.extras?.getString("desc"))
                }

                setFragmentResult(Activity.RESULT_OK, bundle)
                onBackPressedSupport()
            }
        }

        return true
    }
}