package com.pluvio.spectre.spotifystreamerv3.previewer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.widget.Toast;

import com.pluvio.spectre.spotifystreamerv3.R;
import com.pluvio.spectre.spotifystreamerv3.activities.FragmentController;
import com.pluvio.spectre.spotifystreamerv3.navigation.listItems.TrackListItem;
import com.pluvio.spectre.spotifystreamerv3.other.Debugger;
import com.pluvio.spectre.spotifystreamerv3.previewer.PreviewerElements.PlayerNotification;
import com.pluvio.spectre.spotifystreamerv3.previewer.PreviewerElements.PreviewPlayerFragment;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Created by Spectre on 8/15/2015.
 * <p/>
 * Once the user has selected a track from the navigation fragments, the TrackList and TrackIndex are sent here.
 * This class collects and hosts all the info on the track, making it available to the UI.
 * This class is also responsible for running the UI. It is set up as the parent to PlayerNotification, PreviewPlayer.
 */
public class PreviewController extends BroadcastReceiver {

    private static final String LOG_TAG = "PreviewController";

    private static Context sContext;

    private static MediaPlayer sMediaPlayer = new MediaPlayer();
    private static PlayerNotification sPlayerNotification;

    private static ArrayList<TrackListItem> sTrackList;
    private static int sCurrentTrackIndex;
    private static Bitmap sAlbumArt;

    private static String lastID;

    public static void initialize(Context mainActivity) {
        sContext = mainActivity;

        // Prepare MediaPlayer
        sMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                updatePlayerUI();
            }
        });

        // Prepare Notification
        sPlayerNotification = new PlayerNotification(mainActivity);
    }

    /***
     * Top10Tracks sends the User's selected track here. This class assigns the track viarables, downloads the AlbumArt
     * then exports the information to all the UI elements.
     *
     * @param trackList
     * @param currentTrackIndex
     */
    public static void sendSelected(ArrayList<TrackListItem> trackList, int currentTrackIndex) {

        // Allows user to leave NowPlaying, return to list, and resume the same song.
        String trackID = trackList.get(currentTrackIndex).getID();
        if (lastID != null && lastID.equals(trackID)) {
            FragmentController.showPreviewPlayerFragment();
            return;
        }
        lastID = trackID;

        // Assigning Vars
        sTrackList = trackList;
        sCurrentTrackIndex = currentTrackIndex;

        // Downloading AlbumArt
        try {
            sAlbumArt = new AsyncTask<String, Void, Bitmap>() {

                @Override
                protected Bitmap doInBackground(String... urls) {
                    try {
                        return Picasso.with(sContext).load(urls[0]).get();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Bitmap albumArt) {
                    sAlbumArt = albumArt;
                }

            }.execute(getCurrentTrack().getImages().get(0)).get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            Toast.makeText(sContext, R.string.search_failed_no_internet, Toast.LENGTH_LONG).show();
            Debugger.print(LOG_TAG, e.getMessage());
            return;
        }

        // Now that I have everything, load the track
        boolean wasPlaying = isPlaying();
        loadTrack(getTrackAddress());

        if (wasPlaying)
            play();

        // Create Notification
        if (sPlayerNotification.isVisible())
            sPlayerNotification.updateNotification();

        if (!sPlayerNotification.isVisible())
            launchPlayer();

    }

    public static void launchPlayer() {

        PreviewPlayerFragment previewPlayerFragment = FragmentController.getPreviewPlayerFragment();

        // First time creation
        if (previewPlayerFragment == null) {
            FragmentController.showPreviewPlayerFragment();
            return;
        }

        // If the player is running in dialog
        if (FragmentController.isTwoPane() && previewPlayerFragment.getDialog() != null) {
            updatePlayerUI();
            return;
        }

        // If the player is running as a fragment
        if (previewPlayerFragment.isVisible())
            updatePlayerUI();
        else
            FragmentController.showPreviewPlayerFragment();

    }

    /***
     * Track Info
     */

    public static TrackListItem getCurrentTrack() {
        return sTrackList.get(sCurrentTrackIndex);
    }

    public static String getArtistName() {
        return getCurrentTrack().getArtist();
    }

    public static String getAlbumName() {
        return getCurrentTrack().getAlbum();
    }

    public static String getTrackName() {
        return getCurrentTrack().getName();
    }

    public static Bitmap getAlbumArt() {
        return sAlbumArt;
    }

    public static String getTrackAddress() {
        return getCurrentTrack().getPreview_url();
    }

    public static boolean hasNext() {
        return sCurrentTrackIndex < sTrackList.size() - 1;
    }

    public static boolean hasPrevious() {
        return sCurrentTrackIndex > 0;
    }

    public static Intent getShareIntent() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, sContext.getString(R.string.send_message_default) + "\n" + getTrackAddress());
        return intent;
    }

    /***
     * UI Controls
     */

    public static void updatePlayerUI() {
        FragmentController.getPreviewPlayerFragment().updateUI();
    }

    public static void updateNotification() {
        sPlayerNotification.updateNotification();
    }

    public static void clearNotification() {
        sPlayerNotification.clearNotification();
    }

    /***
     * Player Controls
     */

    public static void loadTrack(String preview_url) {

        // Pause music if it is playing
        pause();

        // Creating a new media player
        sMediaPlayer = new android.media.MediaPlayer();
        sMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        // Pause track and seek to 0 when the track is over
        sMediaPlayer.setOnCompletionListener(new android.media.MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(android.media.MediaPlayer mp) {
                sMediaPlayer.pause();
                sMediaPlayer.seekTo(0);
            }
        });

        try {
            //Prepare Song
            sMediaPlayer.setDataSource(preview_url);
            sMediaPlayer.prepare();
        } catch (IOException e) {
            Debugger.print(LOG_TAG, e.getMessage());
        }
    }

    public static boolean isPlaying() {
        return sMediaPlayer.isPlaying();
    }

    public static void play() {
        if (sMediaPlayer.isPlaying())
            return;

        sMediaPlayer.start();
        FragmentController.updateMenu();
    }

    public static void pause() {
        if (!sMediaPlayer.isPlaying())
            return;

        sMediaPlayer.pause();
    }

    public static void togglePlay() {
        if (isPlaying())
            pause();
        else
            play();
        updateNotification();
    }

    public static void nextTrack() {
        if (hasNext())
            sendSelected(sTrackList, sCurrentTrackIndex + 1);
    }

    public static void previousTrack() {
        if (hasPrevious())
            sendSelected(sTrackList, sCurrentTrackIndex - 1);
    }

    public static int getTrackPosition() {
        return sMediaPlayer.getCurrentPosition();
    }

    public static void setTrackPosition(int position) {
        sMediaPlayer.seekTo(position);
    }

    public static boolean isVisible() {
        if (FragmentController.getPreviewPlayerFragment() == null)
            return false;
        return FragmentController.getPreviewPlayerFragment().isVisible();
    }

    public static boolean isPlayingInBackground() {
        return isPlaying() && !isVisible();
    }

    public static void prepareForExit() {
        if (isPlaying())
            pause();

        if (sPlayerNotification.isVisible())
            sPlayerNotification.clearNotification();
    }

    /***
     * Notification Broadcast Receiver
     */
    @Override
    public void onReceive(Context context, Intent intent) {

        switch (intent.getStringExtra(PlayerControls.REQUEST_KEY)) {
            case PlayerControls.REQUEST_TOGGLE_PLAY:
                togglePlay();
                break;
            case PlayerControls.REQUEST_NEXT_TRACK:
                nextTrack();
                break;
            case PlayerControls.REQUEST_PREVIOUS_TRACK:
                previousTrack();
                break;

        }

    }

    public static class PlayerControls {
        public static final String REQUEST_KEY = "Player_Request";
        public static final String REQUEST_TOGGLE_PLAY = "Toggle_Track";
        public static final String REQUEST_NEXT_TRACK = "Next_Track";
        public static final String REQUEST_PREVIOUS_TRACK = "Previous_Track";
    }

}
