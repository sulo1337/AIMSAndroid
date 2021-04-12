package com.example.aimsandroid.fragments.home

import android.app.Activity
import android.content.Intent
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import com.example.aimsandroid.MainActivity
import com.example.aimsandroid.service.ForegroundService
import com.example.aimsandroid.utils.TextToSpeechUtil
import com.here.android.mpa.common.GeoPosition
import com.here.android.mpa.guidance.NavigationManager
import com.here.android.mpa.mapping.AndroidXMapFragment
import com.here.android.mpa.mapping.Map
import com.here.android.mpa.routing.Route

class MapEventListeners(private val homeFragment: HomeFragment) {

    private var m_navigationManager: NavigationManager = NavigationManager.getInstance()

    val m_maneuverListener: NavigationManager.ManeuverEventListener = object: NavigationManager.ManeuverEventListener(){
        override fun onManeuverEvent() {
            Toast.makeText(homeFragment.requireActivity(), m_navigationManager.nextManeuver?.angle.toString(), Toast.LENGTH_SHORT).show()
            val nextManeuver = m_navigationManager.nextManeuver
            val nextManeuverDistance = m_navigationManager.nextManeuverDistance
            val angle = nextManeuver?.angle
            Log.i("navigation", nextManeuver?.action.toString())
            val nextStreet = nextManeuver?.nextRoadName
            val speech = StringBuilder("In ")
            if(nextManeuverDistance < 1200) {
                speech.append(nextManeuverDistance.toString())
                speech.append(" feet")
            } else if(nextManeuverDistance < 2400){
                speech.append("quarter of a mile")
            } else if(nextManeuverDistance < 3000){
                speech.append("half a mile")
            } else if(nextManeuverDistance < 5280){
                speech.append("three quarters of a mile")
            } else {
                val mile = Math.floor(nextManeuverDistance/5280.0)
                speech.append(mile)
                speech.append(" miles")
            }
            speech.append(", turn")
            speech.append(" into")
            speech.append(nextStreet)
            homeFragment.speakText(speech.toString(), TextToSpeech.QUEUE_FLUSH)
        }
    }

    val m_positionListener: NavigationManager.PositionListener = object : NavigationManager.PositionListener() {
        override fun onPositionUpdated(geoPosition: GeoPosition) {
            /* Current position information can be retrieved in this callback */
        }
    }
    val m_navigationManagerEventListener: NavigationManager.NavigationManagerEventListener =
        object : NavigationManager.NavigationManagerEventListener() {
            override fun onRunningStateChanged() {
//                Toast.makeText(m_activity, "Running state changed", Toast.LENGTH_SHORT).show()
            }

            override fun onNavigationModeChanged() {
//                Toast.makeText(m_activity, "Navigation mode changed", Toast.LENGTH_SHORT).show()
            }

            override fun onEnded(navigationMode: NavigationManager.NavigationMode) {
//                Toast.makeText(m_activity, "$navigationMode was ended", Toast.LENGTH_SHORT).show()
                homeFragment.removeAllMapObjects()
            }

            override fun onMapUpdateModeChanged(mapUpdateMode: NavigationManager.MapUpdateMode) {
//                Toast.makeText(
//                    m_activity, "Map update mode is changed to $mapUpdateMode",
//                    Toast.LENGTH_SHORT
//                ).show()
            }

            override fun onRouteUpdated(route: Route) {
//                Toast.makeText(m_activity, "Route updated", Toast.LENGTH_SHORT).show()
            }

            override fun onCountryInfo(s: String, s1: String) {
                Toast.makeText(
                    homeFragment.requireActivity(), "Country info updated from $s to $s1",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
}