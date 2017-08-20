package org.willemsens.player.imagefetchers.musicbrainz;

import android.support.annotation.NonNull;

import org.willemsens.player.exceptions.NetworkClientException;
import org.willemsens.player.exceptions.NetworkServerException;
import org.willemsens.player.exceptions.PlayerException;
import org.willemsens.player.imagefetchers.AlbumInfo;
import org.willemsens.player.imagefetchers.ArtistInfo;
import org.willemsens.player.imagefetchers.InfoFetcher;
import org.willemsens.player.imagefetchers.musicbrainz.dto.ArtistsResponse;
import org.willemsens.player.imagefetchers.musicbrainz.dto.ImagesReponse;
import org.willemsens.player.imagefetchers.musicbrainz.dto.Release;
import org.willemsens.player.imagefetchers.musicbrainz.dto.ReleasesResponse;

import okhttp3.HttpUrl;
import okhttp3.Request;

public class MusicbrainzInfoFetcher extends InfoFetcher {
    @Override
    public Request getRequest(HttpUrl url) {
        return new Request.Builder()
                .url(url)
                .addHeader("User-Agent", "Player/0.1 ( https://github.com/larsdroid/Player )")
                .addHeader("Accept", "application/json")
                .build();
    }

    @Override
    @NonNull
    public String fetchArtistId(String artistName) throws NetworkClientException, NetworkServerException {
        final HttpUrl url = new HttpUrl.Builder()
                .scheme("http")
                .host("musicbrainz.org")
                .addPathSegment("ws")
                .addPathSegment("2")
                .addPathSegment("artist")
                .addQueryParameter("query", "artist:" + sanitizeSearchString(artistName))
                .build();
        final String json = fetch(url);
        final ArtistsResponse artistsResponse = getGson().fromJson(
                json,
                ArtistsResponse.class);
        return artistsResponse.getFirstArtistID();
    }

    @Override
    @NonNull
    public AlbumInfo fetchAlbumInfo(String artistName, String albumName) throws NetworkClientException, NetworkServerException {
        HttpUrl url = new HttpUrl.Builder()
                .scheme("http")
                .host("musicbrainz.org")
                .addPathSegment("ws")
                .addPathSegment("2")
                .addPathSegment("release")
                //.addQueryParameter("query", "release:" + albumName + " AND arid:" + artistId)
                .addQueryParameter("query", "release:" + sanitizeSearchString(albumName) + " AND artist:" + sanitizeSearchString(artistName))
                .build();

        String json = fetch(url);
        final ReleasesResponse releasesResponse = getGson().fromJson(
                json,
                ReleasesResponse.class);
        final Release[] releases = releasesResponse.getReleases();

        if (releases != null) {
            for (Release release : releases) {
                url = new HttpUrl.Builder()
                        .scheme("http")
                        .host("coverartarchive.org")
                        .addPathSegment("release")
                        .addPathSegment(release.getId())
                        .build();

                try {
                    json = fetch(url);
                    ImagesReponse imagesReponse = getGson().fromJson(json, ImagesReponse.class);

                    return new AlbumInfo(
                            imagesReponse.getFirstLargeThumbnail(),
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
    public ArtistInfo fetchArtistInfo(String artistId) {
        throw new PlayerException("Fetching artist images is not supported by Musicbrainz.");
    }

    @Override
    protected String sanitizeSearchString(String string) {
        string = super.sanitizeSearchString(string);

        // Remove ending "Disc X"
        string = string.replaceAll("(?i)\\s+disc\\s+\\d{1,2}$", "");

        // Just a few Apache Lucene escapes
        string = string.replaceAll("\\(", "\\\\(");
        string = string.replaceAll("\\)", "\\\\)");
        return string.replaceAll("/", "\\\\/");
    }
}
