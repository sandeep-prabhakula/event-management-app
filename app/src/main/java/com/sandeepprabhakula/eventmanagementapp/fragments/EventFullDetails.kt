package com.sandeepprabhakula.eventmanagementapp.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.sandeepprabhakula.eventmanagementapp.data.Utils
import com.sandeepprabhakula.eventmanagementapp.databinding.FragmentEventFullDetailsBinding

class EventFullDetails : Fragment() {
    private var _binding: FragmentEventFullDetailsBinding? = null
    private val binding get() = _binding!!
    private val args by navArgs<EventFullDetailsArgs>()
    private val mAuth = FirebaseAuth.getInstance()
    private val currentUser = mAuth.currentUser
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEventFullDetailsBinding.inflate(layoutInflater, container, false)
        binding.scanParticipant.setOnClickListener {
            val action =
                EventFullDetailsDirections.actionEventFullDetailsToQRCodeScanner(args.eventDetails.eventName)
            findNavController().navigate(action)
        }
        val organiserEmail = currentUser?.email
        if (currentUser?.uid == Utils.ADMIN_UID || organiserEmail==args.eventDetails.organizerEmail
        ) {
            binding.scanParticipant.visibility = View.VISIBLE
        }else {
            binding.scanParticipant.visibility = View.GONE
        }
        Glide.with(this).load(args.eventDetails.posterURL).into(binding.eventDetailsPoster)
        binding.eventDetailsEventName.text = args.eventDetails.eventName
        binding.eventDetailsEventDescription.text = args.eventDetails.eventDescription
        binding.eventDetailsEventType.text = args.eventDetails.isPaid
        binding.eventDetailsEventStartDate.text = args.eventDetails.eventStartDate
        binding.eventDetailsEventEndDate.text = args.eventDetails.eventEndDate
        binding.noOfParticipants.text = args.eventDetails.listOfParticipants.size.toString()
        return binding.root
    }

}