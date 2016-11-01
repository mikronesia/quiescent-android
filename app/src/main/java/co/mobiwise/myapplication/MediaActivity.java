package co.mobiwise.myapplication;
import android.app.Activity;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Handler;


import co.mobiwise.library.media.MediaListener;
import co.mobiwise.library.media.MediaManager;

/**
 * Created by mertsimsek on 04/11/15.
 */
public class MediaActivity extends Activity implements MediaListener {

    /*String url = "https://api.soundcloud.com/tracks/230497727/stream?client_id=06a2d17b03d3ff6ae226b007edd5595d";
    String url2 = "https://api.soundcloud.com/tracks/227713501/stream?client_id=06a2d17b03d3ff6ae226b007edd5595d";
  */
  /*String url = "http://earsnake.com/quiescent/bud.mp3";
  String url2 = "http://earsnake.com/quiescent/light.mp3";*/
    SeekBar seekbar;
    Button button;
    TextView textView;
    TextView textTrack;
    String url;
    String track;
    String[] array;
    MediaManager mediaManager = MediaManager.with(this);
    String playingStatus;
    MediaPlayer mPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media);

        seekbar = (SeekBar) findViewById(R.id.seekbar);
        seekbar.setEnabled(false);
        button = (Button) findViewById(R.id.butPlay);
        textView = (TextView) findViewById(R.id.textstatus);
        textTrack = (TextView) findViewById(R.id.textTrack);
        array = getResources().getStringArray(R.array.songs_array);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            if (mPlayer.isPlaying()) {
                mPlayer.pause();
                playingStatus = "paused";
                button.setBackgroundResource(R.drawable.btn_playback_play);
            } else {
                playingStatus = "playing";
                mPlayer.start();
                button.setBackgroundResource(R.drawable.btn_playback_pause);
            }
            }
        });

        /*mediaManager.registerListener(this);*/
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mPlayer.seekTo(seekBar.getProgress());
            }
        });

        new CountDownTimer(1200000, 1000) {
            public void onTick(long millisUntilFinished) {
                textView.setText("Minutes remaining: " + (millisUntilFinished / 1000)/60);
            }

            public void onFinish() {
                textView.setText("done!");
                button.setBackgroundResource(R.drawable.btn_playback_play);
                mediaManager.disconnect();
            }
        }.start();

        playNewTrack();

        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                /*if (mp.isPlaying()) {
                    //playNewTrack();*/
                    textView.setText("FINISHED");
                /*}*/
            }
        });
    }

    public void playNewTrack() {
        track = array[new Random().nextInt(array.length)];
        url = "http://earsnake.com/quiescent/" + track + ".m4a";
        mPlayer = new MediaPlayer();
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mPlayer.setDataSource(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            mPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mPlayer.start();
        textTrack.setText(track.toUpperCase());
        button.setBackgroundResource(R.drawable.btn_playback_pause);
        seekbar.setEnabled(true);
        playingStatus = "paused";
    }

    @Override
    protected void onResume() {
        super.onResume();
        mediaManager.connect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaManager.disconnect();
    }

    @Override
    public void onMediaLoading() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
            seekbar.setEnabled(false);
            textView.setText("LOADING");
            }
        });
    }

    @Override
    public void onMediaStarted(final int totalDuration, int currentDuration) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
            seekbar.setEnabled(true);
            seekbar.setMax(totalDuration / 1000);
            textView.setText("STARTED");
            }
        });
    }

    @Override
    public void onMediaStopped() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.setText("STOPPED");
                playNewTrack();
            }
        });
    }



}
