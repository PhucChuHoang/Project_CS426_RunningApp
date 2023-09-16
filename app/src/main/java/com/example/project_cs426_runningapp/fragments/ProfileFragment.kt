package com.example.project_cs426_runningapp.fragments

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project_cs426_runningapp.adapters.EventAdapter
import com.example.project_cs426_runningapp.adapters.EventData
import com.example.project_cs426_runningapp.R
import com.example.project_cs426_runningapp.ViewModel.HomeViewModel
import com.example.project_cs426_runningapp.databinding.FragmentEventBinding
import com.example.project_cs426_runningapp.databinding.FragmentProfileBinding
import com.example.project_cs426_runningapp.other.TrackingUtility
import java.util.concurrent.TimeUnit

class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding
    private var name: String? = null

    private lateinit var db: FirebaseFirestore

    private var array: ArrayList<EventData> = arrayListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(inflater, container, false)

        val user_name = binding.profileUserName

        val sharedPreferences = requireActivity().getSharedPreferences("sharedPrefs", 0)
        name = sharedPreferences.getString("fullname", null)

        if(HomeViewModel.get().isNotBlank())
            user_name.text = "${HomeViewModel.get()}"
        else user_name.text = name

        val listView = binding.profileListEvent
        listView.layoutManager = LinearLayoutManager(requireContext())

        val adapter = EventAdapter(requireContext(), array)

        listView.adapter = adapter

        val clickListener = View.OnClickListener { v ->
            when (v) {
                binding.editProfileButton -> {
                    findNavController().navigate(R.id.action_profileFragment_to_editProfileFragment)
                }
                binding.profileSettingsButton -> {
                    findNavController().navigate(R.id.action_profileFragment_to_settingFragment)
                }
            }
        }

        binding.editProfileButton.setOnClickListener(clickListener)
        binding.profileSettingsButton.setOnClickListener(clickListener)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()

        val profile_img = binding.profileImage
        val localFilePath = File(requireContext().filesDir, "local_image.jpg").absolutePath
        val localFile = File(localFilePath)

        Glide.with(this)
            .load(localFile)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .placeholder(R.drawable.thang_ngot)
            .into(profile_img)

        val sharedPreferences2 = view.context.getSharedPreferences("sharedPrefs", 0)
        val email = sharedPreferences2.getString("email", null)

        val user_name = binding.profileUserName
        val events_array: ArrayList<EventData> = arrayListOf()
        if(HomeViewModel.get().isNotBlank())
            user_name.text = "${HomeViewModel.get()}"
        else user_name.text = name
        setUpUserRunInfo(view)
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val events = db.collection("events")
                    .whereEqualTo("status", 1)
                    .get()
                    .await()

                // Check if there are documents and update the UI
                if (!events.isEmpty) {
                    for (document in events) {
                        val event_name = document.data.get("event_name") as String
                        val image_url = document.data.get("image_url") as? String
                        val start_date = document.data.get("start_date") as? String
                        val end_date = document.data.get("end_date") as? String
                        Log.d("Image_url", document.data.get("image_url").toString())
                        // Update the UI on the main thread

                        db.collection("events")
                            .document(document.id)
                            .collection("participants")
                            .whereEqualTo("status", 1)
                            .get()
                            .addOnSuccessListener {documents2 ->
                                Log.d("Event id profile", "${document.id}")
                                 for (document2 in documents2) {
                                    if (document2.id == email) {
                                        events_array.add(
                                            EventData(
                                                event_name, true, image_url,
                                                start_date, end_date, document.id
                                            )
                                        )
                                        break
                                    }
                                }
                            }
                            .await()
                    }
                    launch(Dispatchers.Main) {
                        setUpEventAdapter(events_array, view)
                    }
                }

            } catch (e: Exception) {
                // Handle any exceptions that may occur during the Firestore operation
                e.printStackTrace()
            }
        }

        val profile_image = binding.profileImage

        profile_image.setOnClickListener {
            openGallery()
        }

        binding.saveProfileButton.setOnClickListener {
            saveProfilePic()

            binding.saveProfileButton.visibility = View.INVISIBLE
            binding.undoProfileButton.visibility = View.INVISIBLE
        }

        binding.undoProfileButton.setOnClickListener {
            val localFilePath = File(requireContext().filesDir, "local_image.jpg").absolutePath
            val localFile = File(localFilePath)

            Glide.with(this)
                .load(localFile)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .placeholder(R.drawable.thang_ngot)
                .into(profile_img)

            binding.saveProfileButton.visibility = View.INVISIBLE
            binding.undoProfileButton.visibility = View.INVISIBLE
        }
    }

    private val getContentLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val selectedImageUri = result.data?.data

                binding.saveProfileButton.visibility = View.VISIBLE
                binding.undoProfileButton.visibility = View.VISIBLE

                Picasso.with(requireView().context)
                    .load(selectedImageUri)
                    .fit()
                    .centerCrop()
                    .into(binding.profileImage)
            }
        }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore. Images.Media.EXTERNAL_CONTENT_URI)
        getContentLauncher.launch(intent)
    }

    private fun saveProfilePic() {
        val storage = Firebase.storage("gs://cs426-project.appspot.com")

        var storageRef = storage.reference

        val sharedPreferences = context?.getSharedPreferences("sharedPrefs", 0)
        var email = sharedPreferences?.getString("email", null)

        var profileRef = storageRef.child("images/" + email + "_profile.jpg")

        Toast.makeText(context, "Profile updated", Toast.LENGTH_SHORT).show()

        var imageView = binding.profileImage

        var bitmap = getBitmapFromView(imageView)

        val baos = ByteArrayOutputStream()

        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        var uploadTask = profileRef.putBytes(data)
        uploadTask.addOnFailureListener {
            // Handle unsuccessful uploads
        }.addOnSuccessListener { taskSnapshot ->
            // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
            // ...
            Log.d("Image upload", "It's up bro")
        }
    }

    fun getBitmapFromView(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(
            view.width, view.height, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    private fun setUpEventAdapter(events_array: ArrayList<EventData>, curView: View) {
        val listView = binding.profileListEvent

        val adapter = EventAdapter(curView.context, events_array, true)

        listView.adapter = adapter
    }
    private fun setUpUserRunInfo(view: View)
    {
        val sharedPrefID = requireActivity().getSharedPreferences("sharedPrefID", 0)
        var sumM = sharedPrefID.getLong("sumM", 0)
        var sumCaloriesBurned = sharedPrefID.getLong("sumKcal", 0)
        var sumTimeInMillis = sharedPrefID.getLong("sumHr", 0)

        val sumKM = binding.sumKM
        val sumHr = binding.sumHr
        val sumKcal = binding.sumKcal

        sumKM.text = "${sumM / 1000f}"
        sumKcal.text = "$sumCaloriesBurned"
        val hours = TimeUnit.MILLISECONDS.toHours(sumTimeInMillis)
        sumHr.text = "$hours"
    }
}