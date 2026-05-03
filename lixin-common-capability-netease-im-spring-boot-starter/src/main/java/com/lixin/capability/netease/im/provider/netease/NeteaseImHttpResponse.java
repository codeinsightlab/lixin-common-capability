package com.lixin.capability.netease.im.provider.netease;

import java.util.List;
import java.util.Map;

public class NeteaseImHttpResponse {
    private final int statusCode;
    private final String body;
    private final Map<String, List<String>> headers;

    public NeteaseImHttpResponse(int statusCode, String body, Map<String, List<String>> headers) {
        this.statusCode = statusCode;
        this.body = body;
        this.headers = headers;
    }

    public int getStatusCode() { return statusCode; }
    public String getBody() { return body; }
    public Map<String, List<String>> getHeaders() { return headers; }
}
