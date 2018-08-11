package org.willemsens.player.exceptions;

public class NetworkClientException extends Exception {
    public NetworkClientException(String message) {
        super(message);
    }

    public NetworkClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
