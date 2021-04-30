package com.example.aimsandroid.fragments.home

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.util.Log
import android.view.Gravity
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.aimsandroid.R
import com.example.aimsandroid.utils.MapTransformListener
import com.here.android.mpa.common.*
import com.here.android.mpa.guidance.NavigationManager
import com.here.android.mpa.guidance.NavigationManager.MapUpdateMode
import com.here.android.mpa.guidance.VoiceCatalog
import com.here.android.mpa.guidance.VoicePackage
import com.here.android.mpa.mapping.AndroidXMapFragment
import com.here.android.mpa.mapping.Map
import com.here.android.mpa.mapping.MapRoute
import com.here.android.mpa.routing.*
import getDouble
import putDouble
import java.lang.ref.WeakReference
import java.util.*

open class MapFragmentView(
    private var parentFragment: HomeFragment,
    private var viewModel: HomeViewModel
) {
    private lateinit var prefs: SharedPreferences
    private val m_activity = parentFragment.requireActivity()
    private var m_mapFragment: AndroidXMapFragment? = null
    var m_map: Map? = null
    private var m_navigationManager: NavigationManager? = null
    private var m_positioningManager: PositioningManager? = null
    private var m_hereLocation: LocationDataSourceHERE? = null
    private var m_geoBoundingBox: GeoBoundingBox? = null
    private var m_route: Route? = null
    var m_currentRoute: MapRoute? = null
    private var m_foregroundServiceStarted = false
    private var mapEventListeners: MapEventListeners? = null
    private lateinit var mapFragment: AndroidXMapFragment
    fun getMap(): Map? {
        return m_map
    }

    init {
        initMapFragment()
    }

    companion object Singleton{
        private var instance: MapFragmentView? = null
        fun getInstance(parentFragment: HomeFragment,
                        viewModel: HomeViewModel): MapFragmentView {
            if(instance == null) {
                instance = MapFragmentView(parentFragment, viewModel)
            } else {
                instance!!.parentFragment = parentFragment
                instance!!.viewModel = viewModel
            }
            return instance as MapFragmentView
        }
    }

    fun initMapFragment() {
        mapFragment = parentFragment.childFragmentManager.findFragmentById(R.id.mapView) as AndroidXMapFragment
        prefs = m_activity.applicationContext.getSharedPreferences("com.example.aimsandroid", Context.MODE_PRIVATE)
        val latitude = prefs.getDouble("lastFocusLatitude", 39.8097)
        val longitude = prefs.getDouble("lastFocusLongitude", -98.5556)
        val zoomLevel = prefs.getDouble("lastZoomLevel", 15.0)
        val orientation = prefs.getFloat("lastOrientation", 0.0f)
        /* Locate the mapFragment UI element */
        m_mapFragment = mapFragment
        if (m_mapFragment != null) {
            /* Initialize the AndroidXMapFragment, results will be given via the called back. */
            m_mapFragment!!.init { error ->
                if (error == OnEngineInitListener.Error.NONE) {
                    mapEventListeners = MapEventListeners(parentFragment, this)
                    m_mapFragment!!.mapGesture?.addOnGestureListener(mapEventListeners!!.gestureListener, 100 , true)
                    m_map = m_mapFragment!!.map
                    m_map?.let{
                        it.setCenter(
                            GeoCoordinate(latitude, longitude, 0.0),
                            Map.Animation.NONE
                        )
                        it.zoomLevel = zoomLevel
                        it.orientation = orientation
                        it.positionIndicator.isVisible = true
                        val mode = m_activity.applicationContext?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)
                        it.addTransformListener(MapTransformListener())
                        m_navigationManager = NavigationManager.getInstance()
                        m_navigationManager!!.distanceUnit = NavigationManager.UnitSystem.IMPERIAL_US
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
                    viewModel.map = m_map
                    //see if already in navigation mode
                    if(prefs.getBoolean("navigating", false)){
                        val startPoint = GeoCoordinate(viewModel.latitude.value!!, viewModel.longitude.value!!)
                        val lastNavigatedLatitude = prefs.getDouble("lastNavigatedLatitude", 32.5301225)
                        val lastNavigatedLongitude = prefs.getDouble("lastNavigatedLongitude", -92.0796938)
                        val destination = GeoCoordinate(lastNavigatedLatitude, lastNavigatedLongitude)
                        showDirections(startPoint, destination)
                    } else {
                        parentFragment.viewGpsFab()
                    }
                } else {
                    Toast.makeText(m_activity, "Cannot init map", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun showDirections(srcGeoCoordinate: GeoCoordinate, destGeoCoordinate: GeoCoordinate){
        Log.i("aimsDebug", "here")
        Log.i("aimsDebug", m_navigationManager?.navigationMode?.toString()!!)
        if(m_navigationManager?.navigationMode?.toString().equals("NONE")!!){
            createRoute(srcGeoCoordinate, destGeoCoordinate)
        } else {
            m_map?.removeAllMapObjects()
            m_map?.addMapObject(m_currentRoute!!)
            parentFragment.viewStopNavFab()
            startNavigation()
        }
    }

    private fun createRoute(srcGeoCoordinate: GeoCoordinate, destGeoCoordinate: GeoCoordinate) {
        //clear previous navigation data (if any)
        onNavigationEnded()
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
            RouteOptions.TransportMode.TRUCK
        /* Disable highway in this route. */routeOptions.setHighwaysAllowed(true)

        /* Calculate the shortest route available. */routeOptions.routeType = RouteOptions.Type.FASTEST
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
                    parentFragment.routeCalculationOnProgress(i)
                }

                override fun onCalculateRouteFinished(
                    routeResults: List<RouteResult>?,
                    routingError: RoutingError
                ) {
                    /* Calculation is done.Let's handle the result */
                    routeResults?.let {
                        if (routingError == RoutingError.NONE) {
                            if (routeResults[0].route != null) {
                                //TODO implement map data prefetcher
//                                val mapDataPrefetcher = MapDataPrefetcher.getInstance()
//                                mapDataPrefetcher.fetchMapData(m_route!!, 2000)
                                m_route = routeResults[0].route
                                /* Create a MapRoute so that it can be placed on the map */
                                m_currentRoute = MapRoute(
                                    m_route!!
                                )

                                /* Show the maneuver number on top of the route */m_currentRoute!!.isManeuverNumberVisible = true

                                /* Add the MapRoute to the map */m_map!!.addMapObject(m_currentRoute!!)

                                /*
                                     * We may also want to make sure the map view is orientated properly
                                     * so the entire route can be easily seen.
                                     */m_geoBoundingBox = routeResults[0].route.boundingBox
                                parentFragment.afterRouteCalculated(m_geoBoundingBox)
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

     fun startNavigation() {
        /* Configure Navigation manager to launch navigation on current map */
        m_navigationManager!!.setMap(m_map)
         if(m_navigationManager!!.countryCode == null) {
             setUpVoiceNavigation()
         }
        /*
         * Start the turn-by-turn navigation.Please note if the transport mode of the passed-in
         * route is pedestrian, the NavigationManager automatically triggers the guidance which is
         * suitable for walking. Simulation and tracking modes can also be launched at this moment
         * by calling either simulate() or startTracking()
         */

//        /* Choose navigation modes between real time navigation and simulation */
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
            m_navigationManager!!.simulate(m_route!!, 17)
            m_map!!.tilt = 0f
        }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val wmlp = alertDialog.window!!.attributes
        wmlp.gravity = Gravity.TOP or Gravity.LEFT
        wmlp.verticalMargin = 0.6f
         if(m_navigationManager!!.countryCode == null){
             alertDialog.show()
         }
//        m_navigationManager!!.startNavigation(m_route!!)
//        m_map!!.tilt = 0f

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

        m_navigationManager!!.addNavigationManagerEventListener(
            WeakReference(mapEventListeners!!.m_navigationManagerEventListener)
        )

        m_navigationManager!!.addPositionListener(
            WeakReference(mapEventListeners!!.m_positionListener)
        )

        m_navigationManager!!.addManeuverEventListener(
            WeakReference(mapEventListeners!!.m_maneuverListener)
        )

        m_navigationManager!!.addNewInstructionEventListener(
            WeakReference(mapEventListeners!!.m_instructionListener)
        )

        m_navigationManager!!.addLaneInformationListener(
            WeakReference(mapEventListeners!!.m_laneInformationListener)
        )

        m_navigationManager!!.addSpeedWarningListener(
            WeakReference(mapEventListeners!!.m_speedWarningListener)
        )

        m_navigationManager!!.audioPlayer.setDelegate(mapEventListeners!!.m_audioPlayerDelegate)
    }

    fun onPause(){
        val prefs = m_activity.applicationContext.getSharedPreferences("com.example.aimsandroid", Context.MODE_PRIVATE)
        prefs.edit().putDouble("lastFocusLatitude", m_map?.center?.latitude!!).apply()
        prefs.edit().putDouble("lastFocusLongitude", m_map?.center?.longitude!!).apply()
        prefs.edit().putFloat("lastOrientation", m_map?.orientation!!).apply()
        prefs.edit().putDouble("lastZoomLevel", m_map?.zoomLevel!!).apply()
        //onNavigationEnded()
    }

    fun onDestroy(){
//        onNavigationEnded()
    }

    fun onNavigationEnded() {
        if(m_navigationManager != null && mapEventListeners != null){
            m_navigationManager!!.stop()
            m_navigationManager!!.removeNavigationManagerEventListener(mapEventListeners!!.m_navigationManagerEventListener)
            m_navigationManager!!.removePositionListener(mapEventListeners!!.m_positionListener)
            m_navigationManager!!.removeManeuverEventListener(mapEventListeners!!.m_maneuverListener)
            m_navigationManager!!.removeNewInstructionEventListener(mapEventListeners!!.m_instructionListener)
            m_navigationManager!!.removeLaneInformationListener(mapEventListeners!!.m_laneInformationListener)
            m_navigationManager!!.removeSpeedWarningListener(mapEventListeners!!.m_speedWarningListener)
        }
        m_map?.removeAllMapObjects()
        m_map?.positionIndicator?.isVisible = true
        m_map?.setOrientation(0.0f, Map.Animation.BOW)
    }

    fun pauseRoadView() {
        if(m_navigationManager!=null) {
            m_navigationManager!!.mapUpdateMode = MapUpdateMode.NONE
        }
    }

    fun resumeRoadView() {
        if(m_navigationManager!=null){
            m_navigationManager!!.mapUpdateMode = MapUpdateMode.ROADVIEW
        }
    }

    private fun setUpVoiceNavigation() {
        val voiceCatalog = VoiceCatalog.getInstance()
        voiceCatalog.downloadCatalog { error ->
            if(error == VoiceCatalog.Error.NONE){
                searchVoiceCatalog(voiceCatalog)
            }
        }
    }

    private fun searchVoiceCatalog(voiceCatalog: VoiceCatalog){

        val voicePackages = voiceCatalog.catalogList
        for(vPackage in voicePackages){
            if(vPackage.bcP47Code.compareTo("en-US", ignoreCase = false) == 0 ){
                if(checkVoice(vPackage, voiceCatalog)) break
            }
        }
    }

    private fun checkVoice(voicePackage: VoicePackage, voiceCatalog: VoiceCatalog): Boolean{
        if(voicePackage.isTts){
            val voiceId = voicePackage.id
            voiceCatalog.downloadVoice(voiceId){error ->
                if(error == VoiceCatalog.Error.NONE){
                    val voiceGuidanceOptions = m_navigationManager!!.voiceGuidanceOptions
                    voiceGuidanceOptions.setVoiceSkin(voiceCatalog.getLocalVoiceSkin(voiceId)!!)
                    m_navigationManager!!.naturalGuidanceMode = EnumSet.allOf(NavigationManager.NaturalGuidanceMode::class.java)
                }
            }
            return true
        }
        return false
    }

    fun zoomTo(boundingBox: GeoBoundingBox?) {
        if (boundingBox != null) {
            m_map?.zoomTo(
                boundingBox, Map.Animation.BOW,
                Map.MOVE_PRESERVE_ORIENTATION
            )
        }
    }
}
