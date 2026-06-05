package com.lixin.capability.wxpusher.autoconfigure;

import com.lixin.capability.wxpusher.properties.WxPusherProperties;
import com.lixin.capability.wxpusher.provider.OfficialWxPusherProvider;
import com.lixin.capability.wxpusher.provider.OfficialWxPusherSdkGateway;
import com.lixin.capability.wxpusher.provider.WxPusherSdkGateway;
import com.lixin.capability.wxpusher.provider.WxPusherProvider;
import com.lixin.capability.wxpusher.service.DefaultWxPusherService;
import com.lixin.capability.wxpusher.service.WxPusherService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(WxPusherService.class)
@EnableConfigurationProperties(WxPusherProperties.class)
@ConditionalOnProperty(prefix = "lixin.capability.wxpusher", name = "enabled", havingValue = "true")
public class LixinWxPusherAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public WxPusherSdkGateway wxPusherSdkGateway(WxPusherProperties properties) {
        return new OfficialWxPusherSdkGateway(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public WxPusherProvider wxPusherProvider(WxPusherSdkGateway sdkGateway) {
        return new OfficialWxPusherProvider(sdkGateway);
    }

    @Bean
    @ConditionalOnMissingBean(WxPusherService.class)
    public WxPusherService wxPusherService(WxPusherProperties properties, WxPusherProvider provider) {
        return new DefaultWxPusherService(properties, provider);
    }
}
