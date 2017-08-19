package org.willemsens.player.imagefetchers;

import android.support.annotation.NonNull;
import android.util.Log;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.io.IOUtils;
import org.willemsens.player.exceptions.NetworkClientException;
import org.willemsens.player.exceptions.NetworkServerException;

import java.io.IOException;
import java.io.InputStream;

public class ImageDownloader {
    private final OkHttpClient client;

    public ImageDownloader() {
        this.client = new OkHttpClient();
    }

    public @NonNull byte[] downloadImage(String imageURI) throws NetworkClientException, NetworkServerException {
        Request request = new Request.Builder()
                .url(imageURI)
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                InputStream inputStream = response.body().byteStream();
                return IOUtils.toByteArray(inputStream);
            }

            final String errorMessage = "HTTP error '" + response.code() + "' for URL '" + imageURI + "'.";
            Log.v(getClass().getName(), errorMessage);
            if (response.code() >= 400 && response.code() < 500) {
                throw new NetworkClientException(errorMessage);
            } else { // Assuming 500
                throw new NetworkServerException(errorMessage);
            }
        } catch (IOException e) {
            Log.e(getClass().getName(), "Image download failed for '" + imageURI + "'.");
            throw new NetworkClientException(e.getMessage());
        }
    }
}
