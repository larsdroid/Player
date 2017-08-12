package org.willemsens.player.imagefetchers;

import com.google.gson.Gson;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.willemsens.player.model.ImageSource;

import java.io.IOException;

public abstract class ArtFetcher {
    private final OkHttpClient httpClient;
    private final Gson gson;

    public ArtFetcher() {
        this.httpClient = new OkHttpClient();
        this.gson = new Gson();
    }

    protected Gson getGson() {
        return this.gson;
    }

    protected String fetch(HttpUrl url) {
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Accept", "application/json")
                .build();
        try (Response response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                return response.body().string();
            }
            // TODO: handle response codes other than 200
        } catch (IOException e) {
            // TODO: handle this differently?
            e.printStackTrace();
        }
        return null;
    }

    public abstract String fetchArtistId(String artistName);
    public abstract ArtistInfo fetchArtistInfo(String artistId);
    public abstract AlbumInfo fetchAlbumInfo(String artistName, String albumName);
    public abstract ImageSource getImageSource();
}
