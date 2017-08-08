package org.willemsens.player.view.artists;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.willemsens.player.R;
import org.willemsens.player.model.Artist;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display an {@link Artist}.
 */
public class ArtistRecyclerViewAdapter extends RecyclerView.Adapter<ArtistRecyclerViewAdapter.ViewHolder> {
    private final List<Artist> artists;

    public ArtistRecyclerViewAdapter(List<Artist> artists) {
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
        holder.mItem = artists.get(position);
        holder.mIdView.setText(String.valueOf(artists.get(position).getId()));
        holder.mContentView.setText(artists.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return artists.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mIdView;
        public final TextView mContentView;
        public Artist mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mIdView = (TextView) view.findViewById(R.id.id);
            mContentView = (TextView) view.findViewById(R.id.content);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}
