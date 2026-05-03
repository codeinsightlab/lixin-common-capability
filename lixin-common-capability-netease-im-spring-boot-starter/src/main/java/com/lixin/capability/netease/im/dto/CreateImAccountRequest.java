package com.lixin.capability.netease.im.dto;

public class CreateImAccountRequest {
    private String accountId;
    private String name;
    private String avatar;
    private String extensionJson;

    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public String getExtensionJson() { return extensionJson; }
    public void setExtensionJson(String extensionJson) { this.extensionJson = extensionJson; }
}
