package com.lixin.capability.netease.im.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "lixin.capability.netease.im")
public class NeteaseImCapabilityProperties {
    private boolean enabled = false;
    private String appKey;
    private String appSecret;
    private int timeoutMillis = 10000;
    private String baseUrl = "https://api.yunxinapi.com/nimserver";

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getAppKey() { return appKey; }
    public void setAppKey(String appKey) { this.appKey = appKey; }
    public String getAppSecret() { return appSecret; }
    public void setAppSecret(String appSecret) { this.appSecret = appSecret; }
    public int getTimeoutMillis() { return timeoutMillis; }
    public void setTimeoutMillis(int timeoutMillis) { this.timeoutMillis = timeoutMillis; }
    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
}
