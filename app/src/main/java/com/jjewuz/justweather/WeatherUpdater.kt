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
import androidx.work.Worker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WeatherUpdater (context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    private val client = OkHttpClient()
    
    private lateinit var currWeather: String
    private lateinit var nowTxt: String
    private lateinit var descTxt: String
    private lateinit var feels: String

    private lateinit var updatedLast: String
    private lateinit var pressureTxt: String
    private lateinit var cityEnter: String
    private lateinit var site: String
    private lateinit var wind_speed: String
    private lateinit var minTxt: String
    private lateinit var maxTxt: String

    private lateinit var measurment: String
    private lateinit var measureTxt: String

    private lateinit var sharedPreferences: SharedPreferences

    override fun doWork(): Result {
        return try {
            currWeather = applicationContext.resources.getString(R.string.currWeather)
            nowTxt = applicationContext.resources.getString(R.string.nowText)
            descTxt = applicationContext.resources.getString(R.string.notificationDesc)
            feels = applicationContext.resources.getString(R.string.feels)

            updatedLast = applicationContext.resources.getString(R.string.updated_last)
            pressureTxt = applicationContext.resources.getString(R.string.pressure)
            cityEnter = applicationContext.resources.getString(R.string.city_enter)
            site = applicationContext.resources.getString(R.string.jjewuz_site)
            wind_speed = applicationContext.resources.getString((R.string.wind_speed))
            minTxt = applicationContext.resources.getString(R.string.min)
            maxTxt = applicationContext.resources.getString(R.string.max)


            sharedPreferences = applicationContext.getSharedPreferences("prefs", Context.MODE_PRIVATE)

            measurment = sharedPreferences.getString("measure", "metric").toString()
            if (measurment == "metric"){
                measureTxt = "°С"
            } else {
                measureTxt = "°F"
            }

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
                sharedPreferences =
                    applicationContext.getSharedPreferences("prefs", Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()



                val responseString = responseBody.string()
                val jsonObject = JSONObject(responseString)
                val main = jsonObject.getJSONObject("main")
                val wind = jsonObject.getJSONObject("wind")
                val temperature = main.getString("temp")
                val feelslike = main.getString("feels_like")
                val pressure = main.getString("pressure")
                val windspeed = wind.getString("speed")
                val min = main.getString("temp_min")
                val max = main.getString("temp_max")
                val cityName = jsonObject.getString("name")
                val weatherArray = jsonObject.getJSONArray("weather")
                val weather = weatherArray.getJSONObject(0)
                val description = weather.getString("description")
                val updatedOn = jsonObject.getString("dt")
                val updatedAt: String = if (measurment == "metric"){
                    SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(updatedOn.toLong() * 1000))
                } else{
                    SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(updatedOn.toLong() * 1000))
                }

                withContext(Dispatchers.Main) {
                    editor.putString("MainTemp", "${temperature.toFloat().toInt()} $measureTxt")
                    editor.putString("FeelsLikeTxt",  "$feels ${feelslike.toFloat().toInt()} $measureTxt")
                    editor.putString("MinMaxTxt", "$minTxt ${min.toFloat().toInt()}°С\n$maxTxt ${max.toFloat().toInt()} $measureTxt")
                    editor.putString("CityTxt", cityName)
                    editor.putString("PressureTxt", "${pressureTxt} $pressure hPA")
                    editor.putString("WindTxt", "${wind_speed} $windspeed m/s")
                    editor.putString("UpdatedTxt", "${updatedLast} $updatedAt")
                    editor.putString("DesciptionTxt", description)
                    editor.putInt("temperature", temperature.toFloat().toInt())
                    editor.putBoolean("isLoaded", true)
                    editor.apply()
                }

                val title = "$nowTxt ${temperature.toFloat().toInt()} $measureTxt"
                val message = "$feels ${feelslike.toFloat().toInt()} $measureTxt"

                pushWidget()

                    val mNotificationManager =
                        applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    val channel = NotificationChannel(
                        "0",
                        currWeather,
                        NotificationManager.IMPORTANCE_DEFAULT
                    )
                    channel.description = descTxt
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