package org.willemsens.player.view.main.music.albums;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.willemsens.player.R;
import org.willemsens.player.model.Album;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * {@link RecyclerView.Adapter} that can display an {@link Album}.
 */
class AlbumRecyclerViewAdapter extends RecyclerView.Adapter<AlbumRecyclerViewAdapter.ViewHolder> {
    private final List<Album> albums;

    AlbumRecyclerViewAdapter(List<Album> albums) {
        this.albums = albums;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_album, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.setAlbum(albums.get(position));
    }

    @Override
    public int getItemCount() {
        return albums.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.album_list_name)
        TextView albumName;

        @BindView(R.id.album_list_year)
        TextView albumYear;

        @BindView(R.id.album_list_image)
        ImageView albumCover;

        @BindView(R.id.progress_bar)
        ProgressBar progressBar;

        ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        private void setAlbum(Album album) {
            this.albumName.setText(album.getName());
            this.albumYear.setText(album.getYearReleased() == null ? "" : String.valueOf(album.getYearReleased()));

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
}
