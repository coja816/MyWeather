package com.example.myweather.Util

import com.example.myweather.Pojo.Main
import com.example.myweather.Pojo.Weather
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherAPI {
    @GET("weather")
    fun getWeather(
        @Query("lat") latitude: String,
        @Query("lon") longitude: String,
        @Query("APPID") api_key: String
    ): Call<Weather>

    @GET("weather")
    fun getCurrentWeatherData(
        @Query("q") cityName: String,
        @Query("APPID") api_key: String,
        API_KEY: String
    ): Call<Main>
}