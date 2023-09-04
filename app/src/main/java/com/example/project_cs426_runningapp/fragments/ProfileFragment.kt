package com.example.project_cs426_runningapp.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.project_cs426_runningapp.adapters.EventAdapter
import com.example.project_cs426_runningapp.adapters.EventData
import com.example.project_cs426_runningapp.R
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

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

        val profile_image = curView.findViewById<ImageView>(R.id.profile_image)
        val user_name = curView.findViewById<TextView>(R.id.profile_user_name)

        profile_image.setImageResource(R.drawable.thang_ngot)

        val sharedPreferences = requireActivity().getSharedPreferences("sharedPrefs", 0)
        name = sharedPreferences.getString("fullname", null)

        user_name.text = name

        val listView = curView.findViewById<ListView>(R.id.profile_list_event)

        val adapter = EventAdapter(curView.context, array)

        listView.adapter = adapter

        return curView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()

        val sharedPreferences2 = view.context.getSharedPreferences("sharedPrefs", 0)
        val email = sharedPreferences2.getString("email", null)

        val events_array: ArrayList<EventData> = arrayListOf()

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
    }

    private fun setUpEventAdapter(events_array: ArrayList<EventData>, curView: View) {
        val listView = curView.findViewById<ListView>(R.id.profile_list_event)

        val adapter = EventAdapter(curView.context, events_array)

        listView.adapter = adapter
    }
}