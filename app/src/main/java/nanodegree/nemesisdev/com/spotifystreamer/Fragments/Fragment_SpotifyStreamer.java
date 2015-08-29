package nanodegree.nemesisdev.com.spotifystreamer.Fragments;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import nanodegree.nemesisdev.com.spotifystreamer.Objects.ParcelableTrack;
import nanodegree.nemesisdev.com.spotifystreamer.R;
import nanodegree.nemesisdev.com.spotifystreamer.Services.SpotifyStreamerService;


/**
 * A placeholder fragment containing a simple view.
 */
public class Fragment_SpotifyStreamer extends android.support.v4.app.DialogFragment implements ServiceConnection{

    private static final String TAG = Fragment_SpotifyStreamer.class.getSimpleName();

    private ImageButton mButtonPrevious;
    private ImageButton mButtonNext;
    private ImageButton mButtonPlayPause;
    private boolean mIsPlaying = true;

    private TextView mArtistName;
    private TextView mAlbumTitle;
    private TextView mTrackTitle;
    private ImageView mAlbumCover;

    private TextView mCurrentTrackTime;
    private double mTrackLengthSec = 30;
    private SeekBar mTrackSeek;

    private ArrayList<ParcelableTrack> mLoTracks;
    private int mCurrentTrackPos;
    private ParcelableTrack mCurrentTrack;

    private SpotifyStreamerService mSpotifyStreamerService;
    LocalBroadcastManager mLocalBroadcastManager;




    public Fragment_SpotifyStreamer() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        if (savedInstanceState == null){
            Bundle arguments  = getArguments();
            if (arguments != null) {
                mCurrentTrackPos = arguments.getInt(getString(R.string.key_selected_track));
                mLoTracks = arguments.getParcelableArrayList(getString(R.string.key_parcelable_track_list));
                mCurrentTrack = mLoTracks.get(mCurrentTrackPos);
            }else{
                Toast.makeText(getActivity(), R.string.error_generic, Toast.LENGTH_SHORT).show();
            }
        }

        Log.v(TAG, "Binding service");
        Context ctx = getActivity().getApplicationContext();
        Intent bindIntent = new Intent(ctx, SpotifyStreamerService.class);
        ctx.startService(bindIntent);
        ctx.bindService(bindIntent, this, Context.BIND_AUTO_CREATE);

        mLocalBroadcastManager = LocalBroadcastManager.getInstance(ctx);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_spotify_streamer, container, false);

        initUIComponents(rootView);
        attachListeners();

        if (savedInstanceState != null){
            mCurrentTrack = mSpotifyStreamerService.getCurrentTrack();
            updateDialogUI(mCurrentTrack);
        }

        return rootView;

    }


    @Override
    public void onResume() {
        super.onResume();
        mLocalBroadcastManager.registerReceiver(receiver, new IntentFilter(getString(R.string.BROADCAST_STATUS)));
    }

    @Override
    public void onPause() {
        mLocalBroadcastManager.unregisterReceiver(receiver);
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance())
            getDialog().setDismissMessage(null);


        super.onDestroyView();
    }



    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }


    private void initUIComponents(View rootView) {

        mButtonPrevious = (ImageButton) rootView.findViewById(R.id.button_previous);
        mButtonNext = (ImageButton) rootView.findViewById(R.id.button_next);
        mButtonPlayPause = (ImageButton) rootView.findViewById(R.id.button_play_pause);

        mCurrentTrackTime = (TextView) rootView.findViewById(R.id.current_track_time);
        mTrackSeek = (SeekBar) rootView.findViewById(R.id.streamer_seek_bar);
        mTrackSeek.setMax((int) mTrackLengthSec * 1000);

        mArtistName = (TextView) rootView.findViewById(R.id.streamer_artist_name);
        mAlbumTitle = (TextView) rootView.findViewById(R.id.streamer_album_title);
        mTrackTitle = (TextView) rootView.findViewById(R.id.streamer_song_title);
        mAlbumCover = (ImageView) rootView.findViewById(R.id.streamer_album_cover);

    }

    private void attachListeners(){
        mButtonPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsPlaying){
                    pauseTrack();
                }else{
                    playTrack();
                }
            }
        });

        mButtonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextTrack();
            }
        });

        mButtonPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prevTrack();
            }
        });

        mTrackSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //mPlayer.pause();
                if (fromUser) {
                   skipToTime(progress);
                }

                //mPlayer.start();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }


    private void playTrack() {
        Log.v(TAG, "Play track clicked in fragment");
        mSpotifyStreamerService.playTrack();
    }

    private void pauseTrack(){
        Log.v(TAG, "Pause track clicked in fragment");
        mSpotifyStreamerService.pauseTrack();
    }

    private void nextTrack(){
        Log.v(TAG, "Next track clicked in fragment");
        mSpotifyStreamerService.nextTrack();
    }

    private void prevTrack(){
        Log.v(TAG, "Prev track clicked in fragment");
        mSpotifyStreamerService.prevTrack();
    }

    private void skipToTime(int seekTime){
        Log.v(TAG, "Seek bar clicked in fragment");
        mSpotifyStreamerService.skipTo(seekTime);

    }


    //Based on stack overflow snippet from suggestion, determines whether the device has an internet connection
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    private void updateDialogUI(ParcelableTrack track){
        mArtistName.setText(track.getArtistName());
        mAlbumTitle.setText(track.getAlbumName());
        mTrackTitle.setText(track.getSongTitle());

        Picasso.with(getActivity()).load(track.getAlbumImageUrl())
                .error(R.drawable.spotify_icon_no_image_found)
                .placeholder(R.drawable.spotify_icon_no_image_found)
                .into(mAlbumCover);
    }

    private void updatePlayPauseButton(Boolean isPlaying){
        if (isPlaying){
            mButtonPlayPause.setImageResource(android.R.drawable.ic_media_pause);

        }else{
            mButtonPlayPause.setImageResource(android.R.drawable.ic_media_play);

        }
    }

    private void updateSeekBar(int timeElapsed){
        //set seekbar progress
        mTrackSeek.setProgress(timeElapsed);

        //set "seekbar" text
        mCurrentTrackTime.setText(String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes((long) timeElapsed),
                TimeUnit.MILLISECONDS.toSeconds((long) timeElapsed) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) timeElapsed))));

    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(getString(R.string.BROADCAST_STATUS))) {
                Boolean isNewTrack = intent.getBooleanExtra(getString(R.string.broadcast_new_track), false);
                boolean isPlaying = intent.getBooleanExtra(getString(R.string.broadcast_is_playing), false);
                if (isNewTrack) {
                    //If broadcast says a new track is playing
                    ParcelableTrack newTrack = intent.getParcelableExtra(getString(R.string.broadcast_track));
                    updateDialogUI(newTrack);
                    updateSeekBar(0);
                } else if (mIsPlaying != isPlaying) {
                    //If broadcast says the playing status has changed
                    mIsPlaying = isPlaying;
                    updatePlayPauseButton(mIsPlaying);
                } else {
                    //If no special condition, simply update the seekbar
                    int timeElapsed = intent.getIntExtra(getString(R.string.broadcast_time_elapsed), 0);
                    updateSeekBar(timeElapsed);
                }
            }
        }
    };

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        Log.v(TAG, "Service successfully connected");
        mSpotifyStreamerService = ((SpotifyStreamerService.LocalBinder) service).getService();
        mSpotifyStreamerService.setTracks(mLoTracks);
        mSpotifyStreamerService.setTrackPos(mCurrentTrackPos);
        mSpotifyStreamerService.declareNewTrack();
        mSpotifyStreamerService.playTrack();
        updateDialogUI(mSpotifyStreamerService.getCurrentTrack());
        updateSeekBar(0);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mSpotifyStreamerService = null;
    }
}
