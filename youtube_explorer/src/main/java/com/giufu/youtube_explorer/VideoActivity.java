package com.giufu.youtube_explorer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;

import java.util.concurrent.TimeUnit;

public class VideoActivity extends YouTubeBaseActivity  {
    YouTubePlayer.OnInitializedListener mOnInitializedListener;
    private final String TAG = "VideoActivity";
    YouTubePlayerView youTubePlayerView;
    boolean isPaused = false;
    YouTubePlayer player;
    TextView currentTimeView = null;
    private GestureDetector mGestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_activity);

        Intent intent = getIntent();
        String video_id = intent.getStringExtra("id");
        currentTimeView = (TextView) findViewById(R.id.time_text_view);

        youTubePlayerView = (YouTubePlayerView) findViewById(R.id.player);
        youTubePlayerView.setEnabled(false);

        youTubePlayerView.initialize(YoutubeConfig.getApiKey(),
            new YouTubePlayer.OnInitializedListener() {
                @Override
                public void onInitializationSuccess(YouTubePlayer.Provider provider,
                                                    YouTubePlayer youTubePlayer, boolean b) {
                    Log.d(TAG, "onInitializationSuccess: success");
                    youTubePlayer.loadVideo(video_id);
                    player = youTubePlayer;
                    player.setPlayerStyle(YouTubePlayer.PlayerStyle.CHROMELESS);
                    player.setShowFullscreenButton(false);
                }
                @Override
                public void onInitializationFailure(YouTubePlayer.Provider provider,
                                                    YouTubeInitializationResult youTubeInitializationResult) {
                    Log.d(TAG, "onInitializationFailure: fail");
                }
            });
        mGestureDetector = createGestureDetector(this);

    }
    @SuppressLint("DefaultLocale")
    String millisToTime(int millis){
        int hours = (int) (TimeUnit.MILLISECONDS.toHours((long) millis) % 24);
        int minutes = (int) (TimeUnit.MILLISECONDS.toMinutes((long) millis) % 60);
        int seconds = (int) (TimeUnit.MILLISECONDS.toSeconds((long) millis) % 60);
        if (hours > 0){
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        }
        else if(minutes > 0){
            return String.format("%02d:%02d", minutes, seconds);
        }
        else if(seconds > 0){
            return String.format("00:%02d", seconds);
        }
        else {
            return "00:00";
        }
    }

    private GestureDetector createGestureDetector(Context context) {
        GestureDetector gestureDetector = new GestureDetector(context);
        //Create a base listener for generic gestures
        gestureDetector.setBaseListener(gesture -> {
            if (gesture == Gesture.TAP) {
                if (isPaused){
                    player.play();
                    currentTimeView.setVisibility(View.GONE);
                }
                else{
                    player.pause();
                    String currentTime = millisToTime(player.getCurrentTimeMillis());
                    String totalMillis = millisToTime(player.getDurationMillis());
                    currentTimeView.setText(currentTime+"/"+totalMillis);
                    currentTimeView.setVisibility(View.VISIBLE);
                }
                isPaused = !isPaused;
                return true;
            } else if (gesture == Gesture.TWO_TAP) {
                // do something on two finger tap
                return true;
            } else if (gesture == Gesture.SWIPE_RIGHT) {
                player.seekToMillis(player.getCurrentTimeMillis()+30000);
                return true;
            } else if (gesture == Gesture.SWIPE_LEFT) {
                if (player.getCurrentTimeMillis()>30000) {
                    player.seekToMillis(player.getCurrentTimeMillis() - 30000);
                }
                return true;
            }
            return false;
        });
        gestureDetector.setFingerListener((previousCount, currentCount) -> {
            // do something on finger count changes
        });
        gestureDetector.setScrollListener(new GestureDetector.ScrollListener() {
            @Override
            public boolean onScroll(float displacement, float delta, float velocity) {
                // do something on scrolling
                return false;
            }
        });
        return gestureDetector;
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        if (mGestureDetector != null) {
            return mGestureDetector.onMotionEvent(event);
        }
        return false;
    }

}
