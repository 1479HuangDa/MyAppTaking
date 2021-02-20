package com.example.myapptaking.fragment.main.child.location

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.LocationSource
import com.amap.api.maps.model.BitmapDescriptorFactory
import com.amap.api.maps.model.Marker
import com.amap.api.maps.model.MyLocationStyle
import com.example.myapptaking.R

object InitAmap {
    fun initAmap(aMap: AMap?, context: Context,locationSource: (LocationSource.OnLocationChangedListener?) -> Unit,
                 marker: (Marker) -> Boolean) {

        val myLocationStyle = MyLocationStyle()
        val inflate = View.inflate(context,R.layout.trip_origin_layout, null)
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromView(inflate))
        myLocationStyle.strokeColor(Color.TRANSPARENT)
        myLocationStyle.radiusFillColor(Color.TRANSPARENT)
        myLocationStyle.anchor(0.5f,0.576f);

//        myLocationStyle.showMyLocation(false)
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_SHOW)
        myLocationStyle.interval(2000)
        aMap?.myLocationStyle = myLocationStyle
        aMap?.uiSettings?.isMyLocationButtonEnabled = true

        aMap?.isMyLocationEnabled = true

        //缩放
        aMap?.moveCamera(CameraUpdateFactory.zoomTo(17f))

        aMap?.uiSettings?.isZoomControlsEnabled = false
        // 设置地图默认的指南针是否显示
        aMap?.uiSettings?.isCompassEnabled = false
        // 设置定位监听
        aMap?.setLocationSource(object : LocationSource {
            override fun deactivate() {
            }

            override fun activate(p0: LocationSource.OnLocationChangedListener?) {
                locationSource(p0)
            }
        })
        // 设置默认定位按钮是否显示
        aMap?.uiSettings?.isMyLocationButtonEnabled = false
        // 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        aMap?.isMyLocationEnabled = true
//        aMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE)
        aMap?.setOnMarkerClickListener {
            marker(it)
        }
    }
}