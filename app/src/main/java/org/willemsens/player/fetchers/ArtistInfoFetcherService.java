package org.willemsens.player.fetchers;

import android.content.Intent;
import android.support.annotation.Nullable;
import org.willemsens.player.R;
import org.willemsens.player.exceptions.NetworkClientException;
import org.willemsens.player.exceptions.NetworkServerException;
import org.willemsens.player.fetchers.discogs.DiscogsInfoFetcher;
import org.willemsens.player.fetchers.imagegenerators.ImageGenerator;
import org.willemsens.player.model.Artist;
import org.willemsens.player.model.Image;
import org.willemsens.player.musiclibrary.MusicLibraryBroadcastBuilder;

import java.util.List;

import static org.willemsens.player.musiclibrary.MusicLibraryBroadcastType.ARTISTS_INSERTED;
import static org.willemsens.player.musiclibrary.MusicLibraryBroadcastType.ARTIST_INSERTED;
import static org.willemsens.player.musiclibrary.MusicLibraryBroadcastType.ARTIST_UPDATED;

public class ArtistInfoFetcherService extends InfoFetcherService {
    private final InfoFetcher infoFetcher;

    public ArtistInfoFetcherService() {
        super(ArtistInfoFetcherService.class.getName());

        this.infoFetcher = new DiscogsInfoFetcher();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        final ImageDownloader imageDownloader = new ImageDownloader();
        if (intent == null || intent.getAction() == null
                || intent.getAction().equals(ARTISTS_INSERTED.getString(this))) {
            // Scan all artists for missing information that can be fetched.
            final List<Artist> artists = getMusicDao().getAllArtistsMissingImage();
            for (Artist artist : artists) {
                fetchArtist(artist, imageDownloader);
            }
        } else if (intent.getAction().equals(ARTIST_INSERTED.getString(this))) {
            // Fetch info for a single artist.
            long artistId = intent.getLongExtra(getString(R.string.key_artist_id), -1);
            if (artistId != -1) {
                final Artist artist = getMusicDao().findArtist(artistId);
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

            MusicLibraryBroadcastBuilder builder = new MusicLibraryBroadcastBuilder(this);
            builder
                    .setType(ARTIST_UPDATED)
                    .setArtist(artist)
                    .buildAndSubmitBroadcast();
        }

        waitRateLimit();
    }
}
