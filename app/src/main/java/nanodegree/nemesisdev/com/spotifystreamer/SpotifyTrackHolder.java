package nanodegree.nemesisdev.com.spotifystreamer;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Scott on 6/21/2015.
 */
public class SpotifyTrackHolder extends RecyclerView.ViewHolder implements View.OnClickListener  {
    private static final String TAG = SpotifyTrackHolder.class.getSimpleName();
    protected ImageView mThumbnail;
    protected TextView mAlbum;
    protected TextView mTrack;
    private ITrackHolderClickListener mListener;

    public SpotifyTrackHolder(View itemView, ITrackHolderClickListener mListener) {
        super(itemView);
        this.mThumbnail = (ImageView) itemView.findViewById(R.id.album_picture);
        this.mAlbum = (TextView) itemView.findViewById(R.id.album_name);
        this.mTrack = (TextView) itemView.findViewById(R.id.track_name);
        this.mListener = mListener;
        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        mListener.onTrackClick(v, getAdapterPosition());
    }

    public interface ITrackHolderClickListener{
        void onTrackClick(View caller, Integer position);
    }
}
