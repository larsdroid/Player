package org.willemsens.player.view.main.settings;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import org.willemsens.player.R;
import org.willemsens.player.model.Directory;
import org.willemsens.player.persistence.MusicDao;
import org.willemsens.player.view.DataAccessProvider;

import java.util.ArrayList;
import java.util.List;

public class MusicDirectoryAdapter
        extends RecyclerView.Adapter<MusicDirectoryViewHolder>
        implements MusicDirectoryViewHolder.DirectoryDeleteListener {
    private final List<Directory> directories;
    private final MusicDao musicDao;

    public MusicDirectoryAdapter(DataAccessProvider dataAccessProvider) {
        this.directories = new ArrayList<>();
        this.musicDao = dataAccessProvider.getMusicDao();
        readAllDirectories();
    }

    public void readAllDirectories() {
        this.directories.clear();
        this.directories.addAll(this.musicDao.getAllDirectories());
    }

    @NonNull
    @Override
    public MusicDirectoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_settings_directory_list_item, parent, false);
        return new MusicDirectoryViewHolder(view, this);
    }

    @Override
    public void onBindViewHolder(@NonNull MusicDirectoryViewHolder holder, int position) {
        final Directory directory = directories.get(position);
        holder.setDirectory(directory);
    }

    @Override
    public int getItemCount() {
        return directories.size();
    }

    @Override
    public void onDeleteDirectory(Directory directory) {
        this.musicDao.deleteDirectory(directory);
        int index = this.directories.indexOf(directory);
        this.directories.remove(directory);
        this.notifyItemRemoved(index);
    }
}
