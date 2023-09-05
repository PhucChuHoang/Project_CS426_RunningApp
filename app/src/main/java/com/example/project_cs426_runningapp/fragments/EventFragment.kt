package com.example.project_cs426_runningapp.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.lifecycle.lifecycleScope
import com.example.project_cs426_runningapp.adapters.EventAdapter
import com.example.project_cs426_runningapp.adapters.EventData
import com.example.project_cs426_runningapp.R
import com.example.project_cs426_runningapp.databinding.FragmentEventBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File

class EventFragment : Fragment() {
    private lateinit var binding: FragmentEventBinding

    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    @SuppressLint("Range")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment

        binding = FragmentEventBinding.inflate(inflater, container, false)

        binding.eventEditButton.setOnClickListener {
            db.collection("registers")
                .whereEqualTo("status", 1)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        Log.d("Register", "")
                    }
                }
        }

        binding.eventBellButton.setOnClickListener {

        }

        val sharedPreferences = requireActivity().getSharedPreferences("sharedPrefs", 0)
        var email = sharedPreferences.getString("email", null)

        val storageReference = Firebase.storage.reference

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
            Log.d("Login Delete","Image deleted")
        }

        profileRef.getFile(localFile)
            .addOnSuccessListener { taskSnapshot ->

            }
            .addOnFailureListener { exception ->
                // Handle any errors that occurred during the download
            }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()

        //Get events list
        var events_array: ArrayList<EventData> = arrayListOf()

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                var events = db.collection("events")
                    .whereEqualTo("status", 1)
                    .get()
                    .await()

                // Check if there are documents and update the UI
                if (!events.isEmpty) {
                    for (document in events) {

                        var event_name = document.data?.get("event_name") as String
                        var image_url = document.data?.get("image_url") as? String
                        var start_date = document.data?.get("start_date") as? String
                        var end_date = document.data?.get("end_date") as? String
                        Log.d("Image_url", document.data?.get("image_url").toString())
                        // Update the UI on the main thread

                        events_array.add(
                            EventData(event_name, true, image_url,
                                        start_date, end_date, "${document.id}")
                        )
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
    }

    private fun setUpEventAdapter(events_array: ArrayList<EventData>, curView: View) {
        val listView = curView.findViewById<ListView>(R.id.event_list_view)

        val adapter = EventAdapter(curView.context, events_array)

        listView.adapter = adapter
    }
}