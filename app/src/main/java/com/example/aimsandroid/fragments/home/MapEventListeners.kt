package com.example.aimsandroid.fragments.home

import android.graphics.PointF
import android.util.Log
import android.widget.Toast
import com.example.aimsandroid.utils.*
import com.here.android.mpa.common.*
import com.here.android.mpa.guidance.AudioPlayerDelegate
import com.here.android.mpa.guidance.LaneInformation
import com.here.android.mpa.guidance.NavigationManager
import com.here.android.mpa.mapping.MapGesture
import com.here.android.mpa.mapping.MapRoute
import com.here.android.mpa.routing.Route


class MapEventListeners(private val homeFragment: HomeFragment, private val mapFragmentView: MapFragmentView) {

    private var m_navigationManager: NavigationManager = NavigationManager.getInstance()

    val m_maneuverListener: NavigationManager.ManeuverEventListener = object: NavigationManager.ManeuverEventListener(){
        override fun onManeuverEvent() {
        }
    }

    val m_instructionListener: NavigationManager.NewInstructionEventListener = object : NavigationManager.NewInstructionEventListener() {
        override fun onNewInstructionEvent() {
            homeFragment.updateNextManeuverIcon(m_navigationManager.nextManeuver?.icon)
        }
    }

    val m_laneInformationListener: NavigationManager.LaneInformationListener = object : NavigationManager.LaneInformationListener() {
        override fun onLaneInformation(laneInformations: MutableList<LaneInformation>, roadElement: RoadElement?) {
            if(laneInformations.size > 0) {

            } else {

            }
        }
    }

    val m_speedWarningListener: NavigationManager.SpeedWarningListener = object : NavigationManager.SpeedWarningListener() {
        override fun onSpeedExceeded(p0: String, p1: Float) {
            homeFragment.onSpeedExceeded()
        }

        override fun onSpeedExceededEnd(p0: String, p1: Float) {
            homeFragment.onSpeedExceededEnd()
        }
    }

    val m_positionListener: NavigationManager.PositionListener = object : NavigationManager.PositionListener() {
        override fun onPositionUpdated(geoPosition: GeoPosition) {
            homeFragment.updateCurrentSpeed(geoPosition.speed)
            if(m_navigationManager.nextManeuver!=null) {
                homeFragment.updateNextStreet(m_navigationManager.nextManeuver!!.nextRoadNumber, m_navigationManager.nextManeuver!!.nextRoadName)
                homeFragment.updateDistanceToNextManeuver(m_navigationManager.nextManeuverDistance)
                homeFragment.updateEta(m_navigationManager.getEta(true, Route.TrafficPenaltyMode.OPTIMAL))
                homeFragment.updateDistanceRemaining(m_navigationManager.getRemainingDistance(Route.WHOLE_ROUTE))
                if(geoPosition is MatchedGeoPosition) {
                    geoPosition.roadElement?.speedLimit?.let { homeFragment.updateSpeedLimit(it) }
                }
//                homeFragment.updateSpeedLimit(m_navigationManager.lane)
            }
        }
    }
    val m_navigationManagerEventListener: NavigationManager.NavigationManagerEventListener =
        object : NavigationManager.NavigationManagerEventListener() {
            override fun onRunningStateChanged() {

            }

            override fun onNavigationModeChanged() {
//                Toast.makeText(m_activity, "Navigation mode changed", Toast.LENGTH_SHORT).show()
            }

            override fun onEnded(navigationMode: NavigationManager.NavigationMode) {
                homeFragment.endNavigationOnly()
            }

            override fun onDestinationReached() {
                homeFragment.onDestinationReached()
            }

            override fun onMapUpdateModeChanged(mapUpdateMode: NavigationManager.MapUpdateMode) {
//                Toast.makeText(
//                    m_activity, "Map update mode is changed to $mapUpdateMode",
//                    Toast.LENGTH_SHORT
//                ).show()
            }

            override fun onRouteUpdated(route: Route) {
                //replace old route with new one
                mapFragmentView.m_map?.removeMapObject(mapFragmentView.m_currentRoute!!)
                mapFragmentView.m_currentRoute = MapRoute(route)
                mapFragmentView.m_map?.addMapObject(mapFragmentView.m_currentRoute!!)
            }

            override fun onCountryInfo(s: String, s1: String) {
                Toast.makeText(homeFragment.requireContext(), "Country info changed", Toast.LENGTH_SHORT).show()
            }
        }

    val gestureListener: MapGesture.OnGestureListener = object: MapGesture.OnGestureListener{
        override fun onPanStart() {
            pauseRoadView()
        }

        override fun onPanEnd() {
            pauseRoadView()
        }

        override fun onMultiFingerManipulationStart() {
            pauseRoadView()
        }

        override fun onMultiFingerManipulationEnd() {
            pauseRoadView()
        }

        override fun onMapObjectsSelected(p0: MutableList<ViewObject>): Boolean {
            pauseRoadView()
            return false
        }

        override fun onTapEvent(p0: PointF): Boolean {
            return false
        }

        override fun onDoubleTapEvent(p0: PointF): Boolean {
            resumeRoadView()
            return false
        }

        override fun onPinchLocked() {
            pauseRoadView()
        }

        override fun onPinchZoomEvent(p0: Float, p1: PointF): Boolean {
            pauseRoadView()
            return false
        }

        override fun onRotateLocked() {
            pauseRoadView()
        }

        override fun onRotateEvent(p0: Float): Boolean {
            pauseRoadView()
            return false
        }

        override fun onTiltEvent(p0: Float): Boolean {
            return false
        }

        override fun onLongPressEvent(p0: PointF): Boolean {
            return false
        }

        override fun onLongPressRelease() {

        }

        override fun onTwoFingerTapEvent(p0: PointF): Boolean {
            return false
        }
    }

    val m_audioPlayerDelegate: AudioPlayerDelegate = object : AudioPlayerDelegate {
        override fun playText(s: String): Boolean {
            Log.i("aimsDebugNavigationAudio", s)
            return false
        }

        override fun playFiles(strings: Array<String>): Boolean {
            return false
        }
    }

    fun pauseRoadView(){
        homeFragment.pauseRoadView()
    }

    fun resumeRoadView() {
        homeFragment.resumeRoadView()
    }
}