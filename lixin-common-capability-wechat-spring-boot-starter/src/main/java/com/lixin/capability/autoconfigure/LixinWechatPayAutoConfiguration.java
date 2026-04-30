package com.lixin.capability.autoconfigure;

import com.lixin.capability.wechat.exception.WechatCapabilityConfigException;
import com.lixin.capability.wechat.pay.client.DefaultWechatPayClient;
import com.lixin.capability.wechat.pay.client.DefaultWechatPayNotifyClient;
import com.lixin.capability.wechat.pay.client.WechatPayClient;
import com.lixin.capability.wechat.pay.client.WechatPayNotifyClient;
import com.lixin.capability.wechat.pay.client.internal.OfficialWechatPayJsapiAdapter;
import com.lixin.capability.wechat.pay.client.internal.OfficialWechatPayNotifyAdapter;
import com.lixin.capability.wechat.pay.client.internal.OfficialWechatPayRefundAdapter;
import com.lixin.capability.wechat.pay.client.internal.WechatPayJsapiAdapter;
import com.lixin.capability.wechat.pay.client.internal.WechatPayNotifyAdapter;
import com.lixin.capability.wechat.pay.client.internal.WechatPayRefundAdapter;
import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.core.notification.AutoCertificateNotificationConfig;
import com.wechat.pay.java.core.notification.NotificationParser;
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
        try {
            return new RSAAutoCertificateConfig.Builder()
                    .merchantId(pay.getMchId())
                    .privateKeyFromPath(pay.getPrivateKeyPath())
                    .merchantSerialNumber(pay.getMerchantSerialNumber())
                    .apiV3Key(pay.getApiV3Key())
                    .build();
        } catch (RuntimeException e) {
            throw new WechatCapabilityConfigException(
                    "WeChat Pay SDK config initialization failed. Check private-key-path, merchant-serial-number, and api-v3-key configuration.",
                    e);
        }
    }

    @Bean
    @ConditionalOnMissingBean
    public WechatPayJsapiAdapter wechatPayJsapiAdapter(Config wechatPayConfig) {
        return new OfficialWechatPayJsapiAdapter(wechatPayConfig);
    }

    @Bean
    @ConditionalOnMissingBean
    public WechatPayRefundAdapter wechatPayRefundAdapter(Config wechatPayConfig) {
        return new OfficialWechatPayRefundAdapter(wechatPayConfig);
    }

    @Bean
    @ConditionalOnMissingBean
    public NotificationParser wechatPayNotificationParser(WechatCapabilityProperties properties) {
        WechatCapabilityProperties.Pay pay = properties.getPay();
        validatePay(pay);
        try {
            AutoCertificateNotificationConfig notificationConfig = new AutoCertificateNotificationConfig.Builder()
                    .merchantId(pay.getMchId())
                    .privateKeyFromPath(pay.getPrivateKeyPath())
                    .merchantSerialNumber(pay.getMerchantSerialNumber())
                    .apiV3Key(pay.getApiV3Key())
                    .build();
            return new NotificationParser(notificationConfig);
        } catch (RuntimeException e) {
            throw new WechatCapabilityConfigException(
                    "WeChat Pay notification parser initialization failed. Check private-key-path, merchant-serial-number, and api-v3-key configuration.",
                    e);
        }
    }

    @Bean
    @ConditionalOnMissingBean
    public WechatPayNotifyAdapter wechatPayNotifyAdapter(NotificationParser wechatPayNotificationParser) {
        return new OfficialWechatPayNotifyAdapter(wechatPayNotificationParser);
    }

    @Bean
    @ConditionalOnMissingBean
    public WechatPayClient wechatPayClient(WechatCapabilityProperties properties,
                                           WechatPayJsapiAdapter jsapiAdapter,
                                           WechatPayRefundAdapter refundAdapter) {
        WechatCapabilityProperties.Pay pay = properties.getPay();
        validatePay(pay);
        return new DefaultWechatPayClient(
                pay.getAppId(),
                pay.getMchId(),
                pay.getNotifyUrl(),
                pay.getRefundNotifyUrl(),
                jsapiAdapter,
                refundAdapter);
    }

    @Bean
    @ConditionalOnMissingBean
    public WechatPayNotifyClient wechatPayNotifyClient(WechatCapabilityProperties properties,
                                                       WechatPayNotifyAdapter notifyAdapter) {
        validatePay(properties.getPay());
        return new DefaultWechatPayNotifyClient(notifyAdapter);
    }

    private void validatePay(WechatCapabilityProperties.Pay pay) {
        if (pay == null || isBlank(pay.getAppId()) || isBlank(pay.getMchId()) || isBlank(pay.getPrivateKeyPath())
                || isBlank(pay.getMerchantSerialNumber()) || isBlank(pay.getApiV3Key()) || isBlank(pay.getNotifyUrl())) {
            throw new WechatCapabilityConfigException("Pay app-id, mch-id, private-key-path, merchant-serial-number, api-v3-key, and notify-url are required when lixin.capability.wechat.pay.enabled=true.");
        }
    }

    private boolean isBlank(String value) { return value == null || value.trim().isEmpty(); }
}
