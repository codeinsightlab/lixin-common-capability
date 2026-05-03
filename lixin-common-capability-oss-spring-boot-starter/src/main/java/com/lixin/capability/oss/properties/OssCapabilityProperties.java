package com.lixin.capability.oss.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "lixin.capability.oss")
public class OssCapabilityProperties {
    private boolean enabled = false;
    private String provider;
    private String endpoint;
    private String bucketName;
    private String accessKeyId;
    private String accessKeySecret;
    private long defaultExpireSeconds = 3600;
    private String objectKeyPrefix;

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
    public String getBucketName() { return bucketName; }
    public void setBucketName(String bucketName) { this.bucketName = bucketName; }
    public String getAccessKeyId() { return accessKeyId; }
    public void setAccessKeyId(String accessKeyId) { this.accessKeyId = accessKeyId; }
    public String getAccessKeySecret() { return accessKeySecret; }
    public void setAccessKeySecret(String accessKeySecret) { this.accessKeySecret = accessKeySecret; }
    public long getDefaultExpireSeconds() { return defaultExpireSeconds; }
    public void setDefaultExpireSeconds(long defaultExpireSeconds) { this.defaultExpireSeconds = defaultExpireSeconds; }
    public String getObjectKeyPrefix() { return objectKeyPrefix; }
    public void setObjectKeyPrefix(String objectKeyPrefix) { this.objectKeyPrefix = objectKeyPrefix; }
}
