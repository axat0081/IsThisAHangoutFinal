package com.example.isthisahangout.pagingsource

import android.util.Log
import androidx.paging.PagingSource
import com.example.isthisahangout.models.Video
import com.example.isthisahangout.utils.videoQuery
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.tasks.await

class VideosPagingSource : PagingSource<QuerySnapshot, Video>() {
    override suspend fun load(params: LoadParams<QuerySnapshot>): LoadResult<QuerySnapshot, Video> {
        return try {
            val currentPage = params.key ?: videoQuery.limit(10).get().await()
            val lastDocumentSnapshot = currentPage.documents[currentPage.size() - 1]
            val nextPage = videoQuery.limit(10).startAfter(lastDocumentSnapshot)
                .get().await()
            Log.e("Posts", currentPage.documents.toString())
            LoadResult.Page(
                data = currentPage.toObjects(Video::class.java),
                prevKey = null,
                nextKey = nextPage
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}