package com.sandeepprabhakula.eventmanagementapp.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class EventDetails(
    var typeOfEvent:String = "",
    var eventName:String = "",
    var eventDescription:String = "",
    var eventStartDate:String? = "",
    var eventEndDate:String? = "",
    var isPaid:String = "",
    var posterURL:String = "",
    var listOfParticipants:ArrayList<String> = ArrayList(),
):Parcelable
