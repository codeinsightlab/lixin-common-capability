package com.lixin.capability.oss.autoconfigure;

import com.lixin.capability.oss.properties.OssCapabilityProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;

class OssCapabilityPropertiesTest {
    @Test
    void bindsOssProperties() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("lixin.capability.oss.enabled", "true")
                .withProperty("lixin.capability.oss.provider", "aliyun")
                .withProperty("lixin.capability.oss.endpoint", "https://oss-cn-hangzhou.aliyuncs.com")
                .withProperty("lixin.capability.oss.bucket-name", "test-bucket")
                .withProperty("lixin.capability.oss.access-key-id", "test-ak")
                .withProperty("lixin.capability.oss.access-key-secret", "test-sk")
                .withProperty("lixin.capability.oss.default-expire-seconds", "7200")
                .withProperty("lixin.capability.oss.object-key-prefix", "dev/");

        Binder binder = new Binder(ConfigurationPropertySources.get(environment));
        OssCapabilityProperties properties = binder.bind("lixin.capability.oss", Bindable.of(OssCapabilityProperties.class)).get();

        assertThat(properties.isEnabled()).isTrue();
        assertThat(properties.getProvider()).isEqualTo("aliyun");
        assertThat(properties.getEndpoint()).isEqualTo("https://oss-cn-hangzhou.aliyuncs.com");
        assertThat(properties.getBucketName()).isEqualTo("test-bucket");
        assertThat(properties.getAccessKeyId()).isEqualTo("test-ak");
        assertThat(properties.getAccessKeySecret()).isEqualTo("test-sk");
        assertThat(properties.getDefaultExpireSeconds()).isEqualTo(7200);
        assertThat(properties.getObjectKeyPrefix()).isEqualTo("dev/");
    }

    @Test
    void defaultsToDisabledAndOneHourExpiration() {
        OssCapabilityProperties properties = new OssCapabilityProperties();

        assertThat(properties.isEnabled()).isFalse();
        assertThat(properties.getDefaultExpireSeconds()).isEqualTo(3600);
    }
}
