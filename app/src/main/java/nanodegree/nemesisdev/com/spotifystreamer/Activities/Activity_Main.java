package nanodegree.nemesisdev.com.spotifystreamer.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import java.util.ArrayList;
import kaaes.spotify.webapi.android.models.Track;
import nanodegree.nemesisdev.com.spotifystreamer.Fragments.Fragment_SpotifyStreamer;
import nanodegree.nemesisdev.com.spotifystreamer.Fragments.Fragment_Top10Tracks;
import nanodegree.nemesisdev.com.spotifystreamer.Objects.ParcelableTrack;
import nanodegree.nemesisdev.com.spotifystreamer.R;
import nanodegree.nemesisdev.com.spotifystreamer.Helpers.SpotifyArtistRecyclerAdapter;
import nanodegree.nemesisdev.com.spotifystreamer.Helpers.SpotifyTrackRecyclerAdapter;

public class Activity_Main extends AppCompatActivity implements SpotifyArtistRecyclerAdapter.ArtistCallback, SpotifyTrackRecyclerAdapter.TrackCallback {

    //Layout Types:
    //  0: Standard 1 pane layout
    //  1: Widescreen, 2 pane layout
    //  2+: additional screen options as necessary
    private int mLayoutType = 0;

    //Tag the fragment so it can be found later as necessary
    private static final String TOP10FRAG = "TOP10FRAG_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (findViewById(R.id.song_fragment_container) != null) {
            mLayoutType = 1;
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.song_fragment_container, new Fragment_Top10Tracks(), TOP10FRAG)
                        .commit();
            }
        }

        Toast.makeText(this, "Layout type: " + mLayoutType, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_activity__main, menu);
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
    public void onArtistClick(String artistId) {
        if (mLayoutType == 1){
            Toast.makeText(this, "(TABLET)Clicked on " + artistId, Toast.LENGTH_SHORT).show();
            Bundle args = new Bundle();
            args.putString(this.getString(R.string.key_artist_id_extra), artistId);

            Fragment_Top10Tracks fragment = new Fragment_Top10Tracks();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.song_fragment_container, fragment, TOP10FRAG)
                    .commit();
        } else {
            Intent intent = new Intent(this, Activity_Top10Tracks.class);
            intent.putExtra(this.getString(R.string.key_artist_id_extra), artistId);
            startActivity(intent);

           Toast.makeText(this, "(PHONE)Clicked on " + artistId, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onTrackClick(int pos, ArrayList<Track> LoTracks) {
        Track current = LoTracks.get(pos);
        if (mLayoutType == 1){
            Toast.makeText(this, "(TABLET)Clicked on " + current.id, Toast.LENGTH_SHORT).show();
            Bundle args = new Bundle();

            //Build parcelable track list from the list of tracks
            //TODO increase efficiency?
            ArrayList<ParcelableTrack> parcelableTrackList = new ArrayList<ParcelableTrack>();
            for (Track t : LoTracks){
                parcelableTrackList.add(new ParcelableTrack(t));
            }

            args.putInt(getString(R.string.key_selected_track), pos);
            args.putParcelableArrayList(getString(R.string.key_parcelable_track_list), parcelableTrackList);

            showSteamerDialog(args);
        } else {
            Toast.makeText(this, "(PHONE)Clicked on " + current.id, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, Activity_Spotify_Streamer.class);
            intent.putExtra(this.getString(R.string.key_track_id_extra), current.id);
            this.startActivity(intent);
        }
    }


    private void showSteamerDialog(Bundle args){
        android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        android.support.v4.app.Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        Fragment_SpotifyStreamer dialogFragment = new Fragment_SpotifyStreamer();
        dialogFragment.setArguments(args);

        //ft.commitAllowingStateLoss();
        dialogFragment.show(ft, "streamerDialog");

        ft.addToBackStack("streamerDialog");
    }

}
