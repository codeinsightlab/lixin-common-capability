package com.lixin.capability.wechat.miniapp.dto;

public class WxaCodeUnlimitRequest {
    private String scene;
    private String page;
    private Boolean checkPath;
    private String envVersion;
    private Integer width;

    public String getScene() {
        return scene;
    }

    public void setScene(String scene) {
        this.scene = scene;
    }

    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page;
    }

    public Boolean getCheckPath() {
        return checkPath;
    }

    public void setCheckPath(Boolean checkPath) {
        this.checkPath = checkPath;
    }

    public String getEnvVersion() {
        return envVersion;
    }

    public void setEnvVersion(String envVersion) {
        this.envVersion = envVersion;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }
}
