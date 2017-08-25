package org.willemsens.player.fetchers.discogs;

import android.support.annotation.NonNull;

import org.willemsens.player.exceptions.NetworkClientException;
import org.willemsens.player.exceptions.NetworkServerException;
import org.willemsens.player.fetchers.AlbumInfo;
import org.willemsens.player.fetchers.ArtistInfo;
import org.willemsens.player.fetchers.InfoFetcher;
import org.willemsens.player.fetchers.discogs.dto.ArtistDetail;
import org.willemsens.player.fetchers.discogs.dto.ArtistsResponse;
import org.willemsens.player.fetchers.discogs.dto.Release;
import org.willemsens.player.fetchers.discogs.dto.ReleaseDetail;
import org.willemsens.player.fetchers.discogs.dto.ReleasesResponse;

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
    @NonNull
    public String fetchArtistId(String artistName) throws NetworkClientException, NetworkServerException {
        final HttpUrl url = new HttpUrl.Builder()
                .scheme("http")
                .host("api.discogs.com")
                .addPathSegment("database")
                .addPathSegment("search")
                .addQueryParameter("key", KEY)
                .addQueryParameter("secret", SECRET)
                .addQueryParameter("type", "artist")
                .addQueryParameter("q", sanitizeSearchString(artistName))
                .build();

        final String json = fetch(url);
        final ArtistsResponse searchResponse = getGson().fromJson(
                json,
                ArtistsResponse.class);
        final Long artistId = searchResponse.getFirstArtistID();

        if (artistId != null) {
            return String.valueOf(artistId);
        }

        throw new NetworkClientException("No artist ID found for artist '" + artistName + "'.");
    }

    @Override
    @NonNull
    public AlbumInfo fetchAlbumInfo(String artistName, String albumName) throws NetworkClientException, NetworkServerException {
        HttpUrl url = new HttpUrl.Builder()
                .scheme("https")
                .host("api.discogs.com")
                .addPathSegment("database")
                .addPathSegment("search")
                .addQueryParameter("key", KEY)
                .addQueryParameter("secret", SECRET)
                .addQueryParameter("type", "release")
                .addQueryParameter("artist", sanitizeSearchString(artistName))
                .addQueryParameter("q", sanitizeSearchString(albumName))
                .build();

        String json = fetch(url);
        final ReleasesResponse releasesResponse = getGson().fromJson(
                json,
                ReleasesResponse.class);
        final Release[] releases = releasesResponse.getReleases();

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

                try {
                    json = fetch(url);
                    final ReleaseDetail releaseDetail = getGson().fromJson(json, ReleaseDetail.class);

                    return new AlbumInfo(
                            releaseDetail.getFirstImageURL(),
                            releasesResponse.getOldestReleaseYear());
                } catch (NetworkClientException e) {
                    // Ignore and try the next one...
                }
            }
        }

        throw new NetworkClientException("No album info found for artist '" + artistName + "' album '" + albumName + "'.");
    }

    @Override
    @NonNull
    public ArtistInfo fetchArtistInfo(String artistId) throws NetworkClientException, NetworkServerException {
        final HttpUrl url = new HttpUrl.Builder()
                .scheme("https")
                .host("api.discogs.com")
                .addPathSegment("artists")
                .addPathSegment(artistId)
                .addQueryParameter("key", KEY)
                .addQueryParameter("secret", SECRET)
                .build();

        final String json = fetch(url);
        final ArtistDetail artistDetail = getGson().fromJson(json, ArtistDetail.class);
        final String imageURL = artistDetail.getFirstImageURL();
        if (imageURL != null) {
                return new ArtistInfo(imageURL);
        }

        throw new NetworkClientException("No artist info found for artist ID '" + artistId + "'.");
    }
}
