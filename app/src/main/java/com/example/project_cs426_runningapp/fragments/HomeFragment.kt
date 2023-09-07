package com.example.project_cs426_runningapp.fragments

import android.Manifest
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.util.Log
import com.bumptech.glide.Glide
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project_cs426_runningapp.R
import com.example.project_cs426_runningapp.databinding.FragmentHomeBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.example.project_cs426_runningapp.other.Constants.REQUEST_CODE_LOCATION_PERMISSION
import com.example.project_cs426_runningapp.other.TrackingUtility
import com.example.project_cs426_runningapp.run.Run
import com.example.project_cs426_runningapp.run.RunAdapter
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import com.example.project_cs426_runningapp.ViewModel.HomeViewModel
import java.io.ByteArrayOutputStream

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
        if(HomeViewModel.get().isNotBlank())
            binding.fullName.text = "Hello, ${HomeViewModel.get()}"
        else binding.fullName.text = "Hello, $name"
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.fullName.text = "Hello, $name"
        requestPermissions()
        if(HomeViewModel.get().isNotBlank())
            binding.fullName.text = "Hello, ${HomeViewModel.get()}"
        else binding.fullName.text = "Hello, $name"
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
        Log.d("email",email.toString())
            db.collection("aRun")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        val ID = document.data?.get("ID") as Long
                        Log.d("IDget", "$ID")
                        var timestamp = document.data?.get("timestamp") as Long
                        var avgSpeedInKMH = document.data?.get("avgSpeedInKMH") as Double
                        var distanceInMeters = document.data?.get("distanceInMeters") as Long
                        var timeInMillis = document.data?.get("timeInMillis") as Long
                        var caloriesBurned = document.data?.get("caloriesBurned") as Long
                        var emailInDoc = document.data?.get("email") as String

                        val sharedPref = requireActivity().getSharedPreferences("sharedPrefQ", 0)
                        val encodedImage = sharedPref.getString("encodedImage$ID", "DEFAULT")
                        if (encodedImage != "DEFAULT") {
                            val imageBytes = Base64.decode(encodedImage, Base64.DEFAULT)
                            val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

                            var bmp = decodedImage
                            var run = Run(ID,bmp,timestamp,avgSpeedInKMH,distanceInMeters,timeInMillis,caloriesBurned,emailInDoc)
                            if (bmp != null) runArray.add(run)
                            runArray.sortByDescending { it.ID }
                            var sumM : Long
                            var kmleft = binding.kmLeft
                            var kmDone = binding.kmDone
                            var progresss = binding.progress
                            var level = binding.level
                            sumM = 0
                            for (run in runArray)
                            {
                                sumM += run.distanceInMeters
                            }
                            var sumKM : Int
                            sumKM = (sumM/1000f).toInt()
                            if (sumKM > 100)
                            {
                                kmleft.text = "0 km left"
                                kmDone.text = "$sumKM Km done"
                                progresss.progress = 100
                            }
                            else
                            {
                                var valKmLeft = 10 - sumKM
                                kmleft.text = "$valKmLeft km left"
                                kmDone.text = "$sumKM Km done"
                                progresss.progress = sumKM.toInt()
                            }
                            if (sumKM < 1)
                            {
                                level.text = "beginner"
                            }
                            else if (sumKM < 3)
                            {
                                level.text = "Specialist"
                            }
                            else level.text = "Expert"
                            Log.d("AdapterOnfail", "encodeed is not null, Start Adapter!")
                            val runAdapter = RunAdapter(runArray)

                            val sharedPrefID = requireActivity().getSharedPreferences("sharedPrefID", 0)
                            val UserID = runArray[0].ID
                            with(sharedPrefID.edit()) {
                                putLong("UserID", UserID)
                                apply()
                            }
                            Log.d("UserIDInHome","$UserID")
                            val recyclerView: RecyclerView = binding.rvRuns
                            var layoutManager = LinearLayoutManager(requireContext())
                            recyclerView.layoutManager = layoutManager
                            recyclerView.adapter = runAdapter
                        }
                        else {
                            val storage = Firebase.storage("gs://cs426-project.appspot.com")
                            var storageRef = storage.reference
                            var runImageRef = storageRef.child("runs/" + emailInDoc + "_" + "$ID" + ".PNG")
                            var tmpID = ID.toInt()
                            if (emailInDoc == "quoclexx@gmail.com" && (tmpID == 1 || tmpID == 2 || tmpID == 3 || tmpID == 4 || tmpID == 5)) continue
                            if (timestamp == null || ID == null) continue
                            val ONE_MEGABYTE: Long = 1024 * 1024 * 5
                            runImageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener { byteArr ->
                                var bmp = toBitmap(byteArr)
                                var run = Run(ID,bmp,timestamp,avgSpeedInKMH,distanceInMeters,timeInMillis,caloriesBurned,emailInDoc)
                                if (bmp != null) runArray.add(run)

                                runArray.sortByDescending { it.ID }
                                var sumM : Long
                                var kmleft = binding.kmLeft
                                var kmDone = binding.kmDone
                                var progresss = binding.progress
                                var level = binding.level
                                sumM = 0
                                for (run in runArray)
                                {
                                    sumM += run.distanceInMeters
                                }
                                var sumKM : Int
                                sumKM = (sumM/1000f).toInt()
                                if (sumKM > 100)
                                {
                                    kmleft.text = "0 km left"
                                    kmDone.text = "$sumKM Km done"
                                    progresss.progress = 100
                                }
                                else
                                {
                                    var valKmLeft = 10 - sumKM
                                    kmleft.text = "$valKmLeft km left"
                                    kmDone.text = "$sumKM Km done"
                                    progresss.progress = sumKM.toInt()
                                }
                                if (sumKM < 1)
                                {
                                    level.text = "beginner"
                                }
                                else if (sumKM < 3)
                                {
                                    level.text = "Specialist"
                                }
                                else level.text = "Expert"
                                Log.d("AdapterOnSuccess", "encodeed is not null, Start Adapter!")
                                val sharedPrefID = requireActivity().getSharedPreferences("sharedPrefID", 0)
                                val UserID = runArray[0].ID
                                with(sharedPrefID.edit()) {
                                    putLong("UserID", UserID)
                                    apply()
                                }
                                Log.d("UserIDInHome","$UserID")
                                val runAdapter = RunAdapter(runArray)
                                val recyclerView: RecyclerView = binding.rvRuns
                                var layoutManager = LinearLayoutManager(requireContext())
                                recyclerView.layoutManager = layoutManager
                                recyclerView.adapter = runAdapter
                                // Data for "images/island.jpg" is returned, use this as needed
                            }

                                .addOnFailureListener {

                                }
                        }
                    }
                    }

                .addOnFailureListener { exception ->
                    Log.w("TAG", "Error getting documents: ", exception)
                }

    }

    private fun toBitmap(bytes: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
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