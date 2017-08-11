package org.willemsens.player.services;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import org.willemsens.player.R;
import org.willemsens.player.imagefetchers.ImageDownloader;
import org.willemsens.player.imagefetchers.musicbrainz.MusicbrainzArtFetcher;
import org.willemsens.player.model.Album;
import org.willemsens.player.model.Image;
import org.willemsens.player.model.ImageSource;

import java.util.List;

/**
 * A background service that iterates over the albums and artists in DB that don't have an image
 * yet and fetches those images.
 */
public class AlbumImageFetcherService extends ImageFetcherService {
    private final MusicbrainzArtFetcher musicbrainz;

    public AlbumImageFetcherService() {
        super(AlbumImageFetcherService.class.getName());

        this.musicbrainz = new MusicbrainzArtFetcher();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        final ImageDownloader imageDownloader = new ImageDownloader();

        final List<Album> albums = getMusicDao().getAllAlbumsWithoutArt();
        for (Album album : albums) {
            final Image image = new Image();
            final String musicbrainzArtistId = musicbrainz.fetchArtistId(album.getArtist().getName());

            waitRateLimit();

            image.setUrl(musicbrainz.fetchLargeThumbnail(musicbrainzArtistId, album.getName()));
            image.setSource(ImageSource.MUSICBRAINZ);
            image.setImageData(imageDownloader.downloadImage(image.getUrl()));

            getMusicDao().saveImage(image);

            album.setImage(image);
            getMusicDao().updateAlbum(album);

            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
            Intent broadcast = new Intent(getString(R.string.key_album_updated));
            broadcast.putExtra(getString(R.string.key_album_id), album.getId());
            lbm.sendBroadcast(broadcast);

            waitRateLimit();
        }
    }
}
