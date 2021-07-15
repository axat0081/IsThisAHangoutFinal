package com.example.isthisahangout.ui

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.isthisahangout.R
import com.example.isthisahangout.adapter.PostsPagingAdapter
import com.example.isthisahangout.adapter.PostsRecyclerAdapter
import com.example.isthisahangout.databinding.FragmentPostsBinding
import com.example.isthisahangout.models.FirebasePost
import com.example.isthisahangout.viewmodel.PostViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PostsFragment : Fragment(R.layout.fragment_posts), PostsPagingAdapter.OnItemClickListener,
    PostsRecyclerAdapter.OnItemClickListener {
    private var _binding: FragmentPostsBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<PostViewModel>()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPostsBinding.bind(view)
        val postsRecyclerAdapter = PostsRecyclerAdapter(this)
        val postsPagingAdapter = PostsPagingAdapter(this)
        val concatAdapter = ConcatAdapter(
            postsRecyclerAdapter,
            postsPagingAdapter
        )
        binding.apply {
            postsRecyclerview.apply {
                layoutManager =
                    LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                adapter = concatAdapter
                itemAnimator = null
            }

            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.postsFlow.collect {
                    postsPagingAdapter.submitData(viewLifecycleOwner.lifecycle, it)
                }
            }

            viewLifecycleOwner.lifecycleScope.launch {
                postsPagingAdapter.loadStateFlow.collect { loadState ->
                    postsProgressBar.isVisible = loadState.source.refresh is LoadState.Loading
                    postsErrorTextView.isVisible = loadState.source.refresh is LoadState.Error
                    postsRetryButton.isVisible = loadState.source.refresh is LoadState.Error
                }
            }

            postsRetryButton.setOnClickListener {
                postsPagingAdapter.retry()
            }

            createPostsButton.setOnClickListener {
                findNavController().navigate(
                    PostsFragmentDirections.actionPostsFragment2ToCreatePostFragment()
                )
            }
        }
    }

    override fun onItemClickPaged(post: FirebasePost) {
        findNavController().navigate(
            PostsFragmentDirections.actionPostsFragment2ToPostsDetailsFragment2(
                FirebasePost(
                    id = post.id!!,
                    pfp = post.pfp,
                    image = post.image,
                    time = post.time,
                    likes = post.likes ?: 0,
                    text = post.text,
                    username = post.username,
                    title = post.title,
                )
            )
        )
    }

    override fun onItemClick(post: FirebasePost) {
        findNavController().navigate(
            PostsFragmentDirections.actionPostsFragment2ToPostsDetailsFragment2(
                FirebasePost(
                    id = post.id!!,
                    pfp = post.pfp,
                    image = post.image,
                    time = post.time,
                    likes = post.likes ?: 0,
                    text = post.text,
                    username = post.username,
                    title = post.title,
                )
            )
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}