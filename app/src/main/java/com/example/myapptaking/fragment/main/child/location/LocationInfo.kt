package com.example.myapptaking.fragment.main.child.location

import android.content.Context
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.services.core.AMapException
import com.amap.api.services.geocoder.GeocodeQuery
import com.amap.api.services.geocoder.GeocodeResult
import com.amap.api.services.geocoder.GeocodeSearch
import com.amap.api.services.geocoder.RegeocodeResult
import com.example.framework.utils.LogUtils
import java.text.SimpleDateFormat
import java.util.*

object LocationInfo {

    fun getLocationInfo(context: Context, success: (AMapLocation) -> Unit) {
        val mLocationClient = AMapLocationClient(context)
        val mLocationOption = AMapLocationClientOption()
        // 初始化定位
        // 设置高德地图定位回调监听
        mLocationClient.setLocationListener { aMapLocation ->
            if (aMapLocation != null) {
                if (aMapLocation.errorCode == 0) {
                    val locationType = aMapLocation.locationType // 获取当前定位结果来源，如网络定位结果，详见定位类型表
                    val latitude = aMapLocation.latitude // 获取纬度
                    val longitude = aMapLocation.longitude // 获取经度
                    val accuracy = aMapLocation.accuracy // 获取精度信息
                    val address = aMapLocation.address // 地址，如果option中设置isNeedAddress为false，则没有此结果，
                    // 网络定位结果中会有地址信息，GPS定位不返回地址信息。
                    val country = aMapLocation.country // 国家信息
                    val province = aMapLocation.province // 省信息
                    val city = aMapLocation.city // 城市信息
                    val district = aMapLocation.district // 城区信息
                    val street = aMapLocation.street // 街道信息
                    val streetNum = aMapLocation.streetNum // 街道门牌号信息
                    val cityCode = aMapLocation.cityCode // 城市编码
                    val adCode = aMapLocation.adCode // 地区编码
                    val aoiName = aMapLocation.aoiName // 获取当前定位点的AOI信息
                    val buildingId = aMapLocation.buildingId // 获取当前室内定位的建筑物Id
                    val floor = aMapLocation.floor // 获取当前室内定位的楼层
                    val gpsAccuracyStatus = aMapLocation.gpsAccuracyStatus //获取GPS的当前状态
                    // 获取定位时间
                    val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                    val date = Date(aMapLocation.time)
                    df.format(date)
                    success(aMapLocation)
                } else {
                    // 定位失败时，可通过ErrCode（错误码）信息来确定失败的原因，errInfo是错误信息，详见错误码表。
                    LogUtils.i("获取定位失败")
                }

            }
        }
        // 初始化AMapLocationClientOption对象
        // 高精度定位模式：会同时使用网络定位和GPS定位，优先返回最高精度的定位结果，以及对应的地址描述信息
        // 设置定位模式为AMapLocationMode.Hight_Accuracy，高精度模式
        mLocationOption.locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
        // 低功耗定位模式：不会使用GPS和其他传感器，只会使用网络定位（Wi-Fi和基站定位）；
        //设置定位模式为AMapLocationMode.Battery_Saving，低功耗模式。
        //mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Battery_Saving);
        // 仅用设备定位模式：不需要连接网络，只使用GPS进行定位，这种模式下不支持室内环境的定位，自 v2.9.0 版本支持返回地址描述信息。
        // 设置定位模式为AMapLocationMode.Device_Sensors，仅设备模式。
        //mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Device_Sensors);
        // SDK默认采用连续定位模式，时间间隔2000ms
        // 设置定位间隔，单位毫秒，默认为2000ms，最低1000ms。
        mLocationOption.interval = 60000
        // 设置定位同时是否需要返回地址描述
        //设置是否返回地址信息（默认返回地址信息）
        mLocationOption.isNeedAddress = true
        // 设置是否强制刷新WIFI，默认为强制刷新。每次定位主动刷新WIFI模块会提升WIFI定位精度，但相应的会多付出一些电量消耗。
        // 设置是否强制刷新WIFI，默认为true，强制刷新。
//        mLocationOption.isWifiActiveScan = true
        // 设置是否允许模拟软件Mock位置结果，多为模拟GPS定位结果，默认为false，不允许模拟位置。
        // 设置是否允许模拟位置,默认为false，不允许模拟位置
        mLocationOption.isMockEnable = false
        // 设置定位请求超时时间，默认为30秒
        // 单位是毫秒，默认30000毫秒，建议超时时间不要低于8000毫秒。
        mLocationOption.httpTimeOut = 50000
        // 设置是否开启定位缓存机制
        // 缓存机制默认开启，可以通过以下接口进行关闭。
        // 当开启定位缓存功能，在高精度模式和低功耗模式下进行的网络定位结果均会生成本地缓存，不区分单次定位还是连续定位。GPS定位结果不会被缓存。
        // 关闭缓存机制
        mLocationOption.isLocationCacheEnable = false
        // 设置是否只定位一次，默认为false
        mLocationOption.isOnceLocation = true
        // 给定位客户端对象设置定位参数
        mLocationClient.setLocationOption(mLocationOption)
        // 启动高德地图定位
        mLocationClient.startLocation()
    }


    fun getLatLngByAddress(
        context: Context,
        city: String?,
        address: String,
        success: (GeocodeResult) -> Unit
    ) {
        //发起正地理编码搜索
        //构造 GeocodeSearch 对象，并设置监听。
        val geocodeSearch = GeocodeSearch(context)
        geocodeSearch.setOnGeocodeSearchListener(object : GeocodeSearch.OnGeocodeSearchListener {
            override fun onRegeocodeSearched(p0: RegeocodeResult?, position: Int) {
            }

            override fun onGeocodeSearched(geocodeResult: GeocodeResult?, position: Int) {
                if (position == AMapException.CODE_AMAP_SUCCESS) {
                    if (geocodeResult?.geocodeAddressList != null
                        && geocodeResult.geocodeAddressList.size > 0
                    ) {
                        success(geocodeResult)
                    }
                }
            }

        })
        //通过GeocodeQuery设置查询参数,调用getFromLocationNameAsyn(GeocodeQuery geocodeQuery) 方法发起请求。
        //address表示地址，第二个参数表示查询城市，中文或者中文全拼，citycode、adcode都ok
        val query = GeocodeQuery(address, city)
        geocodeSearch.getFromLocationNameAsyn(query)
    }

}