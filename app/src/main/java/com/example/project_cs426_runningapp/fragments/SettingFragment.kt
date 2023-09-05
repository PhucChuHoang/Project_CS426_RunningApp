package com.example.project_cs426_runningapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
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
            }
        }
        binding.SettingEditBack.setOnClickListener(clickListener)

        return binding.root
    }
}