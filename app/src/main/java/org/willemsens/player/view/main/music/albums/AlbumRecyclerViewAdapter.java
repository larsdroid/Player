package org.willemsens.player.view.main.music.albums;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import org.willemsens.player.persistence.entities.Album;
import org.willemsens.player.persistence.entities.Artist;
import org.willemsens.player.persistence.entities.helpers.AlbumWithImageAndArtist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.willemsens.player.musiclibrary.MusicLibraryBroadcastPayloadType.MLBPT_ARTIST_IDS;

/**
 * {@link RecyclerView.Adapter} that can display an {@link Album}.
 */
public class AlbumRecyclerViewAdapter extends RecyclerView.Adapter<AlbumRecyclerViewAdapter.AlbumViewHolder> implements Filterable {
    private final OnAlbumClickedListener listener;
    private final AlbumFilter filter;
    private final List<AlbumWithImageAndArtist> albums;
    private List<AlbumWithImageAndArtist> allAlbums;

    AlbumRecyclerViewAdapter(Context context, Bundle savedInstanceState) {
        this.listener = (OnAlbumClickedListener) context;
        this.allAlbums = new ArrayList<>();
        this.albums = new ArrayList<>();
        this.filter = new AlbumFilter();
        this.filter.initialiseFilter(savedInstanceState);
    }

    public void setAllAlbums(List<AlbumWithImageAndArtist> allAlbums) {
        this.allAlbums = allAlbums;

        getFilter().filter(null);
        notifyDataSetChanged();
    }

    public void setArtists(List<Artist> artists) {
        this.filter.initAllArtists(artists);

        getFilter().filter(null);
        notifyDataSetChanged();
    }

    @Override
    @NonNull
    public AlbumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_album_list_item, parent, false);
        return new AlbumViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final AlbumViewHolder holder, int position) {
        holder.setAlbum(albums.get(position));
    }

    @Override
    public int getItemCount() {
        return albums.size();
    }

    void onSaveInstanceState(Bundle outState) {
        filter.onSaveInstanceState(outState);
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

        private AlbumWithImageAndArtist album;

        AlbumViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            listener.albumClicked(this.album.id);
        }

        private void setAlbum(AlbumWithImageAndArtist album) {
            this.album = album;

            this.albumName.setText(album.name);
            this.albumYear.setText(album.yearReleased == null ? "" : String.valueOf(album.yearReleased));
            this.albumArtist.setText(album.artistName);

            if (album.imageData != null) {
                final Bitmap bitmap = BitmapFactory.decodeByteArray(
                        album.imageData, 0, album.imageData.length);
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
        }

        private void initAllArtists(List<Artist> artists) {
            this.artists.clear();
            for (Artist artist : artists) {
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

        public void add(long artistId) {
            this.artists.put(artistId, true);
        }

        void flipArtist(long artistId) {
            this.artists.put(artistId, !this.artists.get(artistId));
        }

        Boolean getArtistValue(long artistId) {
            return this.artists.get(artistId);
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
            final List<AlbumWithImageAndArtist> newList = new LinkedList<>(allAlbums);
            for (Iterator<AlbumWithImageAndArtist> i = newList.iterator(); i.hasNext();) {
                final AlbumWithImageAndArtist album = i.next();
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
                albums.addAll((List<AlbumWithImageAndArtist>) filterResults.values);
                notifyDataSetChanged();
            }
        }
    }
}
