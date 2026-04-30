package com.lixin.capability.autoconfigure;

import com.lixin.capability.wechat.exception.WechatCapabilityConfigException;
import com.lixin.capability.wechat.pay.client.DefaultWechatPayClient;
import com.lixin.capability.wechat.pay.client.DefaultWechatPayNotifyClient;
import com.lixin.capability.wechat.pay.client.WechatPayClient;
import com.lixin.capability.wechat.pay.client.WechatPayNotifyClient;
import com.lixin.capability.wechat.pay.client.internal.OfficialWechatPayJsapiAdapter;
import com.lixin.capability.wechat.pay.client.internal.WechatPayJsapiAdapter;
import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "lixin.capability.wechat.pay", name = "enabled", havingValue = "true")
public class LixinWechatPayAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public Config wechatPayConfig(WechatCapabilityProperties properties) {
        WechatCapabilityProperties.Pay pay = properties.getPay();
        validatePay(pay);
        return new RSAAutoCertificateConfig.Builder()
                .merchantId(pay.getMchId())
                .privateKeyFromPath(pay.getPrivateKeyPath())
                .merchantSerialNumber(pay.getMerchantSerialNumber())
                .apiV3Key(pay.getApiV3Key())
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    public WechatPayJsapiAdapter wechatPayJsapiAdapter(Config wechatPayConfig) {
        return new OfficialWechatPayJsapiAdapter(wechatPayConfig);
    }

    @Bean
    @ConditionalOnMissingBean
    public WechatPayClient wechatPayClient(WechatCapabilityProperties properties, WechatPayJsapiAdapter jsapiAdapter) {
        WechatCapabilityProperties.Pay pay = properties.getPay();
        validatePay(pay);
        return new DefaultWechatPayClient(pay.getAppId(), pay.getMchId(), pay.getNotifyUrl(), jsapiAdapter);
    }

    @Bean
    @ConditionalOnMissingBean
    public WechatPayNotifyClient wechatPayNotifyClient(WechatCapabilityProperties properties) {
        validatePay(properties.getPay());
        return new DefaultWechatPayNotifyClient();
    }

    private void validatePay(WechatCapabilityProperties.Pay pay) {
        if (pay == null || isBlank(pay.getAppId()) || isBlank(pay.getMchId()) || isBlank(pay.getPrivateKeyPath())
                || isBlank(pay.getMerchantSerialNumber()) || isBlank(pay.getApiV3Key()) || isBlank(pay.getNotifyUrl())) {
            throw new WechatCapabilityConfigException("Pay app-id, mch-id, private-key-path, merchant-serial-number, api-v3-key, and notify-url are required when lixin.capability.wechat.pay.enabled=true.");
        }
    }

    private boolean isBlank(String value) { return value == null || value.trim().isEmpty(); }
}
