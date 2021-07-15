package com.example.isthisahangout.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.example.isthisahangout.MainActivity
import com.example.isthisahangout.models.FirebaseMessage
import com.example.isthisahangout.pagingsource.MessagesPagingSource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val state: SavedStateHandle,
    mAuth: FirebaseAuth,
    @Named("MessagesRef") private val messagesRef: CollectionReference,
) : ViewModel() {

    private val messageChannel = Channel<MessagingEvent>()
    val messageEventFlow = messageChannel.receiveAsFlow()
    var text = state.get<String>("message_text")
        set(value) {
            field = value
            state.set("message_text", text)
        }
    private val userId = mAuth.currentUser!!.uid
    val messagesFlow = Pager(PagingConfig(20)) {
        MessagesPagingSource()
    }.flow.cachedIn(viewModelScope)

    fun onSendClick() {
        Log.e("Message", text.toString())
        if (text?.isNotEmpty() == true) {
            viewModelScope.launch {
                val docRef = messagesRef.document()
                docRef.set(
                    FirebaseMessage(
                        senderId = userId,
                        text = text,
                        id = docRef.id,
                        username = MainActivity.username
                    )
                ).addOnFailureListener {
                    viewModelScope.launch {
                        messageChannel.send(MessagingEvent.MessageError(it.localizedMessage!!))
                    }
                }
            }
        }
    }


    sealed class MessagingEvent {
        data class MessageError(val message: String) : MessagingEvent()
    }
}