package org.willemsens.player.imagefetchers.musicbrainz;

import okhttp3.HttpUrl;
import org.willemsens.player.imagefetchers.ArtFetcher;
import org.willemsens.player.imagefetchers.musicbrainz.dto.ArtistsResponse;
import org.willemsens.player.imagefetchers.musicbrainz.dto.ImagesReponse;
import org.willemsens.player.imagefetchers.musicbrainz.dto.Release;
import org.willemsens.player.imagefetchers.musicbrainz.dto.ReleasesResponse;

public class MusicbrainzArtFetcher extends ArtFetcher {
    public String fetchArtistId(String name) {
        final HttpUrl url = new HttpUrl.Builder()
                .scheme("http")
                .host("musicbrainz.org")
                .addPathSegment("ws")
                .addPathSegment("2")
                .addPathSegment("artist")
                .addQueryParameter("query", "artist:" + name)
                .build();
        final String json = fetch(url);
        String artistId = null;
        if (json != null) {
            ArtistsResponse artistsResponse = getGson().fromJson(
                    json,
                    ArtistsResponse.class);
            artistId = artistsResponse.getFirstArtistID();
        }

        return artistId;
    }

    public String fetchLargeThumbnail(String artistId, String albumName) {
        HttpUrl url = new HttpUrl.Builder()
                .scheme("http")
                .host("musicbrainz.org")
                .addPathSegment("ws")
                .addPathSegment("2")
                .addPathSegment("release")
                .addQueryParameter("query", "release:" + albumName + " AND arid:" + artistId)
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
                        .scheme("http")
                        .host("coverartarchive.org")
                        .addPathSegment("release")
                        .addPathSegment(release.getId())
                        .build();

                json = fetch(url);
                if (json != null) {
                    ImagesReponse imagesReponse = getGson().fromJson(json, ImagesReponse.class);

                    return imagesReponse.getFirstLargeThumbnail();
                }
            }
        }
        return null;
    }
}
