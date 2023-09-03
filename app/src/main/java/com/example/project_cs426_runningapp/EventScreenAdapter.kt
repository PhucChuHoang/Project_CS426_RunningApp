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

        val event_title = rowView.findViewById(R.id.text_event_name) as TextView
        val thumbnail = rowView.findViewById(R.id.event_thumbnail) as ImageView
        val join_button = rowView.findViewById(R.id.join_challenge_button) as TextView

        event_title.text = event_data.event_name

        if (!event_data.image_name.isNullOrEmpty()) {
            val p = event_data.image_name?.split("/")?.toTypedArray()
            //Create the new image link
            //Create the new image link
            val imageLink = "https://drive.google.com/uc?export=download&id=" + (p?.get(5) ?: "")
            Picasso.with(rowView.context)
                .load(imageLink)
                .fit()
                .centerCrop()
                .into(thumbnail)
        }
        return rowView
    }

}

public class EventData(var event_name: String, var joined: Boolean, var image_name: String?)