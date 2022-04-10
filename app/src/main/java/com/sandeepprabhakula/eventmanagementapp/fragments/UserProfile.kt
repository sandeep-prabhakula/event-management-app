package com.sandeepprabhakula.eventmanagementapp.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.sandeepprabhakula.eventmanagementapp.R
import com.sandeepprabhakula.eventmanagementapp.data.User
import com.sandeepprabhakula.eventmanagementapp.databinding.FragmentUserProfileBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class UserProfile : Fragment() {
    private var _binding: FragmentUserProfileBinding? = null
    private val binding get() = _binding!!
    private val mAuth = FirebaseAuth.getInstance()
    private val currentUser = mAuth.currentUser
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserProfileBinding.inflate(layoutInflater, container, false)
        Glide.with(requireContext()).load(currentUser?.photoUrl).override(250, 250).circleCrop()
            .into(binding.dp)
        binding.userName.text = currentUser?.displayName
        binding.userEmail.text = currentUser?.email
        CoroutineScope(Dispatchers.IO).launch {
            val user = usersCollection.document(currentUser?.uid.toString()).get().await().toObject(
                User::class.java
            )
            withContext(Dispatchers.Main){
                binding.userMobile.text = user?.userMobileNumber
            }
        }
        binding.logout.setOnClickListener {
            mAuth.signOut()
            findNavController().navigate(R.id.action_userProfile_to_loginFragment)
        }
        binding.toQrCode.setOnClickListener {
            findNavController().navigate(R.id.action_userProfile_to_qrFragment)
        }
        binding.deleteAccount.setOnClickListener {
            val alertDialog = AlertDialog.Builder(context)
            alertDialog.setTitle("Delete Account ?")
            alertDialog.setMessage("All Data of yours will get vanished.")
            alertDialog.setPositiveButton("Yes,Delete") { _, _ ->
                CoroutineScope(Dispatchers.IO).launch {
                    currentUser?.delete()?.await()
                    usersCollection.document(currentUser?.uid.toString()).delete().await()
                    withContext(Dispatchers.Main) {
                        findNavController().navigate(R.id.action_userProfile_to_loginFragment)
                    }
                }
            }
            alertDialog.setNegativeButton("No") { _, _ ->

            }
            alertDialog.show()
        }
        return binding.root
    }
}