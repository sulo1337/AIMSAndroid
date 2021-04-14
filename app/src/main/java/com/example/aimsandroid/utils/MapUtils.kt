package com.example.aimsandroid.utils

import com.example.aimsandroid.R
import com.example.aimsandroid.fragments.home.HomeFragment
import com.example.aimsandroid.fragments.home.MapEventListeners
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.here.android.mpa.common.GeoPosition
import com.here.android.mpa.common.PositioningManager
import com.here.android.mpa.mapping.Map
import com.here.android.mpa.mapping.MapState
import com.here.android.mpa.routing.Maneuver

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

fun HomeFragment.changeNextManeuverIcon(icon: Maneuver.Icon?) {
    val iconPlaceHolder: ExtendedFloatingActionButton? = view?.findViewById<ExtendedFloatingActionButton>(R.id.nextManeuverIcon)
    iconPlaceHolder?.let {

    }
}