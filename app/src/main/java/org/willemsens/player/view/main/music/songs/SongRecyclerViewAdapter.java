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
import org.willemsens.player.view.DataAccessProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Song}.
 */
public class SongRecyclerViewAdapter extends RecyclerView.Adapter<SongRecyclerViewAdapter.SongViewHolder> implements Filterable {
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
                .inflate(R.layout.fragment_song, parent, false);
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
        filter.addAction(context.getString(R.string.key_songs_inserted));
        filter.addAction(context.getString(R.string.key_song_inserted));
        filter.addAction(context.getString(R.string.key_artists_inserted));
        filter.addAction(context.getString(R.string.key_artist_inserted));
        filter.addAction(context.getString(R.string.key_albums_inserted));
        filter.addAction(context.getString(R.string.key_album_inserted));
        filter.addAction(context.getString(R.string.key_album_updated));
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
            this.songLength.setText(String.format("%d:%02d", song.getLength() / 60, song.getLength() % 60));

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
            outState.putLongArray(context.getString(R.string.key_album_ids), filterAlbumIds);

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
            outState.putLongArray(context.getString(R.string.key_artist_ids), filterArtistIds);
        }

        private void initialiseFilter(Bundle savedInstanceState) {
            if (savedInstanceState != null) {
                long[] filterAlbumIds = savedInstanceState.getLongArray(context.getString(R.string.key_album_ids));
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

                long[] filterArtistIds = savedInstanceState.getLongArray(context.getString(R.string.key_artist_ids));
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
            if (intentAction.equals(context.getString(R.string.key_songs_inserted))) {
                loadSongsFromDb();
            } else if (intentAction.equals(context.getString(R.string.key_song_inserted))) {
                final long songId = intent.getLongExtra(context.getString(R.string.key_song_id), -1);
                final Song song = dataAccessProvider.getMusicDao().findSong(songId);
                allSongs.add(song);
                Collections.sort(allSongs);
                getFilter().filter(null);
            } else if (intentAction.equals(context.getString(R.string.key_artists_inserted))) {
                ((SongFilter)getFilter()).fetchAllArtists();
            } else if (intentAction.equals(context.getString(R.string.key_artist_inserted))) {
                final long artistId = intent.getLongExtra(context.getString(R.string.key_artist_id), -1);
                final Artist artist = dataAccessProvider.getMusicDao().findArtist(artistId);
                ((SongFilter)getFilter()).add(artist);
            } else if (intentAction.equals(context.getString(R.string.key_albums_inserted))) {
                ((SongFilter)getFilter()).fetchAllAlbums();
            } else if (intentAction.equals(context.getString(R.string.key_album_inserted))) {
                final long albumId = intent.getLongExtra(context.getString(R.string.key_album_id), -1);
                final Album album = dataAccessProvider.getMusicDao().findAlbum(albumId);
                ((SongFilter)getFilter()).add(album);
            } else if (intentAction.equals(context.getString(R.string.key_album_updated))) {
                final long albumId = intent.getLongExtra(context.getString(R.string.key_album_id), -1);
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
