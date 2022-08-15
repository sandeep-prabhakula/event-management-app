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
import com.sandeepprabhakula.eventmanagementapp.BuildConfig
import com.sandeepprabhakula.eventmanagementapp.R
import com.sandeepprabhakula.eventmanagementapp.adapters.EventsAdapter
import com.sandeepprabhakula.eventmanagementapp.adapters.NonTechEventAdapter
import com.sandeepprabhakula.eventmanagementapp.adapters.OnClickEventDetails
import com.sandeepprabhakula.eventmanagementapp.data.EventDetails
import com.sandeepprabhakula.eventmanagementapp.data.User
import com.sandeepprabhakula.eventmanagementapp.databinding.FragmentEventsFragementBinding
import com.sandeepprabhakula.eventmanagementapp.singleton.SingletonDaoObjects
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
    private var eventsDao = SingletonDaoObjects.eventsDao
    private val userDao = SingletonDaoObjects.userDao
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEventsFragementBinding.inflate(layoutInflater, container, false)
        if (currentUser?.uid != BuildConfig.ADMIN_UID) {
            binding.addNewEvent.visibility = View.GONE
        } else {
            binding.addNewEvent.visibility = View.VISIBLE
        }
        if (currentUser?.uid != BuildConfig.ADMIN_UID) {
            CoroutineScope(Dispatchers.IO).launch {
                var user = userDao.getCurrentUser(currentUser?.uid.toString()).await()
                    .toObject(User::class.java)
                if(user == null){
                    user = userDao.getUserById(currentUser?.uid.toString()).await().toObject(User::class.java)
                }
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
        CoroutineScope(Dispatchers.IO).launch {
            val event = eventsDao.getEventFromCache(eventName).await().toObject(EventDetails::class.java)
            withContext(Dispatchers.Main) {
                val action =
                    EventsFragmentDirections.actionEventsFragmentToEventFullDetails(event!!)
                findNavController().navigate(action)
            }
        }
    }
}