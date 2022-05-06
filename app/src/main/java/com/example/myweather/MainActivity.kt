package com.example.myweather

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.myweather.Pojo.Weather
import com.example.myweather.Util.ApiUtil
import com.example.myweather.databinding.ActivityMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {
//testing github change
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var binding: ActivityMainBinding
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        getCurrentLocation()

        binding.editTextCityName.setOnEditorActionListener(object :
            TextView.OnEditorActionListener {
            override fun onEditorAction(p0: TextView?, p1: Int, p2: KeyEvent?): Boolean {
                if (p1 == EditorInfo.IME_ACTION_SEARCH) {
                    val location = getLocation(binding.editTextCityName.text.toString())
                    getCurrentLocationsWeather(
                        location?.latitude.toString(),
                        location?.longitude.toString()
                    )
                    return true;
                }
                return false;
            }

        })


    }

    fun getLocation(strAddress: String): Address? {
        val coder = Geocoder(this)
        val address: List<Address>?
        val p1: Address? = null
        try {
            address = coder.getFromLocationName(strAddress, 5)
            if (address == null) {
                return null
            }
            return address[0]
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
        return p1
    }

    private fun getCurrentLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {

                    requestPermission()
                    return
                }
                fusedLocationProviderClient.lastLocation.addOnSuccessListener {
                    if (it == null) {
                        Toast.makeText(this, "uh oh, location is null!", Toast.LENGTH_SHORT).show()
                        // lat and long in logcat
                        Log.i("STATUS", "Lat: ${latitude},Long:${longitude}")
                    } else {
                        getCurrentLocationsWeather(latitude.toString(), longitude.toString())
                    }
                }
            } else {
                Toast.makeText(applicationContext, "Turn on location", Toast.LENGTH_SHORT).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermission()
        }
    }

    private fun getCurrentLocationsWeather(latitude: String, longitude: String) {
        ApiUtil.getApiInterface()?.getWeather(latitude, longitude, API_KEY)
            ?.enqueue(object : Callback<Weather> {
                override fun onResponse(call: Call<Weather>, response: Response<Weather>) {
                    if (response.isSuccessful) {
                        setDataToTheViews(response.body())
                    }
                }

                override fun onFailure(call: Call<Weather>, t: Throwable) {
                    val builder = AlertDialog.Builder(this@MainActivity)
                    builder.setTitle("Attention:")
                    builder.setMessage("An error has occurred. Please contact the developer.")
                    builder.setPositiveButton(android.R.string.ok, null)
                    builder.show()

                }

            })
    }

    private fun setDataToTheViews(body: Weather?) {
        body?.let {
            val dateFormat = SimpleDateFormat("MM/dd/yyyy h:mm a",Locale.getDefault())
                    dateFormat.timeZone = TimeZone.getDefault()
            val currentDateTime = dateFormat.format(Date())
            binding.tvDateTime.text = "Today is: " + currentDateTime
            binding.tvPressure.text = String.format("%.2f", body.main.pressure * 0.014504)
            binding.tvHumidity.text = String.format("%.0f", body.main.humidity) + "%"
            binding.tvWindSpeed.text = "${body.wind.speed}"
            binding.tvSunrise.text = toFarenheit(body.main.temp_min) + "°F"
            binding.tvSunset.text = toFarenheit(body.main.temp_max) + "°F"
            binding.tvTemp.text = toFarenheit(body.main.feels_like) + "°F"
            binding.rlTempFahrenheit.text = toFarenheit(body.main.temp) + "°F"
            binding.tvWeatherType.text = "The temperature currently feels like..."
        }
    }

    private fun toFarenheit(tempMax: Double): String {
        return String.format("%.0f", 1.8 * (tempMax - 273) + 32)
    }


    companion object {
        private const val PERMISSION_REQUEST_ACCESS_LOCATION = 100
        const val API_KEY = "d09ed48683a22f30c293762c453159e3"
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ),
            PERMISSION_REQUEST_ACCESS_LOCATION
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_REQUEST_ACCESS_LOCATION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(applicationContext, "Granted", Toast.LENGTH_SHORT).show()
                getCurrentLocation()

            } else {
                Toast.makeText(applicationContext, "Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

}