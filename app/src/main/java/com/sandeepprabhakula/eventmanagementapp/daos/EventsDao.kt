package com.sandeepprabhakula.eventmanagementapp.daos

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.sandeepprabhakula.eventmanagementapp.data.EventDetails
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class EventsDao {
    private val db = FirebaseFirestore.getInstance()
    val eventCollection = db.collection("events")
        fun createEvent(eventDetails: EventDetails) {
        eventDetails.let {
            CoroutineScope(Dispatchers.IO).launch {
                eventCollection.document(eventDetails.eventName).set(it)
            }
        }
    }

    fun getEventById(postId: String): Task<DocumentSnapshot> {
        return eventCollection.document(postId).get()
    }
    private fun getEventFromCache(postID:String):Task<DocumentSnapshot>{
        return eventCollection.document(postID).get(Source.CACHE)
    }
    fun addParticipantToEvent(email: String, eventId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val eventDetails = getEventFromCache(eventId).await().toObject(EventDetails::class.java)
            eventDetails?.listOfParticipants?.add(email)
            eventCollection.document(eventId).set(eventDetails!!)
        }
    }
}