package org.willemsens.player.fetchers;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import org.willemsens.player.exceptions.NetworkClientException;
import org.willemsens.player.exceptions.NetworkServerException;
import org.willemsens.player.fetchers.imagegenerators.ImageGenerator;
import org.willemsens.player.fetchers.musicbrainz.MusicbrainzInfoFetcher;
import org.willemsens.player.musiclibrary.MusicLibraryBroadcastBuilder;
import org.willemsens.player.persistence.entities.Album;
import org.willemsens.player.persistence.entities.Artist;
import org.willemsens.player.persistence.entities.Image;
import org.willemsens.player.util.ExceptionHandling;

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
    private boolean stopProcessing = false;

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
            int i = 0;
            while (i < albums.size() && !stopProcessing) {
                final Album album = albums.get(i++);
                fetchAlbum(album, imageDownloader);
            }
        } else if (intent.getAction().equals(MLBT_ALBUM_INSERTED.name())) {
            // Fetch info for a single album.
            final long albumId = intent.getLongExtra(MLBPT_ALBUM_ID.name(), -1);
            if (albumId != -1) {
                final Album album = getMusicDao().findAlbum(albumId);
                // Null check mandatory since this service can have a reference to old data
                // at the instant the music library is being cleared.
                if (album != null) {
                    fetchAlbum(album, imageDownloader);
                }
            }
        }
    }

    private Long fetchAlbumArt(Album album, ImageDownloader imageDownloader, @NonNull AlbumInfo albumInfo) {
        if (album.imageId == null && albumInfo.getCoverImageUrl() != null) {
            try {
                final String coverImageUrl = albumInfo.getCoverImageUrl();
                final byte[] imageData = imageDownloader.downloadImage(coverImageUrl);

                final Image image = new Image(imageData);
                image.url = coverImageUrl;
                return getMusicDao().insertImage(image);
            } catch (NetworkClientException e) {
                ExceptionHandling.submitException(e);
                return generateAlbumArt(album);
            } catch (NetworkServerException e) {
                ExceptionHandling.submitException(e);
                return null;
            }
        } else {
            return null;
        }
    }

    private long generateAlbumArt(Album album) {
        final Image image = new Image(ImageGenerator.generateAlbumCover(album));
        return getMusicDao().insertImage(image);
    }

    private void fetchAlbum(Album album, ImageDownloader imageDownloader) {
        try {
            final Artist artist = getMusicDao().findArtist(album.artistId);

            // Null check mandatory since a this service can (for a very brief period of time)
            // have a reference to old data while it is being asked to stop the instant that
            // the music library is being reset/cleared.
            if (artist == null) {
                return;
            }
            final AlbumInfo albumInfo = infoFetcher.fetchAlbumInfo(artist.name, album.name);

            Long newImageId = fetchAlbumArt(album, imageDownloader, albumInfo);

            Integer newAlbumYear = null;
            if (album.yearReleased == null && albumInfo.getYear() != null) {
                newAlbumYear = albumInfo.getYear();
            }

            if (newImageId != null && newAlbumYear != null) {
                this.getMusicDao().updateAlbum(album.id, newImageId, newAlbumYear);
                album.imageId = newImageId;
                album.yearReleased = newAlbumYear;
                broadcastAlbumChange(album);
            } else if (newImageId != null) {
                this.getMusicDao().updateAlbum(album.id, newImageId);
                album.imageId = newImageId;
                broadcastAlbumChange(album);
            } else if (newAlbumYear != null) {
                this.getMusicDao().updateAlbum(album.id, newAlbumYear);
                album.yearReleased = newAlbumYear;
                broadcastAlbumChange(album);
            }
        } catch (NetworkClientException e) {
            ExceptionHandling.submitException(e);

            if (album.imageId == null) {
                long newImageId = generateAlbumArt(album);
                this.getMusicDao().updateAlbum(album.id, newImageId);
                album.imageId = newImageId;
                broadcastAlbumChange(album);
            }
        } catch (NetworkServerException e) {
            // Ignore
        }

        waitRateLimit();
    }

    private void broadcastAlbumChange(Album album) {
        MusicLibraryBroadcastBuilder builder = new MusicLibraryBroadcastBuilder(this);
        builder
                .setType(MLBT_ALBUM_UPDATED)
                .setAlbum(album)
                .buildAndSubmitBroadcast();
    }

    @Override
    public void onDestroy() {
        stopProcessing = true;
        super.onDestroy();
    }
}
