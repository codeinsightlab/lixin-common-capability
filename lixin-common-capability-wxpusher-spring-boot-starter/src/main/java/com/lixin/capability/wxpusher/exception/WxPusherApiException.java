package com.lixin.capability.wxpusher.exception;

public class WxPusherApiException extends WxPusherException {
    private final Integer providerCode;
    private final String rawResponse;

    public WxPusherApiException(String message, Integer providerCode, String rawResponse) {
        super(message);
        this.providerCode = providerCode;
        this.rawResponse = rawResponse;
    }

    public WxPusherApiException(String message, Throwable cause) {
        super(message, cause);
        this.providerCode = null;
        this.rawResponse = null;
    }

    public Integer getProviderCode() { return providerCode; }
    public String getRawResponse() { return rawResponse; }
}
