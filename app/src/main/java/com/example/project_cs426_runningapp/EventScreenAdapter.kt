package com.example.project_cs426_runningapp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.squareup.picasso.Picasso
import org.checkerframework.checker.units.qual.s
import org.w3c.dom.Text


class EventAdapter(private val context: Context, private val dataSource: ArrayList<EventData>) : BaseAdapter() {

    private var inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

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
        val rowView = inflater.inflate(R.layout.event_list_view, parent, false)

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

        join_button.setOnClickListener {
            join_button.text = "Joined"
            join_button.setBackgroundResource(R.drawable.round_outline_gray)
        }
        return rowView
    }
}

public class EventData(var event_name: String, var joined: Boolean, var image_name: String?,
                       var start_date: String?, var end_date: String?)