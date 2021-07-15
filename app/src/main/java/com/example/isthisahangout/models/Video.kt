package com.example.isthisahangout.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Video(
    val id: String? = null,
    val title: String? = null,
    val text: String? = null,
    val url: String? = null,
    val time: Long? = null,
    val username: String? = null,
    val pfp: String? = null,
    val thumbnail: String? = null
):Parcelable
