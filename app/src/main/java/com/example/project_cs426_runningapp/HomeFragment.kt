package com.example.project_cs426_runningapp

import android.Manifest
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.util.Log
import com.bumptech.glide.Glide
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project_cs426_runningapp.databinding.FragmentHomeBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.example.project_cs426_runningapp.other.Constants.REQUEST_CODE_LOCATION_PERMISSION
import com.example.project_cs426_runningapp.other.TrackingUtility
import com.example.project_cs426_runningapp.run.Run
import com.example.project_cs426_runningapp.run.RunAdapter
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions

class HomeFragment : Fragment(), EasyPermissions.PermissionCallbacks {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var db: FirebaseFirestore
    private var name: String? = null
    var runArray: ArrayList<Run> = arrayListOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        db = FirebaseFirestore.getInstance()
        val sharedPreferences = requireActivity().getSharedPreferences("sharedPrefs", 0)
        name = sharedPreferences.getString("fullname", null)
        binding.fullName.text = "Hello, $name"
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.fullName.text = "Hello, $name"
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

        val sharedPreferences = requireActivity().getSharedPreferences("sharedPrefs", 0)
        var email = sharedPreferences.getString("email", null)

//        val runAdapter = RunAdapter(runArray)
//        val recyclerView: RecyclerView = binding.rvRuns
//        var layoutManager = LinearLayoutManager(requireContext())
//        recyclerView.layoutManager = layoutManager
//        recyclerView.adapter = runAdapter




            db.collection("aRun")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        val ID = document.data?.get("ID") as Long
                        var byteImgString = document.data?.get("byteImgString") as String
                        var timestamp = document.data?.get("timestamp") as Long
                        var avgSpeedInKMH = document.data?.get("avgSpeedInKMH") as Double
                        var distanceInMeters = document.data?.get("distanceInMeters") as Long
                        var timeInMillis = document.data?.get("timeInMillis") as Long
                        var caloriesBurned = document.data?.get("caloriesBurned") as Long
                        var email = document.data?.get("email") as String
                        var run = Run(ID,byteImgString,timestamp,avgSpeedInKMH,distanceInMeters,timeInMillis,caloriesBurned,email)
                        if (byteImgString != null) runArray.add(run)

                    }
                        val runAdapter = RunAdapter(runArray)
                        for (run in runArray)
                        {
                            Log.d("idInSetUP", run.ID.toString())
                            Log.d("emailInSetUP", run.email.toString())
                            Log.d("timestamp", run.timestamp.toString())
                        }
                        val recyclerView: RecyclerView = binding.rvRuns
                        var layoutManager = LinearLayoutManager(requireContext())
                        recyclerView.layoutManager = layoutManager
                        recyclerView.adapter = runAdapter
                    }

                .addOnFailureListener { exception ->
                    Log.w("TAG", "Error getting documents: ", exception)
                }


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