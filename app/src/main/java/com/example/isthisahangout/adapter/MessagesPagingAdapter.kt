package com.example.isthisahangout.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.isthisahangout.MainActivity
import com.example.isthisahangout.databinding.MessagesDisplayLayoutBinding
import com.example.isthisahangout.models.FirebaseMessage

class MessagesPagingAdapter :
    PagingDataAdapter<FirebaseMessage, MessagesPagingAdapter.MessagesPagedViewHolder>(COMPARATOR) {

    companion object {
        val COMPARATOR = object : DiffUtil.ItemCallback<FirebaseMessage>() {
            override fun areItemsTheSame(oldItem: FirebaseMessage, newItem: FirebaseMessage) =
                oldItem == newItem

            override fun areContentsTheSame(oldItem: FirebaseMessage, newItem: FirebaseMessage) =
                oldItem == newItem
        }
    }

    override fun onBindViewHolder(holder: MessagesPagedViewHolder, position: Int) {
        val item = getItem(position)
        if (item != null)
            holder.bind(item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessagesPagedViewHolder =
        MessagesPagedViewHolder(
            MessagesDisplayLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    inner class MessagesPagedViewHolder(private val binding: MessagesDisplayLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(message: FirebaseMessage) {
            binding.apply {
                if (message.senderId == MainActivity.userId) {
                    linearLayout1.isVisible = false
                    sentByTextView.text = message.username
                    messageSentTextView.text =
                        message.text + "\n\n" + android.text.format.DateFormat.format(
                            "yyyy-MM-dd hh:mm a",
                            message.time.toDate()
                        ).toString()
                } else {
                    linearLayout2.isVisible = false
                    usernameTextView.text = message.username
                    messageReceivedTextView.text =
                        message.text + "\n\n" + android.text.format.DateFormat.format(
                            "yyyy-MM-dd hh:mm a",
                            message.time.toDate()
                        ).toString()
                }
            }
        }
    }
}