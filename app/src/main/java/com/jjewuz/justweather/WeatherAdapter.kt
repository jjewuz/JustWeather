package com.jjewuz.justweather

import android.media.Image
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class WeatherAdapter : RecyclerView.Adapter<WeatherAdapter.WeatherViewHolder>() {

    private var weatherDataList: List<WeatherData> = emptyList()

    fun setData(data: List<WeatherData>) {
        weatherDataList = data
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeatherViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_weather, parent, false)
        return WeatherViewHolder(view)
    }

    override fun onBindViewHolder(holder: WeatherViewHolder, position: Int) {
        val weatherData = weatherDataList[position]
        holder.bind(weatherData)
    }

    override fun getItemCount(): Int = weatherDataList.size

    inner class WeatherViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dayTextView: TextView = itemView.findViewById(R.id.dayTextView)
        private val conditionTextView: TextView = itemView.findViewById(R.id.conditionTextView)
        private val temperatureTextView: TextView = itemView.findViewById(R.id.temperatureTextView)
        private val imageView: ImageView = itemView.findViewById(R.id.image)

        fun bind(weatherData: WeatherData) {
            dayTextView.text = weatherData.day
            conditionTextView.text = weatherData.condition
            temperatureTextView.text = weatherData.temperature
            imageView.setImageResource(weatherData.image)
        }
    }
}