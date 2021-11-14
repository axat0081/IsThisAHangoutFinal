package com.example.isthisahangout.ui.navDrawer

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.isthisahangout.R
import com.example.isthisahangout.adapter.AnimeNewsAdapter
import com.example.isthisahangout.databinding.FragmentAnimeNewsBinding
import com.example.isthisahangout.models.AnimeNews
import com.example.isthisahangout.viewmodel.AnimeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class AnimeNewsFragment : Fragment(R.layout.fragment_anime_news),
    AnimeNewsAdapter.OnItemClickListener {
    private var _binding: FragmentAnimeNewsBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<AnimeViewModel>()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAnimeNewsBinding.bind(view)
        val animeNewsAdapter = AnimeNewsAdapter(this)
        binding.apply {
            animeNewsRecyclerView.apply {
                adapter = animeNewsAdapter
            }
            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                viewModel.animeNews.collect {
                    animeNewsAdapter.submitList(it)
                }
            }
        }
    }

    override fun onItemClick(animeNews: AnimeNews) {

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}