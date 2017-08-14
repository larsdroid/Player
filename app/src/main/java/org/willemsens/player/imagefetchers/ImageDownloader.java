package org.willemsens.player.imagefetchers;

import android.util.Log;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ImageDownloader {
    private final OkHttpClient client;

    public ImageDownloader() {
        this.client = new OkHttpClient();
    }

    public byte[] downloadImage(String imageURI) {
        Request request = new Request.Builder()
                .url(imageURI)
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                InputStream inputStream = response.body().byteStream();
                return IOUtils.toByteArray(inputStream);
            } else {
                Log.e(getClass().getName(), "Image download failed for '" + imageURI + "'.");
            }
        } catch (IOException e) {
            Log.e(getClass().getName(), "Image download failed for '" + imageURI + "'.");
            Log.e(getClass().getName(), "Image download failed: " + e.getMessage());
        }
        return null;
    }
}
