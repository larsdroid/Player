package org.willemsens.player.view.main.settings;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import org.willemsens.player.R;
import org.willemsens.player.model.Directory;

public class MusicDirectoryViewHolder extends RecyclerView.ViewHolder {
    @BindView(R.id.directory_path)
    TextView directoryPath;

    private Directory directory;

    MusicDirectoryViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }

    @OnClick(R.id.delete_button)
    public void onDeleteClicked(View v) {
        // TODO
    }

    void setDirectory(Directory directory) {
        this.directory = directory;

        this.directoryPath.setText(directory.getPath());
    }
}
