package com.example.isthisahangout.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.Timestamp

@Entity(tableName = "messages")
data class Message(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val senderId: String,
    val time: Long = System.currentTimeMillis(),
    val username: String
)

data class FirebaseMessage(
    val id: String? =null,
    val text: String? = "",
    val senderId: String? = "",
    val time: Timestamp = Timestamp.now(),
    val username: String? = "",
)