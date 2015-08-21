package nanodegree.nemesisdev.com.spotifystreamer.Fragments;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import nanodegree.nemesisdev.com.spotifystreamer.R;
import nanodegree.nemesisdev.com.spotifystreamer.Helpers.SpotifyTrackRecyclerAdapter;


public class Fragment_Top10Tracks extends Fragment {
    private static final String TAG = Fragment_Top10Tracks.class.getSimpleName();

    public SpotifyTrackRecyclerAdapter mSpotifyTracktAdapter;
    private RecyclerView mTrackReyclerView;
    private String artistID;
    private String locale = "US";
    private ArrayList<Track> mLoTrack;
    public Fragment_Top10Tracks() {   }
    private boolean isFirstLaunch = true;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        //Initializes the recycle view adapter, if the list of tracks is not empty use that, otherwise initialize new empty list
        if (mLoTrack !=null){
            mSpotifyTracktAdapter = new SpotifyTrackRecyclerAdapter(mLoTrack, getActivity());
        }else{
            mSpotifyTracktAdapter = new SpotifyTrackRecyclerAdapter(new ArrayList<Track>(), getActivity());
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_top10_tracks, container, false);


        Bundle arguments  = getArguments();

        Intent intent = getActivity().getIntent();

        //Retrieve the artist ID that should be passed from the main search activity
        boolean hasExtra = intent.hasExtra(getString(R.string.key_artist_id_extra));
        if (arguments != null) {
            artistID = arguments.getString(getString(R.string.key_artist_id_extra));
        }else{
            //Error in the event that an artist id isn't passed, which means something went wrong
            Toast.makeText(getActivity(), getActivity().getString(R.string.error_did_not_receive_artist), Toast.LENGTH_SHORT).show();
        }

        //Get the users set local to pass a country code to the spotify API

        initUIComponents(rootView);

        //Only build the track list if this is the first launch of the fragment
        if (isFirstLaunch){
            //locale = getActivity().getResources().getConfiguration().locale.getCountry();
            buildTrackList(artistID);
        }

        //Once the track list has been build once, save state
        isFirstLaunch = false;

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


    }

    private void initUIComponents(View rootView) {
        mTrackReyclerView = (RecyclerView) rootView.findViewById(R.id.track_list);
        mTrackReyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mTrackReyclerView.setAdapter(mSpotifyTracktAdapter);
    }

    //Builds track list based on passed artist id
    private void buildTrackList(String artistID){
        
        try {
            //If network is availble, build track list based on spotify API
            if (isNetworkAvailable()) {
                Log.v(TAG, "BUILDING TRACK LIST");
                buildTrackListTask task = new buildTrackListTask();
                task.execute(artistID, locale);
            }else{
                //If there is no internet connection, do not attempt to connect to spotify API
                Toast.makeText(getActivity(), getActivity().getString(R.string.error_no_internet), Toast.LENGTH_SHORT).show();
            }

            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Based on stack overflow snippet from suggestion, determines whether there is an internet connection
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public class buildTrackListTask extends AsyncTask<String, Void, ArrayList<Track>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected ArrayList<Track> doInBackground(String... params) {
            ArrayList<Track> LoTracks = null;
            Tracks t = null;
            try {
                SpotifyApi api = new SpotifyApi();
                SpotifyService spotify = api.getService();

                String id = params[0];
                final String locale = params[1];

                //Log.v(TAG, "Current local is: " + locale);

                t = spotify.getArtistTopTrack(id, new HashMap<String, Object>(){{put("country", locale);}});
                LoTracks = (ArrayList<Track>) t.tracks;

            } catch (Exception e) {
                e.printStackTrace();
                return LoTracks;
            }
            return LoTracks;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(ArrayList<Track> LoTracks) {
            super.onPostExecute(LoTracks);
            mLoTrack = LoTracks;
            if (LoTracks == null || LoTracks.size() == 0){
                Toast.makeText(getActivity(), getActivity().getString(R.string.notification_failed_to_find_tracks), Toast.LENGTH_SHORT).show();
            }else{
                mSpotifyTracktAdapter.replaceTrackList(mLoTrack);
                mSpotifyTracktAdapter.notifyDataSetChanged();
            }
        }
    }



}
