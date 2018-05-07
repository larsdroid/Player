package org.willemsens.player.view.main.music.albums;

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
import org.willemsens.player.model.Image;
import org.willemsens.player.persistence.AppDatabase;
import org.willemsens.player.persistence.MusicDao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.willemsens.player.musiclibrary.MusicLibraryBroadcastPayloadType.MLBPT_ALBUM_ID;
import static org.willemsens.player.musiclibrary.MusicLibraryBroadcastPayloadType.MLBPT_ARTIST_ID;
import static org.willemsens.player.musiclibrary.MusicLibraryBroadcastPayloadType.MLBPT_ARTIST_IDS;
import static org.willemsens.player.musiclibrary.MusicLibraryBroadcastType.MLBT_ALBUMS_DELETED;
import static org.willemsens.player.musiclibrary.MusicLibraryBroadcastType.MLBT_ALBUMS_INSERTED;
import static org.willemsens.player.musiclibrary.MusicLibraryBroadcastType.MLBT_ALBUM_INSERTED;
import static org.willemsens.player.musiclibrary.MusicLibraryBroadcastType.MLBT_ALBUM_UPDATED;
import static org.willemsens.player.musiclibrary.MusicLibraryBroadcastType.MLBT_ARTISTS_DELETED;
import static org.willemsens.player.musiclibrary.MusicLibraryBroadcastType.MLBT_ARTISTS_INSERTED;
import static org.willemsens.player.musiclibrary.MusicLibraryBroadcastType.MLBT_ARTIST_INSERTED;

/**
 * {@link RecyclerView.Adapter} that can display an {@link Album}.
 */
public class AlbumRecyclerViewAdapter extends RecyclerView.Adapter<AlbumRecyclerViewAdapter.AlbumViewHolder> implements Filterable {
    private final Context context;
    private final List<Album> albums;
    private final List<Album> allAlbums;
    private final OnAlbumClickedListener listener;
    private final MusicDao musicDao;
    private final DBUpdateReceiver dbUpdateReceiver;
    private final AlbumFilter filter;

    AlbumRecyclerViewAdapter(Context context, Bundle savedInstanceState) {
        this.context = context;
        this.musicDao = AppDatabase.getAppDatabase(context).musicDao();
        this.listener = (OnAlbumClickedListener) context;
        this.dbUpdateReceiver = new DBUpdateReceiver();
        this.allAlbums = new ArrayList<>();
        this.albums = new ArrayList<>();
        this.filter = new AlbumFilter();
        this.filter.initialiseFilter(savedInstanceState);
        loadAlbumsFromDb();
    }

    private void loadAlbumsFromDb() {
        allAlbums.clear();
        allAlbums.addAll(musicDao.getAllAlbums());

        getFilter().filter(null);
    }

    @Override
    public AlbumViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_album_list_item, parent, false);
        return new AlbumViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final AlbumViewHolder holder, int position) {
        holder.setAlbum(albums.get(position));
    }

    @Override
    public int getItemCount() {
        return albums.size();
    }

    void onSaveInstanceState(Bundle outState) {
        filter.onSaveInstanceState(outState);
    }

    void registerDbUpdateReceiver() {
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
        IntentFilter filter = new IntentFilter();
        filter.addAction(MLBT_ALBUMS_INSERTED.name());
        filter.addAction(MLBT_ALBUM_INSERTED.name());
        filter.addAction(MLBT_ALBUM_UPDATED.name());
        filter.addAction(MLBT_ALBUMS_DELETED.name());
        filter.addAction(MLBT_ARTISTS_INSERTED.name());
        filter.addAction(MLBT_ARTIST_INSERTED.name());
        filter.addAction(MLBT_ARTISTS_DELETED.name());
        lbm.registerReceiver(this.dbUpdateReceiver, filter);
    }

    void unregisterDbUpdateReceiver() {
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
        lbm.unregisterReceiver(this.dbUpdateReceiver);
    }

    class AlbumViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.album_list_name)
        TextView albumName;

        @BindView(R.id.album_list_artist)
        TextView albumArtist;

        @BindView(R.id.album_list_year)
        TextView albumYear;

        @BindView(R.id.album_list_image)
        ImageView albumCover;

        @BindView(R.id.album_list_progress_bar)
        ProgressBar progressBar;

        private Album album;

        AlbumViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            listener.albumClicked(this.album);
        }

        private void setAlbum(Album album) {
            this.album = album;

            this.albumName.setText(album.name);
            this.albumYear.setText(album.yearReleased == null ? "" : String.valueOf(album.yearReleased));
            this.albumArtist.setText(musicDao.findArtist(album.artistId).name);

            if (album.imageId != null) {
                final Image albumCover = musicDao.findImage(album.imageId);
                final Bitmap bitmap = BitmapFactory.decodeByteArray(
                        albumCover.imageData, 0, albumCover.imageData.length);
                this.albumCover.setImageBitmap(bitmap);

                this.albumCover.setVisibility(View.VISIBLE);
                this.progressBar.setVisibility(View.GONE);
            } else {
                this.albumCover.setImageDrawable(null);

                this.albumCover.setVisibility(View.GONE);
                this.progressBar.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public Filter getFilter() {
        return this.filter;
    }

    public class AlbumFilter extends Filter {
        private final Map<Long, Boolean> artists; // Artist ID --> Include in filter

        AlbumFilter() {
            this.artists = new HashMap<>();
            fetchAllArtists();
        }

        private void clearAllArtists() {
            this.artists.clear();
        }

        private void fetchAllArtists() {
            this.artists.clear();
            for (Artist artist : musicDao.getAllArtists()) {
                this.artists.put(artist.id, true);
            }
        }

        private void setAllArtists(boolean b) {
            for (Long key : this.artists.keySet()) {
                this.artists.put(key, b);
            }
        }

        boolean hasAllArtists() {
            for (Boolean include : this.artists.values()) {
                if (!include) {
                    return false;
                }
            }
            return true;
        }

        void addAllArtists() {
            setAllArtists(true);
        }

        public void removeAllArtists() {
            setAllArtists(false);
        }

        public void add(Artist artist) {
            this.artists.put(artist.id, true);
        }

        void flipArtist(long artistId) {
            this.artists.put(artistId, !this.artists.get(artistId));
        }

        Iterator<Map.Entry<Long, Boolean>> getArtistIterator() {
            return this.artists.entrySet().iterator();
        }

        private void onSaveInstanceState(Bundle outState) {
            List<Long> filterArtists = new ArrayList<>();
            for (Long artistId : this.artists.keySet()) {
                if (this.artists.get(artistId)) {
                    filterArtists.add(artistId);
                }
            }
            long[] filterArtistIds = new long[filterArtists.size()];
            int i = 0;
            for (Long artistId : filterArtists) {
                filterArtistIds[i++] = artistId;
            }
            outState.putLongArray(MLBPT_ARTIST_IDS.name(), filterArtistIds);
        }

        private void initialiseFilter(Bundle savedInstanceState) {
            if (savedInstanceState != null) {
                long[] filterArtistIds = savedInstanceState.getLongArray(MLBPT_ARTIST_IDS.name());
                if (filterArtistIds != null) {
                    for (long filterArtistId : filterArtistIds) {
                        this.artists.put(filterArtistId, true);
                    }
                }
            }
        }

        @Override
        public FilterResults performFiltering(CharSequence charSequence) {
            FilterResults results = new FilterResults();
            final List<Album> newList = new LinkedList<>(allAlbums);
            for (Iterator<Album> i = newList.iterator(); i.hasNext();) {
                final Album album = i.next();
                if (!this.artists.get(album.artistId)) {
                    i.remove();
                }
            }
            results.values = newList;
            results.count = newList.size();
            return results;
        }

        @Override
        public void publishResults(CharSequence charSequence, FilterResults filterResults) {
            if (filterResults.values != null) {
                albums.clear();
                albums.addAll((List<Album>) filterResults.values);
                notifyDataSetChanged();
            }
        }
    }

    private class DBUpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String intentAction = intent.getAction();
            if (intentAction.equals(MLBT_ALBUMS_INSERTED.name())) {
                loadAlbumsFromDb();
            } else if (intentAction.equals(MLBT_ALBUM_INSERTED.name())) {
                loadAlbumsFromDb();

                /*final long albumId = intent.getLongExtra(MLBPT_ALBUM_ID.name(), -1);
                final Album album = musicDao.findAlbum(albumId);
                allAlbums.add(album);
                // SORT: this can't easily be done...
                getFilter().filter(null);*/
            } else if (intentAction.equals(MLBT_ALBUM_UPDATED.name())) {
                final long albumId = intent.getLongExtra(MLBPT_ALBUM_ID.name(), -1);
                final Album album = musicDao.findAlbum(albumId);
                allAlbums.set(allAlbums.indexOf(album), album);
                final int index = albums.indexOf(album);
                if (index != -1) {
                    albums.set(index, album);
                    notifyItemChanged(index);
                }
            } else if (intentAction.equals(MLBT_ALBUMS_DELETED.name())) {
                allAlbums.clear();
                getFilter().filter(null);
            } else if (intentAction.equals(MLBT_ARTISTS_INSERTED.name())) {
                ((AlbumFilter)getFilter()).fetchAllArtists();
            } else if (intentAction.equals(MLBT_ARTIST_INSERTED.name())) {
                final long artistId = intent.getLongExtra(MLBPT_ARTIST_ID.name(), -1);
                final Artist artist = musicDao.findArtist(artistId);
                ((AlbumFilter)getFilter()).add(artist);
            } else if (intentAction.equals(MLBT_ARTISTS_DELETED.name())) {
                ((AlbumFilter)getFilter()).clearAllArtists();
            }
        }
    }
}
