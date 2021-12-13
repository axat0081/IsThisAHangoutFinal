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
import com.example.isthisahangout.models.User
import com.example.isthisahangout.service.uploadService.FirebaseUploadService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.CollectionReference
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class FirebaseAuthViewModel @Inject constructor(
    private val app: Application,
    val state: SavedStateHandle,
    private val mAuth: FirebaseAuth,
    @Named("UserDataRef") private val userRef: DatabaseReference,
    @Named("UserRef") private val userCollectionRef: CollectionReference
) : AndroidViewModel(app) {

    companion object {
        const val LOGIN = "Login Successful"
        const val REGISTER = "Account Created"
        const val DEFAULTPFP =
            "https://firebasestorage.googleapis.com/v0/b/isthisahangout-61d93.appspot.com/o/pfp%2Fpfp_placeholder.jpg?alt=media&token=35fa14c3-6451-41f6-a8be-448a59996f75"
        const val DEFAULTHEADER =
            "https://firebasestorage.googleapis.com/v0/b/isthisahangout-61d93.appspot.com/o/pfp%2Fpfp_placeholder.jpg?alt=media&token=35fa14c3-6451-41f6-a8be-448a59996f75"
    }

    private val _userId = MutableStateFlow("")
    val userId: StateFlow<String> = _userId

    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username

    private val _userPfp = MutableStateFlow("")
    val userPfp: StateFlow<String> = _userPfp

    private val _userHeader = MutableStateFlow("")
    val userHeader: StateFlow<String> = _userHeader

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
            val type = intent.getStringExtra(FirebaseUploadService.TYPE_OF_UPLOAD)
            if (type == "header")
                updateHeaderResult(intent)
            else if (type == "pfp")
                updatePfpResult(intent)
        }
    }
    private val authChannel = Channel<AuthEvent>()
    val loginFlow = authChannel.receiveAsFlow()
    val registrationFlow = authChannel.receiveAsFlow()
    val profileFlow = authChannel.receiveAsFlow()
    val imageTag = MutableStateFlow("pfp")
    fun onLoginClick() {
        if (loginPassword.isBlank()) {
            viewModelScope.launch {
                authChannel.send(AuthEvent.LoginFailure("Password cannot be empty"))
            }
            return
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
                                                    _userId.value = mAuth.currentUser!!.uid
                                                    _username.value =
                                                        snapshot.child("userName").value.toString()
                                                    _userPfp.value =
                                                        snapshot.child("pfp").value.toString()
                                                    _userHeader.value =
                                                        snapshot.child("header").value.toString()
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
                    userCollectionRef.document(mAuth.currentUser!!.uid)
                        .get().addOnCompleteListener {
                            if (it.isSuccessful) {
                                val snapshot = it.result
                                _userId.value = mAuth.currentUser!!.uid
                                _username.value = snapshot?.getString("userName") ?: ""
                                _userPfp.value = snapshot?.getString("pfp") ?: ""
                                _userHeader.value = snapshot?.getString("header") ?: ""
                                viewModelScope.launch {
                                    authChannel.send(
                                        AuthEvent.LoginSuccess(
                                            LOGIN
                                        )
                                    )
                                }
                            } else {
                                mAuth.signOut()
                                var msg = "An error occurred"
                                if (it.exception != null) {
                                    msg = it.exception!!.localizedMessage ?: "An error occurred"
                                }
                                Log.e("Error", "Hey1")
                                viewModelScope.launch {
                                    authChannel.send(
                                        AuthEvent.LoginFailure(
                                            msg
                                        )
                                    )
                                }
                            }
                        }
                } else {
                    viewModelScope.launch {
                        Log.e("Error", "Hey2")
                        authChannel.send(AuthEvent.LoginFailure(login.exception.toString()))
                    }
                }
            }

    }

    fun onRegistrationClick() {
        if (registrationEmail.isBlank()) {
            viewModelScope.launch {
                authChannel.send(AuthEvent.RegistrationFailure("Email cannot be empty"))
            }
            return
        }
        if (registrationPassword.isBlank()) {
            viewModelScope.launch {
                authChannel.send(AuthEvent.RegistrationFailure("Password cannot be empty"))
            }
            return
        }
        mAuth.createUserWithEmailAndPassword(registrationEmail, registrationPassword)
            .addOnCompleteListener { registration ->
                if (registration.isSuccessful) {
                    userCollectionRef.document(mAuth.currentUser!!.uid)
                        .set(
                            User(
                                email = registrationEmail,
                                pfp = DEFAULTPFP,
                                userName = registrationUsername,
                                header = DEFAULTHEADER
                            )
                        ).addOnCompleteListener { task ->

                            if (task.isSuccessful) {
                                _username.value = registrationUsername
                                _userPfp.value = DEFAULTPFP
                                _userId.value = mAuth.currentUser!!.uid
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
                    .putExtra(FirebaseUploadService.EXTRA_FILE_URI, profileHeader)
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
                _userPfp.value = profilePfp.toString()
            }
        }
    }

    fun updateHeaderResult(intent: Intent) {
        viewModelScope.launch {
            profileHeader = intent.getParcelableExtra(FirebaseUploadService.EXTRA_DOWNLOAD_URL)
            if (profileHeader == null) {
                authChannel.send(AuthEvent.UpdateProfileFailure("Image could not be updated"))
            } else {
                authChannel.send(AuthEvent.UpdateProfileSuccess("Image updates", profileHeader!!))
                _userHeader.value = profileHeader.toString()
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