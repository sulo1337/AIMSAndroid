package com.example.aimsandroid.homefragment

import android.content.Context
import android.location.LocationManager
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.example.aimsandroid.R
import com.example.aimsandroid.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    companion object {
        fun newInstance() = HomeFragment()
    }

    private lateinit var viewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //set action bar title
        (activity as AppCompatActivity?)!!.supportActionBar!!.title = getString(R.string.homeFragmentToolbarTitle)

        //obtain databinding
        val binding = FragmentHomeBinding.inflate(inflater)

        //obtain location manager
        val locationManager: LocationManager = (activity as AppCompatActivity?)!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        //inject location manager to view model factory
        val viewModelFactory: HomeViewModelFactory = HomeViewModelFactory(locationManager)

        //generate view model using the factory
        viewModel = ViewModelProvider(this, viewModelFactory).get(HomeViewModel::class.java)

        //enable live data to be bound from viewmodel to ui
        binding.lifecycleOwner = this

        return binding.root
    }

}