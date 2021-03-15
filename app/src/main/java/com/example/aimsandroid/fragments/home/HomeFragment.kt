package com.example.aimsandroid.fragments.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.aimsandroid.R
import com.example.aimsandroid.databinding.FragmentHomeBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.here.android.mpa.common.GeoCoordinate
import com.here.android.mpa.common.OnEngineInitListener
import com.here.android.mpa.mapping.AndroidXMapFragment
import com.here.android.mpa.mapping.Map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class HomeFragment : Fragment() {

    companion object {
        fun newInstance() = HomeFragment()
    }
    private lateinit var map: com.here.android.mpa.mapping.Map
    private lateinit var mapFragment: AndroidXMapFragment
    private lateinit var viewModel: HomeViewModel
    private lateinit var sheetBehavior: BottomSheetBehavior<LinearLayout>
    private lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater);
        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
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

        binding.navFab.setOnClickListener {
            it?.let {
                map.setCenter(GeoCoordinate(viewModel.latitude.value!!, viewModel.longitude.value!!), Map.Animation.BOW, map.maxZoomLevel*0.75, 0.0f, 0.1f)
            }
        }
        initializeMap()
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
        // Search for the map fragment to finish setup by calling init().
        mapFragment = childFragmentManager.findFragmentById(R.id.mapView) as AndroidXMapFragment

        // Set up disk map cache path for this application
        // Use path under your application folder for storing the disk cache
//        MapSettings.setDiskCacheRootPath(
//            ApplicationProvider.getApplicationContext()
//                .getExternalFilesDir(nu  ll) + File.separator.toString() + ".here-maps"
//        )
        mapFragment.init(OnEngineInitListener { error ->
            if (error == OnEngineInitListener.Error.NONE) {
                // retrieve a reference of the map from the map fragment
                map = mapFragment.getMap()!!
                // Set the map center to the Vancouver region (no animation)
                map.setCenter(
                    GeoCoordinate(39.8097, -98.5556, 0.0),
                    Map.Animation.NONE
                )
                map.positionIndicator.isVisible = true
                map.positionIndicator.isSmoothPositionChange = true
                map.setZoomLevel(map.maxZoomLevel*0.15)
            } else {
                println("ERROR: Cannot initialize Map Fragment")
            }
        })
    }
//    override fun onResume() {
//        super.onResume()
//        mapview.onResume()
//    }
//
//    override fun onPause() {
//        super.onPause()
//        mapview.onPause()
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        mapview.onDestroy()
//    }
}