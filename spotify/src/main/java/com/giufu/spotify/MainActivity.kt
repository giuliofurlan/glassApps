package com.giufu.spotify

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.StrictMode
import android.view.Menu
import android.view.MenuItem
import com.giufu.spotify.LiveCardService.Companion.CURRENT_SONG
import com.giufu.spotify.LiveCardService.Companion.REFRESH_SONG

//https://developer.spotify.com/console/post-next/

class MainActivity : Activity() {
    var isPaused: Boolean = true

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        openOptionsMenu()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (Build.VERSION.SDK_INT > 8) {
            val policy = StrictMode.ThreadPolicy.Builder()
                .permitAll().build()
            StrictMode.setThreadPolicy(policy)
        }
        //spotifyApi = SpotifyAPI(oauth)
        refreshSong()
        menuInflater.inflate(R.menu.live_card, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        intent.action = REFRESH_SONG
        intent.putExtra(REFRESH_SONG, "arrivato????")
        sendBroadcast(intent)

        when (item.itemId) {
            R.id.play_pause -> {
                //spotifyApi.pausePlayBack()
                return true
            }
            R.id.next_track -> {
                //spotifyApi.skipToNext()
                return true
            }
            R.id.previous_track -> {
                //spotifyApi.skipToPrevious()
                return true
            }
            R.id.stop -> {
                stopService(Intent(this, LiveCardService::class.java))
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    fun refreshSong() {

        //CURRENT_SONG = spotifyApi.getCurrentlyPlayingTrack()
        /*
        Intent().also { intent ->
            intent.action = REFRESH_SONG
            intent.putExtra(REFRESH_SONG, spotifyApi.getCurrentlyPlayingTrack().toString())
            sendBroadcast(intent)
        }
        */
    }

    override fun onOptionsMenuClosed(menu: Menu) {
        super.onOptionsMenuClosed(menu)
        finish()
    }
}