package com.lixin.capability.autoconfigure;

import com.lixin.capability.wechat.exception.WechatCapabilityConfigException;
import com.lixin.capability.wechat.pay.client.WechatPayClient;
import com.lixin.capability.wechat.pay.client.internal.WechatPayJsapiAdapter;
import com.lixin.capability.wechat.pay.client.internal.WechatPayNotifyAdapter;
import com.lixin.capability.wechat.pay.client.internal.WechatPayRefundAdapter;
import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.notification.NotificationParser;
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
                .withBean(NotificationParser.class, () -> mock(NotificationParser.class))
                .withBean(WechatPayJsapiAdapter.class, () -> mock(WechatPayJsapiAdapter.class))
                .withBean(WechatPayRefundAdapter.class, () -> mock(WechatPayRefundAdapter.class))
                .withBean(WechatPayNotifyAdapter.class, () -> mock(WechatPayNotifyAdapter.class))
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

    @Test
    void wrapsSdkConfigInitializationFailure() {
        contextRunner
                .withPropertyValues(
                        "lixin.capability.wechat.pay.enabled=true",
                        "lixin.capability.wechat.pay.app-id=test-app-id",
                        "lixin.capability.wechat.pay.mch-id=test-mch-id",
                        "lixin.capability.wechat.pay.private-key-path=/tmp/lixin-missing-private-key.pem",
                        "lixin.capability.wechat.pay.merchant-serial-number=test-serial",
                        "lixin.capability.wechat.pay.api-v3-key=secret-api-v3-key",
                        "lixin.capability.wechat.pay.notify-url=https://example.com/pay/notify")
                .run(context -> {
                    assertThat(context).hasFailed();
                    Throwable configFailure = findCause(context.getStartupFailure(), WechatCapabilityConfigException.class);
                    assertThat(configFailure)
                            .isInstanceOf(WechatCapabilityConfigException.class)
                            .hasMessageContaining("SDK config initialization failed")
                            .hasMessageContaining("private-key-path")
                            .hasMessageContaining("merchant-serial-number")
                            .hasMessageContaining("api-v3-key");
                    assertThat(configFailure.getMessage()).doesNotContain("secret-api-v3-key");
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

    private Throwable findCause(Throwable throwable, Class<? extends Throwable> type) {
        Throwable current = throwable;
        while (current != null) {
            if (type.isInstance(current)) {
                return current;
            }
            current = current.getCause();
        }
        return null;
    }
}
