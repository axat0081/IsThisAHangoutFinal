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
import com.example.isthisahangout.models.Message
import com.example.isthisahangout.pagingsource.MessagesPagingSource
import com.example.isthisahangout.room.chat.ChatDao
import com.example.isthisahangout.utils.asFlow
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val state: SavedStateHandle,
    mAuth: FirebaseAuth,
    @Named("MessagesRef") private val messagesRef: CollectionReference,
    private val chatDao: ChatDao,
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

    val isLoading = MutableStateFlow(false)
    private val lastReceivedMessage: MutableStateFlow<FirebaseMessage?> = MutableStateFlow(null)
    val messages = Pager(PagingConfig(20)) {
        chatDao.getMessagesPaged()
    }.flow.cachedIn(viewModelScope)

    val newMessagesFlow: Flow<List<FirebaseMessage>> =
        messagesRef.orderBy("time",Query.Direction.DESCENDING)
            .whereGreaterThanOrEqualTo("time",Timestamp(Date(System.currentTimeMillis())))
            .asFlow().map {
                it.toObjects(FirebaseMessage::class.java)
            }

    init {
        viewModelScope.launch {
            val lastMessage = chatDao.getLastMessage()
            if (lastMessage != null) {
                isLoading.value = true
                messagesRef.orderBy("time")
                    .startAfter(lastMessage)
                    .get().addOnSuccessListener { snapshots ->
                        val serverUnreceivedMessages =
                            snapshots.toObjects(FirebaseMessage::class.java)
                        if (serverUnreceivedMessages.isNotEmpty()) {
                            lastReceivedMessage.value = serverUnreceivedMessages.last()
                        }
                        viewModelScope.launch {
                            chatDao.insertMessages(serverUnreceivedMessages.map { firebaseMessage ->
                                Message(
                                    id = firebaseMessage.id!!,
                                    text = firebaseMessage.text!!,
                                    senderId = firebaseMessage.senderId!!,
                                    username = firebaseMessage.username!!,
                                    time = firebaseMessage.time.toDate().time
                                )
                            })
                            Log.e("message_89", "hey there")
                            isLoading.value = false
                        }
                    }.addOnFailureListener {
                        isLoading.value = false
                        viewModelScope.launch {
                            messageChannel.send(MessagingEvent.MessageFetchFailure)
                        }
                    }
            } else {
                isLoading.value = true
                messagesRef.orderBy("time")
                    .get()
                    .addOnSuccessListener { snapshots ->
                        val serverUnreceivedMessages =
                            snapshots.toObjects(FirebaseMessage::class.java)
                        if (serverUnreceivedMessages.isNotEmpty()) {
                            lastReceivedMessage.value = serverUnreceivedMessages.last()
                        }
                        viewModelScope.launch {
                            chatDao.insertMessages(serverUnreceivedMessages.map { firebaseMessage ->
                                Message(
                                    id = firebaseMessage.id!!,
                                    text = firebaseMessage.text!!,
                                    senderId = firebaseMessage.senderId!!,
                                    username = firebaseMessage.username!!,
                                    time = firebaseMessage.time.toDate().time
                                )
                            })
                            Log.e("message_118", "hey there")
                            isLoading.value = false
                        }
                    }.addOnFailureListener {
                        isLoading.value = false
                        viewModelScope.launch {
                            messageChannel.send(MessagingEvent.MessageFetchFailure)
                        }
                    }
            }
        }
    }

    fun onSendClick() {
        if (text?.isNotEmpty() == true) {
            viewModelScope.launch {
                val docRef = messagesRef.document()
                docRef.set(
                    FirebaseMessage(
                        senderId = userId,
                        text = text,
                        id = docRef.id,
                        username = MainActivity.userName
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
        object MessageFetchSuccess : MessagingEvent()
        object MessageFetchFailure : MessagingEvent()
    }
}