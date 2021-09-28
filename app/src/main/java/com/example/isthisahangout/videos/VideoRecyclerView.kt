package com.example.isthisahangout.videos


import android.content.Context
import android.graphics.Point
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.example.isthisahangout.R
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import java.util.*


/*
class VideoRecyclerView(context: Context) : RecyclerView(context) {
    private enum class VolumeState {
        ON,
        OFF
    }

    // ui
    private var thumbnail: ImageView? = null  // ui
    private var volumeControl: ImageView? = null
    private var progressBar: ProgressBar? = null
    private var viewHolderParent: View? = null
    private var frameLayout: FrameLayout? = null
    private var videoSurfaceView: PlayerView? = null
    private var videoPlayer: SimpleExoPlayer? = null

    // vars
    private val mediaObjects: ArrayList<Video> = ArrayList<Video>()
    private var videoSurfaceDefaultHeight = 0
    private var screenDefaultHeight = 0
    private var playPosition = -1
    private var isVideoViewAdded = false

    // controlling playback state
    private var volumeState: VolumeState? = null

    init {
        init()
    }


    private fun init() {
        val display =
            (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
        val point = Point()
        display.getSize(point)
        videoSurfaceDefaultHeight = point.x
        screenDefaultHeight = point.y
        videoSurfaceView = PlayerView(context)
        videoSurfaceView!!.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
        videoPlayer = SimpleExoPlayer.Builder(context).build()
        videoSurfaceView!!.useController = false
        videoSurfaceView!!.player = videoPlayer
        setVolumeControl(VolumeState.ON)
        addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == SCROLL_STATE_IDLE) {
                    if (thumbnail != null) {
                        thumbnail!!.visibility = VISIBLE;
                    }
                    if (!recyclerView.canScrollVertically(1)) {
                        playVideo(true);
                    } else {
                        playVideo(false);
                    }
                }
            }
        })
    }


    fun playVideo(isEndOfList: Boolean) {
        val targetPosition: Int
        if (!isEndOfList) {
            val startPosition =
                (layoutManager as LinearLayoutManager?)!!.findFirstVisibleItemPosition()
            var endPosition =
                (layoutManager as LinearLayoutManager?)!!.findLastVisibleItemPosition()

            // if there is more than 2 list-items on the screen, set the difference to be 1
            if (endPosition - startPosition > 1) {
                endPosition = startPosition + 1
            }

            // something is wrong. return.
            if (startPosition < 0 || endPosition < 0) {
                return
            }

            // if there is more than 1 list-item on the screen
            targetPosition = if (startPosition != endPosition) {
                val startPositionVideoHeight: Int = getVisibleVideoSurfaceHeight(startPosition)
                val endPositionVideoHeight: Int = getVisibleVideoSurfaceHeight(endPosition)
                if (startPositionVideoHeight > endPositionVideoHeight) startPosition else endPosition
            } else {
                startPosition
            }
        } else {
            targetPosition = mediaObjects.size - 1
        }

        // video is already playing so return
        if (targetPosition == playPosition) {
            return
        }

        // set the position of the list-item that is to be played
        playPosition = targetPosition
        if (videoSurfaceView == null) {
            return
        }

        // remove any old surface views from previously playing videos
        videoSurfaceView!!.visibility = INVISIBLE
        removeVideoView(videoSurfaceView!!)
        val currentPosition =
            targetPosition - (layoutManager as LinearLayoutManager?)!!.findFirstVisibleItemPosition()
        val child = getChildAt(currentPosition) ?: return
        val holder: VideosAdapter.VideoPlayerViewHolder = child.tag as VideosAdapter.VideoPlayerViewHolder
        if (holder == null) {
            playPosition = -1
            return
        }
        thumbnail = holder.thumbnail
        progressBar = holder.progressBar
        volumeControl = holder.volumeControl
        viewHolderParent = holder.itemView
        frameLayout = holder.itemView.findViewById(R.id.media_container)
        videoSurfaceView!!.player = videoPlayer
        viewHolderParent!!.setOnClickListener(videoViewClickListener)
        val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(
            context, Util.getUserAgent(context, "RecyclerView VideoPlayer")
        )
        val mediaUrl: String = mediaObjects[targetPosition].getMedia_url()
        if (mediaUrl != null) {
            val videoSource: MediaSource = ExtractorMediaSource.Factory(dataSourceFactory)
                .createMediaSource(Uri.parse(mediaUrl))
            videoPlayer!!.prepare(videoSource)
            videoPlayer!!.playWhenReady = true
        }
    }

    private fun removeVideoView(videoView: PlayerView) {
        val parent = videoView.parent as ViewGroup ?: return
        val index = parent.indexOfChild(videoView)
        if (index >= 0) {
            parent.removeViewAt(index)
            isVideoViewAdded = false
            viewHolderParent!!.setOnClickListener(null)
        }
    }

    private fun setVolumeControl(state: VolumeState) {
        volumeState = state
        if (state === VolumeState.OFF) {
            videoPlayer!!.volume = 0f
            animateVolumeControl()
        } else if (state === VolumeState.ON) {
            videoPlayer!!.volume = 1f
            animateVolumeControl()
        }
    }

    private fun getVisibleVideoSurfaceHeight(playPosition: Int): Int {
        val at =
            playPosition - (layoutManager as LinearLayoutManager?)!!.findFirstVisibleItemPosition()
        val child = getChildAt(at) ?: return 0
        val location = IntArray(2)
        child.getLocationInWindow(location)
        return if (location[1] < 0) {
            location[1] + videoSurfaceDefaultHeight
        } else {
            screenDefaultHeight - location[1]
        }
    }


    private fun animateVolumeControl() {
        if (volumeControl != null) {
            volumeControl!!.bringToFront()
            if (volumeState === VolumeState.OFF) {
                Glide.with(context)
                    .load(R.drawable.ic_volume_off_grey_24dp)
                    .into(volumeControl!!)

            } else if (volumeState === VolumeState.ON) {
                Glide.with(context)
                    .load(R.drawable.ic_volume_up_grey_24dp)
                    .into(volumeControl!!)

            }
            volumeControl!!.animate().cancel()
            volumeControl!!.alpha = 1f
            volumeControl!!.animate()
                .alpha(0f)
                .setDuration(600).startDelay = 1000
        }
    }
}
*/
