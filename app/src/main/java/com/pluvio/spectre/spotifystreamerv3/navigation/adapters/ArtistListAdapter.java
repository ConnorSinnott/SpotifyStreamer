package com.pluvio.spectre.spotifystreamerv3.navigation.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.pluvio.spectre.spotifystreamerv3.R;
import com.pluvio.spectre.spotifystreamerv3.navigation.listItems.ArtistListItem;
import com.squareup.picasso.Picasso;

/**
 * Created by Spectre on 8/15/2015.
 */
public class ArtistListAdapter extends ArrayAdapter<ArtistListItem> {

    public ArtistListAdapter(Activity context) {
        super(context, R.layout.list_item_artist);
    }

    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View currentView = inflater.inflate(R.layout.list_item_artist, null, true);
        ArtistListItem currentItem = getItem(position);

        // Setting Artist Name
        if (currentItem.getName().length() > 0) {
            TextView textTitle = (TextView) currentView.findViewById(R.id.artist_list_item_textView);
            textTitle.setText(currentItem.getName());
        }

        // Setting Artist Image
        if (currentItem.getImages().size() > 0) {
            ImageView imageView = (ImageView) currentView.findViewById(R.id.artist_list_item_imageView);
            Picasso.with(getContext())
                    .load(currentItem.getImages().get(1))
                    .resize(100, 100)
                    .centerCrop()
                    .into(imageView);
        }

        return currentView;
    }

}
