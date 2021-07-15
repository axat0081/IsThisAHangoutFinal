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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.isthisahangout.R
import com.example.isthisahangout.adapter.VideosAdapter
import com.example.isthisahangout.databinding.FragmentVideosBinding
import com.example.isthisahangout.models.Video
import com.example.isthisahangout.viewmodel.VideoViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class VideosFragment : Fragment(R.layout.fragment_videos), VideosAdapter.OnItemClickListener {
    private var _binding: FragmentVideosBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<VideoViewModel>()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentVideosBinding.bind(view)
        val videosAdapter = VideosAdapter(this)
        binding.apply {
            postVideoButton.setOnClickListener {
                findNavController().navigate(
                    VideosFragmentDirections.actionVideosFragment2ToUploadVideoFragment()
                )
            }
            videosRecyclerview.apply {
                itemAnimator = null
                adapter = videosAdapter
                layoutManager =
                    LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            }
            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                viewModel.videos.collect {
                    videosAdapter.submitData(viewLifecycleOwner.lifecycle, it)
                }
            }
            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                videosAdapter.loadStateFlow.collect { loadState ->
                    videosProgressBar.isVisible = loadState.source.refresh is LoadState.Loading
                    videosErrorTextView.isVisible = loadState.source.refresh is LoadState.Error
                    videosRetryButton.isVisible = loadState.source.refresh is LoadState.Error
                }
            }

            videosRetryButton.setOnClickListener {
                videosAdapter.retry()
            }
        }
    }

    override fun onItemClick(video: Video) {
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