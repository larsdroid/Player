package org.willemsens.player.fetchers;

import android.content.Intent;
import android.support.annotation.Nullable;
import org.willemsens.player.exceptions.NetworkClientException;
import org.willemsens.player.exceptions.NetworkServerException;
import org.willemsens.player.fetchers.discogs.DiscogsInfoFetcher;
import org.willemsens.player.fetchers.imagegenerators.ImageGenerator;
import org.willemsens.player.persistence.entities.Artist;
import org.willemsens.player.persistence.entities.Image;

import java.util.List;

import static org.willemsens.player.musiclibrary.MusicLibraryBroadcastPayloadType.MLBPT_ARTIST_ID;
import static org.willemsens.player.musiclibrary.MusicLibraryBroadcastType.MLBT_ARTISTS_INSERTED;
import static org.willemsens.player.musiclibrary.MusicLibraryBroadcastType.MLBT_ARTIST_INSERTED;

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
                || intent.getAction().equals(MLBT_ARTISTS_INSERTED.name())) {
            // Scan all artists for missing information that can be fetched.
            final List<Artist> artists = getMusicDao().getAllArtistsMissingImage();
            for (Artist artist : artists) {
                fetchArtist(artist, imageDownloader);
            }
        } else if (intent.getAction().equals(MLBT_ARTIST_INSERTED.name())) {
            // Fetch info for a single artist.
            final long artistId = intent.getLongExtra(MLBPT_ARTIST_ID.name(), -1);
            if (artistId != -1) {
                final Artist artist = getMusicDao().findArtist(artistId);
                fetchArtist(artist, imageDownloader);
            }
        }
    }

    private void generateArtistImage(Artist artist) {
        final Image image = new Image(ImageGenerator.generateArtistImage(artist));
        artist.imageId = getMusicDao().insertImage(image);
    }

    private void fetchArtist(Artist artist, ImageDownloader imageDownloader) {
        try {
            final String artistId = infoFetcher.fetchArtistId(artist.name);

            waitRateLimit();

            final ArtistInfo artistInfo = infoFetcher.fetchArtistInfo(artistId);

            final Image image = new Image(imageDownloader.downloadImage(artistInfo.getImageUrl()));
            image.url = artistInfo.getImageUrl();

            artist.imageId = getMusicDao().insertImage(image);

            updateArtist(artist);
        } catch (NetworkClientException e) {
            generateArtistImage(artist);
            updateArtist(artist);
        } catch (NetworkServerException e) {
            // Ignore
        }

        waitRateLimit();
    }

    private void updateArtist(Artist artist) {
        getMusicDao().updateArtist(artist);
    }
}
