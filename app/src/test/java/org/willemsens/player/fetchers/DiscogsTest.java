package org.willemsens.player.fetchers;

import org.junit.Test;
import org.willemsens.player.fetchers.discogs.DiscogsInfoFetcher;

import static org.junit.Assert.assertEquals;

public class DiscogsTest {
    private static final String ARTIST_NAME = "The Triffids";
    private static final String ARTIST_ID = "256846";
    private static final String ARTIST_IMAGE_URI = "https://api-img.discogs.com/uZRMLWNNkIZp_o8s4OlAgLakGjc=/500x694/smart/filters:strip_icc():format(jpeg):mode_rgb():quality(90)/discogs-images/A-256846-1147706255.jpeg.jpg";
    private static final String ALBUM_NAME = "Calenture";
    private static final String ALBUM_IMAGE_URI = "https://api-img.discogs.com/IjcY9wnWEV83jhgKCj64gHo-6Oo=/fit-in/600x606/filters:strip_icc():format(jpeg):mode_rgb():quality(90)/discogs-images/R-2376870-1494421833-4828.jpeg.jpg";
    private static final Integer ALBUM_YEAR = 1987;

    @Test
    public void testFetchArtistId() throws Exception {
        final DiscogsInfoFetcher infoFetcher = new DiscogsInfoFetcher();

        final String artistId = infoFetcher.fetchArtistId(ARTIST_NAME);

        assertEquals(ARTIST_ID, artistId);
    }

    @Test
    public void testFetchArtistInfo() throws Exception {
        final DiscogsInfoFetcher infoFetcher = new DiscogsInfoFetcher();

        final ArtistInfo artistInfo = infoFetcher.fetchArtistInfo(ARTIST_ID);

        assertEquals(ARTIST_IMAGE_URI, artistInfo.getImageUrl());
    }

    @Test
    public void testFetchAlbumInfo() throws Exception {
        final DiscogsInfoFetcher infoFetcher = new DiscogsInfoFetcher();

        final AlbumInfo albumInfo = infoFetcher.fetchAlbumInfo(ARTIST_NAME, ALBUM_NAME);

        assertEquals(ALBUM_IMAGE_URI, albumInfo.getCoverImageUrl());
        assertEquals(ALBUM_YEAR, albumInfo.getYear());
    }
}
