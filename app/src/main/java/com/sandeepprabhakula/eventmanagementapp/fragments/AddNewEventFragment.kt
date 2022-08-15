package com.sandeepprabhakula.eventmanagementapp.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.sandeepprabhakula.eventmanagementapp.R
import com.sandeepprabhakula.eventmanagementapp.data.EventDetails
import com.sandeepprabhakula.eventmanagementapp.databinding.FragmentAddNewEventBinding
import com.sandeepprabhakula.eventmanagementapp.singleton.SingletonDaoObjects
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AddNewEventFragment : Fragment() {
    private var _binding: FragmentAddNewEventBinding? = null
    private val binding get() = _binding!!
    var curFile: Uri? = null
    var extension: String? = ""
    private val imageRef = Firebase.storage.reference
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddNewEventBinding.inflate(layoutInflater, container, false)
        requestPermissions()
        binding.posterImage.setOnClickListener {
            getContent.launch("image/*")
        }
        val isPaid = resources.getStringArray(R.array.ifPaid)
        val typeOfEvent = resources.getStringArray(R.array.eventType)
        if (binding.spinner != null) {
            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item, isPaid
            )
            binding.spinner.adapter = adapter
            binding.spinner.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    p0: AdapterView<*>?,
                    p1: View?,
                    position: Int,
                    p3: Long
                ) {
                    binding.isPaidTxt.setText(isPaid[position])
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    TODO("Not yet implemented")
                }

            }
        }

        if (binding.typeSpinner != null) {
            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item, typeOfEvent
            )
            binding.typeSpinner.adapter = adapter
            binding.typeSpinner.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    p0: AdapterView<*>?,
                    p1: View?,
                    position: Int,
                    p3: Long
                ) {
                    binding.typeOfEvent.setText(typeOfEvent[position])
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    TODO("Not yet implemented")
                }

            }
        }

        binding.createEvent.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                val organizerEmail = binding.eventOrganizerEmail.text.toString()
                val isEmail = organizerEmail.contains("@", ignoreCase = true)
                if (!TextUtils.isEmpty(binding.eventName.text) &&
                    !TextUtils.isEmpty(binding.eventDescription.text) &&
                    !TextUtils.isEmpty(binding.eventStartDate.text) &&
                    !TextUtils.isEmpty(binding.eventEndDate.text) &&
                    !TextUtils.isEmpty(binding.eventOrganizerEmail.text) &&
                    isEventAlreadyPresent(binding.eventName.text.toString()) &&
                    isEmail &&
                    binding.isPaidTxt.text.toString() != "is event paid or not?" &&
                    binding.typeOfEvent.text.toString() != "Select the type the event belongs to" &&
                    curFile != null &&
                    extension != null
                ) {
                    binding.createEvent.text = "Creating Event..."
                    addImagesToStorage(binding.eventName.text.toString(), extension.toString())
                    val posterURL =
                        getDownloadURL(binding.eventName.text.toString(), extension.toString())
                    Log.d("url", posterURL)
                    val eventsDao = SingletonDaoObjects.eventsDao
                    val eventDetails = EventDetails(
                        binding.typeOfEvent.text.toString(),
                        binding.eventName.text.toString(),
                        binding.eventDescription.text.toString(),
                        binding.eventStartDate.text.toString(),
                        binding.eventEndDate.text.toString(),
                        binding.isPaidTxt.text.toString(),
                        posterURL,
                        binding.eventOrganizerEmail.text.toString()
                    )
                    eventsDao.createEvent(eventDetails)
                    withContext(Dispatchers.Main) { findNavController().navigate(R.id.action_addNewEventFragment_to_eventsFragment2) }
                } else {
                    withContext(Dispatchers.Main) {
                        val toast = Toast.makeText(
                            context,
                            "Something went wrong",
                            Toast.LENGTH_SHORT
                        )
                        toast.setGravity(Gravity.CENTER, 0, 0)
                        toast.show()
                    }
                }
            }

        }
        return binding.root
    }

    private suspend fun isEventAlreadyPresent(eventName: String): Boolean =
        withContext(Dispatchers.IO) {
            val db = FirebaseFirestore.getInstance()
            val eventCollection = db.collection("events")
            val event =
                eventCollection.document(eventName).get().await().toObject(EventDetails::class.java)
            event == null
        }

    private fun hasReadExternalStoragePermission() =
        ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

    private fun requestPermissions() {
        val permissionToRequest = mutableListOf<String>()

        if (!hasReadExternalStoragePermission()) {
            permissionToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (permissionToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                permissionToRequest.toTypedArray(),
                200
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 200 && grantResults.isNotEmpty()) {
            for (i in grantResults.indices) {
                if (i == PackageManager.PERMISSION_GRANTED) {
                    Log.d("requestedPermission", "${permissions[i]} granted")
                }
            }
        }
    }

    private val getContent =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            curFile = uri
            val mime = MimeTypeMap.getSingleton()
            extension = mime.getExtensionFromMimeType(context?.contentResolver?.getType(uri!!))
            Log.d("extension", extension.toString())
            binding.posterImage.setImageURI(curFile)

        }

    private suspend fun addImagesToStorage(filename: String, extension: String) {
        try {
            curFile?.let {
                imageRef.child("poster/$filename.$extension").putFile(it).await()
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                val toast = Toast.makeText(context, e.message, Toast.LENGTH_SHORT)
                toast.setGravity(Gravity.CENTER, 0, 0)
                toast.show()
            }
        }
    }

    private suspend fun getDownloadURL(filename: String, extension: String): String {
        return imageRef.child("poster/$filename.$extension").downloadUrl.await().toString()
    }
}