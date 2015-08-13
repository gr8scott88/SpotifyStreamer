package nanodegree.nemesisdev.com.spotifystreamer;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;


/**
 * A placeholder fragment containing a simple view.
 */
public class Fragment_SpotifyStreamer extends android.support.v4.app.DialogFragment {

    private static final String TAG = Fragment_SpotifyStreamer.class.getSimpleName();

    private ImageButton mButtonPrevious;
    private ImageButton mButtonNext;
    private ImageButton mButtonPlayPause;
    private boolean mIsPlaying = false;
    private MediaPlayer mPlayer;

    private String previewURL;
    private String trackID;


    public Fragment_SpotifyStreamer() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        Bundle arguments  = getArguments();

        //Retrieve the artist ID that should be passed from the main search activity

        if (arguments != null) {
            previewURL = arguments.getString(getString(R.string.key_preview_url));
            trackID = arguments.getString(getString(R.string.key_track_id_extra));
        }else{
            //Error in the event that an artist id isn't passed, which means something went wrong
            Toast.makeText(getActivity(), getActivity().getString(R.string.error_did_not_receive_artist), Toast.LENGTH_SHORT).show();
        }

        View rootView = inflater.inflate(R.layout.fragment_spotify_streamer, container, false);
        initUIComponents(rootView);
        attachListeners();
        managePlayback(previewURL);
        return rootView;
    }



    private void initUIComponents(View rootView) {
        mButtonPrevious = (ImageButton) rootView.findViewById(R.id.button_previous);
        mButtonNext = (ImageButton) rootView.findViewById(R.id.button_next);
        mButtonPlayPause = (ImageButton) rootView.findViewById(R.id.button_play_pause);
        mPlayer = new MediaPlayer();
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mIsPlaying = false;
    }

    public void attachListeners(){
        mButtonPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                managePlayback(previewURL);
            }
        });
    }




    public void managePlayback(String trackUrl){

        if (mIsPlaying) {
            //If music is playing, stop it and change status

            Log.v(TAG, "Music was playing, stopping");
            try{
                Log.v(TAG, "....Stopping...");
                mPlayer.stop();
            }catch (Exception e){
                e.printStackTrace();
                Log.v(TAG, e.toString());
            }

        }else {
            //If musics isn't playing, play it and change status

            Log.v(TAG, "Music was not playing, starting");

            try {
                mPlayer.setDataSource(trackUrl);
                Log.v(TAG, "....Preparing...");
                mPlayer.prepare();
                Log.v(TAG, "....Starting...");
                mPlayer.start();
                mIsPlaying = false;

            } catch (Exception e) {
                e.printStackTrace();
                Log.v(TAG, e.toString());
            }
            mIsPlaying = true;

        }

        try {
            SpotifyApi api = new SpotifyApi();
            SpotifyService spotify = api.getService();


        } catch (Exception e) {
            e.printStackTrace();

        }
    }


    @Override
    public void onPause() {
        if (mIsPlaying){
            mIsPlaying = false;
            mPlayer.stop();
        }
        super.onPause();
    }

    //Based on stack overflow snippet from suggestion, determines whether the device has an internet connection
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}
