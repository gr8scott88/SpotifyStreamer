package nanodegree.nemesisdev.com.spotifystreamer.Helpers;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import nanodegree.nemesisdev.com.spotifystreamer.R;

/**
 * Created by Scott on 6/19/2015.
 */
public class SpotifyArtistHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private static final String TAG = SpotifyArtistHolder.class.getSimpleName();
    protected ImageView mThumbnail;
    protected TextView mArtistName;
    private IArtistHolderClickListener mListener;

    public SpotifyArtistHolder(View itemView, IArtistHolderClickListener listener) {
        super(itemView);
        this.mThumbnail = (ImageView) itemView.findViewById(R.id.artist_picture);
        this.mArtistName = (TextView) itemView.findViewById(R.id.artist_name);
        this.mListener = listener;
        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        mListener.onArtistClick(v, getAdapterPosition());
    }

    public interface IArtistHolderClickListener{
        void onArtistClick(View caller, Integer position);
    }
}
