package com.lixin.capability.autoconfigure;

import com.lixin.capability.wechat.subscribe.client.WechatSubscribeClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class LixinWechatSubscribeAutoConfigurationTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(LixinWechatAutoConfiguration.class));

    @Test
    void registersSubscribeClientWhenMiniappAndSubscribeEnabled() {
        contextRunner
                .withPropertyValues(
                        "lixin.capability.wechat.miniapp.enabled=true",
                        "lixin.capability.wechat.miniapp.app-id=test-app-id",
                        "lixin.capability.wechat.miniapp.secret=test-secret",
                        "lixin.capability.wechat.miniapp.storage.type=memory",
                        "lixin.capability.wechat.subscribe.enabled=true",
                        "lixin.capability.wechat.subscribe.default-mini-program-state=developer",
                        "lixin.capability.wechat.subscribe.default-lang=en_US")
                .run(context -> assertThat(context).hasSingleBean(WechatSubscribeClient.class));
    }

    @Test
    void doesNotRegisterSubscribeClientWhenDisabled() {
        contextRunner
                .withPropertyValues(
                        "lixin.capability.wechat.miniapp.enabled=true",
                        "lixin.capability.wechat.miniapp.app-id=test-app-id",
                        "lixin.capability.wechat.miniapp.secret=test-secret",
                        "lixin.capability.wechat.miniapp.storage.type=memory",
                        "lixin.capability.wechat.subscribe.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(WechatSubscribeClient.class));
    }

    @Test
    void failsClearlyWhenWxMaServiceMissing() {
        contextRunner
                .withPropertyValues(
                        "lixin.capability.wechat.miniapp.enabled=false",
                        "lixin.capability.wechat.subscribe.enabled=true",
                        "lixin.capability.wechat.subscribe.default-mini-program-state=formal",
                        "lixin.capability.wechat.subscribe.default-lang=zh_CN")
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                            .hasMessageContaining("WxMaService");
                });
    }
}
