package com.example.isthisahangout

import android.app.Application
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.twitter.TwitterEmojiProvider
import dagger.hilt.android.HiltAndroidApp


@HiltAndroidApp
class HomeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        EmojiManager.install(TwitterEmojiProvider())
        Firebase.database.setPersistenceEnabled(true)
    }
}