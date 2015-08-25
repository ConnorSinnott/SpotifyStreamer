package com.pluvio.spectre.spotifystreamerv3.navigation.listItems;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Spectre on 8/21/2015.
 */
public class ArtistListItem implements Serializable {

    private String artistID;
    private String name;
    private ArrayList<String> images;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setImages(ArrayList<String> imageAddresses) {
        this.images = imageAddresses;
    }

    public ArrayList<String> getImages() {
        return images;
    }

    public String getId() {
        return artistID;
    }

    public void setId(String id) {
        this.artistID = id;
    }
}
