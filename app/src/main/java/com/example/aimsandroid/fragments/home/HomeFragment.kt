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
        sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        backdropHeader.setOnClickListener { it ->
            toggleFilters()
        }

        val adapter = TripsDetailAdapter(TripsDetailAdapter.TripsDetailClickListener{
            Toast.makeText(requireActivity(), "Not yet implemented...", Toast.LENGTH_SHORT).show()
        })

        viewModel.currentTrip.observe(viewLifecycleOwner, Observer {
            if(it == null) {
                Log.i("aims_debug", "null")
            } else {
                Log.i("aims_debug", it.toString())
            }
        })

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

    override fun onDestroy() {
        super.onDestroy()
        mapFragmentView?.onDestroy()
    }

    override fun onPause() {
        super.onPause()
        mapFragmentView?.onPause()
    }
}