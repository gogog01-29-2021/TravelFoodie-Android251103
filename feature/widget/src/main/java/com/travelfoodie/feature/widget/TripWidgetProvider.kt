package com.travelfoodie.feature.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.travelfoodie.core.data.local.AppDatabase
import com.travelfoodie.feature.widget.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class TripWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_trip)

        // Load trip data from database asynchronously
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = AppDatabase.getInstance(context)
                val currentTime = System.currentTimeMillis()

                // Get next upcoming trip (soonest start date in the future)
                val nextTrip = database.tripDao().getUpcomingTrips(currentTime)
                    .firstOrNull()

                if (nextTrip != null) {
                    // Calculate D-day
                    val daysUntil = TimeUnit.MILLISECONDS.toDays(nextTrip.startDate - currentTime)
                    val dDayText = when {
                        daysUntil < 0 -> "완료"
                        daysUntil == 0L -> "D-Day"
                        else -> "D-$daysUntil"
                    }

                    // Get region for this trip
                    val regions = database.regionDao().getRegionsByTripId(nextTrip.tripId)
                    val firstRegion = regions.firstOrNull()

                    // Count POIs and restaurants
                    var poiCount = 0
                    var restaurantCount = 0

                    if (firstRegion != null) {
                        poiCount = database.poiDao().getPoiByRegionId(firstRegion.regionId).size
                        restaurantCount = database.restaurantDao().getRestaurantsByRegionId(firstRegion.regionId).size
                    }

                    // Format date range
                    val dateFormat = SimpleDateFormat("MM/dd", Locale.getDefault())
                    val startDate = dateFormat.format(Date(nextTrip.startDate))
                    val endDate = dateFormat.format(Date(nextTrip.endDate))
                    val dateRange = "$startDate - $endDate"

                    // Update widget on main thread
                    CoroutineScope(Dispatchers.Main).launch {
                        views.setTextViewText(R.id.widget_trip_title, nextTrip.title)
                        views.setTextViewText(R.id.widget_trip_dday, dDayText)

                        val region = firstRegion?.name ?: ""
                        val regionText = if (region.isNotEmpty()) "📍 $region" else ""
                        views.setTextViewText(R.id.widget_trip_dates, dateRange)
                        views.setTextViewText(R.id.widget_trip_region, regionText)
                        views.setTextViewText(R.id.widget_trip_info, "명소 ${poiCount}개 / 맛집 ${restaurantCount}개")

                        // Add click intent to open app
                        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
                        val pendingIntent = PendingIntent.getActivity(
                            context,
                            0,
                            intent,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                        views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)

                        appWidgetManager.updateAppWidget(appWidgetId, views)
                    }
                } else {
                    // No upcoming trips
                    CoroutineScope(Dispatchers.Main).launch {
                        views.setTextViewText(R.id.widget_trip_title, "예정된 여행 없음")
                        views.setTextViewText(R.id.widget_trip_dday, "--")
                        views.setTextViewText(R.id.widget_trip_info, "새로운 여행을 계획하세요")

                        // Add click intent to open app
                        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
                        val pendingIntent = PendingIntent.getActivity(
                            context,
                            0,
                            intent,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                        views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)

                        appWidgetManager.updateAppWidget(appWidgetId, views)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Fallback to default display
                CoroutineScope(Dispatchers.Main).launch {
                    views.setTextViewText(R.id.widget_trip_title, "여행 정보 로딩 실패")
                    views.setTextViewText(R.id.widget_trip_dday, "--")
                    views.setTextViewText(R.id.widget_trip_info, "앱을 열어 확인하세요")
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            }
        }
    }
}
