package org.willemsens.player.services;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import org.willemsens.player.R;
import org.willemsens.player.imagefetchers.ImageDownloader;
import org.willemsens.player.imagefetchers.discogs.DiscogsArtFetcher;
import org.willemsens.player.model.Artist;
import org.willemsens.player.model.Image;
import org.willemsens.player.model.ImageSource;

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

        final List<Artist> artists = getMusicDao().getAllArtistsWithoutArt();
        for (Artist artist : artists) {
            final Image image = new Image();
            final Long discogsArtistId = discogs.fetchArtistId(artist.getName());

            waitRateLimit();

            image.setUrl(discogs.fetchArtistImage(discogsArtistId));
            image.setSource(ImageSource.DISCOGS);
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
}
