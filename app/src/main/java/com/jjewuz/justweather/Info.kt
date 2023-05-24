package com.jjewuz.justweather

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment


class Info : Fragment() {

    private lateinit var version: TextView

    private lateinit var ghbtn: Button
    private lateinit var sitebtn: Button
    private lateinit var ratebtn: Button
    private lateinit var  tgbtn: ImageButton
    private lateinit var  vkbtn: ImageButton
    private lateinit var  dsbtn: ImageButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_info, container, false)

        version = v.findViewById(R.id.version)
        val ver = resources.getString(R.string.ver)

        version.text = "${ver} ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"

        ghbtn = v.findViewById(R.id.github)
        sitebtn = v.findViewById(R.id.site)
        ratebtn = v.findViewById(R.id.rateweather)
        tgbtn = v.findViewById(R.id.tgbtn)
        vkbtn = v.findViewById(R.id.vkbtn)
        dsbtn = v.findViewById(R.id.dsbtn)

        ghbtn.setOnClickListener() {val i = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/jjewuz/JustWeather"))
            startActivity(i)}
        sitebtn.setOnClickListener() {val i = Intent(Intent.ACTION_VIEW, Uri.parse("https://jjewuz.github.io/"))
            startActivity(i)}
        ratebtn.setOnClickListener() {val i = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.jjewuz.justweather"))
            startActivity(i)}
        tgbtn.setOnClickListener() {val i = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/jjewuzhub"))
            startActivity(i)}
        vkbtn.setOnClickListener() {val i = Intent(Intent.ACTION_VIEW, Uri.parse("https://vk.com/jjewuzhub"))
            startActivity(i)}
        dsbtn.setOnClickListener() {val i = Intent(Intent.ACTION_VIEW, Uri.parse("https://discord.gg/jNHNBdYuAR"))
            startActivity(i)}


        return v
    }

}