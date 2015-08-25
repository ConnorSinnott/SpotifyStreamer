package com.pluvio.spectre.spotifystreamerv3.previewer.PreviewerElements;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.pluvio.spectre.spotifystreamerv3.R;
import com.pluvio.spectre.spotifystreamerv3.activities.FragmentController;
import com.pluvio.spectre.spotifystreamerv3.previewer.PreviewController;

import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;

/**
 * Created by Spectre on 8/15/2015.
 */
public class PreviewPlayerFragment extends DialogFragment {

    private static final String LOG_TAG = PreviewPlayerFragment.class.getSimpleName();

    // Views
    private View mPlayerView;
    private TextView mTextArtist, mTextAlbum, mTextTrack, mTextSeekPosition, mTextSeekDuration;
    private ImageButton mButtonLast, mButtonNext;
    private ImageView mImageView;
    private ToggleButton mTogglePlay;
    private SeekBar mSeekBar;

    // Called when using a Two-Paned layout
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Create View
        mPlayerView = getActivity().getLayoutInflater().inflate(R.layout.fragment_preview_player, null);

        // Init view
        init();

        // Create dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(mPlayerView);

        return builder.create();

    }

    // Called when using a Single-Paned layout
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // So onCreateView does not mess up dialog when view is created
        if (FragmentController.isTwoPane())
            return super.onCreateView(inflater, container, savedInstanceState);

        mPlayerView = inflater.inflate(R.layout.fragment_preview_player, container, false);

        init();

        return mPlayerView;

    }

    @Override
    public void onStart() {
        updateUI();
        FragmentController.updateMenu();
        super.onStart();
    }

    // Initialize
    private void init() {

        if (!FragmentController.isTwoPane())
            FragmentController.setSubtitle(null);

        // Assign UI Views
        mTextArtist = (TextView) mPlayerView.findViewById(R.id.player_text_artist);
        mTextAlbum = (TextView) mPlayerView.findViewById(R.id.player_text_album);
        mTextTrack = (TextView) mPlayerView.findViewById(R.id.player_text_track);
        mTextSeekPosition = (TextView) mPlayerView.findViewById(R.id.player_text_position_current);
        mTextSeekDuration = (TextView) mPlayerView.findViewById(R.id.player_text_position_max);
        mButtonLast = (ImageButton) mPlayerView.findViewById(R.id.player_button_last);
        mButtonNext = (ImageButton) mPlayerView.findViewById(R.id.player_button_next);
        mImageView = (ImageView) mPlayerView.findViewById(R.id.player_image);
        mTogglePlay = (ToggleButton) mPlayerView.findViewById(R.id.player_togglebutton_play);
        mSeekBar = (SeekBar) mPlayerView.findViewById(R.id.player_seekBar);

        // Animating Seek Bar
        getActivity().runOnUiThread(new Runnable() {

            final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss");

            Handler mHandler = new Handler();

            @Override
            public void run() {

                mSeekBar.setProgress(PreviewController.getTrackPosition() / 1000);
                mTextSeekPosition.setText(simpleDateFormat.format(PreviewController.getTrackPosition()));
                mHandler.postDelayed(this, 1000);

            }

        });

        // Set Next
        mButtonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PreviewController.nextTrack();
            }
        });

        // Set Last
        mButtonLast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PreviewController.previousTrack();
            }
        });

        // Setup Play/Pause Button
        mTogglePlay.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    PreviewController.play();
                else
                    PreviewController.pause();
            }
        });

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    if (PreviewController.isPlaying())
                        PreviewController.setTrackPosition((int) TimeUnit.SECONDS.toMillis(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }

    public void updateUI() {

        mTogglePlay.setChecked(PreviewController.isPlaying());

        // Set Artist, Album, Track names
        mTextArtist.setText(PreviewController.getArtistName());
        mTextAlbum.setText(PreviewController.getAlbumName());
        mTextTrack.setText(PreviewController.getTrackName());
        mImageView.setImageBitmap(PreviewController.getAlbumArt());

        mSeekBar.setProgress(0);
        mTextSeekPosition.setText("00:00");

        mSeekBar.setMax(30);
        mTextSeekDuration.setText("00:30");

        // Disable next if needed
        mButtonNext.setVisibility(PreviewController.hasNext() ? View.VISIBLE : View.INVISIBLE);

        // Disable previous if needed
        mButtonLast.setVisibility(PreviewController.hasPrevious() ? View.VISIBLE : View.INVISIBLE);

    }

    @Override
    public void onCancel(DialogInterface dialog) {
        FragmentController.updateMenu();
        super.onCancel(dialog);
    }
}