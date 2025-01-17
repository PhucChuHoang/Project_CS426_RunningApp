package com.example.project_cs426_runningapp.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.project_cs426_runningapp.R
import com.example.project_cs426_runningapp.databinding.FragmentOnboardingBinding

class OnboardingFragment : Fragment() {
    private var _binding: FragmentOnboardingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnboardingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val clickListener = View.OnClickListener { v ->
            when (v) {
                binding.joinCommunityButton -> {
                    findNavController().navigate(R.id.action_onboardingFragment_to_registerFragment)
                }
                binding.loginButton -> {
                    findNavController().navigate(R.id.action_onboardingFragment_to_logInFragment)
                }
            }
        }
        binding.joinCommunityButton.setOnClickListener(clickListener)
        binding.loginButton.setOnClickListener(clickListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}