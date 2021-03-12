package com.example.aimsandroid.fragments.currenttrip

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.aimsandroid.R
import com.example.aimsandroid.databinding.FragmentCurrentTripBinding

class CurrentTripFragment : Fragment() {

    companion object {
        fun newInstance() = CurrentTripFragment()
    }

    private lateinit var viewModel: CurrentTripViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentCurrentTripBinding.inflate(inflater);
        val toolbarTitle = activity?.findViewById<TextView>(R.id.toolbarTitle) as TextView;
        toolbarTitle.setText(getString(R.string.current_trip_toolbar_title));
        return binding.root;
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(CurrentTripViewModel::class.java)
        // TODO: Use the ViewModel
    }
}