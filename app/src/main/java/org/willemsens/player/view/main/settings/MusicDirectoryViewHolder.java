package org.willemsens.player.view.main.settings;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import org.willemsens.player.R;
import org.willemsens.player.persistence.entities.Directory;

public class MusicDirectoryViewHolder extends RecyclerView.ViewHolder {
    @BindView(R.id.directory_path)
    TextView directoryPath;

    private final DirectoryDeleteListener listener;

    private Directory directory;

    MusicDirectoryViewHolder(View view, DirectoryDeleteListener listener) {
        super(view);
        this.listener = listener;
        ButterKnife.bind(this, view);
    }

    @OnClick(R.id.delete_button)
    public void onDeleteClicked() {
        this.listener.onDeleteDirectory(this.directory);
    }

    void setDirectory(Directory directory) {
        this.directory = directory;

        this.directoryPath.setText(directory.path);
    }

    interface DirectoryDeleteListener {
        void onDeleteDirectory(Directory directory);
    }
}
