package com.example.isthisahangout.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.isthisahangout.databinding.MessagesDisplayLayoutBinding
import com.example.isthisahangout.models.FirebaseMessage
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query

fun Query.whereAfterTimestamp(): Query =
    whereGreaterThan("time", Timestamp.now())

class FirebaseMessageAdapter :
    ListAdapter<FirebaseMessage, FirebaseMessageAdapter.FirebaseMessageViewHolder>(COMPARATOR) {

    private var mAuth: FirebaseAuth = FirebaseAuth.getInstance()

    companion object {
        val COMPARATOR = object : DiffUtil.ItemCallback<FirebaseMessage>() {
            override fun areItemsTheSame(oldItem: FirebaseMessage, newItem: FirebaseMessage) =
                oldItem == newItem

            override fun areContentsTheSame(oldItem: FirebaseMessage, newItem: FirebaseMessage) =
                oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FirebaseMessageViewHolder =
        FirebaseMessageViewHolder(
            MessagesDisplayLayoutBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )

    override fun onBindViewHolder(
        holder: FirebaseMessageViewHolder,
        position: Int
    ) {
        val item = getItem(position)
        if (item != null) {
            holder.bind(item)
        }
    }

    inner class FirebaseMessageViewHolder(private val binding: MessagesDisplayLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(message: FirebaseMessage) {
            binding.apply {
                if (message.senderId == mAuth.currentUser!!.uid) {
                    linearLayout1.isVisible = false
                    sentByTextView.text = message.username
                    sentByTextView.paint.isUnderlineText = true
                    messageSentTextView.text =
                        message.text + "\n\n" + android.text.format.DateFormat.format(
                            "yyyy-MM-dd hh:mm a",
                            message.time.toDate()
                        ).toString()
                } else {
                    linearLayout2.isVisible = false
                    usernameTextView.text = message.username
                    usernameTextView.paint.isUnderlineText = true
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