package com.example.project_cs426_runningapp

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var name: String? = null

    private lateinit var db: FirebaseFirestore

    var array: ArrayList<EventData> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val curView = inflater.inflate(R.layout.fragment_profile, container, false);

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
        var email = sharedPreferences2.getString("email", null)

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

                        var check = db.collection("events")
                            .document("${document.id}")
                            .collection("participants")
                            .whereEqualTo("status", 1)
                            .get()
                            .addOnSuccessListener {documents2 ->
                                 for (document2 in documents2) {
                                    if (document2.id == email) {
                                        events_array.add(
                                            EventData(
                                                event_name, true, image_url,
                                                start_date, end_date, "${document.id}"
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
    }

    private fun setUpEventAdapter(events_array: ArrayList<EventData>, curView: View) {
        val listView = curView.findViewById<ListView>(R.id.profile_list_event)

        val adapter = EventAdapter(curView.context, events_array, true)

        listView.adapter = adapter
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ProfileFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ProfileFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}