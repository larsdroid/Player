package org.willemsens.player.fetchers;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import org.willemsens.player.exceptions.NetworkClientException;
import org.willemsens.player.exceptions.NetworkServerException;
import org.willemsens.player.fetchers.imagegenerators.ImageGenerator;
import org.willemsens.player.fetchers.musicbrainz.MusicbrainzInfoFetcher;
import org.willemsens.player.model.Album;
import org.willemsens.player.model.Image;
import org.willemsens.player.musiclibrary.MusicLibraryBroadcastBuilder;

import java.util.List;

import static org.willemsens.player.musiclibrary.MusicLibraryBroadcastPayloadType.MLBPT_ALBUM_ID;
import static org.willemsens.player.musiclibrary.MusicLibraryBroadcastType.MLBT_ALBUMS_INSERTED;
import static org.willemsens.player.musiclibrary.MusicLibraryBroadcastType.MLBT_ALBUM_INSERTED;
import static org.willemsens.player.musiclibrary.MusicLibraryBroadcastType.MLBT_ALBUM_UPDATED;

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
        if (intent == null || intent.getAction() == null
                || intent.getAction().equals(MLBT_ALBUMS_INSERTED.name())) {
            // Scan all albums for missing information that can be fetched.
            final List<Album> albums = getMusicDao().getAllAlbumsMissingInfo();
            for (Album album : albums) {
                fetchAlbum(album, imageDownloader);
            }
        } else if (intent.getAction().equals(MLBT_ALBUM_INSERTED.name())) {
            // Fetch info for a single album.
            final long albumId = intent.getLongExtra(MLBPT_ALBUM_ID.name(), -1);
            if (albumId != -1) {
                final Album album = getMusicDao().findAlbum(albumId);
                fetchAlbum(album, imageDownloader);
            }
        }
    }

    private boolean fetchAlbumArt(Album album, ImageDownloader imageDownloader, @NonNull AlbumInfo albumInfo) {
        if (album.getImage() == null) {
            try {
                final String coverImageUrl = albumInfo.getCoverImageUrl();
                final byte[] imageData = imageDownloader.downloadImage(coverImageUrl);

                final Image image = new Image();
                image.setUrl(coverImageUrl);
                image.setImageData(imageData);
                getMusicDao().insertImage(image);
                album.setImage(image);
            } catch (NetworkClientException e) {
                generateAlbumArt(album);
            } catch (NetworkServerException e) {
                return false;
            }
            return true;
        } else {
            return false;
        }
    }

    private void generateAlbumArt(Album album) {
        final Image image = new Image();
        image.setImageData(ImageGenerator.generateAlbumCover(album));
        getMusicDao().insertImage(image);
        album.setImage(image);
    }

    private void fetchAlbum(Album album, ImageDownloader imageDownloader) {
        boolean isAlbumUpdated = false;

        try {
            final AlbumInfo albumInfo = infoFetcher.fetchAlbumInfo(album.getArtist().getName(), album.getName());

            isAlbumUpdated = fetchAlbumArt(album, imageDownloader, albumInfo);

            if (album.getYearReleased() == null && albumInfo.getYear() != null) {
                album.setYearReleased(albumInfo.getYear());
                isAlbumUpdated = true;
            }
        } catch (NetworkClientException e) {
            if (album.getImage() == null) {
                generateAlbumArt(album);
                isAlbumUpdated = true;
            }
        } catch (NetworkServerException e) {
            // Ignore
        }

        if (isAlbumUpdated) {
            getMusicDao().updateAlbum(album);

            MusicLibraryBroadcastBuilder builder = new MusicLibraryBroadcastBuilder(this);
            builder
                    .setType(MLBT_ALBUM_UPDATED)
                    .setAlbum(album)
                    .buildAndSubmitBroadcast();
        }

        waitRateLimit();
    }
}
