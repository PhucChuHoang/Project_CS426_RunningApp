package com.example.project_cs426_runningapp

import android.annotation.SuppressLint
import android.nfc.Tag
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ListView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [DontKnowFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class EventFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    var array = arrayListOf(EventData("Wild running", true, "https://drive.google.com/file/d/1dVtJAWX3GtgH-YuTrUkHacBSKriLxyG0/view?usp=sharing"),
        EventData("Object 2", true, null),
        EventData("Object 3", false, null),
        EventData("Object 4", true, null),
        EventData("Object 5", true, null),)

    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    @SuppressLint("Range")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val curView = inflater.inflate(R.layout.fragment_event, container, false);

        db = FirebaseFirestore.getInstance()

        var edit_button = curView.findViewById<ImageView>(R.id.event_edit_button)

        edit_button.setOnClickListener {

        }

        var bell_button = curView.findViewById<ImageView>(R.id.event_bell_button)

        bell_button.setOnClickListener {

        }

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
                        Log.d("Event name", event_name)
                        // Update the UI on the main thread

                            events_array.add(EventData(event_name, true, image_url))
                    }
                    launch(Dispatchers.Main) {
                        setUpEventAdapter(events_array, curView)
                    }
                }

            } catch (e: Exception) {
                // Handle any exceptions that may occur during the Firestore operation
                e.printStackTrace()
            }
        }

        return curView
    }

    private fun setUpEventAdapter(events_array: ArrayList<EventData>, curView: View) {
        Log.d("Here", "Here")
        val listView = curView.findViewById<ListView>(R.id.event_list_view)

        val adapter = EventAdapter(curView.context, events_array)

        listView.adapter = adapter
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment DontKnowFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            EventFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}