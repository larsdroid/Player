package org.willemsens.player.view.main.album;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import org.willemsens.player.R;
import org.willemsens.player.persistence.entities.Artist;
import org.willemsens.player.persistence.entities.Song;
import org.willemsens.player.util.StringFormat;
import org.willemsens.player.view.main.music.songs.OnSongClickedListener;

import java.util.ArrayList;
import java.util.List;

public class AlbumSongAdapter extends RecyclerView.Adapter<AlbumSongAdapter.SongViewHolder> {
    private final Context context;
    private final OnSongClickedListener listener;
    private List<Song> songs;
    private Long highlightedSongId;
    private Artist artist;

    AlbumSongAdapter(Context context) {
        this.context = context;
        this.listener = (OnSongClickedListener) context;
        this.songs = new ArrayList<>();
        this.highlightedSongId = null;
    }

    public void setSongs(List<Song> songs) {
        this.songs = songs;
        this.notifyDataSetChanged();
    }

    public void setHighlightedSong(Long songId) {
        final Long previouslyHightlightedId = this.highlightedSongId;
        this.highlightedSongId = songId;

        notifyItemWithSongId(previouslyHightlightedId);
        notifyItemWithSongId(this.highlightedSongId);
    }

    public boolean isSongHighlighted() {
        return this.highlightedSongId != null;
    }

    private void notifyItemWithSongId(Long songId) {
        if (songId != null) {
            int index = 0;
            for (Song song : this.songs) {
                if (song.id == songId) {
                    break;
                }
                index++;
            }
            if (index < this.songs.size()) {
                this.notifyItemChanged(index);
            }
        }
    }

    public void setArtist(Artist artist) {
        this.artist = artist;
        this.notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_album_song_list_item, parent, false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        final Song song = this.songs.get(position);
        holder.setSong(song);
    }

    @Override
    public int getItemCount() {
        return this.songs.size();
    }

    class SongViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.song_list_track)
        TextView trackNumber;

        @BindView(R.id.song_list_name)
        TextView songName;

        @BindView(R.id.song_list_artist)
        TextView artistName;

        @BindView(R.id.song_list_length)
        TextView songLength;

        private Song song;

        SongViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            listener.songClicked(this.song.id);
        }

        private void setSong(Song song) {
            this.song = song;

            this.trackNumber.setText(String.valueOf(song.track));
            this.songName.setText(song.name);
            if (artist != null) {
                this.artistName.setText(artist.name);
            }
            this.songLength.setText(StringFormat.formatToSongLength(song.length));

            if (highlightedSongId != null && highlightedSongId == song.id) {
                this.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorAccent));
            } else {
                this.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorBackground));
            }
        }
    }
}
