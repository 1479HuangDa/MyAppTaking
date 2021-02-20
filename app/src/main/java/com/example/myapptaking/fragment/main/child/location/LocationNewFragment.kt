package com.example.myapptaking.fragment.main.child.location

import android.app.Activity
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.amap.api.location.AMapLocation
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.LocationSource
import com.amap.api.maps.model.*
import com.amap.api.services.core.LatLonPoint
import com.amap.api.services.core.PoiItem
import com.amap.api.services.geocoder.GeocodeResult
import com.amap.api.services.geocoder.GeocodeSearch
import com.amap.api.services.geocoder.RegeocodeQuery
import com.amap.api.services.geocoder.RegeocodeResult
import com.amap.api.services.poisearch.PoiResult
import com.amap.api.services.poisearch.PoiSearch
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemClickListener
import com.example.framework.base.BaseLazyDataBindingFragment
import com.example.framework.base.BaseUIFragment
import com.example.framework.global.AppGlobals
import com.example.framework.manager.KeyWordManager
import com.example.framework.recycler.JssBaseQuickAdapter
import com.example.framework.recycler.JssBaseViewHolder
import com.example.framework.utils.LogUtils
import com.example.myapptaking.R
import com.example.myapptaking.databinding.FragmentLocationNewBinding
import com.example.myapptaking.fragment.main.child.location.InitAmap.initAmap
import com.example.myapptaking.fragment.main.child.location.LocationInfo.getLatLngByAddress
import com.permissionx.guolindev.PermissionX
import kotlin.math.abs


class LocationNewFragment : BaseLazyDataBindingFragment<FragmentLocationNewBinding>(),
    View.OnClickListener,
    LocationNearByFragment.OnItemClick {

    companion object {

        fun startRes(fragment: BaseUIFragment, requestCode: Int) {
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
                        val location = newInstance()
                        fragment.startForResult(location, requestCode)
                    }

                }

        }

        fun newInstance(): LocationNewFragment {
            val args = Bundle()
            val fragment = LocationNewFragment()
            fragment.arguments = args
            return fragment
        }
    }


    private var aMap: AMap? = null

    private var locationMaker: Marker? = null

    private var mListener: LocationSource.OnLocationChangedListener? = null


    private var myLocationLatLng: LatLng? = null

    private var isTouchedMap = false

    private var mNearByFragment = LocationNearByFragment.newInstance()

    override fun lazyInit() {
        initAmap(aMap, _mActivity, { locationSourceListener(it) }, { markerListener(it) })

        //定位初始化
        LocationInfo.getLocationInfo(_mActivity) {

            locationSuccess(it)
        }


        aMap?.setAMapGestureListener(mMapGestureListener)

        //设置地图拖动监听
        aMap?.setOnCameraChangeListener(mOnCameraChangeListener)

        //搜索框监听
        viewBinder.etSearchAms.setOnKeyListener { _, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_ENTER) {

                mNearByFragment.keyWord = viewBinder.etSearchAms.text.trim().toString()
                getLatLngByAddress(
                    _mActivity,
                    mNearByFragment.cityName,
                    mNearByFragment.keyWord
                ) { getLatlngSuccess(it) }
                // 强制隐藏软键盘
                KeyWordManager.getInstance().hideKeyWord(_mActivity)


            }
            false
        }
    }


    private val mOnCameraChangeListener = object : AMap.OnCameraChangeListener {
        override fun onCameraChange(p0: CameraPosition?) {

        }

        override fun onCameraChangeFinish(cameraPosition: CameraPosition?) {
            val latLng = cameraPosition?.target

            val latitude = latLng?.latitude ?: 0.0
            val longitude = latLng?.longitude ?: 0.0

            isMyLocation(latitude, longitude)
            if (isTouchedMap) {
                mNearByFragment.keyWord = ""
                mNearByFragment.setLatLng(latLng)

            }

        }

    }


    private val mMapGestureListener = object : AMapGestureListener {
        override fun onDoubleTap(p0: Float, p1: Float) {
            isTouchedMap = true
        }

        override fun onSingleTap(p0: Float, p1: Float) {
            isTouchedMap = true
        }

        override fun onFling(velocityX: Float, velocityY: Float) {
            isTouchedMap = true
        }

        override fun onScroll(distanceX: Float, distanceY: Float) {
            isTouchedMap = true
        }

        override fun onLongPress(p0: Float, p1: Float) {
            isTouchedMap = true
        }

        override fun onDown(p0: Float, p1: Float) {
            isTouchedMap = true
        }

        override fun onUp(p0: Float, p1: Float) {
            isTouchedMap = false
        }

        //地图稳定下来会回到此接口
        override fun onMapStable() {

        }
    }

    private fun isMyLocation(latitude: Double, longitude: Double) {
        if (myLocationLatLng == null) {
            viewBinder.backToMyLocation.isSelected = true
            myLocationLatLng = LatLng(latitude, longitude)

        } else {
            val myLocationLatitude = myLocationLatLng?.latitude ?: 0.0
            val myLocationLongitude = myLocationLatLng?.longitude ?: 0.0
            viewBinder.backToMyLocation.isSelected =
                abs(latitude - myLocationLatitude) == 0.0
                        && abs(longitude - myLocationLongitude) == 0.0
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBinder.mapAms.onCreate(savedInstanceState)


    }

    override fun initView(view: View) {

        mNearByFragment.mOnItemClick = this

        loadRootFragment(R.id.rlv_search_ams, mNearByFragment)

        viewBinder.onViewClick = this

        aMap = viewBinder.mapAms.map

    }


    override fun layoutId(): Int {
        return R.layout.fragment_location_new
    }


    private fun locationSourceListener(locationSource: LocationSource.OnLocationChangedListener?) {
        mListener = locationSource
    }

    //marker的点击事件监听
    private fun markerListener(marker: Marker): Boolean {

        return true
    }

    private fun locationSuccess(location: AMapLocation) {

        //显示系统小蓝点
        mListener?.let { mListener?.onLocationChanged(location) }
        // 设置缩放级别
        aMap?.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(
                    location.latitude,
                    location.longitude
                ), 17f
            )
        )
        // 将地图移动到定位点
        aMap?.moveCamera(
            CameraUpdateFactory.changeLatLng(
                LatLng(location.latitude, location.longitude)
            )
        )
        val markerOption = MarkerOptions()
        val inflate = View.inflate(_mActivity, R.layout.round_location_layout, null)
        markerOption
            .icon(BitmapDescriptorFactory.fromView(inflate))
            .position(LatLng(location.latitude, location.longitude))
            .draggable(true)
            .title(location.address)
            .snippet(location.address)

        locationMaker = aMap?.addMarker(markerOption)

        locationMaker?.setPositionByPixels(
            viewBinder.mapAms.width / 2,
            viewBinder.mapAms.height / 2
        )
        mNearByFragment.setLatLng(LatLng(location.latitude, location.longitude))
    }


    private fun getLatlngSuccess(geocodeResult: GeocodeResult) {
        val address = geocodeResult.geocodeAddressList[0]
        LogUtils.i("经纬度值: ${address.latLonPoint} \n位置描述: ${address.formatAddress}")

        val latLng = LatLng(address.latLonPoint.latitude, address.latLonPoint.longitude)
        aMap?.animateCamera(CameraUpdateFactory.changeLatLng(latLng))
        mNearByFragment.setLatLng(latLng)
        viewBinder.etSearchAms.setText("")
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        viewBinder.mapAms.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        viewBinder.mapAms.onLowMemory()
    }

    override fun onResume() {
        super.onResume()
        // 重新绘制加载地图
        viewBinder.mapAms.onResume()
    }

    override fun onPause() {
        super.onPause()
        // 暂停地图的绘制
        viewBinder.mapAms.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        // 销毁地图
        viewBinder.mapAms.onDestroy()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.back_to_my_location -> {
                aMap?.moveCamera(CameraUpdateFactory.zoomTo(17f))
                myLocationLatLng = null

                isTouchedMap = true
                LocationInfo.getLocationInfo(_mActivity) {

                    locationSuccess(it)
                }
            }
            R.id.iv_sending -> {

                val bundle = Bundle()

                val selectedItem = mNearByFragment.getSelectedItem()

                val poiItem = selectedItem?.poiItem

                val latitude = poiItem?.latLonPoint?.latitude ?: 0.0
                val longitude = poiItem?.latLonPoint?.longitude ?: 0.0

                val iAddress = if (poiItem?.snippet?.isNotEmpty()==true)
                    "${poiItem.snippet}${poiItem.title}"
                else
                    "${poiItem?.title}"

                bundle.putDouble("la", latitude)
                bundle.putDouble("lo", longitude)
                bundle.putString("address", iAddress)
                setFragmentResult(Activity.RESULT_OK, bundle)
                onBackPressedSupport()
            }
            R.id.back_page -> {
                onBackPressedSupport()
            }
        }
    }

    override fun onListItemClick(item: PoiItem?) {
        isTouchedMap = false

        val address =
            item?.provinceName + item?.cityName + item?.adName + item?.snippet

        LogUtils.i("点击的地址：$address")

        val latitude = item?.latLonPoint?.latitude ?: 0.0
        val longitude = item?.latLonPoint?.longitude ?: 0.0
        val latLng = LatLng(latitude, longitude)

        aMap?.animateCamera(CameraUpdateFactory.changeLatLng(latLng))

    }

    override fun onBackPressedSupport(): Boolean {
        pop()
        return true
    }


}