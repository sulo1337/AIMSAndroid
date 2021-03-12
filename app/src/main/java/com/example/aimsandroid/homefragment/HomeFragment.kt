//package com.example.aimsandroid.homefragment
//
//import android.content.Context
//import android.os.Bundle
//import android.util.Log
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.view.inputmethod.InputMethodManager
//import android.widget.TextView
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import androidx.fragment.app.Fragment
//import androidx.lifecycle.Observer
//import androidx.lifecycle.ViewModelProvider
//import com.example.aimsandroid.R
//import com.example.aimsandroid.databinding.FragmentHomeBinding
//import com.example.aimsandroid.utils.NumberAnimatorUtil.NumberAnimator.animateCoordinates
//
//class HomeFragment : Fragment() {
//
//    companion object {
//        fun newInstance() = HomeFragment()
//    }
//
//    private lateinit var viewModel: HomeViewModel
//    private lateinit var binding: FragmentHomeBinding
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        //set action bar title
////        (activity as AppCompatActivity?)!!.supportActionBar!!.title = getString(R.string.homeFragmentToolbarTitle)
//
//        //obtain databinding
//        binding = FragmentHomeBinding.inflate(inflater)
//
//        //inject application to view model factory
//        val viewModelFactory: HomeViewModelFactory = HomeViewModelFactory(requireActivity().application)
//
//        //generate view model using the factory
//        viewModel = ViewModelProvider(this, viewModelFactory).get(HomeViewModel::class.java)
//
//        //bind viewModel to the layout
//        binding.homeViewModel = viewModel
//
//        //enable live data to be bound from viewmodel to ui
//        binding.lifecycleOwner = this
//
//        //creating recyclerview adapter
//        val adapter = ReviewsAdapter(requireActivity().application)
//
//        //assigning adapter to recycler view
//        binding.reviewItems.adapter = adapter
//
//        viewModel.latitude.observe(viewLifecycleOwner, Observer {
//            it?.let{
//                animateCoordinates(binding.latitude, viewModel.prevLatitude, it)
//            }
//        })
//
//        viewModel.longitude.observe(viewLifecycleOwner, Observer {
//            it?.let{
//                animateCoordinates(binding.longitude, viewModel.prevLongitude, it)
//            }
//        })
//
//        //restore saved form data (if any)
//        savedInstanceState?.let{
//            val savedEditText = savedInstanceState.getString("editText")
//            val savedRadioChoice = savedInstanceState.getString("radioChoice")
//            Log.i("here", "$savedEditText $savedRadioChoice")
//        }
//
//        /**
//         * On Click Listeners
//         * */
//
//        //submit button click listener
//        binding.submitButton.setOnClickListener {
//            it?.let {
//                val description = binding.description.text.toString()
//                val radioChoice = binding.atmosphere.checkedRadioButtonId
//                val atmosphere = when(radioChoice) {
//                    R.id.awesome -> "Awesome! \uD83D\uDE01"
//                    R.id.boring -> "Boring "
//                    else -> "None"
//                }
//                viewModel.onClickSubmit(description, atmosphere)
//                //reset form
//                binding.description.text = null
//                binding.atmosphere.clearCheck()
//                //hide keyboard
//                hideKeyboard(it)
//            }
//        }
//
//        return binding.root
//    }
//
//    private fun hideKeyboard(v: View) {
//        val imm: InputMethodManager = (activity as AppCompatActivity)!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//        imm.hideSoftInputFromWindow(v.windowToken, InputMethodManager.RESULT_UNCHANGED_SHOWN)
//    }
//
//    override fun onSaveInstanceState(outState: Bundle) {
//        super.onSaveInstanceState(outState)
//        Log.i("here", binding.description.text.toString())
//        Log.i("here", binding.atmosphere.checkedRadioButtonId.toString())
//        outState.putString("editText", binding.description.text.toString())
//        outState.putInt("radioChoice", binding.atmosphere.checkedRadioButtonId)
//    }
//
//    //save incomplete form data on pause
//    override fun onPause() {
//        super.onPause()
//        val prefs = requireContext().getSharedPreferences("com.example.aimsandroid", Context.MODE_PRIVATE)
//        prefs.edit().putString("descriptionFormData", binding.description.text.toString()).apply()
//        prefs.edit().putInt("radioChoiceFormData", binding.atmosphere.checkedRadioButtonId).apply()
//    }
//
//    //retrieve incomplete form data on resume
//    override fun onResume() {
//        super.onResume()
//        val prefs = requireContext().getSharedPreferences("com.example.aimsandroid", Context.MODE_PRIVATE)
//        val descriptionFormData = prefs.getString("descriptionFormData", "")
//        val radioChoiceFormData = prefs.getInt("radioChoiceFormData", -1)
//        binding.description.setText(descriptionFormData, TextView.BufferType.EDITABLE)
//        binding.atmosphere.check(radioChoiceFormData)
//    }
//}