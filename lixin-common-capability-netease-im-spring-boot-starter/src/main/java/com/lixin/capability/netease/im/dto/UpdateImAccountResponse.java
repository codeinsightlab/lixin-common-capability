package com.lixin.capability.netease.im.dto;

public class UpdateImAccountResponse {
    private String accountId;
    private String providerCode;
    private String providerMessage;
    private String requestId;
    private String rawResponse;

    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }
    public String getProviderCode() { return providerCode; }
    public void setProviderCode(String providerCode) { this.providerCode = providerCode; }
    public String getProviderMessage() { return providerMessage; }
    public void setProviderMessage(String providerMessage) { this.providerMessage = providerMessage; }
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    public String getRawResponse() { return rawResponse; }
    public void setRawResponse(String rawResponse) { this.rawResponse = rawResponse; }
}
