package com.sandeepprabhakula.eventmanagementapp.daos

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.sandeepprabhakula.eventmanagementapp.data.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class UserDao {
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")
    private val auth = FirebaseAuth.getInstance()
    private val firebaseUser = auth.currentUser
    fun addUsers(user: User?) {
        user?.let {
            CoroutineScope(Dispatchers.IO).launch {
                usersCollection.document(user.uid).set(it)
            }
        }
    }
    fun getUserById(uid:String): Task<DocumentSnapshot> {
        return usersCollection.document(uid).get()
    }
    fun getCurrentUser(uid:String):Task<DocumentSnapshot>{
        return usersCollection.document(uid).get(Source.CACHE)
    }
    fun completeProfile(user:User){
        CoroutineScope(Dispatchers.IO).launch {
            usersCollection.document(firebaseUser?.uid.toString()).set(user).await()
        }
    }
}