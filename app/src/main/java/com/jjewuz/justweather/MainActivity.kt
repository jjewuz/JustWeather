package com.jjewuz.justweather

import android.Manifest.permission.POST_NOTIFICATIONS
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.appwidget.AppWidgetManager
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.work.Data
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.material.color.DynamicColors
import com.jjewuz.justweather.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {

    private var cityId = 5391959
    private val apiKey = BuildConfig.API_KEY

    private lateinit var updatedLast: String
    private lateinit var feel: String
    private lateinit var pressureTxt: String
    private lateinit var cityEnter: String
    private lateinit var site: String
    private lateinit var wind_speed: String
    private lateinit var minTxt: String
    private lateinit var maxTxt: String
    private lateinit var nowTxt: String

    private lateinit var sharedPreferences: SharedPreferences

    private val client = OkHttpClient()


    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        supportActionBar?.hide()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(POST_NOTIFICATIONS), 100)
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        replaceFragment(Forecast())

        pushWidget(this@MainActivity, "-0C")

        sharedPreferences = getSharedPreferences("prefs", Context.MODE_PRIVATE)

        updatedLast = resources.getString(R.string.updated_last)
        feel = resources.getString(R.string.feels)
        pressureTxt = resources.getString(R.string.pressure)
        val ver = resources.getString(R.string.ver)
        cityEnter = resources.getString(R.string.city_enter)
        site = resources.getString(R.string.jjewuz_site)
        wind_speed = resources.getString((R.string.wind_speed))
        minTxt = resources.getString(R.string.min)
        maxTxt = resources.getString(R.string.max)
        nowTxt = resources.getString((R.string.nowText))

        getWeather()

        val weatherUpdate =
            PeriodicWorkRequestBuilder<WeatherUpdater>(
                    1, TimeUnit.HOURS,
                    15, TimeUnit.MINUTES)
                .setInputData(Data.Builder().putString(WeatherUpdater.URL, getUrl()).build())
                .build()

        WorkManager.getInstance(this).enqueue(weatherUpdate)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            DynamicColors.applyToActivitiesIfAvailable(application)
        }

        binding.bottomNavView?.setOnItemSelectedListener {
            when(it.itemId){
                R.id.forecast -> replaceFragment(Forecast())
                R.id.city -> replaceFragment(Settings())
                R.id.information -> replaceFragment(Info())
                else ->{

                }
            }
            true
        }

        binding.navigationRail?.setOnItemSelectedListener {  menuItem ->
            when (menuItem.itemId) {
                R.id.forecast -> replaceFragment(Forecast())
                R.id.city -> replaceFragment(Settings())
                R.id.information -> replaceFragment(Info())
                else ->{

                }
            }

            true
        }
    }

    fun saveSelectedCity(cityId: Int) {
        val editor = sharedPreferences.edit()
        editor.putInt("selectedCityId", cityId)
        editor.apply()
    }

    private fun loadSelectedCity(): Int {
        val cityId = sharedPreferences.getInt("selectedCityId", 0)
        if (cityId == 0) {
            return 5391959 // San Francisco
        }
        return cityId
    }

    private fun getUrl(): String{
        val lang = Locale.getDefault().language
        val url = "https://api.openweathermap.org/data/2.5/weather?id=${loadSelectedCity()}&appid=$apiKey&lang=$lang&units=metric"

        return url
    }

    fun getWeather() {
        GlobalScope.launch(Dispatchers.IO) {
            sharedPreferences = getSharedPreferences("prefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            val request = Request.Builder()
                .url(getUrl())
                .build()
            val response = client.newCall(request).execute()
            val responseBody = response.body
            if (responseBody != null) {
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
                val updatedAt: String = SimpleDateFormat("h:mm a", Locale.ENGLISH).format(Date(updatedOn.toLong() * 1000))

                withContext(Dispatchers.Main) {
                    editor.putString("MainTemp", "${temperature.toFloat().toInt()}°С")
                    editor.putString("FeelsLikeTxt",  "${feel} ${feelslike.toFloat().toInt()}°С")
                    editor.putString("MinMaxTxt", "$minTxt ${min.toFloat().toInt()}°С\n$maxTxt ${max.toFloat().toInt()}°С")
                    editor.putString("CityTxt", cityName)
                    editor.putString("PressureTxt", "${pressureTxt} $pressure hPA")
                    editor.putString("WindTxt", "${wind_speed} $windspeed m/s")
                    editor.putString("UpdatedTxt", "${updatedLast} $updatedAt")
                    editor.putString("DesciptionTxt", "$description")
                    editor.putInt("temperature", temperature.toFloat().toInt())
                    editor.putBoolean("isLoaded", true)
                    editor.apply()
                }
                pushWidget(this@MainActivity, temperature )
            }
        }

    }
    private fun pushWidget(context: Context, temp: String ){
        val intent = Intent(this, WeatherWidget::class.java)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        val ids: IntArray = AppWidgetManager.getInstance(application)
            .getAppWidgetIds(ComponentName(getApplication(), WeatherWidget::class.java))
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        sendBroadcast(intent)
    }

    private fun replaceFragment(fragment : Fragment){
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
        fragmentTransaction.replace(R.id.frame_layout, fragment)
        fragmentTransaction.commit ()
    }

}

