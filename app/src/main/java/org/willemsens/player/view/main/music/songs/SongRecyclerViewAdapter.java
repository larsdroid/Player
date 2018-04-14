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
import android.widget.ProgressBar;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import org.willemsens.player.R;
import org.willemsens.player.model.Album;
import org.willemsens.player.model.Artist;
import org.willemsens.player.model.Song;
import org.willemsens.player.util.StringFormat;
import org.willemsens.player.view.DataAccessProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.willemsens.player.musiclibrary.MusicLibraryBroadcastPayloadType.ALBUM_ID;
import static org.willemsens.player.musiclibrary.MusicLibraryBroadcastPayloadType.ALBUM_IDS;
import static org.willemsens.player.musiclibrary.MusicLibraryBroadcastPayloadType.ARTIST_ID;
import static org.willemsens.player.musiclibrary.MusicLibraryBroadcastPayloadType.ARTIST_IDS;
import static org.willemsens.player.musiclibrary.MusicLibraryBroadcastPayloadType.SONG_ID;
import static org.willemsens.player.musiclibrary.MusicLibraryBroadcastType.ALBUMS_INSERTED;
import static org.willemsens.player.musiclibrary.MusicLibraryBroadcastType.ALBUM_INSERTED;
import static org.willemsens.player.musiclibrary.MusicLibraryBroadcastType.ALBUM_UPDATED;
import static org.willemsens.player.musiclibrary.MusicLibraryBroadcastType.ARTISTS_INSERTED;
import static org.willemsens.player.musiclibrary.MusicLibraryBroadcastType.ARTIST_INSERTED;
import static org.willemsens.player.musiclibrary.MusicLibraryBroadcastType.SONGS_INSERTED;
import static org.willemsens.player.musiclibrary.MusicLibraryBroadcastType.SONG_INSERTED;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Song}.
 */
class SongRecyclerViewAdapter extends RecyclerView.Adapter<SongRecyclerViewAdapter.SongViewHolder> implements Filterable {
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
        this.filter.initialiseFilter(savedInstanceState);
        loadSongsFromDb();
    }

    private void loadSongsFromDb() {
        allSongs.clear();
        allSongs.addAll(dataAccessProvider.getMusicDao().getAllSongs());
        Collections.sort(allSongs);

        getFilter().filter(null);
    }

    @Override
    public SongViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_song_list_item, parent, false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final SongViewHolder holder, int position) {
        final Song song = songs.get(position);

        final boolean showAlbumCover =
                position == 0
                        || (position > 0 && !songs.get(position - 1).getAlbum().equals(song.getAlbum()));

        final boolean showLongLine =
                (songs.size() > position + 1) && !songs.get(position + 1).getAlbum().equals(song.getAlbum());

        holder.setSong(song, showAlbumCover, showLongLine);
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    void onSaveInstanceState(Bundle outState) {
        filter.onSaveInstanceState(outState);
    }

    void registerDbUpdateReceiver() {
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
        IntentFilter filter = new IntentFilter();
        filter.addAction(SONGS_INSERTED.getString(context));
        filter.addAction(SONG_INSERTED.getString(context));
        filter.addAction(ARTISTS_INSERTED.getString(context));
        filter.addAction(ARTIST_INSERTED.getString(context));
        filter.addAction(ALBUMS_INSERTED.getString(context));
        filter.addAction(ALBUM_INSERTED.getString(context));
        filter.addAction(ALBUM_UPDATED.getString(context));
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

        @BindView(R.id.song_list_length)
        TextView songLength;

        @BindView(R.id.left_section_of_divider)
        View leftSectionOfDivider;

        @BindView(R.id.song_list_progress_bar)
        ProgressBar progressBar;

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

                this.albumCover.setVisibility(View.VISIBLE);
                this.progressBar.setVisibility(View.GONE);
            } else if (showAlbumCover) {
                this.albumCover.setImageDrawable(null);

                this.albumCover.setVisibility(View.GONE);
                this.progressBar.setVisibility(View.VISIBLE);
            } else {
                this.albumCover.setImageDrawable(null);

                this.albumCover.setVisibility(View.VISIBLE);
                this.progressBar.setVisibility(View.GONE);
            }

            this.songName.setText(song.getName());
            this.songTrack.setText(String.valueOf(song.getTrack()));
            this.songAlbumName.setText(song.getArtist().getName() + " - " + song.getAlbum().getName());
            this.songLength.setText(StringFormat.formatToSongLength(song.getLength()));

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

    class SongFilter extends Filter {
        private final Map<Album, Boolean> albums;
        private final Map<Artist, Boolean> artists;

        SongFilter() {
            this.albums = new TreeMap<>(new Comparator<Album>() {
                @Override
                public int compare(Album a, Album b) {
                    return a.getName().compareTo(b.getName());
                }
            });
            this.artists = new TreeMap<>();
            fetchAllAlbums();
            fetchAllArtists();
        }

        private void fetchAllAlbums() {
            this.albums.clear();
            for (Album album : dataAccessProvider.getMusicDao().getAllAlbums()) {
                this.albums.put(album, true);
            }
        }

        private void fetchAllArtists() {
            this.artists.clear();
            for (Artist artist : dataAccessProvider.getMusicDao().getAllArtists()) {
                this.artists.put(artist, true);
            }
        }

        private void setAllAlbums(boolean b) {
            for (Album key : this.albums.keySet()) {
                this.albums.put(key, b);
            }
        }

        private void setAllArtists(boolean b) {
            for (Artist key : this.artists.keySet()) {
                this.artists.put(key, b);
            }
        }

        boolean hasAllAlbums() {
            for (Album key : this.albums.keySet()) {
                if (!this.albums.get(key)) {
                    return false;
                }
            }
            return true;
        }

        boolean hasAllArtists() {
            for (Artist key : this.artists.keySet()) {
                if (!this.artists.get(key)) {
                    return false;
                }
            }
            return true;
        }

        void addAllAlbums() {
            setAllAlbums(true);
        }

        public void addAllArtists() {
            setAllArtists(true);
        }

        public void removeAllAlbums() {
            setAllAlbums(false);
        }

        void removeAllArtists() {
            setAllArtists(false);
        }

        // TODO: private (once MainActivity no longer calls this)
        public void add(Album album) {
            this.albums.put(album, true);
        }

        private void add(Artist artist) {
            this.artists.put(artist, true);
        }

        void flipAlbum(int albumId) {
            for (Album album : this.albums.keySet()) {
                if (album.getId() == albumId) {
                    this.albums.put(album, !this.albums.get(album));
                }
            }
        }

        void flipArtist(int artistId) {
            for (Artist artist : this.artists.keySet()) {
                if (artist.getId() == artistId) {
                    this.artists.put(artist, !this.artists.get(artist));
                }
            }
        }

        Iterator<Map.Entry<Album, Boolean>> getAlbumIterator() {
            return this.albums.entrySet().iterator();
        }

        Iterator<Map.Entry<Artist, Boolean>> getArtistIterator() {
            return this.artists.entrySet().iterator();
        }

        private void onSaveInstanceState(Bundle outState) {
            List<Album> filterAlbums = new ArrayList<>();
            for (Album album : this.albums.keySet()) {
                if (this.albums.get(album)) {
                    filterAlbums.add(album);
                }
            }
            long[] filterAlbumIds = new long[filterAlbums.size()];
            int i = 0;
            for (Album album : filterAlbums) {
                filterAlbumIds[i++] = album.getId();
            }
            outState.putLongArray(ALBUM_IDS.getString(context), filterAlbumIds);

            List<Artist> filterArtists = new ArrayList<>();
            for (Artist artist : this.artists.keySet()) {
                if (this.artists.get(artist)) {
                    filterArtists.add(artist);
                }
            }
            long[] filterArtistIds = new long[filterArtists.size()];
            i = 0;
            for (Artist artist : filterArtists) {
                filterArtistIds[i++] = artist.getId();
            }
            outState.putLongArray(ARTIST_IDS.getString(context), filterArtistIds);
        }

        private void initialiseFilter(Bundle savedInstanceState) {
            if (savedInstanceState != null) {
                long[] filterAlbumIds = savedInstanceState.getLongArray(ALBUM_IDS.getString(context));
                if (filterAlbumIds != null) {
                    for (Album album : this.albums.keySet()) {
                        this.albums.put(album, false);
                        for (long filterAlbumId : filterAlbumIds) {
                            if (album.getId() == filterAlbumId) {
                                this.albums.put(album, true);
                                break;
                            }
                        }
                    }
                }

                long[] filterArtistIds = savedInstanceState.getLongArray(ARTIST_IDS.getString(context));
                if (filterArtistIds != null) {
                    for (Artist artist : this.artists.keySet()) {
                        this.artists.put(artist, false);
                        for (long filterArtistId : filterArtistIds) {
                            if (artist.getId() == filterArtistId) {
                                this.artists.put(artist, true);
                                break;
                            }
                        }
                    }
                }
            }
        }

        @Override
        public FilterResults performFiltering(CharSequence charSequence) {
            FilterResults results = new FilterResults();
            final List<Song> newList = new LinkedList<>(allSongs);
            for (Iterator<Song> i = newList.iterator(); i.hasNext();) {
                final Song song = i.next();
                if (!this.albums.get(song.getAlbum())) {
                    i.remove();
                }
            }
            for (Iterator<Song> i = newList.iterator(); i.hasNext();) {
                final Song song = i.next();
                if (!this.artists.get(song.getArtist())) {
                    i.remove();
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
            if (intentAction.equals(SONGS_INSERTED.getString(context))) {
                loadSongsFromDb();
            } else if (intentAction.equals(SONG_INSERTED.getString(context))) {
                final long songId = intent.getLongExtra(SONG_ID.getString(context), -1);
                final Song song = dataAccessProvider.getMusicDao().findSong(songId);
                allSongs.add(song);
                Collections.sort(allSongs);
                getFilter().filter(null);
            } else if (intentAction.equals(ARTISTS_INSERTED.getString(context))) {
                ((SongFilter)getFilter()).fetchAllArtists();
            } else if (intentAction.equals(ARTIST_INSERTED.getString(context))) {
                final long artistId = intent.getLongExtra(ARTIST_ID.getString(context), -1);
                final Artist artist = dataAccessProvider.getMusicDao().findArtist(artistId);
                ((SongFilter)getFilter()).add(artist);
            } else if (intentAction.equals(ALBUMS_INSERTED.getString(context))) {
                ((SongFilter)getFilter()).fetchAllAlbums();
            } else if (intentAction.equals(ALBUM_INSERTED.getString(context))) {
                final long albumId = intent.getLongExtra(ALBUM_ID.getString(context), -1);
                final Album album = dataAccessProvider.getMusicDao().findAlbum(albumId);
                ((SongFilter)getFilter()).add(album);
            } else if (intentAction.equals(ALBUM_UPDATED.getString(context))) {
                final long albumId = intent.getLongExtra(ALBUM_ID.getString(context), -1);
                final Album album = dataAccessProvider.getMusicDao().findAlbum(albumId);
                for (int i = 0; i < songs.size(); i++) {
                    if (songs.get(i).getAlbum().equals(album)) {
                        notifyItemChanged(i);
                    }
                }
            }
        }
    }
}
