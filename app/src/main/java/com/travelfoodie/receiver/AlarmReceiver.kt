package com.travelfoodie.receiver

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.travelfoodie.MainActivity
import com.travelfoodie.R
import com.travelfoodie.TravelFoodieApp

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // Support both old and new intent extras format
        val tripTitle = intent.getStringExtra(EXTRA_TRIP_TITLE)
            ?: intent.getStringExtra("trip_title") ?: return
        val notifType = intent.getStringExtra(EXTRA_NOTIF_TYPE)
            ?: intent.getStringExtra("notif_type") ?: return
        val nickname = intent.getStringExtra(EXTRA_NICKNAME)
            ?: intent.getStringExtra("nickname") ?: "여행자"

        val (title, message) = when (notifType) {
            "D-7" -> Pair(
                context.getString(R.string.notif_d7_title),
                context.getString(R.string.notif_d7_message, nickname, tripTitle)
            )
            "D-3" -> Pair(
                context.getString(R.string.notif_d3_title),
                context.getString(R.string.notif_d3_message)
            )
            "D-0" -> Pair(
                context.getString(R.string.notif_d0_title),
                context.getString(R.string.notif_d0_message)
            )
            else -> return
        }

        showNotification(context, title, message)
    }

    private fun showNotification(context: Context, title: String, message: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, TravelFoodieApp.CHANNEL_TRAVEL_REMINDERS)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    companion object {
        const val EXTRA_TRIP_TITLE = "trip_title"
        const val EXTRA_NOTIF_TYPE = "notif_type"
        const val EXTRA_NICKNAME = "nickname"
    }
}
