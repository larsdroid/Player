package org.willemsens.player.fetchers;

import org.junit.Test;
import org.willemsens.player.fetchers.musicbrainz.MusicbrainzInfoFetcher;

import static org.junit.Assert.assertEquals;

public class MusicbrainzTest {
    private static final String ARTIST_NAME = "…And You Will Know Us by the Trail of Dead";
    private static final String ARTIST_ID = "9c1ff574-2ae4-4fea-881f-83293d0d5881";
    private static final String ALBUM_NAME = "Source Tags & Codes";
    private static final String ALBUM_IMAGE_URI = "http://coverartarchive.org/release/e129e1b3-d9b1-4cd2-9c93-586867fbbe4f/4703734914-500.jpg";
    private static final Integer ALBUM_YEAR = 1995;

    @Test
    public void testFetchArtistId() throws Exception {
        final MusicbrainzInfoFetcher artFetcher = new MusicbrainzInfoFetcher();

        final String artistId = artFetcher.fetchArtistId(ARTIST_NAME);

        assertEquals(ARTIST_ID, artistId);
    }

    @Test
    public void testFetchAlbumInfo() throws Exception {
        final MusicbrainzInfoFetcher infoFetcher = new MusicbrainzInfoFetcher();

        final AlbumInfo albumInfo = infoFetcher.fetchAlbumInfo(ARTIST_NAME, ALBUM_NAME);

        assertEquals(ALBUM_IMAGE_URI, albumInfo.getCoverImageUrl());
        assertEquals(ALBUM_YEAR, albumInfo.getYear());
    }
}
