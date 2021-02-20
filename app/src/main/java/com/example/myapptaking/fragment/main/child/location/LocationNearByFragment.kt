package com.example.myapptaking.fragment.main.child.location

import android.os.Bundle
import android.view.View
import com.amap.api.maps.model.LatLng
import com.amap.api.services.core.LatLonPoint
import com.amap.api.services.core.PoiItem
import com.amap.api.services.geocoder.GeocodeResult
import com.amap.api.services.geocoder.GeocodeSearch
import com.amap.api.services.geocoder.RegeocodeQuery
import com.amap.api.services.geocoder.RegeocodeResult
import com.amap.api.services.poisearch.PoiResult
import com.amap.api.services.poisearch.PoiSearch
import com.chad.library.adapter.base.BaseQuickAdapter
import com.example.framework.base.SimpleListFragment
import com.example.framework.model.CommonBean
import com.example.framework.recycler.JssBaseViewHolder
import com.example.framework.utils.LogUtils
import com.example.myapptaking.R
import com.example.myapptaking.fragment.main.child.model.LocationNearByModel
import ikidou.reflect.TypeBuilder
import java.lang.reflect.Type

class LocationNearByFragment : SimpleListFragment<LocationNearByModel>(),
    PoiSearch.OnPoiSearchListener {

    companion object {
        fun newInstance(): LocationNearByFragment {
            val args = Bundle()
            val fragment = LocationNearByFragment()
            fragment.arguments = args
            return fragment
        }
    }

    private var geocoderSearch: GeocodeSearch? = null

    private var latLng: LatLng? = null

    var keyWord = ""

    private var cityCode = ""

    var cityName = ""

    var mOnItemClick: OnItemClick? = null

    private var selectedPos = 0

    private var currentPoiItem: PoiItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mSuperBackPressedSupport = false
    }


    fun setLatLng(latLng: LatLng?) {
        this.latLng = latLng
        selectedPos = 0
        if (isCallOnViewCreated) {
            if (!mSwipeRefreshLayout.isRefreshing) {
                mSwipeRefreshLayout.isRefreshing = true
            }
            onRefresh()

        }
    }


    //先要执行逆地理编码的搜索
    private fun Geo(latlng: LatLng?) {
        if (geocoderSearch == null) {
            geocoderSearch = GeocodeSearch(_mActivity)
        }
        //和上面一样
        geocoderSearch?.setOnGeocodeSearchListener(mOnGeocodeSearchListener)

        // 第一个参数表示一个Latlng(经纬度)，第二参数表示范围多少米，第三个参数表示是火系坐标系还是GPS原生坐标系

        val longitude = latlng?.longitude ?: 0.0

        val latitude = latlng?.latitude ?: 0.0

        val query =
            RegeocodeQuery(LatLonPoint(latitude, longitude), 200f, GeocodeSearch.AMAP)

        geocoderSearch?.getFromLocationAsyn(query)
    }

    private val mOnGeocodeSearchListener = object : GeocodeSearch.OnGeocodeSearchListener {
        override fun onRegeocodeSearched(regeocodeResult: RegeocodeResult?, p1: Int) {

            val mAddress = regeocodeResult?.regeocodeAddress

            val mQuery = regeocodeResult?.regeocodeQuery

            val point = mQuery?.point

            val addressName = mAddress?.formatAddress

            currentPoiItem = PoiItem(mAddress?.cityCode, point, addressName, "")

            cityCode = mAddress?.cityCode.toString()

            cityName = mAddress?.city.toString()


        }

        override fun onGeocodeSearched(p0: GeocodeResult?, p1: Int) {

        }

    }


    override fun getItemLayout(): Int {
        return R.layout.item_rlv_search_ams
    }


    override fun convertItem(helper: JssBaseViewHolder?, item: LocationNearByModel?) {
        val poiItem = item?.poiItem
        helper?.setText(R.id.tv_title_irsa, poiItem?.title)
            ?.setText(R.id.tv_address_irsa, poiItem?.snippet)
            ?.setViewVisible(R.id.tv_address_irsa, poiItem?.snippet?.isNotEmpty() == true)
            ?.setViewVisible(R.id.tv_title_irsa, poiItem?.title?.isNotEmpty() == true)
            ?.setViewSelect(R.id.avi, selectedPos == helper.layoutPosition)
    }

    override fun getListType(): Type {
        return TypeBuilder.newInstance(CommonBean::class.java)
            .beginSubType(List::class.java)
            .addTypeParam(LocationNearByModel::class.java)
            .build()
    }

    override fun netRequest() {
        if (latLng != null) {

            searchNearby()
        } else if (mSwipeRefreshLayout.isRefreshing) {
            mSwipeRefreshLayout.isRefreshing = false
        }
    }

    override fun onRefresh() {

        selectedPos = 0

        super.onRefresh()

        Geo(latLng)
    }


    /**
     * 开始搜索附近
     */
    private fun searchNearby() {
        //这三个参数，第一个是搜索关键字，第二个是搜索的类型，具体类型参照高德api
        val query = PoiSearch.Query(keyWord, "", cityCode)
        //查询条数
        query.pageSize = 6
        query.pageNum = pageIndex
        val search = PoiSearch(_mActivity, query)
        search.bound = PoiSearch.SearchBound(
            LatLonPoint(latLng?.latitude ?: 0.0, latLng?.longitude ?: 0.0),
            10000, true
        )
        search.setOnPoiSearchListener(this)

        search.searchPOIAsyn()
    }

    override fun onPoiSearched(result: PoiResult?, code: Int) {
        if (code == 1000) {

            val ls = mutableListOf<LocationNearByModel>()

            result?.pois?.forEach {
                ls.add(LocationNearByModel(it))
            }

            if (pageIndex == mPrimaryPageIndex) {
                if (ls.isEmpty()) {
                    ls.add(LocationNearByModel(currentPoiItem))
                } else {
                    ls.add(0, LocationNearByModel(currentPoiItem))
                }
            }
            loadListDate(ls)
            if (pageIndex == mPrimaryPageIndex) {
                val item = mAdapter?.getItem(selectedPos)
                mOnItemClick?.onListItemClick(item?.poiItem)
            }


        } else {
            onFailed()

        }
    }

    override fun onPoiItemSearched(p0: PoiItem?, p1: Int) {

    }

    override fun onItemClick(adapter: BaseQuickAdapter<*, *>, view: View, position: Int) {
        super.onItemClick(adapter, view, position)

        selectedPos = position

        val item = mAdapter?.getItem(position)

        mAdapter?.notifyDataSetChanged()

        mRecyclerView.scrollToPosition(position)

        mOnItemClick?.onListItemClick(item?.poiItem)
    }

    fun getSelectedItem()=mAdapter?.getItem(selectedPos)

    interface OnItemClick {
        fun onListItemClick(p0: PoiItem?)
    }
}