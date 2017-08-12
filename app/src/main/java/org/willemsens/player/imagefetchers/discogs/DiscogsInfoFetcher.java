package org.willemsens.player.imagefetchers.discogs;

import org.willemsens.player.imagefetchers.AlbumInfo;
import org.willemsens.player.imagefetchers.InfoFetcher;
import org.willemsens.player.imagefetchers.ArtistInfo;
import org.willemsens.player.imagefetchers.discogs.dto.ArtistDetail;
import org.willemsens.player.imagefetchers.discogs.dto.ArtistsResponse;
import org.willemsens.player.imagefetchers.discogs.dto.Release;
import org.willemsens.player.imagefetchers.discogs.dto.ReleaseDetail;
import org.willemsens.player.imagefetchers.discogs.dto.ReleasesResponse;
import org.willemsens.player.model.InfoSource;

import okhttp3.HttpUrl;
import okhttp3.Request;

public class DiscogsInfoFetcher extends InfoFetcher {
    private static final String KEY = "jdLmQoplPtRzRALOXlyv";
    private static final String SECRET = "uvlyrckmvWeAnsdEXpuFWubBsYIMfaBv";

    @Override
    public Request getRequest(HttpUrl url) {
        return new Request.Builder()
                .url(url)
                .addHeader("Accept", "application/json")
                .build();
    }

    @Override
    public String fetchArtistId(String artistName) {
        final HttpUrl url = new HttpUrl.Builder()
                .scheme("http")
                .host("api.discogs.com")
                .addPathSegment("database")
                .addPathSegment("search")
                .addQueryParameter("key", KEY)
                .addQueryParameter("secret", SECRET)
                .addQueryParameter("type", "artist")
                .addQueryParameter("q", artistName)
                .build();

        final String json = fetch(url);
        Long artistId = null;
        if (json != null) {
            ArtistsResponse searchResponse = getGson().fromJson(
                    json,
                    ArtistsResponse.class);
            artistId = searchResponse.getFirstArtistID();
        }

        return artistId == null ? null : String.valueOf(artistId);
    }

    @Override
    public AlbumInfo fetchAlbumInfo(String artistName, String albumName) {
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
        if (json != null) {
            ReleasesResponse releasesResponse = getGson().fromJson(
                    json,
                    ReleasesResponse.class);
            Release[] releases = releasesResponse.getReleases();

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

                        return new AlbumInfo(
                                InfoSource.DISCOGS,
                                releaseDetail.getFirstImageURL(),
                                releasesResponse.getOldestReleaseYear());
                    }
                }
            }
        }

        return null;
    }

    @Override
    public ArtistInfo fetchArtistInfo(String artistId) {
        final HttpUrl url = new HttpUrl.Builder()
                .scheme("https")
                .host("api.discogs.com")
                .addPathSegment("artists")
                .addPathSegment(artistId)
                .addQueryParameter("key", KEY)
                .addQueryParameter("secret", SECRET)
                .build();

        final String json = fetch(url);
        if (json != null) {
            ArtistDetail artistDetail = getGson().fromJson(json, ArtistDetail.class);
            return new ArtistInfo(InfoSource.DISCOGS, artistDetail.getFirstImageURL());
        }

        return null;
    }
}
