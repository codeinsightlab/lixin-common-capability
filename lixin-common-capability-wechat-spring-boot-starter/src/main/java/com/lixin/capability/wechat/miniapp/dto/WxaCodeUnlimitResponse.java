package com.lixin.capability.wechat.miniapp.dto;

import java.util.Base64;

public class WxaCodeUnlimitResponse {
    private byte[] bytes;
    private String contentType;
    private String base64;
    private String errorCode;
    private String errorMessage;
    private String rawErrorMessage;

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
        this.base64 = bytes == null ? null : Base64.getEncoder().encodeToString(bytes);
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getBase64() {
        return base64;
    }

    public void setBase64(String base64) {
        this.base64 = base64;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getRawErrorMessage() {
        return rawErrorMessage;
    }

    public void setRawErrorMessage(String rawErrorMessage) {
        this.rawErrorMessage = rawErrorMessage;
    }
}
