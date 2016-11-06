package com.mikronesia.slumbr;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.ShareActionProvider;
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

import android.view.Menu;

/**
 * Created by mertsimsek on 04/11/15.
 */
public class MediaActivity extends Activity implements MediaListener {

    SeekBar seekbar;
    Button butPlay;
    TextView textView;
    TextView textTrack;
    String track;
    List<String> tracks = new ArrayList<String>();
    Boolean isPl;
    Integer mTrackLen;
    String url;
    Intent mShareIntent;
    private ShareActionProvider mShareActionProvider;
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
        butPlay = (Button) findViewById(R.id.butPlay);
        textView = (TextView) findViewById(R.id.textstatus);
        textTrack = (TextView) findViewById(R.id.textTrack);
        super.onCreate(savedInstanceState);

        mShareIntent = new Intent();
        mShareIntent.setAction(Intent.ACTION_SEND);
        mShareIntent.setType("text/plain");
        mShareIntent.putExtra(Intent.EXTRA_TEXT, "From me to you, this text is new.");

        //Load tracks
        butPlay.setOnClickListener(new View.OnClickListener() {
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
        butPlay.setBackgroundResource(R.drawable.btn_playback_pause);
        isPl = true;
    }

    public void pauseTrack() {
        mediaManager.pause();
        butPlay.setBackgroundResource(R.drawable.btn_playback_play);
        isPl = false;
    }

    public void resumeTrack() {
        mediaManager.resume();
        butPlay.setBackgroundResource(R.drawable.btn_playback_pause);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        // Find the MenuItem that we know has the ShareActionProvider
        MenuItem item = menu.findItem(R.id.menu_item_share);

        // Get its ShareActionProvider
        mShareActionProvider = (ShareActionProvider) item.getActionProvider();

        // Connect the dots: give the ShareActionProvider its Share Intent
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(mShareIntent);
        }

        // Return true so Android will know we want to display the menu
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                // User chose the "Settings" item, show the app settings UI...
            {
                Intent myIntent = new Intent( MediaActivity.this, PreferencesActivity.class );
                MediaActivity.this.startActivity( myIntent );
            }
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }




}



