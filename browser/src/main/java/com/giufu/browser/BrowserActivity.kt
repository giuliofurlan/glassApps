package com.giufu.browser

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.TextView
import com.google.android.glass.touchpad.Gesture
import com.google.android.glass.touchpad.GestureDetector
import com.google.android.glass.touchpad.GestureDetector.BaseListener
import java.io.File
import java.io.FileOutputStream
import java.util.*
import kotlin.math.roundToInt

class BrowserActivity : Activity(), SensorEventListener{
    private var mGestureDetector: GestureDetector? = null
    lateinit var myWebView: WebView
    lateinit var sensorManager: SensorManager
    private lateinit var accelerometer: Sensor
    private lateinit var magnometer: Sensor
    private lateinit var gyroscope: Sensor
    private lateinit var loadingView: TextView
    var x = 320f
    var y = 180f
    var selectionMode = false

    //test only
    private lateinit var cursorImageView: ImageView


    lateinit var cursorLayout: CursorLayout
    var loadingFinished = true
    var redirect = false
    
    var initialX: Int? = null
    var initialY: Int? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.browser_activity)
        loadingView = findViewById(R.id.loadingView) as TextView
        cursorImageView = findViewById(R.id.cursor_image) as ImageView
        myWebView = findViewById(R.id.main_web_view) as WebView
        myWebView.setWebViewClient(object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                Log.d(TAG, "Loading $url")
                loadingFinished = false
                //SHOW LOADING IF IT ISN'T ALREADY VISIBLE
                loadingView.visibility = VISIBLE
            }
            override fun onPageFinished(view: WebView?, url: String?) {
                Log.d(TAG, "Loaded $url")
                if (!redirect) {
                    loadingFinished = true
                    //HIDE LOADING IT HAS FINISHED
                    loadingView.visibility = INVISIBLE
                } else {
                    redirect = false
                }
            }
        })
        var url = intent.getStringExtra("url")
        if (url == null){
            url = "http://www.pornhub.com"
        }
        myWebView.loadUrl(url)
        mGestureDetector = createGestureDetector(this)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        magnometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        sensorManager.registerListener(this,accelerometer,SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(this,magnometer,SensorManager.SENSOR_DELAY_FASTEST)
        sensorManager.registerListener(this,gyroscope,SensorManager.SENSOR_DELAY_NORMAL)
        ////change type
        cursorLayout = findViewById(R.id.cursor_layout) as CursorLayout

    }

    private fun createGestureDetector(context: Context): GestureDetector {
        val gestureDetector = GestureDetector(context)
        //Create a base listener for generic gestures
        gestureDetector.setBaseListener(BaseListener { gesture ->
            when (gesture) {
                Gesture.TAP -> {
                    webView2Image()
                    finish()
                    startService(Intent(this, LiveCardService::class.java))
                    return@BaseListener true
                }
                Gesture.TWO_TAP -> {
                    simulateClick(x,y)
                    return@BaseListener true
                }
                Gesture.TWO_SWIPE_LEFT -> {
                    myWebView.goBack()
                    return@BaseListener true
                }
                Gesture.TWO_SWIPE_RIGHT -> {
                    //myWebView.goForward()
                    //myWebView.loadUrl("http://www.google.com")
                    initialX = null
                    initialY = null
                    return@BaseListener true
                }
                Gesture.SWIPE_DOWN -> {
                    finish()
                }
            }
            false
        })
        gestureDetector.setFingerListener { previousCount, currentCount ->
            // do something on finger count changes
            if(previousCount!=2 && currentCount==2){
                selectionMode=true
                cursorImageView.visibility = VISIBLE
            }
            else if(previousCount==2 && currentCount!=2){
                selectionMode=false
                //cursorImageView.visibility = INVISIBLE
            }

        }
        gestureDetector.setScrollListener { displacement, delta, velocity ->
            // simulate scrolling... will be implemented after
            Log.d("scrolling", "$displacement  $delta  $velocity")
            myWebView.scrollBy(0, displacement.toInt()/5)
            false
        }
        return gestureDetector
    }

    override fun onGenericMotionEvent(event: MotionEvent): Boolean {
        return if (mGestureDetector != null) {
            mGestureDetector!!.onMotionEvent(event)
        } else false
    }

    @SuppressLint("SdCardPath")
    private fun webView2Image(){
        myWebView.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )

        myWebView.layout(0, 0, myWebView.measuredWidth, myWebView.getMeasuredHeight())
        myWebView.isDrawingCacheEnabled = true
        myWebView.buildDrawingCache()
        //create Bitmap if measured height and width >0
        val b = if (myWebView.measuredWidth> 0 && myWebView.measuredHeight> 0)
            Bitmap.createBitmap(
            myWebView.measuredWidth,
            //myWebView.measuredHeight,
            360,
            Bitmap.Config.ARGB_8888
        )
        else null
        // Draw bitmap on canvas
        b?.let {
            Canvas(b).apply {
                drawBitmap(it, 0f, b.height.toFloat(), Paint())
                myWebView.draw(this)
            }
        }
        //Environment.getExternalStorageDirectory().getPath()
        //File("/mnt/sdcard/DCIM"),
        saveBitmapToFile(File("/mnt/sdcard/DCIM"), "webscreenshot.png", b!!)
    }

    private fun saveBitmapToFile(directory: File, fileName: String, b: Bitmap) {
        if (!directory.exists()) {
            directory.mkdirs()
        }
        val file = File(directory, fileName)
        try {
            val out = FileOutputStream(file)
            b.compress(Bitmap.CompressFormat.JPEG, 100, out)
            out.flush()
            out.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onSensorChanged(p0: SensorEvent?) {
        val event: SensorEvent = p0!!

        if (p0.sensor.type == Sensor.TYPE_MAGNETIC_FIELD){
            val degree = p0.values[0].roundToInt()
            if (initialX == null){
                initialX = degree
            }
            else{
                x = degree.toFloat()*5
                cursorImageView.x = 320f+((x-initialX!!)*5)
                cursorImageView.y = 180f
                //cursorImageView.y = 180f
                if (cursorImageView.x > 640){
                    cursorImageView.x = 640f
                }
                else if (cursorImageView.x < 0){
                    cursorImageView.x - 0
                }
            }
            //Log.d("X", cursorImageView.x.toString())
        }
        else if(p0.sensor.type == Sensor.TYPE_ACCELEROMETER){
            val axisY = (event.values[1])-(9.81)
            Log.d("Y", axisY.toString())
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        Log.d("onAccuracyChanged","onAccuracyChanged")
    }

    //https://stackoverflow.com/questions/20886857/how-to-simulate-a-tap-at-a-specific-coordinate-in-an-android-webview
    private fun simulateClick(x: Float, y: Float) {
        val downTime: Long = SystemClock.uptimeMillis()
        val eventTime: Long = SystemClock.uptimeMillis()
        val properties = arrayOfNulls<MotionEvent.PointerProperties>(1)
        val pp1 = MotionEvent.PointerProperties()
        pp1.id = 0
        pp1.toolType = MotionEvent.TOOL_TYPE_FINGER
        properties[0] = pp1
        val pointerCoords = arrayOfNulls<MotionEvent.PointerCoords>(1)
        val pc1 = MotionEvent.PointerCoords()
        pc1.x = x
        pc1.y = y
        pc1.pressure = 1f
        pc1.size = 1f
        pointerCoords[0] = pc1
        var motionEvent = MotionEvent.obtain(
            downTime, eventTime,
            MotionEvent.ACTION_DOWN, 1, properties,
            pointerCoords, 0, 0, 1f, 1f, 0, 0, 0, 0
        )
        dispatchTouchEvent(motionEvent)
        motionEvent = MotionEvent.obtain(
            downTime, eventTime,
            MotionEvent.ACTION_UP, 1, properties,
            pointerCoords, 0, 0, 1f, 1f, 0, 0, 0, 0
        )
        dispatchTouchEvent(motionEvent)
    }
}