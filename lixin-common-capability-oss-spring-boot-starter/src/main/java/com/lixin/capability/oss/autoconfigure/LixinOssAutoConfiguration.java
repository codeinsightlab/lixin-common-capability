package com.lixin.capability.oss.autoconfigure;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.lixin.capability.oss.client.DefaultLixinOssClient;
import com.lixin.capability.oss.client.LixinOssClient;
import com.lixin.capability.oss.exception.OssCapabilityConfigException;
import com.lixin.capability.oss.properties.OssCapabilityProperties;
import com.lixin.capability.oss.provider.aliyun.AliyunOssProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(OssCapabilityProperties.class)
@ConditionalOnProperty(prefix = "lixin.capability.oss", name = "enabled", havingValue = "true")
public class LixinOssAutoConfiguration {
    private static final String PROVIDER_ALIYUN = "aliyun";

    @Bean
    @ConditionalOnMissingBean
    public OSS aliyunOssSdkClient(OssCapabilityProperties properties) {
        validateProperties(properties);
        return new OSSClientBuilder().build(
                properties.getEndpoint(),
                properties.getAccessKeyId(),
                properties.getAccessKeySecret());
    }

    @Bean
    @ConditionalOnMissingBean
    public AliyunOssProvider aliyunOssProvider(OSS aliyunOssSdkClient) {
        return new AliyunOssProvider(aliyunOssSdkClient);
    }

    @Bean
    @ConditionalOnMissingBean(LixinOssClient.class)
    public LixinOssClient lixinOssClient(OssCapabilityProperties properties, AliyunOssProvider aliyunOssProvider) {
        validateProperties(properties);
        return new DefaultLixinOssClient(properties, aliyunOssProvider);
    }

    private void validateProperties(OssCapabilityProperties properties) {
        if (properties == null) {
            throw new OssCapabilityConfigException("OSS properties must not be null");
        }
        if (!hasText(properties.getProvider())) {
            throw new OssCapabilityConfigException("lixin.capability.oss.provider is required when OSS is enabled");
        }
        if (!PROVIDER_ALIYUN.equalsIgnoreCase(properties.getProvider())) {
            throw new OssCapabilityConfigException("OSS V1 only supports provider aliyun");
        }
        if (!hasText(properties.getEndpoint())) {
            throw new OssCapabilityConfigException("lixin.capability.oss.endpoint is required when OSS is enabled");
        }
        if (!hasText(properties.getBucketName())) {
            throw new OssCapabilityConfigException("lixin.capability.oss.bucket-name is required when OSS is enabled");
        }
        if (!hasText(properties.getAccessKeyId())) {
            throw new OssCapabilityConfigException("lixin.capability.oss.access-key-id is required when OSS is enabled");
        }
        if (!hasText(properties.getAccessKeySecret())) {
            throw new OssCapabilityConfigException("lixin.capability.oss.access-key-secret is required when OSS is enabled");
        }
        if (properties.getDefaultExpireSeconds() <= 0) {
            throw new OssCapabilityConfigException("lixin.capability.oss.default-expire-seconds must be greater than 0");
        }
    }

    private boolean hasText(String value) {
        return value != null && value.trim().length() > 0;
    }
}
