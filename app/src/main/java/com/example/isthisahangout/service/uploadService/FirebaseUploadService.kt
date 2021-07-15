package com.example.isthisahangout.service.uploadService

import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.IBinder
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.isthisahangout.MainActivity
import com.example.isthisahangout.models.Comments
import com.example.isthisahangout.models.FirebasePost
import com.example.isthisahangout.models.Song
import com.example.isthisahangout.models.Video
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.component1
import com.google.firebase.storage.ktx.component2
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
@AndroidEntryPoint
class FirebaseUploadService : BaseService() {

    @Inject
    @Named("UserRef")
    lateinit var userRef: DatabaseReference

    @Inject
    @Named("PfpRef")
    lateinit var pfpRef: StorageReference

    @Inject
    @Named("PostImagesRef")
    lateinit var postImageRef: StorageReference

    @Inject
    @Named("ThumbnailRef")
    lateinit var thumbnailRef: StorageReference

    @Inject
    @Named("VideoUrlRef")
    lateinit var videoUrleRef: StorageReference

    @Inject
    @Named("SongUrlRef")
    lateinit var songUrlRef: StorageReference

    @Inject
    @Named("CommentsUrlRef")
    lateinit var commentsUrlRef: StorageReference

    @Inject
    @Named("PostsRef")
    lateinit var postCollectionRef: CollectionReference

    @Inject
    @Named("SongRef")
    lateinit var songCollectionRef: CollectionReference

    @Inject
    @Named("VideoRef")
    lateinit var videoCollectionRef: CollectionReference

    @Inject
    @Named("CommentsRef")
    lateinit var commentsCollectionRef: CollectionReference

    @Inject
    lateinit var mAuth: FirebaseAuth

    companion object {
        private const val TAG = "MyUploadService"
        const val ACTION_UPLOAD = "action_upload"
        const val UPLOAD_COMPLETED = "upload_completed"
        const val UPLOAD_ERROR = "upload_error"

        const val EXTRA_FILE_URI = "extra_file_uri"
        const val EXTRA_DOWNLOAD_URL = "extra_download_url"
        const val FIREBASE_POST = "firebase_post_upload"
        const val FIREBASE_SONG = "firebase_song_upload"
        const val FIREBASE_VIDEO = "firebase_video_upload"
        const val FIREBASE_COMMENT = "firebase_comment_upload"

        const val SUCCESS = "Uploaded to database"
        const val FAILURE = "Aw snap , an error occurred"
        const val CAPTION = "Uploading image..."
        const val CREATE_POST_CAPTION = "Uploading post ...."
        const val UPLOAD_SONG_CAPTION = "Uploading song....."
        const val UPLOAD_VIDEO_CAPTION = "Uploading video....."
        const val UPLOAD_COMMENT_CAPTION = "Posting Comment...."

        const val defaultImage =
            "https://firebasestorage.googleapis.com/v0/b/isthisahangout-61d93.appspot.com/o/pfp%2Fpfp_placeholder.jpg?alt=media&token=35fa14c3-6451-41f6-a8be-448a59996f75"

        val intentFilter: IntentFilter
            get() {
                val filter = IntentFilter()
                filter.addAction(UPLOAD_COMPLETED)
                filter.addAction(UPLOAD_ERROR)

                return filter
            }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.e("FirebaseAuthViewModel2", intent.toString())
        if (ACTION_UPLOAD == intent.action) {
            when (intent.getStringExtra("path")!!) {
                "pfp" -> {
                    val fileUri = intent.getParcelableExtra<Uri>(EXTRA_FILE_URI)!!
                    uploadPfpFromUri(fileUri)
                }
                "postImage" -> {
                    val post = intent.getParcelableExtra<FirebasePost>(FIREBASE_POST)!!
                    uploadPost(post)
                }
                "song" -> {
                    val song = intent.getParcelableExtra<Song>(FIREBASE_SONG)!!
                    Log.e("Music", song.title!!)
                    uploadSong(song)
                }
                "video" -> {
                    val video = intent.getParcelableExtra<Video>(FIREBASE_VIDEO)!!
                    uploadVideo(video)
                }

                "comment" ->{
                    val comment = intent.getParcelableExtra<Comments>(FIREBASE_COMMENT)!!
                    uploadComment(comment)
                }
            }
        }

        return START_REDELIVER_INTENT
    }

    private fun uploadPfpFromUri(fileUri: Uri) {
        Log.e("FirebaseAuthViewModel4", fileUri.toString())
        taskStarted()
        showProgressNotification(CAPTION, 0, 0, false)

        fileUri.lastPathSegment?.let {
            val photoRef = pfpRef
            photoRef.child(it).putFile(fileUri)
                .addOnProgressListener { (bytesTransferred, totalByteCount) ->
                    showProgressNotification(
                        CAPTION,
                        bytesTransferred,
                        totalByteCount,
                        false
                    )
                }.continueWithTask { task ->
                    if (!task.isSuccessful) {
                        throw task.exception!!
                    }

                    // Request the public download URL
                    photoRef.child(it).downloadUrl
                }.addOnSuccessListener { downloadUri ->
                    // Upload succeeded

                    userRef.child(MainActivity.userId!!).child("pfp").setValue(
                        downloadUri.toString()
                    )
                        .addOnCompleteListener { updateTask ->
                            if (updateTask.isSuccessful) {
                                Log.d(TAG, "uploadFromUri: getDownloadUri success")

                                // [START_EXCLUDE]
                                broadcastUploadFinished(downloadUri, fileUri)
                                showUploadFinishedNotification(downloadUri, fileUri)
                                taskCompleted()
                                // [END_EXCLUDE]
                            } else {
                                Log.w(TAG, "uploadFromUri:onFailure", updateTask.exception)

                                // [START_EXCLUDE]
                                broadcastUploadFinished(null, fileUri)
                                showUploadFinishedNotification(null, fileUri)
                                taskCompleted()
                                // [END_EXCLUDE]
                            }
                        }
                }.addOnFailureListener { exception ->
                    // Upload failed
                    Log.w(TAG, "uploadFromUri:onFailure", exception)

                    // [START_EXCLUDE]
                    broadcastUploadFinished(null, fileUri)
                    showUploadFinishedNotification(null, fileUri)
                    taskCompleted()
                    // [END_EXCLUDE]
                }
        }
    }

    private fun uploadComment(comments: Comments){
        taskStarted()
        showProgressNotification(UPLOAD_COMMENT_CAPTION, 0, 0, true)
        if(comments.image !=null){
            val image = Uri.parse(comments.image)
            image.lastPathSegment?.let{
                commentsUrlRef.child(it).putFile(image)
                    .addOnProgressListener { (bytesTransferred, totalByteCount) ->
                        showProgressNotification(
                            UPLOAD_COMMENT_CAPTION,
                            bytesTransferred,
                            totalByteCount,
                            true
                        )
                    }.continueWithTask { task->
                        if (!task.isSuccessful) {
                            throw task.exception!!
                        }
                        commentsUrlRef.child(it).downloadUrl
                    }.addOnSuccessListener { downloadUri->
                        val commentsRef = commentsCollectionRef.document(comments.contentId!!).collection("comments")
                        val id = commentsRef.document().id
                        commentsRef.document(id).set(
                            Comments(
                                contentId = comments.contentId,
                                pfp = comments.pfp,
                                username = comments.username,
                                time = comments.time,
                                text = comments.text,
                                image = downloadUri.toString()
                            )
                        ).addOnSuccessListener {
                            broadcastUploadFinished(downloadUri, image)
                            showUploadFinishedNotification(downloadUri, image)
                            taskCompleted()
                        }.addOnFailureListener { exception ->
                            // Upload failed
                            Log.w(TAG, "uploadFromUri:onFailure", exception)

                            // [START_EXCLUDE]
                            broadcastUploadFinished(null, image)
                            showUploadFinishedNotification(null, image)
                            taskCompleted()
                            // [END_EXCLUDE]
                        }
                    }.addOnFailureListener { exception ->
                        // Upload failed
                        Log.w(TAG, "uploadFromUri:onFailure", exception)

                        // [START_EXCLUDE]
                        broadcastUploadFinished(null, image)
                        showUploadFinishedNotification(null, image)
                        taskCompleted()
                    }
            }
        } else {
            val commentsRef = commentsCollectionRef.document(comments.contentId!!).collection("comments")
            val id = commentsRef.document().id
            commentsRef.document(id).set(
                Comments(
                    contentId = comments.contentId,
                    pfp = comments.pfp,
                    username = comments.username,
                    time = comments.time,
                    text = comments.text,
                    image = null
                )
            ).addOnSuccessListener {
                broadcastUploadFinished(Uri.parse(defaultImage), Uri.parse(defaultImage))
                showUploadFinishedNotification(Uri.parse(defaultImage), Uri.parse(defaultImage))
                taskCompleted()
            }.addOnFailureListener {
                // Upload failed
                broadcastUploadFinished(null, Uri.parse(defaultImage))
                showUploadFinishedNotification(null, Uri.parse(defaultImage))
                taskCompleted()
                // [END_EXCLUDE]
            }
        }
    }

    private fun uploadPost(post: FirebasePost) {
        taskStarted()
        showProgressNotification(CREATE_POST_CAPTION, 0, 0, true)
        if (post.image != null) {
            //upload image to firebase storage
            val image: Uri = Uri.parse(post.image)
            image.lastPathSegment?.let {
                postImageRef.child(it).putFile(image)
                    .addOnProgressListener { (bytesTransferred, totalByteCount) ->
                        showProgressNotification(
                            CREATE_POST_CAPTION,
                            bytesTransferred,
                            totalByteCount,
                            true
                        )
                    }.continueWithTask { task ->
                        if (!task.isSuccessful) {
                            throw task.exception!!
                        }
                        postImageRef.child(it).downloadUrl
                    }.addOnSuccessListener { downloadUri ->
                        val id = postCollectionRef.document().id
                        postCollectionRef.document(id).set(
                            FirebasePost(
                                id = id,
                                username = post.username,
                                text = post.text,
                                likes = 0,
                                time = post.time,
                                pfp = post.pfp,
                                title = post.title,
                                image = downloadUri.toString()
                            )
                        ).addOnSuccessListener {
                            // Upload succeeded
                            Log.d(TAG, "uploadFromUri: getDownloadUri success")

                            // [START_EXCLUDE]
                            broadcastUploadFinished(downloadUri, image)
                            showUploadFinishedNotification(downloadUri, image)
                            taskCompleted()
                            // [END_EXCLUDE]
                        }.addOnFailureListener { exception ->
                            // Upload failed
                            Log.w(TAG, "uploadFromUri:onFailure", exception)

                            // [START_EXCLUDE]
                            broadcastUploadFinished(null, image)
                            showUploadFinishedNotification(null, image)
                            taskCompleted()
                            // [END_EXCLUDE]
                        }
                    }.addOnFailureListener { exception ->
                        // Upload failed
                        Log.w(TAG, "uploadFromUri:onFailure", exception)

                        // [START_EXCLUDE]
                        broadcastUploadFinished(null, image)
                        showUploadFinishedNotification(null, image)
                        taskCompleted()
                    }
            }
        } else {
            //Post does not have image
            val id = postCollectionRef.document().id
            postCollectionRef.document(id).set(
                FirebasePost(
                    id = id,
                    username = post.username,
                    text = post.text,
                    likes = 0,
                    time = post.time,
                    pfp = post.pfp,
                    title = post.title,
                    image = null
                )
            ).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "uploadFromUri: getDownloadUri success")

                    // [START_EXCLUDE]
                    broadcastUploadFinished(Uri.parse(defaultImage), Uri.parse(defaultImage))
                    showUploadFinishedNotification(Uri.parse(defaultImage), Uri.parse(defaultImage))
                    taskCompleted()
                } else {
                    val exception = task.exception
                    Log.w(TAG, "uploadFromUri:onFailure", exception)

                    // [START_EXCLUDE]
                    broadcastUploadFinished(null, Uri.parse(defaultImage))
                    showUploadFinishedNotification(null, Uri.parse(defaultImage))
                    taskCompleted()
                }
            }
        }
    }

    private fun uploadSong(song: Song) {
        taskStarted()
        showProgressNotification(UPLOAD_SONG_CAPTION, 0, 0, true)
        val songUrl: Uri = Uri.parse(song.url)
        Log.e("Music", songUrl.toString())
        songUrl.lastPathSegment?.let {
            songUrlRef.child(it).putFile(songUrl)
                .addOnProgressListener { (bytesTransferred, totalByteCount) ->
                    showProgressNotification(
                        UPLOAD_SONG_CAPTION,
                        bytesTransferred,
                        totalByteCount,
                        true
                    )
                }.continueWithTask { task ->
                    if (!task.isSuccessful) {
                        throw task.exception!!
                    }
                    songUrlRef.child(it).downloadUrl
                }.addOnFailureListener { songUploadException ->
                    Log.w(TAG, "uploadFromUri:onFailure", songUploadException)
                    // [START_EXCLUDE]
                    broadcastUploadFinished(null, Uri.parse(song.url))
                    showUploadFinishedNotification(null, Uri.parse(song.url))
                    taskCompleted()
                }.addOnSuccessListener { songUri ->
                    val thumbnailUrl = Uri.parse(song.thumbnail)
                    thumbnailUrl.lastPathSegment?.let {
                        thumbnailRef.child(it).putFile(thumbnailUrl)
                            .addOnProgressListener { (bytesTransferred1, totalByteCount1) ->
                                showProgressNotification(
                                    UPLOAD_SONG_CAPTION,
                                    bytesTransferred1,
                                    totalByteCount1,
                                    true
                                )
                            }.continueWithTask { task ->
                                if (!task.isSuccessful) {
                                    throw task.exception!!
                                }
                                thumbnailRef.child(it).downloadUrl
                            }.addOnFailureListener { thumbnailUploadException ->
                                Log.w(TAG, "uploadFromUri:onFailure", thumbnailUploadException)
                                // [START_EXCLUDE]
                                broadcastUploadFinished(null, songUri)
                                showUploadFinishedNotification(null, songUri)
                                taskCompleted()
                            }.addOnSuccessListener { thumbnailUri ->
                                val id = songCollectionRef.document().id
                                songCollectionRef.document(id).set(
                                    Song(
                                        id = id,
                                        text = song.text,
                                        title = song.title,
                                        time = song.time,
                                        pfp = song.pfp,
                                        username = song.username,
                                        thumbnail = thumbnailUri.toString(),
                                        url = songUri.toString()
                                    )
                                ).addOnCompleteListener { songUpload ->
                                    if (songUpload.isSuccessful) {
                                        Log.d(TAG, "uploadFromUri: getDownloadUri success")
                                        // [START_EXCLUDE]
                                        broadcastUploadFinished(songUri, thumbnailUri)
                                        showUploadFinishedNotification(songUri, thumbnailUri)
                                        taskCompleted()
                                    } else {
                                        Log.w(TAG, "uploadFromUri:onFailure", songUpload.exception)
                                        // [START_EXCLUDE]
                                        broadcastUploadFinished(null, songUri)
                                        showUploadFinishedNotification(null, songUri)
                                        taskCompleted()
                                    }
                                }
                            }
                    }
                }
        }
    }

    private fun uploadVideo(video: Video) {
        taskStarted()
        showProgressNotification(UPLOAD_VIDEO_CAPTION, 0, 0, true)
        val videoUrl: Uri = Uri.parse(video.url)
        videoUrl.lastPathSegment?.let {
            videoUrleRef.child(it).putFile(videoUrl)
                .addOnProgressListener { (bytesTransferred, totalByteCount) ->
                    showProgressNotification(
                        UPLOAD_VIDEO_CAPTION,
                        bytesTransferred,
                        totalByteCount,
                        true
                    )
                }.continueWithTask { task ->
                    if (!task.isSuccessful) {
                        throw task.exception!!
                    }
                    videoUrleRef.child(it).downloadUrl
                }.addOnFailureListener { songUploadException ->
                    Log.w(TAG, "uploadFromUri:onFailure", songUploadException)
                    // [START_EXCLUDE]
                    broadcastUploadFinished(null, Uri.parse(video.url))
                    showUploadFinishedNotification(null, Uri.parse(video.url))
                    taskCompleted()
                }.addOnSuccessListener { videoUri ->
                    val thumbnailUrl = Uri.parse(video.thumbnail)
                    thumbnailUrl.lastPathSegment?.let {
                        thumbnailRef.child(it).putFile(thumbnailUrl)
                            .addOnProgressListener { (bytesTransferred1, totalByteCount1) ->
                                showProgressNotification(
                                    UPLOAD_VIDEO_CAPTION,
                                    bytesTransferred1,
                                    totalByteCount1,
                                    true
                                )
                            }.continueWithTask { task ->
                                if (!task.isSuccessful) {
                                    throw task.exception!!
                                }
                                thumbnailRef.child(it).downloadUrl
                            }.addOnFailureListener { thumbnailUploadException ->
                                Log.w(TAG, "uploadFromUri:onFailure", thumbnailUploadException)
                                // [START_EXCLUDE]
                                broadcastUploadFinished(null, videoUri)
                                showUploadFinishedNotification(null, videoUri)
                                taskCompleted()
                            }.addOnSuccessListener { thumbnailUri ->
                                val id = videoCollectionRef.document().id
                                videoCollectionRef.document(id).set(
                                    Video(
                                        id = id,
                                        text = video.text,
                                        title = video.title,
                                        time = video.time,
                                        pfp = video.pfp,
                                        username = video.username,
                                        thumbnail = thumbnailUri.toString(),
                                        url = videoUri.toString()
                                    )
                                ).addOnCompleteListener { videoUpload ->
                                    if (videoUpload.isSuccessful) {
                                        Log.d(TAG, "uploadFromUri: getDownloadUri success")
                                        // [START_EXCLUDE]
                                        broadcastUploadFinished(videoUri, thumbnailUri)
                                        showUploadFinishedNotification(videoUri, thumbnailUri)
                                        taskCompleted()
                                    } else {
                                        Log.w(TAG, "uploadFromUri:onFailure", videoUpload.exception)
                                        // [START_EXCLUDE]
                                        broadcastUploadFinished(null, videoUri)
                                        showUploadFinishedNotification(null, videoUri)
                                        taskCompleted()
                                    }
                                }
                            }
                    }
                }
        }
    }

    private fun broadcastUploadFinished(downloadUrl: Uri?, fileUri: Uri?): Boolean {
        val success = downloadUrl != null

        val action = if (success) UPLOAD_COMPLETED else UPLOAD_ERROR

        val broadcast = Intent(action)
            .putExtra(EXTRA_DOWNLOAD_URL, downloadUrl)
            .putExtra(EXTRA_FILE_URI, fileUri)
        return LocalBroadcastManager.getInstance(applicationContext)
            .sendBroadcast(broadcast)
    }

    private fun showUploadFinishedNotification(downloadUrl: Uri?, fileUri: Uri?) {
        dismissProgressNotification()

        // Make Intent to MainActivity
        val intent = Intent(this, MainActivity::class.java)
            .putExtra(EXTRA_DOWNLOAD_URL, downloadUrl)
            .putExtra(EXTRA_FILE_URI, fileUri)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)

        val success = downloadUrl != null
        val caption =
            if (success) SUCCESS else FAILURE
        showFinishedNotification(caption, intent, success)
    }
}