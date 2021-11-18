package com.example.isthisahangout.viewmodel

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.isthisahangout.MainActivity
import com.example.isthisahangout.models.User
import com.example.isthisahangout.service.uploadService.FirebaseUploadService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class FirebaseAuthViewModel @Inject constructor(
    private val app: Application,
    val state: SavedStateHandle,
    private val mAuth: FirebaseAuth,
    @Named("UserRef") private val userRef: DatabaseReference
) : AndroidViewModel(app) {

    companion object {
        const val LOGIN = "Login Successful"
        const val REGISTER = "Account Created"
        const val DEFAULTPFP =
            "https://firebasestorage.googleapis.com/v0/b/isthisahangout-61d93.appspot.com/o/pfp%2Fpfp_placeholder.jpg?alt=media&token=35fa14c3-6451-41f6-a8be-448a59996f75"
        const val DEFAULTHEADER =
            "https://firebasestorage.googleapis.com/v0/b/isthisahangout-61d93.appspot.com/o/pfp%2Fpfp_placeholder.jpg?alt=media&token=35fa14c3-6451-41f6-a8be-448a59996f75"
    }

    var loginEmail = state.get<String>("loginEmail") ?: ""
        set(value) {
            field = value
            state.set("loginEmail", loginEmail)
        }
    var loginPassword = state.get<String>("loginPassword") ?: ""
        set(value) {
            field = value
            state.set("loginPassword", loginPassword)
        }
    var registrationEmail = state.get<String>("registrationEmail") ?: ""
        set(value) {
            field = value
            state.set("registrationEmail", registrationEmail)
        }
    var registrationPassword = state.get<String>("registrationPassword") ?: ""
        set(value) {
            field = value
            state.set("registrationPassword", registrationPassword)
        }
    var registrationUsername = state.get<String>("registrationUsername") ?: ""
        set(value) {
            field = value
            state.set("registrationUsername", registrationUsername)
        }
    var profilePfp: Uri? = state.get<Uri>("profilePfp")
        set(value) {
            field = value
            state.set("profilePfp", profilePfp)
        }
    var profileHeader: Uri? = state.get<Uri>("profileHeader")
        set(value) {
            field = value
            state.set("profileHeader", profileHeader)
        }
    val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            Log.e("FirebaseAuthViewModel", "onReceive:$intent")
            updatePfpResult(intent)
        }
    }
    private val authChannel = Channel<AuthEvent>()
    val loginFlow = authChannel.receiveAsFlow()
    val registrationFlow = authChannel.receiveAsFlow()
    val profileFlow = authChannel.receiveAsFlow()
    fun onLoginClick() {
        if (loginPassword.isBlank()) {
            viewModelScope.launch {
                authChannel.send(AuthEvent.LoginFailure("Password cannot be empty"))
            }
        }
        if (loginEmail.isBlank()) {
            if (mAuth.currentUser == null) {
                viewModelScope.launch {
                    authChannel.send(AuthEvent.LoginFailure("Email cannot be empty"))
                }
            } else {
                userRef.child(mAuth.currentUser!!.uid).child("email")
                    .addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            loginEmail = snapshot.value.toString()
                            mAuth.signInWithEmailAndPassword(loginEmail, loginPassword)
                                .addOnCompleteListener { login ->
                                    if (login.isSuccessful) {
                                        userRef.child(mAuth.currentUser!!.uid)
                                            .addValueEventListener(object : ValueEventListener {
                                                override fun onDataChange(snapshot: DataSnapshot) {
                                                    MainActivity.userId = mAuth.currentUser!!.uid
                                                    MainActivity.userIdObv.value =
                                                        MainActivity.userId ?: ""
                                                    MainActivity.username =
                                                        snapshot.child("userName").value.toString()
                                                    MainActivity.userNameObv.value =
                                                        MainActivity.username ?: ""
                                                    MainActivity.userpfp =
                                                        snapshot.child("pfp").value.toString()
                                                    MainActivity.userPfpObv.value =
                                                        MainActivity.userpfp ?: ""
                                                    viewModelScope.launch {
                                                        authChannel.send(
                                                            AuthEvent.LoginSuccess(
                                                                LOGIN
                                                            )
                                                        )
                                                    }
                                                }

                                                override fun onCancelled(error: DatabaseError) {
                                                    mAuth.signOut()
                                                    viewModelScope.launch {
                                                        authChannel.send(
                                                            AuthEvent.LoginFailure(
                                                                error.message
                                                            )
                                                        )
                                                    }
                                                }
                                            })
                                    } else {
                                        viewModelScope.launch {
                                            authChannel.send(AuthEvent.LoginFailure(login.exception.toString()))
                                        }
                                    }
                                }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            viewModelScope.launch {
                                authChannel.send(AuthEvent.LoginFailure(error.message))
                            }
                        }
                    })
            }
            return
        }
        mAuth.signInWithEmailAndPassword(loginEmail, loginPassword)
            .addOnCompleteListener { login ->
                if (login.isSuccessful) {
                    userRef.child(mAuth.currentUser!!.uid)
                        .addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                MainActivity.userId = mAuth.currentUser!!.uid
                                MainActivity.userIdObv.value =
                                    MainActivity.userId ?: ""
                                MainActivity.username =
                                    snapshot.child("userName").value.toString()
                                MainActivity.userNameObv.value =
                                    MainActivity.username ?: ""
                                MainActivity.userpfp =
                                    snapshot.child("pfp").value.toString()
                                MainActivity.userPfpObv.value =
                                    MainActivity.userpfp ?: ""
                                MainActivity.userHeader = snapshot.child("header").value.toString()
                                MainActivity.userHeaderObv.value =
                                    MainActivity.userHeader ?: ""
                                viewModelScope.launch {
                                    authChannel.send(
                                        AuthEvent.LoginSuccess(
                                            LOGIN
                                        )
                                    )
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                mAuth.signOut()
                                viewModelScope.launch {
                                    authChannel.send(
                                        AuthEvent.LoginFailure(
                                            error.message
                                        )
                                    )
                                }
                            }
                        })
                } else {
                    viewModelScope.launch {
                        authChannel.send(AuthEvent.LoginFailure(login.exception.toString()))
                    }
                }
            }

    }

    fun onRegistrationClick() {
        mAuth.createUserWithEmailAndPassword(registrationEmail, registrationPassword)
            .addOnCompleteListener { registration ->
                if (registration.isSuccessful) {
                    userRef.child(mAuth.currentUser!!.uid).setValue(
                        User(
                            email = registrationEmail,
                            pfp = DEFAULTPFP,
                            userName = registrationUsername,
                            header = DEFAULTHEADER
                        )
                    ).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            viewModelScope.launch {
                                authChannel.send(AuthEvent.RegistrationSuccess(REGISTER))
                            }
                        } else {
                            viewModelScope.launch {
                                authChannel.send(AuthEvent.RegistrationFailure(task.exception.toString()))
                            }
                        }
                    }
                } else {
                    viewModelScope.launch {
                        authChannel.send(AuthEvent.RegistrationFailure(registration.exception.toString()))
                    }
                }
            }
    }


    fun onUpdatePfpClick() {
        if (profilePfp == null) {
            viewModelScope.launch {
                authChannel.send(AuthEvent.UpdateProfileFailure("Please select a profile picture"))
            }
            return
        }
        viewModelScope.launch {
            app.startService(
                Intent(app, FirebaseUploadService::class.java)
                    .putExtra(FirebaseUploadService.EXTRA_FILE_URI, profilePfp)
                    .putExtra("path", "pfp")
                    .setAction(FirebaseUploadService.ACTION_UPLOAD).apply {
                    }
            )
        }
    }

    fun onUpdateHeaderClick() {
        if (profileHeader == null) {
            viewModelScope.launch {
                authChannel.send(AuthEvent.UpdateProfileFailure("Please select a header for your profile"))
            }
            return
        }
        viewModelScope.launch {
            app.startService(
                Intent(app, FirebaseUploadService::class.java)
                    .putExtra(FirebaseUploadService.EXTRA_FILE_URI, profilePfp)
                    .putExtra("path", "header")
                    .setAction(FirebaseUploadService.ACTION_UPLOAD).apply {
                    }
            )
        }
    }

    fun updatePfpResult(intent: Intent) {
        viewModelScope.launch {
            profilePfp = intent.getParcelableExtra(FirebaseUploadService.EXTRA_DOWNLOAD_URL)
            if (profilePfp == null) {
                authChannel.send(AuthEvent.UpdateProfileFailure("Image could not be updated"))
            } else {
                authChannel.send(AuthEvent.UpdateProfileSuccess("Image updates", profilePfp!!))
            }
        }
    }

    sealed class AuthEvent {
        data class LoginSuccess(val message: String) : AuthEvent()
        data class LoginFailure(val message: String) : AuthEvent()
        data class RegistrationSuccess(val message: String) : AuthEvent()
        data class RegistrationFailure(val message: String) : AuthEvent()
        data class UpdateProfileSuccess(val message: String, val uri: Uri) : AuthEvent()
        data class UpdateProfileFailure(val message: String) : AuthEvent()
    }
}