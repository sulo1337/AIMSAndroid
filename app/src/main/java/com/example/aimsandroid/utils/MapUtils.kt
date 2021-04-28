package com.example.aimsandroid.utils

import android.icu.text.SimpleDateFormat
import android.util.Log
import android.view.View
import androidx.core.content.res.ResourcesCompat
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

fun HomeFragment.updateNextManeuverIcon(icon: Maneuver.Icon?) {
    binding.nextManeuverLayout.nextManeuverIcon.setImageDrawable(ResourcesCompat.getDrawable(resources, getManeuverDrawableId(icon), null))
}

fun HomeFragment.updateDistanceRemaining(distance: Int) {
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
    binding.nextManeuverLayout.remainingDistance.text = displayString
}

fun getManeuverDrawableId(icon: Maneuver.Icon?): Int {
    when(icon){
        Maneuver.Icon.CHANGE_LINE -> return R.drawable.direction_continue
        Maneuver.Icon.END -> return R.drawable.direction_arrive
        Maneuver.Icon.ENTER_HIGHWAY_LEFT_LANE -> return R.drawable.direction_merge_slight_right
        Maneuver.Icon.ENTER_HIGHWAY_RIGHT_LANE -> return R.drawable.direction_merge_slight_left
        Maneuver.Icon.FERRY -> return R.drawable.direction_flag
        Maneuver.Icon.GO_STRAIGHT -> return R.drawable.direction_continue_straight
        Maneuver.Icon.HEAD_TO -> return R.drawable.direction_continue_straight
        Maneuver.Icon.HEAVY_LEFT -> return R.drawable.direction_turn_sharp_left
        Maneuver.Icon.HEAVY_RIGHT -> return R.drawable.direction_turn_sharp_right
        Maneuver.Icon.HIGHWAY_KEEP_LEFT -> return R.drawable.direction_fork_slight_left
        Maneuver.Icon.HIGHWAY_KEEP_RIGHT -> return R.drawable.direction_fork_slight_right
        Maneuver.Icon.KEEP_LEFT -> return  R.drawable.direction_fork_slight_left
        Maneuver.Icon.KEEP_RIGHT -> return R.drawable.direction_fork_slight_right
        Maneuver.Icon.LEAVE_HIGHWAY_LEFT_LANE -> return R.drawable.direction_fork_slight_left
        Maneuver.Icon.LEAVE_HIGHWAY_RIGHT_LANE -> return R.drawable.direction_fork_slight_right
        Maneuver.Icon.LIGHT_LEFT -> return R.drawable.direction_turn_slight_left
        Maneuver.Icon.LIGHT_RIGHT -> return R.drawable.direction_turn_slight_right
        Maneuver.Icon.PASS_STATION -> return R.drawable.direction_continue
        Maneuver.Icon.QUITE_LEFT -> return R.drawable.direction_turn_left
        Maneuver.Icon.QUITE_RIGHT -> return R.drawable.direction_turn_right
        Maneuver.Icon.START -> return R.drawable.direction_depart
        Maneuver.Icon.UNDEFINED -> return R.drawable.direction_flag
        Maneuver.Icon.UTURN_LEFT -> return R.drawable.direction_uturn
        Maneuver.Icon.UTURN_RIGHT -> return R.drawable.direction_uturn
        else -> return R.drawable.direction_roundabout
    }
}