package com.example.isthisahangout.ui.detailsscreen

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.ActivityResultLauncher
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageView
import com.example.isthisahangout.MainActivity
import com.example.isthisahangout.R
import com.example.isthisahangout.adapter.CommentsAdapter
import com.example.isthisahangout.databinding.FragmentVideoDetailsBinding
import com.example.isthisahangout.models.Comments
import com.example.isthisahangout.viewmodel.FavouritesViewModel
import com.example.isthisahangout.viewmodel.PlayVideoViewModel
import com.example.isthisahangout.viewmodel.VideoViewModel
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.Query
import com.norulab.exofullscreen.setSource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import java.lang.Integer.min
import java.text.DateFormat
import javax.inject.Inject
import javax.inject.Named

const val PLAYER_POSITION = "Player postiion"

@AndroidEntryPoint
class VideoDetailsFragment : Fragment(R.layout.fragment_video_details) {
    private var _binding: FragmentVideoDetailsBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<VideoViewModel>()
    private val playVideoViewModel by viewModels<PlayVideoViewModel>()
    private val favViewModel by viewModels<FavouritesViewModel>()
    private val args by navArgs<VideoDetailsFragmentArgs>()

    @Inject
    @Named("CommentsRef")
    lateinit var commentsRef: CollectionReference
    private lateinit var commentsAdapter: CommentsAdapter
    private lateinit var cropImage: ActivityResultLauncher<CropImageContractOptions>
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentVideoDetailsBinding.bind(view)
        val video = args.video
        val query = commentsRef.document(video.id!!).collection("comments")
            .orderBy("time", Query.Direction.DESCENDING)
        val options = FirestoreRecyclerOptions.Builder<Comments>()
            .setQuery(
                query,
                Comments::class.java
            )
            .build()
        commentsAdapter = CommentsAdapter(options)
        cropImage = registerForActivityResult(CropImageContract()) { result ->
            if (result.isSuccessful) {
                val uri = result.uriContent
                viewModel.commentImage = uri
            } else {
                val error = result.error
                error?.let { exception ->
                    Snackbar.make(
                        requireView(),
                        exception.localizedMessage!!.toString(),
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
        }

        binding.apply {
            addCommentImageView.isVisible = false
            commentRecyclerView.apply {
                layoutManager =
                    LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                adapter = commentsAdapter
                itemAnimator = null
                isVisible = true
            }

            if(playVideoViewModel.simpleExoPlayer == null) {
                playVideoViewModel.simpleExoPlayer =
                    SimpleExoPlayer.Builder(requireActivity().applicationContext).build()
                playerView.player = playVideoViewModel.simpleExoPlayer
                playVideoViewModel.simpleExoPlayer!!.setSource(requireActivity().applicationContext, video.url!!)
            }

            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                favViewModel.favVideo.collect { favVideos ->
                    val isFav = favVideos.any {
                        it.id == video.id
                    }
                    viewModel.isBookMarked.value = isFav
                }
            }

            viewModel.isBookMarked.observe(viewLifecycleOwner) { bookMarked ->
                if (bookMarked) {
                    bookmarkImageView.setImageResource(R.drawable.bookmarked)
                } else {
                    bookmarkImageView.setImageResource(R.drawable.bookmark)
                }
            }

            if (video.text == null) {
                showDetailsButton.isClickable = false
                showDetailsTextView.isVisible = false
                showDetailsButton.isVisible = false
            }

            viewModel.showDetails.observe(viewLifecycleOwner) {
                if (it) {
                    showDetailsButton.setImageResource(R.drawable.hide_details)
                    showDetailsTextView.text = "Hide Details"
                    descTextView.text = video.text!!
                } else {
                    showDetailsButton.setImageResource(R.drawable.show_details)
                    showDetailsTextView.text = "Show Details"
                    descTextView.text =
                        video.text!!.subSequence(0, min(10, video.text!!.length - 1))
                }
            }

            showDetailsButton.setOnClickListener {
                viewModel.onShowDetailsClick()
            }

            bookmarkImageView.setOnClickListener {
                viewModel.onBookMarkClick(video)
            }

            videoTitleTextView.text = video.title
            uploaderUsername.text = video.username
            timeTextView.text = DateFormat.getDateTimeInstance().format(video.time)

            Glide.with(requireContext())
                .load(video.pfp)
                .placeholder(R.drawable.click_to_add_image)
                .into(uploaderPfpImageView)

            addCommentImageButton.setOnClickListener {
                cropImage.launch(
                    com.canhub.cropper.options {
                        setGuidelines(CropImageView.Guidelines.ON)
                            .setAspectRatio(1920, 1080)
                            .setCropShape(CropImageView.CropShape.RECTANGLE)
                    }
                )
            }

            commentEditText.addTextChangedListener { text ->
                viewModel.commentText = text.toString()
            }

            commentSendButton.setOnClickListener {
                hideKeyboard(requireContext())
                addCommentImageView.isVisible = false
                viewModel.onCommentSendClick(video)
                commentEditText.text.clear()
                addCommentImageView.isVisible = false
            }

            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                viewModel.videoEventFlow.collect { event ->
                    when (event) {
                        is VideoViewModel.VideoEvent.UploadVideoSuccess -> {
                            Snackbar.make(
                                requireView(),
                                event.message,
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
                        is VideoViewModel.VideoEvent.UploadVideoError -> {
                            Snackbar.make(
                                requireView(),
                                event.message,
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }
    }

 /*   override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (playVideoViewModel.simpleExoPlayer != null) {
            outState.putLong(PLAYER_POSITION, playVideoViewModel.simpleExoPlayer!!.contentPosition)
        }
    }*/

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            (requireActivity() as MainActivity).supportActionBar!!.hide()
        } else {
            (requireActivity() as MainActivity).supportActionBar!!.show()
        }
    }

   /* override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.let { state ->
            playVideoViewModel.simpleExoPlayer?.seekTo(state.getLong(PLAYER_POSITION))
        }
    }*/

    override fun onStart() {
        super.onStart()
        commentsAdapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        commentsAdapter.stopListening()
    }

    private fun hideKeyboard(mContext: Context) {
        val imm = mContext
            .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(
            requireActivity().window
                .currentFocus!!.windowToken, 0
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        playVideoViewModel.simpleExoPlayer?.release()
        playVideoViewModel.simpleExoPlayer = null
    }
}