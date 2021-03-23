package com.example.aimsandroid.fragments.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.aimsandroid.databinding.FragmentHomeBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior


class HomeFragment : Fragment() {
    private lateinit var viewModel: HomeViewModel
    private lateinit var sheetBehavior: BottomSheetBehavior<LinearLayout>
    private lateinit var binding: FragmentHomeBinding
    private var mapFragmentView: MapFragmentView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater);

        //initialize map fragment
        mapFragmentView = MapFragmentView(requireActivity(), childFragmentManager, Runnable {
            //initialize viewmodel
            val homeViewModelFactory = HomeViewModelFactory(requireActivity().application)
            viewModel = ViewModelProvider(this, homeViewModelFactory).get(HomeViewModel::class.java)
            mapFragmentView?.let {
                viewModel.map = it.getMap()
            }
            binding.viewModel = viewModel
        })

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
//        binding.followFab.setOnClickListener {
//            if(mNavigationManager !=null) {
//                if(followMode) {
//                    followMode = false
//                    mNavigationManager.setMapUpdateMode(MapUpdateMode.NONE)
//                } else {
//                    followMode = true
//                    mNavigationManager.setMapUpdateMode(MapUpdateMode.ROADVIEW)
//                }
//
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

//    private fun initializeMap() {
//        mapFragment = childFragmentManager.findFragmentById(R.id.mapView) as AndroidXMapFragment
//
//        mapFragment.init { error ->
//            if (error == OnEngineInitListener.Error.NONE) {
//                val map = mapFragment.getMap()!!
//                map.setCenter(
//                    GeoCoordinate(39.8097, -98.5556, 0.0),
//                    Map.Animation.NONE
//                )
//                map.setZoomLevel(map.maxZoomLevel * 0.15)
//                val mode = context?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)
//                map.addTransformListener(MapTransformListener())
//                mNavigationManager = NavigationManager.getInstance()
//                mPositioningManager = PositioningManager.getInstance()
//                mHereLocation = LocationDataSourceHERE.getInstance()
//                mPositioningManager.setDataSource(mHereLocation)
//                mPositioningManager.addListener(WeakReference<PositioningManager.OnPositionChangedListener>(MapPositionChangedListener()))
//                if(mPositioningManager.start(PositioningManager.LocationMethod.GPS_NETWORK_INDOOR)) {
//                    Log.i("insideIf", "here")
//                    map.positionIndicator.setVisible(true)
//                }
//                when (mode) {
//                    Configuration.UI_MODE_NIGHT_YES -> {
//                        map.mapScheme = Map.Scheme.NORMAL_NIGHT
//                    }
//                    Configuration.UI_MODE_NIGHT_NO -> {
//                        map.mapScheme = Map.Scheme.NORMAL_DAY
//                    }
//                    Configuration.UI_MODE_NIGHT_UNDEFINED -> {
//                        map.mapScheme = Map.Scheme.NORMAL_DAY
//                    }
//                }
//                initializeViewModel(map)
//            } else {
//                println("ERROR: Cannot initialize Map Fragment")
//            }
//        }
//    }

    override fun onDestroy() {
        super.onDestroy()
        mapFragmentView?.onDestroy()
    }

    override fun onPause() {
        super.onPause()
        mapFragmentView?.onPause()
    }
}