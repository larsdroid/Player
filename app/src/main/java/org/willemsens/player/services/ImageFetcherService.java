package org.willemsens.player.services;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import org.willemsens.player.PlayerApplication;
import org.willemsens.player.imagefetchers.ImageDownloader;
import org.willemsens.player.imagefetchers.discogs.DiscogsArtFetcher;
import org.willemsens.player.imagefetchers.musicbrainz.MusicbrainzArtFetcher;
import org.willemsens.player.model.Album;
import org.willemsens.player.model.Artist;
import org.willemsens.player.model.Image;
import org.willemsens.player.model.ImageSource;
import org.willemsens.player.persistence.MusicDao;

import java.util.List;

import io.requery.Persistable;
import io.requery.sql.EntityDataStore;

/**
 * A background service that iterates over the albums and artists in DB that don't have an image
 * yet and fetches those images.
 */
public class ImageFetcherService extends IntentService {
    private static final int WAIT_MILLIS = 1100;

    private final DiscogsArtFetcher discogs;
    private final MusicbrainzArtFetcher musicbrainz;
    private MusicDao musicDao;

    public ImageFetcherService() {
        super(ImageFetcherService.class.getName());

        this.discogs = new DiscogsArtFetcher();
        this.musicbrainz = new MusicbrainzArtFetcher();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (this.musicDao == null) {
            final EntityDataStore<Persistable> dataStore = ((PlayerApplication)getApplication()).getData();
            this.musicDao = new MusicDao(dataStore);
        }

        final ImageDownloader imageDownloader = new ImageDownloader();

        final List<Artist> artists = this.musicDao.getAllArtistsWithoutArt();
        for (Artist artist : artists) {
            final Image image = new Image();
            final Long discogsArtistId = discogs.fetchArtistId(artist.getName());

            waitRateLimit();

            image.setUrl(discogs.fetchArtistImage(discogsArtistId));
            image.setSource(ImageSource.DISCOGS);
            image.setImageData(imageDownloader.downloadImage(image.getUrl()));

            this.musicDao.saveImage(image);

            artist.setImage(image);
            this.musicDao.updateArtist(artist);

            // TODO: LocalBroadCast --> artist updated

            waitRateLimit();
        }

        final List<Album> albums = this.musicDao.getAllAlbumsWithoutArt();
        for (Album album : albums) {
            final Image image = new Image();
            final String musicbrainzArtistId = musicbrainz.fetchArtistId(album.getArtist().getName());

            waitRateLimit();

            image.setUrl(musicbrainz.fetchLargeThumbnail(musicbrainzArtistId, album.getName()));
            image.setSource(ImageSource.MUSICBRAINZ);
            image.setImageData(imageDownloader.downloadImage(image.getUrl()));

            this.musicDao.saveImage(image);

            album.setImage(image);
            this.musicDao.updateAlbum(album);

            // TODO: LocalBroadCast --> album updated

            waitRateLimit();
        }
    }

    private void waitRateLimit() {
        try {
            Thread.sleep(WAIT_MILLIS);
        }
        catch (InterruptedException e) {
            Log.d(getClass().getName(), e.getMessage());
        }
    }
}
