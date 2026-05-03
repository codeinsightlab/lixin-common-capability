package com.lixin.capability.netease.im.autoconfigure;

import com.lixin.capability.netease.im.callback.DefaultNeteaseImCallbackVerifier;
import com.lixin.capability.netease.im.callback.NeteaseImCallbackVerifier;
import com.lixin.capability.netease.im.client.DefaultNeteaseImAccountClient;
import com.lixin.capability.netease.im.client.NeteaseImAccountClient;
import com.lixin.capability.netease.im.exception.NeteaseImConfigException;
import com.lixin.capability.netease.im.properties.NeteaseImCapabilityProperties;
import com.lixin.capability.netease.im.provider.netease.JdkNeteaseImHttpTransport;
import com.lixin.capability.netease.im.provider.netease.NeteaseImAccountProvider;
import com.lixin.capability.netease.im.provider.netease.NeteaseImHttpAccountProvider;
import com.lixin.capability.netease.im.provider.netease.NeteaseImHttpTransport;
import com.lixin.capability.netease.im.provider.netease.NeteaseImSignatureSupport;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(NeteaseImCapabilityProperties.class)
@ConditionalOnProperty(prefix = "lixin.capability.netease.im", name = "enabled", havingValue = "true")
public class LixinNeteaseImAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public NeteaseImSignatureSupport neteaseImSignatureSupport() {
        return new NeteaseImSignatureSupport();
    }

    @Bean
    @ConditionalOnMissingBean
    public NeteaseImHttpTransport neteaseImHttpTransport() {
        return new JdkNeteaseImHttpTransport();
    }

    @Bean
    @ConditionalOnMissingBean
    public NeteaseImAccountProvider neteaseImAccountProvider(NeteaseImCapabilityProperties properties,
                                                             NeteaseImHttpTransport httpTransport,
                                                             NeteaseImSignatureSupport signatureSupport) {
        validateProperties(properties);
        return new NeteaseImHttpAccountProvider(properties, httpTransport, signatureSupport);
    }

    @Bean
    @ConditionalOnMissingBean(NeteaseImAccountClient.class)
    public NeteaseImAccountClient neteaseImAccountClient(NeteaseImCapabilityProperties properties,
                                                         NeteaseImAccountProvider provider) {
        validateProperties(properties);
        return new DefaultNeteaseImAccountClient(provider);
    }

    @Bean
    @ConditionalOnMissingBean(NeteaseImCallbackVerifier.class)
    public NeteaseImCallbackVerifier neteaseImCallbackVerifier(NeteaseImCapabilityProperties properties,
                                                               NeteaseImSignatureSupport signatureSupport) {
        validateProperties(properties);
        return new DefaultNeteaseImCallbackVerifier(properties, signatureSupport);
    }

    private void validateProperties(NeteaseImCapabilityProperties properties) {
        if (properties == null) {
            throw new NeteaseImConfigException("Netease IM properties must not be null");
        }
        if (!hasText(properties.getAppKey())) {
            throw new NeteaseImConfigException("lixin.capability.netease.im.app-key is required when Netease IM is enabled");
        }
        if (!hasText(properties.getAppSecret())) {
            throw new NeteaseImConfigException("lixin.capability.netease.im.app-secret is required when Netease IM is enabled");
        }
        if (!hasText(properties.getBaseUrl())) {
            throw new NeteaseImConfigException("lixin.capability.netease.im.base-url is required when Netease IM is enabled");
        }
        if (properties.getTimeoutMillis() <= 0) {
            throw new NeteaseImConfigException("lixin.capability.netease.im.timeout-millis must be greater than 0");
        }
    }

    private boolean hasText(String value) {
        return value != null && value.trim().length() > 0;
    }
}
