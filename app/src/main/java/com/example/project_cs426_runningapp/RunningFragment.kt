package com.example.project_cs426_runningapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.project_cs426_runningapp.databinding.FragmentRunningBinding
import com.google.android.gms.maps.GoogleMap


class RunningFragment : Fragment() {
    private lateinit var binding: FragmentRunningBinding
    private var map: GoogleMap? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentRunningBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync()
        {
            map = it
        }


    }

    override fun onResume() {
        super.onResume()
        binding.mapView?.onResume()
    }
    override fun onStart() {
        super.onStart()
        binding.mapView?.onStart()
    }
    override fun onStop() {
        super.onStop()
        binding.mapView?.onStop()
    }
    override fun onPause() {
        super.onPause()
        binding.mapView?.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView?.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView?.onSaveInstanceState(outState)
    }

}