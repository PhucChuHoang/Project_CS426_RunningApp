package com.example.project_cs426_runningapp

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.project_cs426_runningapp.databinding.FragmentHomeBinding
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        db = FirebaseFirestore.getInstance()
        val email = arguments?.getString("email")
        db.collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                binding.fullName.text = "Hello, " + documents.documents[0].get("fullname").toString()
            }
        val user = db.collection("users").document(email.toString())
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