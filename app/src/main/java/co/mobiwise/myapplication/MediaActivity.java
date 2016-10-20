package co.mobiwise.myapplication;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import java.util.Random;

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
    Button buttonNext;
    TextView textView;
    TextView textTrack;
    String url;
    String track;
    String[] array;

    MediaManager mediaManager = MediaManager.with(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media);

        seekbar = (SeekBar) findViewById(R.id.seekbar);
        seekbar.setEnabled(false);
        button = (Button) findViewById(R.id.buttoncontrol);
        buttonNext = (Button) findViewById(R.id.buttonNext);
        textView = (TextView) findViewById(R.id.textstatus);
        textTrack = (TextView) findViewById(R.id.textTrack);
        array = getResources().getStringArray(R.array.songs_array);
        track = array[new Random().nextInt(array.length)];
        url = "http://earsnake.com/quiescent/" + track + ".mp3";

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaManager.isPlaying()) {
                    mediaManager.pause();
                } else {
                    mediaManager.play(url);
                    textTrack.setText(track);
                }
            }
        });

        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                track = array[new Random().nextInt(array.length)];
                url = "http://earsnake.com/quiescent/" + track + ".mp3";
                mediaManager.play(url);
                textTrack.setText(track);
            }
        });

        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaManager.seekTo(seekBar.getProgress());
            }
        });

        mediaManager.registerListener(this);
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
            }
        });
    }
}
