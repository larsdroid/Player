package org.willemsens.player.fetchers.discogs;

import android.support.annotation.NonNull;

import org.willemsens.player.exceptions.NetworkClientException;
import org.willemsens.player.exceptions.NetworkServerException;
import org.willemsens.player.fetchers.AlbumInfo;
import org.willemsens.player.fetchers.ArtistInfo;
import org.willemsens.player.fetchers.InfoFetcher;
import org.willemsens.player.fetchers.UnparsedResponse;
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

        final UnparsedResponse unparsedResponse = fetch(url);
        final ArtistsResponse searchResponse = getGson().fromJson(
                unparsedResponse.getJson(),
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

        UnparsedResponse unparsedResponse = fetch(url);
        final ReleasesResponse releasesResponse = getGson().fromJson(
                unparsedResponse.getJson(),
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

                unparsedResponse = fetch(url);
                if (unparsedResponse.getHttpStatusCode() != 404) {
                    final ReleaseDetail releaseDetail = getGson().fromJson(unparsedResponse.getJson(), ReleaseDetail.class);

                    return new AlbumInfo(
                            releaseDetail.getFirstImageURL(),
                            releasesResponse.getOldestReleaseYear());
                }
            }
        }

        return new AlbumInfo(
                null,
                releasesResponse.getOldestReleaseYear());
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

        final UnparsedResponse unparsedResponse = fetch(url);
        final ArtistDetail artistDetail = getGson().fromJson(unparsedResponse.getJson(), ArtistDetail.class);
        if (artistDetail != null) {
            final String imageURL = artistDetail.getFirstImageURL();
            if (imageURL != null) {
                return new ArtistInfo(imageURL);
            }
        }

        throw new NetworkClientException("No artist info found for artist ID '" + artistId + "'.");
    }
}
