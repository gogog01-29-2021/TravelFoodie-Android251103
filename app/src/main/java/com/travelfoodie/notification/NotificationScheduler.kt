package com.travelfoodie.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.travelfoodie.core.data.local.entity.TripEntity
import com.travelfoodie.receiver.AlarmReceiver
import java.util.concurrent.TimeUnit

object NotificationScheduler {

    fun scheduleNotificationsForTrip(context: Context, trip: TripEntity, nickname: String = "여행자") {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Cancel existing notifications for this trip
        cancelNotificationsForTrip(context, trip.tripId)

        // Schedule D-7 notification
        val d7Time = trip.startDate - TimeUnit.DAYS.toMillis(7)
        if (d7Time > System.currentTimeMillis()) {
            scheduleNotification(
                context,
                alarmManager,
                trip,
                d7Time,
                "D-7",
                nickname,
                getRequestCode(trip.tripId, "D-7")
            )
        }

        // Schedule D-3 notification
        val d3Time = trip.startDate - TimeUnit.DAYS.toMillis(3)
        if (d3Time > System.currentTimeMillis()) {
            scheduleNotification(
                context,
                alarmManager,
                trip,
                d3Time,
                "D-3",
                nickname,
                getRequestCode(trip.tripId, "D-3")
            )
        }

        // Schedule D-0 notification (8 AM on trip day)
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = trip.startDate
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 8)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        
        val d0Time = calendar.timeInMillis
        if (d0Time > System.currentTimeMillis()) {
            scheduleNotification(
                context,
                alarmManager,
                trip,
                d0Time,
                "D-0",
                nickname,
                getRequestCode(trip.tripId, "D-0")
            )
        }
    }

    private fun scheduleNotification(
        context: Context,
        alarmManager: AlarmManager,
        trip: TripEntity,
        triggerTime: Long,
        notifType: String,
        nickname: String,
        requestCode: Int
    ) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(AlarmReceiver.EXTRA_TRIP_TITLE, trip.title)
            putExtra(AlarmReceiver.EXTRA_NOTIF_TYPE, notifType)
            putExtra(AlarmReceiver.EXTRA_NICKNAME, nickname)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Use exact alarm for precise timing
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }

    fun cancelNotificationsForTrip(context: Context, tripId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        listOf("D-7", "D-3", "D-0").forEach { notifType ->
            val intent = Intent(context, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                getRequestCode(tripId, notifType),
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )

            pendingIntent?.let {
                alarmManager.cancel(it)
                it.cancel()
            }
        }
    }

    private fun getRequestCode(tripId: String, notifType: String): Int {
        // Generate unique request code from tripId and notifType
        return (tripId + notifType).hashCode()
    }

    fun rescheduleAllNotifications(context: Context) {
        // This should be called from BootReceiver to reschedule all notifications after device restart
        // Implementation would load all future trips from database and reschedule
    }
}
