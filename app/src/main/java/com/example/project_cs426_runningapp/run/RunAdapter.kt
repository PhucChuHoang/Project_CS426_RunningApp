package com.example.project_cs426_runningapp.run

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.project_cs426_runningapp.R
import com.example.project_cs426_runningapp.other.TrackingUtility
import com.google.android.material.textview.MaterialTextView
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class RunAdapter(private val dataSet: ArrayList<Run>) :
    RecyclerView.Adapter<RunAdapter.ViewHolder>() {
    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivRunImage : ImageView
        val tvDate : MaterialTextView
        val tvAvgSpeed : MaterialTextView
        val tvDistance : MaterialTextView
        val tvTime : MaterialTextView
        val tvCalories : MaterialTextView
        init {
            // Define click listener for the ViewHolder's View
            ivRunImage = view.findViewById(R.id.ivRunImage)
            tvDate = view.findViewById(R.id.tvDate)
            tvAvgSpeed = view.findViewById(R.id.tvAvgSpeed)
            tvDistance = view.findViewById(R.id.tvDistance)
            tvTime = view.findViewById(R.id.tvTime)
            tvCalories = view.findViewById(R.id.tvCalories)
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_run, viewGroup, false)
        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
       // Glide.with(viewHolder.ivRunImage.context).load(toBitmap(dataSet[position].byteImgString)).into(viewHolder.ivRunImage)
        val calendar = Calendar.getInstance().apply {
            timeInMillis = dataSet[position].timestamp
        }
        val dateFormat = SimpleDateFormat("dd.MM.yy", Locale.getDefault())
        viewHolder.tvDate.text = dateFormat.format(calendar.time)

        val avgSpeed = "${dataSet[position].avgSpeedInKMH}km/h"
        viewHolder.tvAvgSpeed.text = avgSpeed

        val distanceInKm = "${dataSet[position].distanceInMeters / 1000f}km"
        viewHolder.tvDistance.text = distanceInKm

        viewHolder.tvTime.text = TrackingUtility.getFormattedStopWatchTime(dataSet[position].timeInMillis)

        val caloriesBurned = "${dataSet[position].caloriesBurned}kcal"
        viewHolder.tvCalories.text = caloriesBurned
    }
    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size
    private fun toBitmap(string: String): Bitmap {
        Log.d("toBitmap", string)
        val byteArray = string.toByteArray()
        Log.d("byteArraySize", String(byteArray))
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }

}