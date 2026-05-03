package com.lixin.capability.netease.im.dto;

public class VerifyImCallbackResponse {
    private boolean verified;
    private String reason;
    private String providerCode;
    private String requestId;

    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getProviderCode() { return providerCode; }
    public void setProviderCode(String providerCode) { this.providerCode = providerCode; }
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
}
