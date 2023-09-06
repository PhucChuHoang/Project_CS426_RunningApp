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
import com.example.project_cs426_runningapp.adapters.EventAdapter
import com.example.project_cs426_runningapp.adapters.EventData
import com.example.project_cs426_runningapp.R
import com.example.project_cs426_runningapp.ViewModel.HomeViewModel

class ProfileFragment : Fragment() {
    private var name: String? = null

    private lateinit var db: FirebaseFirestore

    private var array: ArrayList<EventData> = arrayListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val curView = inflater.inflate(R.layout.fragment_profile, container, false)

        val user_name = curView.findViewById<TextView>(R.id.profile_user_name)

        val sharedPreferences = requireActivity().getSharedPreferences("sharedPrefs", 0)
        name = sharedPreferences.getString("fullname", null)

        if(HomeViewModel.get().isNotBlank())
            user_name.text = "${HomeViewModel.get()}"
        else user_name.text = name

        val listView = curView.findViewById<ListView>(R.id.profile_list_event)

        val adapter = EventAdapter(curView.context, array)

        listView.adapter = adapter

        return curView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()

        val profile_img = view.findViewById<CircleImageView>(R.id.profile_image)
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

        val user_name = view.findViewById<TextView>(R.id.profile_user_name)
        val events_array: ArrayList<EventData> = arrayListOf()
        if(HomeViewModel.get().isNotBlank())
            user_name.text = "${HomeViewModel.get()}"
        else user_name.text = name
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

                        var check = db.collection("events")
                            .document(document.id)
                            .collection("participants")
                            .whereEqualTo("status", 1)
                            .get()
                            .addOnSuccessListener {documents2 ->
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

        val profile_image = view.findViewById<CircleImageView>(R.id.profile_image)

        profile_image.setOnClickListener {
            openGallery()
        }

        view.findViewById<TextView>(R.id.save_profile_button).setOnClickListener {
            saveProfilePic()

            view.findViewById<TextView>(R.id.save_profile_button).visibility = View.INVISIBLE
            view.findViewById<TextView>(R.id.undo_profile_button).visibility = View.INVISIBLE
        }

        view.findViewById<TextView>(R.id.undo_profile_button).setOnClickListener {
            val localFilePath = File(requireContext().filesDir, "local_image.jpg").absolutePath
            val localFile = File(localFilePath)

            Glide.with(this)
                .load(localFile)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .placeholder(R.drawable.thang_ngot)
                .into(profile_img)

            view.findViewById<TextView>(R.id.save_profile_button).visibility = View.INVISIBLE
            view.findViewById<TextView>(R.id.undo_profile_button).visibility = View.INVISIBLE
        }

        val edit_profile_button = view.findViewById<ImageView>(R.id.edit_profile_button)

        edit_profile_button.setOnClickListener {
            //saveProfilePic()
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                openGallery()
            } else {
                // Handle permission denied
            }
        }

    private val getContentLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val selectedImageUri = result.data?.data

                requireView().findViewById<TextView>(R.id.save_profile_button).visibility = View.VISIBLE
                requireView().findViewById<TextView>(R.id.undo_profile_button).visibility = View.VISIBLE

                Picasso.with(requireView().context)
                    .load(selectedImageUri)
                    .fit()
                    .centerCrop()
                    .into(requireView().findViewById<CircleImageView>(R.id.profile_image))
            }
        }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore. Images.Media.EXTERNAL_CONTENT_URI)
        getContentLauncher.launch(intent)
    }

    override fun onStop() {
        super.onStop()

        val sharedPreferences = requireActivity().getSharedPreferences("sharedPrefs", 0)
        var email = sharedPreferences.getString("email", null)

        val storageReference = Firebase.storage("gs://cs426-project.appspot.com").reference

        var profileRef = storageReference.child("images/" + email + "_profile.jpg")

        val localFilePath = File(requireContext().filesDir, "local_image.jpg").absolutePath

        // Create parent directories if they don't exist
        val parentDir = File(localFilePath).parentFile
        if (!parentDir.exists()) {
            parentDir.mkdirs()
        }

        val localFile = File(localFilePath)

        if (localFile.exists()) {
            // If it exists, delete it
            localFile.delete()
        }

        profileRef.getFile(localFile)
            .addOnSuccessListener { taskSnapshot ->
                Log.d("On stop email", email.toString())
            }
            .addOnFailureListener { exception ->
                // Handle any errors that occurred during the download
            }
    }

    private fun saveProfilePic() {
        val storage = Firebase.storage("gs://cs426-project.appspot.com")

        var storageRef = storage.reference

        val sharedPreferences = context?.getSharedPreferences("sharedPrefs", 0)
        var email = sharedPreferences?.getString("email", null)

        var profileRef = storageRef.child("images/" + email + "_profile.jpg")

        Toast.makeText(context, "Profile updated", Toast.LENGTH_SHORT).show()

        var imageView = requireView().findViewById<CircleImageView>(R.id.profile_image)

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
        val listView = curView.findViewById<ListView>(R.id.profile_list_event)

        val adapter = EventAdapter(curView.context, events_array, true)

        listView.adapter = adapter
    }
}