package com.jjewuz.justweather

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject


class Settings : Fragment() {

    private lateinit var cityEditText: EditText
    private lateinit var cityListView: ListView
    private lateinit var notifSett: MaterialCardView
    private lateinit var sharedPreferences: SharedPreferences

    private var cityId = 5391959
    private val apiKey = BuildConfig.API_KEY

    private val client = OkHttpClient()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_settings, container, false)
        cityEditText = v.findViewById(R.id.city_edittext)
        cityListView = v.findViewById(R.id.city_listview)
        notifSett = v.findViewById(R.id.card3)

        sharedPreferences = requireActivity().getSharedPreferences("prefs", Context.MODE_PRIVATE)

        cityEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchCities(s.toString())
            }
        })

        notifSett.setOnClickListener {
            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            val uri = Uri.fromParts("package", requireActivity().packageName, null)
            intent.data = uri
            startActivity(intent)
        }

        cityId = loadSelectedCity()
        (activity as MainActivity?)!!.getWeather(false)

        cityListView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val selectedCity = parent.getItemAtPosition(position) as City
            cityId = selectedCity.id
            saveSelectedCity(cityId)
            cityEditText.setText(selectedCity.name)
            cityListView.visibility = View.GONE
            (activity as MainActivity?)!!.getWeather(false)
            cityEditText.text.clear()
            searchCities("хуйня")
        }

        return v
    }

    fun saveSelectedCity(cityId: Int) {
        val editor = sharedPreferences.edit()
        editor.putInt("selectedCityId", cityId)
        editor.apply()
    }

    fun loadSelectedCity(): Int {
        val cityId = sharedPreferences.getInt("selectedCityId", 0)
        if (cityId == 0) {
            return 5391959 // San Francisco
        }
        return cityId
    }


    data class City(val id: Int, val name: String, val country: String)

    class CityAdapter(context: Context, cities: List<City>) : ArrayAdapter<City>(context, 0, cities) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val city = getItem(position)
            val view = convertView ?: LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false)
            val textView = view.findViewById<TextView>(android.R.id.text1)
            if (city != null) {
                textView.text = "${city.name}, ${city.country}"
            }
            return view
        }
    }

    fun searchCities(query: String) {
        GlobalScope.launch(Dispatchers.IO) {
            val request = Request.Builder()
                .url("https://api.openweathermap.org/data/2.5/find?q=$query&type=like&units=metrics&appid=$apiKey")
                .build()
            val response = client.newCall(request).execute()
            val responseBody = response.body
            if (responseBody != null) {
                val responseString = responseBody.string()
                val jsonObject = JSONObject(responseString)
                val list = jsonObject.optJSONArray("list")
                if (list == null) {

                } else {
                    val cities = mutableListOf<City>()
                    for (i in 0 until list.length()) {
                        val cityJson = list.getJSONObject(i)
                        val city = City(
                            cityJson.getInt("id"),
                            cityJson.getString("name"),
                            cityJson.getJSONObject("sys").getString("country")
                        )
                        cities.add(city)
                    }
                    withContext(Dispatchers.Main) {
                        cityListView.visibility = View.VISIBLE
                        cityListView.adapter =
                            CityAdapter(requireContext().applicationContext, cities)

                    }
                }
            }
        }

    }
}