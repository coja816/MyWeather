package com.example.myweather.Pojo

import com.google.gson.annotations.SerializedName

data class Weather(
    @SerializedName("id") val id: Int,
    @SerializedName("main") val main: Main,
    @SerializedName("wind") val wind: Wind,
    @SerializedName("icon") val icon: String

)
