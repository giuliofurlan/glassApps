package com.giufu.browser

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.SystemClock
import android.preference.PreferenceManager
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.google.android.glass.touchpad.Gesture
import com.google.android.glass.touchpad.GestureDetector
import com.google.android.glass.touchpad.GestureDetector.BaseListener
import java.io.File
import java.io.FileOutputStream
import kotlin.math.roundToInt

class BrowserActivity : Activity(){
    private var mGestureDetector: GestureDetector? = null
    lateinit var myWebView: WebView
    private lateinit var loadingView: TextView
    var selectionMode = false
    private lateinit var cursorImageView: ImageView
    private lateinit var settings: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    var lastX = 320f
    var lastY= 320f


    lateinit var cursorLayout: FrameLayout
    var loadingFinished = true
    var redirect = false


    override fun onPause() {
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.browser_activity)
        loadingView = findViewById(R.id.loadingView) as TextView
        cursorImageView = findViewById(R.id.cursor_image) as ImageView
        cursorImageView.x = 320f
        cursorImageView.y = 180f
        myWebView = findViewById(R.id.main_web_view) as WebView
        myWebView.isVerticalScrollBarEnabled = true
        myWebView.isHorizontalScrollBarEnabled = false
        myWebView.settings.domStorageEnabled = true

        settings = PreferenceManager.getDefaultSharedPreferences(this)

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
            url = settings.getString("lastUrl", null)
        }
        myWebView.loadUrl(url)
        mGestureDetector = createGestureDetector(this)

        ////change type
        cursorLayout = findViewById(R.id.cursor_layout) as FrameLayout

    }

    private fun createGestureDetector(context: Context): GestureDetector {
        val gestureDetector = GestureDetector(context)
        //Create a base listener for generic gestures
        gestureDetector.setBaseListener(BaseListener { gesture ->
            when (gesture) {

                Gesture.TAP -> {
                    simulateClick(cursorImageView.x,cursorImageView.y)
                    return@BaseListener true
                }
                Gesture.TWO_TAP -> {
                    selectionMode= !selectionMode
                    if (!selectionMode){
                        cursorImageView.visibility = INVISIBLE
                    }
                    else{
                        cursorImageView.visibility = VISIBLE
                    }
                    Log.d("THREE", "EUREKA!")
                }
                Gesture.TWO_LONG_PRESS -> {
                    if(loadingFinished){
                        editor = settings.edit()
                        editor.putString("lastUrl", myWebView.url)
                        editor.commit()
                        webView2Image()
                        //onPause()
                        finish()
                        startService(Intent(this, LiveCardService::class.java))
                    }
                    return@BaseListener true
                }
                Gesture.TWO_SWIPE_LEFT -> {
                    if (!selectionMode){
                        myWebView.goBack()
                    }
                    return@BaseListener true
                }
                Gesture.TWO_SWIPE_RIGHT -> {
                    if (!selectionMode){
                        myWebView.goForward()
                    }
                    return@BaseListener true
                }

                Gesture.SWIPE_DOWN -> {
                    finish()
                }
            }
            false
        })
        gestureDetector.setFingerListener { previousCount, currentCount ->
        }
        gestureDetector.setOneFingerScrollListener { displacement, delta, velocity ->
            // simulate scrolling... will be implemented after
            Log.d("scrolling", "$displacement  $delta  $velocity")
            //https://developers.google.com/glass/develop/gdk/reference/com/google/android/glass/touchpad/GestureDetector.ScrollListener
            if (selectionMode && (displacement>15 || displacement<-15)){
                cursorImageView.x = lastX+(delta/5)
                lastX = cursorImageView.x
                if (lastX<=0){
                    lastX = 640f
                }
                else if(lastX>=640){
                    lastX = 0f
                }
            }
            else{
                myWebView.scrollBy(0, delta.toInt()/3)
            }

            false
        }
        gestureDetector.setTwoFingerScrollListener{ displacement, delta, velocity ->
            Log.d("scrolling2", "$displacement  $delta  $velocity")
            if (selectionMode && (delta>15 || delta<-15)){
                cursorImageView.y = lastY+(delta/3)
                lastY = cursorImageView.y
                if (lastY<=0){
                    lastY = 360f
                }
                else if(lastY>=360){
                    lastY = 0f
                }
            }
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