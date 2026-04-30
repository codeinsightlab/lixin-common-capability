package com.lixin.capability.autoconfigure;

import com.lixin.capability.wechat.pay.client.internal.WechatPayJsapiAdapter;
import com.wechat.pay.java.core.Config;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

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

    @Test
    void bindsSubscribeProperties() {
        contextRunner
                .withPropertyValues(
                        "lixin.capability.wechat.subscribe.enabled=true",
                        "lixin.capability.wechat.subscribe.default-mini-program-state=developer",
                        "lixin.capability.wechat.subscribe.default-lang=en_US")
                .run(context -> {
                    WechatCapabilityProperties properties = context.getBean(WechatCapabilityProperties.class);
                    assertThat(properties.getSubscribe().isEnabled()).isTrue();
                    assertThat(properties.getSubscribe().getDefaultMiniProgramState()).isEqualTo("developer");
                    assertThat(properties.getSubscribe().getDefaultLang()).isEqualTo("en_US");
                });
    }


    @Test
    void bindsPayProperties() {
        contextRunner
                .withBean(Config.class, () -> mock(Config.class))
                .withBean(WechatPayJsapiAdapter.class, () -> mock(WechatPayJsapiAdapter.class))
                .withPropertyValues(
                        "lixin.capability.wechat.pay.enabled=true",
                        "lixin.capability.wechat.pay.app-id=pay-app-id",
                        "lixin.capability.wechat.pay.mch-id=pay-mch-id",
                        "lixin.capability.wechat.pay.private-key-path=/tmp/pay-private-key.pem",
                        "lixin.capability.wechat.pay.merchant-serial-number=pay-serial",
                        "lixin.capability.wechat.pay.api-v3-key=pay-api-v3-key",
                        "lixin.capability.wechat.pay.notify-url=https://example.com/pay/notify",
                        "lixin.capability.wechat.pay.refund-notify-url=https://example.com/pay/refund-notify")
                .run(context -> {
                    WechatCapabilityProperties properties = context.getBean(WechatCapabilityProperties.class);
                    assertThat(properties.getPay().isEnabled()).isTrue();
                    assertThat(properties.getPay().getAppId()).isEqualTo("pay-app-id");
                    assertThat(properties.getPay().getMchId()).isEqualTo("pay-mch-id");
                    assertThat(properties.getPay().getPrivateKeyPath()).isEqualTo("/tmp/pay-private-key.pem");
                    assertThat(properties.getPay().getMerchantSerialNumber()).isEqualTo("pay-serial");
                    assertThat(properties.getPay().getApiV3Key()).isEqualTo("pay-api-v3-key");
                    assertThat(properties.getPay().getNotifyUrl()).isEqualTo("https://example.com/pay/notify");
                    assertThat(properties.getPay().getRefundNotifyUrl()).isEqualTo("https://example.com/pay/refund-notify");
                });
    }

}
