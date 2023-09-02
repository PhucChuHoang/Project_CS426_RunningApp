package com.example.project_cs426_runningapp

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.project_cs426_runningapp.databinding.FragmentHomeBinding
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import android.util.Log

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

        // Use Kotlin Coroutines to perform Firestore operation asynchronously
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val querySnapshot = db.collection("users")
                    .whereEqualTo("email", email)
                    .get()
                    .await()

                // Check if there are documents and update the UI
                if (!querySnapshot.isEmpty) {
                    Log.d("TAG", "CO TENNNNNN")
                    val document = querySnapshot.documents[0]
                    val fullName = document.data?.get("fullname") as? String
                    fullName?.let {
                        // Update the UI on the main thread
                        launch(Dispatchers.Main) {
                            binding.fullName.text = "Hello, $it"
                            Log.d("TAG", "Hello, $it")
                        }
                    }
                }
                else Log.d("TAG", "HONG CO TEN")
            } catch (e: Exception) {
                // Handle any exceptions that may occur during the Firestore operation
                e.printStackTrace()
            }
        }

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val clickListener = View.OnClickListener { v ->
            when (v) {
                binding.startCurrentLayout -> {
                    findNavController().navigate(R.id.action_homeFragment_to_runningFragment)
                }
                binding.profileImage -> {
                    findNavController().navigate(R.id.action_homeFragment_to_editProfileFragment)
                }
                binding.setting -> {
                    findNavController().navigate(R.id.action_homeFragment_to_settingFragment)
                }
            }
        }

        binding.startCurrentLayout.setOnClickListener(clickListener)
        binding.profileImage.setOnClickListener(clickListener)
        binding.setting.setOnClickListener(clickListener)
    }

}