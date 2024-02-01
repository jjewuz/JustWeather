package com.jjewuz.justweather

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.internal.toImmutableList
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FutureWeather : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var weatherAdapter: WeatherAdapter
    private lateinit var loading: ProgressBar

    private val client = OkHttpClient()
    private val apiKey = BuildConfig.API_KEY
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var measurment: String
    private lateinit var measureTxt: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_future_weather, container, false)

        recyclerView = rootView.findViewById(R.id.recyclerView)
        weatherAdapter = WeatherAdapter()
        loading = rootView.findViewById(R.id.loading)

        sharedPreferences = requireActivity().getSharedPreferences("prefs", Context.MODE_PRIVATE)

        measurment = sharedPreferences.getString("measure", "metric").toString()
        measureTxt = if (measurment == "metric") {
            "°С"
        } else {
            "°F"
        }

        weatherAdapter.registerAdapterDataObserver(object: AdapterDataObserver(){
            override fun onChanged() {
                val count = weatherAdapter.itemCount
                if (count == 0){
                    loading.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                }
                else {
                    loading.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                }
            }
        })

        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = weatherAdapter
        }


        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            val weatherDataList = mutableListOf<WeatherData>()
            val request = Request.Builder().url(getUrl()).build()
            try {
                val response = withContext(Dispatchers.IO) {
                    client.newCall(request).execute()
                }
                val responseBody: String? = response.body?.string()
                if (response.isSuccessful && !responseBody.isNullOrEmpty()) {
                    val jsonObject = JSONObject(responseBody)
                    val jsonArray = jsonObject.getJSONArray("list")

                    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

                    for (i in 0 until jsonArray.length()) {
                        val item = jsonArray.getJSONObject(i)
                        val dt = item.getInt("dt")
                        val main = item.getJSONObject("main")
                        val temp = main.getInt("temp")
                        val weatherArray = item.getJSONArray("weather")
                        val weatherObject = weatherArray.getJSONObject(0)
                        val icon = weatherObject.getInt("id")
                        val description = weatherObject.getString("description")

                        val data: String = if (measurment == "metric"){
                            SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date(dt.toLong() * 1000))
                        } else{
                            SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault()).format(Date(dt.toLong() * 1000))
                        }

                        val image: Int
                        if (icon == 800){
                            image = R.drawable.weather
                        } else if (icon / 100 == 8){
                            image = R.drawable.cloud_weather
                        } else if (icon / 100 == 7){
                            image = R.drawable.mist
                        } else if (icon / 100 == 6){
                            image = R.drawable.snow
                        } else if (icon / 100 == 5){
                            image = R.drawable.rainy
                        } else if (icon == 300){
                            image = R.drawable.cloud
                        } else if (icon / 100 == 3){
                            image = R.drawable.drizzle
                        } else if (icon / 100 == 2){
                            image = R.drawable.thunderstorm
                        } else {
                            image = R.drawable.buttonc
                        }
                        val weather = WeatherData(data, description, temp.toString() + measureTxt, image)
                        weatherDataList.add(weather)
                    }
                    }
                    weatherAdapter.setData(weatherDataList)
                    response.body?.close()
                } catch (e: Exception) {

                }
            }



        return rootView
    }


    private fun getLat(): String? {
        return sharedPreferences.getString("citylat", "0.0")
    }

    private fun getLon(): String? {
        return sharedPreferences.getString("citylon", "0.0")
    }

    fun getUrl(): String{
        val lang = Locale.getDefault().language
        val url = "https://api.openweathermap.org/data/2.5/forecast?lat=${getLat()}&lon=${getLon()}&appid=$apiKey&lang=$lang&units=$measurment&cnt=18"
        return url
    }
}