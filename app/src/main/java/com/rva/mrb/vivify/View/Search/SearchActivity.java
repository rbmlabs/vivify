package com.rva.mrb.vivify.View.Search;

import android.app.Activity;
import android.app.LauncherActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcel;
import android.support.v4.app.NavUtils;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.rva.mrb.vivify.AlarmApplication;
import com.rva.mrb.vivify.ApplicationModule;
import com.rva.mrb.vivify.BaseActivity;
import com.rva.mrb.vivify.Model.Data.AccessToken;
import com.rva.mrb.vivify.Model.Data.Album;
import com.rva.mrb.vivify.Model.Data.Artist;
import com.rva.mrb.vivify.Model.Data.MediaType;
import com.rva.mrb.vivify.Model.Data.Playlist;
import com.rva.mrb.vivify.Model.Data.PlaylistPager;
import com.rva.mrb.vivify.Model.Data.Search;
import com.rva.mrb.vivify.Model.Data.SimpleTrack;
import com.rva.mrb.vivify.Model.Data.Track;
import com.rva.mrb.vivify.Model.Data.User;
import com.rva.mrb.vivify.Spotify.NodeService;
import com.rva.mrb.vivify.Spotify.SpotifyService;
import com.rva.mrb.vivify.R;
import com.rva.mrb.vivify.View.Adapter.SearchAdapter;
import com.rva.mrb.vivify.View.Adapter.SimpleSectionedRecyclerViewAdapter;
import com.rva.mrb.vivify.View.Settings.SettingsActivity;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Player;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * This class allows user to search spotify
 */
public class SearchActivity extends BaseActivity implements SearchView,
        SearchInterface {

    @Inject SearchPresenter searchPresenter;
    @Inject NodeService nodeService;
    @Inject SpotifyService spotifyService;
    @BindView(R.id.search_recyclerview) RecyclerView recyclerview;
    @BindView(R.id.search_edittext) TextView searchEditText;
    @BindView(R.id.toolbar) Toolbar toolbar;
    private SearchAdapter searchAdapter;
    private SearchModule searchModule = new SearchModule(this);
    private ApplicationModule applicationModule = new ApplicationModule((AlarmApplication) getApplication());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        //Inject dagger and butterknife dependencies
        SearchComponent searchComponent = DaggerSearchComponent.builder()
                .applicationModule(applicationModule)
                .searchModule(searchModule)
                .applicationComponent(((AlarmApplication) getApplication()).getComponent())
                .build();
        searchComponent.inject(this);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Inititialize view and retrieve a fresh access token
        initView();
        refreshToken();

        searchEditText.setOnEditorActionListener((textView, i, keyEvent) -> {
            boolean handled = false;
            if (i == EditorInfo.IME_ACTION_SEARCH) {
                onSearchClick();
                handled = true;
            }
            return handled;
        });

    }

    /**
     * This method initializes the view
     */
    private void initView() {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerview.setLayoutManager(layoutManager);
        recyclerview.setHasFixedSize(true);
    }

    public void setUserPlaylists() {
        spotifyService.getMyPlaylists()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    PlaylistPager myPlaylists = response;

                    List<MediaType> mediaTypeList = searchPresenter.setupMediaList(myPlaylists);
                    searchAdapter = new SearchAdapter(mediaTypeList);
                    List<SimpleSectionedRecyclerViewAdapter.Section> sections =
                            new ArrayList<>();
                    sections.add(new SimpleSectionedRecyclerViewAdapter.Section(0,"My Playlists"));
                    setInterface();
                    SimpleSectionedRecyclerViewAdapter.Section[] dummy = new SimpleSectionedRecyclerViewAdapter.Section[sections.size()];
                    SimpleSectionedRecyclerViewAdapter mSectionedAdapter = new
                            SimpleSectionedRecyclerViewAdapter(getApplicationContext(),R.layout.section,R.id.section_text,searchAdapter);
                    mSectionedAdapter.setSections(sections.toArray(dummy));
                    recyclerview.setAdapter(mSectionedAdapter);
                });
//                .enqueue(new Callback<PlaylistPager>() {
//            @Override
//            public void onResponse(Call<PlaylistPager> call, Response<PlaylistPager> response) {
//                Log.d("response", response.message());
//                PlaylistPager myPlaylists = response.body();
//
//                List<MediaType> mediaTypeList = searchPresenter.setupMediaList(myPlaylists);
//                searchAdapter = new SearchAdapter(mediaTypeList);
//                List<SimpleSectionedRecyclerViewAdapter.Section> sections =
//                        new ArrayList<>();
//                sections.add(new SimpleSectionedRecyclerViewAdapter.Section(0,"My Playlists"));
//                setInterface();
//                SimpleSectionedRecyclerViewAdapter.Section[] dummy = new SimpleSectionedRecyclerViewAdapter.Section[sections.size()];
//                SimpleSectionedRecyclerViewAdapter mSectionedAdapter = new
//                        SimpleSectionedRecyclerViewAdapter(getApplicationContext(),R.layout.section,R.id.section_text,searchAdapter);
//                mSectionedAdapter.setSections(sections.toArray(dummy));
//                recyclerview.setAdapter(mSectionedAdapter);
//            }
//
//            @Override
//            public void onFailure(Call<PlaylistPager> call, Throwable t) {
//
//            }
//        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.search_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
                super.onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }
    /**
     * This method sets the search interface to communicate with searchAdapter
     */
    public void setInterface() {
        searchAdapter.setSearchInterface(this);
    }

    /**
     * This method retrieves a new access token from the backend server.
     */
    private void refreshToken() {
        //Get refreshToken from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        String refreshToken = sharedPreferences.getString("refresh_token", null);
        Log.d("Node", "sharedpref refresh token: " + refreshToken);

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(sharedPreferences.getLong("expires", -1));
        Date expires = cal.getTime();
        Log.d("Node", "Expired: " + sharedPreferences.getLong("expires", -1));
        if(expires.before(Calendar.getInstance().getTime())) {
            Log.d("Node", "making node call");
            //Make call to node.js server to obtain a fresh access token
            nodeService.refreshToken(refreshToken).enqueue(new Callback<AccessToken>() {
                @Override
                public void onResponse(Call<AccessToken> call, Response<AccessToken> response) {
                    AccessToken results = response.body();
                    applicationModule.setAccessToken(results.getAccessToken());
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    Calendar calendar = Calendar.getInstance();
                    calendar.add(Calendar.SECOND, results.getExpiresIn());
                    Log.d("Node", "New expire time: " + calendar.getTimeInMillis());
                    editor.putLong("expires", calendar.getTimeInMillis());
                    editor.putString("access_token", results.getAccessToken());
                    editor.commit();
                    setUserPlaylists();
                }

                @Override
                public void onFailure(Call<AccessToken> call, Throwable t) {
                    Log.d("Node", "error: " + t.getMessage());
                }
            });
        }
        else{
            String accesstoken = sharedPreferences.getString("access_token", null);
            applicationModule.setAccessToken(accesstoken);
            setUserPlaylists();
        }
    }

    /**
     * Search button was clicked. This method makes a retrofit call to Spotify API with the string in
     * searchEditText to search for music.
     */
//    @OnClick(R.id.fab3)
    public void onSearchClick() {
        Log.d("MyApp", "Fab Click");
        String searchQuery = searchEditText.getText().toString();
        if (searchQuery != null && !searchQuery.isEmpty()) {
            spotifyService.getFullSearchResults(searchQuery)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(response -> {
                        Search results = response;
                        List<MediaType> mediaTypeList = searchPresenter.setupMediaList(results);//new ArrayList<MediaType>();

                        searchAdapter = new SearchAdapter(mediaTypeList);
                        List<SimpleSectionedRecyclerViewAdapter.Section> sections =
                                new ArrayList<>();

                        sections.add(new SimpleSectionedRecyclerViewAdapter.Section(0, "Tracks"));
                        sections.add(new SimpleSectionedRecyclerViewAdapter.Section(results.getTracks().getItems().size(), "Albums"));
                        sections.add(new SimpleSectionedRecyclerViewAdapter.Section(results.getAlbums().getItems().size() +
                                results.getTracks().getItems().size(), "Playlists"));
                        setInterface();

                        //Add your adapter to the sectionAdapter
                        SimpleSectionedRecyclerViewAdapter.Section[] dummy = new SimpleSectionedRecyclerViewAdapter.Section[sections.size()];
                        SimpleSectionedRecyclerViewAdapter mSectionedAdapter = new
                                SimpleSectionedRecyclerViewAdapter(getApplicationContext(), R.layout.section, R.id.section_text, searchAdapter);
                        mSectionedAdapter.setSections(sections.toArray(dummy));

                        //Apply this adapter to the RecyclerView
                        recyclerview.setAdapter(mSectionedAdapter);
                    });
        }
    }

    @Override
    protected void closeRealm() {

    }


    @Override
    public void onStart() {
        super.onStart();
        searchPresenter.setView(this);
    }

    @Override
    public void onBackPressed() {
        this.finish();
    }

    @Override
    public void onMediaSelected(MediaType mediaType) {
        Log.d("Search Activity", "At onMediaSelected");
        Intent intent = new Intent();
        intent.putExtra("track", Parcels.wrap(mediaType));
        setResult(Activity.RESULT_OK, intent);
        finish();
    }
}
