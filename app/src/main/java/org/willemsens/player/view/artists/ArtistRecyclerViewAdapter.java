package org.willemsens.player.view.artists;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.willemsens.player.R;
import org.willemsens.player.model.Artist;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * {@link RecyclerView.Adapter} that can display an {@link Artist}.
 */
class ArtistRecyclerViewAdapter extends RecyclerView.Adapter<ArtistRecyclerViewAdapter.ViewHolder> {
    private final List<Artist> artists;

    ArtistRecyclerViewAdapter(List<Artist> artists) {
        this.artists = artists;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_artist, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.setArtist(artists.get(position));
    }

    @Override
    public int getItemCount() {
        return artists.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.artist_list_name)
        TextView artistName;

        @BindView(R.id.artist_list_image)
        ImageView artistImage;

        ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        private void setArtist(Artist artist) {
            this.artistName.setText(artist.getName());

            if (artist.getImage() != null) {
                final Bitmap bitmap = BitmapFactory.decodeByteArray(
                        artist.getImage().getImageData(), 0, artist.getImage().getImageData().length);
                this.artistImage.setImageBitmap(bitmap);
            } else {
                this.artistImage.setImageDrawable(null);
            }
        }
    }
}
