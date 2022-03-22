package com.giufu.spotify

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.StrictMode
import android.view.Menu
import android.view.MenuItem

//https://developer.spotify.com/console/post-next/

class MainActivity : Activity() {
    private var oauth ="your key here"
    var isPaused: Boolean = true
    private lateinit var spotifyApi: SpotifyAPI

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
        spotifyApi = SpotifyAPI(oauth)
        menuInflater.inflate(R.menu.live_card, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.play_pause -> {
                spotifyApi.pausePlayBack()
                !isPaused
                if (isPaused){
                }
                else{

                }
                return true
            }
            R.id.next_track -> {
                spotifyApi.skipToNext()
                return true
            }
            R.id.previous_track -> {
                spotifyApi.skipToPrevious()
                return true
            }
            R.id.stop -> {
                stopService(Intent(this, LiveCardService::class.java))
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onOptionsMenuClosed(menu: Menu) {
        super.onOptionsMenuClosed(menu)
        finish()
    }
}