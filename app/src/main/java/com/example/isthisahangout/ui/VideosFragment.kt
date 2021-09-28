package com.example.isthisahangout.ui

import android.os.Bundle
import android.util.Log
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
import com.example.isthisahangout.adapter.VideosPagingAdapter
import com.example.isthisahangout.adapter.VideosRecyclerAdapter
import com.example.isthisahangout.databinding.FragmentVideosBinding
import com.example.isthisahangout.models.FirebaseVideo
import com.example.isthisahangout.viewmodel.VideoViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class VideosFragment : Fragment(R.layout.fragment_videos), VideosPagingAdapter.OnItemClickListener,
    VideosRecyclerAdapter.OnItemClickListener {
    private var _binding: FragmentVideosBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<VideoViewModel>()
    private lateinit var videosRecyclerAdapter: VideosRecyclerAdapter
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentVideosBinding.bind(view)
        val videosPagingAdapter = VideosPagingAdapter(this)
        videosRecyclerAdapter = VideosRecyclerAdapter(this)
        val concatAdapter = ConcatAdapter(
            videosPagingAdapter,
            videosRecyclerAdapter
        )
        binding.apply {
            postVideoButton.setOnClickListener {
                findNavController().navigate(
                    VideosFragmentDirections.actionVideosFragment2ToUploadVideoFragment()
                )
            }
            videosRecyclerview.apply {
                itemAnimator = null
                adapter = concatAdapter
                layoutManager =
                    LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            }
            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                viewModel.videos.collect {
                    videosPagingAdapter.submitData(viewLifecycleOwner.lifecycle, it)
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

    override fun onStart() {
        super.onStart()
        videosRecyclerAdapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        videosRecyclerAdapter.stopListening()
    }

    override fun onItemClick(video: FirebaseVideo) {
        Log.e("Navigation", "Navigating")
        findNavController().navigate(
            VideosFragmentDirections.actionVideosFragment2ToVideoDetailsFragment(video)
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}