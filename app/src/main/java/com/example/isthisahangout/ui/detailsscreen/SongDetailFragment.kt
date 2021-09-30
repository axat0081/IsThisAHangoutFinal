package com.example.isthisahangout.ui.detailsscreen

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.ActivityResultLauncher
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageView
import com.example.isthisahangout.R
import com.example.isthisahangout.adapter.CommentsAdapter
import com.example.isthisahangout.databinding.FragmentSongDetailBinding
import com.example.isthisahangout.models.Comments
import com.example.isthisahangout.viewmodel.SongDetailViewModel
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.Query
import java.text.DateFormat
import javax.inject.Inject
import javax.inject.Named

class SongDetailFragment : Fragment(R.layout.fragment_song_detail) {
    private var _binding: FragmentSongDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var commentsAdapter: CommentsAdapter

    @Inject
    @Named("CommentsRef")
    lateinit var commentsRef: CollectionReference
    private val args by navArgs<SongDetailFragmentArgs>()
    private lateinit var cropImage: ActivityResultLauncher<CropImageContractOptions>
    private val viewModel by viewModels<SongDetailViewModel>()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSongDetailBinding.bind(view)
        val song = args.song
        val query = commentsRef.document(song.id!!).collection("comments")
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
                Glide.with(requireContext())
                    .load(uri)
                    .into(binding.addCommentImageView)
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
            commentRecyclerView.apply {
                layoutManager =
                    LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                adapter = commentsAdapter
                itemAnimator = null
                isVisible = true
            }
            viewModel.isBookMarked.observe(viewLifecycleOwner) { bookMarked ->
                if (bookMarked) {
                    bookmarkImageView.setImageResource(R.drawable.bookmarked)
                } else {
                    bookmarkImageView.setImageResource(R.drawable.bookmark)
                }
            }
            if (song.text == null) {
                showDetailsButton.isClickable = false
                showDetailsTextView.isVisible = false
                showDetailsButton.isVisible = false
            }
            viewModel.showDetails.observe(viewLifecycleOwner) {
                if (it) {
                    showDetailsButton.setImageResource(R.drawable.hide_details)
                    showDetailsTextView.text = "Hide Details"
                    descTextView.text = song.text!!
                } else {
                    showDetailsButton.setImageResource(R.drawable.show_details)
                    showDetailsTextView.text = "Show Details"
                    descTextView.text =
                        song.text!!.subSequence(0, Integer.min(10, song.text!!.length - 1))
                }
            }
            showDetailsButton.setOnClickListener {
                viewModel.onShowDetailsClick()
            }
            songTitleTextView.text = song.title
            Glide.with(requireContext())
                .load(song.pfp)
                .into(uploaderPfpImageView)
            uploaderUsername.text = song.username
            timeTextView.text = DateFormat.getDateTimeInstance().format(song.time)

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
                viewModel.onCommentSendClick(song)
                commentEditText.text.clear()
                addCommentImageView.isVisible = false
            }

        }
    }

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
    }
}