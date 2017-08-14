package org.willemsens.player.imagefetchers;

import android.util.Log;

import com.google.gson.Gson;

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

    protected String fetch(HttpUrl url) {
        try (Response response = httpClient.newCall(getRequest(url)).execute()) {
            if (response.isSuccessful()) {
                return response.body().string();
            } else if (response.code() == 404) {
                Log.v(getClass().getName(), "Not found: "+ url);
            } else {
                Log.e(getClass().getName(), "Fetch issue: " + response.message() + " *** "+ url);
            }
        } catch (IOException e) {
            Log.e(getClass().getName(), e.getMessage());
        }
        return null;
    }

    protected String sanitizeSearchString(String string) {
        if (string.indexOf('[') != -1) {
            string = string.substring(0, string.indexOf('[')).trim();
        }
        return string;
    }

    public abstract Request getRequest(HttpUrl url);
    public abstract String fetchArtistId(String artistName);
    public abstract ArtistInfo fetchArtistInfo(String artistId);
    public abstract AlbumInfo fetchAlbumInfo(String artistName, String albumName);
}
