package com.giufu.weather

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.location.*
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.Menu
import android.view.MenuItem
import com.giufu.weather.LiveCardService.Companion.COORDINATES
import com.giufu.weather.LiveCardService.Companion.COORDINATES_CHANGE_ACTION
import com.giufu.weather.LiveCardService.Companion.UNITS
import com.giufu.weather.LiveCardService.Companion.UNITS_CHANGE_ACTION
import com.giufu.weather.LiveCardService.Companion.coordinates
import com.giufu.weather.LiveCardService.Companion.units

class LiveCardMenuActivity : Activity(), LocationListener {
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
            coordinates = getLocationFromAddress(spokenText)
            stopped = true
            Intent().also { intent ->
                intent.action = COORDINATES_CHANGE_ACTION
                intent.putExtra(COORDINATES, coordinates)
                sendBroadcast(intent)
            }
        }
        shouldFinishOnMenuClose = true
        finish()
    }

    fun getLocationFromAddress(strAddress: String?, context: Context = this): String {
        val coder = Geocoder(context)
        val address: List<Address>?
        try {
            address = coder.getFromLocationName(strAddress, 5)
            if (address != null) {
                val location = address[0]
                return "lat=${location.latitude}&lon=${location.longitude}"
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        shouldFinishOnMenuClose = true
        when (item.itemId) {
            R.id.metric -> {
                units = "metric"
                Intent().also { intent ->
                    intent.action = UNITS_CHANGE_ACTION
                    intent.putExtra(UNITS, units)
                    sendBroadcast(intent)
                }
                return true
            }
            R.id.imperial -> {
                units = "imperial"
                Intent().also { intent ->
                    intent.action = UNITS_CHANGE_ACTION
                    intent.putExtra(UNITS, units)
                    sendBroadcast(intent)
                }
                return true
            }
            R.id.current_location -> {
                stopped = false
                refreshLocation()
                return true
            }
            R.id.change_location -> {
                stopped = true
                displaySpeechRecognizer()
                return true
            }
            R.id.action_stop -> {
                stopService(Intent(this, LiveCardService::class.java))
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

    private lateinit var locationManager: LocationManager
    @SuppressLint("MissingPermission")
    private fun refreshLocation() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val criteria = Criteria()
        criteria.accuracy = Criteria.NO_REQUIREMENT
        criteria.isAltitudeRequired = true;
        val providers = locationManager.getProviders(criteria, true)
        for (provider in providers) {
            locationManager.requestLocationUpdates(provider, 0, 0f, this)
            try {
                onLocationChanged(locationManager.getLastKnownLocation(provider))//mmm just in case
            } catch (e: Exception){}
        }
    }
    override fun onLocationChanged(location: Location) {
        if (!stopped){
            coordinates = "lat=${location.latitude}&lon=${location.longitude}"
            Intent().also { intent ->
                intent.action = COORDINATES_CHANGE_ACTION
                intent.putExtra(COORDINATES, coordinates)
                sendBroadcast(intent)
            }
        }
    }

    override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {
        TODO("Not yet implemented")
    }
    override fun onProviderEnabled(p0: String?) {
        TODO("Not yet implemented")
    }
    override fun onProviderDisabled(p0: String?) {
        TODO("Not yet implemented")
    }
}
