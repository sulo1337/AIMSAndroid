package com.example.aimsandroid.fragments.home

import android.graphics.PointF
import android.widget.Toast
import com.here.android.mpa.common.GeoPosition
import com.here.android.mpa.common.ViewObject
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
            m_navigationManager.nextManeuver
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

            }

            override fun onNavigationModeChanged() {
//                Toast.makeText(m_activity, "Navigation mode changed", Toast.LENGTH_SHORT).show()
            }

            override fun onEnded(navigationMode: NavigationManager.NavigationMode) {
                homeFragment.onNavigationEnded()
            }

            override fun onDestinationReached() {
                Toast.makeText(homeFragment.requireContext(), "Arrived", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(
                    homeFragment.requireActivity(), "Country info updated from $s to $s1",
                    Toast.LENGTH_SHORT
                ).show()
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

    fun pauseRoadView(){
        homeFragment.pauseRoadView()
    }

    fun resumeRoadView() {
        homeFragment.resumeRoadView()
    }
}