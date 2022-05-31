package com.giufu.browser

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.Service
import android.content.*
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.IBinder
import android.preference.PreferenceManager
import android.util.Log
import android.view.WindowManager
import android.webkit.WebView
import android.widget.RemoteViews
import com.google.android.glass.timeline.LiveCard
import java.io.File
//import okhttp3.OkHttpClient


class LiveCardService : Service() {
    private var liveCard: LiveCard? = null
    private lateinit var remoteViews: RemoteViews
    private lateinit var broadcastReceiver: BroadcastReceiver
    private lateinit var apiKey: String
    private lateinit var settings: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    //private val client = OkHttpClient()
    private var webView: WebView? = null
    private var winManager: WindowManager? = null

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    @SuppressLint("UnspecifiedImmutableFlag", "RemoteViewLayout")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (liveCard == null) {
            liveCard = LiveCard(this, LIVE_CARD_TAG)
            remoteViews = RemoteViews(packageName, R.layout.live_card)
            liveCard!!.setViews(remoteViews)
            val menuIntent = Intent(this, LiveCardMenuActivity::class.java)
            liveCard!!.setAction(PendingIntent.getActivity(this, 0, menuIntent, 0))
            liveCard!!.publish(LiveCard.PublishMode.REVEAL)
            //broadcastReceiver = MyBroadcastReceiver()
            settings = PreferenceManager.getDefaultSharedPreferences(this)

            val imgFile = File("/mnt/sdcard/DCIM/webscreenshot.png")

            if(imgFile.exists()){
                val myBitmap: Bitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
                Log.d("test", imgFile.absolutePath)
                remoteViews.setImageViewBitmap(R.id.imageView,myBitmap)
            }
            else{
                val intent = Intent(this, BrowserActivity::class.java)
                startActivity(intent)
            }
            liveCard?.setViews(remoteViews)
        } else {
            liveCard!!.navigate()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        if (liveCard != null && liveCard!!.isPublished) {
            liveCard!!.unpublish()
            liveCard = null
        }
        //unregisterReceiver(broadcastReceiver)
        super.onDestroy()
    }

    companion object {
        private const val LIVE_CARD_TAG = "LiveCardService2"

    }
}