package com.example.isthisahangout.notifications

import android.R
import android.annotation.SuppressLint
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class NotificationService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        notify(
            remoteMessage.notification!!.title,
            remoteMessage.notification!!.body
        )
    }

    private fun notify(title: String?, message: String?) {
        val builder = NotificationCompat.Builder(this, "notification_channel")
            .setSmallIcon(R.drawable.ic_notification_clear_all)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
        val managerCompat = NotificationManagerCompat.from(this)
        managerCompat.notify(123, builder.build())
    }
}