package com.example.isthisahangout.ui

import android.content.Context
import android.graphics.Point
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnimationUtils
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.paging.map
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.isthisahangout.R
import com.example.isthisahangout.adapter.VideosPagingAdapter
import com.example.isthisahangout.adapter.VideosRecyclerAdapter
import com.example.isthisahangout.databinding.FragmentVideosBinding
import com.example.isthisahangout.models.FirebaseVideo
import com.example.isthisahangout.utils.startAnimation
import com.example.isthisahangout.viewmodel.VideoViewModel
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Log
import com.google.android.exoplayer2.util.Util
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect


@AndroidEntryPoint
class VideosFragment : Fragment(R.layout.fragment_videos), VideosPagingAdapter.OnItemClickListener,
    VideosRecyclerAdapter.OnItemClickListener {
    private var _binding: FragmentVideosBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<VideoViewModel>()
    private lateinit var videosRecyclerAdapter: VideosRecyclerAdapter
    private lateinit var videosPagingAdapter: VideosPagingAdapter
    private lateinit var concatAdapter: ConcatAdapter

    //
    private var exoPlayer: SimpleExoPlayer? = null
    private var videoSurfaceDefaultHeight = 0
    private var screenDefaultHeight = 0
    private var playPosition = -1
    private var videoSurfaceView: PlayerView? = null
    private var isVideoViewAdded = false
    private var viewHolderParent: View? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentVideosBinding.bind(view)
        val display =
            (requireContext().getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
        val point = Point()
        display.getSize(point)
        videoSurfaceDefaultHeight = point.x
        screenDefaultHeight = point.y
        videoSurfaceView = PlayerView(requireContext())
        videoSurfaceView!!.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
        exoPlayer = SimpleExoPlayer.Builder(requireContext()).build()
        videoSurfaceView!!.useController = false
        videoSurfaceView!!.player = exoPlayer
        videosPagingAdapter = VideosPagingAdapter(this)
        videosRecyclerAdapter = VideosRecyclerAdapter(this)
        concatAdapter = ConcatAdapter(
            videosPagingAdapter,
            videosRecyclerAdapter
        )
        val animation =
            AnimationUtils.loadAnimation(requireContext(), R.anim.circle_explosion_anim).apply {
                duration = 1000
                interpolator = AccelerateDecelerateInterpolator()
            }
        binding.apply {
            postVideoButton.setOnClickListener {
                binding.postVideoButton.isVisible = false
                circleBackground.isVisible = true
                circleBackground.startAnimation(animation) {
                    circleBackground.isVisible = false
                    findNavController().navigate(
                        VideosFragmentDirections.actionVideosFragment2ToUploadVideoFragment()
                    )
                }
            }
            videosRecyclerview.apply {
                itemAnimator = null
                adapter = concatAdapter
                layoutManager =
                    LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                        super.onScrollStateChanged(recyclerView, newState)
                        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                            playVideo(!recyclerView.canScrollVertically(1))
                        }
                    }
                })
                addOnChildAttachStateChangeListener(object :
                    RecyclerView.OnChildAttachStateChangeListener {
                    override fun onChildViewAttachedToWindow(view: View) {

                    }

                    override fun onChildViewDetachedFromWindow(view: View) {
                        resetVideoView()
                    }
                })
            }
            exoPlayer!!.addListener(object : Player.EventListener {
                override fun onPlaybackStateChanged(state: Int) {
                    super.onPlaybackStateChanged(state)
                    when (state) {
                        Player.STATE_ENDED -> exoPlayer!!.seekTo(0)
                        Player.STATE_READY -> if (!isVideoViewAdded) addVideoView()
                        else -> Unit
                    }
                }
            })
            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                viewModel.videos.collect { pagedVideos ->
                    videosPagingAdapter.submitData(viewLifecycleOwner.lifecycle, pagedVideos)
                    viewModel.videoPlayList.addAll(videosPagingAdapter.snapshot().items)
                    Log.e("list",viewModel.videoPlayList.size.toString())
                }
            }
            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                videosPagingAdapter.loadStateFlow.collect { loadState ->
                    videosProgressBar.isVisible = loadState.source.refresh is LoadState.Loading
                    videosErrorTextView.isVisible = loadState.source.refresh is LoadState.Error
                    videosRetryButton.isVisible = loadState.source.refresh is LoadState.Error
                }
            }

            videosRetryButton.setOnClickListener {
                videosPagingAdapter.retry()
            }
        }
    }

    private fun playVideo(isEndOfList: Boolean) {
        var targetPosition = 0
        if (!isEndOfList) {
            val startPosition =
                (binding.videosRecyclerview.layoutManager as LinearLayoutManager?)!!
                    .findFirstVisibleItemPosition()
            var endPosition =
                (binding.videosRecyclerview.layoutManager as LinearLayoutManager?)!!
                    .findLastVisibleItemPosition()
            // if there is more than 2 list-items on the screen, set the difference to be 1
            if (endPosition - startPosition > 1) {
                endPosition = startPosition + 1
            }

            // something is wrong. return.
            if (startPosition < 0 || endPosition < 0) {
                return
            }
            targetPosition = if (startPosition != endPosition) {
                val startPositionVideoHeight: Int = getVisibleVideoSurfaceHeight(startPosition)
                val endPositionVideoHeight: Int = getVisibleVideoSurfaceHeight(endPosition)
                if (startPositionVideoHeight > endPositionVideoHeight) startPosition else endPosition
            } else {
                startPosition
            }
        } else {
            targetPosition = concatAdapter.itemCount - 1
        }
        if (targetPosition == playPosition) {
            return
        }
        playPosition = targetPosition
        videoSurfaceView!!.visibility = View.INVISIBLE
        removeVideoView(videoSurfaceView!!)
        val currentPosition =
            targetPosition - (binding.videosRecyclerview.layoutManager as LinearLayoutManager?)!!
                .findFirstVisibleItemPosition()
        val child = binding.videosRecyclerview.getChildAt(currentPosition) ?: return
        val holder = child.tag
        if (holder == null) {
            playPosition = -1
            return
        }
        videoSurfaceView!!.player = exoPlayer
        val dataSourceFactory = DefaultDataSourceFactory(
            requireContext(),
            Util.getUserAgent(
                requireContext(),
                "RecyclerView VideoPlayer"
            )
        )
        val videoUrl = viewModel.videoPlayList[targetPosition].url!!
        val videoSource: MediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(Uri.parse(videoUrl))
        exoPlayer!!.prepare(videoSource)
        exoPlayer!!.playWhenReady = true

    }

    private fun addVideoView() {
        videosPagingAdapter.viewHolder.binding.root.addView(videoSurfaceView)
        isVideoViewAdded = true
        videoSurfaceView!!.requestFocus()
        videoSurfaceView!!.visibility = RecyclerView.VISIBLE
        videoSurfaceView!!.alpha = 1f
    }

    private fun resetVideoView() {
        if (isVideoViewAdded) {
            removeVideoView(videoSurfaceView!!)
            playPosition = -1
            videoSurfaceView!!.visibility = RecyclerView.INVISIBLE
        }
    }

    private fun getVisibleVideoSurfaceHeight(playPosition: Int): Int {
        val at =
            playPosition - (binding.videosRecyclerview.layoutManager as LinearLayoutManager?)!!.findFirstVisibleItemPosition()
        val child = binding.videosRecyclerview.getChildAt(at) ?: return 0
        val location = IntArray(2)
        child.getLocationInWindow(location)
        return if (location[1] < 0) {
            location[1] + videoSurfaceDefaultHeight
        } else {
            screenDefaultHeight - location[1]
        }
    }

    private fun removeVideoView(videoView: PlayerView) {
        videosPagingAdapter.viewHolder.binding.root.removeView(videoSurfaceView)
        val index = videoSurfaceView!!.indexOfChild(videoView)
        if (index >= 0) {
            videoSurfaceView!!.removeViewAt(index)
            isVideoViewAdded = false
        }
//        val parent = videoView.parent as ViewGroup
//        val index = parent.indexOfChild(videoView)
//        if (index >= 0) {
//            parent.removeViewAt(index)
//            isVideoViewAdded = false
//        }
    }

    override fun onStart() {
        super.onStart()
        videosRecyclerAdapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        videosRecyclerAdapter.stopListening()
    }

    override fun onItemClick(video: FirebaseVideo) {
        findNavController().navigate(
            VideosFragmentDirections.actionVideosFragment2ToVideoDetailsFragment(video)
        )
    }

    fun releasePlayer() {
        if (exoPlayer != null) {
            exoPlayer!!.release()
            exoPlayer = null
        }
        viewHolderParent = null
    }

    override fun onDestroy() {
        super.onDestroy()
        releasePlayer()
        _binding = null
    }
}