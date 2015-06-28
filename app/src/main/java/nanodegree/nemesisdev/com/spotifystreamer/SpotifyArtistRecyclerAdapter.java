package nanodegree.nemesisdev.com.spotifystreamer;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import java.util.ArrayList;


import kaaes.spotify.webapi.android.models.Artist;

/**
 * Created by Scott on 6/19/2015.
 */
public class SpotifyArtistRecyclerAdapter extends RecyclerView.Adapter<SpotifyArtistHolder>{
    private final String TAG = SpotifyArtistRecyclerAdapter.class.getSimpleName();

    private ArrayList<Artist> mLoArtist;
    private Context mContext;

    public SpotifyArtistRecyclerAdapter(ArrayList<Artist> mLoArtist, Context mContext) {
        this.mLoArtist = mLoArtist;
        this.mContext = mContext;
    }

    @Override
    public SpotifyArtistHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_artist, null);
        SpotifyArtistHolder holder = new SpotifyArtistHolder(view, new SpotifyArtistHolder.IArtistHolderClickListener() {
            @Override
            public void onArtistClick(View caller, Integer pos) {
                String clickedArtist = (String) ((TextView) caller.findViewById(R.id.artist_name)).getText().toString();
                //Log.v(TAG, "Clicked on " + clickedArtist + " in position " + pos + " with id " + mLoArtist.get(pos).id);
                Intent intent = new Intent(mContext, Activity_Top10Tracks.class);
                intent.putExtra(mContext.getString(R.string.key_artist_id_extra), mLoArtist.get(pos).id);
                mContext.startActivity(intent);

                //Toast.makeText(mContext, "Clicked on " + clickedArtist, Toast.LENGTH_SHORT).show();
            }
        });
        return holder;
    }


    //Replace the current list of artists with a new passed list
    public void replaceArtistList(ArrayList<Artist> newLoArtist){
        this.mLoArtist = newLoArtist;
    }

    //Clear the list of artists
    public void clearArtists(){
        this.mLoArtist = new ArrayList<Artist>();
    }

    @Override
    public void onBindViewHolder(SpotifyArtistHolder holder, int position) {
        Artist artist = mLoArtist.get(position);
        //Log.d(TAG, "Processing " + artist.name + " --> " + Integer.toString(position));

        holder.mArtistName.setText(artist.name);

        try {
            //TODO: Add logic to which image gets pulled based on screen size
            String imageUrl = artist.images.get(0).url;

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
        return mLoArtist.size();
    }

    public Artist getArtist(int pos) {
        return (null != mLoArtist ? mLoArtist.get(pos) : null);
    }


}
