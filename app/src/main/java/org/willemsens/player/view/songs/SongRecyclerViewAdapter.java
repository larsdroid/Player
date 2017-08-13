package org.willemsens.player.view.songs;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import org.willemsens.player.R;
import org.willemsens.player.model.Song;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Song}.
 */
class SongRecyclerViewAdapter extends RecyclerView.Adapter<SongRecyclerViewAdapter.ViewHolder> {
    private final List<Song> songs;
    private final OnSongClickedListener listener;

    SongRecyclerViewAdapter(List<Song> songs, OnSongClickedListener listener) {
        this.songs = songs;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_song, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.setSong(songs.get(position));
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.song_list_album_image)
        ImageView albumCover;

        @BindView(R.id.song_list_name)
        TextView songName;

        @BindView(R.id.song_list_track)
        TextView songTrack;

        @BindView(R.id.song_list_album)
        TextView songAlbumName;

        private Song song;

        ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            listener.songClicked(this.song);
        }

        private void setSong(Song song) {
            this.song = song;

            if (song.getAlbum().getImage() != null) {
                final Bitmap bitmap = BitmapFactory.decodeByteArray(
                        song.getAlbum().getImage().getImageData(), 0, song.getAlbum().getImage().getImageData().length);
                this.albumCover.setImageBitmap(bitmap);
            }

            this.songName.setText(song.getName());
            this.songTrack.setText(String.valueOf(song.getTrack()));
            this.songAlbumName.setText(song.getAlbum().getName());
        }
    }
}
