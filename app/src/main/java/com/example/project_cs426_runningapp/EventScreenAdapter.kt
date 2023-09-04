package com.example.project_cs426_runningapp

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import org.checkerframework.checker.units.qual.s
import org.w3c.dom.Text


class EventAdapter(private val context: Context, private val dataSource: ArrayList<EventData>,
                    private val profile_specific: Boolean = false) : BaseAdapter() {

    private var inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    private lateinit var db: FirebaseFirestore

    private var can_join = false

    override fun getCount(): Int {
        return dataSource.size
    }

    override fun getItem(position: Int): Any {
        return dataSource[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        can_join = true

        val rowView: View

        if (convertView == null) {
            rowView = inflater.inflate(R.layout.event_list_view, parent, false)
        }
        else {
            rowView = convertView
        }
        val event_data = getItem(position) as EventData

        val event_title = rowView.findViewById<TextView>(R.id.text_event_name)
        val start_date = rowView.findViewById<TextView>(R.id.event_start_date)
        val end_date = rowView.findViewById<TextView>(R.id.event_end_date)

        val thumbnail = rowView.findViewById(R.id.event_thumbnail) as ImageView
        val join_button = rowView.findViewById(R.id.join_challenge_button) as TextView

        event_title.text = event_data.event_name
        start_date.text = "Start: " + event_data.start_date
        end_date.text = "End: " + event_data.end_date

        if (!event_data.image_name.isNullOrEmpty()) {
            val p = event_data.image_name?.split("/")?.toTypedArray()
            val imageLink = "https://drive.google.com/uc?export=download&id=" + (p?.get(5) ?: "")
            Picasso.with(rowView.context)
                .load(imageLink)
                .fit()
                .centerCrop()
                .into(thumbnail)
        }

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
                setJoin(emailArray, email, rowView)
            }
            .addOnFailureListener {exception ->
                Log.w("Error", "Error getting documents: ", exception)
            }

        join_button.setOnClickListener {
            Log.d("Can Join?", can_join.toString())
            if (join_button.text.toString() == "Join challenge") {
                Log.d("Email", email.toString())

                if (email != null && emailArray.indexOf(email) == -1) {
                    Log.d("Join", "Here")
                    join_button.text = "Joined"
                    join_button.setBackgroundResource(R.drawable.round_outline_gray)

                    db.collection("events")
                        .document(event_data.event_id)
                        .collection("participants").document(email).set(hashMapOf("status" to 1))
                }
            }
            else {
                if (profile_specific) {
                    Log.d("Join", "Not anymore")

                    //UI
                    val removed_item = dataSource.get(position)
                    dataSource.remove(removed_item)
                    notifyDataSetChanged()

                    if (email != null) {
                        db.collection("events")
                            .document(event_data.event_id)
                            .collection("participants").document(email).set(hashMapOf("status" to 0))
                    }
                }
            }
        }
        return rowView
    }

     private fun setJoin(emailArray: ArrayList<String?>, email: String?, rowView: View) {
        var join_button = rowView.findViewById(R.id.join_challenge_button) as TextView

        if (emailArray.indexOf(email) != -1) {
            join_button.text = "Joined"
            if (profile_specific) {
                join_button.text = "Withdraw"
            }
            join_button.setBackgroundResource(R.drawable.round_outline_gray)
            can_join = false;
        }
        else {
            can_join = true;
        }
    }

}

public class EventData(var event_name: String, var joined: Boolean, var image_name: String?,
                       var start_date: String?, var end_date: String?, var event_id: String)