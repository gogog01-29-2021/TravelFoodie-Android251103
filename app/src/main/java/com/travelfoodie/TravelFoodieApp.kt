package com.travelfoodie

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.kakao.sdk.common.KakaoSdk
import com.navercorp.nid.NaverIdLoginSDK
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TravelFoodieApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        initializeSocialSDKs()
    }

    private fun initializeSocialSDKs() {
        // Initialize Kakao SDK
        KakaoSdk.init(this, BuildConfig.KAKAO_API_KEY)

        // Initialize Naver SDK
        NaverIdLoginSDK.initialize(
            context = this,
            clientId = BuildConfig.NAVER_CLIENT_ID,
            clientSecret = BuildConfig.NAVER_CLIENT_SECRET,
            clientName = getString(R.string.app_name)
        )
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_TRAVEL_REMINDERS,
                "Travel Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for upcoming trips"
                enableVibration(true)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_TRAVEL_REMINDERS = "travel_reminders"
    }
}
