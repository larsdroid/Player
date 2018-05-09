package org.willemsens.player.fetchers;

import android.net.TrafficStats;
import android.support.annotation.NonNull;
import android.util.Log;
import com.google.gson.Gson;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.willemsens.player.exceptions.NetworkClientException;
import org.willemsens.player.exceptions.NetworkServerException;

import javax.net.SocketFactory;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;

public abstract class InfoFetcher {
    private final OkHttpClient httpClient;
    private final Gson gson;

    // Crazy workaround for https://github.com/square/okhttp/issues/3537
    private final class DelegatingSocketFactory extends SocketFactory {
        private final SocketFactory theRealSocketFactory;

        DelegatingSocketFactory() {
            this.theRealSocketFactory = SocketFactory.getDefault();
        }

        private Socket configureSocket(Socket socket) {
            try {
                TrafficStats.tagSocket(socket);
            } catch (SocketException e) {
                Log.e(getClass().getName(), e.getMessage());
            }
            return socket;
        }

        @Override
        public Socket createSocket(String host, int port) throws IOException {
            return configureSocket(this.theRealSocketFactory.createSocket(host, port));
        }

        @Override
        public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException {
            return configureSocket(this.theRealSocketFactory.createSocket(host, port, localHost, localPort));
        }

        @Override
        public Socket createSocket(InetAddress host, int port) throws IOException {
            return configureSocket(this.theRealSocketFactory.createSocket(host, port));
        }

        @Override
        public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
            return configureSocket(this.theRealSocketFactory.createSocket(address, port, localAddress, localPort));
        }
    }

    public InfoFetcher() {
        // Crazy workaround for https://github.com/square/okhttp/issues/3537
        this.httpClient = new OkHttpClient.Builder().socketFactory(new DelegatingSocketFactory()).build();

        //this.httpClient = new OkHttpClient();
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
