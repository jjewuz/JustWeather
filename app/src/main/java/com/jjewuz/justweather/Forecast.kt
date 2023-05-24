package com.jjewuz.justweather

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit

class Forecast : Fragment() {

    private lateinit var temperatureTextView: TextView
    private lateinit var locationTextView: TextView
    private lateinit var updatedTextView: TextView
    private lateinit var weatherDescriptionTextView: TextView
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var feelslikeTextView: TextView
    private lateinit var minmaxTextView: TextView
    private lateinit var pressureTextView: TextView
    private lateinit var windTextView: TextView
    private lateinit var setupcaution: String

    private lateinit var updateToast: String

    private lateinit var updateBtn: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        updateToast = resources.getString(R.string.load)

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_forecast, container, false)

        setupcaution = resources.getString((R.string.setupcity))

        updateBtn = v.findViewById(R.id.update_button)

        sharedPreferences = requireActivity().getSharedPreferences("prefs", Context.MODE_PRIVATE)

        temperatureTextView = v.findViewById(R.id.temperature_textview)
        locationTextView = v.findViewById(R.id.location_textview)
        updatedTextView = v.findViewById(R.id.updated_textview)
        weatherDescriptionTextView = v.findViewById(R.id.weather_description_textview)
        feelslikeTextView = v.findViewById(R.id.feelslike_textview)
        minmaxTextView = v.findViewById(R.id.maxmin_textview)
        windTextView = v.findViewById(R.id.wind_textview)
        pressureTextView = v.findViewById(R.id.pressure_textview)

        updateBtn.setOnClickListener(){
            (activity as MainActivity?)!!.getWeather()
            Toast.makeText(this.requireActivity(), updateToast, Toast.LENGTH_SHORT).show()
            reloadWeather()
        }


        reloadWeather()


        return v
    }

    fun reloadWeather(){
        temperatureTextView.text = sharedPreferences.getString("MainTemp", "$setupcaution")
        feelslikeTextView.text = sharedPreferences.getString("FeelsLikeTxt", "")
        minmaxTextView.text = sharedPreferences.getString("MinMaxTxt", "")
        locationTextView.text = sharedPreferences.getString("CityTxt", "")
        pressureTextView.text = sharedPreferences.getString("PressureTxt", "")
        windTextView.text = sharedPreferences.getString("WindTxt", "")
        updatedTextView.text = sharedPreferences.getString("UpdatedTxt", "")
        weatherDescriptionTextView.text = sharedPreferences.getString("DesciptionTxt", "")
    }

}