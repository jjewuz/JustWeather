package com.jjewuz.justweather

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.widget.RemoteViews
import android.content.ComponentName

/**
 * Implementation of App Widget functionality.
 */
class WeatherWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {

            updateWidget(context)
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

}

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {

    val views = RemoteViews(context.packageName, R.layout.weather_widget)

    val sharedPreferences = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
    val widgetText = sharedPreferences.getString("MainTemp", "0")



    views.setTextViewText(R.id.appwidget_text, "$widgetText")

    val intent = Intent(context, MainActivity::class.java)
    val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

    views.setOnClickPendingIntent(android.R.id.background, pendingIntent)

    appWidgetManager.updateAppWidget(appWidgetId, views)
}

fun updateWidget(context: Context){
    val sharedPreferences = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
    val appWidgetManager = AppWidgetManager.getInstance(context)
    val views = RemoteViews(context.packageName, R.layout.weather_widget)
    val appWidgetIds = appWidgetManager.getAppWidgetIds( ComponentName(context, WeatherWidget::class.java))
    views.setTextViewText(R.id.appwidget_text, "${sharedPreferences.getInt("temperature", 0)}°С")
    appWidgetManager.updateAppWidget(appWidgetIds, views)
}

