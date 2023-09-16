package com.example.project_cs426_runningapp.fragments

import android.annotation.SuppressLint
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
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
                        val admin = document.data.get("admin") as? String
                        // Update the UI on the main thread

                        events_array.add(
                            EventData(event_name, true, image_url,
                                        start_date, end_date, document.id, admin)
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
                val email = sharedPreferences.getString("email", null)

                adapter.notifyItemChanged(position)
                if (email != null) {
                    if (email.isNotEmpty() && email == events_array[position].admin) {
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

                if (actionState != ItemTouchHelper.ACTION_STATE_SWIPE) {
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

        findNextIndex(events_array)
    }

    private fun findNextIndex(event_array: ArrayList<EventData>) {
        val p = ArrayList<Int>()
        for (i in 0 ..(event_array.size - 1)) {
            val sub = event_array[i].event_id.subSequence(5, event_array[i].event_id.length).toString()
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
    }

    private fun setUpEventImage(event_array: ArrayList<EventData>) {
        CoroutineScope(Dispatchers.Main).launch {
            val storageReference = Firebase.storage("gs://cs426-project.appspot.com").reference

            findNextIndex(event_array)

            val p = ArrayList<Int>()
            for (i in 0 ..(event_array.size - 1)) {
                val sub = event_array[i].event_id.subSequence(5, event_array[i].event_id.length).toString()
                p.add(Integer.parseInt(sub))
            }
            p.sort()

            for (i in 0..(event_array.size - 1)) {
                val profileRef = storageReference.child("events/eid" + p[i] + ".jpg")

                val localFilePath =
                    File(binding.root.context.filesDir, "event_image_" + p[i] + ".jpg").absolutePath

                val parentDir = File(localFilePath).parentFile
                if (parentDir != null) {
                    if (!parentDir.exists()) {
                        parentDir.mkdirs()
                    }
                }

                val localFile = File(localFilePath)

                if (!localFile.exists()) {
                    profileRef.getFile(localFile)
                        .addOnSuccessListener { taskSnapshot ->
                        }
                        .addOnFailureListener { exception ->
                        }
                }
            }
        }
    }
}