package com.giufu.spotify

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.res.Resources
import android.util.Log
import android.view.Menu
import android.view.MenuItem
//https://developer.spotify.com/console/post-next/

class MainActivity : Activity() {
    var isPaused: Boolean = true

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        openOptionsMenu()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.live_card, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.play_pause -> {
                !isPaused
                if (isPaused){
                    //playPauseView.setIcon(android.R.drawable.ic_media_play
                }
                else{

                }
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