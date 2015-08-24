package nanodegree.nemesisdev.com.spotifystreamer.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import java.util.ArrayList;
import kaaes.spotify.webapi.android.models.Track;
import nanodegree.nemesisdev.com.spotifystreamer.Fragments.Fragment_Top10Tracks;
import nanodegree.nemesisdev.com.spotifystreamer.Objects.ParcelableTrack;
import nanodegree.nemesisdev.com.spotifystreamer.R;
import nanodegree.nemesisdev.com.spotifystreamer.Helpers.SpotifyTrackRecyclerAdapter;

public class Activity_Top10Tracks extends AppCompatActivity implements SpotifyTrackRecyclerAdapter.TrackCallback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top10_tracks);

        if (savedInstanceState == null) {

            // Create the detail fragment and add it to the activity
            // using a fragment transaction.

            String artistName = getIntent().getStringExtra(this.getString(R.string.key_artist_id_extra));

            Bundle arguments = new Bundle();
            arguments.putString(this.getString(R.string.key_artist_id_extra), artistName);

            Fragment_Top10Tracks fragment = new Fragment_Top10Tracks();
            fragment.setArguments(arguments);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_window_top_10_tracks, fragment)
                    .commit();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_top10_tracks, menu);
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


    @Override
    public void onTrackClick(int pos, ArrayList<Track> LoTracks) {
        Track current = LoTracks.get(pos);
        //Toast.makeText(this, "(PHONE)Clicked on " + current.id, Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, Activity_Spotify_Streamer.class);
        intent.putExtra(getString(R.string.key_selected_track), pos);


        //Build parcelable track list from the list of tracks
        //TODO increase efficiency?
        ArrayList<ParcelableTrack> parcelableTrackList = new ArrayList<>();
        for (Track t : LoTracks){
            parcelableTrackList.add(new ParcelableTrack(t));
        }

        intent.putParcelableArrayListExtra(getString(R.string.key_parcelable_track_list), parcelableTrackList);
        this.startActivity(intent);


    }

}
