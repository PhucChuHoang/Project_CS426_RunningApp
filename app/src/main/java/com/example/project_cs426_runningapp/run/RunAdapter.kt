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

    private lateinit var mListener : onItemClickListener

    interface onItemClickListener{
        fun onItemClick(position: Int)

    }
    fun setOnItemClickListener(listener: onItemClickListener)
    {
        mListener = listener
    }

    class ViewHolder(view: View, listener: onItemClickListener) : RecyclerView.ViewHolder(view) {
        val ivRunImage : ImageView
        val tvDate : MaterialTextView
        val tvAvgSpeed : MaterialTextView
        val tvDistance : MaterialTextView
        val tvTime : MaterialTextView
        val tvCalories : MaterialTextView
        init {
            ivRunImage = view.findViewById(R.id.ivRunImage)
            tvDate = view.findViewById(R.id.tvDate)
            tvAvgSpeed = view.findViewById(R.id.tvAvgSpeed)
            tvDistance = view.findViewById(R.id.tvDistance)
            tvTime = view.findViewById(R.id.tvTime)
            tvCalories = view.findViewById(R.id.tvCalories)
            view.setOnClickListener{
                listener.onItemClick(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_run, viewGroup, false)
        return ViewHolder(view,mListener)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {


        Glide.with(viewHolder.ivRunImage.context).load(dataSet[position].Img).into(viewHolder.ivRunImage)
        val calendar = Calendar.getInstance().apply {
            timeInMillis = dataSet[position].timestamp
        }
        val dateFormat = SimpleDateFormat("dd.MM.yy", Locale.getDefault())
        viewHolder.tvDate.text = dateFormat.format(calendar.time)

        val avgSpeed = "${dataSet[position].avgSpeedInKMH} km/h"
        viewHolder.tvAvgSpeed.text = avgSpeed

        val distanceInKm = "${dataSet[position].distanceInMeters / 1000f} km"
        viewHolder.tvDistance.text = distanceInKm

        viewHolder.tvTime.text = TrackingUtility.getFormattedStopWatchTime(dataSet[position].timeInMillis)

        val caloriesBurned = "${dataSet[position].caloriesBurned} kcal"
        viewHolder.tvCalories.text = caloriesBurned
    }
    override fun getItemCount() = dataSet.size



}