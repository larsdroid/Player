package org.willemsens.player.exceptions;

public class NetworkServerException extends Exception {
    public NetworkServerException(String message) {
        super(message);
    }

    public NetworkServerException(String message, Throwable cause) {
        super(message, cause);
    }
}
