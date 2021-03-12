package com.example.aimsandroid.fragments.home

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import com.example.aimsandroid.R
import com.example.aimsandroid.databinding.FragmentHomeBinding
import com.example.aimsandroid.utils.OnBottomSheetCallbacks
import com.google.android.material.bottomsheet.BottomSheetBehavior

class HomeFragment : Fragment() {

    companion object {
        fun newInstance() = HomeFragment()
    }

    private lateinit var viewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentHomeBinding.inflate(inflater);
        val toolbarTitle = activity?.findViewById<TextView>(R.id.toolbarTitle) as TextView;
        toolbarTitle.setText(getString(R.string.current_trip_toolbar_title));
        return binding.root;
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        // TODO: Use the ViewModel
    }
}