package org.willemsens.player.imagefetchers;

import org.junit.Test;
import org.willemsens.player.imagefetchers.musicbrainz.MusicbrainzArtFetcher;

import static org.junit.Assert.assertEquals;

public class MusicbrainzTest {
    private static final String ARTIST_NAME = "Wilco";
    private static final String ARTIST_ID = "9e53f84d-ef44-4c16-9677-5fd4d78cbd7d";
    private static final String ALBUM_NAME = "Summerteeth";
    private static final String ALBUM_IMAGE_URI = "http://coverartarchive.org/release/312cb54d-9c3c-4181-8f13-1345d87f5810/9752090182-500.jpg";

    @Test
    public void testFetchArtistId() throws Exception {
        final MusicbrainzArtFetcher artFetcher = new MusicbrainzArtFetcher();

        final String artistId = artFetcher.fetchArtistId(ARTIST_NAME);

        assertEquals(ARTIST_ID, artistId);
    }

    @Test
    public void testFetchLargeThumbnail() throws Exception {
        final MusicbrainzArtFetcher artFetcher = new MusicbrainzArtFetcher();

        final String largeThumbnail = artFetcher.fetchLargeThumbnail(ARTIST_ID, ALBUM_NAME);

        assertEquals(ALBUM_IMAGE_URI, largeThumbnail);
    }
}
