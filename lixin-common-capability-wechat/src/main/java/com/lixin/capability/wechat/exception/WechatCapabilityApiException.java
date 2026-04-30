package com.lixin.capability.wechat.exception;

public class WechatCapabilityApiException extends WechatCapabilityException {
    private final String code;
    private final String rawBody;

    public WechatCapabilityApiException(String message) {
        this(null, message, null, null);
    }

    public WechatCapabilityApiException(String code, String message, String rawBody, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.rawBody = rawBody;
    }

    public String getCode() { return code; }
    public String getRawBody() { return rawBody; }
}
