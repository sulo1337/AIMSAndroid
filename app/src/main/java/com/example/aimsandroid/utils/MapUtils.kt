package com.example.aimsandroid.utils

import android.icu.text.SimpleDateFormat
import android.util.Log
import android.view.View
import com.example.aimsandroid.R
import com.example.aimsandroid.fragments.home.HomeFragment
import com.example.aimsandroid.fragments.home.MapEventListeners
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.here.android.mpa.common.GeoPosition
import com.here.android.mpa.common.Image
import com.here.android.mpa.common.PositioningManager
import com.here.android.mpa.mapping.Map
import com.here.android.mpa.mapping.MapState
import com.here.android.mpa.routing.Maneuver
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*

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

fun HomeFragment.updateNextManeuverIcon(icon: Maneuver.Icon?) {

}

fun HomeFragment.updateCurrentSpeed(speed: Double){
    Log.i("aimsDebug_nav_speed",Math.round(speed).toString().trim() )
    if(speed < 300) {
        binding.nextManeuverLayout.currentSpeed.text = java.lang.String.valueOf(Math.round(speed*2.23694)).trim()
    } else {
        binding.nextManeuverLayout.currentSpeed.text = "..."
    }
}

fun HomeFragment.updateSpeedLimit(speedLimit: Float){
    val speedLimitUS = Math.round(speedLimit*2.23694)
    if(speedLimitUS >= 25) {
        binding.nextManeuverLayout.speedLimit.text = java.lang.String.valueOf(speedLimitUS).trim()
    } else {
        binding.nextManeuverLayout.speedLimit.text = "..."
    }
}

fun HomeFragment.updateDistanceToNextManeuver(distance: Long){
    Log.i("aimsDebug_nav-distance", distance.toString())
    var distanceImperial: Double = distance*3.28084
    var unit = " feet"
    if(distanceImperial > 1320) {
        distanceImperial /= 5280
        unit = " miles"
    }
    val rounded: BigDecimal
    if(unit.trim().equals("feet")){
        rounded = BigDecimal(distanceImperial).setScale(0, RoundingMode.HALF_EVEN)
    } else {
        rounded = BigDecimal(distanceImperial).setScale(2, RoundingMode.HALF_EVEN)
    }

    val displayString = rounded.toPlainString() + unit
    binding.nextManeuverLayout.distanceToNextManeuver.text = displayString
}

fun HomeFragment.updateNextStreet(streetNum: String, streetName: String){
    Log.i("aimsDebug_nav-street", streetNum.trim() + " " + streetName.trim())
    binding.nextManeuverLayout.nextManeuverStreet.text = streetNum.trim() + " " + streetName.trim()
}

fun HomeFragment.updateEta(eta: Date) {
    val dateFormat = SimpleDateFormat("hh:mm aa", Locale.US)
    val time = dateFormat.format(eta)
    binding.nextManeuverLayout.arrivalTime.text = time.toString()
}