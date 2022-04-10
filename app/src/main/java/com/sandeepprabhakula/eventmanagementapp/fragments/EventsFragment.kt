package com.sandeepprabhakula.eventmanagementapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import com.sandeepprabhakula.eventmanagementapp.R
import com.sandeepprabhakula.eventmanagementapp.adapters.EventsAdapter
import com.sandeepprabhakula.eventmanagementapp.adapters.NonTechEventAdapter
import com.sandeepprabhakula.eventmanagementapp.adapters.OnClickEventDetails
import com.sandeepprabhakula.eventmanagementapp.daos.EventsDao
import com.sandeepprabhakula.eventmanagementapp.daos.UserDao
import com.sandeepprabhakula.eventmanagementapp.data.EventDetails
import com.sandeepprabhakula.eventmanagementapp.data.User
import com.sandeepprabhakula.eventmanagementapp.data.Utils
import com.sandeepprabhakula.eventmanagementapp.databinding.FragmentEventsFragementBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class EventsFragment : Fragment(), OnClickEventDetails {
    private var _binding: FragmentEventsFragementBinding? = null
    private val binding get() = _binding!!
    private val mAuth = FirebaseAuth.getInstance()
    private val currentUser = mAuth.currentUser
    private lateinit var adapter: EventsAdapter
    private lateinit var nonEveAdapter: NonTechEventAdapter
    private lateinit var eventsDao: EventsDao
    private val userDao = UserDao()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEventsFragementBinding.inflate(layoutInflater, container, false)
        if (currentUser?.uid != Utils.ADMIN_UID) {
            binding.addNewEvent.visibility = View.GONE
        } else {
            binding.addNewEvent.visibility = View.VISIBLE
        }
        if (currentUser?.uid != Utils.ADMIN_UID) {
            CoroutineScope(Dispatchers.IO).launch {
                val user = userDao.getUserById(currentUser?.uid.toString()).await()
                    .toObject(User::class.java)
                if (!user?.profileIsComplete!!) {
                    withContext(Dispatchers.Main) {
                        findNavController().navigate(R.id.action_eventsFragment_to_completeProfile2)
                    }
                }
            }
        }
        setUpRecyclerViewForTech()
        setUpRecyclerViewForNonTech()
        binding.addNewEvent.setOnClickListener {
            findNavController().navigate(R.id.action_eventsFragment_to_addNewEventFragment)
        }
        return binding.root
    }

    private fun setUpRecyclerViewForTech() {
        eventsDao = EventsDao()
        val query = eventsDao.eventCollection.whereEqualTo("typeOfEvent", "Technical")
            .orderBy("eventStartDate", Query.Direction.DESCENDING)
        val recyclerViewOptions = FirestoreRecyclerOptions.Builder<EventDetails>()
            .setQuery(query, EventDetails::class.java).build()
        binding.technical.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        adapter = EventsAdapter(recyclerViewOptions, this)
        binding.technical.adapter = adapter
    }

    private fun setUpRecyclerViewForNonTech() {
        eventsDao = EventsDao()
        val query = eventsDao.eventCollection.whereEqualTo("typeOfEvent", "Non Technical")
        val recyclerViewOptions = FirestoreRecyclerOptions.Builder<EventDetails>()
            .setQuery(query, EventDetails::class.java).build()
        binding.nonTechnical.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        nonEveAdapter = NonTechEventAdapter(recyclerViewOptions, this)
        binding.nonTechnical.adapter = nonEveAdapter
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
        nonEveAdapter.startListening()
    }

    override fun onDestroy() {
        super.onDestroy()
        adapter.stopListening()
        nonEveAdapter.stopListening()
    }

    override fun onEventDetailsClicked(eventName: String) {
        val eventDao = EventsDao()
        CoroutineScope(Dispatchers.IO).launch {
            val event = eventDao.getEventById(eventName).await().toObject(EventDetails::class.java)
            withContext(Dispatchers.Main) {
                val action =
                    EventsFragmentDirections.actionEventsFragmentToEventFullDetails(event!!)
                findNavController().navigate(action)
            }
        }
    }
}