package com.example.isthisahangout

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.widget.Toast
import com.google.firebase.messaging.FirebaseMessaging
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.twitter.TwitterEmojiProvider
import dagger.hilt.android.HiltAndroidApp

private const val NOTIFICATION_ID = "notification_channel"
private const val CHANNEL_NAME = "notification_channel"

@HiltAndroidApp
class HomeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        EmojiManager.install(TwitterEmojiProvider())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
        FirebaseMessaging.getInstance().subscribeToTopic("general")
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Toast.makeText(this, "Aw snap!!!", Toast.LENGTH_SHORT).show()
                }
            }
    }
}