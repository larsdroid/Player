package org.willemsens.player.services;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import org.willemsens.player.R;
import org.willemsens.player.exceptions.NetworkClientException;
import org.willemsens.player.exceptions.NetworkServerException;
import org.willemsens.player.imagefetchers.ArtistInfo;
import org.willemsens.player.imagefetchers.ImageDownloader;
import org.willemsens.player.imagefetchers.InfoFetcher;
import org.willemsens.player.imagefetchers.discogs.DiscogsInfoFetcher;
import org.willemsens.player.imagegenerators.ImageGenerator;
import org.willemsens.player.model.Artist;
import org.willemsens.player.model.Image;

import java.util.List;

public class ArtistInfoFetcherService extends InfoFetcherService {
    private final InfoFetcher infoFetcher;

    public ArtistInfoFetcherService() {
        super(ArtistInfoFetcherService.class.getName());

        this.infoFetcher = new DiscogsInfoFetcher();
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
            fetchArtist(artist, imageDownloader);
        } else {
            final List<Artist> artists = getMusicDao().getAllArtistsMissingImage();
            for (Artist artist : artists) {
                fetchArtist(artist, imageDownloader);
            }
        }
    }

    private void generateArtistImage(Artist artist) {
        final Image image = new Image();
        image.setImageData(ImageGenerator.generateArtistImage(artist));
        getMusicDao().saveImage(image);
        artist.setImage(image);
    }

    private void fetchArtist(Artist artist, ImageDownloader imageDownloader) {
        boolean isArtistUpdated = false;

        try {
            final String artistId = infoFetcher.fetchArtistId(artist.getName());

            waitRateLimit();

            final ArtistInfo artistInfo = infoFetcher.fetchArtistInfo(artistId);

            final Image image = new Image();
            image.setUrl(artistInfo.getImageUrl());
            image.setImageData(imageDownloader.downloadImage(image.getUrl()));

            getMusicDao().saveImage(image);

            artist.setImage(image);

            isArtistUpdated = true;
        } catch (NetworkClientException e) {
            generateArtistImage(artist);
            isArtistUpdated = true;
        } catch (NetworkServerException e) {
            // Ignore
        }

        if (isArtistUpdated) {
            getMusicDao().updateArtist(artist);

            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
            Intent broadcast = new Intent(getString(R.string.key_artist_updated));
            broadcast.putExtra(getString(R.string.key_artist_id), artist.getId());
            lbm.sendBroadcast(broadcast);
        }

        waitRateLimit();
    }
}
