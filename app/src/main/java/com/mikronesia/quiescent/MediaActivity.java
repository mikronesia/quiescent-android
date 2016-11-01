package com.mikronesia.quiescent;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import co.mobiwise.library.HttpHandler;
import co.mobiwise.library.media.MediaListener;
import co.mobiwise.library.media.MediaManager;
import co.mobiwise.myapplication.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by mertsimsek on 04/11/15.
 */
public class MediaActivity extends Activity implements MediaListener {

    SeekBar seekbar;
    Button button;
    TextView textView;
    TextView textTrack;
    String track;
    List<String> tracks = new ArrayList<String>();
    Boolean isPl;
    Integer mTrackLen;
    String url;
    private static String JSONurl = "http://earsnake.com/quiescent/tracks.json";
    MediaManager mediaManager = MediaManager.with(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        isPl = false;
        mTrackLen = 0;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media);

        seekbar = (SeekBar) findViewById(R.id.seekbar);
        seekbar.setEnabled(false);
        button = (Button) findViewById(R.id.buttoncontrol);
        textView = (TextView) findViewById(R.id.textstatus);
        textTrack = (TextView) findViewById(R.id.textTrack);

        //Load tracks
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPl) {
                    pauseTrack();
                }
                else {
                    resumeTrack();
                }
            }
        });

        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                /*textView.setText(Integer.toString(progress));*/
                if (isPl) {
                    if (progress == mTrackLen/1000) {
                        textView.setText("SONG IS OVER");
                        playNewTrack();
                    }
                }
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

        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                seekbar.setProgress(mediaManager.currentPos() / 1000);
            }
        }, 0, 1000);//put here time 1000 milliseconds=1 second
        new getTracks().execute();
        //playNewTrack();
    }

    public void playNewTrack() {
        Random randomGenerator;
        randomGenerator = new Random();
        Integer idx = randomGenerator.nextInt( tracks.size());
        track = tracks.get(idx);
        url = "http://earsnake.com/quiescent/"+track+".mp3";
        mediaManager.play(url);
        textTrack.setText(track);
        button.setBackgroundResource(R.drawable.btn_playback_pause);
        isPl = true;
    }

    public void pauseTrack() {
        mediaManager.pause();
        button.setBackgroundResource(R.drawable.btn_playback_play);
        isPl = false;
    }

    public void resumeTrack() {
        mediaManager.resume();
        button.setBackgroundResource(R.drawable.btn_playback_pause);
        isPl = true;
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
    public void onMediaStarted(final int totalDuration, final int currentDuration) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTrackLen = totalDuration;
                seekbar.setEnabled(true);
                seekbar.setMax(totalDuration / 1000);
                textView.setText("PLAYING");
                seekbar.setProgress(currentDuration / 1000);
            }
        });
    }

    @Override
    public void onMediaStopped() {
        runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    textView.setText("PAUSED");
                }
            });
    }

    private void getTracks() {

    }

    private class getTracks extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler sh = new HttpHandler();

            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall(JSONurl);

            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    // Getting JSON Array node
                    JSONArray JSONTracks = jsonObj.getJSONArray("tracks");
                    for (int i = 0; i < JSONTracks.length(); i++) {
                        JSONObject c = JSONTracks.getJSONObject(i);
                        String JSONTrack = c.getString("track");
                        tracks.add(JSONTrack);
                    }
                } catch (final JSONException e) {
                    Log.e("TAG", "Json parsing error: " + e.getMessage());
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
                playNewTrack();
        }
    }


}



