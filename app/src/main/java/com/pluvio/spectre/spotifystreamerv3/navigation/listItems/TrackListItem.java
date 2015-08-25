package com.pluvio.spectre.spotifystreamerv3.navigation.listItems;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Spectre on 8/21/2015.
 */
public class TrackListItem implements Serializable {

    private String name;
    private String album;
    private String artist;
    private String preview_url;
    private String trackID;
    private ArrayList<String> images;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getPreview_url() {
        return preview_url;
    }

    public void setPreview_url(String preview_url) {
        this.preview_url = preview_url;
    }

    public ArrayList<String> getImages() {
        return images;
    }

    public void setImages(ArrayList<String> imageAddresses) {
        this.images = imageAddresses;
    }

    public String getID() {
        return trackID;
    }

    public void setID(String trackID) {
        this.trackID = trackID;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }
}
