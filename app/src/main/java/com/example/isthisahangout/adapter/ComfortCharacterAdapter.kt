package com.example.isthisahangout.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.isthisahangout.models.ComfortCharacter
import com.firebase.ui.database.FirebaseRecyclerAdapter

class ComfortCharacterAdapter :
    FirebaseRecyclerAdapter<ComfortCharacter, ComfortCharacterAdapter.ComfortCharacterViewHolder>() {



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComfortCharacterViewHolder {
        TODO("Not yet implemented")
    }

    override fun onBindViewHolder(
        holder: ComfortCharacterViewHolder,
        position: Int,
        model: ComfortCharacter
    ) {
        TODO("Not yet implemented")
    }

    inner class ComfortCharacterViewHolder() : RecyclerView.ViewHolder() {

    }
}