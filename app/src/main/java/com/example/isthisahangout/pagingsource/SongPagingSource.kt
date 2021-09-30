package com.example.isthisahangout.pagingsource

import android.util.Log
import androidx.paging.PagingSource
import com.example.isthisahangout.models.Song
import com.example.isthisahangout.utils.songQuery
import com.example.isthisahangout.utils.videoQuery
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.tasks.await

class SongPagingSource : PagingSource<QuerySnapshot, Song>() {
    override suspend fun load(params: LoadParams<QuerySnapshot>): LoadResult<QuerySnapshot, Song> {
        return try {
            val currentPage = params.key ?: songQuery.limit(10).get().await()
            val lastDocumentSnapshot = currentPage.documents[currentPage.size() - 1]
            val nextPage = songQuery.limit(10).startAfter(lastDocumentSnapshot)
                .get().await()
            LoadResult.Page(
                data = currentPage.toObjects(Song::class.java),
                prevKey = null,
                nextKey = nextPage
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}