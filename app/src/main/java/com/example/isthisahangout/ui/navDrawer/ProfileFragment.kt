package com.example.isthisahangout.ui.navDrawer

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.isthisahangout.MainActivity
import com.example.isthisahangout.R
import com.example.isthisahangout.adapter.ComfortCharacterAdapter
import com.example.isthisahangout.databinding.FragmentProfileBinding
import com.example.isthisahangout.models.ComfortCharacter
import com.example.isthisahangout.service.uploadService.FirebaseUploadService
import com.example.isthisahangout.viewmodel.FirebaseAuthViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileFragment : Fragment(R.layout.fragment_profile),
    ComfortCharacterAdapter.OnItemClickListener {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel by activityViewModels<FirebaseAuthViewModel>()
    private lateinit var comfortCharacterAdapter: ComfortCharacterAdapter
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentProfileBinding.bind(view)
        comfortCharacterAdapter = ComfortCharacterAdapter(this)
        binding.apply {
            usernameTextView.text = MainActivity.username
            pfpImageview.setOnClickListener {
                viewModel.imageTag.value = "pfp"
                findNavController().navigate(
                    ProfileFragmentDirections.actionProfileFragment2ToProfileImageUpdateFragment()
                )
            }
            headerImageView.setOnClickListener {
                viewModel.imageTag.value = "header"
                findNavController().navigate(
                    ProfileFragmentDirections.actionProfileFragment2ToProfileImageUpdateFragment()
                )
            }
            comfortCharacterTextRecyclerview.apply {
                adapter = comfortCharacterAdapter
                layoutManager =
                    LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            }
            addComfortCharacterButton.setOnClickListener {
                findNavController().navigate(
                    ProfileFragmentDirections.actionProfileFragment2ToAddComfortCharacterFragment()
                )
            }
            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                MainActivity.userPfpObv.collectLatest { pfp ->
                    Glide.with(requireContext())
                        .load(pfp)
                        .into(pfpImageview)
                }
            }
            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                MainActivity.userHeaderObv.collectLatest { header ->
                    Glide.with(requireContext())
                        .load(header)
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
                                return false
                            }
                        }).into(headerImageView)
                }
            }
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.profileFlow.collect { event ->
                    when (event) {
                        is FirebaseAuthViewModel.AuthEvent.UpdateProfileSuccess -> {
                            Snackbar.make(requireView(), event.message, Snackbar.LENGTH_SHORT)
                                .show()
                            pfpProgressBar.isVisible = true
                            Glide.with(requireContext())
                                .load(viewModel.profilePfp)
                                .listener(object : RequestListener<Drawable> {
                                    override fun onLoadFailed(
                                        e: GlideException?,
                                        model: Any?,
                                        target: Target<Drawable>?,
                                        isFirstResource: Boolean
                                    ): Boolean {
                                        pfpProgressBar.isVisible = false
                                        return false
                                    }

                                    override fun onResourceReady(
                                        resource: Drawable?,
                                        model: Any?,
                                        target: Target<Drawable>?,
                                        dataSource: DataSource?,
                                        isFirstResource: Boolean
                                    ): Boolean {
                                        pfpProgressBar.isVisible = false
                                        return false
                                    }
                                }).into(pfpImageview)
                        }
                        is FirebaseAuthViewModel.AuthEvent.RegistrationFailure -> {
                            Snackbar.make(
                                requireView(),
                                "Error: ${event.message}",
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
                        else -> Unit
                    }
                }
            }
        }
    }

    override fun onItemClick(character: ComfortCharacter) {

    }

    override fun onStart() {
        super.onStart()
        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(viewModel.broadcastReceiver, FirebaseUploadService.intentFilter)
        comfortCharacterAdapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(requireContext())
            .unregisterReceiver(viewModel.broadcastReceiver)
        comfortCharacterAdapter.stopListening()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}