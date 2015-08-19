package nanodegree.nemesisdev.com.spotifystreamer;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
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


/**
 * A placeholder fragment containing a simple view.
 */
public class Fragment_SpotifyStreamer extends android.support.v4.app.DialogFragment {

    private static final String TAG = Fragment_SpotifyStreamer.class.getSimpleName();

    private ImageButton mButtonPrevious;
    private ImageButton mButtonNext;
    private ImageButton mButtonPlayPause;
    private boolean mIsPlaying = true;
    private MediaPlayer mPlayer;
    private TextView mArtistName;
    private TextView mAlbumTitle;
    private TextView mTrackTitle;
    private ImageView mAlbumCover;

    private TextView mCurrentTrackTime;
    private TextView mCurrentTrackLength;
    private int mDurationPostDelay = 1000;
    private double mTimeElapsedMillis, mTrackLengthSec = 30;
    private SeekBar mTrackSeek;
    private Handler mSeekDurationHandler = new Handler();

    private ArrayList<ParcelableTrack> mLoTracks;
    private int mCurrentTrackPos;
    private ParcelableTrack mCurrentTrack;



    private String previewURL;
    private String trackID;



    public Fragment_SpotifyStreamer() {


    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        if (savedInstanceState == null){
            Bundle arguments  = getArguments();
            if (arguments != null) {
                mCurrentTrackPos = arguments.getInt(getString(R.string.key_selected_track));
                mLoTracks = arguments.getParcelableArrayList(getString(R.string.key_parcelable_track_list));
                mCurrentTrack = mLoTracks.get(mCurrentTrackPos);
            }else{
                //Error in the event that an artist id isn't passed, which means something went wrong
                //TODO Fix Error Message
                Toast.makeText(getActivity(), getActivity().getString(R.string.error_did_not_receive_artist), Toast.LENGTH_SHORT).show();
            }
        }


        View rootView = inflater.inflate(R.layout.fragment_spotify_streamer, container, false);

        initUIComponents(rootView);
        attachListeners();

        if (savedInstanceState == null){
            managePlayback(0);
        }else{
            managePlayback(4);
        }

        return rootView;

    }


    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance())
            getDialog().setDismissMessage(null);
        super.onDestroyView();
    }

    @Override
    public void onPause() {
        if (mIsPlaying){
            mIsPlaying = false;
            mSeekDurationHandler.removeCallbacks(updateSeekBarTime);
            mPlayer.stop();
        }
        super.onPause();
    }


    private void initUIComponents(View rootView) {

        mButtonPrevious = (ImageButton) rootView.findViewById(R.id.button_previous);
        mButtonNext = (ImageButton) rootView.findViewById(R.id.button_next);
        mButtonPlayPause = (ImageButton) rootView.findViewById(R.id.button_play_pause);

        mCurrentTrackTime = (TextView) rootView.findViewById(R.id.current_track_time);
        mTrackSeek = (SeekBar) rootView.findViewById(R.id.streamer_seek_bar);
        mTrackSeek.setMax((int) mTrackLengthSec * 1000);

        if (mPlayer == null){
            mPlayer = new MediaPlayer();
            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        }else{

        }

        mArtistName = (TextView) rootView.findViewById(R.id.streamer_artist_name);
        mAlbumTitle = (TextView) rootView.findViewById(R.id.streamer_album_title);
        mTrackTitle = (TextView) rootView.findViewById(R.id.streamer_song_title);
        mAlbumCover = (ImageView) rootView.findViewById(R.id.streamer_album_cover);



    }

    public void attachListeners(){
        mButtonPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                managePlayback(1);
            }
        });

        mButtonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                managePlayback(2);
            }
        });

        mButtonPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                managePlayback(3);
            }
        });

    }

    //Type:
    //0 = Init
    //1 = PlayPause
    //2 = Track Next
    //3 = Track Prev
    //4 = Continue Playing Track
    public void managePlayback(int type){
        switch (type){
            case 0:
                mIsPlaying = true;
                rebuildUI();
                playTrack(1);

                break;
            case 1:
                if (mIsPlaying) {
                    stopTrack();
                    mSeekDurationHandler.removeCallbacks(updateSeekBarTime);
                    mIsPlaying = false;
                    rebuildUI();

                }else {
                    playTrack(0);
                    mIsPlaying = true;
                    rebuildUI();
                }
                break;
            case 2:
                this.mCurrentTrackPos += 1;
                if (this.mCurrentTrackPos >= this.mLoTracks.size()){
                    this.mCurrentTrackPos = 0;
                }
                mSeekDurationHandler.removeCallbacks(updateSeekBarTime);
                this.mCurrentTrack = this.mLoTracks.get(this.mCurrentTrackPos);
                rebuildUI();
                playTrack(1);

                break;
            case 3:
                this.mCurrentTrackPos -= 1;
                if (this.mCurrentTrackPos < 0){
                    this.mCurrentTrackPos = this.mLoTracks.size()-1;
                }
                mSeekDurationHandler.removeCallbacks(updateSeekBarTime);
                this.mCurrentTrack = this.mLoTracks.get(this.mCurrentTrackPos);
                rebuildUI();
                playTrack(1);
                break;

            case 4:
                mIsPlaying = true;

                try {
                    mPlayer.prepare();
                    mPlayer.seekTo((int) mTimeElapsedMillis);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mTrackSeek.setProgress((int) mTimeElapsedMillis);
                playTrack(0);
                rebuildUI();
                break;
        }


        mTrackSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //mPlayer.pause();
                if (fromUser){
                    mPlayer.seekTo(progress);
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

    private void playTrack(int type){

        //Type
        //0 = Play / Pause Same Track
        // 1 = Play New Track
        switch (type){
            case 0:
                try {
                    mPlayer.start();
                    mTimeElapsedMillis = mPlayer.getCurrentPosition();
                    mTrackSeek.setProgress((int) mTimeElapsedMillis);
                    mSeekDurationHandler.postDelayed(updateSeekBarTime, mDurationPostDelay);
                    mIsPlaying = true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            case 1:
                try {
                    mPlayer.reset();
                    mPlayer.setDataSource(mCurrentTrack.getTrackPreviewUrl());
                    mTimeElapsedMillis = mPlayer.getCurrentPosition();
                    mTrackSeek.setProgress((int) mTimeElapsedMillis);
                    mSeekDurationHandler.postDelayed(updateSeekBarTime, mDurationPostDelay);
                    Log.v(TAG, "....Preparing...");
                    mPlayer.prepare();
                    Log.v(TAG, "....Starting...");
                    mPlayer.start();
                    mIsPlaying = true;
                    rebuildUI();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
        }

    }

    private void stopTrack(){
        //If music is playing, stop it and change status
        Log.v(TAG, "Music was playing, stopping");
        try{
            Log.v(TAG, "....Stopping...");
            mPlayer.pause();
            mIsPlaying = false;
        }catch (Exception e){
            e.printStackTrace();
            Log.v(TAG, e.toString());
        }
    }


    private void rebuildUI(){
        mArtistName.setText(mCurrentTrack.getArtistName());
        mAlbumTitle.setText(mCurrentTrack.getAlbumName());
        mTrackTitle.setText(mCurrentTrack.getSongTitle());

        Picasso.with(getActivity()).load(mCurrentTrack.getAlbumImageUrl())
                .error(R.drawable.spotify_icon_no_image_found)
                .placeholder(R.drawable.spotify_icon_no_image_found)
                .into(mAlbumCover);

        if (mIsPlaying){
            mButtonPlayPause.setImageResource(android.R.drawable.ic_media_pause);

        }else{
            mButtonPlayPause.setImageResource(android.R.drawable.ic_media_play);

        }
    }


    //Based on stack overflow snippet from suggestion, determines whether the device has an internet connection
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    //handler to change seekBarTime
    private Runnable updateSeekBarTime = new Runnable() {
        public void run() {
            //get current position
            mTimeElapsedMillis = mPlayer.getCurrentPosition();

            //set seekbar progress
            mTrackSeek.setProgress((int) mTimeElapsedMillis);

            mCurrentTrackTime.setText(String.format("%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes((long) mTimeElapsedMillis),
                    TimeUnit.MILLISECONDS.toSeconds((long) mTimeElapsedMillis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) mTimeElapsedMillis))));

            //repeat yourself that again in 100 miliseconds
            mSeekDurationHandler.postDelayed(this, mDurationPostDelay);
        }
    };
}
