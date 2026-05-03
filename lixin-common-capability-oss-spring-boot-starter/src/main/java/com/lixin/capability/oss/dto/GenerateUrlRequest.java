package com.lixin.capability.oss.dto;

public class GenerateUrlRequest {
    private String objectKey;
    private Long expireSeconds;

    public String getObjectKey() { return objectKey; }
    public void setObjectKey(String objectKey) { this.objectKey = objectKey; }
    public Long getExpireSeconds() { return expireSeconds; }
    public void setExpireSeconds(Long expireSeconds) { this.expireSeconds = expireSeconds; }
}
