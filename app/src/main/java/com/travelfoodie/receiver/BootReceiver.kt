package com.travelfoodie.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // TODO: Reschedule all pending notifications
            // This would require accessing the database to get all NotifScheduleEntity
            // and rescheduling them with AlarmManager
        }
    }
}
