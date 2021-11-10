package com.example.isthisahangout.utils

import com.example.isthisahangout.adapter.whereAfterTimestamp
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

val chatCollectionReference by lazy {
    FirebaseFirestore
        .getInstance()
        .collection("Messages")
}

val messagesQuery by lazy {
    chatCollectionReference
        .orderBy("time", Query.Direction.DESCENDING)
}

val newMessagesQuery by lazy {
    messagesQuery.whereAfterTimestamp()
}

val postCollectionReference by lazy {
    FirebaseFirestore.getInstance()
        .collection("Posts")
}

val postsQuery by lazy {
    postCollectionReference
        .orderBy("time", Query.Direction.DESCENDING)
}

val newPostsQuery by lazy {
    postsQuery.whereAfterTimestamp()
}

val videoCollectionReference by lazy {
    FirebaseFirestore.getInstance()
        .collection("Videos")
}

val videoQuery by lazy {
    videoCollectionReference.orderBy("time", Query.Direction.DESCENDING)
}

val newVideoQuery by lazy {
    videoQuery.whereAfterTimestamp()
}

val songCollectionReference by lazy {
    FirebaseFirestore.getInstance()
        .collection("Songs")
}
val songQuery by lazy {
    songCollectionReference.orderBy("time", Query.Direction.DESCENDING)
}

val newSongQuery by lazy {
    songQuery.whereAfterTimestamp()
}