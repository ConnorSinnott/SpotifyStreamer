package com.pluvio.spectre.spotifystreamerv3.navigation;

import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.pluvio.spectre.spotifystreamerv3.R;
import com.pluvio.spectre.spotifystreamerv3.activities.FragmentController;
import com.pluvio.spectre.spotifystreamerv3.navigation.adapters.SongListAdapter;
import com.pluvio.spectre.spotifystreamerv3.navigation.listItems.TrackListItem;
import com.pluvio.spectre.spotifystreamerv3.other.Debugger;
import com.pluvio.spectre.spotifystreamerv3.previewer.PreviewController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyError;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import retrofit.RetrofitError;

/**
 * Created by Spectre on 8/15/2015.
 */
public class Top10TracksFragment extends Fragment {

    View mRootView;

    private static ArrayList<TrackListItem> mTopList;
    private SongListAdapter mSongListAdapter;
    private String artistSubtitle = null;

    public final static String EXTRA_ID = "extra_position";
    public final static String EXTRA_ARTIST = "extra_artist";
    public final static String INSTANCE_STATE_TRACK = "saved_track_list";

    public Top10TracksFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            artistSubtitle = savedInstanceState.getString(EXTRA_ARTIST);
            mTopList = (ArrayList<TrackListItem>) savedInstanceState.getSerializable(INSTANCE_STATE_TRACK);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_top10_tracks, container, false);

        init();

        if (getArguments().size() == 0)
            updateList(mTopList);
        else {
            artistSubtitle = getArguments().getString(EXTRA_ARTIST);
            new SpotifyTop10Retriever().execute(getArguments().getString(EXTRA_ID));
            getArguments().clear();
        }

        FragmentController.setSubtitle(artistSubtitle);

        return mRootView;

    }

    private void updateList(ArrayList<TrackListItem> trackList) {
        mTopList = trackList;
        mSongListAdapter.addAll(trackList);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(INSTANCE_STATE_TRACK, mTopList);
        outState.putString(EXTRA_ARTIST, artistSubtitle);
        super.onSaveInstanceState(outState);
    }

    private void init() {

        // Hide keyboard if open
        InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(mRootView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

        // Setting listView adapter to SongListAdapter
        ListView listView = (ListView) mRootView.findViewById(R.id.activity_top_10_list);

        mSongListAdapter = new SongListAdapter(getActivity());

        // Set listView adapter to populated SongListAdapter
        mSongListAdapter = new SongListAdapter(getActivity());
        listView.setAdapter(mSongListAdapter);

        // Adding an onItemClickListener to the listView
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // Sending selected track over the the PreviewController
                PreviewController.sendSelected(mTopList, position);

            }
        });

    }

    private class SpotifyTop10Retriever extends AsyncTask<String, Void, List<Track>> {

        @Override
        protected List<Track> doInBackground(String... params) {

            // Get spotify service
            SpotifyService spotifyService = new SpotifyApi().getService();

            // Creating a country query required for getArtistTop to work
            Map<String, Object> queryMap = new HashMap<>();
            queryMap.put("country", PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(getString(R.string.pref_country_key), "US"));

            // Getting top tracks using ArtistId and Country
            try {
                return spotifyService.getArtistTopTrack(params[0], queryMap).tracks;
            } catch (RetrofitError e) {
                Debugger.print(getClass().getSimpleName(), SpotifyError.fromRetrofitError(e).getMessage());
                return null;
            }

        }

        @Override
        protected void onPostExecute(List<Track> tracks) {

            // Internet Problem
            if (tracks == null) {
                Toast.makeText(mRootView.getContext(), getString(R.string.search_failed_no_internet), Toast.LENGTH_SHORT).show();
                return;
            }

            // No results found
            if (tracks.size() == 0) {
                Toast.makeText(mRootView.getContext(), getString(R.string.activity_top10_no_results), Toast.LENGTH_SHORT).show();
                return;
            }

            ArrayList<TrackListItem> trackList = new ArrayList<>();

            for (Track currentTrack : tracks) {

                TrackListItem newListItem = new TrackListItem();

                newListItem.setName(currentTrack.name);
                newListItem.setAlbum(currentTrack.album.name);
                newListItem.setArtist(currentTrack.artists.get(0).name);
                newListItem.setPreview_url(currentTrack.preview_url);
                newListItem.setID(currentTrack.id);

                ArrayList<String> newImages = new ArrayList<>();
                for (kaaes.spotify.webapi.android.models.Image u : currentTrack.album.images)
                    newImages.add(u.url);

                newListItem.setImages(newImages);

                trackList.add(newListItem);

            }

            // Else populate list
            updateList(trackList);

        }

    }

}
