package nanodegree.nemesisdev.com.spotifystreamer;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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

        //Retreive last search string
        loadLastSearch();


        //Initialize the recyler view adapter, if theres an existing artist list use that, otherwise use a new empty list
        if (mLoArtist !=null){
            mSpotifyArtistAdapter = new SpotifyArtistRecyclerAdapter(mLoArtist, getActivity());
        }else{
            mSpotifyArtistAdapter = new SpotifyArtistRecyclerAdapter(new ArrayList<Artist>(), getActivity());
        }

        //If there is a search string saved, but the list of artists is null or size 0, then rebuild the list of artists with the search string
        if (!mRecentSearch.equalsIgnoreCase("") && (mLoArtist == null || mLoArtist.size() == 0)){
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
        //This will be called in the event that the artist array is null or empty and there is a search string
        //This could happen if the activity is closed at any point but the most recent search was retained
        //The alternative is a populated search box with no results, which I'd prefer not to have
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

        //Search for artists based on search string on click
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

        //Clear the edit text and the search results on click
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


        //This text changed listener exists solely to show & hide the clear button based on whether there is text in the search bar
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

        //Map the enter key to search, so the user doesnt have to interact directly with the ui buttons if they don't want to
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

    //Save the most recent search to a shared preference to enable rebuilding app state if necessary
    private void saveLastSearch(String searchString){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        prefs.edit().putString(getActivity().getString(R.string.pref_key_last_searched), searchString).apply();
    }


    //Retrieve the most recent saved search from shared preferences
    private void loadLastSearch(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mRecentSearch = prefs.getString(getActivity().getString(R.string.pref_key_last_searched), "");
    }

    //Search for a list of artists based on the search bar string
    public void searchForArtist(){
        mRecentSearch = searchBar.getText().toString();
        mRecentSearch = mRecentSearch.trim();
        saveLastSearch(mRecentSearch);

        //Log.v(TAG, "Searching on string: " + mRecentSearch);

        //Handle a condition in which the search bar is empty
        if(mRecentSearch.length() == 0){
            Toast.makeText(getActivity(), getActivity().getString(R.string.error_invalid_search_string), Toast.LENGTH_SHORT).show();
            //Log.v(TAG, "Invalid Search String");
        }else{
            try {

                //Check if the network is available, if it is, search for artists
                if (isNetworkAvailable()) {
                    Log.v(TAG, "SEARCHING FOR ARTIST");
                    searchTask task = new searchTask();
                    task.execute(mRecentSearch);
                }else{
                    //If there is no internet connection, alert the user and do not attempt to search
                    Toast.makeText(getActivity(), getActivity().getString(R.string.error_no_internet), Toast.LENGTH_SHORT).show();
                }

            } catch (Exception e) {

                //Log.v(TAG, "Invalid Search String");
                e.printStackTrace();
            }
        }
    }

    //Asynce task responsible for returning an artist list via the spotify API
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
            if (LoArtists != null && LoArtists.size() == 0){
                Toast.makeText(getActivity(), getActivity().getString(R.string.notificaiton_failed_search), Toast.LENGTH_SHORT).show();
                mSpotifyArtistAdapter.clearArtists();
                mSpotifyArtistAdapter.notifyDataSetChanged();
            }else{
                mLoArtist = LoArtists;
                mSpotifyArtistAdapter.replaceArtistList(mLoArtist);
                mSpotifyArtistAdapter.notifyDataSetChanged();
            }
        }
    }

    //Based on stack overflow snippet from suggestion, determines whether the device has an internet connection
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}
