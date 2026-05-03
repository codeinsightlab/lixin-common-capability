package com.lixin.capability.netease.im.dto;

public class VerifyImCallbackRequest {
    private String appKey;
    private String curTime;
    private String bodyMd5;
    private String checkSum;
    private String body;

    public String getAppKey() { return appKey; }
    public void setAppKey(String appKey) { this.appKey = appKey; }
    public String getCurTime() { return curTime; }
    public void setCurTime(String curTime) { this.curTime = curTime; }
    public String getBodyMd5() { return bodyMd5; }
    public void setBodyMd5(String bodyMd5) { this.bodyMd5 = bodyMd5; }
    public String getCheckSum() { return checkSum; }
    public void setCheckSum(String checkSum) { this.checkSum = checkSum; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
}
