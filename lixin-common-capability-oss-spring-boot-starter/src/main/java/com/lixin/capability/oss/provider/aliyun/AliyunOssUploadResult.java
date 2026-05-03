package com.lixin.capability.oss.provider.aliyun;

public class AliyunOssUploadResult {
    private String etag;
    private String url;
    private String rawResponse;

    public String getEtag() { return etag; }
    public void setEtag(String etag) { this.etag = etag; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getRawResponse() { return rawResponse; }
    public void setRawResponse(String rawResponse) { this.rawResponse = rawResponse; }
}
