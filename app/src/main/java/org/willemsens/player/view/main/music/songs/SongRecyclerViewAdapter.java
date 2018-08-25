package org.willemsens.player.view.main.music.songs;

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
import org.willemsens.player.persistence.entities.Song;
import org.willemsens.player.persistence.entities.helpers.SongWithAlbumInfo;
import org.willemsens.player.util.StringFormat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.willemsens.player.musiclibrary.MusicLibraryBroadcastPayloadType.MLBPT_ALBUM_IDS;
import static org.willemsens.player.musiclibrary.MusicLibraryBroadcastPayloadType.MLBPT_ARTIST_IDS;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Song}.
 */
class SongRecyclerViewAdapter extends RecyclerView.Adapter<SongRecyclerViewAdapter.SongViewHolder> implements Filterable {
    private final OnSongClickedListener listener;
    private final SongFilter filter;
    private final List<SongWithAlbumInfo> songs;
    private List<SongWithAlbumInfo> allSongs;

    SongRecyclerViewAdapter(Context context, Bundle savedInstanceState) {
        this.listener = (OnSongClickedListener) context;
        this.allSongs = new ArrayList<>();
        this.songs = new ArrayList<>();
        this.filter = new SongFilter(savedInstanceState);
    }

    public void setAllSongs(List<SongWithAlbumInfo> allSongs) {
        this.allSongs = allSongs;

        getFilter().filter(null);
        notifyDataSetChanged();
    }

    public void setArtists(List<Artist> artists) {
        this.filter.initAllArtists(artists);

        getFilter().filter(null);
        notifyDataSetChanged();
    }

    public void setAlbums(List<Album> albums) {
        this.filter.initAllAlbums(albums);

        getFilter().filter(null);
        notifyDataSetChanged();
    }

    @Override
    @NonNull
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_song_list_item, parent, false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final SongViewHolder holder, int position) {
        final SongWithAlbumInfo song = songs.get(position);

        final boolean showAlbumCover =
                position == 0
                        || (position > 0 && songs.get(position - 1).albumId != song.albumId);

        final boolean showLongLine =
                (songs.size() > position + 1) && songs.get(position + 1).albumId != song.albumId;

        holder.setSong(song, showAlbumCover, showLongLine);
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    void onSaveInstanceState(Bundle outState) {
        filter.onSaveInstanceState(outState);
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

        private SongWithAlbumInfo song;

        SongViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            listener.songClicked(this.song.id);
        }

        private void setSong(SongWithAlbumInfo song, boolean showAlbumCover, boolean showLongLine) {
            this.song = song;

            if (showAlbumCover) {
                if (song.albumImageData != null) {
                    final Bitmap bitmap = BitmapFactory.decodeByteArray(
                            song.albumImageData, 0, song.albumImageData.length);
                    this.albumCover.setImageBitmap(bitmap);

                    this.albumCover.setVisibility(View.VISIBLE);
                    this.progressBar.setVisibility(View.GONE);
                } else {
                    this.albumCover.setImageDrawable(null);

                    this.albumCover.setVisibility(View.GONE);
                    this.progressBar.setVisibility(View.VISIBLE);
                }
            } else {
                this.albumCover.setImageDrawable(null);

                this.albumCover.setVisibility(View.VISIBLE);
                this.progressBar.setVisibility(View.GONE);
            }

            this.songName.setText(song.name);
            this.songTrack.setText(String.valueOf(song.track));
            this.songAlbumName.setText(song.artistName + " - " + song.albumName);
            this.songLength.setText(StringFormat.formatToSongLength(song.length));

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
        private final Map<Long, Boolean> albums; // Album ID --> Include in filter
        private final Map<Long, Boolean> artists; // Artist ID --> Include in filter
        private Bundle savedInstanceState;
        private boolean artistsInitialised;
        private boolean albumsInitialised;

        SongFilter(Bundle savedInstanceState) {
            this.albums = new HashMap<>();
            this.artists = new HashMap<>();
            this.savedInstanceState = savedInstanceState;
        }

        private void initAllArtists(List<Artist> artists) {
            this.artists.clear();
            for (Artist artist : artists) {
                this.artists.put(artist.id, true);
            }
            this.artistsInitialised = true;

            handleSavedInstanceState();
        }

        private void initAllAlbums(List<Album> albums) {
            this.albums.clear();
            for (Album album : albums) {
                this.albums.put(album.id, true);
            }
            this.albumsInitialised = true;

            handleSavedInstanceState();
        }

        private void setAllAlbums(boolean b) {
            for (Long albumId : this.albums.keySet()) {
                this.albums.put(albumId, b);
            }
        }

        private void setAllArtists(boolean b) {
            for (Long artistId : this.artists.keySet()) {
                this.artists.put(artistId, b);
            }
        }

        boolean hasAllAlbums() {
            for (Boolean b : this.albums.values()) {
                if (!b) {
                    return false;
                }
            }
            return true;
        }

        boolean hasAllArtists() {
            for (Boolean b : this.artists.values()) {
                if (!b) {
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

        void flipAlbum(long albumId) {
            this.albums.put(albumId, !this.albums.get(albumId));
        }

        void flipArtist(long artistId) {
            this.artists.put(artistId, !this.artists.get(artistId));
        }

        Boolean getAlbumValue(long albumId) {
            return this.albums.get(albumId);
        }

        Boolean getArtistValue(long artistId) {
            return this.artists.get(artistId);
        }

        private void onSaveInstanceState(Bundle outState) {
            List<Long> filterAlbums = new ArrayList<>();
            for (Long albumId : this.albums.keySet()) {
                if (this.albums.get(albumId)) {
                    filterAlbums.add(albumId);
                }
            }
            long[] filterAlbumIds = new long[filterAlbums.size()];
            int i = 0;
            for (Long albumId : filterAlbums) {
                filterAlbumIds[i++] = albumId;
            }
            outState.putLongArray(MLBPT_ALBUM_IDS.name(), filterAlbumIds);

            List<Long> filterArtists = new ArrayList<>();
            for (Long artistId : this.artists.keySet()) {
                if (this.artists.get(artistId)) {
                    filterArtists.add(artistId);
                }
            }
            long[] filterArtistIds = new long[filterArtists.size()];
            i = 0;
            for (Long artistId : filterArtists) {
                filterArtistIds[i++] = artistId;
            }
            outState.putLongArray(MLBPT_ARTIST_IDS.name(), filterArtistIds);
        }

        private void handleSavedInstanceState() {
            if (this.savedInstanceState != null && this.albumsInitialised && this.artistsInitialised) {
                long[] filterAlbumIds = this.savedInstanceState.getLongArray(MLBPT_ALBUM_IDS.name());
                if (filterAlbumIds != null) {
                    removeAllAlbums();
                    for (long filterAlbumId : filterAlbumIds) {
                        this.albums.put(filterAlbumId, true);
                    }
                }

                long[] filterArtistIds = this.savedInstanceState.getLongArray(MLBPT_ARTIST_IDS.name());
                if (filterArtistIds != null) {
                    removeAllArtists();
                    for (long filterArtistId : filterArtistIds) {
                        this.artists.put(filterArtistId, true);
                    }
                }

                this.savedInstanceState = null;
            }
        }

        @Override
        public FilterResults performFiltering(CharSequence charSequence) {
            FilterResults results = new FilterResults();
            final List<SongWithAlbumInfo> newList = new LinkedList<>(allSongs);
            for (Iterator<SongWithAlbumInfo> i = newList.iterator(); i.hasNext();) {
                final SongWithAlbumInfo song = i.next();
                if ((this.albums.get(song.albumId) != null && !this.albums.get(song.albumId))
                        || (this.artists.get(song.artistId) != null && !this.artists.get(song.artistId))) {
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
            if (filterResults.values != null) {
                songs.addAll((List<SongWithAlbumInfo>) filterResults.values);
            }
            notifyDataSetChanged();
        }
    }
}
