package com.example.isthisahangout.utils

import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

fun Query.asFlow() = callbackFlow {
    val registration = addSnapshotListener { snapshots, error ->
        if (error != null) {
            close(error)
        } else {
            if (snapshots != null) {
                this.trySend(snapshots).isSuccess
            }
        }
    }
    awaitClose { registration.remove() }
}