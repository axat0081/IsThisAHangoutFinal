package com.example.isthisahangout.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.isthisahangout.models.User
import com.example.isthisahangout.utils.FirebaseResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.storage.StorageReference
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

const val LOGIN = "Login Successful"
const val REGISTER = "Account Created"
const val UPDATE = "Profile Updated"
const val DEFAULTPFP =
    "https://firebasestorage.googleapis.com/v0/b/isthisahangout-61d93.appspot.com/o/pfp%2Fpfp_placeholder.jpg?alt=media&token=35fa14c3-6451-41f6-a8be-448a59996f75"

@Singleton
class FirebaseAuthRepository @Inject constructor(
    private val mAuth: FirebaseAuth,
    @Named("UserRef")
    private val UserRef: DatabaseReference,
    @Named("PfpRef")
    private val pfpRef: StorageReference
) {
    fun login(email: String, password: String): FirebaseResult {
        val result = FirebaseResult(false, "")
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                result.message = task.exception.toString()
            } else {
                result.result = true
                result.message = LOGIN
            }
        }
        return result
    }

    suspend fun register(username: String, email: String, password: String): FirebaseResult {
        val result = FirebaseResult(false, "Not Done")
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                result.message = task.exception.toString()
            } else {
                val id = mAuth.currentUser!!.uid
                UserRef.child(id).setValue(
                    User(
                        userName = username,
                        email = email,
                        pfp = DEFAULTPFP
                    )
                ).addOnCompleteListener { task2 ->
                    if (!task2.isSuccessful) {
                        result.message = task2.exception.toString()
                    } else {
                        Log.e("enic","hello")
                        result.result = true
                        result.message = REGISTER

                    }
                }
            }
        }
        return result
    }

    fun updateProfile(username: String, pfp: Uri?, context: Context): FirebaseResult {
        val result = FirebaseResult(false, "")
        if (pfp != null) {
            pfpRef.putFile(pfp).addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    result.message = task.exception.toString()
                } else {
                    UserRef.child(mAuth.currentUser!!.uid).child("pfp").setValue(username)
                        .addOnCompleteListener { task2 ->
                            if (!task2.isSuccessful) {
                                result.message = task2.exception.toString()
                            } else {
                                result.message = UPDATE
                            }
                        }
                }
            }
        }
        return result
    }
}