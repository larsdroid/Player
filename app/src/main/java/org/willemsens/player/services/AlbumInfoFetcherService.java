package org.willemsens.player.services;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import org.willemsens.player.R;
import org.willemsens.player.imagefetchers.AlbumInfo;
import org.willemsens.player.imagefetchers.ImageDownloader;
import org.willemsens.player.imagefetchers.InfoFetcher;
import org.willemsens.player.imagefetchers.musicbrainz.MusicbrainzInfoFetcher;
import org.willemsens.player.imagegenerators.ImageGenerator;
import org.willemsens.player.model.Album;
import org.willemsens.player.model.Image;

import java.util.List;

/**
 * A background service that iterates over the albums and artists in DB that don't have an image
 * yet and fetches those images.
 */
public class AlbumInfoFetcherService extends InfoFetcherService {
    private final InfoFetcher infoFetcher;

    public AlbumInfoFetcherService() {
        super(AlbumInfoFetcherService.class.getName());

        this.infoFetcher = new MusicbrainzInfoFetcher();
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
            final List<Album> albums = getMusicDao().getAllAlbumsMissingInfo();
            for (Album album : albums) {
                fetchSingleAlbum(album, imageDownloader);
            }
        }
    }

    private boolean fetchSingleAlbumArt(Album album, ImageDownloader imageDownloader, AlbumInfo albumInfo) {
        if (album.getImage() == null) {
            byte[] imageData = null;
            String coverImageUrl = null;
            if (albumInfo != null) {
                coverImageUrl = albumInfo.getCoverImageUrl();
                imageData = imageDownloader.downloadImage(coverImageUrl);
            }

            if (imageData == null) { // TODO: ... AND response was 404
                // TODO: try the discogs service first?
                coverImageUrl = null;
                imageData = ImageGenerator.generateAlbumCover();
            }
            // TODO: else if not 404 (e.g. 500, serice down) --> keep image at NULL and return false

            final Image image = new Image();
            image.setUrl(coverImageUrl);
            image.setImageData(imageData);
            getMusicDao().saveImage(image);
            album.setImage(image);

            return true;
        } else {
            return false;
        }
    }

    private void fetchSingleAlbum(Album album, ImageDownloader imageDownloader) {
        final AlbumInfo albumInfo = infoFetcher.fetchAlbumInfo(album.getArtist().getName(), album.getName());

        boolean albumUpdated = false;

        if (albumInfo != null) { //  TODO:  ... OR albumInfo was 404
            albumUpdated = fetchSingleAlbumArt(album, imageDownloader, albumInfo);
        }

        if (album.getYearReleased() == null && albumInfo != null && albumInfo.getYear() != null) {
            album.setYearReleased(albumInfo.getYear());
            album.setSource(albumInfo.getInfoSource());
            albumUpdated = true;
        }

        if (albumUpdated) {
            getMusicDao().updateAlbum(album);

            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
            Intent broadcast = new Intent(getString(R.string.key_album_updated));
            broadcast.putExtra(getString(R.string.key_album_id), album.getId());
            lbm.sendBroadcast(broadcast);
        }

        waitRateLimit();
    }
}
