package com.lixin.capability.autoconfigure;

import com.lixin.capability.wechat.exception.WechatCapabilityConfigException;
import com.lixin.capability.wechat.pay.client.WechatPayClient;
import com.lixin.capability.wechat.pay.client.internal.WechatPayJsapiAdapter;
import com.wechat.pay.java.core.Config;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class LixinWechatPayAutoConfigurationTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(LixinWechatAutoConfiguration.class));

    @Test
    void registersPayClientWhenEnabledAndRequiredConfigPresent() {
        contextRunner
                .withBean(Config.class, () -> mock(Config.class))
                .withBean(WechatPayJsapiAdapter.class, () -> mock(WechatPayJsapiAdapter.class))
                .withPropertyValues(requiredPayProperties())
                .run(context -> assertThat(context).hasSingleBean(WechatPayClient.class));
    }

    @Test
    void doesNotRegisterPayClientWhenDisabled() {
        contextRunner
                .withPropertyValues("lixin.capability.wechat.pay.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(WechatPayClient.class));
    }

    @Test
    void failsClearlyWhenRequiredConfigMissing() {
        contextRunner
                .withPropertyValues("lixin.capability.wechat.pay.enabled=true")
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                            .hasRootCauseInstanceOf(WechatCapabilityConfigException.class)
                            .hasMessageContaining("notify-url");
                });
    }

    private String[] requiredPayProperties() {
        return new String[] {
                "lixin.capability.wechat.pay.enabled=true",
                "lixin.capability.wechat.pay.app-id=test-app-id",
                "lixin.capability.wechat.pay.mch-id=test-mch-id",
                "lixin.capability.wechat.pay.private-key-path=/tmp/test-private-key.pem",
                "lixin.capability.wechat.pay.merchant-serial-number=test-serial",
                "lixin.capability.wechat.pay.api-v3-key=test-api-v3-key",
                "lixin.capability.wechat.pay.notify-url=https://example.com/pay/notify"
        };
    }
}
