package nanodegree.nemesisdev.com.spotifystreamer.Services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;

import nanodegree.nemesisdev.com.spotifystreamer.Objects.ParcelableTrack;
import nanodegree.nemesisdev.com.spotifystreamer.R;

/**
 * Created by Scott on 8/20/2015.
 */
public class SpotifyStreamerService extends Service {
    private final String TAG = SpotifyStreamerService.class.getSimpleName();

    static MediaPlayer mPlayer;
    private final IBinder mBinder = new LocalBinder();
    private Context mContext;
    private LocalBroadcastManager mBroadcastManager;
    private ArrayList<ParcelableTrack> mTracks;
    private int mTrackPos;
    private boolean mIsPlaying = false, mNewTrack = true;
    private ParcelableTrack mCurrentTrack;
    private static Handler mBroadcastHandler = new Handler();
    private int BROADCAST_DELAY_TIME = 100;

    public SpotifyStreamerService(){};

    public SpotifyStreamerService(int pos, ArrayList<ParcelableTrack> LoTracks) { this.mTrackPos = pos;  this.mTracks = LoTracks;}


    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        mBroadcastManager = LocalBroadcastManager.getInstance(mContext);
        mPlayer = new MediaPlayer();
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        attachListeners();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void setTracks(ArrayList<ParcelableTrack> tracks) {
        mTracks = tracks;
    }

    public void setTrackPos(int trackPos) {
        mTrackPos = trackPos;
        mCurrentTrack = mTracks.get(mTrackPos);
    }

    public boolean isPlaying() {
        return mIsPlaying;
    }

    private void attachListeners(){
        mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mPlayer.start();
                mIsPlaying = true;
                broadcastMessage();
                mNewTrack = false;
                mBroadcastHandler.postDelayed(broadcastStatus, BROADCAST_DELAY_TIME);
            }
        });

        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mBroadcastHandler.removeCallbacks(broadcastStatus);
                broadcastMessage();
            }
        });
    }

    public void declareNewTrack(){
        this.mNewTrack = true;
    }

    private void broadcastMessage(){
        //Log.v(TAG, "Broadcasting Message");
        int timeElapsed = mPlayer.getCurrentPosition();
        Intent intent = new Intent(getString(R.string.BROADCAST_STATUS));
        intent.putExtra(getString(R.string.broadcast_track), mTracks.get(mTrackPos));
        intent.putExtra(getString(R.string.broadcast_is_playing), mIsPlaying);
        intent.putExtra(getString(R.string.broadcast_time_elapsed), timeElapsed);
        intent.putExtra(getString(R.string.broadcast_new_track), mNewTrack);
        mBroadcastManager.sendBroadcast(intent);
    }

    public ParcelableTrack getCurrentTrack(){
        return mCurrentTrack;
    };

    public void loadAndPlay(){
        String previewUrl = mCurrentTrack.getTrackPreviewUrl();

        try {
            mPlayer.reset();
            mPlayer.setDataSource(previewUrl);
            mPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mIsPlaying = true;
        mBroadcastHandler.postDelayed(broadcastStatus, BROADCAST_DELAY_TIME);

    }

    public void continuePlay(){
        mPlayer.start();
        mIsPlaying = true;
    }

    public void playTrack(){
        Log.v(TAG, "Playing track in service");
        if (mNewTrack){
            mCurrentTrack = mTracks.get(mTrackPos);
            mBroadcastHandler.removeCallbacks(broadcastStatus);
            loadAndPlay();
        }else{
            if (mIsPlaying){
                mPlayer.pause();
                mBroadcastHandler.removeCallbacks(broadcastStatus);
                mCurrentTrack = mTracks.get(mTrackPos);
                loadAndPlay();
            }else{
                continuePlay();
            }
        }
    }

    public void pauseTrack(){
        Log.v(TAG, "Pausing track in service");
        mPlayer.pause();
        mIsPlaying = false;
    }

    public void nextTrack(){
        Log.v(TAG, "Playing next track in service");
        this.mTrackPos += 1;
        if (this.mTrackPos >= this.mTracks.size()){
            this.mTrackPos = 0;
        }
        mNewTrack = true;
        playTrack();
    }

    public void prevTrack(){
        Log.v(TAG, "Playing prev track in service");
        this.mTrackPos -= 1;
        if (this.mTrackPos < 0){
            this.mTrackPos = this.mTracks.size()-1;
        }
        mNewTrack = true;
        playTrack();
    }

    public void skipTo(int seekTime){
        Log.v(TAG, "Seeking to a time in service");
        mPlayer.seekTo(seekTime);

    }

    private Runnable broadcastStatus = new Runnable() {
        @Override
        public void run() {
            broadcastMessage();
            mBroadcastHandler.postDelayed(this, BROADCAST_DELAY_TIME);
        }
    };



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public  class LocalBinder extends Binder {
        public SpotifyStreamerService getService(){return  SpotifyStreamerService.this;}
    }

    public void clearCallbackQueue(){
        mBroadcastHandler.removeCallbacks(broadcastStatus);
    }





}
