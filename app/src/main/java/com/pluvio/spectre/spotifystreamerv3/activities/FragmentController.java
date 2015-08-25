package com.pluvio.spectre.spotifystreamerv3.activities;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;

import com.pluvio.spectre.spotifystreamerv3.R;
import com.pluvio.spectre.spotifystreamerv3.navigation.ArtistSearchFragment;
import com.pluvio.spectre.spotifystreamerv3.navigation.Top10TracksFragment;
import com.pluvio.spectre.spotifystreamerv3.navigation.listItems.ArtistListItem;
import com.pluvio.spectre.spotifystreamerv3.previewer.PreviewerElements.PreviewPlayerFragment;

/**
 * Created by Spectre on 8/15/2015.
 * <p/>
 * This class handles the switching of fragments.
 */
public class FragmentController {

    // Private Vars
    private static MainActivity sContext;
    private static FragmentManager sFragmentManager;

    private static boolean mIsTwoPane;

    // Fragments
    public static final String FRAGMENT_ARTIST_SEARCH = "artist_search_fragment";
    public static final String FRAGMENT_TOP_10 = "top_10_fragment";
    public static final String FRAGMENT_PREVIEW_PLAYER = "preview_player_fragment";

    public static void initialize(MainActivity context) {
        sContext = context;
        sFragmentManager = context.getFragmentManager();
        mIsTwoPane = context.findViewById(R.id.activity_main_artist_fragment) != null;
    }

    /**
     * This functions calls for the ArtistSearchFragment to be shown
     */
    public static void showArtistSearchFragment() {

        ArtistSearchFragment artistSearchFragment;
        if ((artistSearchFragment = getArtistSearchFragment()) == null)
            artistSearchFragment = new ArtistSearchFragment();

        if (!isTwoPane()) {
            createFragment(artistSearchFragment, FRAGMENT_ARTIST_SEARCH);
        } else {
            FragmentTransaction ft = sFragmentManager.beginTransaction();
            ft.replace(R.id.activity_main_artist_fragment, artistSearchFragment, FRAGMENT_ARTIST_SEARCH);
            ft.commit();
        }

    }

    /**
     * This functions calls for the top10TracksFragment to be shown
     *
     * @param artist The Spotify ID of the selected Artist
     * @return the created Top10TracksFragment
     */
    public static void showTop10TracksFragment(ArtistListItem artist) {

        Top10TracksFragment top10Fragment = new Top10TracksFragment();

        Bundle arguments = new Bundle();
        arguments.putString(Top10TracksFragment.EXTRA_ID, artist.getId());
        arguments.putString(Top10TracksFragment.EXTRA_ARTIST, artist.getName());
        top10Fragment.setArguments(arguments);

        createFragment(top10Fragment, FRAGMENT_TOP_10);
    }

    /**
     * This functions calls for the PreviewPlayerFragment to be shown
     *
     * @return the created PreviewPlayerFragment
     */
    public static PreviewPlayerFragment showPreviewPlayerFragment() {

        PreviewPlayerFragment previewPlayerFragment = getPreviewPlayerFragment();

        if (previewPlayerFragment == null)
            previewPlayerFragment = new PreviewPlayerFragment();

        if (!mIsTwoPane)
            createFragment(previewPlayerFragment, FRAGMENT_PREVIEW_PLAYER);
        else
            previewPlayerFragment.show(sFragmentManager, FRAGMENT_PREVIEW_PLAYER);

        return previewPlayerFragment;

    }

    /**
     * This function clears the top10Fragment. Useful when the user clicks on a new artist in twoPane.
     */
    public static void clearTop10Fragment() {

        Top10TracksFragment top10Fragment = getTop10TracksFragment();

        if (top10Fragment == null || !top10Fragment.isVisible())
            return;

        FragmentTransaction ft = sFragmentManager.beginTransaction();
        ft.remove(top10Fragment);
        ft.commit();

    }

    /**
     * This functions creates a fragment
     */
    private static void createFragment(Fragment fragment, String tag) {

        // If the fragment already is visible, stop.
        if (fragment.isVisible())
            return;

        FragmentTransaction ft = sFragmentManager.beginTransaction();

        if (!mIsTwoPane) {
            // Invalidate options menu so it will check for "Now Playing"
            ft.addToBackStack(null);
            updateMenu();
        }

        ft.replace(R.id.fragment, fragment, tag).commit();

    }

    public static ArtistSearchFragment getArtistSearchFragment() {
        return (ArtistSearchFragment) sFragmentManager.findFragmentByTag(FRAGMENT_ARTIST_SEARCH);
    }

    public static Top10TracksFragment getTop10TracksFragment() {
        return (Top10TracksFragment) sFragmentManager.findFragmentByTag(FRAGMENT_TOP_10);
    }

    public static PreviewPlayerFragment getPreviewPlayerFragment() {
        return (PreviewPlayerFragment) sFragmentManager.findFragmentByTag(FRAGMENT_PREVIEW_PLAYER);
    }

    public static void setSubtitle(String subtitle) {
        sContext.setSubtitle(subtitle);
    }

    public static void updateMenu() {
        sContext.refreshMenu();
    }

    public static boolean isTwoPane() {
        return mIsTwoPane;
    }


}

