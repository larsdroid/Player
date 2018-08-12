package org.willemsens.player.fetchers;

public class UnparsedResponse {
    private final int httpStatusCode;
    private final String json;

    public UnparsedResponse(int httpStatusCode, String json) {
        this.httpStatusCode = httpStatusCode;
        this.json = json;
    }

    public int getHttpStatusCode() {
        return httpStatusCode;
    }

    public String getJson() {
        return json;
    }
}
