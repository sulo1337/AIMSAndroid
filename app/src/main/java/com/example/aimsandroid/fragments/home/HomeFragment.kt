package com.example.aimsandroid.fragments.home

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.example.aimsandroid.R
import com.example.aimsandroid.databinding.FragmentHomeBinding
import com.example.aimsandroid.service.ForegroundService
import com.example.aimsandroid.utils.MapPositionChangedListener
import com.example.aimsandroid.utils.MapTransformListener
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.here.android.mpa.common.*
import com.here.android.mpa.guidance.AudioPlayerDelegate
import com.here.android.mpa.guidance.NavigationManager
import com.here.android.mpa.guidance.NavigationManager.MapUpdateMode
import com.here.android.mpa.guidance.NavigationManager.NavigationManagerEventListener
import com.here.android.mpa.mapping.AndroidXMapFragment
import com.here.android.mpa.mapping.Map
import com.here.android.mpa.mapping.MapRoute
import com.here.android.mpa.routing.*
import java.lang.ref.WeakReference


class HomeFragment : Fragment() {
    private lateinit var mapFragment: AndroidXMapFragment
    private lateinit var viewModel: HomeViewModel
    private lateinit var sheetBehavior: BottomSheetBehavior<LinearLayout>
    private lateinit var binding: FragmentHomeBinding
    private lateinit var mPositioningManager: PositioningManager
    private lateinit var mHereLocation: LocationDataSourceHERE
    private lateinit var mNavigationManager: NavigationManager
    private lateinit var mGeoBoundingBox: GeoBoundingBox
    private lateinit var mRoute: Route
    private var followMode: Boolean = false
    private var mForegroundServiceStarted: Boolean = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater);
        initializeMap()
        binding.lifecycleOwner = this
        //drawer layout
        val backdropHeader = binding.backdropHeader
        val contentLayout = binding.contentLayout
        sheetBehavior = BottomSheetBehavior.from(contentLayout)
        sheetBehavior.isFitToContents = false
        sheetBehavior.isHideable = false
        sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        backdropHeader.setOnClickListener { it ->
            toggleFilters()
        }
        binding.followFab.setOnClickListener {
            if(mNavigationManager !=null) {
                if(followMode) {
                    followMode = false
                    mNavigationManager.setMapUpdateMode(MapUpdateMode.NONE)
                } else {
                    followMode = true
                    mNavigationManager.setMapUpdateMode(MapUpdateMode.ROADVIEW)
                }

            }
        }
        return binding.root;
    }

    private fun toggleFilters() {
        if(sheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED){
            sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        } else if(sheetBehavior.state == BottomSheetBehavior.STATE_HALF_EXPANDED) {
            sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        } else {
            sheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    private fun initializeMap() {
        mapFragment = childFragmentManager.findFragmentById(R.id.mapView) as AndroidXMapFragment

        mapFragment.init { error ->
            if (error == OnEngineInitListener.Error.NONE) {
                val map = mapFragment.getMap()!!
                map.setCenter(
                    GeoCoordinate(39.8097, -98.5556, 0.0),
                    Map.Animation.NONE
                )
                map.setZoomLevel(map.maxZoomLevel * 0.15)
                val mode = context?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)
                map.addTransformListener(MapTransformListener())
                mNavigationManager = NavigationManager.getInstance()
                mPositioningManager = PositioningManager.getInstance()
                mHereLocation = LocationDataSourceHERE.getInstance()
                mPositioningManager.setDataSource(mHereLocation)
                mPositioningManager.addListener(WeakReference<PositioningManager.OnPositionChangedListener>(MapPositionChangedListener()))
                if(mPositioningManager.start(PositioningManager.LocationMethod.GPS_NETWORK_INDOOR)) {
                    Log.i("insideIf", "here")
                    map.positionIndicator.setVisible(true)
                }
                when (mode) {
                    Configuration.UI_MODE_NIGHT_YES -> {
                        map.mapScheme = Map.Scheme.NORMAL_NIGHT
                    }
                    Configuration.UI_MODE_NIGHT_NO -> {
                        map.mapScheme = Map.Scheme.NORMAL_DAY
                    }
                    Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                        map.mapScheme = Map.Scheme.NORMAL_DAY
                    }
                }
                initializeViewModel(map)
                createRoute()
            } else {
                println("ERROR: Cannot initialize Map Fragment")
            }
        }
    }

    private fun initializeViewModel(map: Map) {
        val homeViewModelFactory = HomeViewModelFactory(requireActivity().application, map)
        viewModel = ViewModelProvider(this, homeViewModelFactory).get(HomeViewModel::class.java)
        binding.viewModel = viewModel
        viewModel.recenterMapNoAnimation()
    }

    private fun createRoute() {
        val coreRouter = CoreRouter()
        val routePlan = RoutePlan()

        val routeOptions = RouteOptions()
        routeOptions.setTransportMode(RouteOptions.TransportMode.TRUCK)
        routeOptions.setHighwaysAllowed(true)
        routeOptions.setRouteType(RouteOptions.Type.SHORTEST)
        routeOptions.setRouteCount(1)
        routePlan.setRouteOptions(routeOptions)

        val startPoint = RouteWaypoint(GeoCoordinate(viewModel.latitude.value!!, viewModel.longitude.value!!))
        val destination = RouteWaypoint(GeoCoordinate(32.5300467,-92.0782897))
        routePlan.addWaypoint(startPoint)
        routePlan.addWaypoint(destination)

        coreRouter.calculateRoute(routePlan, object: Router.Listener<List<RouteResult>, RoutingError>{
            override fun onProgress(percent: Int) {
                //ignore
            }

            override fun onCalculateRouteFinished(routeResults: List<RouteResult>?, routingError: RoutingError) {
                if(routingError == RoutingError.NONE){
                    routeResults?.let {
                        mRoute = routeResults.get(0).route
                        val mapRoute = MapRoute(routeResults.get(0).route)
                        mapRoute.setManeuverNumberVisible(true)
                        viewModel.map.addMapObject(mapRoute)
                        mGeoBoundingBox = routeResults.get(0).route.boundingBox!!
                        viewModel.map.zoomTo(mGeoBoundingBox, Map.Animation.BOW, Map.MOVE_PRESERVE_ORIENTATION)
                        startNavigation()
                    }
                } else {
                    createRoute()
                }
            }

        })
    }

    private fun startNavigation() {
        mNavigationManager.setMap(viewModel.map)
        mNavigationManager.startNavigation(mRoute)
        viewModel.map.tilt = 0.0f
        startForegroundService()
        mNavigationManager.mapUpdateMode = MapUpdateMode.ROADVIEW
        mNavigationManager.distanceUnit = NavigationManager.UnitSystem.IMPERIAL_US
        addNavigationListeners()
    }

    private fun addNavigationListeners() {
        /*
         * Register a NavigationManagerEventListener to monitor the status change on
         * NavigationManager
         */
        /*
         * Register a NavigationManagerEventListener to monitor the status change on
         * NavigationManager
         */mNavigationManager.addNavigationManagerEventListener(
            WeakReference(mNavigationManagerEventListener)
        )

        /* Register a PositionListener to monitor the position updates */

        /* Register a PositionListener to monitor the position updates */mNavigationManager.addPositionListener(
            WeakReference(mPositionListener)
        )

        /* Register a AudioPlayerDelegate to monitor TTS text */

        /* Register a AudioPlayerDelegate to monitor TTS text */mNavigationManager.getAudioPlayer()
            .setDelegate(mAudioPlayerDelegate)
    }

    private val mPositionListener: NavigationManager.PositionListener = object : NavigationManager.PositionListener() {
        override fun onPositionUpdated(geoPosition: GeoPosition) {
            /* Current position information can be retrieved in this callback */
        }
    }

    private val mNavigationManagerEventListener: NavigationManagerEventListener =
        object : NavigationManagerEventListener() {
            override fun onRunningStateChanged() {
            }

            override fun onNavigationModeChanged() {
            }

            override fun onEnded(navigationMode: NavigationManager.NavigationMode) {
                stopForegroundService()
            }

            override fun onMapUpdateModeChanged(mapUpdateMode: MapUpdateMode) {
            }

            override fun onRouteUpdated(p0: Route) {
            }

            override fun onCountryInfo(s: String, s1: String) {
            }
        }

    private val mAudioPlayerDelegate: AudioPlayerDelegate = object : AudioPlayerDelegate {
        override fun playText(s: String): Boolean {
            return false
        }

        override fun playFiles(strings: Array<String>): Boolean {
            return false
        }
    }

    private fun startForegroundService() {
        if(!mForegroundServiceStarted) {
            mForegroundServiceStarted = true
            val startIntent = Intent(requireActivity(), ForegroundService::class.java)
            startIntent.setAction(ForegroundService.START_ACTION)
            requireActivity().applicationContext.startService(startIntent)
        }
    }

    private fun stopForegroundService() {
        if (mForegroundServiceStarted) {
            mForegroundServiceStarted = false
            val stopIntent = Intent(requireActivity(), ForegroundService::class.java)
            stopIntent.action = ForegroundService.STOP_ACTION
            requireActivity().getApplicationContext().startService(stopIntent)
        }
    }

    override fun onResume() {
        super.onResume()
        mapFragment.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapFragment.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapFragment.onDestroy()
        if(mNavigationManager !=null) {
            stopForegroundService()
            mNavigationManager.stop()
        }
    }
}