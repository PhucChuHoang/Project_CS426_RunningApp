package com.example.project_cs426_runningapp.adapters

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.project_cs426_runningapp.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import java.io.ByteArrayOutputStream
import java.io.File

class EventAdapter(private val context: Context, private val dataSource: ArrayList<EventData>,
                   private val profile_specific: Boolean = false): RecyclerView.Adapter<EventAdapter.ViewHolder>() {

    private lateinit var db: FirebaseFirestore

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val event_title = view.findViewById<TextView>(R.id.text_event_name)
        val start_date = view.findViewById<TextView>(R.id.event_start_date)
        val end_date = view.findViewById<TextView>(R.id.event_end_date)

        val thumbnail = view.findViewById(R.id.event_thumbnail) as ImageView
        val join_button = view.findViewById(R.id.join_challenge_button) as TextView

        init {

        }
    }

    override fun getItemCount(): Int {
        return dataSource.size
    }

    private fun getItem(position: Int): Any {
        return dataSource[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.event_list_view, viewGroup, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        var event_data = getItem(position) as EventData

        viewHolder.event_title.text = event_data.event_name
        viewHolder.start_date.text = "Start: " + event_data.start_date
        viewHolder.end_date.text = "End: " + event_data.end_date

        addEventImage(viewHolder.thumbnail, event_data.image_name)

        var emailArray = arrayListOf<String?>()

        db = FirebaseFirestore.getInstance()

        val sharedPreferences = context.getSharedPreferences("sharedPrefs", 0)
        var email = sharedPreferences.getString("email", null)

        db.collection("events")
            .document(event_data.event_id)
            .collection("participants")
            .whereEqualTo("status", 1)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    emailArray.add("${document.id}")
                }
                setJoin(emailArray, email, viewHolder, position)
            }
            .addOnFailureListener { exception ->
                Log.w("Error", "Error getting documents: ", exception)
            }

        viewHolder.join_button.setOnClickListener {
            if (viewHolder.join_button.text.toString() == "Join challenge") {
                Log.d("Email", email.toString())

                if (email != null && emailArray.indexOf(email) == -1) {
                    Log.d("Join", "Here")
                    viewHolder.join_button.text = "Joined"
                    viewHolder.join_button.setBackgroundResource(R.drawable.round_outline_gray)

                    db.collection("events")
                        .document(event_data.event_id)
                        .collection("participants").document(email).set(hashMapOf("status" to 1))
                }
            } else {
                if (profile_specific) {
                    Log.d("Join", "Not anymore")

                    //UI
                    val removed_item = dataSource.get(position)
                    dataSource.remove(removed_item)
                    notifyDataSetChanged()

                    if (email != null) {
                        db.collection("events")
                            .document(event_data.event_id)
                            .collection("participants").document(email)
                            .set(hashMapOf("status" to 0))
                    }
                }
            }
        }
    }

    fun addEventImage(imageView: ImageView, position: String?) {
        val localFilePath = File(context.filesDir, position).absolutePath
        val localFile = File(localFilePath)

        Picasso.with(context)
            .load(localFile)
            .centerCrop()
            .fit()
            .into(imageView)
    }

     private fun setJoin(emailArray: ArrayList<String?>, email: String?, viewHolder: ViewHolder, position: Int = 10) {
        if (emailArray.isNotEmpty() && emailArray.indexOf(email) != -1) {
            viewHolder.join_button.text = "Joined"
            if (profile_specific) {
                viewHolder.join_button.text = "Withdraw"
            }
            viewHolder.join_button.setBackgroundResource(R.drawable.round_outline_gray)
        }
        else {
            Log.d("Empty here " + position, "true")
            viewHolder.join_button.text = "Join challenge"
            viewHolder.join_button.setBackgroundResource(R.drawable.round_outline)
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

}

class EventData(var event_name: String, var joined: Boolean, var image_name: String?,
                       var start_date: String?, var end_date: String?, var event_id: String, var admin: String? = "")