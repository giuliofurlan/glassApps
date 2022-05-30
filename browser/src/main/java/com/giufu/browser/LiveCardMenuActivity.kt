package com.giufu.browser

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.location.*
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.Menu
import android.view.MenuItem
import com.giufu.browser.R

class LiveCardMenuActivity : Activity(){
    private var stopped: Boolean = false
    private var shouldFinishOnMenuClose = true
    private val SPEECH_REQUEST = 0

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        openOptionsMenu()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.live_card, menu)
        return true
    }

    private fun displaySpeechRecognizer() {
        shouldFinishOnMenuClose = false;
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
        }
        shouldFinishOnMenuClose = true
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        shouldFinishOnMenuClose = true
        return when (item.itemId) {
            R.id.open_action -> {
                val intent = Intent(this, BrowserActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.action_stop -> {
                stopService(Intent(this, LiveCardService::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onOptionsMenuClosed(menu: Menu) {
        super.onOptionsMenuClosed(menu)
        if (shouldFinishOnMenuClose) {
            finish();
        }
    }
}
