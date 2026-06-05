package com.lixin.capability.wxpusher.dto;

import java.util.ArrayList;
import java.util.List;

public class WxPusherResponse {
    private Integer code;
    private String msg;
    private Boolean success;
    private List<Item> data = new ArrayList<>();
    private String rawResponse;

    public Integer getCode() { return code; }
    public void setCode(Integer code) { this.code = code; }
    public String getMsg() { return msg; }
    public void setMsg(String msg) { this.msg = msg; }
    public Boolean getSuccess() { return success; }
    public void setSuccess(Boolean success) { this.success = success; }
    public List<Item> getData() { return data; }
    public void setData(List<Item> data) { this.data = data; }
    public String getRawResponse() { return rawResponse; }
    public void setRawResponse(String rawResponse) { this.rawResponse = rawResponse; }

    public static class Item {
        private String uid;
        private Integer topicId;
        private Integer messageId;
        private Integer messageContentId;
        private Long sendRecordId;
        private Integer code;
        private String status;

        public String getUid() { return uid; }
        public void setUid(String uid) { this.uid = uid; }
        public Integer getTopicId() { return topicId; }
        public void setTopicId(Integer topicId) { this.topicId = topicId; }
        public Integer getMessageId() { return messageId; }
        public void setMessageId(Integer messageId) { this.messageId = messageId; }
        public Integer getMessageContentId() { return messageContentId; }
        public void setMessageContentId(Integer messageContentId) { this.messageContentId = messageContentId; }
        public Long getSendRecordId() { return sendRecordId; }
        public void setSendRecordId(Long sendRecordId) { this.sendRecordId = sendRecordId; }
        public Integer getCode() { return code; }
        public void setCode(Integer code) { this.code = code; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}
