package com.travelfoodie.core.ui

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

/**
 * Helper class for showing immediate notifications
 * Used for instant feedback when user actions complete (e.g., trip creation)
 */
object NotificationHelper {

    private const val CHANNEL_ID = "travel_reminders"
    private const val MAIN_ACTIVITY_CLASS = "com.travelfoodie.MainActivity"

    /**
     * Show notification when a new trip is created
     *
     * @param context Application context
     * @param tripTitle Title of the created trip
     * @param attractionCount Number of attractions generated
     * @param restaurantCount Number of restaurants generated
     */
    fun showTripCreatedNotification(
        context: Context,
        tripTitle: String,
        attractionCount: Int,
        restaurantCount: Int
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create intent to open app when notification is tapped using reflection to avoid direct dependency
        val intent = Intent().apply {
            component = android.content.ComponentName(
                context.packageName,
                MAIN_ACTIVITY_CLASS
            )
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("open_trips", true)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Build notification - get strings from app's resources
        val title = try {
            context.getString(
                context.resources.getIdentifier("notif_trip_created_title", "string", context.packageName)
            )
        } catch (e: Exception) {
            "새 여행이 생성되었습니다! ✈️"
        }

        val message = try {
            context.getString(
                context.resources.getIdentifier("notif_trip_created_message", "string", context.packageName),
                tripTitle,
                attractionCount,
                restaurantCount
            )
        } catch (e: Exception) {
            "$tripTitle 여행이 생성되었습니다. 명소 ${attractionCount}개와 맛집 ${restaurantCount}개를 확인해보세요!"
        }

        // Get launcher icon
        val iconResId = try {
            context.resources.getIdentifier("ic_launcher", "mipmap", context.packageName)
        } catch (e: Exception) {
            android.R.drawable.ic_dialog_info
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(iconResId)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Show as heads-up notification
            .setContentIntent(pendingIntent)
            .setAutoCancel(true) // Dismiss when tapped
            .setVibrate(longArrayOf(0, 300, 100, 300)) // Vibration pattern
            .build()

        // Show notification with unique ID
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    /**
     * Show a simple notification with title and message
     * Generic method for other types of notifications
     */
    fun showSimpleNotification(
        context: Context,
        title: String,
        message: String
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent().apply {
            component = android.content.ComponentName(
                context.packageName,
                MAIN_ACTIVITY_CLASS
            )
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val iconResId = try {
            context.resources.getIdentifier("ic_launcher", "mipmap", context.packageName)
        } catch (e: Exception) {
            android.R.drawable.ic_dialog_info
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(iconResId)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
