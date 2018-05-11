package org.willemsens.player.view.main.album;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import org.willemsens.player.R;
import org.willemsens.player.model.Song;
import org.willemsens.player.util.StringFormat;
import org.willemsens.player.view.main.music.songs.OnSongClickedListener;

import java.util.ArrayList;
import java.util.List;

import static org.willemsens.player.playback.PlayBackBroadcastType.PBBT_PLAYER_STATUS_UPDATE;

public class AlbumSongAdapter extends RecyclerView.Adapter<AlbumSongAdapter.SongViewHolder> {
    private final Context context;
    private final OnSongClickedListener listener;
    private final PlayBackUpdateReceiver playBackUpdateReceiver;
    private List<Song> songs;

    AlbumSongAdapter(Context context) {
        this.context = context;
        this.listener = (OnSongClickedListener) context;
        this.playBackUpdateReceiver = new PlayBackUpdateReceiver();
        this.songs = new ArrayList<>();
    }

    public void setSongs(List<Song> songs) {
        this.songs = songs;
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
            listener.songClicked(this.song);
        }

        private void setSong(Song song) {
            this.song = song;

            this.trackNumber.setText(String.valueOf(song.track));
            this.songName.setText(song.name);
            // TODO this.artistName.setText(musicDao.findArtist(song.artistId).name);
            this.songLength.setText(StringFormat.formatToSongLength(song.length));

            /*
            TODO
            final Song currentSong = musicDao.getCurrentSong();
            if (currentSong != null && currentSong.id == song.id) {
                this.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorAccent));
            } else {
                this.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorBackground));
            }
            */
        }
    }

    void registerPlayBackUpdateReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(PBBT_PLAYER_STATUS_UPDATE.name());
        context.registerReceiver(this.playBackUpdateReceiver, filter);
    }

    void unregisterPlayBackUpdateReceiver() {
        context.unregisterReceiver(this.playBackUpdateReceiver);
    }

    private class PlayBackUpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            notifyDataSetChanged();
            // TODO: listen to STOPPED --> set current song ID to -1
        }
    }
}
