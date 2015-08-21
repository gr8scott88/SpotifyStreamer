package nanodegree.nemesisdev.com.spotifystreamer.Activities;

import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;

import nanodegree.nemesisdev.com.spotifystreamer.Fragments.Fragment_SpotifyStreamer;
import nanodegree.nemesisdev.com.spotifystreamer.Objects.ParcelableTrack;
import nanodegree.nemesisdev.com.spotifystreamer.R;


public class Activity_Spotify_Streamer extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spotify__streamer);

        if (savedInstanceState == null) {
            // Create the streamer fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();

            int currentTrack = getIntent().getIntExtra(this.getString(R.string.key_selected_track), 0);
            ArrayList<ParcelableTrack> LoTracks = getIntent().getParcelableArrayListExtra(getString(R.string.key_parcelable_track_list));

            arguments.putInt(getString(R.string.key_selected_track), currentTrack);
            arguments.putParcelableArrayList(getString(R.string.key_parcelable_track_list), LoTracks);

            Fragment_SpotifyStreamer fragment = new Fragment_SpotifyStreamer();
            fragment.setShowsDialog(false);
            fragment.setArguments(arguments);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.streamer_fragment_container, fragment)
                    .commit();
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_spotify__streamer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
