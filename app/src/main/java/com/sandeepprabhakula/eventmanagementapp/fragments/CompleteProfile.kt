package com.sandeepprabhakula.eventmanagementapp.fragments

import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.sandeepprabhakula.eventmanagementapp.R
import com.sandeepprabhakula.eventmanagementapp.daos.UserDao
import com.sandeepprabhakula.eventmanagementapp.data.User
import com.sandeepprabhakula.eventmanagementapp.databinding.FragmentCompleteProfileBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class CompleteProfile : Fragment() {
    private var _binding: FragmentCompleteProfileBinding? = null
    private val binding get() = _binding!!
    private val mAuth = FirebaseAuth.getInstance()
    private val currentUser = mAuth.currentUser
    private val userDao = UserDao()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCompleteProfileBinding.inflate(layoutInflater, container, false)
        binding.completeProfile.setOnClickListener {
            if (!TextUtils.isEmpty(binding.userBranch.text) &&
                !TextUtils.isEmpty(binding.userMobile.text) &&
                !TextUtils.isEmpty(binding.userRollNo.text) &&
                !TextUtils.isEmpty(binding.userYear.text) &&
                !TextUtils.isEmpty(binding.userCollegeName.text)
            ) {
                val branch = binding.userBranch.text.toString()
                val year = binding.userYear.text.toString()
                val mobile = binding.userMobile.text.toString()
                val rollNumber = binding.userRollNo.text.toString()
                val collegeName = binding.userCollegeName.text.toString()
                CoroutineScope(Dispatchers.IO).launch {
                    val user = userDao.getCurrentUser(currentUser?.uid.toString()).await()
                        .toObject(User::class.java)
                    user?.profileIsComplete = true
                    user?.userBranch = branch
                    user?.userYear = year
                    user?.userMobileNumber = mobile
                    user?.userRollNumber = rollNumber
                    user?.userCollegeName = collegeName
                    userDao.completeProfile(user!!)
                    withContext(Dispatchers.Main) {
                        val toast = Toast.makeText(
                            context,
                            "profile completion successful.",
                            Toast.LENGTH_SHORT
                        )
                        toast.setGravity(Gravity.CENTER, 0, 0)
                        toast.show()
                        findNavController().navigate(R.id.action_completeProfile2_to_eventsFragment)
                    }
                }
            } else {
                val toast = Toast.makeText(context, "Something went wrong.", Toast.LENGTH_SHORT)
                toast.setGravity(Gravity.CENTER, 0, 0)
                toast.show()
            }
        }
        return binding.root
    }

}