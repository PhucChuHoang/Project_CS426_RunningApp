package com.example.project_cs426_runningapp.fragments

import android.Manifest
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.project_cs426_runningapp.R
import com.example.project_cs426_runningapp.ViewModel.HomeViewModel
import com.example.project_cs426_runningapp.databinding.FragmentHomeBinding
import com.example.project_cs426_runningapp.other.Constants.REQUEST_CODE_LOCATION_PERMISSION
import com.example.project_cs426_runningapp.other.TrackingUtility
import com.example.project_cs426_runningapp.run.Run
import com.example.project_cs426_runningapp.run.RunAdapter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import java.io.ByteArrayOutputStream
import java.io.File

class HomeFragment : Fragment(), EasyPermissions.PermissionCallbacks {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var db: FirebaseFirestore
    private var name: String? = null
    var runArray: ArrayList<Run> = arrayListOf()
    // Declare SharedPreferences as class properties
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var sharedPref: SharedPreferences
    private lateinit var sharedPrefID: SharedPreferences
    private lateinit var sharedPrefDetailRun: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        db = FirebaseFirestore.getInstance()


        // Initialize SharedPreferences
        sharedPreferences = requireActivity().getSharedPreferences("sharedPrefs", 0)
        sharedPref = requireActivity().getSharedPreferences("sharedPrefQ", 0)
        sharedPrefID = requireActivity().getSharedPreferences("sharedPrefID", 0)
        sharedPrefDetailRun = requireActivity().getSharedPreferences("sharedPrefDetailRun", 0)
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

        setProfileImage()
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

        val email = sharedPreferences.getString("email", null)
            db.collection("aRun")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        val ID = document.data.get("ID") as Long
                        val timestamp = document.data.get("timestamp") as Long
                        val avgSpeedInKMH = document.data.get("avgSpeedInKMH") as Double
                        val distanceInMeters = document.data.get("distanceInMeters") as Long
                        val timeInMillis = document.data.get("timeInMillis") as Long
                        val caloriesBurned = document.data.get("caloriesBurned") as Long
                        val emailInDoc = document.data.get("email") as String

                        val encodedImage = sharedPref.getString("encodedImage$ID", "DEFAULT")
                        if (encodedImage != "DEFAULT") {
                            val imageBytes = Base64.decode(encodedImage, Base64.DEFAULT)
                            val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                            val bmp = decodedImage
                            val run = Run(ID,bmp,timestamp,avgSpeedInKMH,distanceInMeters,timeInMillis,caloriesBurned,emailInDoc)
                            setAdapter(run,runArray)
                        }
                        else {
                            val storage = Firebase.storage("gs://cs426-project.appspot.com")
                            val storageRef = storage.reference
                            val runImageRef = storageRef.child("runs/" + emailInDoc + "_" + "$ID" + ".PNG")
                            val tmpID = ID.toInt()
                            if (emailInDoc == "quoclexx@gmail.com" && (tmpID == 1 || tmpID == 2 || tmpID == 3 || tmpID == 4 || tmpID == 5)) continue
                            if (timestamp == null || ID == null) continue
                            val ONE_MEGABYTE: Long = 1024 * 1024 * 5
                            runImageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener { byteArr ->
                                val bmp = toBitmap(byteArr)
                                val run = Run(ID,bmp,timestamp,avgSpeedInKMH,distanceInMeters,timeInMillis,caloriesBurned,emailInDoc)
                                setAdapter(run,runArray)
                            }
                                .addOnFailureListener {
                                }
                        }
                    }}

                .addOnFailureListener { exception ->
                }
    }
    private fun setAdapter(run : Run, runArray: ArrayList<Run>)
    {
        if (run.Img != null) runArray.add(run)
        runArray.sortByDescending { it.ID }
        var sumM : Long
        var sumKcal : Long
        var sumHr : Long
        val kmleft = binding.kmLeft
        val kmDone = binding.kmDone
        val progresss = binding.progress
        val level = binding.level
        sumM = 0
        sumKcal = 0
        sumHr = 0
        for (run in runArray)
        {
            sumM += run.distanceInMeters
            sumKcal += run.caloriesBurned
            sumHr += run.timeInMillis
        }
        val sumKM : Int
        sumKM = (sumM/1000f).toInt()
        if (sumKM > 100)
        {
            kmleft.text = "0 km left"
            kmDone.text = "$sumKM Km done"
            progresss.progress = 100
        }
        else
        {
            val valKmLeft = 10 - sumKM
            kmleft.text = "$valKmLeft km left"
            kmDone.text = "$sumKM Km done"
            progresss.progress = sumKM * 10
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
        val runAdapter = RunAdapter(runArray)

        val UserID = runArray[0].ID
        with(sharedPrefID.edit()) {
            putLong("UserID", UserID)
            putLong("sumM", sumM)
            putLong("sumKcal", sumKcal)
            putLong("sumHr", sumHr)
            apply()
        }

        val recyclerView: RecyclerView = binding.rvRuns
        val layoutManager = LinearLayoutManager(requireContext())
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = runAdapter
        runAdapter.setOnItemClickListener(object: RunAdapter.onItemClickListener{
            override fun onItemClick(position: Int) {
                val bitmap = runArray[position].Img
                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
                val encodedImage = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT)
                with(sharedPrefDetailRun.edit()) {
                    putLong("timeInMillis", runArray[position].timeInMillis)
                    putLong("caloriesBurned", runArray[position].caloriesBurned)
                    putLong("distanceInMeters", runArray[position].distanceInMeters)
                    putFloat("avgSpeedInKMH", runArray[position].avgSpeedInKMH.toFloat())
                    putString("img", encodedImage)
                    apply()
                }
                findNavController().navigate(R.id.action_homeFragment_to_detailRunningFragment)
            }

        })
    }
    private fun setProfileImage() {
        val localFilePath = File(requireContext().filesDir, "local_image.jpg").absolutePath

        val localFile = File(localFilePath)

        val profile_img = binding.profileImage

        Glide.with(this)
            .load(localFile)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .placeholder(R.drawable.thang_ngot)
            .into(profile_img)
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