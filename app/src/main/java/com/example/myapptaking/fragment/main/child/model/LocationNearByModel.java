package com.example.myapptaking.fragment.main.child.model;

import com.amap.api.services.core.PoiItem;

import java.io.Serializable;

public class LocationNearByModel implements Serializable {

    private PoiItem PoiItem;

    public LocationNearByModel( PoiItem poiItem) {
        PoiItem = poiItem;
    }

    public PoiItem getPoiItem() {
        return PoiItem;
    }

}
