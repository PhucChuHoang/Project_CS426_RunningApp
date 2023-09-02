package com.example.project_cs426_runningapp

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.project_cs426_runningapp.databinding.FragmentHomeBinding
import com.example.project_cs426_runningapp.databinding.FragmentLogInBinding

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val clickListener = View.OnClickListener { v ->
            when (v) {
                binding.startCurrentLayout -> {
                    findNavController().navigate(R.id.action_homeFragment_to_runningFragment)
                }
            }
        }

        // Add a click listener to the profile_image ImageView
        binding.profileImage.setOnClickListener {
            // Start the EditProfileActivity
            val intent = Intent(requireContext(), EditProfileActivity::class.java)
            startActivity(intent)
        }

        // Add a click listener to the profile_image ImageView
        binding.setting.setOnClickListener {
            // Start the EditProfileActivity
            val intent = Intent(requireContext(), SettingActivity::class.java)
            startActivity(intent)
        }

        binding.startCurrentLayout.setOnClickListener(clickListener)
    }

}