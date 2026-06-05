package com.lixin.capability.wxpusher.dto;

import java.util.ArrayList;
import java.util.List;

public class WxPusherSendRequest {
    private String title;
    private String content;
    private Integer contentType;
    private String url;
    private List<String> uids = new ArrayList<>();
    private List<Integer> topicIds = new ArrayList<>();

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Integer getContentType() { return contentType; }
    public void setContentType(Integer contentType) { this.contentType = contentType; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public List<String> getUids() { return uids; }
    public void setUids(List<String> uids) { this.uids = uids; }
    public List<Integer> getTopicIds() { return topicIds; }
    public void setTopicIds(List<Integer> topicIds) { this.topicIds = topicIds; }
}
