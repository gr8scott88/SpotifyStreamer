package nanodegree.nemesisdev.com.spotifystreamer;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import java.util.ArrayList;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.*;
import kaaes.spotify.webapi.android.models.Artist;


public class Fragment_Search extends Fragment{
    private static final String TAG = Fragment_Search.class.getSimpleName();

    public SpotifyArtistRecyclerAdapter mSpotifyArtistAdapter;
    private RecyclerView mArtistReyclerView;
    private ArrayList<Artist> mLoArtist;
    private String mRecentSearch;
    private boolean bRepopulateArtists = false;
    private SearchView searchView;

    public Fragment_Search() {    }




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
            searchForArtist(mRecentSearch);
            bRepopulateArtists = false;
        }
    }

    private void initUIComponents(View rootView){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mRecentSearch = prefs.getString(getActivity().getString(R.string.pref_key_last_searched), "");
        searchView = (SearchView)rootView.findViewById(R.id.search_artist);
        searchView.setQuery(mRecentSearch, true);
        mArtistReyclerView = (RecyclerView) rootView.findViewById(R.id.artist_list);
        mArtistReyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mArtistReyclerView.setAdapter(mSpotifyArtistAdapter);
    }

    public void attachListeners(){
        searchView.setOnQueryTextListener(
                new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        Log.v(TAG, "Initial Query: " + query);
                        String trimmedQuery = query.trim();
                        Log.v(TAG, "Trimmed Query: " + trimmedQuery);
                        searchForArtist(trimmedQuery);
                        return true;
                    }
                    @Override
                    public boolean onQueryTextChange(String newText) {
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
    public void searchForArtist(String search){

        saveLastSearch(search);

        //Log.v(TAG, "Searching on string: " + mRecentSearch);

        //Handle a condition in which the search bar is empty
        if(search.length() == 0){
            Toast.makeText(getActivity(), getActivity().getString(R.string.error_invalid_search_string), Toast.LENGTH_SHORT).show();
            //Log.v(TAG, "Invalid Search String");
        }else{
            try {

                //Check if the network is available, if it is, search for artists
                if (isNetworkAvailable()) {
                    Log.v(TAG, "SEARCHING FOR ARTIST");
                    searchTask task = new searchTask();
                    task.execute(search);
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

    //Async task responsible for returning an artist list via the spotify API
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
