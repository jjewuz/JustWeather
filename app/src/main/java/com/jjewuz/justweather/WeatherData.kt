package com.jjewuz.justweather

import android.media.Image
import android.widget.ImageView

data class WeatherData(
    val day: String,
    val condition: String,
    val temperature: String,
    val image: Int
)