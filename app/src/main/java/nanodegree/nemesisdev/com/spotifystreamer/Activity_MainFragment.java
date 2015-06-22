package nanodegree.nemesisdev.com.spotifystreamer;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
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
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.*;
import kaaes.spotify.webapi.android.models.Artist;


/**
 * A placeholder fragment containing a simple view.
 */
public class Activity_MainFragment extends Fragment {
    private static final String TAG = Activity_MainFragment.class.getSimpleName();

    public SpotifyArtistRecyclerAdapter mSpotifyArtistAdapter;

    private EditText searchBar;
    private ImageButton searchButton;
    private ImageButton clearSearch;
    private RecyclerView mArtistReyclerView;

    private ArrayList<Artist> mLoArtist;
    private String mRecentSearch;
    private boolean bRepopulateArtists = false;

    public Activity_MainFragment() {    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        loadLastSearch();

        if (mLoArtist !=null){
            mSpotifyArtistAdapter = new SpotifyArtistRecyclerAdapter(mLoArtist, getActivity());
        }else{
            mSpotifyArtistAdapter = new SpotifyArtistRecyclerAdapter(new ArrayList<Artist>(), getActivity());
        }

        if (!mRecentSearch.equalsIgnoreCase("")){
            bRepopulateArtists = true;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        initUIComponents(rootView);
        attachListeners();
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (bRepopulateArtists){
            searchForArtist();
            bRepopulateArtists = false;
        }
    }

    private void initUIComponents(View rootView){
        searchButton = (ImageButton) rootView.findViewById(R.id.search_bar_search_button);
        searchBar = (EditText) rootView.findViewById(R.id.search_bar_input_box);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mRecentSearch = prefs.getString(getActivity().getString(R.string.pref_key_last_searched), "");
        searchBar.setText(mRecentSearch);

        clearSearch = (ImageButton) rootView.findViewById(R.id.search_bar_clear_search);
        if (searchBar.getText().toString().trim().length() == 0){
            clearSearch.setVisibility(View.GONE);
        }else{
            clearSearch.setVisibility(View.VISIBLE);
        }

        mArtistReyclerView = (RecyclerView) rootView.findViewById(R.id.artist_list);
        mArtistReyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mArtistReyclerView.setAdapter(mSpotifyArtistAdapter);
    }

    public void attachListeners(){
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Log.v(TAG, "Search Button Clicked");
                try {
                    searchForArtist();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        clearSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchBar.setText("");
                saveLastSearch("");
                clearSearch.setVisibility(View.GONE);
                mSpotifyArtistAdapter.clearArtists();
                mSpotifyArtistAdapter.notifyDataSetChanged();
            }
        });

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchBar.getText().toString().trim().length() == 0) {
                    clearSearch.setVisibility(View.GONE);
                } else {
                    clearSearch.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        searchBar.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_DPAD_CENTER:
                        case KeyEvent.KEYCODE_ENTER:
                            //Log.v(TAG, "Search Button Clicked");
                            try {
                                searchForArtist();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            return true;
                        default:
                            break;
                    }
                }
                return false;
            }
        });
    }

    private void saveLastSearch(String searchString){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        prefs.edit().putString(getActivity().getString(R.string.pref_key_last_searched), searchString).apply();
    }

    private void loadLastSearch(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mRecentSearch = prefs.getString(getActivity().getString(R.string.pref_key_last_searched), "");
    }

    public void searchForArtist(){
        mRecentSearch = searchBar.getText().toString();
        mRecentSearch = mRecentSearch.trim();
        saveLastSearch(mRecentSearch);

        //Log.v(TAG, "Searching on string: " + mRecentSearch);
        if(mRecentSearch.length() == 0){
            Log.v(TAG, "Invalid Search String");
        }else{
            try {
                searchTask task = new searchTask();
                task.execute(mRecentSearch);
            } catch (Exception e) {

                //Log.v(TAG, "Invalid Search String");
                e.printStackTrace();
            }
        }
    }

    public class searchTask extends AsyncTask<String, Void, ArrayList<Artist>>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected ArrayList<Artist> doInBackground(String... params) {
            ArrayList<Artist> LoArtists = null;
            ArtistsPager ap = null;
            Pager p = null;
            try {
                SpotifyApi api = new SpotifyApi();
                SpotifyService spotify = api.getService();
                ap = spotify.searchArtists(params[0]);
                p = ap.artists;
                LoArtists = (ArrayList<Artist>) p.items;

            } catch (Exception e) {

                //Log.v(TAG, "Search Returned No Responses");
                e.printStackTrace();
                return LoArtists;

            }
            return LoArtists;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(ArrayList<Artist> LoArtists) {
            super.onPostExecute(LoArtists);
            if (LoArtists.size() == 0){
                Toast.makeText(getActivity(), getActivity().getString(R.string.notificaiton_failed_search), Toast.LENGTH_SHORT).show();
            }else{
                mLoArtist = LoArtists;
                mSpotifyArtistAdapter.replaceArtistList(mLoArtist);
                mSpotifyArtistAdapter.notifyDataSetChanged();
            }
        }
    }
}
