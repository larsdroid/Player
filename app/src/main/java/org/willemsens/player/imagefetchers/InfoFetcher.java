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
            } else {
                Log.e(getClass().getName(), "Fetch issue: " + response.message() + " *** "+ response.body());
            }
        } catch (IOException e) {
            Log.e(getClass().getName(), e.getMessage());
        }
        return null;
    }

    public abstract Request getRequest(HttpUrl url);
    public abstract String fetchArtistId(String artistName);
    public abstract ArtistInfo fetchArtistInfo(String artistId);
    public abstract AlbumInfo fetchAlbumInfo(String artistName, String albumName);
}
