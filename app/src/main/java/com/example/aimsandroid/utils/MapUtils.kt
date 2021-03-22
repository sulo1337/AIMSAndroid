package com.example.aimsandroid.utils

import com.here.android.mpa.common.GeoPosition
import com.here.android.mpa.common.PositioningManager
import com.here.android.mpa.mapping.Map
import com.here.android.mpa.mapping.MapState

class MapPositionChangedListener: PositioningManager.OnPositionChangedListener{
    override fun onPositionUpdated(p0: PositioningManager.LocationMethod?, p1: GeoPosition?, p2: Boolean) {
    }

    override fun onPositionFixChanged(p0: PositioningManager.LocationMethod?, p1: PositioningManager.LocationStatus?) {
    }
}

class MapTransformListener: Map.OnTransformListener{
    override fun onMapTransformStart() {
    }

    override fun onMapTransformEnd(p0: MapState?) {
    }

}