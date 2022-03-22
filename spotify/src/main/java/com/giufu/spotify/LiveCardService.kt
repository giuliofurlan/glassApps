package com.giufu.spotify

import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import android.widget.RemoteViews
import com.google.android.glass.timeline.LiveCard


class LiveCardService: Service()  {
    private var liveCard: LiveCard? = null
    private lateinit var remoteViews: RemoteViews
    private lateinit var broadcastReceiver: BroadcastReceiver
    private var currentSongThread: Thread? = null
    var count: Int = 0;
    var oauth = "BQAwdYKkDYL5C1jvteMmI1b98GFMKsDmDK-dWvS3CLGxBAnJgHlc2kPsNTmbBWXG7UpQDJ1k7mzJLzMT74f_KxIXhqkEDFEr-BfNA5DCRRRK3bnRMsrBohPMx6_5DtgxFwh6xjlUhRc7QWDQBvUYZA"

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (liveCard == null) {
            liveCard = LiveCard(this, LIVE_CARD_TAG)
            remoteViews = RemoteViews(packageName, R.layout.live_card)
            remoteViews.setTextViewText(R.id.textView, "$count")
            liveCard!!.setViews(remoteViews)
            startStopMetronome()
            val menuIntent = Intent(this, MainActivity::class.java)
            liveCard!!.setAction(PendingIntent.getActivity(this, 0, menuIntent, 0))
            liveCard!!.publish(LiveCard.PublishMode.REVEAL)
            broadcastReceiver = MyBroadcastReceiver()
        }
        return START_STICKY
    }

    private fun startStopMetronome() {
        currentSongThread = Thread {
            while (true) {
                remoteViews.setTextViewText(R.id.textView, "$count")
                Log.d("LIVE CARD", "$count")
                liveCard?.setViews(remoteViews)
                Thread.sleep(1000)//update every ..ms
            }
        }
        currentSongThread?.start()
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    companion object {
        private const val LIVE_CARD_TAG = "LiveCardService"



    }

    class MyBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            (context as LiveCardService)
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