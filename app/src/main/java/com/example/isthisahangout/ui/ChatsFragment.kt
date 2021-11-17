package com.example.isthisahangout.ui

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.isthisahangout.R
import com.example.isthisahangout.adapter.FirebaseMessageAdapter
import com.example.isthisahangout.adapter.MessagesPagingAdapter
import com.example.isthisahangout.databinding.FragmentChatBinding
import com.example.isthisahangout.viewmodel.ChatViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class
ChatsFragment : Fragment(R.layout.fragment_chat) {
    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var mAuth: FirebaseAuth
    private val viewModel by viewModels<ChatViewModel>()
    private lateinit var messageAdapter: FirebaseMessageAdapter
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentChatBinding.bind(view)
        messageAdapter = FirebaseMessageAdapter()
        val pagedMessageAdapter = MessagesPagingAdapter()
        val concatAdapter = ConcatAdapter(
            messageAdapter,
            pagedMessageAdapter
        )
        binding.apply {
            messagesRecyclerview.apply {
                itemAnimator = null
                adapter = concatAdapter
                layoutManager =
                    LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, true)
            }
            messagesRecyclerview.scrollToPosition(concatAdapter.itemCount - 1)
            messageEditText.addTextChangedListener { text ->
                viewModel.text = text.toString()
                Log.e("MessageFragment", text.toString())
            }
            sendButton.setOnClickListener {
                viewModel.onSendClick()
                if (messageEditText.text != null && messageEditText.text!!.isNotEmpty()) {
                    messageEditText.text!!.clear()
                }
                hideKeyboard(requireContext())
                messagesRecyclerview.scrollToPosition(concatAdapter.itemCount - 1)
            }
            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                viewModel.messagesFlow.collect {
                    pagedMessageAdapter.submitData(viewLifecycleOwner.lifecycle, it)
                    messagesRecyclerview.scrollToPosition(concatAdapter.itemCount - 1)
                }
                viewModel.messageEventFlow.collect { event ->
                    if (event is ChatViewModel.MessagingEvent.MessageError) {
                        Snackbar.make(
                            requireView(),
                            "Message could not be sent",
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            viewLifecycleOwner.lifecycleScope.launch {
                pagedMessageAdapter.loadStateFlow.collect { loadState ->
                    messagesProgressBar.isVisible = loadState.source.refresh is LoadState.Loading
                    messagesErrorTextView.isVisible = loadState.source.refresh is LoadState.Error
                    messagesRetryButton.isVisible = loadState.source.refresh is LoadState.Error
                }
            }
            messagesRetryButton.setOnClickListener {
                pagedMessageAdapter.retry()
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
        messageAdapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        messageAdapter.stopListening()
    }
}