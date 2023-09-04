package com.example.project_cs426_runningapp

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.project_cs426_runningapp.databinding.FragmentRunningBinding
import com.example.project_cs426_runningapp.other.Constants.ACTION_PAUSE_SERVICE
import com.example.project_cs426_runningapp.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.project_cs426_runningapp.other.Constants.ACTION_STOP_SERVICE
import com.example.project_cs426_runningapp.other.Constants.MAP_ZOOM
import com.example.project_cs426_runningapp.other.Constants.POLYLINE_COLOR
import com.example.project_cs426_runningapp.other.Constants.POLYLINE_WIDTH
import com.example.project_cs426_runningapp.other.TrackingUtility
import com.example.project_cs426_runningapp.run.Run
import com.example.project_cs426_runningapp.run.RunAdapter
import com.example.project_cs426_runningapp.services.Polyline
import com.example.project_cs426_runningapp.services.TrackingService
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.lang.Math.round
import java.util.*


class RunningFragment : Fragment() {
    private lateinit var binding: FragmentRunningBinding

    private var isTracking = false
    private var pathPoints = mutableListOf<Polyline>()

    private var map: GoogleMap? = null
    private var weight = 80
    private var curTimeInMillis = 0L


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


        val clickListener = View.OnClickListener { v ->
            when (v) {
                binding.btnToggleRun -> {
                    toggleRun()
                }
                binding.btnFinishRun -> {
                    zoomToSeeWholeTrack()
                    endRunAndSaveToDb()
                }
            }
        }
        binding.btnToggleRun.setOnClickListener(clickListener)
        binding.btnFinishRun.setOnClickListener(clickListener)


        binding.mapView.getMapAsync()
        {
            map = it
            addAllPolylines()
        }
        subscribeToObservers()

    }


    private fun subscribeToObservers() {
        TrackingService.isTracking.observe(viewLifecycleOwner, Observer {
            updateTracking(it)
        })

        TrackingService.pathPoints.observe(viewLifecycleOwner, Observer {
            pathPoints = it
            addLatestPolyline()
            moveCameraToUser()
        })
        TrackingService.timeRunInMillis.observe(viewLifecycleOwner, Observer {
            curTimeInMillis = it
            val formattedTime = TrackingUtility.getFormattedStopWatchTime(curTimeInMillis, true)
            binding.tvTimer.text = formattedTime
        })
    }

    private fun toggleRun() {
        if(isTracking) {
            sendCommandToService(ACTION_PAUSE_SERVICE)
        } else {
            sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
        }
    }

    private fun updateTracking(isTracking: Boolean) {
        this.isTracking = isTracking
        if(!isTracking) {
            binding.btnToggleRun.setImageResource(R.drawable.start_button)
            binding.btnFinishRun.visibility = View.VISIBLE
        } else {
            binding.btnToggleRun.setImageResource(R.drawable.pause_button)
            binding.btnFinishRun.visibility = View.GONE
        }
    }

    private fun moveCameraToUser() {
        if(pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty()) {
            map?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    pathPoints.last().last(),
                    MAP_ZOOM
                )
            )
        }
    }

    private fun zoomToSeeWholeTrack() {
        val bounds = LatLngBounds.Builder()
        for(polyline in pathPoints) {
            for(pos in polyline) {
                bounds.include(pos)
            }
        }

        map?.moveCamera(
            CameraUpdateFactory.newLatLngBounds(
                bounds.build(),
                binding.mapView.width,
                binding.mapView.height,
                (binding.mapView.height * 0.05f).toInt()
            )
        )
    }



    private fun fromBitmap(bmp: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        return String(outputStream.toByteArray())
    }

    private fun endRunAndSaveToDb() {
        map?.snapshot { bmp ->
            var distanceInMeters : Long
            distanceInMeters = 0
            for(polyline in pathPoints) {
                distanceInMeters += TrackingUtility.calculatePolylineLength(polyline).toLong()
            }
            val avgSpeed = round((distanceInMeters / 1000.0) / (curTimeInMillis / 1000.0 / 60 / 60) * 10) / 10.0
            val dateTimestamp = Calendar.getInstance().timeInMillis
            val caloriesBurned = ((distanceInMeters / 1000f) * weight).toLong()
            var email: String? = null
            val sharedPreferences = requireActivity().getSharedPreferences("sharedPrefs", 0)
            email = sharedPreferences.getString("email", null)
            var convertedBmp = fromBitmap(bmp)
            var db = FirebaseFirestore.getInstance()
            var count : Long
            count  = 1
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                try {
                    var aRun = db.collection("aRun")
                        .whereEqualTo("email", email)
                        .get()
                        .await()

                    val totalEvent = aRun.size()
                    count = (totalEvent+1).toLong()
                    val run = Run(count,convertedBmp, dateTimestamp, avgSpeed, distanceInMeters, curTimeInMillis, caloriesBurned,email)
                    Log.d("countRun", count.toString())
                    run.saveRun()
                    Snackbar.make(
                        requireActivity().findViewById(android.R.id.content),
                        "Run saved successfully",
                        Snackbar.LENGTH_LONG
                    ).show()
                    stopRun()
                    // Check if there are documents and update the UI
                } catch (e: Exception) {
                    // Handle any exceptions that may occur during the Firestore operation
                    e.printStackTrace()
                }
            }

        }
    }

    private fun stopRun() {
        sendCommandToService(ACTION_STOP_SERVICE)
        findNavController().navigate(R.id.action_runningFragment_to_homeFragment)
    }
    private fun addAllPolylines() {
        for(polyline in pathPoints) {
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .addAll(polyline)
            map?.addPolyline(polylineOptions)
        }
    }

    private fun addLatestPolyline() {
        if(pathPoints.isNotEmpty() && pathPoints.last().size > 1) {
            val preLastLatLng = pathPoints.last()[pathPoints.last().size - 2]
            val lastLatLng = pathPoints.last().last()
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .add(preLastLatLng)
                .add(lastLatLng)
            map?.addPolyline(polylineOptions)
        }
    }


    private fun sendCommandToService(action: String) =
        Intent(requireContext(), TrackingService::class.java).also {
            it.action = action
            requireContext().startService(it)
            Log.d("sendCommandToService1","sendCommandToService1")
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