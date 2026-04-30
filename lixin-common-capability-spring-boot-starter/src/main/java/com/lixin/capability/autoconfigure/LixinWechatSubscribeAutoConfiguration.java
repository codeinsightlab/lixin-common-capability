package com.lixin.capability.autoconfigure;

import com.lixin.capability.wechat.exception.WechatCapabilityConfigException;
import com.lixin.capability.wechat.subscribe.client.DefaultWechatSubscribeClient;
import com.lixin.capability.wechat.subscribe.client.WechatSubscribeClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "lixin.capability.wechat.subscribe", name = "enabled", havingValue = "true")
public class LixinWechatSubscribeAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public WechatSubscribeClient wechatSubscribeClient(WechatCapabilityProperties properties) {
        validateSubscribe(properties.getSubscribe());
        return new DefaultWechatSubscribeClient();
    }

    private void validateSubscribe(WechatCapabilityProperties.Subscribe subscribe) {
        if (isBlank(subscribe.getDefaultMiniProgramState()) || isBlank(subscribe.getDefaultLang())) {
            throw new WechatCapabilityConfigException("Subscribe default-mini-program-state and default-lang are required when lixin.capability.wechat.subscribe.enabled=true.");
        }
    }

    private boolean isBlank(String value) { return value == null || value.trim().isEmpty(); }
}
