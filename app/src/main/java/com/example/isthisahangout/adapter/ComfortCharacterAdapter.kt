package com.example.isthisahangout.adapter

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.isthisahangout.R
import com.example.isthisahangout.databinding.AnimeDisplayLayoutBigBinding
import com.example.isthisahangout.models.ComfortCharacter
import com.example.isthisahangout.utils.comfortCharacterQuery
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions

class ComfortCharacterAdapter(private val listener: OnItemClickListener) :
    FirebaseRecyclerAdapter<ComfortCharacter, ComfortCharacterAdapter.ComfortCharacterViewHolder>(
        options
    ) {


    companion object {
        val options = FirebaseRecyclerOptions.Builder<ComfortCharacter>()
            .setQuery(comfortCharacterQuery, ComfortCharacter::class.java)
            .build()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComfortCharacterViewHolder =
        ComfortCharacterViewHolder(
            AnimeDisplayLayoutBigBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(
        holder: ComfortCharacterViewHolder,
        position: Int,
        model: ComfortCharacter
    ) {
        val item = getItem(position)
        holder.bind(item)
    }

    interface OnItemClickListener{
        fun onItemClick(character: ComfortCharacter)
    }

    inner class ComfortCharacterViewHolder(private val binding: AnimeDisplayLayoutBigBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if(position!=RecyclerView.NO_POSITION){
                    val item = getItem(position)
                    listener.onItemClick(item)
                }
            }
        }

        fun bind(character: ComfortCharacter) {
            binding.apply {
                Glide.with(itemView)
                    .load(character.image)
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            animeProgressBarBig.isVisible = false
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: Target<Drawable>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            animeProgressBarBig.isVisible = false
                            return false
                        }
                    }).error(R.drawable.image_not_available)
                    .into(animeImageViewBig)
                animeTitleTextView.text = character.name
            }
        }
    }
}