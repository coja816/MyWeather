package com.example.myweather.Util

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object ApiUtil {
    private var retrofit: Retrofit? = null
    var url = "https://api.openweathermap.org/data/2.5/"
    fun getApiInterface(): WeatherAPI? {
        if (retrofit == null) {
            val logging = HttpLoggingInterceptor()
            logging.level = HttpLoggingInterceptor.Level.BODY
            val httpClient = OkHttpClient.Builder()
            httpClient.addInterceptor(logging) // <-- this is the important line!

            retrofit = Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build()
        }
        return retrofit!!.create(WeatherAPI::class.java)
    }
}
