package com.example.isthisahangout.pagingsource

import android.util.Log
import androidx.paging.PagingSource
import com.example.isthisahangout.models.FirebaseMessage
import com.example.isthisahangout.utils.messagesQuery
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.tasks.await

class MessagesPagingSource :
    PagingSource<QuerySnapshot, FirebaseMessage>() {

    override suspend fun load(params: LoadParams<QuerySnapshot>): LoadResult<QuerySnapshot, FirebaseMessage> {
        return try {
            val currentPage = params.key ?: messagesQuery.limit(5).get().await()
            val lastDocumentSnapshot = currentPage.documents[currentPage.size() - 1]
            val nextPage = messagesQuery.limit(5).startAfter(lastDocumentSnapshot)
                .get().await()
            Log.e("Messages",currentPage.documents.toString())
            LoadResult.Page(
                data = currentPage.toObjects(FirebaseMessage::class.java),
                prevKey = null,
                nextKey = nextPage
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}