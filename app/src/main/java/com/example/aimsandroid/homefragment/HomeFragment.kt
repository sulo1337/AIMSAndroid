package com.example.aimsandroid.homefragment

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.location.LocationManager
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.lifecycle.Observer
import com.example.aimsandroid.R
import com.example.aimsandroid.databinding.FragmentHomeBinding
import kotlin.properties.Delegates

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

        //bind viewModel to the layout
        binding.homeViewModel = viewModel

        //enable live data to be bound from viewmodel to ui
        binding.lifecycleOwner = this

//        viewModel.locationChanged.observe(viewLifecycleOwner, Observer {
//            it?.let {
//                this.animateCoordinates(binding.latitude, viewModel.prevLatitude.value!!, viewModel.latitude.value!!)
//                this.animateCoordinates(binding.longitude, viewModel.prevLongitude.value!!, viewModel.latitude.value!!)
//                viewModel.doneOnLocationChanged()
//            }
//        })

        viewModel.latitude.observe(viewLifecycleOwner, Observer {
            it?.let{
                this.animateCoordinates(binding.latitude, viewModel.prevLatitude, it)
            }
        })

        viewModel.longitude.observe(viewLifecycleOwner, Observer {
            it?.let{
                this.animateCoordinates(binding.longitude, viewModel.prevLongitude, it)
            }
        })

        return binding.root
    }

    //defining value animator function
    private fun animateCoordinates(view: TextView, begin: Double, end: Double) {
        val valueAnimator: ValueAnimator = ValueAnimator.ofFloat(begin.toFloat(), end.toFloat())
        valueAnimator.duration =1000 //in millis
        valueAnimator.addUpdateListener{
            animation ->  view.text = String.format("%.15f", animation.animatedValue)
        }
//        valueAnimator.doOnStart {
//            view.setTextColor(Color.GREEN)
//        }
//        valueAnimator.doOnEnd {
//            view.setTextColor(Color.BLACK)
//        }
        valueAnimator.start()
    }
}