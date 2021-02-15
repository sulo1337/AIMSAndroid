package com.example.aimsandroid.homefragment

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
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

        //inject application to view model factory
        val viewModelFactory: HomeViewModelFactory = HomeViewModelFactory(requireActivity().application)

        //generate view model using the factory
        viewModel = ViewModelProvider(this, viewModelFactory).get(HomeViewModel::class.java)

        //bind viewModel to the layout
        binding.homeViewModel = viewModel

        //enable live data to be bound from viewmodel to ui
        binding.lifecycleOwner = this

        //creating recyclerview adapter
        val adapter = ReviewsAdapter(requireActivity().application)

        //assigning adapter to recycler view
        binding.reviewItems.adapter = adapter

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

        /**
         * On Click Listeners
         * */

        //submit button click listener
        binding.submitButton.setOnClickListener {
            it?.let {
                val description = binding.description.text.toString()
                val radioChoice = binding.atmosphere.checkedRadioButtonId
                val atmosphere = when(radioChoice) {
                    R.id.awesome -> "Awesome! \uD83D\uDE01"
                    R.id.boring -> "Boring "
                    else -> "None"
                }
                viewModel.onClickSubmit(description, atmosphere)
                //reset form
                binding.description.text = null
                binding.atmosphere.clearCheck()
                //hide keyboard
                hideKeyboard(it)
            }
        }

        return binding.root
    }

    //defining value animator function
    private fun animateCoordinates(view: TextView, begin: Double, end: Double) {
        val valueAnimator: ValueAnimator = ValueAnimator.ofFloat(begin.toFloat(), end.toFloat())
        valueAnimator.duration =1000 //in millis
        valueAnimator.addUpdateListener{
            animation ->  view.text = String.format("%.08f", animation.animatedValue)
        }
//        valueAnimator.doOnStart {
//            view.setTextColor(Color.GREEN)
//        }
//        valueAnimator.doOnEnd {
//            view.setTextColor(Color.BLACK)
//        }
        valueAnimator.start()
    }

    private fun hideKeyboard(v: View) {
        val imm: InputMethodManager = (activity as AppCompatActivity)!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(v.windowToken, InputMethodManager.RESULT_UNCHANGED_SHOWN)
    }
}