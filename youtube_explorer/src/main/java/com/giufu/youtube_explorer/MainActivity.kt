package com.giufu.youtube_explorer

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import android.view.*

class MainActivity : Activity() {
    private val SPEECH_REQUEST = 0
    private var stopped: Boolean = false
    private var shouldFinishOnMenuClose = true

    private fun displaySpeechRecognizer() {
        shouldFinishOnMenuClose = false;
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        startActivityForResult(intent, SPEECH_REQUEST)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        openOptionsMenu()
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_activity, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        shouldFinishOnMenuClose = true
        when (item.itemId) {
            R.id.voice_search -> {
                stopped = true
                displaySpeechRecognizer()
                return true
            }
            R.id.scan_qr -> {
                val intent = Intent(this, QrScannerActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.action_stop -> {
                //stopService(Intent(this, LiveCardService::class.java))
                finish()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onOptionsMenuClosed(menu: Menu) {
        super.onOptionsMenuClosed(menu)
        if (shouldFinishOnMenuClose) {
            finish();
        }
    }
}