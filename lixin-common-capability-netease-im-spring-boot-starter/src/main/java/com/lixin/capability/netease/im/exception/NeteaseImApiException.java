package com.lixin.capability.netease.im.exception;

public class NeteaseImApiException extends NeteaseImCapabilityException {
    private final String providerCode;
    private final String rawResponse;

    public NeteaseImApiException(String message) {
        this(message, null, null, null);
    }

    public NeteaseImApiException(String message, Throwable cause) {
        this(message, null, null, cause);
    }

    public NeteaseImApiException(String message, String providerCode, String rawResponse) {
        this(message, providerCode, rawResponse, null);
    }

    public NeteaseImApiException(String message, String providerCode, String rawResponse, Throwable cause) {
        super(message, cause);
        this.providerCode = providerCode;
        this.rawResponse = rawResponse;
    }

    public String getProviderCode() { return providerCode; }
    public String getRawResponse() { return rawResponse; }
}
