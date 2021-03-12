package com.example.aimsandroid.fragments.trips

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.aimsandroid.R
import com.example.aimsandroid.databinding.FragmentTripsBinding

class TripsFragment : Fragment() {

    companion object {
        fun newInstance() = TripsFragment()
    }

    private lateinit var viewModel: TripsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentTripsBinding.inflate(inflater);
        val fragmentTitle = binding.fragmentTitle
        fragmentTitle.setText(getString(R.string.trips_toolbar_title));
        return binding.root;
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(TripsViewModel::class.java)
        // TODO: Use the ViewModel
    }
}