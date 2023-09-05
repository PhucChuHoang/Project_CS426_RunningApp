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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

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