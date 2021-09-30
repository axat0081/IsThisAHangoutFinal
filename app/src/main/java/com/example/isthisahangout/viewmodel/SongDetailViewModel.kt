package com.example.isthisahangout.viewmodel

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.isthisahangout.MainActivity
import com.example.isthisahangout.models.Comments
import com.example.isthisahangout.models.FirebaseVideo
import com.example.isthisahangout.models.Song
import com.example.isthisahangout.service.uploadService.FirebaseUploadService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SongDetailViewModel @Inject constructor(
    private val app: Application,
    private val state: SavedStateHandle
) : AndroidViewModel(app) {
    private val songId = MutableLiveData("any_song_id")
    val showDetails = MutableLiveData(false)
    val isBookMarked = MutableLiveData(false)
    var commentText = state.get<String>("comment_text")
        set(value) {
            field = value
            state.set("comment_text", commentText)
        }

    var commentImage = state.get<Uri>("comment_image")
        set(value) {
            field = value
            state.set("comment_image", commentImage)
        }

    private val songEventChannel = Channel<SongEvent>()
    val songEventFlow = songEventChannel.receiveAsFlow()

    fun onShowDetailsClick() {
        showDetails.value = !showDetails.value!!
    }

    fun onCommentSendClick(song: Song) {
        if (commentText.isNullOrBlank()) {
            viewModelScope.launch {
                songEventChannel.send(SongEvent.SongError("Comment cannot be blank"))
            }
        } else {
            val comment = Comments(
                username = MainActivity.username!!,
                text = commentText,
                pfp = MainActivity.userpfp!!,
                time = System.currentTimeMillis(),
                image = if (commentImage == null) null else commentImage.toString(),
                contentId = song.id
            )
            app.startService(
                Intent(app, FirebaseUploadService::class.java)
                    .putExtra(FirebaseUploadService.FIREBASE_COMMENT, comment)
                    .putExtra("path", "comment")
                    .setAction(FirebaseUploadService.ACTION_UPLOAD)
            )
            commentImage = null
        }
    }

    sealed class SongEvent {
        data class SongError(val message: String) : SongEvent()
    }

}