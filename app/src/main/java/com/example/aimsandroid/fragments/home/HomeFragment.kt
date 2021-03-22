package com.example.aimsandroid.fragments.home

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.aimsandroid.R
import com.example.aimsandroid.databinding.FragmentHomeBinding
import com.example.aimsandroid.utils.MapPositionChangedListener
import com.example.aimsandroid.utils.MapTransformListener
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.here.android.mpa.common.*
import com.here.android.mpa.mapping.AndroidXMapFragment
import com.here.android.mpa.mapping.Map
import com.here.android.mpa.mapping.MapState
import java.lang.ref.WeakReference


class HomeFragment : Fragment() {
    private lateinit var mapFragment: AndroidXMapFragment
    private lateinit var viewModel: HomeViewModel
    private lateinit var sheetBehavior: BottomSheetBehavior<LinearLayout>
    private lateinit var binding: FragmentHomeBinding
    private lateinit var mPositioningManager: PositioningManager
    private lateinit var mHereLocation: LocationDataSourceHERE
    private var mTransforming: Boolean = false
    private var mPendingUpdate: Runnable? = null

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

//        binding.navFab.setOnClickListener {
//            it?.let {
//                viewModel.map.setCenter(GeoCoordinate(viewModel.latitude.value!!, viewModel.longitude.value!!), Map.Animation.BOW, map.maxZoomLevel*0.75, 0.0f, 0.1f)
//            }
//        }
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

//        MapSettings.setDiskCacheRootPath(
//            ApplicationProvider.getApplicationContext()
//                .getExternalFilesDir(nu  ll) + File.separator.toString() + ".here-maps"
//        )
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
    }
}