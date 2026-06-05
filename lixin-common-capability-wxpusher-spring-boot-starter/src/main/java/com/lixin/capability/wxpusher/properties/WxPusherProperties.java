package com.lixin.capability.wxpusher.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "lixin.capability.wxpusher")
public class WxPusherProperties {
    private boolean enabled = false;
    private String appToken;
    /**
     * Retained for configuration compatibility.
     * In official SDK mode this value is not applied because the SDK owns its base URL.
     */
    private String baseUrl = "https://wxpusher.zjiecode.com/api";
    /**
     * Retained for configuration compatibility.
     * In official SDK mode this value is not applied because the SDK owns HTTP timeouts.
     */
    private int connectTimeoutMs = 3000;
    /**
     * Retained for configuration compatibility.
     * In official SDK mode this value is not applied because the SDK owns HTTP timeouts.
     */
    private int readTimeoutMs = 5000;

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getAppToken() { return appToken; }
    public void setAppToken(String appToken) { this.appToken = appToken; }
    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public int getConnectTimeoutMs() { return connectTimeoutMs; }
    public void setConnectTimeoutMs(int connectTimeoutMs) { this.connectTimeoutMs = connectTimeoutMs; }
    public int getReadTimeoutMs() { return readTimeoutMs; }
    public void setReadTimeoutMs(int readTimeoutMs) { this.readTimeoutMs = readTimeoutMs; }
}
