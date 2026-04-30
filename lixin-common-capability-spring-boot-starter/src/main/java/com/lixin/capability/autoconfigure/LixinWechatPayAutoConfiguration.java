package com.lixin.capability.autoconfigure;

import com.lixin.capability.wechat.exception.WechatCapabilityConfigException;
import com.lixin.capability.wechat.pay.client.DefaultWechatPayClient;
import com.lixin.capability.wechat.pay.client.DefaultWechatPayNotifyClient;
import com.lixin.capability.wechat.pay.client.WechatPayClient;
import com.lixin.capability.wechat.pay.client.WechatPayNotifyClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "lixin.capability.wechat.pay", name = "enabled", havingValue = "true")
public class LixinWechatPayAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public WechatPayClient wechatPayClient(WechatCapabilityProperties properties) {
        validatePay(properties.getPay());
        return new DefaultWechatPayClient();
    }

    @Bean
    @ConditionalOnMissingBean
    public WechatPayNotifyClient wechatPayNotifyClient(WechatCapabilityProperties properties) {
        validatePay(properties.getPay());
        return new DefaultWechatPayNotifyClient();
    }

    private void validatePay(WechatCapabilityProperties.Pay pay) {
        if (isBlank(pay.getAppId()) || isBlank(pay.getMchId()) || isBlank(pay.getPrivateKeyPath())
                || isBlank(pay.getMerchantSerialNumber()) || isBlank(pay.getApiV3Key())) {
            throw new WechatCapabilityConfigException("Pay app-id, mch-id, private-key-path, merchant-serial-number, and api-v3-key are required when lixin.capability.wechat.pay.enabled=true.");
        }
    }

    private boolean isBlank(String value) { return value == null || value.trim().isEmpty(); }
}
