package com.example.project_cs426_runningapp.fragments

import android.annotation.SuppressLint
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project_cs426_runningapp.R
import com.example.project_cs426_runningapp.adapters.EventAdapter
import com.example.project_cs426_runningapp.adapters.EventData
import com.example.project_cs426_runningapp.databinding.FragmentEventBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File


class EventFragment : Fragment() {
    private lateinit var binding: FragmentEventBinding

    private lateinit var db: FirebaseFirestore

    private var total_event = 0

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

        binding.eventListView.layoutManager = LinearLayoutManager(requireContext())

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
//            val event0 = hashMapOf(
//                "event_name" to "Wild Running 2023",
//                "image_url" to "event_image_0.jpg",
//                "status" to 1,
//                "start_date" to "9/10/2023",
//                "end_date" to "9/17/2023",
//                "event_id" to "eid000"
//            )
//
//            db.collection("events").document("eid000")
//                .set(event0)
//
//            db.collection("events")
//                .document("eid000")
//                .collection("participants")
//                .document("nmvinhdl1215@gmail.com").set(hashMapOf("status" to 0))
//
//            val event1 = hashMapOf(
//                "event_name" to "Nike 2nd Open Cup",
//                "image_url" to "event_image_1.jpg",
//                "status" to 1,
//                "start_date" to "10/2/2023",
//                "end_date" to "10/5/2023",
//                "event_id" to "eid001"
//            )
//

//
//            db.collection("events").document("eid001")
//                .set(event1)
//
//            db.collection("events")
//                .document("eid001")
//                .collection("participants")
//                .document("nmvinhdl1215@gmail.com").set(hashMapOf("status" to 0))
//
//            val event2 = hashMapOf(
//                "event_name" to "MARTHE College Training Camp",
//                "image_url" to "event_image_2.jpg",
//                "status" to 1,
//                "start_date" to "9/9/2023",
//                "end_date" to "11/9/2023",
//                "event_id" to "eid002"
//            )
//
//            db.collection("events").document("eid002")
//                .set(event2)
//
//            db.collection("events")
//                .document("eid002")
//                .collection("participants")
//                .document("nmvinhdl1215@gmail.com").set(hashMapOf("status" to 0))
//
//            val event3 = hashMapOf(
//                "event_name" to "Vitafield Green Marathon",
//                "image_url" to "event_image_3.jpg",
//                "status" to 1,
//                "start_date" to "11/17/2023",
//                "end_date" to "11/17/2023",
//                "event_id" to "eid003"
//            )
//
//            db.collection("events").document("eid003")
//                .set(event3)
//
//            db.collection("events")
//                .document("eid003")
//                .collection("participants")
//                .document("nmvinhdl1215@gmail.com").set(hashMapOf("status" to 0))
//
//            val event4 = hashMapOf(
//                "event_name" to "Race for Life Marathon",
//                "image_url" to "event_image_4.jpg",
//                "status" to 1,
//                "start_date" to "1/23/2024",
//                "end_date" to "11/23/2024",
//                "event_id" to "eid004"
//            )
//
//            db.collection("events").document("eid004")
//                .set(event4)
//
//            db.collection("events")
//                .document("eid004")
//                .collection("participants")
//                .document("nmvinhdl1215@gmail.com").set(hashMapOf("status" to 0))

//            for (i in 0..0) {
//                db.collection("events").document("eid00" + i)
//                    .delete()
//            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Change fragment
        val clickListener = View.OnClickListener { v ->
            when (v) {
                binding.eventEditButton -> {
                    findNavController().navigate(R.id.action_eventFragment_to_addEventFragment2)
                }
            }
        }

        binding.eventEditButton.setOnClickListener(clickListener)

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
                        var admin = document.data?.get("admin") as? String
                        Log.d("Image_url", document.data?.get("image_url").toString())
                        // Update the UI on the main thread

                        events_array.add(
                            EventData(event_name, true, image_url,
                                        start_date, end_date, "${document.id}", admin)
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
        setUpEventImage(events_array)

        val listView = curView.findViewById<RecyclerView>(R.id.event_list_view)

        total_event = events_array.size
        val adapter = EventAdapter(curView.context, events_array)

        //Log.d("Layout manager", "Layout setup")

        val simpleItemTouchCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition

                val sharedPreferences = requireContext().getSharedPreferences("sharedPrefs", 0)
                var email = sharedPreferences.getString("email", null)

                adapter.notifyItemChanged(position)
                Log.d("Current log in", email.toString())
                if (email != null) {
                    if (email.isNotEmpty() && email == events_array[position].admin) {
                        Log.d("Remove admin", events_array[position].admin.toString())
                        removeItemList(events_array, position)
                    } else {
                        Toast.makeText(requireContext(), "No permission", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
                RecyclerViewSwipeDecorator.Builder(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
                    .addBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.light_red
                        )
                    )
                    .addActionIcon(R.drawable.ic_delete)
                    .create()
                    .decorate()

                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    val itemView = viewHolder.itemView
                    val height = itemView.bottom.toFloat() - itemView.top.toFloat()
                    val width = height / 3

                    if (dX < 0) {

                    }
                } else {
                    c.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX / 5, dY, actionState, isCurrentlyActive)
            }
        }

        val itemTouchHelper = ItemTouchHelper(simpleItemTouchCallback)
        itemTouchHelper.attachToRecyclerView(listView)
        listView.adapter = adapter
    }

    private fun removeItemList(events_array: ArrayList<EventData>, postion: Int) {
        db.collection("events").document(events_array[postion].event_id)
                   .delete()

        events_array.removeAt(postion)
        binding.eventListView.adapter?.notifyItemRemoved(postion)
    }

    private fun setUpEventImage(event_array: ArrayList<EventData>) {
        CoroutineScope(Dispatchers.Main).launch {
            val storageReference = Firebase.storage("gs://cs426-project.appspot.com").reference

            var p = ArrayList<Int>()
            for (i in 0 ..(event_array.size - 1)) {
                var sub = event_array[i].event_id.subSequence(5, event_array[i].event_id.length).toString()
                p.add(Integer.parseInt(sub))
            }
            p.sort()

            var nIndex = 0

            for (i in 0 .. (p.size - 1)) {
                if (p[i] != nIndex) {
                    break
                }
                else {
                    nIndex++
                }
            }

            //Should have been new_event_id instead, but work just fine
            val sharedPreferences = requireActivity().getSharedPreferences("total_event", 0)
            val editor = sharedPreferences.edit()
            editor.putString("total", nIndex.toString())
            editor.apply()

            for (i in 0..(event_array.size - 1)) {
                //Log.d("Events id", "eid" + i)
                var profileRef = storageReference.child("events/eid" + p[i] + ".jpg")

                val localFilePath =
                    File(binding.root.context.filesDir, "event_image_" + p[i] + ".jpg").absolutePath

                val parentDir = File(localFilePath).parentFile
                if (!parentDir.exists()) {
                    parentDir.mkdirs()
                }

                val localFile = File(localFilePath)

                if (!localFile.exists()) {
                    profileRef.getFile(localFile)
                        .addOnSuccessListener { taskSnapshot ->
                        }
                        .addOnFailureListener { exception ->
                            // Handle any errors that occurred during the download
                        }
                }
            }
        }
    }
}