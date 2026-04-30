package com.lixin.capability.autoconfigure;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@EnableConfigurationProperties(WechatCapabilityProperties.class)
@Import({
        LixinWechatMiniappAutoConfiguration.class,
        LixinWechatSubscribeAutoConfiguration.class,
        LixinWechatPayAutoConfiguration.class
})
public class LixinWechatAutoConfiguration {
}
