package org.willemsens.player.imagefetchers;

import org.junit.Test;
import org.willemsens.player.imagefetchers.discogs.DiscogsArtFetcher;

import static org.junit.Assert.assertEquals;

public class DiscogsTest {
    private static final String ARTIST_NAME = "The Triffids";
    private static final Long ARTIST_ID = 256846L;
    private static final String ARTIST_IMAGE_URI = "https://api-img.discogs.com/uZRMLWNNkIZp_o8s4OlAgLakGjc=/500x694/smart/filters:strip_icc():format(jpeg):mode_rgb():quality(90)/discogs-images/A-256846-1147706255.jpeg.jpg";
    private static final String ALBUM_NAME = "Calenture";
    private static final String ALBUM_IMAGE_URI = "https://api-img.discogs.com/IjcY9wnWEV83jhgKCj64gHo-6Oo=/fit-in/600x606/filters:strip_icc():format(jpeg):mode_rgb():quality(90)/discogs-images/R-2376870-1494421833-4828.jpeg.jpg";


    @Test
    public void testFetchArtistId() throws Exception {
        final DiscogsArtFetcher artFetcher = new DiscogsArtFetcher();

        Long artistId = artFetcher.fetchArtistId(ARTIST_NAME);

        assertEquals(ARTIST_ID, artistId);
    }

    @Test
    public void testFetchArtistImage() throws Exception {
        final DiscogsArtFetcher artFetcher = new DiscogsArtFetcher();

        String artistImage = artFetcher.fetchArtistImage(ARTIST_ID);

        assertEquals(ARTIST_IMAGE_URI, artistImage);
    }

    @Test
    public void testFetchAlbumImage() throws Exception {
        final DiscogsArtFetcher artFetcher = new DiscogsArtFetcher();

        String albumImage = artFetcher.fetchAlbumImage(ARTIST_NAME, ALBUM_NAME);

        assertEquals(ALBUM_IMAGE_URI, albumImage);
    }
}
