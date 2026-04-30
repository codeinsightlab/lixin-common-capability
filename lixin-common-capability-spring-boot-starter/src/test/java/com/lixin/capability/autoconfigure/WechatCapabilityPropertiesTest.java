package com.lixin.capability.autoconfigure;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class WechatCapabilityPropertiesTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(LixinWechatAutoConfiguration.class));

    @Test
    void bindsMiniappProperties() {
        contextRunner
                .withPropertyValues(
                        "lixin.capability.wechat.miniapp.app-id=test-app-id",
                        "lixin.capability.wechat.miniapp.secret=test-secret",
                        "lixin.capability.wechat.miniapp.storage.type=memory",
                        "lixin.capability.wechat.miniapp.storage.key-prefix=test:prefix")
                .run(context -> {
                    WechatCapabilityProperties properties = context.getBean(WechatCapabilityProperties.class);
                    assertThat(properties.getMiniapp().getAppId()).isEqualTo("test-app-id");
                    assertThat(properties.getMiniapp().getSecret()).isEqualTo("test-secret");
                    assertThat(properties.getMiniapp().getStorage().getType()).isEqualTo("memory");
                    assertThat(properties.getMiniapp().getStorage().getKeyPrefix()).isEqualTo("test:prefix");
                });
    }
}
