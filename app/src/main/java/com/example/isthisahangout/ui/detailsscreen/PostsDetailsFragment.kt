package com.example.isthisahangout.ui.detailsscreen

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.isthisahangout.MainActivity
import com.example.isthisahangout.R
import com.example.isthisahangout.adapter.CommentsAdapter
import com.example.isthisahangout.databinding.FragmentPostDetailsBinding
import com.example.isthisahangout.models.Comments
import com.example.isthisahangout.models.FirebasePost
import com.example.isthisahangout.models.favourites.FavPost
import com.example.isthisahangout.viewmodel.FavouritesViewModel
import com.example.isthisahangout.viewmodel.PostViewModel
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.Query
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import java.text.DateFormat
import javax.inject.Inject
import javax.inject.Named

@AndroidEntryPoint
class PostsDetailsFragment : Fragment(R.layout.fragment_post_details) {
    private var _binding: FragmentPostDetailsBinding? = null
    private val binding get() = _binding!!
    private val args by navArgs<PostsDetailsFragmentArgs>()
    private val viewModel by viewModels<PostViewModel>()
    private val favViewModel by viewModels<FavouritesViewModel>()

    @Inject
    @Named("CommentsRef")
    lateinit var commentsRef: CollectionReference
    private lateinit var commentsAdapter: CommentsAdapter
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val post: FirebasePost = args.post
        _binding = FragmentPostDetailsBinding.bind(view)
        val query = commentsRef.document(post.id!!).collection("comments")
            .orderBy("time", Query.Direction.DESCENDING)
        val options = FirestoreRecyclerOptions.Builder<Comments>()
            .setQuery(
                query,
                Comments::class.java
            )
            .build()
        commentsAdapter = CommentsAdapter(options)
        val getCommentImage =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                if (uri != null) {
                    viewModel.commentImage = uri
                    binding.addCommentImageView.isVisible = true
                    Glide.with(requireContext())
                        .load(uri)
                        .into(binding.addCommentImageView)
                }
            }
        binding.apply {
            addCommentImageView.isVisible = false
            bookmarkImageView.setImageResource(R.drawable.bookmark)
            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                favViewModel.favPost.collect { favPost ->
                    val isFav = favPost.any {
                        it.id == post.id
                    }
                    if (isFav) {
                        viewModel.isBookMarked.value = true
                    }
                }
            }

            viewModel.isBookMarked.observe(viewLifecycleOwner){isBookmarked->
                if(isBookmarked){
                    bookmarkImageView.setImageResource(R.drawable.bookmarked)
                } else {
                    bookmarkImageView.setImageResource(R.drawable.bookmark)
                }
            }

            commentsRecyclerview.apply {
                isVisible = true
                layoutManager =
                    LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                adapter = commentsAdapter
                itemAnimator = null
            }

            Glide.with(requireContext())
                .load(post.pfp)
                .placeholder(R.drawable.click_to_add_image)
                .into(posterPfpImageView)
            likeTextView.text = post.likes.toString()
            postTitleTextView.text = post.title!!
            postBody.text = post.text
            posterUsername.text = post.username
            timeTextView.text = DateFormat.getDateTimeInstance().format(post.time)
            postImageView.isClickable = false
            Glide.with(requireContext())
                .load(post.image)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        imageProgressBar.isVisible = false
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        imageProgressBar.isVisible = false
                        postImageView.isClickable = true
                        return false
                    }
                }).into(postImageView)

            postImageView.setOnClickListener {
                postImageView.animate()
                    .setDuration(1000)
                    .scaleX(20F)
                    .scaleY(20F)
                    .alpha(0F)
            }

            bookmarkImageView.setOnClickListener {
                viewModel.onBookMarkClick(post)
            }

            addCommentImageButton.setOnClickListener {
                getCommentImage.launch("image/*")
            }

            commentEditText.addTextChangedListener { text ->
                viewModel.commentText = text.toString()
            }

            commentSendButton.setOnClickListener {
                hideKeyboard(requireContext())
                addCommentImageView.isVisible = false
                viewModel.onCommentSendClick(post)
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