package com.jjewuz.justweather

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.getSystemService
import androidx.work.Worker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class WeatherUpdater (context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    private val client = OkHttpClient()
    private lateinit var sharedPreferences: SharedPreferences
    
    private lateinit var currWeather: String
    private lateinit var nowTxt: String
    private lateinit var feels: String

    override fun doWork(): Result {
        return try {
            val url = inputData.getString(URL)
            if (url != null) {
                getWeather(url)
            }
            Result.success()
        } catch (e: Exception){
            Result.failure()
        }

    }

    private fun getWeather(url: String){
        GlobalScope.launch(Dispatchers.IO) {
            val request = Request.Builder()
                .url(url)
                .build()
            val response = client.newCall(request).execute()
            val responseBody = response.body
            val feel = R.string.feels.toString()
            if (responseBody != null) {
                val responseString = responseBody.string()
                val jsonObject = JSONObject(responseString)
                val main = jsonObject.getJSONObject("main")
                val temperature = main.getString("temp")
                val feelslike = main.getString("feels_like")

                val title = "${temperature.toFloat().toInt()}°С"
                val message = "${feelslike.toFloat().toInt()}°С"

                sharedPreferences =
                    applicationContext.getSharedPreferences("prefs", Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()

                withContext(Dispatchers.Main) {
                    editor.putString("MainTemp", "${temperature.toFloat().toInt()}°С")
                    editor.putString("FeelsLikeTxt", "$feel ${feelslike.toFloat().toInt()}°С")
                    editor.putInt("temperature", temperature.toFloat().toInt())
                    editor.apply()
                }

                pushWidget()

                    val mNotificationManager =
                        applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    val channel = NotificationChannel(
                        "0",
                        R.string.currWeather.toString(),
                        NotificationManager.IMPORTANCE_DEFAULT
                    )
                    channel.description = R.string.notificationDesc.toString()
                    mNotificationManager.createNotificationChannel(channel)
                    val mBuilder = NotificationCompat.Builder(applicationContext, "0")
                        .setSmallIcon(R.drawable.cloud)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setAutoCancel(true)
                    val intent = Intent(applicationContext, MainActivity::class.java)
                    val pi = PendingIntent.getActivity(
                        applicationContext,
                        0,
                        intent,
                        PendingIntent.FLAG_IMMUTABLE
                    )
                    mBuilder.setContentIntent(pi)
                    mNotificationManager.notify(0, mBuilder.build())
                }
            }
    }

    private fun pushWidget(){
        val intent = Intent(applicationContext, WeatherWidget::class.java)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        val ids: IntArray = AppWidgetManager.getInstance(applicationContext)
            .getAppWidgetIds(ComponentName(applicationContext, WeatherWidget::class.java))
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        applicationContext.sendBroadcast(intent)
    }

    companion object {
        const val URL = "url"
    }

}