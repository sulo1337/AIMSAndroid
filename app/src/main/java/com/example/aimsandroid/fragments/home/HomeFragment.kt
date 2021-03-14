package com.example.aimsandroid.fragments.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentManager
import com.example.aimsandroid.R
import com.example.aimsandroid.databinding.FragmentHomeBinding
import com.example.aimsandroid.utils.OnBottomSheetCallbacks
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.lang.Exception
import kotlin.math.log

class HomeFragment : Fragment() {

    companion object {
        fun newInstance() = HomeFragment()
    }

    private lateinit var viewModel: HomeViewModel
    private lateinit var sheetBehavior: BottomSheetBehavior<LinearLayout>
    private lateinit var binding: FragmentHomeBinding
    private lateinit var mapview: MapView
    private lateinit var map: GoogleMap

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater);

        mapview = binding.mapView
        mapview.onCreate(savedInstanceState)
//        mapview.getMapAsync { googleMap ->
//            map = googleMap
//            try {
//                MapsInitializer.initialize(requireActivity())
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//
//            val cameraUpdate: CameraUpdate = CameraUpdateFactory.newLatLngZoom(LatLng(43.1, -87.9), 10F)
//            map.animateCamera(cameraUpdate)
//        }

        mapview.getMapAsync(OnMapReadyCallback() {
            it?.let {
                map = it
                val coordinates: LatLng = LatLng(32.509312, -92.119301)
                map.addMarker(MarkerOptions().position(coordinates).title("Maps"))
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinates, 10f))
                mapview.onResume()
            }
        })


        val backdropHeader = binding.backdropHeader
        val contentLayout = binding.contentLayout
        sheetBehavior = BottomSheetBehavior.from(contentLayout)
        sheetBehavior.isFitToContents = false
        sheetBehavior.isHideable = false
        sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

        backdropHeader.setOnClickListener { it ->
            toggleFilters()
        }
        return binding.root;
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        // TODO: Use the ViewModel
    }

    private fun toggleFilters() {
        if(sheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED){
            sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        } else {
            sheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    override fun onResume() {
        super.onResume()
        mapview.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapview.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapview.onDestroy()
    }
}