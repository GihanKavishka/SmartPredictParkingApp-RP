package com.example.androidsmartpredictparkinggihan.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
object BookingNotificationHelper {
    private const val CHANNEL_ID = "owner_booking_alerts"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "House owner booking alerts",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Notifications when a driver books your parking space"
        }
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    fun showOwnerBookingNotification(
        context: Context,
        ownerUserId: Long,
        bookingId: Long,
        title: String,
        message: String,
    ) {
        ensureChannel(context)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        val notificationId = (ownerUserId * 100_000 + bookingId).toInt()
        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (_: SecurityException) {
            // POST_NOTIFICATIONS not granted — in-app owner list still shows booking
        }
    }
}
