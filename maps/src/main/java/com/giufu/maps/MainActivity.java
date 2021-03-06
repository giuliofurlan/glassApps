package com.giufu.maps;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements GlassGestureDetector.OnGestureListener {
    private static final int REQUEST_CODE = 999;
    private GlassGestureDetector glassGestureDetector;

    List<Address> listOfAddress;
    Geocoder geocoder;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);
        glassGestureDetector = new GlassGestureDetector(this, this);

        geocoder = new Geocoder(this, Locale.ITALY);
        test();
    }

    void test(){
        String speechResult = "marina julia";
        try {
            listOfAddress = geocoder.getFromLocationName(speechResult,5);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Intent intent = new Intent(this,MapActivity.class);
        intent.putExtra("lat",String.valueOf(listOfAddress.get(0).getLatitude()));
        intent.putExtra("lng",String.valueOf(listOfAddress.get(0).getLongitude()));
        startActivity(intent);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    private void detectSpeech() {
        final Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            final List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            Log.d("app", "results: " + results.toString());
            if (results != null && results.size() > 0 && !results.get(0).isEmpty()) {
                String speechResult = results.get(0);
                try {
                    listOfAddress = geocoder.getFromLocationName(speechResult,5);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Intent intent = new Intent(this,MapActivity.class);
                intent.putExtra("lat",String.valueOf(listOfAddress.get(0).getLatitude()));
                intent.putExtra("lng",String.valueOf(listOfAddress.get(0).getLongitude()));
                startActivity(intent);
                Log.d("app", speechResult);
            }
        } else {
            Log.d("app", "Result not OK");
        }
    }


    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return glassGestureDetector.onTouchEvent(ev) || super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent ev) {
        return glassGestureDetector.onTouchEvent(ev) || super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onGesture(GlassGestureDetector.Gesture gesture) {
        switch (gesture) {
            case TAP:
                detectSpeech();
                return true;
            default:
                return false;
        }
    }
}