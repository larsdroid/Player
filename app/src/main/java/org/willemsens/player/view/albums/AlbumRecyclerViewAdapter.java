package org.willemsens.player.view.albums;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import org.willemsens.player.R;
import org.willemsens.player.model.AbstractAlbum;
import org.willemsens.player.model.Album;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display an {@link AbstractAlbum}.
 */
public class AlbumRecyclerViewAdapter extends RecyclerView.Adapter<AlbumRecyclerViewAdapter.ViewHolder> {
    private final List<Album> albums;

    public AlbumRecyclerViewAdapter(List<Album> albums) {
        this.albums = albums;
    }

    public List<Album> getAlbums() {
        return this.albums;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_album, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = albums.get(position);
        holder.mIdView.setText(String.valueOf(albums.get(position).getYearReleased()));
        holder.mContentView.setText(albums.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return albums.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mIdView;
        public final TextView mContentView;
        public AbstractAlbum mItem;

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
