package com.sandeepprabhakula.eventmanagementapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.sandeepprabhakula.eventmanagementapp.R
import com.sandeepprabhakula.eventmanagementapp.data.EventDetails

class NonTechEventAdapter(options: FirestoreRecyclerOptions<EventDetails>,private val listener :OnClickEventDetails) :
    FirestoreRecyclerAdapter<EventDetails, NonTechEventAdapter.NonTechViewHolder>(options) {
    class NonTechViewHolder(itemView: View) :RecyclerView.ViewHolder(itemView){
        val eventName: TextView = itemView.findViewById(R.id.createdEventName)
        val eventStartDate: TextView = itemView.findViewById(R.id.createdEventStartDate)
        val eventEndDate: TextView = itemView.findViewById(R.id.createdEventEndDate)
        val eventType: TextView = itemView.findViewById(R.id.createdEventType)
        val viewMore: Button = itemView.findViewById(R.id.viewMore)
        val poster: ImageView = itemView.findViewById(R.id.posterFromDB)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NonTechViewHolder {
        val view = NonTechViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.event_details_layout, parent, false
            )
        )
        view.viewMore.setOnClickListener {
            listener.onEventDetailsClicked(snapshots.getSnapshot(view.absoluteAdapterPosition).id)
        }
        return view
    }

    override fun onBindViewHolder(holder: NonTechViewHolder, position: Int, model: EventDetails) {
        holder.eventEndDate.text = model.eventEndDate
        holder.eventStartDate.text = model.eventStartDate
        holder.eventName.text = model.eventName
        holder.eventType.text = model.isPaid
        Glide.with(holder.poster).load(model.posterURL).into(holder.poster)
    }
}