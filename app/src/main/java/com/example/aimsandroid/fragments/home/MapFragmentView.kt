package com.example.aimsandroid.fragments.home

import android.app.AlertDialog
import android.content.Context
import android.content.res.Configuration
import android.view.Gravity
import android.view.Window
import android.widget.Button
import android.widget.Toast
import com.example.aimsandroid.R
import com.example.aimsandroid.utils.MapTransformListener
import com.here.android.mpa.common.*
import com.here.android.mpa.guidance.NavigationManager
import com.here.android.mpa.guidance.NavigationManager.MapUpdateMode
import com.here.android.mpa.mapping.AndroidXMapFragment
import com.here.android.mpa.mapping.Map
import com.here.android.mpa.mapping.MapRoute
import com.here.android.mpa.routing.*
import getDouble
import putDouble
import java.lang.ref.WeakReference


/**
 * This class encapsulates the properties and functionality of the Map view.It also triggers a
 * turn-by-turn navigation from HERE Burnaby office to Langley BC.There is a sample voice skin
 * bundled within the SDK package to be used out-of-box, please refer to the Developer's guide for
 * the usage.
 */
class MapFragmentView(
    private val parentFragment: HomeFragment,
    private val initializeViewModel: Runnable
) {
    private val m_activity = parentFragment.requireActivity()
    private var m_mapFragment: AndroidXMapFragment? = null
    private var m_naviControlButton: Button? = null
    var m_map: Map? = null
    private var m_navigationManager: NavigationManager? = null
    private var m_positioningManager: PositioningManager? = null
    private var m_hereLocation: LocationDataSourceHERE? = null
    private var m_geoBoundingBox: GeoBoundingBox? = null
    private var m_route: Route? = null
    private var m_foregroundServiceStarted = false
    private lateinit var mapFragment: AndroidXMapFragment

    fun getMap(): Map? {
        return m_map
    }

    private fun initMapFragment(latitude: Double, longitude: Double, zoomLevel: Double) {
        /* Locate the mapFragment UI element */
        m_mapFragment = mapFragment
        if (m_mapFragment != null) {
            /* Initialize the AndroidXMapFragment, results will be given via the called back. */
            m_mapFragment!!.init { error ->
                if (error == OnEngineInitListener.Error.NONE) {
                    m_map = m_mapFragment!!.map
                    m_map?.let{
                        it.setCenter(
                            GeoCoordinate(latitude, longitude, 0.0),
                            Map.Animation.NONE
                        )
                        it.setZoomLevel(zoomLevel)
                        it.positionIndicator.isVisible = true
                        val mode = m_activity.applicationContext?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)
                        it.addTransformListener(MapTransformListener())
                        m_navigationManager = NavigationManager.getInstance()
                        when (mode) {
                            Configuration.UI_MODE_NIGHT_YES -> {
                                it.mapScheme = Map.Scheme.NORMAL_NIGHT
                            }
                            Configuration.UI_MODE_NIGHT_NO -> {
                                it.mapScheme = Map.Scheme.NORMAL_DAY
                            }
                            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                                it.mapScheme = Map.Scheme.NORMAL_DAY
                            }
                        }
                    }
                    initializeViewModel.run()
                } else {
                    Toast.makeText(m_activity, "Cannot init map", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun navigate(srcGeoCoordinate: GeoCoordinate, destGeoCoordinate: GeoCoordinate){
        createRoute(srcGeoCoordinate, destGeoCoordinate)
    }

    private fun createRoute(srcGeoCoordinate: GeoCoordinate, destGeoCoordinate: GeoCoordinate) {
        /* Initialize a CoreRouter */
        val coreRouter = CoreRouter()

        /* Initialize a RoutePlan */
        val routePlan = RoutePlan()

        /*
         * Initialize a RouteOption. HERE Mobile SDK allow users to define their own parameters for the
         * route calculation,including transport modes,route types and route restrictions etc.Please
         * refer to API doc for full list of APIs
         */
        val routeOptions = RouteOptions()
        /* Other transport modes are also available e.g Pedestrian */routeOptions.transportMode =
            RouteOptions.TransportMode.CAR
        /* Disable highway in this route. */routeOptions.setHighwaysAllowed(false)
        /* Calculate the shortest route available. */routeOptions.routeType = RouteOptions.Type.SHORTEST
        /* Calculate 1 route. */routeOptions.routeCount = 1
        /* Finally set the route option */routePlan.routeOptions = routeOptions

        /* Define waypoints for the route */
        /* START: 4350 Still Creek Dr */
        val startPoint = RouteWaypoint(srcGeoCoordinate)
        /* END: Langley BC */
        val destination = RouteWaypoint(destGeoCoordinate)

        /* Add both waypoints to the route plan */routePlan.addWaypoint(startPoint)
        routePlan.addWaypoint(destination)

        /* Trigger the route calculation,results will be called back via the listener */coreRouter.calculateRoute(
            routePlan,
            object : Router.Listener<List<RouteResult>, RoutingError> {
                override fun onProgress(i: Int) {
                    /* The calculation progress can be retrieved in this callback. */
                }

                override fun onCalculateRouteFinished(
                    routeResults: List<RouteResult>?,
                    routingError: RoutingError
                ) {
                    /* Calculation is done.Let's handle the result */
                    routeResults?.let {
                        if (routingError == RoutingError.NONE) {
                            if (routeResults[0].route != null) {
                                m_route = routeResults[0].route
                                /* Create a MapRoute so that it can be placed on the map */
                                val mapRoute = MapRoute(
                                    routeResults[0].route
                                )

                                /* Show the maneuver number on top of the route */mapRoute.isManeuverNumberVisible =
                                    true

                                /* Add the MapRoute to the map */m_map!!.addMapObject(mapRoute)

                                /*
                                     * We may also want to make sure the map view is orientated properly
                                     * so the entire route can be easily seen.
                                     */m_geoBoundingBox = routeResults[0].route.boundingBox
                                m_map!!.zoomTo(
                                    m_geoBoundingBox!!, Map.Animation.BOW,
                                    Map.MOVE_PRESERVE_ORIENTATION
                                )
                                startNavigation()
                            } else {
                                Toast.makeText(
                                    m_activity,
                                    "Error:route results returned is not valid",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        } else {
                            Toast.makeText(
                                m_activity,
                                "Error:route calculation returned error code: $routingError",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }

                }
            })
    }


    /*
     * Android 8.0 (API level 26) limits how frequently background apps can retrieve the user's
     * current location. Apps can receive location updates only a few times each hour.
     * See href="https://developer.android.com/about/versions/oreo/background-location-limits.html
     * In order to retrieve location updates more frequently start a foreground service.
     * See https://developer.android.com/guide/components/services.html#Foreground
     */

    private fun startNavigation() {
        /* Configure Navigation manager to launch navigation on current map */
        m_navigationManager!!.setMap(m_map)
        // show position indicator
        // note, it is also possible to change icon for the position indicator
        m_mapFragment!!.positionIndicator!!.isVisible = true

        /*
         * Start the turn-by-turn navigation.Please note if the transport mode of the passed-in
         * route is pedestrian, the NavigationManager automatically triggers the guidance which is
         * suitable for walking. Simulation and tracking modes can also be launched at this moment
         * by calling either simulate() or startTracking()
         */

        /* Choose navigation modes between real time navigation and simulation */
        val alertDialogBuilder = AlertDialog.Builder(m_activity, R.style.AlertDialogTheme)
        alertDialogBuilder.setTitle("Navigation")
        alertDialogBuilder.setMessage("Choose Mode")
        alertDialogBuilder.setNegativeButton(
            "Navigation"
        ) { dialoginterface, i ->
            m_navigationManager!!.startNavigation(m_route!!)
            m_map!!.tilt = 0f
//            startForegroundService()
        }
        alertDialogBuilder.setPositiveButton(
            "Simulation"
        ) { dialoginterface, i ->
            m_navigationManager!!.simulate(m_route!!, 60) //Simualtion speed is set to 60 m/s
            m_map!!.tilt = 0f
        }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val wmlp = alertDialog.window!!.attributes
        wmlp.gravity = Gravity.TOP or Gravity.LEFT
        wmlp.verticalMargin = 0.6f
        alertDialog.show()

        /*
         * Set the map update mode to ROADVIEW.This will enable the automatic map movement based on
         * the current location.If user gestures are expected during the navigation, it's
         * recommended to set the map update mode to NONE first. Other supported update mode can be
         * found in HERE Mobile SDK for Android (Premium) API doc
         */m_navigationManager!!.mapUpdateMode = MapUpdateMode.ROADVIEW

        /*
         * NavigationManager contains a number of listeners which we can use to monitor the
         * navigation status and getting relevant instructions.In this example, we will add 2
         * listeners for demo purpose,please refer to HERE Android SDK API documentation for details
         */addNavigationListeners()
    }

    private fun addNavigationListeners() {

        val mapEventListeners = MapEventListeners(parentFragment)

        m_navigationManager!!.addNavigationManagerEventListener(
            WeakReference(mapEventListeners.m_navigationManagerEventListener)
        )

        m_navigationManager!!.addPositionListener(
            WeakReference(mapEventListeners.m_positionListener)
        )

        m_navigationManager!!.addManeuverEventListener(
            WeakReference(mapEventListeners.m_maneuverListener)
        )
    }

    fun onDestroy() {
        /* Stop the navigation when app is destroyed */
        if (m_navigationManager != null) {
            m_navigationManager!!.stop()
//            stopForegroundService()
        }
    }

    fun onPause(){
        //        val prefs = requireContext().getSharedPreferences("com.example.aimsandroid", Context.MODE_PRIVATE)
//        prefs.edit().putString("descriptionFormData", binding.description.text.toString()).apply()
//        prefs.edit().putInt("radioChoiceFormData", binding.atmosphere.checkedRadioButtonId).apply()
        val prefs = m_activity.applicationContext.getSharedPreferences("com.example.aimsandroid", Context.MODE_PRIVATE)
        prefs.edit().putDouble("lastFocusLatitude", m_map?.center?.latitude!!).apply()
        prefs.edit().putDouble("lastFocusLongitude", m_map?.center?.longitude!!).apply()
        prefs.edit().putDouble("lastZoomLevel", m_map?.zoomLevel!!).apply()
    }

    fun removeAllMapObjects(){
        m_map?.removeAllMapObjects()
        m_map?.positionIndicator?.isVisible = true
        m_map?.setOrientation(0.0f, Map.Animation.BOW)
    }

    init {
        mapFragment = parentFragment.childFragmentManager.findFragmentById(R.id.mapView) as AndroidXMapFragment
        val prefs = m_activity.applicationContext.getSharedPreferences("com.example.aimsandroid", Context.MODE_PRIVATE)
        var latitude = prefs.getDouble("lastFocusLatitude", 39.8097)
        var longitude = prefs.getDouble("lastFocusLongitude", -98.5556)
        var zoomLevel = prefs.getDouble("lastZoomLevel", 15.0)
        initMapFragment(latitude, longitude, zoomLevel)
    }
}
