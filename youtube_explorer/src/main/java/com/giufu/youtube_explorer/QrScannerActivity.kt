package com.giufu.youtube_explorer

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import com.budiyev.android.codescanner.*
import java.util.regex.Matcher
import java.util.regex.Pattern

class QrScannerActivity: Activity() {
    private lateinit var codeScanner: CodeScanner
    private val mOnClickListener: DialogInterface.OnClickListener = object :
        DialogInterface.OnClickListener {
        override fun onClick(p0: DialogInterface?, p1: Int) {

        }
    }

    fun restartActivity(){
        val intent = Intent(this, QrScannerActivity::class.java)
        startActivity(intent)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.qr_scanner_activity)
        val scannerView = findViewById(R.id.scanner_view)

        codeScanner = CodeScanner(this, scannerView as CodeScannerView)

        // Parameters (default values)
        codeScanner.camera = CodeScanner.CAMERA_BACK // or CAMERA_FRONT or specific camera id
        codeScanner.formats = CodeScanner.TWO_DIMENSIONAL_FORMATS // list of type BarcodeFormat,
        // ex. listOf(BarcodeFormat.QR_CODE)
        codeScanner.autoFocusMode = AutoFocusMode.CONTINUOUS // or CONTINUOUS
        codeScanner.scanMode = ScanMode.SINGLE // or CONTINUOUS or PREVIEW
        codeScanner.isAutoFocusEnabled = true // Whether to enable auto focus or not
        codeScanner.isFlashEnabled = false // Whether to enable flash or not

        // Callbacks
        codeScanner.decodeCallback = DecodeCallback {
            runOnUiThread {
                //works but very slow and unreliable
                if (isYoutubeUrl(it.text)){
                    val id = getVideoIdFromYoutubeUrl(it.text)
                    val intent = Intent(this, VideoActivity::class.java)
                    intent.putExtra("id", id)
                    startActivity(intent)
                }
                else {
                    AlertDialog(
                        this, R.drawable.ic_stop, R.string.alert_text,
                        R.string.alert_footnote_text,mOnClickListener).show()
                }

            }
        }
        codeScanner.errorCallback = ErrorCallback { // or ErrorCallback.SUPPRESS
            runOnUiThread {
                Toast.makeText(this, "Camera initialization error: ${it.message}",
                    Toast.LENGTH_LONG).show()
            }
        }
        scannerView.setOnClickListener {
            codeScanner.startPreview()
        }
    }

    override fun onResume() {
        super.onResume()
        codeScanner.startPreview()
    }

    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()
    }

    fun isYoutubeUrl(youTubeURl: String): Boolean {
        val success: Boolean
        val pattern = "^(http(s)?:\\/\\/)?((w){3}.)?youtu(be|.be)?(\\.com)?\\/.+".toRegex()
        success = !youTubeURl.isEmpty() && youTubeURl.matches(pattern)
        return success
    }

    fun getVideoIdFromYoutubeUrl(youtubeUrl: String?): String? {
        val pattern =
            "(?<=watch\\?v=|/videos/|embed\\/|youtu.be\\/|\\/v\\/|\\/e\\/|watch\\?v%3D|watch\\?feature=player_embedded&v=|%2Fvideos%2F|embed%\u200C\u200B2F|youtu.be%2F|%2Fv%2F)[^#\\&\\?\\n]*"
        val compiledPattern: Pattern = Pattern.compile(pattern)
        //url is youtube url for which you want to extract the id.
        val matcher: Matcher = compiledPattern.matcher(youtubeUrl)
        return if (matcher.find()) {
            matcher.group()
        } else null
    }
}