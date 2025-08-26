package com.example.weatherapplication

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.weatherapplication.databinding.ActivityMainBinding
import androidx.appcompat.widget.SearchView

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.locks.Condition

class MainActivity : AppCompatActivity() {
    private val binding : ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)
        // Yahan call karo
        fetchWeatherData("Kamalia")
        SearchCity()
    }
    private fun SearchCity() {
        val searchView = binding.searchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    fetchWeatherData(query)
                    searchView.clearFocus() // keyboard band karne ke liye
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })
    }



    // Function ko class ke andar rakho, onCreate ke andar nahi
    private fun fetchWeatherData(cityname : String) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/") // ✅ fixed
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiInterface::class.java)

        val response = retrofit.getWeatherData(
            cityname,
            "42fd1f4647f9ad93654ccfce65e29dc4",
            "metric"
        )

        response.enqueue(object : Callback<WeatherApp> {
            override fun onResponse(call: Call<WeatherApp>, response: Response<WeatherApp>) {
                val responseBody = response.body()
                if (response.isSuccessful && responseBody != null) {

                    val temperature = responseBody.main.temp.toString()
                   val humidity = responseBody.main.humidity
                    val sunRise = responseBody.sys.sunrise.toLong()
                    val sunSet =  responseBody.sys.sunset.toLong()
                    val pressure = responseBody.main.pressure
                    val windSpeed = responseBody.wind.speed
                    val seaLevel = responseBody.main.sea_level ?: pressure
                    val condition = responseBody.weather.firstOrNull()?.main?:"unknown"
                    val maxTemp = responseBody.main.temp_max
                    val minTemp = responseBody.main.temp_min
                    binding.temperatue.text = "$temperature °C"
                    binding.wind.text = condition
                    //binding.conditions.text = condition
                    binding.max.text = "Max  Temp :$maxTemp °C"
                    binding.min.text = "Max  Temp :$minTemp °C"
                    binding.humidity.text = "$humidity %"
                    binding.wind.text = "$windSpeed m/s"
                    binding.sunrise.text = "${time(sunRise)}"
                    binding.sunset.text = "${time(sunSet)}"
                    binding.wind.text = "$windSpeed m/s"
                    binding.Sea.text = "$windSpeed m/s"
                    //binding.wind.text = condition
                    binding.sunny.text = condition
                    binding.conditions.text = condition
                    //binding.pressure.text = "$seaLevel hPa"
                    binding.day.text =dayName(System.currentTimeMillis())
                    binding.date.text =date()
                    binding.location.text = "$cityname"
                    changeImagesAccordingToWeatherCondition(condition)

                } else {
                    Log.e(TAG, "Response failed: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<WeatherApp>, t: Throwable) {
                Log.e(TAG, "API Failed: ${t.message}")
            }
        })

    }

    private fun changeImagesAccordingToWeatherCondition (condition : String){
        when(condition){
            "Clear Sky ","Sunny","Clear" -> {
                binding.root.setBackgroundResource(R.drawable.sunny_background)
                binding.lottieAnimationView.setAnimation(R.raw.sun)

            }
            "Partly Clouds ","Clouds","Overcast","Mist","Foggy" -> {
                binding.root.setBackgroundResource(R.drawable.colud_background)
                binding.lottieAnimationView.setAnimation(R.raw.cloud)

            }
            "Light Rain ","Drizzle","Moderate Rain","Showers","Heavy Rain" -> {
                binding.root.setBackgroundResource(R.drawable.rain_background)
                binding.lottieAnimationView.setAnimation(R.raw.rain)

            }
            "Light Snow ","Moderate Snow","Heavy Snow","Blizzard" -> {
                binding.root.setBackgroundResource(R.drawable.snow_background)
                binding.lottieAnimationView.setAnimation(R.raw.snow)

            }
            else ->  {
                binding.root.setBackgroundResource(R.drawable.sunny_background)
                binding.lottieAnimationView.setAnimation(R.raw.sun)
            }
        }
        binding.lottieAnimationView.playAnimation()
    }
//    fun timeFormat(timestamp: Long): String {
//        val sdf = SimpleDateFormat("HH:mm a", Locale.getDefault())
//        return sdf.format(Date(timestamp * 1000)) // multiply by 1000 because API gives seconds
//    }

    fun dayName (timestamp: Long) : String
    {
        val sdf = SimpleDateFormat("EEEE", Locale.getDefault())
        return  sdf.format((Date()))
    }
     fun time(timestamp: Long): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp * 1000)) // multiply by 1000 because API gives seconds
    }

    fun date (): String {
        val sdf = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        return sdf.format(Date())
    }
}


