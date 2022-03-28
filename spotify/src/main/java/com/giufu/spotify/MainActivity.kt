package com.giufu.spotify

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.StrictMode
import android.speech.RecognizerIntent
import android.view.Menu
import android.view.MenuItem


class MainActivity : Activity() {
    var isPaused: Boolean = true
    private val SPEECH_REQUEST = 0
    private var stopped: Boolean = false
    private var shouldFinishOnMenuClose = true
    private lateinit var spotifyAPI: SpotifyAPI

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
        refreshSong()
        spotifyAPI = SpotifyAPI(SpotifyAPI.oauth)
        menuInflater.inflate(R.menu.live_card, menu)
        return true
    }

    private fun displaySpeechRecognizer() {
        shouldFinishOnMenuClose = false
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        startActivityForResult(intent, SPEECH_REQUEST)
    }

    override fun onActivityResult(
        requestCode: Int, resultCode: Int,
        data: Intent) {
        if (requestCode == SPEECH_REQUEST && resultCode == RESULT_OK) {
            val results: List<String> = data.getStringArrayListExtra(
                RecognizerIntent.EXTRA_RESULTS)
            val spokenText = results[0]
            val intent = Intent(this, ResultsActivity::class.java)
            intent.putExtra("query", spokenText)
            startActivity(intent)
            stopped = true
        }
        shouldFinishOnMenuClose = true
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        sendBroadcast(intent)

        when (item.itemId) {
            R.id.play_pause -> {
                spotifyAPI.pausePlayBack()
                return true
            }
            R.id.next_track -> {
                spotifyAPI.skipToNext()
                return true
            }
            R.id.previous_track -> {
                spotifyAPI.skipToPrevious()
                return true
            }
            R.id.search -> {
                stopped = true
                displaySpeechRecognizer()
                return true
            }
            R.id.stop -> {
                stopService(Intent(this, LiveCardService::class.java))
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun refreshSong() {

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
        if (shouldFinishOnMenuClose) {
            finish()
        }
    }
}