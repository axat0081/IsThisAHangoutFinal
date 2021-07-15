package com.example.isthisahangout.adapter

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.isthisahangout.databinding.AnimeDisplayLayoutBinding
import com.example.isthisahangout.models.AnimeSeasonResults

class AnimeSeasonAdapter(private val listener: OnItemClickListener) :
    ListAdapter<AnimeSeasonResults.RoomAnimeBySeason, AnimeSeasonAdapter.AnimeSeasonViewHolder>(
        COMPARATOR
    ) {
    companion object {
        private val COMPARATOR =
            object : DiffUtil.ItemCallback<AnimeSeasonResults.RoomAnimeBySeason>() {
                override fun areItemsTheSame(
                    oldItem: AnimeSeasonResults.RoomAnimeBySeason,
                    newItem: AnimeSeasonResults.RoomAnimeBySeason
                ) = oldItem.title == newItem.title

                override fun areContentsTheSame(
                    oldItem: AnimeSeasonResults.RoomAnimeBySeason,
                    newItem: AnimeSeasonResults.RoomAnimeBySeason
                ) = oldItem == newItem
            }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnimeSeasonViewHolder {
        return AnimeSeasonViewHolder(
            AnimeDisplayLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: AnimeSeasonViewHolder, position: Int) {
        val item = getItem(position)
        if (item != null) {
            holder.bind(item)
        }
    }

    interface OnItemClickListener {
        fun onItemClick(anime: AnimeSeasonResults.RoomAnimeBySeason)
    }

    inner class AnimeSeasonViewHolder(private val binding: AnimeDisplayLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val item = getItem(position)
                    if (item != null) {
                        listener.onItemClick(item)
                    }
                }
            }
        }

        fun bind(anime: AnimeSeasonResults.RoomAnimeBySeason) {
            binding.apply {
                Glide.with(itemView)
                    .load(anime.imageUrl)
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            animeProgressBar.isVisible = false
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: Target<Drawable>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            animeProgressBar.isVisible = false
                            return false
                        }
                    }).into(animeImageView)
                animeTitleTextView.text = anime.title
            }
        }
    }
}