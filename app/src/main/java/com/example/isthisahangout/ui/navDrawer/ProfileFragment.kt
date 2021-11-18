package com.example.isthisahangout.ui.navDrawer

import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
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
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileFragment : Fragment(R.layout.fragment_profile),
    ComfortCharacterAdapter.OnItemClickListener {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<FirebaseAuthViewModel>()
    private lateinit var comfortCharacterAdapter: ComfortCharacterAdapter
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentProfileBinding.bind(view)
        val getProfilePfp =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                if (uri != null) {
                    viewModel.profilePfp = uri
                }
            }
        comfortCharacterAdapter = ComfortCharacterAdapter(this)
        binding.apply {
            usernameTextView.text = MainActivity.username
            Glide.with(requireContext())
                .load(MainActivity.userpfp)
                .into(pfpImageView)
            pfpImageView.setOnClickListener {
                getProfilePfp.launch("image/*")
            }
            updateButton.setOnClickListener {
                viewModel.onUpdatePfpClick()
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
                                }).into(pfpImageView)
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