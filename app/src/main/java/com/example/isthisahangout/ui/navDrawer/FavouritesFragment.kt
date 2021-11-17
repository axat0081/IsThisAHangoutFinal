package com.example.isthisahangout.ui.navDrawer

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.isthisahangout.R
import com.example.isthisahangout.adapter.favourites.FavAnimeAdapter
import com.example.isthisahangout.adapter.favourites.FavGameAdapter
import com.example.isthisahangout.adapter.favourites.FavPostAdapter
import com.example.isthisahangout.adapter.favourites.FavVideosAdapter
import com.example.isthisahangout.databinding.FragmentFavouritesBinding
import com.example.isthisahangout.models.AnimeGenreResults
import com.example.isthisahangout.models.favourites.FavAnime
import com.example.isthisahangout.models.favourites.FavGame
import com.example.isthisahangout.models.favourites.FavPost
import com.example.isthisahangout.models.favourites.FavVideo
import com.example.isthisahangout.utils.onQueryTextChanged
import com.example.isthisahangout.viewmodel.FavouritesViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class FavouritesFragment : Fragment(R.layout.fragment_favourites),
    FavAnimeAdapter.OnItemClickListener, FavVideosAdapter.OnItemClickListener,
    FavPostAdapter.OnItemClickListener, FavGameAdapter.OnItemClickListener {
    private var _binding: FragmentFavouritesBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<FavouritesViewModel>()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentFavouritesBinding.bind(view)
        val animeAdapter = FavAnimeAdapter(this)
        val videosAdapter = FavVideosAdapter(this)
        val postAdapter = FavPostAdapter(this)
        val gameAdapter = FavGameAdapter(this)
        binding.apply {
            animeSearchView.onQueryTextChanged {
                viewModel.favAnimeQuery.value = it
            }
            gameSearchView.onQueryTextChanged {
                viewModel.favGameQuery.value = it
            }
            videoSearchView.onQueryTextChanged {
                viewModel.favVideoQuery.value = it
            }
            postSearchView.onQueryTextChanged {
                viewModel.favPostQuery.value = it
            }
            animeRecyclerview.apply {
                adapter = animeAdapter
                layoutManager =
                    LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            }
            gamesRecyclerview.apply {
                adapter = gameAdapter
                layoutManager =
                    LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            }
            videoRecyclerview.apply {
                adapter = videosAdapter
                layoutManager =
                    LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            }
            postsRecyclerview.apply {
                adapter = postAdapter
                layoutManager =
                    LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            }
            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                viewModel.favAnime.collect {
                    animeAdapter.submitList(it)
                }
            }
            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                viewModel.favGame.collect {
                    gameAdapter.submitList(it)
                }
            }

            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                viewModel.favVideo.collect {
                    videosAdapter.submitList(it)
                }
            }

            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                viewModel.favPost.collect {
                    postAdapter.submitList(it)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onItemClick(anime: FavAnime) {
        findNavController().navigate(
            FavouritesFragmentDirections.actionFavouritesFragmentToDetailDisplayFragment(
                AnimeGenreResults.AnimeByGenres(
                    id = anime.id.toString(),
                    title = anime.title,
                    imageUrl = anime.image,
                    url = "X",
                    synopsis = "X"
                )
            )
        )
    }

    override fun onItemClick(anime: FavVideo) {
        TODO("Not yet implemented")
    }

    override fun onItemClick(anime: FavPost) {
        TODO("Not yet implemented")
    }

    override fun onItemClick(anime: FavGame) {
        TODO("Not yet implemented")
    }
}