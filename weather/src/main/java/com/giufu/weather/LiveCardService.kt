package com.giufu.weather

import com.giufu.weather.weatherConditions
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.Service
import android.content.*
import android.content.ContentValues.TAG
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.IBinder
import android.preference.PreferenceManager
import android.util.Log
import android.widget.RemoteViews
import com.google.android.glass.timeline.LiveCard
import com.google.android.glass.timeline.LiveCard.PublishMode
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URL
import java.util.*

class LiveCardService : Service() {
    private var liveCard: LiveCard? = null
    private lateinit var remoteViews: RemoteViews
    private lateinit var broadcastReceiver: BroadcastReceiver
    private var metronomeThread: Thread? = null
    private val apiKey: String = "06c921750b9a82d8f5d1294e1586276f"
    private lateinit var settings:SharedPreferences
    private lateinit var editor:SharedPreferences.Editor

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (liveCard == null) {
            liveCard = LiveCard(this, LIVE_CARD_TAG)
            remoteViews = RemoteViews(packageName, R.layout.live_card)
            liveCard!!.setViews(remoteViews)
            remoteViews.setTextViewText(R.id.tvTempo, "--°")
            remoteViews.setTextViewText(R.id.t_max, "--°")
            remoteViews.setTextViewText(R.id.t_min, "--°")
            remoteViews.setTextViewText(R.id.location, "-")
            val bm:Bitmap = BitmapFactory.decodeResource(resources, R.drawable.rainpercent)
            remoteViews.setImageViewBitmap(R.id.imageView2, bm)
            startStopMetronome()
            val menuIntent = Intent(this, LiveCardMenuActivity::class.java)
            liveCard!!.setAction(PendingIntent.getActivity(this, 0, menuIntent, 0))
            liveCard!!.publish(PublishMode.REVEAL)
            broadcastReceiver = MyBroadcastReceiver()
            //setup filter or communication with activity
            registerReceiver(broadcastReceiver, IntentFilter(UNITS_CHANGE_ACTION))
            registerReceiver(broadcastReceiver, IntentFilter(COORDINATES_CHANGE_ACTION))
            //get last location if present
            settings = PreferenceManager.getDefaultSharedPreferences(this)
            val lastLocation = settings.getString("last_coordinates", null)
            if(lastLocation != null){
                coordinates = settings.getString("last_coordinates", "0")!!
            }
        } else {
            liveCard!!.navigate()
        }
        return START_STICKY
    }

    private fun startStopMetronome() {
        metronomeThread = Thread {
            while (true) {
                getWeather()
                Log.d(TAG, "startStopMetronome: $units")
                Log.d(TAG, "startStopMetronome: $coordinates")
                Thread.sleep(60000)//update every ..ms
            }
        }
        metronomeThread?.start()
    }

    private fun getWeather(){
        val client = OkHttpClient()
        val  url = URL("http://api.openweathermap.org/data/2.5/weather" +
                "?$coordinates" +
                "&units=$units" +
                "&appid=$apiKey")
        val request = Request.Builder()
            .url(url)
            .get()
            .build()
        val response = client.newCall(request).execute()
        val responseBody = response.body()!!.string()
        val jsonObj = JSONObject(responseBody)
        val main = jsonObj.getJSONObject("main")
        val sys = jsonObj.getJSONObject("sys")
        //var windSpeed = jsonObj.getJSONObject("wind").getString("speed").toInt()
        //if (units=="metric") windSpeed *= 3.6.toInt()
        val weather = jsonObj.getJSONArray("weather").getJSONObject(0)
        val temp = main.getString("temp").toFloat().toInt().toString()
        val tempMin = main.getString("temp_min").toFloat().toInt().toString()
        val tempMax = main.getString("temp_max").toFloat().toInt().toString()
        val weatherDescription = weather.getString("description")
        val address = jsonObj.getString("name")//+" "+sys.getString("country")
        val timezone = jsonObj.getString("timezone").toInt()
        val sunrise = sys.getString("sunrise").toInt()
        val sunset = sys.getString("sunset").toInt()
        populate(temp, tempMin, tempMax, weatherDescription, address, timezone, sunset, sunrise, getRainProbability())
    }

    private fun getRainProbability(): String {
        val client = OkHttpClient()
        val  url = URL("http://api.openweathermap.org/data/2.5/onecall?$coordinates&units=$units&appid=$apiKey")
        val request = Request.Builder()
            .url(url)
            .get()
            .build()
        val response = client.newCall(request).execute()
        val responseBody = response.body()!!.string()
        val jsonObj = JSONObject(responseBody)
        val daily = jsonObj.getJSONArray("daily")
        val hourly = jsonObj.getJSONArray("hourly")
        var pop = ""
         if (hourly.length() > 0){
            pop = hourly.getJSONObject(hourly.length()-1).getString("pop")
             return pop.toFloat().times(100).toInt().toString()
        }
        else if (daily.length() > 0 ){
            pop = daily.getJSONObject(daily.length()-1).getString("pop")
             return pop.toFloat().times(100).toInt().toString()
        }
        return "--"
    }

    private fun populate(t:String, t_min:String, t_max:String, weatherDescription:String,
                         address:String, timezone:Int, sunrise:Int, sunset:Int, pop:String){
        Log.d("IMPORTANT",weatherDescription)
        remoteViews.setTextViewText(R.id.tvTempo, "$t°")
        remoteViews.setTextViewText(R.id.t_min, "$t_min°")
        remoteViews.setTextViewText(R.id.t_max, "$t_max°")
        remoteViews.setTextViewText(R.id.location, address)
        remoteViews.setTextViewText(R.id.rain_percent, "$pop%")
        //https://gist.github.com/h0wardch3ng/03047ea601e47e1476176833fd95efa0
        var id: Int = R.drawable.ic_glass_logo
        val calendar: Calendar = Calendar.getInstance()
        val now: Int = (calendar.timeInMillis /1000).toInt()+timezone
        val day = now in (sunrise + 1) until sunset
        when (weatherDescription.lowercase()) {
            "few clouds" -> id = if (day) R.drawable.few_clouds_day else R.drawable.few_clouds_night
            "scattered clouds"-> id=R.drawable.scattered_clouds
            "broken clouds" -> id=R.drawable.broken_clouds
            "overcast clouds" -> id=R.drawable.broken_clouds
            in weatherConditions.Rain_light -> id = if (day) R.drawable.rain_day else R.drawable.rain_night
            in weatherConditions.Rain -> id = R.drawable.shower_rain
            in weatherConditions.Clear -> id = if (day) R.drawable.clear_sky_day else R.drawable.clear_sky_night
            in weatherConditions.Thunderstorm -> id=R.drawable.thunderstorm
            in weatherConditions.Drizzle-> id=R.drawable.shower_rain
            in weatherConditions.Snow -> id=R.drawable.snow
            in weatherConditions.Atmosphere -> id=R.drawable.mist
        }

        val bm:Bitmap = BitmapFactory.decodeResource(resources, id)
        remoteViews.setImageViewBitmap(R.id.imageView, bm)
        liveCard?.setViews(remoteViews)
    }

    override fun onDestroy() {
        if (liveCard != null && liveCard!!.isPublished) {
            liveCard!!.unpublish()
            liveCard = null
        }
        unregisterReceiver(broadcastReceiver)
        metronomeThread?.interrupt()
        super.onDestroy()
    }

    companion object {
        private const val LIVE_CARD_TAG = "LiveCardService"

        const val UNITS_CHANGE_ACTION = "com.giufu.weather.UNITS_CHANGE"
        const val UNITS = "units"
        var units = "metric"

        const val COORDINATES_CHANGE_ACTION = "com.giufu.weather.COORDINATES_CHANGE"
        const val COORDINATES = "coordinates"
        var coordinates = "lat=-33.865143" +
                "&lon=151.209900"
    }

    class MyBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            (context as LiveCardService).startStopMetronome()
            (context).saveCoordinates()
        }
    }

    fun saveCoordinates(){
        editor = settings.edit()
        editor.putString("last_coordinates", coordinates)
        editor.commit()
    }
}