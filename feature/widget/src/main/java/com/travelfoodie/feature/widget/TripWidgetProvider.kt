package com.travelfoodie.feature.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.travelfoodie.R

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
        // TODO: Load next trip from database
        val views = RemoteViews(context.packageName, R.layout.widget_trip)
        
        // Update widget views
        views.setTextViewText(R.id.widget_trip_title, "다음 여행")
        views.setTextViewText(R.id.widget_trip_dday, "D-5")
        views.setTextViewText(R.id.widget_trip_info, "명소 5개 / 맛집 10개")

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}
