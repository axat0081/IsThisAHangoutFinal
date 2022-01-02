package com.example.isthisahangout.ui

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.isthisahangout.R
import com.example.isthisahangout.adapter.chat.LoadingState
import com.example.isthisahangout.databinding.FragmentChatBinding
import com.example.isthisahangout.viewmodel.ChatViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@AndroidEntryPoint
class
ChatsFragment : Fragment(R.layout.fragment_chat) {
    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var mAuth: FirebaseAuth

    private val viewModel by viewModels<ChatViewModel>()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentChatBinding.bind(view)
        binding.apply {
            messagesRecyclerview.apply {
                itemAnimator = null
                adapter = viewModel.chatAdapter
                layoutManager =
                    LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            }
            messageEditText.addTextChangedListener { text ->
                viewModel.text = text.toString()
            }
            sendButton.setOnClickListener {
                viewModel.onSendClick()
                if (messageEditText.text != null && messageEditText.text!!.isNotEmpty()) {
                    messageEditText.text!!.clear()
                }
                hideKeyboard(requireContext())
            }
            viewModel.chatAdapter.loadingState.observe(viewLifecycleOwner) { loadState ->
                headerProgressBar.isVisible =
                    loadState == LoadingState.LOADING_MORE
                messagesProgressBar.isVisible =
                    loadState == LoadingState.LOADING_INITIAL
                messagesErrorTextView.isVisible =
                    loadState == LoadingState.ERROR
                if (loadState == LoadingState.INITIAL_LOADED || loadState == LoadingState.NEW_ITEM)
                    messagesRecyclerview.scrollToPosition(viewModel.chatAdapter.itemCount - 1)
            }
            viewLifecycleOwner.lifecycleScope.launchWhenStarted {

                viewModel.messageEventFlow.collectLatest { event ->
                    when (event) {
                        is ChatViewModel.MessagingEvent.MessageError -> {
                            Toast.makeText(requireContext(), event.message, Toast.LENGTH_SHORT)
                                .show()
                        }
                        else -> Unit
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
        viewModel.chatAdapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        viewModel.chatAdapter.stopListening()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.chatAdapter.cleanup()
    }
}