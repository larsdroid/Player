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
import org.willemsens.player.view.DataAccessProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.willemsens.player.musiclibrary.MusicLibraryBroadcastPayloadType.ALBUM_ID;
import static org.willemsens.player.musiclibrary.MusicLibraryBroadcastPayloadType.ARTIST_ID;
import static org.willemsens.player.musiclibrary.MusicLibraryBroadcastType.ALBUMS_INSERTED;
import static org.willemsens.player.musiclibrary.MusicLibraryBroadcastType.ALBUM_INSERTED;
import static org.willemsens.player.musiclibrary.MusicLibraryBroadcastType.ALBUM_UPDATED;
import static org.willemsens.player.musiclibrary.MusicLibraryBroadcastType.ARTISTS_INSERTED;
import static org.willemsens.player.musiclibrary.MusicLibraryBroadcastType.ARTIST_INSERTED;

/**
 * {@link RecyclerView.Adapter} that can display an {@link Album}.
 */
public class AlbumRecyclerViewAdapter extends RecyclerView.Adapter<AlbumRecyclerViewAdapter.AlbumViewHolder> implements Filterable {
    private final Context context;
    private final List<Album> albums;
    private final List<Album> allAlbums;
    private final OnAlbumClickedListener listener;
    private final DataAccessProvider dataAccessProvider;
    private final DBUpdateReceiver dbUpdateReceiver;
    private final AlbumFilter filter;

    AlbumRecyclerViewAdapter(Context context, DataAccessProvider dataAccessProvider, Bundle savedInstanceState) {
        this.context = context;
        this.dataAccessProvider = dataAccessProvider;
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
        allAlbums.addAll(dataAccessProvider.getMusicDao().getAllAlbums());
        Collections.sort(allAlbums);

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
        filter.addAction(ALBUMS_INSERTED.getString(context));
        filter.addAction(ALBUM_INSERTED.getString(context));
        filter.addAction(ALBUM_UPDATED.getString(context));
        filter.addAction(ARTISTS_INSERTED.getString(context));
        filter.addAction(ARTIST_INSERTED.getString(context));
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

            this.albumName.setText(album.getName());
            this.albumYear.setText(album.getYearReleased() == null ? "" : String.valueOf(album.getYearReleased()));
            this.albumArtist.setText(album.getArtist().getName());

            if (album.getImage() != null) {
                final Bitmap bitmap = BitmapFactory.decodeByteArray(
                        album.getImage().getImageData(), 0, album.getImage().getImageData().length);
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
        private final Map<Artist, Boolean> artists;

        AlbumFilter() {
            this.artists = new TreeMap<>();
            fetchAllArtists();
        }

        private void fetchAllArtists() {
            this.artists.clear();
            for (Artist artist : dataAccessProvider.getMusicDao().getAllArtists()) {
                this.artists.put(artist, true);
            }
        }

        private void setAllArtists(boolean b) {
            for (Artist key : this.artists.keySet()) {
                this.artists.put(key, b);
            }
        }

        boolean hasAllArtists() {
            for (Artist key : this.artists.keySet()) {
                if (!this.artists.get(key)) {
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
            this.artists.put(artist, true);
        }

        void flipArtist(int artistId) {
            for (Artist artist : this.artists.keySet()) {
                if (artist.getId() == artistId) {
                    this.artists.put(artist, !this.artists.get(artist));
                }
            }
        }

        Iterator<Map.Entry<Artist, Boolean>> getArtistIterator() {
            return this.artists.entrySet().iterator();
        }

        private void onSaveInstanceState(Bundle outState) {
            List<Artist> filterArtists = new ArrayList<>();
            for (Artist artist : this.artists.keySet()) {
                if (this.artists.get(artist)) {
                    filterArtists.add(artist);
                }
            }
            long[] filterArtistIds = new long[filterArtists.size()];
            int i = 0;
            for (Artist artist : filterArtists) {
                filterArtistIds[i++] = artist.getId();
            }
            outState.putLongArray(context.getString(R.string.key_artist_ids), filterArtistIds);
        }

        private void initialiseFilter(Bundle savedInstanceState) {
            if (savedInstanceState != null) {
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
            final List<Album> newList = new LinkedList<>(allAlbums);
            for (Iterator<Album> i = newList.iterator(); i.hasNext();) {
                final Album album = i.next();
                if (!this.artists.get(album.getArtist())) {
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
            if (intentAction.equals(ALBUMS_INSERTED.getString(context))) {
                loadAlbumsFromDb();
            } else if (intentAction.equals(ALBUM_INSERTED.getString(context))) {
                final long albumId = intent.getLongExtra(ALBUM_ID.getString(context), -1);
                final Album album = dataAccessProvider.getMusicDao().findAlbum(albumId);
                allAlbums.add(album);
                Collections.sort(allAlbums);
                getFilter().filter(null);
            } else if (intentAction.equals(ALBUM_UPDATED.getString(context))) {
                final long albumId = intent.getLongExtra(ALBUM_ID.getString(context), -1);
                final Album album = dataAccessProvider.getMusicDao().findAlbum(albumId);
                allAlbums.set(allAlbums.indexOf(album), album);
                final int index = albums.indexOf(album);
                if (index != -1) {
                    albums.set(index, album);
                    notifyItemChanged(index);
                }
            } else if (intentAction.equals(ARTISTS_INSERTED.getString(context))) {
                ((AlbumFilter)getFilter()).fetchAllArtists();
            } else if (intentAction.equals(ARTIST_INSERTED.getString(context))) {
                final long artistId = intent.getLongExtra(ARTIST_ID.getString(context), -1);
                final Artist artist = dataAccessProvider.getMusicDao().findArtist(artistId);
                ((AlbumFilter)getFilter()).add(artist);
            }
        }
    }
}
