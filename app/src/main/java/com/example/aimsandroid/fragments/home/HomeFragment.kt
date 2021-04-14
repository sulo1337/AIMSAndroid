package com.example.aimsandroid.fragments.home

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.aimsandroid.databinding.FragmentHomeBinding
import com.example.aimsandroid.fragments.home.currenttrip.CurrentTripAdapter
import com.example.aimsandroid.fragments.home.currenttrip.WaypointDetailDialog
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.here.android.mpa.common.GeoCoordinate
import com.here.android.mpa.mapping.Map
import putDouble


class HomeFragment : Fragment() {
    private lateinit var viewModel: HomeViewModel
    private lateinit var sheetBehavior: BottomSheetBehavior<LinearLayout>
    private lateinit var binding: FragmentHomeBinding
    private var mapFragmentView: MapFragmentView? = null
    private lateinit var prefs: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //initialize viewmodel and data binding
        binding = FragmentHomeBinding.inflate(inflater);
        val homeViewModelFactory = HomeViewModelFactory(requireActivity().application)
        viewModel = ViewModelProvider(this, homeViewModelFactory).get(HomeViewModel::class.java)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        prefs = requireActivity().applicationContext.getSharedPreferences("com.example.aimsandroid", Context.MODE_PRIVATE)
        return binding.root;
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        hideNavFab()
        hideGpsFab()
        binding.navFab.setOnClickListener {
            resumeRoadView()
        }

        //drawer layout
        val backdropHeader = binding.backdropHeader
        val contentLayout = binding.contentLayout
        sheetBehavior = BottomSheetBehavior.from(contentLayout)
        sheetBehavior.isFitToContents = false
        sheetBehavior.isHideable = false
        sheetBehavior.isDraggable = false

        backdropHeader.setOnClickListener { it ->
            if(viewModel.currentTrip.value != null) {
                toggleFilters()
            }
        }

        viewModel.currentTrip.observe(viewLifecycleOwner, Observer {
            if(it == null) {
                binding.currentTripRecyclerView.visibility = View.GONE
                binding.currentTripTitle.text = "You have not started any trips"
                binding.bottomSheetTitle.text = "You have not started any trips"
            } else {
                binding.currentTripRecyclerView.visibility = View.VISIBLE
                binding.bottomSheetTitle.text = "Current Trip"
                binding.currentTripTitle.text = "Trip #"+it.trip.tripId

                val clickListeners = CurrentTripAdapter.CurrentTripClickListener(
                    detailsClickListener = {
                        val dialog: WaypointDetailDialog = WaypointDetailDialog.newInstance(it)
                        dialog.show(requireActivity().supportFragmentManager, "wayPointDetailDialogCurrentTrip")
                    },
                    navigateClickListener = {
                        val startPoint = GeoCoordinate(viewModel.latitude.value!!, viewModel.longitude.value!!)
                        val destination = GeoCoordinate(it.latitude, it.longitude)
                        startNavigationMode()
                        prefs.edit().putDouble("lastNavigatedLatitude", it.latitude).apply()
                        prefs.edit().putDouble("lastNavigatedLongitude", it.longitude).apply()
                        sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                        mapFragmentView?.navigate(startPoint, destination)
                    })

                val adapter = CurrentTripAdapter(clickListeners)
                adapter.submitList(it.waypoints)
                binding.currentTripRecyclerView.adapter = adapter
            }
        })

        //check if args is passed to expand or collapse drawer layout
        arguments?.let {
            val fromStartTrip = HomeFragmentArgs.fromBundle(it).tripStarted
            if(fromStartTrip){
                sheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            } else {
                sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }
        }
    }

    private fun toggleFilters() {
        if(sheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED){
            sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        } else {
            sheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    fun onNavigationEnded() {
        mapFragmentView?.onNavigationEnded()
    }

    fun onDestinationReached() {
        stopNavigationMode()
        mapFragmentView?.onNavigationEnded()
    }

    fun startNavigationMode() {
        prefs.edit().putBoolean("navigating", true).apply()
        hideGpsFab()
    }

    fun stopNavigationMode() {
        prefs.edit().putBoolean("navigating", false).apply()
        viewGpsFab()
        hideNavFab()
    }

    override fun onPause() {
        super.onPause()
        mapFragmentView?.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapFragmentView?.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        mapFragmentView = MapFragmentView(this, binding.viewModel!!)
    }

    fun viewNavFab() {
        requireActivity().runOnUiThread {
            binding.navFab.visibility = View.VISIBLE
        }
    }

    fun viewGpsFab() {
        binding.gpsFab.visibility = View.VISIBLE
    }

    fun hideNavFab() {
        binding.navFab.visibility = View.GONE
    }

    fun hideGpsFab() {
        binding.gpsFab.visibility = View.GONE
    }

    fun pauseRoadView(){
        if(prefs.getBoolean("navigating", false)){
            mapFragmentView?.pauseRoadView()
            viewNavFab()
        }
    }

    fun resumeRoadView() {
        if(prefs.getBoolean("navigating", false)){
            mapFragmentView?.resumeRoadView()
            hideNavFab()
        }
    }
}