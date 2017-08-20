package org.willemsens.player.imagefetchers;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.Gson;

import org.willemsens.player.exceptions.NetworkClientException;
import org.willemsens.player.exceptions.NetworkServerException;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public abstract class InfoFetcher {
    private final OkHttpClient httpClient;
    private final Gson gson;

    public InfoFetcher() {
        this.httpClient = new OkHttpClient();
        this.gson = new Gson();
    }

    protected Gson getGson() {
        return this.gson;
    }

    protected @NonNull String fetch(HttpUrl url) throws NetworkClientException, NetworkServerException {
        try (Response response = httpClient.newCall(getRequest(url)).execute()) {
            if (response.isSuccessful()) {
                return response.body().string();
            }

            final String errorMessage = "HTTP error '" + response.code() + "' for URL '" + url + "'.";
            Log.e(getClass().getName(), errorMessage);
            if (response.code() >= 400 && response.code() < 500) {
                throw new NetworkClientException(errorMessage);
            } else { // Assuming 500
                throw new NetworkServerException(errorMessage);
            }
        } catch (IOException e) {
            Log.e(getClass().getName(), e.getMessage());
            throw new NetworkClientException(e.getMessage());
        }
    }

    protected String sanitizeSearchString(String string) {
        if (string.indexOf('[') != -1) {
            string = string.substring(0, string.indexOf('[')).trim();
        }
        return string;
    }

    public abstract Request getRequest(HttpUrl url);
    @NonNull public abstract String fetchArtistId(String artistName) throws NetworkClientException, NetworkServerException;
    @NonNull public abstract ArtistInfo fetchArtistInfo(String artistId) throws NetworkClientException, NetworkServerException;
    @NonNull public abstract AlbumInfo fetchAlbumInfo(String artistName, String albumName) throws NetworkClientException, NetworkServerException;
}
