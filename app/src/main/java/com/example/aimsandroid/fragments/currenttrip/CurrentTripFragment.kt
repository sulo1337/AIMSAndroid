package com.example.aimsandroid.fragments.currenttrip

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.aimsandroid.MainActivity
import com.example.aimsandroid.R
import com.example.aimsandroid.databinding.FragmentCurrentTripBinding
import com.example.aimsandroid.databinding.FragmentHomeBinding
import com.example.aimsandroid.fragments.home.HomeFragment
import com.example.aimsandroid.fragments.profile.ProfileViewModel
import com.example.aimsandroid.utils.OnBottomSheetCallbacks
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class CurrentTripFragment : Fragment() {

    private lateinit var viewModel: CurrentTripViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding = FragmentCurrentTripBinding.inflate(inflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(CurrentTripViewModel::class.java)
        // TODO: Use the ViewModel
    }


}