package com.giufu.weather

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.Service
import android.content.*
import android.os.IBinder
import android.preference.PreferenceManager
import android.widget.RemoteViews
import com.google.android.glass.timeline.LiveCard
import com.google.android.glass.timeline.LiveCard.PublishMode
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URL

class LiveCardService : Service() {
    private var liveCard: LiveCard? = null
    private lateinit var remoteViews: RemoteViews
    private lateinit var broadcastReceiver: BroadcastReceiver
    private var metronomeThread: Thread? = null
    private lateinit var apiKey: String
    private lateinit var settings: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private val client = OkHttpClient()

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (liveCard == null) {
            apiKey = getString(R.string.api_key)
            liveCard = LiveCard(this, LIVE_CARD_TAG)
            remoteViews = RemoteViews(packageName, R.layout.live_card)
            liveCard!!.setViews(remoteViews)
            remoteViews.setTextViewText(R.id.description, "Loading...")
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
            startStopMetronome()
        } else {
            liveCard!!.navigate()
        }
        return START_STICKY
    }

    private fun startStopMetronome() {
        metronomeThread = Thread {
            while (true) {
                getWeather()
                Thread.sleep(60000)//update every ..ms
            }
        }
        metronomeThread?.start()
    }

    private fun getWeather(){
        val  url = URL("http://api.openweathermap.org/data/2.5/weather" +
                "?$coordinates" +
                "&units=$units" +
                "&appid=$apiKey")
        val request = Request.Builder().url(url).get().build()
        val response = client.newCall(request).execute()
        val responseBody = response.body()!!.string()
        val jsonObj = JSONObject(responseBody)
        val main = jsonObj.getJSONObject("main")
        val weather = jsonObj.getJSONArray("weather").getJSONObject(0)
        val temp = main.getDouble("temp").toInt()
        val tempMin = main.getDouble("temp_min").toInt()
        val tempMax = main.getDouble("temp_max").toInt()
        val weatherDescription = weather.getString("description")
        val address = jsonObj.getString("name")
        remoteViews.setTextViewText(R.id.tvTempo, "$temp°")
        remoteViews.setTextViewText(R.id.t_min, "$tempMin°")
        remoteViews.setTextViewText(R.id.t_max, "$tempMax°")
        remoteViews.setTextViewText(R.id.location, address)
        remoteViews.setTextViewText(R.id.description, weatherDescription)
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

        const val UNITS_CHANGE_ACTION = "com.giufu.weather.LiveCardService.Companion.UNITS_CHANGE_ACTION"
        const val UNITS = "units"
        var units = "metric"

        const val COORDINATES_CHANGE_ACTION = "com.giufu.weather.LiveCardService.Companion..COORDINATES_CHANGE"
        const val COORDINATES = "coordinates"
        var coordinates = "lat=-33.865143" +
                "&lon=151.209900"
    }

    class MyBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            (context as LiveCardService).liveCard?.setViews(context.remoteViews)
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