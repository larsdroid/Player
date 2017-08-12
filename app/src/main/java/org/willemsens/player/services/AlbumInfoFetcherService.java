package org.willemsens.player.services;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import org.willemsens.player.R;
import org.willemsens.player.imagefetchers.AlbumInfo;
import org.willemsens.player.imagefetchers.ArtFetcher;
import org.willemsens.player.imagefetchers.ImageDownloader;
import org.willemsens.player.imagefetchers.musicbrainz.MusicbrainzArtFetcher;
import org.willemsens.player.model.Album;
import org.willemsens.player.model.Image;

import java.util.List;

/**
 * A background service that iterates over the albums and artists in DB that don't have an image
 * yet and fetches those images.
 */
public class AlbumInfoFetcherService extends InfoFetcherService {
    private final ArtFetcher artFetcher;

    public AlbumInfoFetcherService() {
        super(AlbumInfoFetcherService.class.getName());

        this.artFetcher = new MusicbrainzArtFetcher();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        final ImageDownloader imageDownloader = new ImageDownloader();

        long albumId = -1;
        if (intent != null) {
            albumId = intent.getLongExtra(getString(R.string.key_album_id), -1);
        }

        if (albumId != -1) {
            final Album album = getMusicDao().findAlbum(albumId);
            fetchSingleAlbum(album, imageDownloader);
        } else {
            final List<Album> albums = getMusicDao().getAllAlbumsWithoutArt();
            for (Album album : albums) {
                fetchSingleAlbum(album, imageDownloader);
            }
        }
    }

    private void fetchSingleAlbum(Album album, ImageDownloader imageDownloader) {
        final Image image = new Image();

        final AlbumInfo albumInfo = artFetcher.fetchAlbumInfo(album.getArtist().getName(), album.getName());
        image.setUrl(albumInfo.getCoverImageUrl());
        image.setSource(artFetcher.getImageSource());
        image.setImageData(imageDownloader.downloadImage(image.getUrl()));

        getMusicDao().saveImage(image);

        album.setImage(image);
        if (album.getYearReleased() == null && albumInfo.getYear() != null) {
            album.setYearReleased(albumInfo.getYear());
        }
        getMusicDao().updateAlbum(album);

        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        Intent broadcast = new Intent(getString(R.string.key_album_updated));
        broadcast.putExtra(getString(R.string.key_album_id), album.getId());
        lbm.sendBroadcast(broadcast);

        waitRateLimit();
    }
}
