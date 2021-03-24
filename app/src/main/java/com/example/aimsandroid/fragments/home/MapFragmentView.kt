package com.example.aimsandroid.fragments.home

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.content.res.Configuration
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.example.aimsandroid.R
import com.example.aimsandroid.service.ForegroundService
import com.example.aimsandroid.utils.MapPositionChangedListener
import com.example.aimsandroid.utils.MapTransformListener
import com.here.android.mpa.common.*
import com.here.android.mpa.guidance.NavigationManager
import com.here.android.mpa.guidance.NavigationManager.MapUpdateMode
import com.here.android.mpa.guidance.NavigationManager.NavigationManagerEventListener
import com.here.android.mpa.mapping.AndroidXMapFragment
import com.here.android.mpa.mapping.Map
import com.here.android.mpa.mapping.MapRoute
import com.here.android.mpa.routing.*
import java.lang.ref.WeakReference


/**
 * This class encapsulates the properties and functionality of the Map view.It also triggers a
 * turn-by-turn navigation from HERE Burnaby office to Langley BC.There is a sample voice skin
 * bundled within the SDK package to be used out-of-box, please refer to the Developer's guide for
 * the usage.
 */
class MapFragmentView(
    private val m_activity: FragmentActivity,
    childFragmentManager: FragmentManager,
    private val initializeViewModel: Runnable
) {
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
            Log.i("here", "here2")
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
                        val mode = m_activity.applicationContext?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)
                        it.addTransformListener(MapTransformListener())
                        m_navigationManager = NavigationManager.getInstance()
                        m_positioningManager = PositioningManager.getInstance()
                        m_hereLocation = LocationDataSourceHERE.getInstance()
                        m_positioningManager!!.setDataSource(m_hereLocation)
                        m_positioningManager!!.addListener(WeakReference<PositioningManager.OnPositionChangedListener>(
                            MapPositionChangedListener()
                        ))
                        if(m_positioningManager!!.start(PositioningManager.LocationMethod.GPS_NETWORK_INDOOR)) {
                            Log.i("insideIf", "here")
                            it.positionIndicator.setVisible(true)
                        }
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
                    createRoute()
                } else {
                    Toast.makeText(m_activity, "Cannot init map", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun createRoute() {
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
        val startPoint = RouteWaypoint(GeoCoordinate(49.259149, -123.008555))
        /* END: Langley BC */
        val destination = RouteWaypoint(GeoCoordinate(49.073640, -122.559549))

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
//                                m_map!!.zoomTo(
//                                    m_geoBoundingBox!!, Map.Animation.NONE,
//                                    Map.MOVE_PRESERVE_ORIENTATION
//                                )
//                                startNavigation()
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
    private fun startForegroundService() {
        if (!m_foregroundServiceStarted) {
            m_foregroundServiceStarted = true
            val startIntent = Intent(m_activity, ForegroundService::class.java)
            startIntent.action = ForegroundService.START_ACTION
            m_activity.applicationContext.startService(startIntent)
        }
    }

    private fun stopForegroundService() {
        if (m_foregroundServiceStarted) {
            m_foregroundServiceStarted = false
            val stopIntent = Intent(m_activity, ForegroundService::class.java)
            stopIntent.action = ForegroundService.STOP_ACTION
            m_activity.applicationContext.startService(stopIntent)
        }
    }

    private fun startNavigation() {
        /* Configure Navigation manager to launch navigation on current map */m_navigationManager!!.setMap(m_map)
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
        val alertDialogBuilder = AlertDialog.Builder(m_activity)
        alertDialogBuilder.setTitle("Navigation")
        alertDialogBuilder.setMessage("Choose Mode")
        alertDialogBuilder.setNegativeButton(
            "Navigation"
        ) { dialoginterface, i ->
            m_navigationManager!!.startNavigation(m_route!!)
            m_map!!.tilt = 60f
            startForegroundService()
        }
        alertDialogBuilder.setPositiveButton(
            "Simulation"
        ) { dialoginterface, i ->
            m_navigationManager!!.simulate(m_route!!, 60) //Simualtion speed is set to 60 m/s
            m_map!!.tilt = 60f
            startForegroundService()
        }
        val alertDialog = alertDialogBuilder.create()
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

        /*
         * Register a NavigationManagerEventListener to monitor the status change on
         * NavigationManager
         */
        m_navigationManager!!.addNavigationManagerEventListener(
            WeakReference(
                m_navigationManagerEventListener
            )
        )

        /* Register a PositionListener to monitor the position updates */m_navigationManager!!.addPositionListener(
            WeakReference(m_positionListener)
        )
    }

    private val m_positionListener: NavigationManager.PositionListener = object : NavigationManager.PositionListener() {
        override fun onPositionUpdated(geoPosition: GeoPosition) {
            /* Current position information can be retrieved in this callback */
        }
    }
    private val m_navigationManagerEventListener: NavigationManagerEventListener =
        object : NavigationManagerEventListener() {
            override fun onRunningStateChanged() {
                Toast.makeText(m_activity, "Running state changed", Toast.LENGTH_SHORT).show()
            }

            override fun onNavigationModeChanged() {
                Toast.makeText(m_activity, "Navigation mode changed", Toast.LENGTH_SHORT).show()
            }

            override fun onEnded(navigationMode: NavigationManager.NavigationMode) {
                Toast.makeText(m_activity, "$navigationMode was ended", Toast.LENGTH_SHORT).show()
                stopForegroundService()
            }

            override fun onMapUpdateModeChanged(mapUpdateMode: MapUpdateMode) {
                Toast.makeText(
                    m_activity, "Map update mode is changed to $mapUpdateMode",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onRouteUpdated(route: Route) {
                Toast.makeText(m_activity, "Route updated", Toast.LENGTH_SHORT).show()
            }

            override fun onCountryInfo(s: String, s1: String) {
                Toast.makeText(
                    m_activity, "Country info updated from $s to $s1",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    fun onDestroy() {
        /* Stop the navigation when app is destroyed */
        if (m_navigationManager != null) {
            stopForegroundService()
//            m_navigationManager!!.stop()
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

    fun Editor.putDouble(key: String, double: Double) =
        putLong(key, java.lang.Double.doubleToRawLongBits(double))

    fun SharedPreferences.getDouble(key: String, default: Double) =
        java.lang.Double.longBitsToDouble(getLong(key, java.lang.Double.doubleToRawLongBits(default)))

    init {
        mapFragment = childFragmentManager.findFragmentById(R.id.mapView) as AndroidXMapFragment
        val prefs = m_activity.applicationContext.getSharedPreferences("com.example.aimsandroid", Context.MODE_PRIVATE)
        var latitude = prefs.getDouble("lastFocusLatitude", 39.8097)
        var longitude = prefs.getDouble("lastFocusLongitude", -98.5556)
        var zoomLevel = prefs.getDouble("lastZoomLevel", 15.0)
        initMapFragment(latitude, longitude, zoomLevel)
    }
}
