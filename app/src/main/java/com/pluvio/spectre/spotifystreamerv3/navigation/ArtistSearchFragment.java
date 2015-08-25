package com.pluvio.spectre.spotifystreamerv3.navigation;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.pluvio.spectre.spotifystreamerv3.R;
import com.pluvio.spectre.spotifystreamerv3.activities.FragmentController;
import com.pluvio.spectre.spotifystreamerv3.navigation.adapters.ArtistListAdapter;
import com.pluvio.spectre.spotifystreamerv3.navigation.listItems.ArtistListItem;
import com.pluvio.spectre.spotifystreamerv3.other.Debugger;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyError;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import retrofit.RetrofitError;

/**
 * Created by Spectre on 8/15/2015.
 */
public class ArtistSearchFragment extends Fragment {

    private View mRootView;

    private ListView mArtistListView;
    private ArtistListAdapter mArtistListAdapter;
    private ArrayList<ArtistListItem> mArtistList = null;
    private EditText mSearchEditText;

    public final static String INSTANCE_STATE_ARTIST = "saved_artist_list";

    @Override
    public void onCreate(Bundle savedInstanceState) {

        if (savedInstanceState != null)
            mArtistList = (ArrayList<ArtistListItem>) savedInstanceState.getSerializable(INSTANCE_STATE_ARTIST);

        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_artist_search, container, false);

        init();

        if (mArtistList != null)
            updateList(mArtistList);

//      Check if there is a saved search in preferences
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if (sharedPreferences.contains(getString(R.string.pref_lastSearch))) {

            String lastSearch = sharedPreferences.getString(getString(R.string.pref_lastSearch), "");

            // If so, restore search
            if (lastSearch.length() > 0) {
                mSearchEditText.setText(lastSearch);
                if (mArtistList == null)
                    searchSpotify(lastSearch);
            }

        }

        return mRootView;

    }

    private void init() {
        FragmentController.setSubtitle(null);

        // Set up listView with ArtistAdapter
        mArtistListView = (ListView) mRootView.findViewById(R.id.activity_main_artist_list_view);
        mArtistListAdapter = new ArtistListAdapter(getActivity());
        mArtistListView.setAdapter(mArtistListAdapter);

        // This is what happens when a user selects an artist from the list
        mArtistListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                ArtistListItem selected = (ArtistListItem) parent.getItemAtPosition(position);
                FragmentController.showTop10TracksFragment(selected);

            }
        });

        // Set Up Search
        mSearchEditText = (EditText) mRootView.findViewById(R.id.activity_main_edit_text_search);
        mSearchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                // Saving last search
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                sharedPreferences.edit().putString(getString(R.string.pref_lastSearch), mSearchEditText.getText().toString()).apply();

                if (actionId == EditorInfo.IME_ACTION_SEARCH)
                    searchSpotify(mSearchEditText.getText().toString());

                return true;

            }
        });

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        outState.putSerializable(INSTANCE_STATE_ARTIST, mArtistList);
        super.onSaveInstanceState(outState);

    }

    private void searchSpotify(String searchTerm) {
        if (searchTerm.length() == 0)
            return;

        new SpotifyArtistListUpdater().execute(searchTerm);
    }

    public void updateList(ArrayList<ArtistListItem> artists) {
        // Clear, populate, and notify listAdapter.
        mArtistList = artists;
        mArtistListAdapter.clear();
        mArtistListAdapter.addAll(artists);
        mArtistListAdapter.notifyDataSetChanged();
    }

    /**
     * This AsyncTask uses the Spotify Api to retrieve a List of Artists.
     * OnPostExecute calls updateList(), passing the list of found Artists.
     */
    private class SpotifyArtistListUpdater extends AsyncTask<String, Void, List<Artist>> {

        @Override
        protected List<Artist> doInBackground(String... params) {

            // Get Spotify Service
            SpotifyService spotifyService = new SpotifyApi().getService();

            // Return Artist Search Results
            try {
                ArtistsPager artistsPager = spotifyService.searchArtists(params[0]);
                return artistsPager.artists.items;
            } catch (RetrofitError e) {
                Debugger.print(getClass().getSimpleName(), SpotifyError.fromRetrofitError(e).getMessage());
                return null;
            }

        }

        @Override
        protected void onPostExecute(List<Artist> artists) {

            // Internet Problem
            if (artists == null) {
                Toast.makeText(mRootView.getContext(), getString(R.string.search_failed_no_internet), Toast.LENGTH_SHORT).show();
                return;
            }

            // No results found
            if (artists.size() == 0) {
                Toast.makeText(mRootView.getContext(), getString(R.string.activity_main_search_failed), Toast.LENGTH_SHORT).show();
                return;
            }

            // Clear the Top10Fragment if it already exists
            if (FragmentController.isTwoPane())
                FragmentController.clearTop10Fragment();

            // Save List

            ArrayList<ArtistListItem> artistList = new ArrayList<>();

            for (Artist currentArtist : artists) {

                ArtistListItem newListItem = new ArtistListItem();

                newListItem.setName(currentArtist.name);
                newListItem.setId(currentArtist.id);

                ArrayList<String> newImages = new ArrayList<>();
                for (kaaes.spotify.webapi.android.models.Image u : currentArtist.images)
                    newImages.add(u.url);

                newListItem.setImages(newImages);

                artistList.add(newListItem);

            }

            updateList(artistList);

        }

    }

}
