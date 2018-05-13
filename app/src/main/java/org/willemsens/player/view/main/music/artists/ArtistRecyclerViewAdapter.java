package org.willemsens.player.view.main.music.artists;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
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
import org.willemsens.player.persistence.entities.Artist;
import org.willemsens.player.persistence.entities.helpers.ArtistWithImage;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display an {@link Artist}.
 */
class ArtistRecyclerViewAdapter extends RecyclerView.Adapter<ArtistRecyclerViewAdapter.ArtistViewHolder> {
    private List<ArtistWithImage> artistsWithImages;
    private final OnArtistClickedListener listener;

    ArtistRecyclerViewAdapter(OnArtistClickedListener listener) {
        this.artistsWithImages = new ArrayList<>();
        this.listener = listener;
    }

    public void setArtistsWithImages(List<ArtistWithImage> artistsWithImages) {
        this.artistsWithImages = artistsWithImages;
        this.notifyDataSetChanged();
    }

    @Override
    @NonNull
    public ArtistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_artist_list_item, parent, false);
        return new ArtistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ArtistViewHolder holder, int position) {
        holder.setArtistWithImage(artistsWithImages.get(position));
    }

    @Override
    public int getItemCount() {
        return artistsWithImages.size();
    }

    class ArtistViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.artist_list_name)
        TextView artistName;

        @BindView(R.id.artist_list_image)
        ImageView artistImage;

        @BindView(R.id.artist_list_progress_bar)
        ProgressBar progressBar;

        private ArtistWithImage artistWithImage;

        ArtistViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            listener.artistClicked(this.artistWithImage.id);
        }

        private void setArtistWithImage(ArtistWithImage artistWithImage) {
            this.artistWithImage = artistWithImage;

            this.artistName.setText(artistWithImage.name);

            if (artistWithImage.imageData != null) {
                final Bitmap bitmap = BitmapFactory.decodeByteArray(
                        artistWithImage.imageData, 0, artistWithImage.imageData.length);
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
