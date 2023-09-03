package com.example.project_cs426_runningapp

import android.content.Intent
import android.Manifest
import android.os.Build
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
import com.example.project_cs426_runningapp.databinding.FragmentLogInBinding
import com.example.project_cs426_runningapp.other.Constants.REQUEST_CODE_LOCATION_PERMISSION
import com.example.project_cs426_runningapp.other.TrackingUtility
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions

class HomeFragment : Fragment(), EasyPermissions.PermissionCallbacks {
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
        requestPermissions()
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
    private fun requestPermissions() {
        if(TrackingUtility.hasLocationPermissions(requireContext())) {
            return
        }
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            EasyPermissions.requestPermissions(
                this,
                "You need to accept location permissions to use this app.",
                REQUEST_CODE_LOCATION_PERMISSION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            EasyPermissions.requestPermissions(
                this,
                "You need to accept location permissions to use this app.",
                REQUEST_CODE_LOCATION_PERMISSION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {   }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if(EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        } else {
            requestPermissions()
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

}