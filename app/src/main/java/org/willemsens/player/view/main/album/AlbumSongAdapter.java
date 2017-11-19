package org.willemsens.player.view.main.album;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import org.willemsens.player.R;
import org.willemsens.player.model.Album;
import org.willemsens.player.model.Song;
import org.willemsens.player.util.StringFormat;
import org.willemsens.player.view.DataAccessProvider;
import org.willemsens.player.view.main.music.songs.OnSongClickedListener;

import java.util.List;

public class AlbumSongAdapter extends RecyclerView.Adapter<AlbumSongAdapter.SongViewHolder> {
    private final List<Song> songs;
    private final OnSongClickedListener listener;

    AlbumSongAdapter(Context context, DataAccessProvider dataAccessProvider, Album album) {
        this.listener = (OnSongClickedListener) context;
        // TODO: can requery handle 'album.getSongs()'?
        this.songs = dataAccessProvider.getMusicDao().getAllSongs(album);
    }

    @Override
    public SongViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_album_song_list_item, parent, false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SongViewHolder holder, int position) {
        final Song song = songs.get(position);
        holder.setSong(song);
    }

    @Override
    public int getItemCount() {
        return songs.size();
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
            listener.songClicked(this.song);
        }

        private void setSong(Song song) {
            this.song = song;

            this.trackNumber.setText(String.valueOf(song.getTrack()));
            this.songName.setText(song.getName());
            this.artistName.setText(song.getArtist().getName());
            this.songLength.setText(StringFormat.formatToSongLength(song.getLength()));
        }
    }
}
