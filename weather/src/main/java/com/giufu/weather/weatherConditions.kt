package com.giufu.weather
class weatherConditions {
    //https://openweathermap.org/weather-conditions
    companion object{
        val Thunderstorm = listOf<String>(
            "thunderstorm with light rain", "thunderstorm with rain", "thunderstorm with heavy rain",
            "light thunderstorm", "thunderstorm", "heavy thunderstorm", "ragged thunderstorm",
            "thunderstorm with light drizzle", "thunderstorm with drizzle", "thunderstorm with heavy drizzle"
        )

        val Drizzle = listOf<String>(
            "light intensity drizzle", "drizzle", "heavy intensity drizzle", "light intensity drizzle rain",
            "drizzle rain", "heavy intensity drizzle rain", "shower rain and drizzle",
            "heavy shower rain and drizzle", "shower drizzle"
        )
        val Rain_light = listOf<String>(
            "light rain", "moderate rain", "heavy intensity rain", "very heavy rain", "extreme rain"
        )
        val Rain = listOf<String>(
            "freezing rain", "light intensity shower rain", "shower rain", "heavy intensity shower rain",
            "ragged shower rain"
        )

        val Snow = listOf<String>(
            "light snow", "snow", "heavy snow", "sleet", "light shower sleet", "shower sleet",
            "light rain and snow", "rain and snow", "light shower snow", "shower snow", "heavy shower snow"
        )

        val Atmosphere = listOf<String>(
            "mist", "smoke", "haze", "sand/ dust whirls", "fog", "sand", "dust", "volcanic ash", "squalls",
            "tornado"
        )

        val Clear = listOf<String>("clear sky")

        val Clouds = listOf<String>("few clouds", "scattered clouds", "broken clouds",
            "overcast clouds")
    }
}