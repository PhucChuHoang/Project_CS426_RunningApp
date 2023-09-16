package com.example.project_cs426_runningapp.fragments

import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.project_cs426_runningapp.R
import com.example.project_cs426_runningapp.databinding.FragmentDetailRunningBinding
import com.example.project_cs426_runningapp.other.TrackingUtility

class detailRunningFragment : Fragment() {
    private var _binding: FragmentDetailRunningBinding? = null
    private val binding get() = _binding!!
    private lateinit var sharedPrefDetailRun: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailRunningBinding.inflate(inflater, container, false)
        sharedPrefDetailRun = requireActivity().getSharedPreferences("sharedPrefDetailRun", 0)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val detailImg = binding.detailImg
        val tvTimer  = binding.tvTimer
        val run_km = binding.runKm
        val run_kcal = binding.runKcal
        val run_velo = binding.runVelo
        val encodedImage = sharedPrefDetailRun.getString("img", "DEFAULT")
        val time = sharedPrefDetailRun.getLong("timeInMillis", 0)
        val kcal = sharedPrefDetailRun.getLong("caloriesBurned", 0)
        val distance = sharedPrefDetailRun.getLong("distanceInMeters", 0)
        val avgSpeed = sharedPrefDetailRun.getFloat("avgSpeedInKMH", 0f)
        if (encodedImage != "DEFAULT") {
            val imageBytes = Base64.decode(encodedImage, Base64.DEFAULT)
            val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            Glide.with(this).load(decodedImage).into(detailImg)

            val avgSpeedS = "${avgSpeed} km/h"
            run_velo.text = avgSpeedS

            val distanceInKm = "${distance / 1000f} km"
            run_km.text = distanceInKm

            tvTimer.text = TrackingUtility.getFormattedStopWatchTime(time)

            val caloriesBurned = "${kcal} kcal"
            run_kcal.text = caloriesBurned

        }
        val clickListener = View.OnClickListener { v ->
            when (v) {
                binding.returnButton -> {
                    findNavController().navigate(R.id.action_detailRunningFragment_to_homeFragment)
                }
            }
        }
        binding.returnButton.setOnClickListener(clickListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}