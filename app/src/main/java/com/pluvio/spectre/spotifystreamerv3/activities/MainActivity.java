package com.pluvio.spectre.spotifystreamerv3.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.pluvio.spectre.spotifystreamerv3.R;
import com.pluvio.spectre.spotifystreamerv3.previewer.PreviewController;

public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize the two control classes
        FragmentController.initialize(this);
        PreviewController.initialize(this);

        if (FragmentController.getArtistSearchFragment() == null)
            FragmentController.showArtistSearchFragment();

    }

    @Override
    protected void onStart() {

        // Hides the notification when Application is visible.
        PreviewController.clearNotification();

        super.onStart();

    }

    @Override
    public void onBackPressed() {

        // Override back button to use fragment setup
        if (getFragmentManager().getBackStackEntryCount() > 1) {
            getFragmentManager().popBackStack();

            // Invalidate options menu so it will check for "Now Playing"
            refreshMenu();

        } else {
            if (PreviewController.isPlaying())
                PreviewController.prepareForExit();

            super.onBackPressed();

        }
    }

    @Override
    protected void onStop() {
        // Shows PlayerNotification if a track is still playing
        if (PreviewController.isPlaying())
            PreviewController.updateNotification();

        super.onStop();
    }

    /**
     * Menu/ActionBar Functionality
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        // Show "Now Playing" icon if music is playing and PreviewPlayerFragment is not visible.
        menu.findItem(R.id.action_nowplaying).setVisible(PreviewController.isPlayingInBackground());

        // Show "Share" icon if music is playing
        MenuItem share = menu.findItem(R.id.action_share);
        if (PreviewController.isVisible() || (FragmentController.isTwoPane() && PreviewController.isPlaying())) {
            share.setVisible(true);
            android.support.v7.widget.ShareActionProvider shareActionProvider = (android.support.v7.widget.ShareActionProvider) MenuItemCompat.getActionProvider(share);
            shareActionProvider.setShareIntent(PreviewController.getShareIntent());
        } else
            share.setVisible(false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Open Settings Activity
        if (item.getItemId() == R.id.action_settings)
            startActivity(new Intent(this, SettingsActivity.class));

        // Resume PreviewPlayerFragment
        if (item.getItemId() == R.id.action_nowplaying)
            FragmentController.showPreviewPlayerFragment();

        return super.onOptionsItemSelected(item);

    }

    public void refreshMenu() {
        invalidateOptionsMenu();
    }

    public void setSubtitle(String subtitle) {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setSubtitle(subtitle);
        refreshMenu();
    }

}
