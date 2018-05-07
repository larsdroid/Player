package org.willemsens.player.view.main.music.artists;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import org.willemsens.player.R;
import org.willemsens.player.model.Artist;
import org.willemsens.player.model.Image;
import org.willemsens.player.persistence.AppDatabase;
import org.willemsens.player.persistence.MusicDao;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display an {@link Artist}.
 */
class ArtistRecyclerViewAdapter extends RecyclerView.Adapter<ArtistRecyclerViewAdapter.ArtistViewHolder> {
    private final List<Artist> artists;
    private final OnArtistClickedListener listener;
    private final MusicDao musicDao;

    ArtistRecyclerViewAdapter(Context context, List<Artist> artists, OnArtistClickedListener listener) {
        this.artists = artists;
        this.listener = listener;
        this.musicDao = AppDatabase.getAppDatabase(context).musicDao();
    }

    @Override
    public ArtistViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_artist_list_item, parent, false);
        return new ArtistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ArtistViewHolder holder, int position) {
        holder.setArtist(artists.get(position));
    }

    @Override
    public int getItemCount() {
        return artists.size();
    }

    class ArtistViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.artist_list_name)
        TextView artistName;

        @BindView(R.id.artist_list_image)
        ImageView artistImage;

        @BindView(R.id.artist_list_progress_bar)
        ProgressBar progressBar;

        private Artist artist;

        ArtistViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            listener.artistClicked(this.artist);
        }

        private void setArtist(Artist artist) {
            this.artist = artist;

            this.artistName.setText(artist.name);

            if (artist.imageId != null) {
                final Image artistImage = musicDao.findImage(artist.imageId);
                final Bitmap bitmap = BitmapFactory.decodeByteArray(
                        artistImage.imageData, 0, artistImage.imageData.length);
                this.artistImage.setImageBitmap(bitmap);

                this.artistImage.setVisibility(View.VISIBLE);
                this.progressBar.setVisibility(View.GONE);
            } else {
                this.artistImage.setImageDrawable(null);

                this.artistImage.setVisibility(View.GONE);
                this.progressBar.setVisibility(View.VISIBLE);
            }
        }
    }
}
