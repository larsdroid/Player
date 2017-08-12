package org.willemsens.player.services;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import org.willemsens.player.R;
import org.willemsens.player.imagefetchers.ImageDownloader;
import org.willemsens.player.imagefetchers.discogs.DiscogsArtFetcher;
import org.willemsens.player.model.Artist;
import org.willemsens.player.model.Image;

import java.util.List;

public class ArtistImageFetcherService extends ImageFetcherService {
    private final DiscogsArtFetcher discogs;

    public ArtistImageFetcherService() {
        super(ArtistImageFetcherService.class.getName());

        this.discogs = new DiscogsArtFetcher();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        final ImageDownloader imageDownloader = new ImageDownloader();

        long artistId = -1;
        if (intent != null) {
            artistId = intent.getLongExtra(getString(R.string.key_artist_id), -1);
        }

        if (artistId != -1) {
            final Artist artist = getMusicDao().findArtist(artistId);
            fetchSingleArtist(artist, imageDownloader);
        } else {
            final List<Artist> artists = getMusicDao().getAllArtistsWithoutArt();
            for (Artist artist : artists) {
                fetchSingleArtist(artist, imageDownloader);
            }
        }
    }

    private void fetchSingleArtist(Artist artist, ImageDownloader imageDownloader) {
        final Image image = new Image();
        final Long discogsArtistId = discogs.fetchArtistId(artist.getName());

        waitRateLimit();

        image.setUrl(discogs.fetchArtistImage(discogsArtistId));
        image.setSource(discogs.getImageSource());
        image.setImageData(imageDownloader.downloadImage(image.getUrl()));

        getMusicDao().saveImage(image);

        artist.setImage(image);
        getMusicDao().updateArtist(artist);

        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        Intent broadcast = new Intent(getString(R.string.key_artist_updated));
        broadcast.putExtra(getString(R.string.key_artist_id), artist.getId());
        lbm.sendBroadcast(broadcast);

        waitRateLimit();
    }
}
