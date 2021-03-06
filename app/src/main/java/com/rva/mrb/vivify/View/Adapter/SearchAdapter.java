package com.rva.mrb.vivify.View.Adapter;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.rva.mrb.vivify.Model.Data.Album;
import com.rva.mrb.vivify.Model.Data.Artist;
import com.rva.mrb.vivify.Model.Data.MediaType;
import com.rva.mrb.vivify.Model.Data.Playlist;
import com.rva.mrb.vivify.Model.Data.Search;
import com.rva.mrb.vivify.Model.Data.SimpleTrack;
import com.rva.mrb.vivify.Model.Data.Track;
import com.rva.mrb.vivify.R;
import com.rva.mrb.vivify.View.Search.SearchInterface;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {
    private static final String TAG = SearchAdapter.class.getSimpleName();

//    private List<Object> items; // List of Track or Album objects
    private List<MediaType> mediaTypeList;
    private SearchInterface searchInterface; // Listens for selected media

    private Search results;
    private Playlist playlists;
    private SimpleTrack simpleTrack;

    public static final int TRACK = 0;
    public static final int ALBUM = 1;

    public SearchAdapter(List<MediaType> mediaTypeList) { this.mediaTypeList = mediaTypeList; }
//    public SearchAdapter(List<Object> item) { this.items = item; }
    public SearchAdapter(Search results) { this.results = results; }
    public SearchAdapter(Playlist playlists) {
        this.playlists = playlists;
    }
    public SearchAdapter(SimpleTrack simpleTrack) {this.simpleTrack = simpleTrack; }

    public void setSearchInterface(SearchInterface searchInterface) {
        this.searchInterface = searchInterface;
    }

    @Override
    public SearchAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        SearchAdapter.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());


        switch (viewType) {
            case MediaType.TRACK_TYPE:
                Log.d("onCreateViewHolder", "Type: " + viewType + ", Track inflated");
                View viewTrack = inflater.inflate(R.layout.track_card, parent, false);
                viewHolder = new TrackViewHolder(viewTrack);
                break;
            case MediaType.ALBUM_TYPE:
                Log.d("onCreateViewHolder", "Type: " + viewType + ", Album inflated");
                View viewAlbum = inflater.inflate(R.layout.album_card, parent, false);
                viewHolder = new AlbumViewHolder(viewAlbum);
                break;
            case MediaType.PLAYLIST_TYPE:
                Log.d("onCreateViewHolder", "Type: " + viewType + ", Album inflated");
                View viewPlaylist = inflater.inflate(R.layout.playlist_card, parent, false);
                viewHolder = new PlaylistViewHolder(viewPlaylist);
                break;
            case MediaType.ARTIST_TYPE:
                Log.d("onCreateViewHolder", "Type: " + viewType + ", Album inflated");
                View viewArtist = inflater.inflate(R.layout.artist_card, parent, false);
                viewHolder = new ArtistViewHolder(viewArtist);
                break;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(SearchAdapter.ViewHolder holder, final int position) {

        Log.d("onBindViewHolder", "Type: " + holder.getItemViewType());
        MediaType type = mediaTypeList.get(position);
//      For some reason its not calling our override getItemViewType()
        if (type != null) {
            switch (type.getMediaType()) {
                case MediaType.TRACK_TYPE:
                    final Track t = type.getTrack();
                    Log.d("BindTrack", "Holder viewtype: " + type.getMediaType());
                    TrackViewHolder viewTrackHolder = (TrackViewHolder) holder;
                    Glide.with(viewTrackHolder.itemView.getContext())
                            .load(t.getAlbum().getImages().get(0).getUrl())
                            .fitCenter()
                            .into(viewTrackHolder.trackArt);
                    viewTrackHolder.trackName.setText(t.getName()
                            + "\n" + t.getArtists().get(0).getName());
                    viewTrackHolder.cardView.setOnClickListener(view -> {
                        Log.d("Search cardView", "Successful track click");
                        searchInterface.onMediaSelected(new MediaType(t));
                    });
                    break;
                case MediaType.ALBUM_TYPE:
                    final Album a = type.getAlbum();
                    Log.d("BindAlbum", "Holder viewtyep: " + holder.getItemViewType());
                    AlbumViewHolder viewAlbumHolder;
                    viewAlbumHolder = (AlbumViewHolder) holder;
                    Glide.with(viewAlbumHolder.itemView.getContext())
                            .load(a.getImages().get(0).getUrl())
                            .fitCenter()
                            .into(viewAlbumHolder.albumArt);
                    viewAlbumHolder.albumName.setText(a.getName()+ "\n" + a.getArtists().get(0).getName());
                    viewAlbumHolder.cardView.setOnClickListener(view -> {
                        Log.d("Search cardView", "Successful album click");
                        searchInterface.onMediaSelected(new MediaType(a));
                    });
                    break;
                case MediaType.PLAYLIST_TYPE:
                    final Playlist p = type.getPlaylist();
                    Log.d("BindPlaylist", "Holder viewtype: " + type.getMediaType());
                    PlaylistViewHolder viewPlayListHolder;
                    viewPlayListHolder = (PlaylistViewHolder) holder;
                    if(p.getImages().size() > 0) {
                        Glide.with(viewPlayListHolder.itemView.getContext())
                                .load(p.getImages().get(0).getUrl())
                                .fitCenter()
                                .into(viewPlayListHolder.playlistArt);
                        viewPlayListHolder.cardView.setOnClickListener(view -> {
                            Log.d("Search cardView", "Successful click");
                            searchInterface.onMediaSelected(new MediaType(p));
                        });
                    }
                    viewPlayListHolder.playlistName.setText(p.getName());
                    break;

                case MediaType.ARTIST_TYPE:
                    final Artist artist = type.getArtist();
                    Log.d("BindArtist", "Holder viewtype: " + type.getMediaType());
                    ArtistViewHolder viewArtistHolder;
                    viewArtistHolder = (ArtistViewHolder) holder;
                    viewArtistHolder.artistName.setText(artist.getName());
                    viewArtistHolder.cardView.setOnClickListener(view -> {
                        Log.d("Search cardView", "Successful click");
                        searchInterface.onMediaSelected(new MediaType(artist));
                    });
                    break;
            }
        }
    }

    @Override
    public int getItemCount() {
        return mediaTypeList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
        }
    }

    public class AlbumViewHolder extends SearchAdapter.ViewHolder {
        @BindView(R.id.album_name) TextView albumName;
        @BindView(R.id.artist_name) TextView artistName;
        @BindView(R.id.card_search) CardView cardView;
        @BindView(R.id.album_card_art) ImageView albumArt;
        public AlbumViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public class PlaylistViewHolder extends SearchAdapter.ViewHolder {
        @BindView(R.id.playlist_name) TextView playlistName;
        @BindView(R.id.playlist_owner) TextView playlist_owner;
        @BindView(R.id.card_search) CardView cardView;
        @BindView(R.id.playlist_card_art) ImageView playlistArt;
        public PlaylistViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public class TrackViewHolder extends SearchAdapter.ViewHolder {
        @BindView(R.id.track_name) TextView trackName;
        @BindView(R.id.track_artist_name) TextView artistName;
        @BindView(R.id.card_search) CardView cardView;
        @BindView(R.id.track_card_art) ImageView trackArt;
        public TrackViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public class ArtistViewHolder extends SearchAdapter.ViewHolder {
        @BindView(R.id.artist_name) TextView artistName;
        @BindView(R.id.card_search) CardView cardView;

        public ArtistViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (mediaTypeList != null) {
            MediaType mediaType = mediaTypeList.get(position);
            if (mediaType != null)
                return mediaType.getMediaType();
        }
        return 0;
    }
}