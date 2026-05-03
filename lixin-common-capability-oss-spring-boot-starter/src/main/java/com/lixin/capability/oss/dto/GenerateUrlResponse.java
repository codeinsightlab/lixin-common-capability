package com.lixin.capability.oss.dto;

import java.util.Date;

public class GenerateUrlResponse {
    private String objectKey;
    private String url;
    private Date expireAt;
    private String provider;

    public String getObjectKey() { return objectKey; }
    public void setObjectKey(String objectKey) { this.objectKey = objectKey; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public Date getExpireAt() { return expireAt; }
    public void setExpireAt(Date expireAt) { this.expireAt = expireAt; }
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
}
