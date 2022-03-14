package com.giufu.youtube_explorer;

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
    String millisToTime(int millis){
        return String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1));
    }

    private GestureDetector createGestureDetector(Context context) {
        GestureDetector gestureDetector = new GestureDetector(context);
        //Create a base listener for generic gestures
        gestureDetector.setBaseListener( new GestureDetector.BaseListener() {
            @Override
            public boolean onGesture(Gesture gesture) {
                if (gesture == Gesture.TAP) {
                    Log.d("App", "TAPPED!");
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
            }
        });
        gestureDetector.setFingerListener(new GestureDetector.FingerListener() {
            @Override
            public void onFingerCountChanged(int previousCount, int currentCount) {
                // do something on finger count changes
            }
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
