package com.lixin.capability.wechat.subscribe.dto;

import java.util.List;

public class SubscribeMessageSendRequest {
    private String toUser;
    private String templateId;
    private String page;
    private String miniProgramState;
    private String lang;
    private List<SubscribeMessageData> data;

    public String getToUser() {
        return toUser;
    }

    public void setToUser(String toUser) {
        this.toUser = toUser;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page;
    }

    public String getMiniProgramState() {
        return miniProgramState;
    }

    public void setMiniProgramState(String miniProgramState) {
        this.miniProgramState = miniProgramState;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public List<SubscribeMessageData> getData() {
        return data;
    }

    public void setData(List<SubscribeMessageData> data) {
        this.data = data;
    }

}
