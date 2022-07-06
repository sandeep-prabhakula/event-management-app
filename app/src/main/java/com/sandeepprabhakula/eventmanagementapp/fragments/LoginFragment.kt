package com.sandeepprabhakula.eventmanagementapp.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.sandeepprabhakula.eventmanagementapp.BuildConfig
import com.sandeepprabhakula.eventmanagementapp.R
import com.sandeepprabhakula.eventmanagementapp.daos.UserDao
import com.sandeepprabhakula.eventmanagementapp.data.User
import com.sandeepprabhakula.eventmanagementapp.databinding.FragmentLoginBinding
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

class LoginFragment : Fragment() {
    private var _binding:FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val RC_SIGN_IN = 200
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var mAuth:FirebaseAuth
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(layoutInflater, container, false)
        val userSignInText: TextView = binding.googleSignIn.getChildAt(0) as TextView
        userSignInText.text = "Sign Up With Google"
        mAuth = FirebaseAuth.getInstance()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(BuildConfig.OAUTH_CLIENT_ID)
            .requestEmail()
            .build()
        if(mAuth.currentUser==null){
            binding.checkUserActiveTxt.visibility = View.GONE
            binding.progressBar.visibility = View.GONE
        }
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
        binding.googleSignIn.setOnClickListener {
            signIn()
        }
        return binding.root
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(task: Task<GoogleSignInAccount>) {
        try {
            val account = task.getResult(ApiException::class.java)!!
            Log.d("TAG", "firebaseAuthWithGoogle:" + account.id)
            firebaseAuthWithGoogle(account.idToken!!)
        } catch (e: ApiException) {
            Log.w("TAG", "Google sign in failed", e)
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        CoroutineScope(Dispatchers.IO).launch {
            val auth = mAuth.signInWithCredential(credential).await()
            val user = auth.user
            withContext(Dispatchers.Main) {
                updateUI(user)
            }
        }
    }

    private fun updateUI(firebaseUser: FirebaseUser?) {
        val db = FirebaseFirestore.getInstance()
        val usersCollection = db.collection("users")
        if (firebaseUser != null) {
            GlobalScope.launch(Dispatchers.IO) {
                var user = usersCollection.document(firebaseUser.uid).get().await()
                    .toObject(User::class.java)
                if (user != null) {
                    withContext(Dispatchers.Main) {
                        findNavController().navigate(R.id.action_loginFragment_to_eventsFragment)
                    }
                } else {
                    user = User(
                        firebaseUser.uid,
                        firebaseUser.displayName.toString(),
                        firebaseUser.email.toString(),
                        firebaseUser.photoUrl.toString()
                    )
                    val userDao = UserDao()
                    userDao.addUsers(user)
                    withContext(Dispatchers.Main) {
                        findNavController().navigate(R.id.action_loginFragment_to_eventsFragment)
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        googleSignInClient.signOut()
        updateUI(mAuth.currentUser)
    }
}