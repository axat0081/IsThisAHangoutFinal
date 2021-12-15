package com.example.isthisahangout.videos

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.isthisahangout.databinding.VideoPlaylistLayoutBinding
import com.example.isthisahangout.models.FirebaseVideo


class VideosAdapter(
    private val mediaObjects: ArrayList<FirebaseVideo>,
    private val listener: OnItemClickListener
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, i: Int): RecyclerView.ViewHolder {
        return VideoPlayerViewHolder(
            VideoPlaylistLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, i: Int) {
        (viewHolder as VideoPlayerViewHolder).bind(mediaObjects[i])
    }

    override fun getItemCount(): Int {
        return mediaObjects.size
    }

    interface OnItemClickListener {
        fun onItemClick(video: FirebaseVideo)
    }

    inner class VideoPlayerViewHolder(val binding: VideoPlaylistLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val item = mediaObjects[position]
                    listener.onItemClick(item)
                }
            }
        }

        @SuppressLint("SetTextI18n")
        fun bind(video: FirebaseVideo) {
            binding.apply {
                Glide.with(itemView)
                    .load(video.thumbnail)
                    .apply(object : RequestOptions() {}.override(600, 200))
                    .into(thumbnail)
                title.text = video.title
            }
        }
    }
}