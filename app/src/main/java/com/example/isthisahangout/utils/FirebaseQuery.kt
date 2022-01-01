package com.example.isthisahangout.utils

import android.util.Log
import com.example.isthisahangout.MainActivity
import com.example.isthisahangout.adapter.whereAfterTimestamp
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

val firebaseAuth by lazy{
    FirebaseAuth.getInstance()
}

val chatCollectionReference by lazy {
    FirebaseFirestore
        .getInstance()
        .collection("Messages")
}

val messagesQuery by lazy {
    chatCollectionReference
        .orderBy("time",Query.Direction.DESCENDING)
        .whereLessThan("time", Timestamp.now())
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

val firebaseAuth by lazy {
    FirebaseAuth.getInstance()
}
