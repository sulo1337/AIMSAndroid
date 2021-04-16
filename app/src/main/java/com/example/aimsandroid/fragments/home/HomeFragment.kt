package com.example.aimsandroid.fragments.home

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.aimsandroid.R
import com.example.aimsandroid.databinding.FragmentHomeBinding
import com.example.aimsandroid.fragments.home.currenttrip.CurrentTripAdapter
import com.example.aimsandroid.fragments.home.currenttrip.dialogs.WaypointDetailDialog
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.here.android.mpa.common.GeoBoundingBox
import com.here.android.mpa.common.GeoCoordinate
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
        resetButtons()
        hideGpsFab()
        binding.navFab.setOnClickListener {
            resumeRoadView()
        }

        binding.startNavFab.setOnClickListener {
            AlertDialog.Builder(requireActivity(), R.style.AlertDialogTheme)
            .setTitle("Are you sure?")
            .setMessage("Do you want to start navigation?")
            .setNegativeButton(
                "No"
            ) { dialog, which ->
                dialog.cancel()
            }
            .setPositiveButton(
                "Yes"
            ) { dialog, which ->
                startNavigationMode()
            }.create().show()
        }

        binding.stopNavFab.setOnClickListener {
            AlertDialog.Builder(requireActivity(), R.style.AlertDialogTheme)
                .setTitle("Are you sure?")
                .setMessage("Do you want to stop navigation?")
                .setNegativeButton(
                    "No"
                ) { dialog, which ->
                    dialog.cancel()
                }
                .setPositiveButton(
                    "Yes"
                ) { dialog, which ->
                    stopNavigationMode()
                }.create().show()
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
                        val dialog = WaypointDetailDialog.newInstance(it)
                        dialog.show(childFragmentManager, "wayPointDetailDialogCurrentTrip")
                    },
                    navigateClickListener = {
                        val destination = GeoCoordinate(it.latitude, it.longitude)
                        showDirections(destination)
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

    fun showDirections(destination: GeoCoordinate) {
        val startPoint = GeoCoordinate(viewModel.latitude.value!!, viewModel.longitude.value!!)
        stopNavigationMode()
        prefs.edit().putDouble("lastNavigatedLatitude", destination.latitude).apply()
        prefs.edit().putDouble("lastNavigatedLongitude", destination.longitude).apply()
        sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        mapFragmentView?.showDirections(startPoint, destination)
    }

    private fun toggleFilters() {
        if(sheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED){
            sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        } else {
            sheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    fun afterRouteCalculated(boundingBox: GeoBoundingBox?) {
        if(prefs.getBoolean("navigating", false)){
            continueNavigationMode()
        } else {
            mapFragmentView?.zoomTo(boundingBox)
            viewStartNavFab()
            hideStopNavFab()
            hideGpsFab()
            hideNavFab()
        }
    }

    fun endNavigationOnly() {
        mapFragmentView?.onNavigationEnded()
    }

    fun onDestinationReached() {
        stopNavigationMode()
    }

    fun startNavigationMode() {
        prefs.edit().putBoolean("navigating", true).apply()
        mapFragmentView?.startNavigation()
        viewStopNavFab()
        hideStartNavFab()
        hideGpsFab()
        hideNavFab()
    }

    fun continueNavigationMode() {
        hideStartNavFab()
        hideGpsFab()
        hideNavFab()
        viewStopNavFab()
        mapFragmentView?.startNavigation()
    }

    fun stopNavigationMode() {
        prefs.edit().putBoolean("navigating", false).apply()
        mapFragmentView?.onNavigationEnded()
        viewGpsFab()
        hideNavFab()
        hideStartNavFab()
        hideStopNavFab()
    }

    override fun onPause() {
        super.onPause()
        Log.i("aimsDebug", "onPause called")
        mapFragmentView?.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i("aimsDebug", "onDestroy called")
        mapFragmentView?.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        Log.i("aimsDebug", "onResumeCalled")
        mapFragmentView = MapFragmentView.getInstance(this, binding.viewModel!!)
        mapFragmentView!!.initMapFragment()
    }

    fun viewNavFab() {
        requireActivity().runOnUiThread {
            binding.navFab.visibility = View.VISIBLE
        }
    }

    fun viewGpsFab() {
        binding.gpsFab.visibility = View.VISIBLE
    }

    fun viewStartNavFab() {
        binding.startNavFab.visibility = View.VISIBLE
    }

    fun viewStopNavFab() {
        binding.stopNavFab.visibility = View.VISIBLE
    }

    fun hideNavFab() {
        binding.navFab.visibility = View.GONE
    }

    fun hideGpsFab() {
        binding.gpsFab.visibility = View.GONE
    }

    fun hideStartNavFab() {
        binding.startNavFab.visibility = View.GONE
    }

    fun hideStopNavFab() {
        binding.stopNavFab.visibility = View.GONE
    }

    fun resetButtons() {
        viewGpsFab()
        hideNavFab()
        hideStartNavFab()
        hideStopNavFab()
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