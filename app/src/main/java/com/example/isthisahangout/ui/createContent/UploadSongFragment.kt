package com.example.isthisahangout.ui.createContent

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.isthisahangout.R
import com.example.isthisahangout.databinding.FragmentUploadSongBinding
import com.example.isthisahangout.service.uploadService.FirebaseUploadService
import com.example.isthisahangout.viewmodel.SongViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class UploadSongFragment : Fragment(R.layout.fragment_upload_song) {
    private var _binding: FragmentUploadSongBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<SongViewModel>()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentUploadSongBinding.bind(view)
        val getSongUri =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                if (uri != null) {
                    viewModel.songUrl = uri
                }
            }
        val getSongThumbnail =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                if (uri != null) {
                    viewModel.songThumbnail = uri
                }
            }
        binding.apply {
            songTitleEditText.editText!!.addTextChangedListener { title ->
                viewModel.songTitle = title.toString()
            }
            songTextEditText.editText!!.addTextChangedListener { text ->
                viewModel.songText = text.toString()
            }
            selectSongButton.setOnClickListener {
                getSongUri.launch("audio/*")
            }
            selectThumbnailButton.setOnClickListener {
                getSongThumbnail.launch("image/*")
            }
            uploadSongButton.setOnClickListener {
                hideKeyboard(requireContext())
                viewModel.onUploadClick()
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.songEventFlow.collect { event ->
                when (event) {
                    is SongViewModel.SongEvent.UploadSongSuccess -> {
                        Snackbar.make(
                            requireView(),
                            event.message,
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                    is SongViewModel.SongEvent.UploadSongError -> {
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

    private fun hideKeyboard(mContext: Context) {
        val imm = mContext
            .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(
            requireActivity().window
                .currentFocus!!.windowToken, 0
        )
    }

    override fun onStart() {
        super.onStart()
        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(viewModel.broadcastReceiver, FirebaseUploadService.intentFilter)
    }

    override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(requireContext())
            .unregisterReceiver(viewModel.broadcastReceiver)
    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}