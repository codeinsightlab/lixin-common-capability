package com.lixin.capability.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "lixin.capability.wechat")
public class WechatCapabilityProperties {
    private Miniapp miniapp = new Miniapp();
    private Subscribe subscribe = new Subscribe();
    private Pay pay = new Pay();

    public Miniapp getMiniapp() { return miniapp; }
    public void setMiniapp(Miniapp miniapp) { this.miniapp = miniapp; }
    public Subscribe getSubscribe() { return subscribe; }
    public void setSubscribe(Subscribe subscribe) { this.subscribe = subscribe; }
    public Pay getPay() { return pay; }
    public void setPay(Pay pay) { this.pay = pay; }

    public static class Miniapp {
        private boolean enabled;
        private String appId;
        private String secret;
        private String token;
        private String aesKey;
        private Storage storage = new Storage();
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public String getAppId() { return appId; }
        public void setAppId(String appId) { this.appId = appId; }
        public String getSecret() { return secret; }
        public void setSecret(String secret) { this.secret = secret; }
        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
        public String getAesKey() { return aesKey; }
        public void setAesKey(String aesKey) { this.aesKey = aesKey; }
        public Storage getStorage() { return storage; }
        public void setStorage(Storage storage) { this.storage = storage; }
    }

    public static class Storage {
        private String type = "memory";
        private String keyPrefix = "lixin:wechat:miniapp";
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getKeyPrefix() { return keyPrefix; }
        public void setKeyPrefix(String keyPrefix) { this.keyPrefix = keyPrefix; }
    }

    public static class Subscribe {
        private boolean enabled;
        private String defaultMiniProgramState = "formal";
        private String defaultLang = "zh_CN";
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public String getDefaultMiniProgramState() { return defaultMiniProgramState; }
        public void setDefaultMiniProgramState(String defaultMiniProgramState) { this.defaultMiniProgramState = defaultMiniProgramState; }
        public String getDefaultLang() { return defaultLang; }
        public void setDefaultLang(String defaultLang) { this.defaultLang = defaultLang; }
    }

    public static class Pay {
        private boolean enabled;
        private String appId;
        private String mchId;
        private String privateKeyPath;
        private String merchantSerialNumber;
        private String apiV3Key;
        private String wechatPayPublicKeyPath;
        private String wechatPayPublicKeyId;
        private String notifyUrl;
        private String refundNotifyUrl;
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public String getAppId() { return appId; }
        public void setAppId(String appId) { this.appId = appId; }
        public String getMchId() { return mchId; }
        public void setMchId(String mchId) { this.mchId = mchId; }
        public String getPrivateKeyPath() { return privateKeyPath; }
        public void setPrivateKeyPath(String privateKeyPath) { this.privateKeyPath = privateKeyPath; }
        public String getMerchantSerialNumber() { return merchantSerialNumber; }
        public void setMerchantSerialNumber(String merchantSerialNumber) { this.merchantSerialNumber = merchantSerialNumber; }
        public String getApiV3Key() { return apiV3Key; }
        public void setApiV3Key(String apiV3Key) { this.apiV3Key = apiV3Key; }
        public String getWechatPayPublicKeyPath() { return wechatPayPublicKeyPath; }
        public void setWechatPayPublicKeyPath(String wechatPayPublicKeyPath) { this.wechatPayPublicKeyPath = wechatPayPublicKeyPath; }
        public String getWechatPayPublicKeyId() { return wechatPayPublicKeyId; }
        public void setWechatPayPublicKeyId(String wechatPayPublicKeyId) { this.wechatPayPublicKeyId = wechatPayPublicKeyId; }
        public String getNotifyUrl() { return notifyUrl; }
        public void setNotifyUrl(String notifyUrl) { this.notifyUrl = notifyUrl; }
        public String getRefundNotifyUrl() { return refundNotifyUrl; }
        public void setRefundNotifyUrl(String refundNotifyUrl) { this.refundNotifyUrl = refundNotifyUrl; }
    }
}
