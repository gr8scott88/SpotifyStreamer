package nanodegree.nemesisdev.com.spotifystreamer.Helpers;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import kaaes.spotify.webapi.android.models.Track;
import nanodegree.nemesisdev.com.spotifystreamer.R;

/**
 * Created by Scott on 6/21/2015.
 */
public class SpotifyTrackRecyclerAdapter extends RecyclerView.Adapter<SpotifyTrackHolder>{
    private final String TAG = SpotifyTrackRecyclerAdapter.class.getSimpleName();

    private ArrayList<Track> mLoTracks;
    private Context mContext;

    public SpotifyTrackRecyclerAdapter(ArrayList<Track> mLoTracks, Context mContext) {
        this.mLoTracks = mLoTracks;
        this.mContext = mContext;
    }

    public interface TrackCallback {
        public void onTrackClick(int pos,  ArrayList<Track> LoTrack);
    }
    
    @Override
    public SpotifyTrackHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_track, null);
        SpotifyTrackHolder holder = new SpotifyTrackHolder(view, new SpotifyTrackHolder.ITrackHolderClickListener() {
            @Override
            public void onTrackClick(View caller, Integer pos) {
                String clickedTrack = (String) ((TextView) caller.findViewById(R.id.track_name)).getText().toString();

                Log.v(TAG, "Clicked track has sample url of: " + mLoTracks.get(pos).preview_url);

                ((TrackCallback) mContext).onTrackClick(pos, mLoTracks);

                /*
                Intent intent = new Intent(mContext, Activity_Spotify_Streamer.class);
                intent.putExtra(mContext.getString(R.string.key_track_id_extra), mLoTracks.get(pos).id);
                mContext.startActivity(intent);
                */

            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(SpotifyTrackHolder holder, int position) {
        Track track = mLoTracks.get(position);
        //Log.d(TAG, "Processing " + artist.name + " --> " + Integer.toString(position));

        holder.mAlbum.setText(track.album.name);
        holder.mTrack.setText(track.name);

        try {
            //TODO: Add logic to which image gets pulled based on screen size
            String imageUrl = track.album.images.get(0).url;

            Picasso.with(mContext).load(imageUrl)
                    .error(R.drawable.spotify_icon_no_image_found)
                    .placeholder(R.drawable.spotify_icon_no_image_found)
                    .into(holder.mThumbnail);
        } catch (Exception e) {
            //Log.v(TAG, "Artist " + artist.name + " has no images in spotify");
            holder.mThumbnail.setImageResource(R.drawable.spotify_icon_no_image_found);
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return this.mLoTracks.size();
    }


    //Replace current track list with a passed track list
    public void replaceTrackList(ArrayList<Track> newLoTrack){
        this.mLoTracks = newLoTrack;
    }
}
