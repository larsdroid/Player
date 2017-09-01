package org.willemsens.player.view.main.music.songs;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import org.willemsens.player.R;
import org.willemsens.player.model.Album;
import org.willemsens.player.model.Artist;
import org.willemsens.player.model.Song;
import org.willemsens.player.view.DataAccessProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Song}.
 */
public class SongRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable {
    private final Context context;
    private final List<Song> songs;
    private final List<Song> allSongs;
    private final OnSongClickedListener listener;
    private final DataAccessProvider dataAccessProvider;
    private final DBUpdateReceiver dbUpdateReceiver;
    private final SongFilter filter;

    SongRecyclerViewAdapter(Context context, DataAccessProvider dataAccessProvider, Bundle savedInstanceState) {
        this.context = context;
        this.dataAccessProvider = dataAccessProvider;
        this.listener = (OnSongClickedListener) context;
        this.dbUpdateReceiver = new DBUpdateReceiver();
        this.allSongs = new ArrayList<>();
        this.songs = new ArrayList<>();
        this.filter = new SongFilter();
        loadSavedInstanceState(savedInstanceState);
        loadSongsFromDb();
    }

    private void loadSavedInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            long[] filterAlbumIds = savedInstanceState.getLongArray(context.getString(R.string.key_album_ids));
            if (filterAlbumIds != null) {
                for (long filterAlbumId : filterAlbumIds) {
                    this.filter.add(this.dataAccessProvider.getMusicDao().findAlbum(filterAlbumId));
                }
            }
        }
    }

    private void loadSongsFromDb() {
        allSongs.clear();
        allSongs.addAll(dataAccessProvider.getMusicDao().getAllSongs());
        Collections.sort(allSongs);

        getFilter().filter(null);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_song, parent, false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        final Song song = songs.get(position);

        final boolean showAlbumCover =
                position == 0
                        || (position > 0 && !songs.get(position - 1).getAlbum().equals(song.getAlbum()));

        final boolean showLongLine =
                (songs.size() > position + 1) && !songs.get(position + 1).getAlbum().equals(song.getAlbum());

        ((SongViewHolder)holder).setSong(song, showAlbumCover, showLongLine);
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    void onSaveInstanceState(Bundle outState) {
        List<Album> filterAlbums = ((SongFilter)getFilter()).getAlbums();
        long[] filterAlbumIds = new long[filterAlbums.size()];
        int i = 0;
        for (Album album : filterAlbums) {
            filterAlbumIds[i++] = album.getId();
        }
        outState.putLongArray(context.getString(R.string.key_album_ids), filterAlbumIds);
    }

    void registerDbUpdateReceiver() {
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
        IntentFilter filter = new IntentFilter();
        filter.addAction(context.getString(R.string.key_songs_inserted));
        lbm.registerReceiver(this.dbUpdateReceiver, filter);
    }

    void unregisterDbUpdateReceiver() {
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
        lbm.unregisterReceiver(this.dbUpdateReceiver);
    }

    class SongViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.song_list_album_image)
        ImageView albumCover;

        @BindView(R.id.song_list_name)
        TextView songName;

        @BindView(R.id.song_list_track)
        TextView songTrack;

        @BindView(R.id.song_list_album)
        TextView songAlbumName;

        @BindView(R.id.left_section_of_divider)
        View leftSectionOfDivider;

        private Song song;

        SongViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            listener.songClicked(this.song);
        }

        private void setSong(Song song, boolean showAlbumCover, boolean showLongLine) {
            this.song = song;

            if (showAlbumCover && song.getAlbum().getImage() != null) {
                final Bitmap bitmap = BitmapFactory.decodeByteArray(
                        song.getAlbum().getImage().getImageData(), 0, song.getAlbum().getImage().getImageData().length);
                this.albumCover.setImageBitmap(bitmap);
            } else {
                this.albumCover.setImageDrawable(null);
            }

            this.songName.setText(song.getName());
            this.songTrack.setText(String.valueOf(song.getTrack()));
            this.songAlbumName.setText(song.getArtist().getName() + " - " + song.getAlbum().getName());

            if (!showLongLine) {
                this.leftSectionOfDivider.setVisibility(View.INVISIBLE);
            } else {
                this.leftSectionOfDivider.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public Filter getFilter() {
        return this.filter;
    }

    public class SongFilter extends Filter {
        private final List<Album> albums;
        private final List<Artist> artists;

        SongFilter() {
            this.albums = new ArrayList<>();
            this.artists = new ArrayList<>();
        }

        /**
         * Clears this filter. All songs will be shown.
         */
        public void clear() {
            this.albums.clear();
            this.artists.clear();
        }

        public void add(Album album) {
            this.albums.add(album);
        }

        void remove(Album album) {
            this.albums.remove(album);
        }

        public void add(Artist artist) {
            this.artists.add(artist);
        }

        void remove(Artist artist) {
            this.artists.remove(artist);
        }

        public List<Album> getAlbums() {
            return this.albums;
        }

        public List<Artist> getArtists() {
            return this.artists;
        }

        public void addAllAlbums(List<Album> albums) {
            this.albums.addAll(albums);
        }

        public void addAllArtists(List<Artist> artists) {
            this.artists.addAll(artists);
        }

        @Override
        public FilterResults performFiltering(CharSequence charSequence) {
            FilterResults results = new FilterResults();
            final List<Song> newList = new LinkedList<>(allSongs);
            if (!this.albums.isEmpty()) {
                for (Iterator<Song> i = newList.iterator(); i.hasNext();) {
                    final Song song = i.next();
                    if (!this.albums.contains(song.getAlbum())) {
                        i.remove();
                    }
                }
            }
            if (!this.artists.isEmpty()) {
                for (Iterator<Song> i = newList.iterator(); i.hasNext();) {
                    final Song song = i.next();
                    if (!this.artists.contains(song.getArtist())) {
                        i.remove();
                    }
                }
            }
            results.values = newList;
            results.count = newList.size();
            return results;
        }

        @Override
        public void publishResults(CharSequence charSequence, FilterResults filterResults) {
            songs.clear();
            songs.addAll((List<Song>)filterResults.values);
            notifyDataSetChanged();
        }
    }

    private class DBUpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String intentAction = intent.getAction();
            if (intentAction.equals(context.getString(R.string.key_songs_inserted))) {
                loadSongsFromDb();
            } else if (intentAction.equals(context.getString(R.string.key_song_inserted))) {
                final long songId = intent.getLongExtra(context.getString(R.string.key_song_id), -1);
                final Song song = dataAccessProvider.getMusicDao().findSong(songId);
                allSongs.add(song);
                Collections.sort(allSongs);
                getFilter().filter(null);
            }
        }
    }
}
