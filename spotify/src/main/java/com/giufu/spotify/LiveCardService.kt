package com.giufu.spotify

import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.IBinder
import android.util.Log
import android.widget.RemoteViews
import com.google.android.glass.timeline.LiveCard
import junit.framework.Assert.fail
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL


class LiveCardService: Service()  {
    private var liveCard: LiveCard? = null
    private lateinit var remoteViews: RemoteViews
    private lateinit var broadcastReceiver: BroadcastReceiver
    private var currentSongThread: Thread? = null
    var count: Int = 0;
    var spotifyApi: SpotifyAPI = SpotifyAPI("")

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (liveCard == null) {
            liveCard = LiveCard(this, LIVE_CARD_TAG)
            remoteViews = RemoteViews(packageName, R.layout.live_card)
            remoteViews.setTextViewText(R.id.textView, "$count")
            liveCard!!.setViews(remoteViews)
            startStopTheme()
            val menuIntent = Intent(this, MainActivity::class.java)
            liveCard!!.setAction(PendingIntent.getActivity(this, 0, menuIntent, 0))
            liveCard!!.publish(LiveCard.PublishMode.REVEAL)
            broadcastReceiver = MyBroadcastReceiver()
            registerReceiver(broadcastReceiver, IntentFilter(REFRESH_SONG))
        }
        return START_STICKY
    }

    private fun startStopTheme() {
        currentSongThread = Thread {
            while (true) {
                CURRENT_SONG = spotifyApi.getCurrentlyPlayingTrack()

                val item = CURRENT_SONG.getJSONObject("item")

                val song = item.getString("id")
                if (song != CURRENT_SONG_ID){
                    CURRENT_SONG_ID = song
                    val songName: String? = (item
                        .getString("name") ?: fail("Error")) as String?

                    val artistName: String? = (item
                        .getJSONArray("artists").getJSONObject(0)
                        .getString("name") ?: fail("Error")) as String?

                    val imageUrl: String? = (item
                        .getJSONObject("album")
                        .getJSONArray("images")
                        .getJSONObject(0)//0:600px 1:300px 2:64px
                        .getString("url") ?: fail("")) as String?

                    val duration = (item
                        .getString("duration_ms") ?: fail("Error")) as String?

                    remoteViews.setTextViewText(R.id.textView, "$songName")
                    remoteViews.setTextViewText(R.id.footer, "$artistName")
                    remoteViews.setImageViewBitmap(R.id.imageView, BitmapFromurl(imageUrl!!))

                }
                val progress: String? = (CURRENT_SONG
                    .getString("progress_ms") ?: fail("Error")) as String?
                if (progress != null) {
                    var seconds: Int  = progress.toInt()/1000
                    val minutes = seconds / 60
                    seconds %= 60
                    remoteViews.setTextViewText(R.id.timestamp, "$minutes:$seconds")
                }
                liveCard?.setViews(remoteViews)
                Thread.sleep(500)//update every ..ms
            }
        }
        currentSongThread?.start()
    }

    fun BitmapFromurl(imageUrl: String): Bitmap? {
        val url = URL(imageUrl)
        val image =
            BitmapFactory.decodeStream(url.openConnection().getInputStream())
        return image
    }

    fun drawableFromUrl(url: String?): Drawable? {
        val x: Bitmap
        val connection: HttpURLConnection = URL(url).openConnection() as HttpURLConnection
        connection.connect()
        val input: InputStream = connection.getInputStream()
        x = BitmapFactory.decodeStream(input)
        return BitmapDrawable(Resources.getSystem(), x)
    }

    fun drawableToBitmap(drawable: Drawable): Bitmap? {
        var bitmap: Bitmap? = null
        if (drawable is BitmapDrawable) {
            if (drawable.bitmap != null) {
                return drawable.bitmap
            }
        }
        bitmap = if (drawable.intrinsicWidth <= 0 || drawable.intrinsicHeight <= 0) {
            Bitmap.createBitmap(
                1,
                1,
                Bitmap.Config.ARGB_8888
            )
        } else {
            Bitmap.createBitmap(
                drawable.intrinsicWidth,
                drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
        }
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight())
        drawable.draw(canvas)
        return bitmap
    }


    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    companion object {
        private const val LIVE_CARD_TAG = "LiveCardService"
        const val REFRESH_SONG = "REFRESH_SONG"
        var CURRENT_SONG: JSONObject = JSONObject("{}")
        var CURRENT_SONG_ID: String = ""

    }

    class MyBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            //(context as LiveCardService)
            Log.d("broadcast", "ricevuto ${intent.toString()}")
        }
    }

    override fun onDestroy() {
        if (liveCard != null && liveCard!!.isPublished) {
            liveCard!!.unpublish()
            liveCard = null
        }
        unregisterReceiver(broadcastReceiver)
        super.onDestroy()
    }
}