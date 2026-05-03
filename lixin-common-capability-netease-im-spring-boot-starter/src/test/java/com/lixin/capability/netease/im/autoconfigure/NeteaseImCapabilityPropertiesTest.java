package com.lixin.capability.netease.im.autoconfigure;

import com.lixin.capability.netease.im.properties.NeteaseImCapabilityProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;

class NeteaseImCapabilityPropertiesTest {
    @Test
    void bindsProperties() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("lixin.capability.netease.im.enabled", "true")
                .withProperty("lixin.capability.netease.im.app-key", "app-key")
                .withProperty("lixin.capability.netease.im.app-secret", "app-secret")
                .withProperty("lixin.capability.netease.im.timeout-millis", "3000")
                .withProperty("lixin.capability.netease.im.base-url", "https://api.example.com/nimserver");

        NeteaseImCapabilityProperties properties = Binder.get(environment)
                .bind("lixin.capability.netease.im", Bindable.of(NeteaseImCapabilityProperties.class))
                .get();

        assertThat(properties.isEnabled()).isTrue();
        assertThat(properties.getAppKey()).isEqualTo("app-key");
        assertThat(properties.getAppSecret()).isEqualTo("app-secret");
        assertThat(properties.getTimeoutMillis()).isEqualTo(3000);
        assertThat(properties.getBaseUrl()).isEqualTo("https://api.example.com/nimserver");
    }

    @Test
    void hasSafeDefaults() {
        NeteaseImCapabilityProperties properties = new NeteaseImCapabilityProperties();

        assertThat(properties.isEnabled()).isFalse();
        assertThat(properties.getTimeoutMillis()).isEqualTo(10000);
        assertThat(properties.getBaseUrl()).isEqualTo("https://api.yunxinapi.com/nimserver");
    }
}
