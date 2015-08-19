package nanodegree.nemesisdev.com.spotifystreamer;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistSimple;
import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by Scott on 8/17/2015.
 */
public class ParcelableTrack implements Parcelable {

    private String mAlbumName;
    private String mArtistName;
    private String mSongTitle;
    private String mAlbumImageUrl;
    private String mTrackPreviewUrl;


    public ParcelableTrack(String mAlbumName, String mArtistName, String mSongTitle, String mAlbumImageUrl, String mTrackPreviewUrl) {
        this.mAlbumName = mAlbumName;
        this.mArtistName = mArtistName;
        this.mSongTitle = mSongTitle;
        this.mAlbumImageUrl = mAlbumImageUrl;
        this.mTrackPreviewUrl = mTrackPreviewUrl;
    }

    public ParcelableTrack(Track track){
        this.mAlbumName = track.album.name.toString();
        List<ArtistSimple> tempList = track.artists;
        this.mArtistName = "";
        for (ArtistSimple a : tempList){
            this.mArtistName = this.mArtistName + a.name + " ";
        }
        this.mSongTitle = track.name.toString();
        this.mAlbumImageUrl = track.album.images.get(0).url;
        this.mTrackPreviewUrl = track.preview_url;
    }

    public ParcelableTrack(Parcel in){
        String[] data = new String[5];
        in.readStringArray(data);
        this.mAlbumName = data[0];
        this.mArtistName = data[1];
        this.mSongTitle = data[2];
        this.mAlbumImageUrl = data[3];
        this.mTrackPreviewUrl = data[4];
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[] {
                this.mAlbumName,
                this.mArtistName,
                this.mSongTitle,
                this.mAlbumImageUrl,
                this.mTrackPreviewUrl});
    }

    public static final Parcelable.Creator<ParcelableTrack> CREATOR = new Parcelable.Creator<ParcelableTrack>(){
        public ParcelableTrack createFromParcel(Parcel in){
            return new ParcelableTrack(in);
        }

        public ParcelableTrack[] newArray(int size) {
            return new ParcelableTrack[size];
        }
    };

    public String getAlbumName() {
        return mAlbumName;
    }

    public String getArtistName() {
        return mArtistName;
    }

    public String getSongTitle() {
        return mSongTitle;
    }

    public String getAlbumImageUrl() {
        return mAlbumImageUrl;
    }

    public String getTrackPreviewUrl() {
        return mTrackPreviewUrl;
    }
}
