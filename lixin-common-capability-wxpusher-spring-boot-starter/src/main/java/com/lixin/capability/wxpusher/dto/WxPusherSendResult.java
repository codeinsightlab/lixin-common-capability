package com.lixin.capability.wxpusher.dto;

import java.util.ArrayList;
import java.util.List;

public class WxPusherSendResult {
    private boolean success;
    private String message;
    private Integer providerCode;
    private String providerMessage;
    /**
     * Official message content id when the underlying provider exposes it.
     * In official SDK v3.0.2 mode this may be null because SDK MessageResult does not expose it.
     */
    private Integer messageContentId;
    /**
     * Official send record ids when the underlying provider exposes them.
     * In official SDK v3.0.2 mode this may be empty because SDK MessageResult does not expose them.
     */
    private List<Long> sendRecordIds = new ArrayList<>();
    private String rawResponse;

    public static WxPusherSendResult failure(String message) {
        WxPusherSendResult result = new WxPusherSendResult();
        result.setSuccess(false);
        result.setMessage(message);
        return result;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Integer getProviderCode() { return providerCode; }
    public void setProviderCode(Integer providerCode) { this.providerCode = providerCode; }
    public String getProviderMessage() { return providerMessage; }
    public void setProviderMessage(String providerMessage) { this.providerMessage = providerMessage; }
    public Integer getMessageContentId() { return messageContentId; }
    public void setMessageContentId(Integer messageContentId) { this.messageContentId = messageContentId; }
    public List<Long> getSendRecordIds() { return sendRecordIds; }
    public void setSendRecordIds(List<Long> sendRecordIds) { this.sendRecordIds = sendRecordIds; }
    public String getRawResponse() { return rawResponse; }
    public void setRawResponse(String rawResponse) { this.rawResponse = rawResponse; }
}
