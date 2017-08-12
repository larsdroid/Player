package org.willemsens.player.imagefetchers.discogs;

import org.willemsens.player.imagefetchers.ArtFetcher;
import org.willemsens.player.imagefetchers.discogs.dto.ArtistDetail;
import org.willemsens.player.imagefetchers.discogs.dto.ArtistsResponse;
import org.willemsens.player.imagefetchers.discogs.dto.Release;
import org.willemsens.player.imagefetchers.discogs.dto.ReleaseDetail;
import org.willemsens.player.imagefetchers.discogs.dto.ReleasesResponse;
import org.willemsens.player.model.ImageSource;

import okhttp3.HttpUrl;

public class DiscogsArtFetcher extends ArtFetcher {
    private static final String KEY = "jdLmQoplPtRzRALOXlyv";
    private static final String SECRET = "uvlyrckmvWeAnsdEXpuFWubBsYIMfaBv";

    public Long fetchArtistId(String name) {
        final HttpUrl url = new HttpUrl.Builder()
                .scheme("http")
                .host("api.discogs.com")
                .addPathSegment("database")
                .addPathSegment("search")
                .addQueryParameter("key", KEY)
                .addQueryParameter("secret", SECRET)
                .addQueryParameter("type", "artist")
                .addQueryParameter("q", name)
                .build();

        final String json = fetch(url);
        Long artistId = null;
        if (json != null) {
            ArtistsResponse searchResponse = getGson().fromJson(
                    json,
                    ArtistsResponse.class);
            artistId = searchResponse.getFirstArtistID();
        }

        return artistId;
    }

    public String fetchAlbumImage(String artistName, String albumName) {
        HttpUrl url = new HttpUrl.Builder()
                .scheme("https")
                .host("api.discogs.com")
                .addPathSegment("database")
                .addPathSegment("search")
                .addQueryParameter("key", KEY)
                .addQueryParameter("secret", SECRET)
                .addQueryParameter("type", "release")
                .addQueryParameter("artist", artistName)
                .addQueryParameter("q", albumName)
                .build();

        String json = fetch(url);
        Release[] releases = null;
        if (json != null) {
            ReleasesResponse releasesResponse = getGson().fromJson(
                    json,
                    ReleasesResponse.class);
            releases = releasesResponse.getReleases();
        }

        if (releases != null) {
            for (Release release : releases) {
                url = new HttpUrl.Builder()
                        .scheme("https")
                        .host("api.discogs.com")
                        .addPathSegment("releases")
                        .addPathSegment(String.valueOf(release.getId()))
                        .addQueryParameter("key", KEY)
                        .addQueryParameter("secret", SECRET)
                        .build();

                json = fetch(url);
                if (json != null) {
                    ReleaseDetail releaseDetail = getGson().fromJson(json, ReleaseDetail.class);

                    return releaseDetail.getFirstImageURL();
                }
            }
        }
        return null;
    }

    public String fetchArtistImage(Long artistId) {
        final HttpUrl url = new HttpUrl.Builder()
                .scheme("https")
                .host("api.discogs.com")
                .addPathSegment("artists")
                .addPathSegment(String.valueOf(artistId))
                .addQueryParameter("key", KEY)
                .addQueryParameter("secret", SECRET)
                .build();

        final String json = fetch(url);
        String artistImageUrl = null;
        if (json != null) {
            ArtistDetail artistDetail = getGson().fromJson(json, ArtistDetail.class);

            artistImageUrl = artistDetail.getFirstImageURL();
        }

        return artistImageUrl;
    }

    @Override
    public ImageSource getImageSource() {
        return ImageSource.DISCOGS;
    }
}
