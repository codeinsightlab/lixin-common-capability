package com.lixin.capability.wechat.subscribe.dto;

public class SubscribeMessageSendResponse {
    private boolean success;
    private String rawResponse;

    public boolean getSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getRawResponse() {
        return rawResponse;
    }

    public void setRawResponse(String rawResponse) {
        this.rawResponse = rawResponse;
    }

}
