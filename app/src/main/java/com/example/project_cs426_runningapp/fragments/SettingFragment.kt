package com.example.project_cs426_runningapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.project_cs426_runningapp.R
import com.example.project_cs426_runningapp.databinding.FragmentSettingBinding

class SettingFragment : Fragment() {
    private lateinit var binding: FragmentSettingBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentSettingBinding.inflate(inflater, container, false)
        val clickListener = View.OnClickListener { v ->
            when (v) {
                binding.SettingEditBack -> {
                    findNavController().popBackStack()
                }
                binding.LogoutBtn -> {
                    findNavController().popBackStack(R.id.onboardingFragment,false)
                    Toast.makeText(requireContext(), "Log out successfully", Toast.LENGTH_SHORT).show()
                }
            }
        }
        binding.SettingEditBack.setOnClickListener(clickListener)
        binding.LogoutBtn.setOnClickListener(clickListener)
        return binding.root
    }
}