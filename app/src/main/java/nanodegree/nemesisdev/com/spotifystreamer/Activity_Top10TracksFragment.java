package nanodegree.nemesisdev.com.spotifystreamer;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;


/**
 * A placeholder fragment containing a simple view.
 */
public class Activity_Top10TracksFragment extends Fragment {
    private static final String TAG = Activity_Top10TracksFragment.class.getSimpleName();

    public SpotifyTrackRecyclerAdapter mSpotifyTracktAdapter;
    private RecyclerView mTrackReyclerView;
    private String artistID;
    private String locale;

    private ArrayList<Track> mLoTrack;

    public Activity_Top10TracksFragment() {   }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

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
        mSpotifyTracktAdapter = new SpotifyTrackRecyclerAdapter(new ArrayList<Track>(), getActivity());

        Intent intent = getActivity().getIntent();
        boolean hasExtra = intent.hasExtra(getString(R.string.key_artist_id_extra));
        if (intent != null && hasExtra) {
            artistID = intent.getStringExtra(getString(R.string.key_artist_id_extra));
        }

        locale = getActivity().getResources().getConfiguration().locale.getCountry();
        initUIComponents(rootView);
        return rootView;
    }

    private void initUIComponents(View rootView) {
        mTrackReyclerView = (RecyclerView) rootView.findViewById(R.id.track_list);
        mTrackReyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mTrackReyclerView.setAdapter(mSpotifyTracktAdapter);
    }


    @Override
    public void onStart() {
        super.onStart();

        buildTrackList(artistID);
    }


    private void buildTrackList(String artistID){
        try {
            buildTrackListTask task = new buildTrackListTask();
            task.execute(artistID, locale);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
