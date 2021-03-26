package com.example.aimsandroid.fragments.home

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.aimsandroid.databinding.FragmentHomeBinding
import com.example.aimsandroid.fragments.home.currenttrip.CurrentTripAdapter
import com.example.aimsandroid.fragments.trips.detaildialog.TripsDetailAdapter
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
        //initialize viewmodel and data binding
        binding = FragmentHomeBinding.inflate(inflater);
        val homeViewModelFactory = HomeViewModelFactory(requireActivity().application)
        viewModel = ViewModelProvider(this, homeViewModelFactory).get(HomeViewModel::class.java)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        //initialize map fragment
        mapFragmentView = MapFragmentView(requireActivity(), childFragmentManager, Runnable {
            mapFragmentView?.let {
                val viewModel = binding.viewModel
                //TODO remove map from view model as map is ui
                viewModel?.map = it.getMap()
            }
        })

        //drawer layout
        val backdropHeader = binding.backdropHeader
        val contentLayout = binding.contentLayout
        sheetBehavior = BottomSheetBehavior.from(contentLayout)
        sheetBehavior.isFitToContents = false
        sheetBehavior.isHideable = false
        sheetBehavior.isDraggable = false
        sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
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
                binding.bottomSheetTitle.text = "Tap here to see your current trip"
                sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                binding.currentTripTitle.text = "Trip #"+it.trip.tripId
                val adapter = CurrentTripAdapter(CurrentTripAdapter.CurrentTripClickListener {
                    Toast.makeText(requireActivity(), "Yet to be implemented...", Toast.LENGTH_SHORT).show()
                })
                adapter.submitList(it.waypoints)
                binding.currentTripRecyclerView.adapter = adapter
            }
        })

        return binding.root;
    }

    private fun toggleFilters() {
        if(sheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED){
            binding.bottomSheetTitle.text = "Tap here to see your current trip"
            sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        } else {
            binding.bottomSheetTitle.text = "Current Trip"
            sheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mapFragmentView?.onDestroy()
    }

    override fun onPause() {
        super.onPause()
        mapFragmentView?.onPause()
    }
}